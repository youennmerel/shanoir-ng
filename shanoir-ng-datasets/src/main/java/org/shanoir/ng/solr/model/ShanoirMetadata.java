/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

/**
 * NOTE: This class is auto generated by the swagger code generator program (2.2.3).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.shanoir.ng.solr.model;

import java.time.LocalDate;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;

import org.shanoir.ng.dataset.modality.MrDatasetNature;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

/**
 * @author yyao
 *
 */
@Entity
@SqlResultSetMapping(name = "SolrResult", classes = {@ConstructorResult(targetClass = ShanoirMetadata.class,
	columns = {@ColumnResult(name="datasetId", type = Long.class), @ColumnResult(name="datasetName", type = String.class),
			@ColumnResult(name="datasetType", type = Integer.class), @ColumnResult(name="datasetNature", type = Integer.class),
			@ColumnResult(name="datasetCreationDate", type = LocalDate.class), @ColumnResult(name="examinationId", type = Long.class),
			@ColumnResult(name="examinationComment", type = String.class),
			@ColumnResult(name="examinationDate", type = LocalDate.class), @ColumnResult(name="subjectName", type = String.class),
			@ColumnResult(name="subjectId", type = Long.class),
			@ColumnResult(name="studyName", type = String.class), @ColumnResult(name="studyId", type = Long.class),
			@ColumnResult(name="centerName", type = String.class), @ColumnResult(name="sliceThickness", type = Double.class),
			@ColumnResult(name="pixelBandwidth", type = Double.class), @ColumnResult(name="magneticFieldStrength", type = Double.class)
	})
})
public class ShanoirMetadata {
	
	@Id
	private	Long datasetId;
	
	private	String datasetName;
	
	// DatasetModalityType: MR, CT, PET etc..
	private	Integer datasetType;
	
	// T1, T2, Diff, etc..
	private Integer datasetNature;
	
	@LocalDateAnnotations
	private LocalDate datasetCreationDate;

	private Long examinationId;
	
	private String examinationComment;
	
	@LocalDateAnnotations
	private LocalDate examinationDate;
	
	private String subjectName;
	
	private String studyName;
	
	private Long studyId;
	
	private String centerName;
	
	private Double sliceThickness;
	
	private Double pixelBandwidth;
	
	private Double magneticFieldStrength;
	
	private Long subjectId;
	
	public ShanoirMetadata () {
		
	}
	
	public ShanoirMetadata (Long datasetId, String datasetName, Integer datasetType, Integer datasetNature,
			LocalDate datasetCreationDate, Long examinationId, String examinationComment, LocalDate examinationDate,
			String subjectName, Long subjectId, String studyName, Long studyId, String centerName, Double sliceThickness,
			Double pixelBandwidth, Double magneticFieldStrength) {
		this.datasetId = datasetId;
		this.datasetName = datasetName;
		this.datasetType = datasetType;
		this.datasetNature = datasetNature;
		this.datasetCreationDate = datasetCreationDate;
		this.examinationId = examinationId;
		this.examinationComment = examinationComment;
		this.examinationDate = examinationDate;
		this.subjectName = subjectName;
		this.subjectId = subjectId;
		this.studyName = studyName;
		this.studyId = studyId;
		this.centerName = centerName;
		this.sliceThickness = sliceThickness;
		this.pixelBandwidth = pixelBandwidth;
		this.magneticFieldStrength = magneticFieldStrength;
	}	
	
	/**
	 * @return the datasetId
	 */
	public Long getDatasetId() {
		return datasetId;
	}

	/**
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(Long datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * @return the datasetName
	 */
	public String getDatasetName() {
		return datasetName;
	}

	/**
	 * @param datasetName the datasetName to set
	 */
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	/**
	 * @return the datasetType
	 */
	public String getDatasetType() {
		if (DatasetModalityType.getType(datasetType) == null) {
			return "unknown";
		} else {
			return DatasetModalityType.getType(datasetType).toString();
		}
	}

	/**
	 * @param datasetType the datasetType to set
	 */
	public void setDatasetType(DatasetModalityType datasetType) {
		if (datasetType == null) {
			this.datasetType = null;
		} else {
			this.datasetType = datasetType.getId();
		}
	}

	/**
	 * @return the datasetNature
	 */
	public String getDatasetNature() {
		if (MrDatasetNature.getNature(datasetNature) == null) {
			return "unknown";
		} else {
			return MrDatasetNature.getNature(datasetNature).toString();
		}
	}

	/**
	 * @param datasetNature the datasetNature to set
	 */
	public void setDatasetNature(MrDatasetNature datasetNature) {
		if (datasetNature == null) {
			this.datasetNature = null;
		} else {
			this.datasetNature = datasetNature.getId();
		}
	}

	/**
	 * @return the datasetCreationDate
	 */
	public LocalDate getDatasetCreationDate() {
		return datasetCreationDate;
	}

	/**
	 * @param datasetCreationDate the datasetCreationDate to set
	 */
	public void setDatasetCreationDate(LocalDate datasetCreationDate) {
		this.datasetCreationDate = datasetCreationDate;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	/**
	 * @return the examinationComment
	 */
	public String getExaminationComment() {
		return examinationComment;
	}

	/**
	 * @param examinationComment the examinationComment to set
	 */
	public void setExaminationComment(String examinationComment) {
		this.examinationComment = examinationComment;
	}

	/**
	 * @return the examinationDate
	 */
	public LocalDate getExaminationDate() {
		return examinationDate;
	}

	/**
	 * @param examinationDate the examinationDate to set
	 */
	public void setExaminationDate(LocalDate examinationDate) {
		this.examinationDate = examinationDate;
	}

	/**
	 * @return the subjectName
	 */
	public String getSubjectName() {
		return subjectName;
	}

	/**
	 * @param subjectName the subjectName to set
	 */
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	/**
	 * @return the studyName
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public String getCenterName() {
		return centerName;
	}

	public void setCenterName(String centerName) {
		this.centerName = centerName;
	}

	public Double getSliceThickness() {
		return sliceThickness;
	}

	public void setSliceThickness(Double sliceThickness) {
		this.sliceThickness = sliceThickness;
	}

	public Double getPixelBandwidth() {
		return pixelBandwidth;
	}

	public void setPixelBandwidth(Double pixelBandwidth) {
		this.pixelBandwidth = pixelBandwidth;
	}

	public Double getMagneticFieldStrength() {
		return magneticFieldStrength;
	}

	public void setMagneticFieldStrength(Double magneticFieldStrength) {
		this.magneticFieldStrength = magneticFieldStrength;
	}

	/**
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}
	
}