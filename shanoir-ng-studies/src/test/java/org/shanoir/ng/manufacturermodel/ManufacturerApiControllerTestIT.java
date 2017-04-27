package org.shanoir.ng.manufacturermodel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.utils.KeycloakControllerTestIT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Integration tests for manufacturer controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ManufacturerApiControllerTestIT extends KeycloakControllerTestIT {
	
	private static final String REQUEST_PATH = "/manufacturer";
	private static final String REQUEST_PATH_FOR_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void findManufacturerByIdProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_WITH_ID, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findManufacturerByIdWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_WITH_ID, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void findManufacturersProtected() {
		final ResponseEntity<String> response = restTemplate.getForEntity(REQUEST_PATH_FOR_ALL, String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void findManufacturersWithLogin() throws ClientProtocolException, IOException {
		final HttpEntity<String> entity = new HttpEntity<String>(getHeadersWithToken(true));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}
	
	@Test
	public void findManufacturersWithBadRole() {
		final HttpEntity<Manufacturer> entity = new HttpEntity<Manufacturer>(null, getHeadersWithToken(false));

		final ResponseEntity<String> response = restTemplate.exchange(REQUEST_PATH_FOR_ALL, HttpMethod.GET, entity,
				String.class);
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void saveNewManufacturerProtected() {
		final ResponseEntity<String> response = restTemplate.postForEntity(REQUEST_PATH, new Manufacturer(), String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

}