package com.atlassian.uwc.exporters.liferay;

import java.io.File;

public class Params {
	private String orphanPages = "Uncategorized";
	private String confluenceStartPage = "Home";
	private String attachmentDir = "attach";
	private String larDirectory;
	private File outputDir;
	private boolean original = false;
	private boolean noHtml = false;

	/**
	 * This is the name of the page in Confluence that will hold the orphans
	 * 
	 * @return
	 */
	public String getOrphanPage() {
		return orphanPages;
	}

	public void setOrphanPages(String orphanPages) {
		this.orphanPages = orphanPages;
	}

	public String getConfluenceStartPage() {
		return confluenceStartPage;
	}

	public void setConfluenceStartPage(String confluenceStartPage) {
		this.confluenceStartPage = confluenceStartPage;
	}

	public String getAttachmentDir() {
		return attachmentDir;
	}

	public void setAttachmentDir(String attachmentDir) {
		this.attachmentDir = attachmentDir;
	}

	public String getLarDirectory() {
		return larDirectory;
	}

	public void setLarDirectory(String larDirectory) {
		this.larDirectory = larDirectory;
	}

	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public boolean isOriginal() {
		return original;
	}

	public void setOriginal(boolean original) {
		this.original = original;
	}

	public void setOriginal(String original) {
		this.original = Boolean.parseBoolean(original);
	}

	public boolean isNoHtml() {
		return noHtml;
	}

	public void setNoHtml(boolean noHtml) {
		this.noHtml = noHtml;
	}

}
