package com.atlassian.uwc.ui;

import java.io.File;

public class Attachment {

	private String name;
	private File file;
	
	public Attachment(File file) {
		this.file = file;
	}
	public Attachment(File file, String name) {
		this.file = file;
		this.name = name;
	}
	
	public String getName() {
		if (this.name != null) return this.name;
		return this.file.getName();
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	
}
