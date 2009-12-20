package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.ui.Page;	

/**
 * uses TWiki Metatag to set parent-child relationships	
 * 
 * @author Jim Brandt, based on FilepathHierarchy by Laura Kolker
 * 
 */
public class TWikiHierarchy extends FilepathHierarchy {

	/**
	 * Creating a Hashmap for easier parent lookups.
	 * Maybe the pages List could be converted to a Map up the chain?
	 */
	private Map<String, Page> pageMap;
	
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		log.debug("Checking Pages object is valid.");
		//check that pages is valid and non-empty
		if (pages == null) {
			String message = "--> Cannot build hierarchy. Pages object is null.";
			log.debug(message);
			return null;
		}
		if (pages.isEmpty()) {
			String message = "--> Cannot build hierarchy. Pages object is empty.";
			log.debug(message);
			return null;
		}
		log.debug("--> Pages object is valid.");
		
		pageMap = new HashMap<String, Page>();
		
		for (Page page : pages){
			pageMap.put(page.getName(), page);
		}
		log.debug("Got "+pageMap.size()+" pages in the hash.");
		
		HierarchyNode root = getRootNode();
		log.info("Building Hierarchy.");
		log.debug("foreach Page in Pages...");
		for (Page page : pages) {
			if (page == null) {
				log.debug(".. page is null!"); 
				continue;
			}
			log.debug(".. page: " + page.getName());

			buildRelationships(page, root);
		}
		
		return root;
	}

	/**
	 * builds all the child parent relationships for the given page, 
	 * rooted in the given root node.
	 * @param page
	 * @param root
	 */
	protected void buildRelationships(Page page, HierarchyNode root) {
		log.debug(".. Building Relationships.");
		String currentPagePath = page.getPath();
		String currentPageName = page.getName();
		String extension = getFileExtension(currentPageName); //XXX Does TWikiHierarchy need this?
		Vector<String> emptyAncestors = new Vector<String>();

//		Vector<String> ancestorsNotRoot = getAncestors(currentPagePath);
		Vector<String> ancestorsNotRoot = getAncestors(page, emptyAncestors);
		
		HierarchyNode parent = root;
		for (String childString : ancestorsNotRoot) {
			if ("".equals(childString)) continue;
			log.debug(".... ancestor string = " + childString);
			HierarchyNode child = null;
			childString += extension; //XXX Does TWikiHierarchy need this?
			if (hasExistingRelationship(parent, childString)) 
				child = getChildNode(parent, childString);
			else 
				child = createChildNode(parent, childString);
			parent = child;
		}
		log.debug(".... creating leaf: " + currentPageName);
		if (hasExistingRelationship(parent, currentPageName)) {
			HierarchyNode child = getChildNode(parent, currentPageName);
			if (child == null) {
				String fullname = page.getPath() + 
						(page.getPath().endsWith(File.separator)?"":File.separator) + 
						page.getName();
				log.warn("Problem assigning page '" + fullname +
						"' to a node. Skipping.\n" +
						"NOTE: Check for duplicate page names, especially case sensitive " +
						"naming conventions. Confluence requires that all pages in the same space " +
						"have unique names, and case sensitive page titles will not be preserved.");
				return;
			}
			child.setPage(page);
		}
		else {
			createChildNode(parent, page);
		}
	}

	/**
	 * vector of path strings, in order
	 * @param path Example: Food/Fruit/Apple
	 * @return Vector of Strings:
	 * <br/>
	 * "Food", "Fruit", "Apple"
	 */
	protected Vector<String> getAncestors(Page page, Vector<String> currentAncestors) {
		log.debug("...... getting ancestors from: '" + page.getName() + "'");

		// Find %META:TOPICPARENT{name="ParentName"}%
	    Pattern twikiParentName =
	    	Pattern.compile("TOPICPARENT\\{name=\\\"(.*?)\\\"");

	    Matcher matcher = twikiParentName.matcher( page.getUnchangedSource() );
	    String parentName = "";

	    if( matcher.find() ){
	    	parentName = matcher.group(1);
	    	log.debug("After the match, got: "+parentName);
	    }
	    
	    if( !("".equals(parentName))){
	    	// We got a page name.
	    	if( parentName.equals(page.getName()) ){
	    		// Can't be your own parent and I don't think Confluence
	    		// allows two pages with the same file name.
				String message = "Parent cannot have the same name as current page. Skipping...";
				log.error(message);
	    	}
	    	else{
	    		// Add it to the list
	    		// then recurse to get the next parent.
	    		Page parentPage = pageMap.get(parentName);
	    		if( parentPage == null ){
	    			log.info("No parent page found for "+page.getName());
	    		}
	    		else{
	    			currentAncestors = getAncestors(parentPage, currentAncestors);
	    			currentAncestors.add(parentName);
	    		}
	    	}
	    }
		return currentAncestors;
	}
	 
}
