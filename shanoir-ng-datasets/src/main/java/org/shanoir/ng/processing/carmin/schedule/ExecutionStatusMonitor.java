package org.shanoir.ng.processing.carmin.schedule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.processing.carmin.model.Execution;
import org.shanoir.ng.processing.carmin.model.ExecutionStatus;
import org.shanoir.ng.processing.carmin.output.DefaultOutputProcessing;
import org.shanoir.ng.processing.carmin.output.OutputProcessing;
import org.shanoir.ng.processing.carmin.service.CarminDatasetProcessingService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.security.KeycloakServiceAccountUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * CRON job to request VIP api and create processedDataset
 * 
 * @author KhalilKes
 */
@Service
public class ExecutionStatusMonitor implements ExecutionStatusMonitorService {

	@Value("${vip.uri}")
	private String VIP_URI;

	@Value("${vip.upload-folder}")
	private String importDir;

	@Value("${vip.sleep-time}")
	private long sleepTime;

	private ThreadLocal<Boolean> stop = new ThreadLocal<>();

	private String identifier;

	private String accessToken = "";

	private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitor.class);

	@Autowired
	private CarminDatasetProcessingService carminDatasetProcessingService;

	@Autowired
	private KeycloakServiceAccountUtils keycloakServiceAccountUtils;

	@Autowired
	private List<OutputProcessing> outputProcessings;

	@Async
	@Override
	@Transactional
	public void startJob(String identifier) throws EntityNotFoundException, SecurityException {
		int attempts = 1;
		this.identifier = identifier;

		stop.set(false);

		String uri = VIP_URI + identifier + "/summary";
		RestTemplate restTemplate = new RestTemplate();

		// check if the token is initialized
		if (this.accessToken.isEmpty()) {
			// refresh the token
			this.refreshServiceAccountAccessToken();
		}

		CarminDatasetProcessing carminDatasetProcessing = this.carminDatasetProcessingService
				.findByIdentifier(this.identifier)
				.orElseThrow(() -> new EntityNotFoundException(
						"entity not found with identifier :" + this.identifier));

		while (!stop.get()) {

			// init headers with the active access token
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + this.accessToken);
			HttpEntity entity = new HttpEntity(headers);

			// check how many times the loop tried to get the execution's info without success (only UNAUTHORIZED error)
			if(attempts >= 3){
				LOG.error("failed to get execution details in {} attempts.", attempts);
				LOG.error("Stopping the thread...");
				stop.set(true);
				break;
			}

			try {
				ResponseEntity<Execution> executionResponseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, Execution.class);
				Execution execution = executionResponseEntity.getBody();
				// init attempts due to successful response
				attempts = 1;
				switch (execution.getStatus()) {
				case FINISHED:
					/**
					 * updates the status and finish the job
					 */

					carminDatasetProcessing.setStatus(ExecutionStatus.FINISHED);

					this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);

					// untar the .tgz files
					final File userImportDir = new File(
							this.importDir + File.separator +
							carminDatasetProcessing.getResultsLocation());

					final PathMatcher matcher = userImportDir.toPath().getFileSystem()
							.getPathMatcher("glob:**/*.{tgz,tar.gz}");
					final Stream<java.nio.file.Path> stream = Files.list(userImportDir.toPath());

					OutputProcessing processing = this.getProcessingForPipeline(carminDatasetProcessing.getPipelineIdentifier());

					if(processing == null){
						LOG.error("No processing found for pipeline [{}].", carminDatasetProcessing.getPipelineIdentifier());
						stop.set(true);
						break;
					}

					stream.filter(matcher::matches)
					.forEach(zipFile -> {
						processing.manageTarGzResult(zipFile.toFile(), userImportDir.getAbsoluteFile(), carminDatasetProcessing);
					});

					LOG.info("execution status updated, stopping job...");

					stop.set(true);
					break;

				case UNKOWN:
				case EXECUTION_FAILED:
				case KILLED:

					carminDatasetProcessing.setStatus(execution.getStatus());
					this.carminDatasetProcessingService.updateCarminDatasetProcessing(carminDatasetProcessing);
					LOG.info("execution status updated, stopping job...");

					stop.set(true);
					break;

				case RUNNING:
					Thread.sleep(sleepTime); // sleep/stop a thread for 20 seconds
					break;

				default:
					stop.set(true);
					break;
				}
			} catch (HttpStatusCodeException e) {
				// in case of an error with response payload
				if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
					LOG.warn("Unauthorized");
					LOG.info("Getting new token...");
					this.refreshServiceAccountAccessToken();
					// inc attempts.
					attempts++;
				} else {
					LOG.error("error while getting execution info with status : {} ,and message :", e.getStatusCode(), e.getMessage());
					stop.set(true);
				}
			} catch (RestClientException e) {
				// in case of an error with no response payload
				LOG.error("there is no response payload while getting execution info", e);
				stop.set(true);
			} catch (InterruptedException e) {
				LOG.error("sleep thread exception :", e);
				stop.set(true);
			} catch (IOException e) {
				LOG.error("file exception :", e);
				stop.set(true);
			}
		}
	}

	private OutputProcessing getProcessingForPipeline(String identifier){
		OutputProcessing defaultProcessing = null;
		for(OutputProcessing processing : outputProcessings){
			if(processing instanceof DefaultOutputProcessing){
				defaultProcessing = processing;
			}else if (processing.doManage(identifier)){
				return processing;
			}
		}
		return defaultProcessing;
	}

	/**
	 * Get token from keycloak service account
	 * @return
	 */
	private void refreshServiceAccountAccessToken() throws SecurityException {
		AccessTokenResponse accessTokenResponse = keycloakServiceAccountUtils.getServiceAccountAccessToken();
		this.accessToken = accessTokenResponse.getToken();
	}
}
