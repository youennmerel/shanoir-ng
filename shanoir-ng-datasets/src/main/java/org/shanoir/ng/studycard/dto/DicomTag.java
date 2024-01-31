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

package org.shanoir.ng.studycard.dto;

import org.shanoir.ng.studycard.model.DicomTagType;

public class DicomTag {
	
	private int code;
	
	private String label;

	private DicomTagType type;

	public DicomTag(int code, String label, DicomTagType type) {
		this.code = code;
		this.label = label;
		this.type = type;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public DicomTagType getType() {
		return type;
	}

	public void setType(DicomTagType type) {
		this.type = type;
	}
}
