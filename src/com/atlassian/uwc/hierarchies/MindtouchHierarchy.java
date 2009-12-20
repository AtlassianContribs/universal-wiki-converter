package com.atlassian.uwc.hierarchies;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

public class MindtouchHierarchy extends FilepathHierarchy {

	Logger log = Logger.getLogger(this.getClass());
	
	HashMap<String,String> realtitles;
	
	protected boolean hasExistingRelationship(HierarchyNode parent, String childname) {
		childname = childname.replaceFirst("_subpages$", "");
		if (getRealTitles().containsKey(childname)) childname = getRealTitles().get(childname);
		else childname = childname.replaceFirst("^\\d+_", "");
		
		return super.hasExistingRelationship(parent, childname);
	}
	
	protected HierarchyNode getChildNode(HierarchyNode parent, String childname) {
		childname = childname.replaceFirst("_subpages$", "");
		if (getRealTitles().containsKey(childname)) childname = getRealTitles().get(childname);
		else childname = childname.replaceFirst("^\\d+_", "");
		return super.getChildNode(parent, childname);
	}
	
	protected String getOrigPagename(Page page, String currentPageName, String extension) {
		String orig = super.getOrigPagename(page, currentPageName, extension);
		getRealTitles().put(orig, currentPageName);
		return orig;
	}
	
	private HashMap<String,String> getRealTitles() {
		if (this.realtitles == null)
			this.realtitles = new HashMap<String, String>();
		return this.realtitles;
	}
}
