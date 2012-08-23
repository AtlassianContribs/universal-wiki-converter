package com.atlassian.uwc.exporters.liferay.digester;



public class WikiData {

	private String header = "head";	
	private Pages pages;
	
	public WikiData() {
	}
	
			
	public Pages getPages() {
		return pages;
	}

	public void setPages(Pages portlet) {
		this.pages = portlet;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	@Override
	public String toString() {
		return "WikiData [header=" + header + ", pages=" + pages + "]";
	}
		

}
