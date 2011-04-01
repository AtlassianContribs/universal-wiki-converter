package com.atlassian.uwc.ui;

public class Comment {

	public String text;
	public String creator;
	public String timestamp;//yyyy:MM:dd:HH:mm:ss:SS
	
	public Comment() {
		
	}
	
	public Comment(String text) {
		this.text = text;
	}
	
	public Comment(String text, String creator, String timestamp) {
		this(text);
		this.creator = creator;
		this.timestamp = timestamp;
	}

	public boolean hasCreator() {
		return creator != null;
	}

	public boolean hasTimestamp() {
		return timestamp != null;
	}
}
