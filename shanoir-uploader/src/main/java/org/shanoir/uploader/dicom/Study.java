package org.shanoir.uploader.dicom;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "id", "name" })
public class Study {

	private int id;

	private String name;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}

}
