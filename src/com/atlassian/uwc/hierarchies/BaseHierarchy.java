package com.atlassian.uwc.hierarchies;

import com.atlassian.uwc.ui.Page;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

/**
 * This is a base implementation of the HierarchyBuilder interface.
 * For many wikis, it should be enough.
 *
 * It takes a set of pages and orders them in a hierarchy according
 * to the path field of each page. If there are "holes" in the
 * hierarchy (e.g., if the page <code>foo/bar</code> is present, but not the parent
 * page <code>foo</code), those nodes will have a <code>null</code> page field.
 *
 * When the converter engine later sends the pages to Confluence and encounters a node
 * with a <code>null</code> page, it will try to find any existing page with the same name
 * in the Confluence space. If no such page is found, an empty page will be generated and
 * sent so that the hierarchy is preserved.
 *
 * @author Rolf Staflin
 */
public class BaseHierarchy implements HierarchyBuilder {
    private static Logger log = Logger.getLogger(BaseHierarchy.class);

    
    public HierarchyNode buildHierarchy(Collection<Page> pages) {
        log.debug(">buildHierarchy(" + pages.size() + " pages)");
        
        if (pages == null || pages.size() == 0) {
            return null;
        }

        HierarchyNode root = new HierarchyNode();

        log.debug("For each page in pages...");
        for (Page page : pages) {
        	log.debug(".. next page: " + page.getName() + " at path: " + page.getPath());
            HierarchyNode parent = findNode(page.getPath(), root);
            log.debug(".. parent: " + parent.getName());
            HierarchyNode newNode = new HierarchyNode(page, parent);
            log.debug(".. newNode: " + newNode.getName());
            parent.addChild(newNode);
            log.debug(".. adding newNode to parent.");
        }
        log.debug("<buildHierarchy()");
        return root;
    }

    /**
     * This method looks up a node, given the root node and a path.
     * Example:
     * If the path is "foo/bar/baz", the method looks through 
     * @param path The path should be a string with each component separated
     *        by <code>File.separator</code>.
     * @param root The root node of the hierarchy. Must not be null!
     * @return The node.
     */
    protected HierarchyNode findNode(String path, HierarchyNode root) {
    	log.debug(".... findNode params. path = " + path);
    	log.debug(".... findNode params. root name = " + root.getName());
        if (root == null) {
        	String message = "Parameter root must not be null!";
        	log.error(message);
            throw new IllegalArgumentException(message);
        }
        if (path == null || path.trim().length() == 0) {
        	log.debug("Could not find node. Returning root node.");
            return root;
        }

        String separator = File.separator;
        if ("\\".equals(separator)) {
            separator = "\\\\"; // Escape backslashes!
        }

        String[] components = path.split(separator);
        HierarchyNode current = root;
        log.debug(".... foreach name in components:");
        for (String name : components) {
        	log.debug("....* name = " + name);
        	log.debug(".... finding the child in the root node.");
            HierarchyNode next = current.findChild(name);
            if (next == null) {
                // Create a new node if none existed
            	log.debug(".... Creating a new child node: " + name);
                next = new HierarchyNode();
                next.setName(name);
                log.debug(".... Adding child node to root node. ");
                current.addChild(next); // This sets next.parent too.
            }
            current = next;
        }
        return current;
    }

	public Properties getProperties() {
		//not using this feature
		return null;
	}

	public void setProperties(Properties properties) {
		// not using this feature
	}

    
}
