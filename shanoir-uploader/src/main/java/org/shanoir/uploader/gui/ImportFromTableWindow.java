package org.shanoir.uploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.shanoir.ng.importer.dicom.ImagesCreatorAndDicomFileAnalyzerService;
import org.shanoir.ng.importer.model.ImportJob;
import org.shanoir.uploader.action.DownloadOrCopyActionListener;
import org.shanoir.uploader.action.ImportFromTableActionListener;
import org.shanoir.uploader.action.UpdateTableImportStudyListener;
import org.shanoir.uploader.action.UploadFromTableActionListener;
import org.shanoir.uploader.dicom.IDicomServerClient;
import org.shanoir.uploader.gui.customcomponent.JComboBoxMandatory;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFromTableWindow extends JFrame {

	private static final Logger logger = LoggerFactory.getLogger(ImportFromTableWindow.class);

    public JLabel studyLabel;
    public JComboBoxMandatory studyCB;

	public JButton uploadButton;
	public JButton openButton;
	public JProgressBar progressBar;

	public File shanoirUploaderFolder;
	public ResourceBundle resourceBundle;
	public JFrame frame;
	public JLabel error = new JLabel();
	public JLabel csvDetail = new JLabel();

	final JPanel masterPanel;

	JTable table;

	UploadFromTableActionListener uploadListener;
	ImportFromTableActionListener importListener;
	IDicomServerClient dicomServerClient;
	ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer;
	ShanoirUploaderServiceClient shanoirUploaderServiceClient;
	public JScrollPane scrollPaneUpload;
	UpdateTableImportStudyListener updateTableImportStudyListener;
	private DownloadOrCopyActionListener dOCAL;

	public ImportFromTableWindow(File shanoirUploaderFolder, ResourceBundle resourceBundle, JScrollPane scrollPaneUpload, IDicomServerClient dicomServerClient, ImagesCreatorAndDicomFileAnalyzerService dicomFileAnalyzer, ShanoirUploaderServiceClient shanoirUploaderServiceClientNG, DownloadOrCopyActionListener dOCAL) {
		this.updateTableImportStudyListener = new UpdateTableImportStudyListener(shanoirUploaderServiceClientNG, this);
		this.shanoirUploaderFolder = shanoirUploaderFolder;
		this.resourceBundle = resourceBundle;
		this.dicomServerClient = dicomServerClient;
		this.dicomFileAnalyzer = dicomFileAnalyzer;
		this.shanoirUploaderServiceClient = shanoirUploaderServiceClientNG;
		this.dOCAL = dOCAL;
		this.scrollPaneUpload = scrollPaneUpload;

		// Create the frame.
		frame = new JFrame(resourceBundle.getString("shanoir.uploader.import.table.title"));
		frame.setSize(1600, 700);
		this.setSize(1600, 700);

		// What happens when the frame closes?
		frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		// Panel content
		masterPanel = new JPanel(new GridBagLayout());
		masterPanel.setLayout(new GridBagLayout());
		masterPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		frame.setContentPane(masterPanel);

		// Study selection
        GridBagConstraints studyLabelGBC = new GridBagConstraints();
        studyLabel = new JLabel(resourceBundle.getString("shanoir.uploader.studyLabel") + " *");
        studyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        studyLabelGBC.anchor = GridBagConstraints.NORTHWEST;
        studyLabelGBC.gridx = 0;
        studyLabelGBC.gridy = 0;
        masterPanel.add(studyLabel, studyLabelGBC);
        studyLabel.setVisible(true);

        GridBagConstraints studyGBC = new GridBagConstraints();
        studyCB = new JComboBoxMandatory();
        studyCB.setBackground(Color.WHITE);
        studyGBC.anchor = GridBagConstraints.NORTHWEST;
        studyGBC.gridx = 0;
        studyGBC.gridy = 1;
        masterPanel.add(studyCB, studyGBC);
        studyCB.addItemListener(updateTableImportStudyListener);
        studyCB.setVisible(true);

		// Table description here
		this.csvDetail.setText(resourceBundle.getString("shanoir.uploader.import.table.detail"));
		GridBagConstraints gBCcsvDetail = new GridBagConstraints();
		gBCcsvDetail.anchor = GridBagConstraints.NORTHWEST;
		gBCcsvDetail.gridx = 0;
		gBCcsvDetail.gridy = 2;
		masterPanel.add(this.csvDetail, gBCcsvDetail);

		// Potential error here
		GridBagConstraints gBCError = new GridBagConstraints();
		gBCError.anchor = GridBagConstraints.NORTHWEST;
		gBCError.gridx = 0;
		gBCError.gridy = 3;
		this.error.setForeground(Color.RED);
		masterPanel.add(this.error, gBCError);

		// OPEN button here
		openButton = new JButton(resourceBundle.getString("shanoir.uploader.import.table.button.open"));
		GridBagConstraints gBCOpenButton = new GridBagConstraints();
		gBCOpenButton.anchor = GridBagConstraints.NORTHWEST;
		gBCOpenButton.gridx = 0;
		gBCOpenButton.gridy = 4;
		openButton.setEnabled(true);
		masterPanel.add(openButton, gBCOpenButton);

		uploadListener = new UploadFromTableActionListener(this, resourceBundle);
		openButton.addActionListener(uploadListener);

		this.updateTableImportStudyListener.updateTableImportForStudy();

		// headers for the table
		String[] columns = new String[] {
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.query.level"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.patient.name"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.patient.id"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.patient.birth.date"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.study.description"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.study.date"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.modality"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.filter.study.description"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.filter.study.min.date"),
			resourceBundle.getString("shanoir.uploader.import.table.column.dicom.filter.serie.description"),
			"PATIENT_VERIFICATION_FIRSTNAME",
			"PATIENT_VERIFICATION_LASTNAME",
			"PATIENT_VERIFICATION_BIRTHNAME",
			"PATIENT_VERIFICATION_BIRTHDATE",
			resourceBundle.getString("shanoir.uploader.import.table.column.studycard"),
			resourceBundle.getString("shanoir.uploader.import.table.column.common.name"),
			resourceBundle.getString("shanoir.uploader.import.table.column.comment"),
			resourceBundle.getString("shanoir.uploader.import.table.column.error")
		};
		// Create table with data
		table = new JTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setColumnIdentifiers(columns);
		// 1100 to go
		table.getColumnModel().getColumn(0).setMinWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(150);
		table.getColumnModel().getColumn(2).setMinWidth(130);
		table.getColumnModel().getColumn(3).setMinWidth(200);
		table.getColumnModel().getColumn(4).setMinWidth(200);
		table.getColumnModel().getColumn(5).setMinWidth(150);
		table.getColumnModel().getColumn(6).setMinWidth(120);
		table.getColumnModel().getColumn(7).setMinWidth(90);
		table.getColumnModel().getColumn(8).setMinWidth(30);
		table.getColumnModel().getColumn(9).setMinWidth(90);
		table.getColumnModel().getColumn(10).setMinWidth(350);

		// Add the table to the frame
		JPanel tablePanel = new JPanel(new BorderLayout());
		GridBagConstraints gBCTableanchor = new GridBagConstraints();
		gBCTableanchor.anchor = GridBagConstraints.NORTHWEST;
		gBCTableanchor.gridx = 0;
		gBCTableanchor.gridy = 5;
		tablePanel.setSize(1500, 500);
		masterPanel.add(tablePanel , gBCTableanchor);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setSize(1500, 500);
		scrollPane.setPreferredSize(new Dimension(1500, 500));
		table.getParent().setVisible(false);
		table.setSize(1500, 500);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tablePanel.add(scrollPane);

		// IMPORT button here when necessary
		uploadButton = new JButton(resourceBundle.getString("shanoir.uploader.import.table.button.import"));
		GridBagConstraints gBCuploadButton = new GridBagConstraints();
		gBCuploadButton.anchor = GridBagConstraints.NORTHWEST;
		gBCuploadButton.gridx = 0;
		gBCuploadButton.gridy = 6;
		uploadButton.setEnabled(false);
		masterPanel.add(uploadButton, gBCuploadButton);
		
		progressBar = new JProgressBar(0);
		GridBagConstraints gBCProgressBar = new GridBagConstraints();
		gBCProgressBar.anchor = GridBagConstraints.NORTHWEST;
		gBCProgressBar.gridx = 0;
		gBCProgressBar.gridy = 7;
		progressBar.setVisible(false);
		masterPanel.add(progressBar, gBCProgressBar);
	
		importListener = new ImportFromTableActionListener(this, resourceBundle, dicomServerClient, dicomFileAnalyzer, shanoirUploaderServiceClientNG, dOCAL);
		uploadButton.addActionListener(importListener);

		// center the frame
		// frame.setLocationRelativeTo( null );
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		int windowWidth = 1600;
		int windowHeight = 700;
		// set position and size
		frame.setBounds(center.x - windowWidth / 2, center.y - windowHeight / 2, windowWidth, windowHeight);

		// Show it.
		frame.setVisible(true);
	}

	/**
	 * Displays an error in the  located error field
	 * @param string the generated error to display
	 */
	public void displayError(String string) {
		this.error.setText(string);
		this.error.setVisible(true);
	}

	public void displayImportJobs(Map<String, ImportJob> importJobs) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.getDataVector().removeAllElements();
		boolean inError = false;
		for (ImportJob importJob : importJobs.values()) {
			String[] dicomQuery = importJob.getDicomQuery().displayDicomQuery();
			String[] patientVerification = importJob.getPatientVerification().displayPatientVerification();
			String[] importJobStrArray = {importJob.getStudyCardName(), importJob.getSubjectName(), importJob.getExaminationComment()};
			String[] combinedArray1 = Stream.concat(
				Arrays.stream(dicomQuery),
				Arrays.stream(patientVerification)).toArray(String[]::new);
			String[] combinedArray2 = Stream.concat(Arrays.stream(combinedArray1), Arrays.stream(importJobStrArray)).toArray(String[]::new);
			model.addRow(combinedArray2);

		}
		this.error.setVisible(inError);

		model.fireTableDataChanged();
		table.getParent().setVisible(true);
		this.importListener.setImportJobs(importJobs);

		uploadButton.setEnabled(!inError);
	}

}
