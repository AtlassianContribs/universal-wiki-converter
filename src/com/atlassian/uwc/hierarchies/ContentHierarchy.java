package com.atlassian.uwc.hierarchies;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ContentHierarchy implements HierarchyBuilder {

	Properties props = new Properties();
	Logger log = Logger.getLogger(this.getClass());

	/* Constants */
	public static final String PROP_PATTERN = "content-hierarchy-pattern";
	public static final String PROP_CURRENT = "content-hierarchy-pattern-includes-current";
	public static final String PROP_DELIM = "content-hierarchy-delim";
	public static final String PROP_ROOT = "content-hierarchy-default-root";
	private static final String PROP_HISTORY = "switch.page-history-preservation";
	private static final String PROP_HISTORY_SUFFIX = "suffix.page-history-preservation";
	public static final String DEFAULT_PATTERN = "\\{orig-title:([^}]*)\\}";
	public static final String DEFAULT_CURRENT = "true";
	public static final String DEFAULT_DELIM = "/";
	public static final String DEFAULT_ROOT = "";
	private static final String DEFAULT_HISTORY_SUFFIX = "-#";
	private static final String DEFAULT_HISTORY = "false";
	
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
		
		//set up root node
		HierarchyNode root = new HierarchyNode();
		root.setChildrenComparator(getVersionComparator());
		HierarchyNode pen = getPenultimateNode(getRootName(), root);
		
		log.info("Building Hierarchy.");
		boolean hasCurrent = getHasCurrent(); 
		log.debug("Creating Nodes...");
		//create a node for each page; ancestorstring -> node
		TreeMap<String, HierarchyNode> nodes = getAncestorNodeMap(pages, hasCurrent);

		//connect leaf to each parent
		log.debug("Building Connections...");
		for (Iterator iter = nodes.keySet().iterator(); iter.hasNext();) {
			//get node
			String hierarchy = (String) iter.next();
			HierarchyNode node = nodes.get(hierarchy);
			node.setName(node.getPage().getName());
			//get ancestors
			Vector<String> ancestors = getAncestors(hierarchy);
			log.debug(".. connecting node: " + node.getName());
			setTopLevelNodes(pen, node, ancestors);
			//go through each ancestor, find its node and connect them together
			HierarchyNode currentChild = node;
			for (int i = 0; i < ancestors.size(); i++) {
				String fullAncestor = ancestors.get(i);
				HierarchyNode ancestorNode = nodes.get(fullAncestor);
				if (ancestorNode == currentChild) continue;
				if (ancestorNode != null)
					log.debug(".... to node: " + ancestorNode.getName());
				//the addChild method handles redundant children for us
				ancestorNode.addChild(currentChild); 
				log.debug(".... has " + ancestorNode.getChildren().size() + " children");
				currentChild = ancestorNode;
			}
		}  
			
		return root;
	}

	private void setTopLevelNodes(HierarchyNode pen, HierarchyNode node, Vector<String> ancestors) {
		boolean isTopLevel = ancestors.size() == 1;
		if (isTopLevel) pen.addChild(node);
	}

	/**
	 * creates a map of ancestor keys -> node
	 * An ancestor key must be unique, so it represents the entire hierarchy/ancestor
	 * path until the node. If we're preserving history, we must maintain uniqueness by appending
	 * the page version to the ancestor key.
	 * @param pages
	 * @param hasCurrent
	 * @return
	 */
	private TreeMap<String, HierarchyNode> getAncestorNodeMap(Collection<Page> pages, boolean hasCurrent) {
		TreeMap<String,HierarchyNode> nodes = new TreeMap<String, HierarchyNode>();
		for (Page page : pages) {
			log.debug(".. creating node: " + page.getName() + " version: "+ page.getVersion());
			if (page == null) continue;
			HierarchyNode node = new HierarchyNode();
			node.setPage(page);
			node.setChildrenComparator(getVersionComparator());
			String hierarchy = getHierarchy(page);
			//if we don't have the current node's name in the ancestor path, add it
			if (!hasCurrent) { 
				if (!"".equals(hierarchy)) hierarchy += getDelim();
				hierarchy += page.getName();
			}
			//if we're preserving history, add version to preserve path uniqueness
			if (preservingHistory()) {
				String filename = page.getFile().getName();
				String suffix = getHistorySuffix();
				String version = getPageVersion(filename, suffix);
				if (!"".equals(hierarchy)) hierarchy += getDelim();
				hierarchy += version;

			}
			nodes.put(hierarchy, node);
		}
		return nodes;
	}

	private HierarchyNode getPenultimateNode(String rootname, HierarchyNode root) {
		HierarchyNode pen;
		if (rootname != null && !"".equals(rootname)) {
			log.debug("Page root set to: " + rootname);
			pen = new HierarchyNode();
			pen.setName(rootname);
			root.addChild(pen);
		}
		else pen = root;
		pen.setChildrenComparator(getVersionComparator());
		return pen;
	}

	private static VersionComparator versionComparator;
	private Comparator getVersionComparator() {
		if (versionComparator == null) versionComparator = new VersionComparator();
		return versionComparator;
	}

	/**
	 * @return the root name for the hierarchy. Can be set with property 
	 * "content-hierarchy-default-root". If blank top level pages will be given no parent, 
	 * ie. will be an orphan page, and sibling to Home
	 */
	protected String getRootName() {//provided by the misc props framework
		return getProperties().getProperty(PROP_ROOT, DEFAULT_ROOT);
	}

	private boolean getHasCurrent() {//provided by the misc props framework
		return Boolean.parseBoolean(getProperties().getProperty(PROP_CURRENT, DEFAULT_CURRENT));
	}
	
	private String getDelim() { //provided by the misc props framework
		return getProperties().getProperty(PROP_DELIM, DEFAULT_DELIM);
	}
	
	private boolean preservingHistory() { //provided by the ConverterEngine/page histories framework
		return Boolean.parseBoolean(getProperties().getProperty(PROP_HISTORY, DEFAULT_HISTORY));
	}
	
	private String getHistorySuffix() { //provided by the ConverterEngine/page histories framework
		return getProperties().getProperty(PROP_HISTORY_SUFFIX, DEFAULT_HISTORY_SUFFIX);
	}
	
	/**
	 * find the hierarchy from inside the page content, using the
	 * property "content-hierarchy-pattern", a regex whose group 1 should be the ancestor string 
	 * @param page
	 * @return
	 */
	protected String getHierarchy(Page page) {
		String patternString = getProperties().getProperty(PROP_PATTERN, DEFAULT_PATTERN);
		Pattern pattern;
		Matcher finder;
		try {
			pattern = Pattern.compile(patternString);
			finder = pattern.matcher(page.getUnchangedSource());
			if (finder.find()) {
				try {
					return finder.group(1); //the pattern must have a group 1
				} catch (IndexOutOfBoundsException e) {
					String msg = "BAD_PROPERTY: Regular Expression '" + patternString + "' could not be used. " +
					"Make sure it has at least one group. Cause: " + e.getMessage();
					log.error(msg);
					throw new IllegalArgumentException(msg, e);
				}
			}			
		} catch (Exception e) {
			String msg = "BAD_PROPERTY: Regular Expression '" + patternString + "' could not be compiled. " +
					"Cause: " + e.getMessage();
			log.error(msg);
			throw new IllegalArgumentException(msg, e);
		}
		return null;
	}

	/**
	 * Split given hierarchy string into ancestor strings, such that
	 * each ancestor string contains it's entire ancestor set as well as itself.
	 * The delimiter can be set using the property: "content-hierarchy-delim".
	 * @param hierarchy 
	 * @return set of ancestor strings
	 * Example. Hierarchy: A/B/C <br/>
	 * would return A/B/C, A/B, A
	 */
	protected Vector<String> getAncestors(String hierarchy) {
		String delim = getProperties().getProperty(PROP_DELIM, DEFAULT_DELIM);
		Vector<String> ancestors = new Vector<String>();
		//spilt hierarchy using delim
		Pattern delimPattern = Pattern.compile("" +
				"[^" +
					"\\Q"+delim+"\\E" +
				"]*");
		Matcher delimFinder = delimPattern.matcher(hierarchy);
		while (delimFinder.find()) {
			String ancestor = delimFinder.group().trim();
			if (ancestor == null || "".equals(ancestor)) continue;
			ancestors.add(ancestor);
		}
		//create unique ancestor keys. 
		String current = "";
		int max = ancestors.size();
		for (int i = 0; i < max; i++) {
			String ancestor = (String) ancestors.get(i);
			String full = ancestor;
			//build this ancestor's key using its ancestors
			if (!"".equals(current)) {
				current += delim;
				full = current + ancestor;
				ancestors.set(i, full);
			}
			//if preserving history, we must maintain uniqueness
			//by using version numbers. When version is unknown, 1 is sufficient
			if (preservingHistory() && !full.matches(".*\\/\\d+$")) {
				full += delim + "1";
				ancestors.set(i, full);
			}
			//remember this ancestor key for next time
			current += ancestor;
		}
		
		//We need the ancestor keys in descending order
		Vector<String> descending = new Vector<String>();
		current = "";
		for (int i = max-1; i >= 0; i--) {
			String ancestor = (String) ancestors.get(i);
			
			//double check we didn't end up with duplicates due to preserving history
			if (ancestor.equals(current)) continue;
			if (ancestor.replaceFirst("\\d+$", "").equals(current.replaceFirst("\\d+$", "")))
				continue;
			current = ancestor;
			
			descending.add(ancestor);
		}
		
		return descending;
	}
	
	Pattern hashPattern = Pattern.compile("#+");
	/**
	 * @param filename
	 * @param suffix
	 * @return the version represented in the filename, using suffix as a pattern
	 */
	protected String getPageVersion(String filename, String suffix) {
		Matcher hashFinder = hashPattern.matcher(suffix);
    	String suffixReplaceRegex = "";
    	if (hashFinder.find()) {
    		suffixReplaceRegex = hashFinder.replaceAll("(\\\\d+)");
    		suffixReplaceRegex = "(.*)" + suffixReplaceRegex;
    	} 
    	Pattern suffixReplacePattern = Pattern.compile(suffixReplaceRegex);
    	Matcher suffixReplacer = suffixReplacePattern.matcher(filename);
    	if (suffixReplacer.find()) {
    		return suffixReplacer.group(2);
    	}
    	return null;
	}

	public Properties getProperties() {
		if (props == null)
			props = new Properties();
		return props;
	}

	public void setProperties(Properties properties) {
		this.props = properties;
	}

	public class VersionComparator implements Comparator {

		public int compare(Object o1, Object o2) {
			HierarchyNode n1 = (HierarchyNode) o1;
			HierarchyNode n2 = (HierarchyNode) o2;
			String name1 = n1.getName();
			String name2 = n2.getName();
			log.debug("comparing: " + name1 + " and " + name2);
			int nameCompare = name1.compareTo(name2);
			if (nameCompare != 0) return nameCompare;
			int v1 = n1.getPage().getVersion();
			int v2 = n2.getPage().getVersion();
			return v1 - v2;
		}
		
	}
}
