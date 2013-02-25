package com.atlassian.uwc.hierarchies;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

/**
 * This class represents one node in a page hierarchy. It has links to its
 * parents and children and holds a page object as "payload".
 * @author Rolf Staflin
 */
public class HierarchyNode {
    private String name;
    private Page page;
    private HierarchyNode parent;
    private Collection<HierarchyNode> children;
    private static Comparator childrenComparator;
    private static boolean childrenAsList = false;

    Logger log = Logger.getLogger(this.getClass());
    
    public HierarchyNode() {
    }

    /**
     * This constructor sets up the page, parent and name fields.
     * @param page The page. If the page in non-null, the node will
     *        use the page name as it's own.
     * @param parent The parent node, or <code>null</code>.
     */
    public HierarchyNode(Page page, HierarchyNode parent) {
        this.page = page;
        this.parent = parent;
        if (page != null) {
            name = page.getName();
        }
		//set this child as a member of the parent's children set
		parent.addChild(this);
    }

    /**
     * Adds a child node to the set of children.
     * Note that this method also sets the parent node of the child.
     * @param child The child to be removed. Must not be <code>null</code>.
     */
    public void addChild(HierarchyNode child) {
        if (child == null) {
            throw new IllegalArgumentException("The parameter must not be null!");
        }
        getChildren(); //init
        children.add(child);
        child.setParent(this);
    }

    /**
     * Removes a child from the set of children.
     * Note that this method also clears the parent field of the child.
     * If the supplied node was not a child of this node, nothing is done. 
     *
     * @param child The child to be removed. Must not be <code>null</code>.
     */
    public boolean removeChild(HierarchyNode child) {
        if (child == null) {
            throw new IllegalArgumentException("The parameter must not be null!");
        }
        if (getChildren().remove(child)) {
            log.debug("Removed: '" + child.getName() + "' from " + this.getName());
            if (children.size() == 0) {
                children = null;
            }
            child.setParent(null);
            return true;
        }
        else {
        	log.debug("Unable to remove this child: " + child.getName());
        	return false;
        }
    }
    
    /**
     * Returns the parent of this node.
     * @return The parent of this node, or <code>null</code> if
     *         this node doesn't have a parent.
     */
    public HierarchyNode getParent() {
        return parent;
    }

    public void setParent(HierarchyNode newParent) {
        parent = newParent;
    }

    /**
     * Returns the children of this node.
     * @return A set of child nodes, or an empty set if
     *         this node has no children.
     */
    public Collection<HierarchyNode> getChildren() {
    	if (this.children == null) {
    		if (childrenAsList) {
    			this.children = new Vector<HierarchyNode>();
    		}
    		else if (childrenComparator == null)
    			this.children = new HashSet<HierarchyNode>();
    		else //if you want to be able to control the child sort order
    			this.children = new TreeSet<HierarchyNode>(childrenComparator);
    	}
        return this.children;
    }

    /**
     * Returns an iterator for the children of this node.
     * @return An iterator, or <code>null</code> if this
     *         node has no children.
     */
    public Iterator<HierarchyNode> getChildIterator() {
        return children == null ? null : children.iterator();
    }

    public void setChildren(Collection<HierarchyNode> children) {
        this.children = children;
    }
    
    /**
     * set this if you want to be able to control the sort order of children.
     * You only need to set it once. (maintained with a static object.)
     * @param comparator
     */
    public void setChildrenComparator(Comparator comparator) {
    	childrenComparator = comparator;
    }

    /**
     * Looks up a child node.
     * @param name The name of the sought-after child.
     * @return The child node, or <code>null</code> if the
     *         child wasn't found.
     */
    public HierarchyNode findChild(String name) {
        if (name == null || children == null) {
            return null;
        }
        for (HierarchyNode child : children) {
            String pagename = child.getName();
			if (name.equalsIgnoreCase(pagename)) {
                return child;
            }
        }
        return null;
    }
    
    private static Pattern ext = Pattern.compile("[.].{3,4}$");
    /**
     * Looks up a child node.
     * @param name The name of the sought-after child.
     * @param ignorableExtension file extension we can ignore. should be "", if none.
     * @return The child node, or <code>null</code> if the
     *         child wasn't found.
     */
    public HierarchyNode findChildByFilename(String name) {
        if (name == null || children == null) {
            return null;
        }
        for (HierarchyNode child : children) {
            String pagename = getFilename(child);
			if (name.equalsIgnoreCase(pagename)) {
                return child;
            }
        }
        return null;
    }

	public static String getFilename(HierarchyNode child) {
		if (child.getPage() == null) {
			return child.getName();
		}
		String pagename = null;
		if (child.getPage().getFile() != null)
			pagename = child.getPage().getFile().getName();
		else
			pagename = child.getPage().getName();
		Matcher extFinder = ext.matcher(pagename);
		if (extFinder.find()) 
			pagename = extFinder.replaceFirst("");
		return pagename;
	}
    
    /**
     * Returns the converter page associated with this node
     * @return A page object, or <code>null</code> if
     *         this node has no page associated with it.
     */
    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Recursive method that counts the descendants of the node.
     * @return The number of descendants of the node.
     */
    public int countDescendants() {
    	int i = 0;
        if (children == null) {
            return 1;
        }
        for (HierarchyNode child : children) {
            // Add one for the child plus the number of descendants it has
            i += child.countDescendants();
        }
        i++;
        return i;
    }
    
//    public String toString() {
//    	return treeAsString(this);
//    }
    public String treeAsString(HierarchyNode node) {
		return treeAsString(node.getChildren(), "");
	}

	private String treeAsString(Collection<HierarchyNode> children, String delim) {
		String msg = "";
		for (HierarchyNode child : children) {
			String newdelim = delim + " .";
			msg += newdelim + child.getName() + "\n";
			msg += treeAsString(child.getChildren(), newdelim);
		}
		return msg;
	}
	
	public static void childrenAsList(boolean aslist) {
		childrenAsList = aslist;
	}
}
