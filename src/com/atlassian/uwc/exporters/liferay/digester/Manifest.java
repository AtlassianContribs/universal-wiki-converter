package com.atlassian.uwc.exporters.liferay.digester;


public class Manifest {

	private String header = "head";	
	private ManifestPortlet portlet;
	
	public Manifest() {
	}
	
			
	public ManifestPortlet getPortlet() {
		return portlet;
	}

	public void setPortlet(ManifestPortlet portlet) {
		this.portlet = portlet;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return "Manifest [header=" + header + ", portlet=" + portlet + "]";
	}

}
