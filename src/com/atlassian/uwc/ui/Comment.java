package com.atlassian.uwc.ui;

public class Comment {

	public String text;
	public String creator;
	public String timestamp;//yyyy:MM:dd:HH:mm:ss:SS
	//by default we set this to false. When false the engine will ask Confluence to transform markup to xhtml
	private boolean isXhtml = false; 
	
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
	
	public Comment(String text, String creator, String timestamp, boolean isXhtml) {
		this(text);
		this.creator = creator;
		this.timestamp = timestamp;
		this.isXhtml = isXhtml;
	}

	public boolean hasCreator() {
		return creator != null;
	}

	public boolean hasTimestamp() {
		return timestamp != null;
	}

	public boolean isXhtml() {
		return this.isXhtml; 
	}
}
