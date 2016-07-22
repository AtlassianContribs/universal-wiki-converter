package com.atlassian.uwc.hierarchies;

import java.util.Collection;
import java.util.Properties;

import com.atlassian.uwc.ui.Page;

/**
 * An interface for classes that build page hierarchies.
 *
 * @author Rolf Staflin
 */
public interface HierarchyBuilder {
    /**
     * Creates a tree of HierarchyNodes representing the hierarchy
     * of the supplied pages.
     *
     * @param pages The pages to order.
     * @return The root node of the hierarchy. Note that the root node
     *         itself will <em>not</em> be added to Confluence.
     *         It represents the space front page if you like.
     */
    public HierarchyNode buildHierarchy(Collection<Page> pages);

    /**
     * sets miscellaneous properties map provided by converter properties via ConverterEngine
     * @param properties
     */
    public void setProperties(Properties properties);
    /**
     * @return miscellaneous properties map
     */
    public Properties getProperties();
}
