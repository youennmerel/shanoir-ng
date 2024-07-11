package org.shanoir.uploader.action;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.ng.importer.model.Patient;
import org.shanoir.ng.importer.model.Serie;
import org.shanoir.ng.importer.model.Study;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.ImportFromTableWindow;
import org.shanoir.uploader.model.CsvImport;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.IdList;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.upload.UploadJob;
import org.shanoir.uploader.upload.UploadJobManager;
import org.shanoir.uploader.upload.UploadState;
import org.shanoir.uploader.utils.ImportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFromCsvRunner extends SwingWorker<Void, Integer> {

	private static final String WILDCARD = "*";
	private static final String WILDCARD_REPLACE = "\\*";

	private static final Logger logger = LoggerFactory.getLogger(ImportFromCsvRunner.class);

	private List<CsvImport> csvImports;
	private ResourceBundle resourceBundle;
	private ImportFromTableWindow importFromTableWindow;
	private IdentifierCalculator identifierCalculator;
	private IDicomServerClient dicomServerClient;
	private ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
	private ShanoirUploaderServiceClient shanoirUploaderServiceClientNG;

	public ImportFromCsvRunner(List<CsvImport> csvImports, ResourceBundle ressourceBundle, ImportFromTableWindow importFromCSVWindow, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG) {
		this.csvImports = csvImports;
		this.resourceBundle = ressourceBundle;
		this.importFromTableWindow = importFromCSVWindow;
		this.identifierCalculator = new IdentifierCalculator();
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.shanoirUploaderServiceClientNG = shanoirUploaderServiceClientNG;
	}

	@Override
	protected Void doInBackground() throws Exception {
		// Iterate over import to import them one by one
		Set<Long> idList = new HashSet<>();
		Map<String, ArrayList<StudyCard>> studyCardsByStudy = new HashMap<>();

		importFromTableWindow.openButton.setEnabled(false);
		importFromTableWindow.uploadButton.setEnabled(false);

		importFromTableWindow.progressBar.setStringPainted(true);
		importFromTableWindow.progressBar.setString("Preparing import...");
		importFromTableWindow.progressBar.setVisible(true);

		// Get the list of studies, study card, center, equipments to check their existence
		for (CsvImport importTodo : this.csvImports) {
			idList.add(Long.valueOf(importTodo.getStudyId()));
			studyCardsByStudy.put(importTodo.getStudyId(), new ArrayList<StudyCard>());
		}
		List<org.shanoir.uploader.model.rest.Study> studies = new ArrayList<>();
		try {
			studies = shanoirUploaderServiceClientNG.findStudiesNamesAndCenters();

			IdList idealist = new IdList();
			idealist.setIdList(new ArrayList<>(idList));
			List<StudyCard> studyCards = shanoirUploaderServiceClientNG.findStudyCardsByStudyIds(idealist);
			if (studyCards == null) {
				throw new Exception(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
			}

			List<AcquisitionEquipment> acquisitionEquipments = shanoirUploaderServiceClientNG.findAcquisitionEquipments();
			if (acquisitionEquipments == null) {
				throw new Exception("Error while retrieving acquisition equipments");
			}

			// Iterate over study cards to get equipment + fill study => SC map
			for (StudyCard studyCard : studyCards) {
				Long acquisitionEquipmentIdSC = studyCard.getAcquisitionEquipmentId();
				for (Iterator<AcquisitionEquipment> acquisitionEquipmentsIt = acquisitionEquipments.iterator(); acquisitionEquipmentsIt.hasNext();) {
					AcquisitionEquipment acquisitionEquipment = (AcquisitionEquipment) acquisitionEquipmentsIt.next();
					if (acquisitionEquipment.getId().equals(acquisitionEquipmentIdSC)) {
						studyCard.setAcquisitionEquipment(acquisitionEquipment);
						studyCard.setCenterId(acquisitionEquipment.getCenter().getId());
						break;
					}
				}
				studyCardsByStudy.get(studyCard.getStudyId().toString()).add(studyCard);
			}
		} catch (Exception e1) {
			this.importFromTableWindow.error.setText(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
			return null;
		}

		boolean success = true;
		int i = 1;

		for (CsvImport importTodo : this.csvImports) {
			importFromTableWindow.progressBar.setString("Preparing import " + i + "/" + this.csvImports.size());
			importFromTableWindow.progressBar.setValue(100*i/this.csvImports.size() + 1);

			org.shanoir.uploader.model.rest.Study study = studies.stream().filter(element -> element.getId().toString().equals(importTodo.getStudyId())).findFirst().get();
			success = importData(importTodo, studyCardsByStudy.get(importTodo.getStudyId()), study ) && success;
			i++;
		}

		if (success) {
			importFromTableWindow.progressBar.setString("Success !");
			importFromTableWindow.progressBar.setValue(100);

			// Open current import tab and close csv import panel
			((JTabbedPane) this.importFromTableWindow.scrollPaneUpload.getParent().getParent()).setSelectedComponent(this.importFromTableWindow.scrollPaneUpload.getParent());

			this.importFromTableWindow.frame.setVisible(false);
			this.importFromTableWindow.frame.dispose();
		} else {
//			this.importFromCSVWindow.displayImportJobs(csvImports);
			importFromTableWindow.openButton.setEnabled(true);
			importFromTableWindow.uploadButton.setEnabled(false);
		}
		return null;
	}

	/**
	 * Loads data to shanoir NG
	 * @param csvImport the import
	 * @param studyCardsByStudy the list of study
	 * @param study2
	 * @return
	 */
	private boolean importData(CsvImport csvImport, List<StudyCard> studyCardsByStudy, org.shanoir.uploader.model.rest.Study study2) {

		// 1. Request PACS to check the presence of data
		logger.info("1 Request PACS");
		List<Patient> patients = null;
		try {
			if (!StringUtils.isEmpty(csvImport.getIpp())) {
				patients = dicomServerClient.queryDicomServer(false, "MR", "", csvImport.getIpp(), "", null, null);
			}
			if (patients == null) {
				String name = csvImport.getName().toUpperCase();
				if (!StringUtils.isEmpty(csvImport.getSurname())) {
					name+="^";
					name+=csvImport.getSurname().toUpperCase();
				}
				patients = dicomServerClient.queryDicomServer(false, "MR", name, "", "", null, null);
			}
		} catch (Exception e) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}

		// 2. Select series
		logger.info("2 Select series");
		List<Serie> selectedSeries = new ArrayList<>();

		Patient pat = null;
		Study stud = null;
		if (patients == null || patients.isEmpty()) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}
		String serialNumber = null;
		String modelName = null;
		
		Study selectedStudy = null;
		Map<Study, List<Serie>> selectedSeriesByStudy = new HashMap<>();

		LocalDate minDate;
		LocalDate selectedStudyDate = LocalDate.now();
		if (!StringUtils.isBlank(csvImport.getMinDateFilter())) {
			String[] acceptedFormats = {"yyyy","yyyy-MM-dd","yyyy-MM-dd-HH"};
			try {
				minDate = LocalDate.from(DateUtils.parseDate(csvImport.getMinDateFilter(), acceptedFormats).toInstant());
				selectedStudyDate = minDate;
			} catch (Exception e) {
				csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.date.format"));
				return false;
			}
		} else {
			Calendar cal = Calendar.getInstance();
			cal.set(1000, 0, 1, 0, 0, 0);
			minDate = LocalDate.from(cal.toInstant());
		}

		boolean foundPatient = false;
		for (Patient patient : patients) {
			if (foundPatient) {
				// Get only one patient => Once we've selected a serie with interesting data, do not iterate more
				break;
			}

			List<Study> studies = patient.getStudies();
			LocalDate currentDate = LocalDate.now();
			for (Study study : studies) {
				LocalDate studyDate = study.getStudyDate();
				if (studyDate.isAfter(currentDate) || studyDate.isAfter(minDate)) {
					// We take the first valid date, if we are after on valid date, don't check the data
					continue;
				}
				
				if (!searchField(study.getStudyDescription(), csvImport.getStudyFilter())) {
					continue;
				}
				stud = study;
				pat = patient;
				selectedSeriesByStudy.put(stud, new ArrayList<>());
				Collection<Serie> series = study.getSelectedSeries();
				for (Iterator<Serie> seriesIt = series.iterator(); seriesIt.hasNext();) {
					// Filter on serie
					Serie serie = seriesIt.next();
					if (searchField(serie.getSeriesDescription(), csvImport.getAcquisitionFilter())) {
						selectedSeriesByStudy.get(stud).add(serie);
						selectedStudy = stud;
						// TOOD: get these
						serialNumber = serie.getEquipment().getDeviceSerialNumber();
						modelName = serie.getEquipment().getManufacturerModelName();
						foundPatient = true;
						currentDate = studyDate;
						selectedStudyDate = studyDate;
					}
				}
			}
		}

		if (selectedStudy == null) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}
		selectedSeries = selectedSeriesByStudy.get(selectedStudy);
		if (selectedSeries.isEmpty()) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}
		
		/**
		 * For the moment the ImportFromCSVRunner processes line-by-line, study-by-study,
		 * so we only send one import job to the DownloadOrCopyRunnable, to download only
		 * one DICOM study, as the code after directly finishes the import of this study.
		 */
		HashMap<String, ImportJob> importJobs = new HashMap<String, ImportJob>();
		ImportJob importJob = ImportUtils.createNewImportJob(pat, selectedStudy);
		selectedSeries.stream().forEach(s -> importJob.getSelectedSeries().add(s));
		importJobs.put(importJob.getStudy().getStudyInstanceUID(), importJob);
		Runnable runnable = new DownloadOrCopyRunnable(true, dicomServerClient, dicomFileAnalyzer,  null, importJobs);
		Thread thread = new Thread(runnable);
		thread.start();

		// 3. Check existence of study / study card
		logger.info("3 Check study card");

		StudyCard sc = null;
		for (StudyCard studyc : studyCardsByStudy) {
			if (serialNumber != null && serialNumber.equals(studyc.getAcquisitionEquipment().getSerialNumber())) {
				sc = studyc;
				break;
			}
			if (modelName != null && modelName.equals(studyc.getAcquisitionEquipment().getManufacturerModel().getName())) {
				sc = studyc;
				break;
			}
		}

		// No study card by default => get the one in the file (if existing of course)
		if (sc == null) {
			Optional<StudyCard> scOpt = studyCardsByStudy.stream().filter(element -> element.getName().equals(csvImport.getStudyCardName())).findFirst();
			if (!scOpt.isPresent()) {
				csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.studycard"));
				return false;
			} else {
				sc = scOpt.get();
			}
		}

		logger.info("4 Complete data");
		Subject subject = new Subject();
		try {
			// Calculate identifier
			// #1609 we encounter problems when using same subject data across multiple studies.
			// We add study id in front of identifier to correct this
			String subjectIdentifier = stud.getStudyDate() +  this.identifierCalculator.calculateIdentifier(pat.getPatientFirstName(), pat.getPatientLastName(), pat.getPatientBirthDate().toString());
			subject.setIdentifier(subjectIdentifier);
			// Change birth date to first day of year
			final LocalDate dicomBirthDate = pat.getPatientBirthDate();
			if (dicomBirthDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(Date.from(dicomBirthDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				subject.setBirthDate(LocalDate.from(cal.toInstant()));
			}
		} catch (Exception e) {
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.missing.data"));
			return false;
		}

		Long centerId = sc.getCenterId();

		// 8.  Create subject if necessary
		Subject subjectFound = null;
		String subjectStudyIdentifier = null;
		try {
			subjectFound = shanoirUploaderServiceClientNG.findSubjectBySubjectIdentifier(subject.getIdentifier());
			if (!subjectFound.getName().equals(csvImport.getCommonName())) {
				// If the name does not match, change the subjectStudyIdentifier for this study
				subjectStudyIdentifier = csvImport.getCommonName();
			}
		} catch (Exception e) {
			//Do nothing, if it fails, we'll just create a new subject
		}
		
		if (subjectFound != null) {
			logger.info("8 Subject exists, just use it");
			subject = subjectFound;
			ImportUtils.addSubjectStudy(study2, subject, SubjectType.PATIENT, true, subjectStudyIdentifier);
			subject = shanoirUploaderServiceClientNG.createSubjectStudy(subject);
		} else {
			logger.info("8 Creating a new subject");
			subject = new org.shanoir.uploader.model.rest.Subject();
			subject.setName(csvImport.getCommonName());
			if (!StringUtils.isEmpty(pat.getPatientSex())) {
				subject.setSex(Sex.valueOf(pat.getPatientSex()));
			} else {
				// Force feminine (girl power ?)
				subject.setSex(Sex.F);
			}
			subject.setIdentifier(subject.getIdentifier());
			subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);
			subject.setBirthDate(subject.getBirthDate());
			ImportUtils.addSubjectStudy(study2, subject, SubjectType.PATIENT, true, subjectStudyIdentifier);
			// Get center ID from study card
			subject = shanoirUploaderServiceClientNG.createSubject(subject, true, centerId);
		}

		File uploadJobFile = new File(importJob.getWorkFolder() + File.separator + UploadJobManager.UPLOAD_JOB_XML);
		UploadJobManager uploadJobManager = new UploadJobManager(uploadJobFile);
		UploadJob uploadJob = uploadJobManager.readUploadJob();

		if (subject == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.subject"));
			return false;
		}

		// 9. Create examination
		logger.info("9 Create exam");

		Examination examDTO = new Examination();
		examDTO.setCenterId(centerId);
		examDTO.setComment(csvImport.getComment());
		examDTO.setExaminationDate(Date.from(selectedStudyDate.atStartOfDay().toInstant(ZoneOffset.UTC)));
		examDTO.setPreclinical(false);
		examDTO.setStudyId(Long.valueOf(csvImport.getStudyId()));
		examDTO.setSubjectId(subject.getId());
		Examination createdExam = shanoirUploaderServiceClientNG.createExamination(examDTO);

		if (createdExam == null) {
			uploadJob.setUploadState(UploadState.ERROR);
			csvImport.setErrorMessage(resourceBundle.getString("shanoir.uploader.import.csv.error.examination"));
			return false;
		}

		logger.info("10 Import.json");
		ImportUtils.prepareImportJob(importJob, subject.getName(), subject.getId(), createdExam.getId(), study2, sc);
		Runnable runnable2 = new ImportFinishRunnable(uploadJob, uploadJobFile.getParentFile(), importJob, subject.getName());
		Thread thread2 = new Thread(runnable2);
		thread2.start();

		return true;
	}

	/**
	 * This method allows to check if a filter with potentiel wildcard '*' is contained in the searched element
	 * @param searchedElement the string that is checked
	 * @param filter the filter we want to find
	 * @return true if the filter matches, false otherwise
	 */
	protected boolean searchField(String searchedElement, String filter) {
		if (StringUtils.isBlank(searchedElement)) {
			return false;
		}
		if (StringUtils.isBlank(filter) || filter.equals("*")) {
			return true;
		}
		String[] filters;
		boolean valid = true;
		// NB: It is possible to have AND ";" filters OR OR ";;" filters but not both at the same time for the moment.

		if (filter.contains(";;")) {
			valid = false;
			filters = filter.split(";;");
			for (String filterToApply : filters) {
				if (filterToApply.startsWith("!")) {
					valid =  valid || !filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				} else {
					valid =  valid || filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				}
			}
			return valid;
		} else if (filter.contains(";")) {
			filters = filter.split(";");
			for (String filterToApply : filters) {
				if (filterToApply.startsWith("!")) {
					valid =  valid && !filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				} else {
					valid =  valid && filterWildCard(searchedElement, filterToApply.replaceAll("!", ""));
				}
			}
			return valid;
		} else {
			if (filter.startsWith("!")) {
				valid = !filterWildCard(searchedElement, filter.replaceAll("!", ""));
			} else {
				valid = filterWildCard(searchedElement, filter.replaceAll("!", ""));
			}
			return valid;
		}
	}

	/*
	@Test
	public void testSearchField() {
		assertFalse(searchField("", ""));
		assertTrue(searchField("tested", ""));
		assertTrue(searchField("tested", "*"));
		assertTrue(searchField("tested", "*sted"));
		assertTrue(searchField("tested", "test*"));
		assertTrue(searchField("tested", "*est*"));
		assertFalse(searchField("tested", "*ast*"));
		assertTrue(searchField("tested", "*st*;*ed"));
		assertTrue(searchField("tested", "*st*;;*ed"));
		assertFalse(searchField("tested", "*sta*;*ed"));
		assertTrue(searchField("tested", "*sta*;;*ed"));
		assertFalse(searchField("tested", "*sta*;*tad*"));
		assertFalse(searchField("tested", "*sta*;;*tad*"));
	}
	*/

	/**
	 * Check if filterd elements contains or not the data sent in argument
	 * @param searchedElement
	 * @param filter
	 * @return
	 */
	private boolean filterWildCard(String searchedElement, String filter) {
		// Set all to uppercase
		searchedElement = searchedElement.toUpperCase();
		if(filter.endsWith(WILDCARD)) {
			if(filter.startsWith(WILDCARD)) {
				// *filter*
				return searchedElement.contains(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
			}
			// filter*
			return searchedElement.startsWith(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
		}
		if(filter.startsWith(WILDCARD)) {
			// *filter
			return searchedElement.endsWith(filter.replaceAll(WILDCARD_REPLACE, "").toUpperCase());
		}
		// filter
		return searchedElement.equalsIgnoreCase(filter);
	}

}
