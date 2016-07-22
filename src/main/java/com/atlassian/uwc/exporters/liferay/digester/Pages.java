package com.atlassian.uwc.exporters.liferay.digester;

import java.util.ArrayList;



public class Pages {

	private ArrayList<Page> pageList = new ArrayList<Page>();;
	
	public Pages() {
	}	
			
	public ArrayList<Page> getPageList() {
		return pageList;
	}

	public void setPage(Page portlet) {
		this.pageList.add(portlet);
	}


	@Override
	public String toString() {
		return "Pages [pageList=" + pageList + "]";
	}


}
