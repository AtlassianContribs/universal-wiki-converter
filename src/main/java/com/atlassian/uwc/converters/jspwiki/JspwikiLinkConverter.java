package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;

public abstract class JspwikiLinkConverter extends BaseConverter {
	public static final String JSPWIKI_PAGEDIR = "jspwiki-pagedir";
	Logger log = Logger.getLogger(this.getClass());
	String pagedir = null;
	String[] pagenames = null;
	/**
	 * gets the page dir from the jspwiki-pagedir key in the properties object
	 * Note: cached unless you call clearState
	 * @return
	 */
	protected String getPageDir() {
		if (pagedir == null) {
			if (getProperties().containsKey(JspwikiLinkConverter.JSPWIKI_PAGEDIR)) {
				pagedir = getProperties().getProperty(JspwikiLinkConverter.JSPWIKI_PAGEDIR);
				File file = new File(pagedir);
				if (!file.exists() ||!file.isDirectory()) {
					String error = "Miscellaneous Property " + JspwikiLinkConverter.JSPWIKI_PAGEDIR + " was set, but is not an existing directory: " + pagedir;
					log.error(error);
					addError(Feedback.BAD_PROPERTY, error, true);
					return null;
				}
				return pagedir;
			}
			log.debug(JspwikiLinkConverter.JSPWIKI_PAGEDIR + " property not set. Using best guess method for determining pagenames.");
			return null;
		}
		return pagedir;
	}
	/**
	 * clears all cached state in this class 
	 */
	protected void clearState() { //for junit
		pagedir = null;
		pagenames = null;
	}
	/**
	 * List of filenames in the given directory that are not themselves directories.
	 * Note: cached unless you call clearState
	 * @param dir
	 * @return
	 */
	protected String[] getPageFiles(File dir) {
		if (pagenames == null) {
			File[] files = dir.listFiles(new OnlyFilesFilter());
			pagenames = new String[files.length];
			int i = 0;
			for (File file : files) {
				pagenames[i++] = file.getName();
			}
		}
		return pagenames;
	}
	/**
	 * FileFilter that only returns files (not directories)
	 */
	public class OnlyFilesFilter implements FileFilter {
		public boolean accept(File file) {
			return !file.isDirectory();
		}
	}
}
