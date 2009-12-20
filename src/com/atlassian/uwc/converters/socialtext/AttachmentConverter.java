package com.atlassian.uwc.converters.socialtext;

import java.io.File;
import java.io.FileFilter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.filters.SocialtextFilter;
import com.atlassian.uwc.ui.Page;

/**
 * Finds attachments for the given page, and attaches them if
 * - they have not been deleted
 * - they are the most recent version of that attachments
 */
public class AttachmentConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String parent = page.getFile().getParentFile().getName();
		String attdir = getAttachmentDirectory();
		if (hasAttachments(attdir, parent)) {
			HashMap<String, File> uniqueAttachments = new HashMap<String, File>();
			File pagedir = getPageDir(attdir, parent);
			TreeSet<String> sorted = getSortedTimestampDirectories(pagedir);
			for (String timestamp : sorted) {
				File[] attachments = getAttachments(pagedir, timestamp);
				for (File att : attachments) {
					if (isDeleted(att)) continue;
					if (!uniqueAttachments.containsKey(att.getName())) {
						page.addAttachment(att);
						uniqueAttachments.put(att.getName(), att);
					}
				}
			}
		}
	}

	/**
	 * @param file
	 * @return true if the given file was "deleted" by socialtext
	 */
	protected boolean isDeleted(File file) {
		SocialtextFilter filter = new SocialtextFilter();
		File metafile = new File(file.getParent() + ".txt");
		String input = filter.read(metafile);
		return filter.isDeleted(input);
	}

	/**
	 * @param attdir
	 * @param parent
	 * @return true if attachment directory contains directory named the same as parent
	 */
	protected boolean hasAttachments(String attdir, String parent) {
		File dir = new File(attdir);
		if (!dir.exists() || !dir.isDirectory()) {
			log.error("Attachment Directory does not exist or is not a directory: " + attdir);
			return false;
		}
		String[] pagedirs = dir.list();
		for (int i = 0; i < pagedirs.length; i++) {
			String pagedir = pagedirs[i];
			if (parent.equals(pagedir)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * gets the page directory in the attachment directory with the same name as parent
	 * @param attdir
	 * @param parent
	 * @return
	 */
	protected File getPageDir(String attdir, String parent) {
		return new File(attdir + File.separator + parent);
	}

	/**
	 * @param pagedir
	 * @return a list of directory names that are children of pagedir. 
	 * These directories should be in timestamp form, and should be sorted in
	 * descending order (largest first).
	 */
	protected TreeSet<String> getSortedTimestampDirectories(File pagedir) {
		TreeSet<String> sorted = new TreeSet<String>(new DescendingComparator());
		File[] timestampDirs = pagedir.listFiles();
		for (int i = 0; i < timestampDirs.length; i++) {
			File timestampFile = timestampDirs[i];
			if (timestampFile.isDirectory())
				sorted.add(timestampFile.getName());
		}
		return sorted;
	}

	/**
	 * 
	 * @param pagedir
	 * @param timestamp
	 * @return all the attachments in the timestamp directory that's in the pagedir
	 */
	protected File[] getAttachments(File pagedir, String timestamp) {
		File timestampFile = new File(pagedir.getAbsolutePath() + File.separator + timestamp);
		return timestampFile.listFiles(new JustFilesFilter());
	}

	/**
	 * Comparator for returning descending ascii order
	 */
	public class DescendingComparator implements Comparator {

		public int compare(Object o0, Object o1) {
			String s0 = (String) o0;
			String s1 = (String) o1;
			return s1.compareTo(s0);
		}
		
	}

	/**
	 * filter that allows only files (not directories)
	 */
	public class JustFilesFilter implements FileFilter {

		public boolean accept(File file) {
			return file.isFile();
		}
		
	}
}
