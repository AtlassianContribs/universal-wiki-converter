package com.atlassian.uwc.hierarchies;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
    private Set<HierarchyNode> children;
    private static Comparator childrenComparator;

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
    public void removeChild(HierarchyNode child) {
        if (child == null) {
            throw new IllegalArgumentException("The parameter must not be null!");
        }
        if (children != null && children.contains(child)) {
            children.remove(child);
            if (children.size() == 0) {
                children = null;
            }
            child.setParent(null);
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
    public Set<HierarchyNode> getChildren() {
    	if (this.children == null) {
    		if (childrenComparator == null)
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

    public void setChildren(Set<HierarchyNode> children) {
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
            if (name.equalsIgnoreCase(child.getName())) {
                return child;
            }
        }
        return null;
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
}
