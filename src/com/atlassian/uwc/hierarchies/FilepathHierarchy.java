package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

/**
 * uses pages' path to set parent-child relationships	
 * 
 * @author Laura Kolker
 * 
 */
public class FilepathHierarchy implements HierarchyBuilder {

	private static final String PROPKEY_EXT = "filepath-hierarchy-ext";
	private static final String PROPKEY_MATCHPAGENAME = "filepath-hierarchy-matchpagename";
	private static final String DEFAULT_MATCHPAGENAME = "true";
	Logger log = Logger.getLogger(this.getClass());
	private HierarchyNode root;
	private String fileExtension = null;
	private String ignorableAncestors = "";
	private String matchpagename = "";
	Properties properties = new Properties();
	
	
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
		
		//debug messages for properties
		log.debug("Filepath properties - ignorable-ancestors: " + 
				getProperties().getProperty("filepath-hierarchy-ignorable-ancestors"));
		log.debug("Filepath properties - ext: " + 
				getProperties().getProperty(PROPKEY_EXT, ""));
		log.debug("Filepath properties - matchpagename: "+
				getProperties().getProperty(PROPKEY_MATCHPAGENAME, DEFAULT_MATCHPAGENAME));
		this.matchpagename = getProperties().getProperty(PROPKEY_MATCHPAGENAME, DEFAULT_MATCHPAGENAME);
		
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
		String extension = getFileExtension(currentPageName);
		String origPageName = getOrigPagename(page, currentPageName, extension);

		Vector<String> ancestorsNotRoot = getAncestors(currentPagePath);
		
		HierarchyNode parent = root;
		for (String childString : ancestorsNotRoot) {
			if ("".equals(childString)) continue;
			log.debug(".... ancestor string = " + childString);
			HierarchyNode child = null;
			childString += extension;
			if (hasExistingRelationship(parent, childString)) 
				child = getChildNode(parent, childString);
			else 
				child = createChildNode(parent, childString);
			parent = child;
		}
		log.debug(".... creating leaf: " + currentPageName);
		if (hasExistingRelationship(parent, origPageName)) {
			HierarchyNode child = getChildNode(parent, origPageName);
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
			if (!origPageName.equals(currentPageName))
				child.setName(currentPageName);
			child.setPage(page);
		}
		else {
			createChildNode(parent, page);
		}
	}

	protected String getOrigPagename(Page page, String currentPageName, String extension) {
		String origPageName = (page.getFile() != null)?page.getFile().getName():currentPageName;
		String end = "[.][^.]+$";				//any ext
		origPageName = origPageName.replaceFirst(end, extension);
		if ("".equals(origPageName)) origPageName = currentPageName; //unit tests
		return origPageName;
	}

	/**
	 * vector of path strings, in order
	 * @param path Example: Food/Fruit/Apple
	 * @return Vector of Strings:
	 * <br/>
	 * "Food", "Fruit", "Apple"
	 */
	protected Vector<String> getAncestors(String path) {
		Properties props = getProperties();
		if (props.containsKey("filepath-hierarchy-ignorable-ancestors")) {
			String ignorable = props.getProperty("filepath-hierarchy-ignorable-ancestors");
			path = removePrefix(path, ignorable, File.separator); 
		}
		log.debug("...... getting ancestors from: '" + path + "'");
		String seperator = getSeperator();
		String[] ancArray = path.split(seperator);
		Vector<String> ancestors = new Vector<String>(ancArray.length);
		for (int i = 0; i < ancArray.length; i++) {
			String ancestor = ancArray[i];
			ancestors.add(ancestor);
		}
		return ancestors;
	}

	protected String removePrefix(String fullpath, String prefix, String separator) {
		if (prefix.endsWith(separator) && !fullpath.endsWith(separator)) fullpath += separator; 
		if (fullpath.startsWith(prefix)) fullpath = fullpath.replaceFirst("\\Q"+prefix+"\\E", "");
		if (fullpath.startsWith(separator)) fullpath = fullpath.substring(1);
		log.debug("removed prefix: " + fullpath);
		return fullpath;
	}
	/**
	 * @param parent
	 * @param childname
	 * @return true if parent node has a child with the given childname
	 */
	protected boolean hasExistingRelationship(HierarchyNode parent, String childname) {
		if (parent == null) {
			log.debug("...... -> parent is null.");
			return false;
		}
		
		log.debug("...... checking parent '" + parent.getName() + "' has relationship with '" + childname + "'");
		boolean relationship = false;
		
		Collection<HierarchyNode> children = parent.getChildren();
		if (children == null || children.isEmpty()) {
			log.debug("...... -> parent has no children");
			return false;
		}
		
		HierarchyNode child = null;
		if ("true".equals(matchpagename))
			child = parent.findChildByFilename(childname);
		else
			child = parent.findChild(childname);
		if (child == null) relationship = false;
		else relationship = true;
		
		log.debug("...... -> " + relationship);
		return relationship;
	}
	/**
	 * creates a node, with the parent and name info set.
	 * @param parent
	 * @param childname
	 * @return new node
	 */
	protected HierarchyNode createChildNode(HierarchyNode parent, String childname) {
		log.debug("...... Creating new child node for: " + childname);
		log.debug("...... Page not set.");
		HierarchyNode child = new HierarchyNode();
		child.setName(childname);
		child.setParent(parent);
		parent.addChild(child);
		return child;
	}
	/**
	 * creates a new node with all node fields set
	 * @param parent
	 * @param childPage
	 */
	protected HierarchyNode createChildNode(HierarchyNode parent, Page childPage) {
		log.debug("...... Creating new child node for: " + childPage.getName());
		log.debug("...... Page set.");
		HierarchyNode child = new HierarchyNode(childPage, parent);
		return child;
	}
	
	/**
	 * gets a node from the parent node that has a name matching the
	 * given childname
	 * @param parent
	 * @param childname
	 * @return child node or null if none exist
	 */
	protected HierarchyNode getChildNode(HierarchyNode parent, String childname) {
		log.debug("...... Getting child node with name: " + childname);
		Collection<HierarchyNode> children = (Collection<HierarchyNode>) parent.getChildren();
		if (children == null || children.isEmpty()) {
			log.debug("...... -> parent has no children. Returning null.");
			return null;
		}
		for (HierarchyNode child : children) { 
			String thisname = null;
			if ("true".equals(matchpagename)) {
				thisname = HierarchyNode.getFilename(child);
			}
			else
				thisname = child.getName();
			log.debug("...... -> comparing child with: " + thisname);
			if (childname.equalsIgnoreCase(thisname)) {
				log.debug("...... -> found child.");
				return child;
			}
		}
		log.debug("...... -> no child node with that name found.");
		return null;
	}
	
	/**
	 * @return the root node
	 */
	protected HierarchyNode getRootNode() {
		if (this.root == null) {
			this.root = new HierarchyNode();
		}
		return this.root;
	}
	
	protected void clearRootNode() {
		this.root = null; //useful for junit tests
	}

	/**
	 * @return returns the system file separator
	 */
	private String getSeperator() {
		String separator = File.separator;
        if ("\\".equals(separator)) {
            separator = "\\\\"; // Escape backslashes!
        }
        return separator;
	}
	
	/**
	 * gets the file extension of the file in the given path
	 * @param path Example: abc/def.txt
	 * @return Example .txt
	 */
	protected String getFileExtension(String path) {
		//only attempt to discover file extension if we haven't bothered already
		if (this.fileExtension != null) 
			return this.fileExtension;
		
		//first check to see if it was explicitly set as a property. Use that, if it's there.
		Properties props = getProperties();
		if (props != null && props.containsKey(PROPKEY_EXT)) {
			this.fileExtension = props.getProperty(PROPKEY_EXT, "");
			return this.fileExtension;
		}
		
		if (path == null) return null;
		
		//if path does not have an extension, return nothing
		if (!path.matches("([^.]+\\.)+[^.]+")) return "";
		
		//get extension
		String replacement = path.replaceFirst(".*?(\\.[^.]*)$", "$1");
		log.debug("...... extension discovered: " + replacement );
		this.fileExtension = replacement;
		return this.fileExtension;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
