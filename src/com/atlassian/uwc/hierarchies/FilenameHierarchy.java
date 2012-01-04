package com.atlassian.uwc.hierarchies;

import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.ui.Page;

/**
 * NOTE: This hierarchy will truncate the page name to the leaf node of the filename with a settable delimiter property
 * for identifying nodes. It presumes all leaf nodes are unique. Any naming collisions MUST be dealt with prior to 
 * this hierarchy being applied.
 */
public class FilenameHierarchy implements HierarchyBuilder {

	private static final String DEFAULT_DELIM = "%2F";
	public static final String PROPKEY_DELIM = "filename-hierarchy-delimiter";
	Properties props = new Properties();
	Logger log = Logger.getLogger(this.getClass());
	private String delim = DEFAULT_DELIM;
	private IllegalLinkNameConverter decoder;
	
	@Override
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		log.info("Building Filename Hierarchy");
		
		if (pages == null || pages.isEmpty()) {
			log.error("Cannot build hierarchy. Pages object is null or empty.");
			return null;
		}
		
		initProperties();

		return getAllNodes(pages);
	}
	
	private void initProperties() {
		if (getProperties().containsKey(PROPKEY_DELIM)) 
			delim = getProperties().getProperty(PROPKEY_DELIM, DEFAULT_DELIM);
		
	}

	public HierarchyNode getAllNodes(Collection<Page> pages) {
		HierarchyNode root = new HierarchyNode();
		for (Page page : pages) {
			if (page != null) {
				log.debug("Page [name, filename, abspath]: [" + page.getName() + ", " + page.getFile().getName() + ", " + page.getFile().getAbsolutePath()+"]");
				root = buildRelationships(page, root);
			}
		}
		log.debug("Hierarchy: " + root.toString());
		return root;
	}

	private HierarchyNode buildRelationships(Page page, HierarchyNode root) {
		//get the original filename of the page
		String origFilename = page.getFile().getName();
		//construct the set of Strings that will represent page names and nodes
		String[] names = origFilename.split(this.delim);
		//for each, if the node exists, get it, if not create a placeholder
		HierarchyNode current = root;
		for (int i = 0; i < names.length; i++) {
			String name = names[i];
			HierarchyNode node = getNode(current, name);
			current.addChild(node);
			current = node;
		}
		page.setName(current.getName());
		current.setPage(page);
		return root;
	}

	private HierarchyNode getNode(HierarchyNode node, String name) {
		for (HierarchyNode child : node.getChildren()) {
			if (child.getName().equalsIgnoreCase(name)) return child;
		}
		HierarchyNode newnode = new HierarchyNode();
		newnode.setName(urldecode(name));
		return newnode;
	}

	private String urldecode(String name) {
		if (this.decoder == null) this.decoder = new IllegalLinkNameConverter();
		return this.decoder.decodeUrl(name);
	}
	

	@Override
	public void setProperties(Properties properties) {
		this.props = properties;

	}

	@Override
	public Properties getProperties() {
		return this.props;
	}

}
