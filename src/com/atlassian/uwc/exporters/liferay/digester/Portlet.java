package com.atlassian.uwc.exporters.liferay.digester;



public class Portlet {

	private String header = "head";	
	private PortletData portlet;
	
	public Portlet() {
	}
	
			
	public PortletData getPortlet() {
		return portlet;
	}

	public void setPortlet(PortletData portlet) {
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
		return "Portlet [header=" + header + ", portlet=" + portlet + "]";
	}

}
