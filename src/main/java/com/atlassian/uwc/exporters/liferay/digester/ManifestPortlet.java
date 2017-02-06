package com.atlassian.uwc.exporters.liferay.digester;



public class ManifestPortlet {

	private String path = "path1";

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Portlet [path=" + path + "]";
	}

}
