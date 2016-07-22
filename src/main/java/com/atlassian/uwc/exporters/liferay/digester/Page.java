package com.atlassian.uwc.exporters.liferay.digester;

import java.util.ArrayList;



public class Page {

	private String path = "pathX";	
	private ArrayList<Attachment> attachments = new ArrayList<Attachment>();;
	
	public Page() {
	}	
			
	public ArrayList<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachment(Attachment portlet) {
		this.attachments.add(portlet);
	}
	

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Page [path=" + path + ", attachments=" + attachments + "]";
	}

}
