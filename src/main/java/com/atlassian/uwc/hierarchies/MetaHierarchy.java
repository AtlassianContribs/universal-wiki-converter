package com.atlassian.uwc.hierarchies;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

public abstract class MetaHierarchy implements HierarchyBuilder {

	private static final String PROPKEY_CHILD = "hierarchy-children-comparator";
	Properties properties = null;
	Logger log = Logger.getLogger(this.getClass());

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
	
	protected void init() {
		; //this can be shadowed by implementing classes
	}

	protected abstract HierarchyNode buildRelationships(Page page, HierarchyNode root);

	public static Properties getMeta(Page page) throws FileNotFoundException, IOException {
		 String metapath = getMetaPath(page);
		 Properties meta = new Properties();
		 meta.load(new FileInputStream(metapath));
		 return meta;
	}

	protected static String getMetaPath(Page page) {
		return page.getFile().getAbsolutePath().replaceFirst("\\.txt$", ".meta");
	}

	public Properties getProperties() {
		if (this.properties == null) this.properties = new Properties();
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
