package com.atlassian.uwc.ui.xmlrpcwrapperOld;

import java.util.Hashtable;

/**
 * This class encapsulates info around a Confluence attachment.
 */
public class AttachmentForXmlRpcOld {

    /**
     * String 	 numeric id of the attachment
     */
    protected String id;
    /**
     * String 	page ID of the attachment
     */
    protected String pageId;
    /**
     * String 	title of the attachment
     */
    protected String title;
    /**
     * String 	file name of the attachment (Required)
     */
    protected String fileName;
    /**
     * String 	numeric file size of the attachment in bytes
     */
    protected String fileSize;
    /**
     * String 	mime content type of the attachment (Required)
     */
    protected String contentType;
    /**
     * Date 	creation date of the attachment
     */
    protected String created;
    /**
     * String 	creator of the attachment
     */
    protected String creator;
    /**
     * String 	url to download the attachment online
     */
    protected String url;
    /**
     * String  	comment for the attachment (Required)
     */
    protected String comment;
    protected String fileLocation;
    /**
     * This is the Hashtable holding the instance variables associated with
     * a page. Confluence-XMLRPC expects a Hashtable.
     */
    Hashtable<String, String> pageParams = new Hashtable<String, String>();

    public String getComment() {
        return pageParams.get("comment");
    }

    public void setComment(String comment) {
        pageParams.put("comment",comment);
    }

    public String getContentType() {
        return pageParams.get("contentType");
    }

    public void setContentType(String contentType) {
        pageParams.put("contentType",contentType);
    }

    public String getCreated() {
        return  pageParams.get("created");
    }

    public void setCreated(String created) {
        pageParams.put("created",created);
    }

    public String getCreator() {
        return  pageParams.get("creator");
    }

    public void setCreator(String creator) {
        pageParams.put("creator",creator);
    }

    public String getFileName() {
        return  pageParams.get("fileName");
    }

    public void setFileName(String fileName) {
        pageParams.put("fileName",fileName);
    }

    public String getFileSize() {
        return  pageParams.get("fileSize");
    }

    public void setFileSize(String fileSize) {
        pageParams.put("fileSize",fileSize);
    }

    public String getId() {
        return  pageParams.get("id");
    }

    public void setId(String id) {
        pageParams.put("id",id);
    }

    public String getPageId() {
        return  pageParams.get("pageId");
    }

    public void setPageId(String pageId) {
        pageParams.put("pageId",pageId);
    }

    public String getTitle() {
        return  pageParams.get("title");
    }

    public void setTitle(String title) {
        pageParams.put("title",title);
    }

    public String getUrl() {
        return  pageParams.get("url");
    }

    public void setUrl(String url) {
        pageParams.put("url",url);
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public Hashtable<String, String> getPageParams() {
        return pageParams;
    }

    public void setPageParams(Object pageParams) {
        this.pageParams = (Hashtable<String, String>) pageParams;
    }

}
