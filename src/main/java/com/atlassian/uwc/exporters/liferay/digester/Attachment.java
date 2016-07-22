package com.atlassian.uwc.exporters.liferay.digester;



public class Attachment {

	private String binPath = "b-pathX";
	private String name;	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBinPath() {
		return binPath;
	}

	public void setBinPath(String binPath) {
		this.binPath = binPath;
	}

	@Override
	public String toString() {
		return "Attachment [name=" + name + "]";
	}


}
