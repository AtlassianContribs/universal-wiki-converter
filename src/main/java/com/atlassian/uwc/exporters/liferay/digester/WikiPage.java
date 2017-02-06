package com.atlassian.uwc.exporters.liferay.digester;

import java.io.File;
import java.util.ArrayList;

/* @formatter:off
<WikiPage>
<__new>false</__new>
<__cachedModel>false</__cachedModel>
<__escapedModel>false</__escapedModel>
<__uuid>f1819813-8bba-4fcf-8eb8-df63e73f3882</__uuid>
<__originalUuid>f1819813-8bba-4fcf-8eb8-df63e73f3882</__originalUuid>
<__pageId>113470</__pageId>
<__resourcePrimKey>113471</__resourcePrimKey>
<__originalResourcePrimKey>113471</__originalResourcePrimKey>
<__setOriginalResourcePrimKey>false</__setOriginalResourcePrimKey>
<__groupId>113333</__groupId>
<__originalGroupId>113333</__originalGroupId>
<__setOriginalGroupId>false</__setOriginalGroupId>
<__companyId>10233</__companyId>
<__userId>10272</__userId>
<__userUuid>5f04387c-e071-43d8-b2a1-ece933d80c31</__userUuid>
<__userName>Test Test</__userName>
<__createDate class="sql-timestamp">2011-03-25 10:35:13.0</__createDate>
<__modifiedDate class="sql-timestamp">2011-03-25 10:35:13.0</__modifiedDate>
<__nodeId>113469</__nodeId>
<__originalNodeId>113469</__originalNodeId>
<__setOriginalNodeId>false</__setOriginalNodeId>
<__title>FrontPage</__title>
<__originalTitle>FrontPage</__originalTitle>
<__version>1.0</__version>
<__originalVersion>1.0</__originalVersion>
<__setOriginalVersion>false</__setOriginalVersion>
<__minorEdit>true</__minorEdit>
<__content></__content>
<__summary>New</__summary>
<__format>creole</__format>
<__head>false</__head>
<__parentTitle></__parentTitle>
<__redirectTitle></__redirectTitle>
<__status>0</__status>
<__statusByUserId>10272</__statusByUserId>
<__statusByUserName>Test Test</__statusByUserName>
<__statusDate class="sql-timestamp">2011-10-03 16:45:33.0</__statusDate>
</WikiPage>
*/ //@formatter:on

public class WikiPage implements Comparable<WikiPage>{
	private String __title;
	private String __version;
	private String __parentTitle = "";
	private String __redirectTitle = "";
	private String __format = "";
	private String __content;
	private ArrayList<WikiPage> childList = new ArrayList<WikiPage>();
	private ArrayList<Attachment> attachments = new ArrayList<Attachment>();	
	private int depth = 0;	
	private String path = "root";
	private File file; // the location on disk of the data that produced this object
	private File outDir;
		
	public File getOutDir() {
		return outDir;
	}
	public void setOutDir(File outDir) {
		this.outDir = outDir;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	
	public String get__format() {
		return __format;
	}
	
	public void set__format(String __format) {
		this.__format = __format;
	}
	
	public String get__parentTitle() {
		return __parentTitle;
	}

	public void set__parentTitle(String __parentTitle) {
		this.__parentTitle = __parentTitle;
	}
		
	public String get__redirectTitle() {
		return __redirectTitle;
	}
	
	public void set__redirectTitle(String __redirectTitle) {
		this.__redirectTitle = __redirectTitle;
	}
	
	public String get__title() {
		return __title;
	}

	public void set__title(String __title) {
		this.__title = __title;
	}

	public String get__version() {
		return __version;
	}

	public Double getVersion() {
		return Double.parseDouble(__version);
	}

	public void set__version(String __version) {
		this.__version = __version;
	}

	public String get__content() {
		return __content;
	}

	public void set__content(String __content) {
		this.__content = __content;
	}

	public void addChildPage(WikiPage page) {
		childList.add(page);
	}
	public ArrayList<WikiPage> getChildren() {
		return childList;
	}
		
	public ArrayList<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(ArrayList<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	boolean isFrontPage(){
		boolean retval = false;
		if( get__title().equals("FrontPage")){
			retval = true;
		}
		
		return retval;
	}

	@Override
	public String toString() {
		return depth + "-WikiPage [__title=" + __title + ", __version=" + __version + ", __parentTitle=" + __parentTitle + "]";
	}

	@Override
	public int compareTo(WikiPage o) {		
		return this.get__title().compareTo(o.get__title());
	}
	
	public String showChildren() {
		String retval = "";

		if (!childList.isEmpty()) {
			retval = "[children(" + childList.size() + ") " + childList + "]";
		}

		return retval;
	}

	public boolean removeChild(WikiPage child) {
		return childList.remove(child);
	}
	
}
