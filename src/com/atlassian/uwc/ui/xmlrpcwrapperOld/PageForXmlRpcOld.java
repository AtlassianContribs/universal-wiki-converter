package com.atlassian.uwc.ui.xmlrpcwrapperOld;

import java.util.Hashtable;

/**
 * Encapsulates data and functionality around a PageForXmlRpcOld which gets
 * moved to and from Confluence via XMLRPC
 * <p/>
 * We could have designed this with a bit more flexiblity using an interface,
 * but I don't want to overdesign just now.
 */
public class PageForXmlRpcOld {

    /**
     * the id of the page
     */
    protected final String id = "id";
    /**
     * the key of the space that this page belongs to
     */
    protected final String space = "space";
    /**
     * the id of the parent page
     */
    protected final String parentId = "parentId";
    /**
     * the title of the page
     */
    protected final String title = "title";
    /**
     * the url to view this page online
     */
    protected final String url = "url";
    /**
     * the version number of this page
     */
    protected final String version = "version";
    /**
     * the page content
     */
    protected final String content = "content";
    /**
     * timestamp page was created
     */
    protected final String created = "created";
    /**
     * username of the creator
     */
    protected final String creator = "creator";
    /**
     * timestamp page was modified
     */
    protected final String modified = "modified";
    /**
     * username of the page's last modifier
     */
    protected final String modifier = "modifier";
    /**
     * whether or not this page is the space's homepage
     */
    protected final String homePage = "homePage";
    /**
     * the number of locks current on this page
     */
    protected final String locks = "locks";
    /**
     * status of the page (eg. current or deleted)
     */
    protected final String contentStatus = "contentStatus";
    /**
     * whether the page is current and not deleted
     */
    protected final String current = "current";

    /**
     * This is the Hashtable holding the instance variables associated with
     * a page. Confluence-XMLRPC expects a Hashtable.
     */
    Hashtable<String, String> pageParams = new Hashtable<String, String>();

    public Hashtable<String, String> getPageParams() {
        return pageParams;
    }

    public void setPageParams(Object pageParams) {
        this.pageParams = (Hashtable<String, String>) pageParams;
    }

    public String getId() {
        return String.valueOf(pageParams.get(id));
    }

    public void setId(String idVal) {
        pageParams.put(id, idVal);
    }

    public String getSpace() {
        return String.valueOf(pageParams.get(space));
    }

    public void setSpace(String spaceVal) {
        pageParams.put(space, spaceVal);
    }

    public String getParentId() {
        return String.valueOf(pageParams.get(parentId));
    }

    public void setParentId(String parentIdVal) {
        pageParams.put(parentId, parentIdVal);
    }

    public String getTitle() {
        return String.valueOf(pageParams.get(title));
    }

    public void setTitle(String titleVal) {
        pageParams.put(title, titleVal);
    }

    public String getUrl() {
        return String.valueOf(pageParams.get(url));
    }

    public void setUrl(String urlVal) {
        pageParams.put(url, urlVal);
    }

    public String getVersion() {
        return String.valueOf(pageParams.get(version));
    }

    public void setVersion(String versionVal) {
        pageParams.put(version, versionVal);
    }

    public String getContent() {
        return String.valueOf(pageParams.get(content));
    }

    public void setContent(String contentVal) {
        pageParams.put(content, contentVal);
    }

    public String getCreated() {
        return String.valueOf(pageParams.get(created));
    }

    public void setCreated(String createdVal) {
        pageParams.put(created, createdVal);
    }

    public String getCreator() {
        return String.valueOf(pageParams.get(creator));
    }

    public void setCreator(String creatorVal) {
        pageParams.put(creator, creatorVal);
    }

    public String getModified() {
        return String.valueOf(pageParams.get(modified));
    }

    public void setModified(String modifiedVal) {
        pageParams.put(modified, modifiedVal);
    }

    public String getModifier() {
        return String.valueOf(pageParams.get(modifier));
    }

    public void setModifier(String modifierVal) {
        pageParams.put(modifier, modifierVal);
    }

    public String getHomePage() {
        return String.valueOf(pageParams.get(homePage));
    }

    public void setHomePage(String homePageVal) {
        pageParams.put(homePage, homePageVal);
    }

    public String getLocks() {
        return String.valueOf(pageParams.get(locks));
    }

    public void setLocks(String locksVal) {
        pageParams.put(locks, locksVal);
    }

    public String getContentStatus() {
        return String.valueOf(pageParams.get(contentStatus));
    }

    public void setContentStatus(String contentStatusVal) {
        pageParams.put(contentStatus, contentStatusVal);
    }

    public String getCurrent() {
        return String.valueOf(pageParams.get(current));
    }

    public void setCurrent(String currentVal) {
        pageParams.put(current, currentVal);
    }
}
