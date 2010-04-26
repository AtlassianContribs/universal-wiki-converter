package com.atlassian.uwc.hierarchies;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

public class Many2OneHierarchy implements HierarchyBuilder {

	private static final String DEFAULT_EXEMPTION_PARENT = "Exemptions";
	Logger log = Logger.getLogger(this.getClass());
	Properties properties;
	HashMap<String, HierarchyNode> parents;
	
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		log.debug("Building Hierarchy");
		log.debug("Checking Pages object is valid.");
		//check that pages is valid and non-empty
		if (pages == null) {
			String message = "Cannot build hierarchy. Pages object is null.";
			log.warn(message);
			return null;
		}
		if (pages.isEmpty()) {
			String message = "Cannot build hierarchy. Pages object is empty.";
			log.warn(message);
			return null;
		}
		log.debug("Pages object is valid.");
		init();

		//instantiate the root object
		HierarchyNode root = new HierarchyNode();

		log.info("Building Hierarchy.");
		for (Page page : pages) {
			if (page == null) {
				log.debug("page is null!");
				continue;
			}
			buildRelationships(page, root);
		}
		return root;
	}

	 
	
	protected void buildRelationships(Page page, HierarchyNode root) {
		String parent = getParent(page);
		if (parent == null) return;
		HierarchyNode parentNode = null;
		if (isExempt(page)) {
			parent = getProperties().getProperty("hierarchy-exemption-parent", DEFAULT_EXEMPTION_PARENT); 
		}
		if (getParents().containsKey(parent)) 
			parentNode = getParents().get(parent);
		else {
			parentNode = new HierarchyNode();
			parents.put(parent, parentNode);
			root.addChild(parentNode);
		}
		parentNode.setName(parent);
		if (parentNode != null)
			new HierarchyNode(page, parentNode);
	}
	
	
	Pattern workspacename = Pattern.compile("" +
			"[/\\\\]" +
			"([^/\\\\]*)" +
			"[/\\\\][^/\\\\]*$");
	
	protected String getParent(Page page) {
		String path = page.getPath();
		Matcher workspaceFinder = workspacename.matcher(path);
		if (workspaceFinder.find()) {
			return workspaceFinder.group(1);
		}
		return null;
	}

	protected boolean isExempt(Page page) {
		return isExempt(page.getName());
	}
	protected boolean isExempt(String pagetitle) {
		String exemptRegex = getProperties().getProperty("many2one-exemption", null);
		if (exemptRegex == null) return false;
		Pattern p = Pattern.compile(exemptRegex);
		Matcher m = p.matcher(pagetitle);
		return m.find();
	}
	
	protected void init() {
		; //this can be shadowed by subclasses
	}
	
	public Properties getProperties() {
		if (properties == null)
			properties = new Properties();
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Map<String, HierarchyNode> getParents() {
		if (parents == null) 
			parents = new HashMap<String, HierarchyNode>();
		return parents;
	}

}
