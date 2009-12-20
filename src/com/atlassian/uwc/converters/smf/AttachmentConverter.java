package com.atlassian.uwc.converters.smf;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.exporters.SMFExporter;
import com.atlassian.uwc.hierarchies.MetaHierarchy;
import com.atlassian.uwc.ui.Page;

public class AttachmentConverter extends BaseConverter {

	private static final String PROPKEY_REMOVE = "attachment-chars-remove";
	private static final String PROPKEY_TOUNDERSCORE = "attachment-chars-to-underscore";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		Properties meta = null;
		try {
			meta = MetaHierarchy.getMeta(page);
		} catch (IOException e) {
			log.error("Problem getting meta data for file: " + page.getFile().getAbsolutePath());
			e.printStackTrace();
			return;
		}
		String delim = meta.getProperty("attachments.delim", SMFExporter.Data.ATTACH_DELIM);
		Vector<String> filepaths = getAttachmentPaths(meta.getProperty("attachments.location", ""), delim);
		Vector<String> tmppaths = createTmpPaths(meta.getProperty("attachments.name", ""), page.getFile().getParent(), delim);
		copyToTmp(filepaths, tmppaths);
		attach(page, tmppaths);
	}
	
	protected Vector<String> getAttachmentPaths(String attachments) {
		return getAttachmentPaths(attachments, SMFExporter.Data.ATTACH_DELIM);
	}
	protected Vector<String> getAttachmentPaths(String attachments, String delim) {
		String out = this.getAttachmentDirectory();
		return createPaths(attachments, out, delim);
	}

	private Vector<String> createPaths(String files, String parent, String delim) {
		
		Vector<String> paths = new Vector<String>();
		if (files == null || "".equals(files) || "null".equals(files)) return paths;
		String[] all = splitFiles(files, delim);
		if (!parent.endsWith(File.separator)) parent += File.separator;
		for (int i = 0; i < all.length; i++) {
			String att = all[i];
			att = att.trim();
			att = removeChars(att);
			att = toUnderscore(att);
			paths.add(parent + att);
		}
		return paths;
	}

	private String[] splitFiles(String files, String delim) {
		String[] all; 
		if (delim == null) all = new String[] {files};
		else all = files.split(delim);
		return all;
	}

	private String toUnderscore(String att) {
		String toUnderscoreChars = getToUnderscoreChars();
		if (toUnderscoreChars != null && !"".equals(toUnderscoreChars)) {
			for (int j = 0; j < toUnderscoreChars.length(); j++) {
				String c = Character.toString(toUnderscoreChars.charAt(j));
				//can't use a char class. how do we know what to escape?
				att = att.replaceAll("\\Q"+c+"\\E", "_"); 
			}
		}
		return att;
	}

	private String removeChars(String att) {
		String removeChars = getRemoveChars();
		if (removeChars != null && !"".equals(removeChars)) {
			for (int j = 0; j < removeChars.length(); j++) {
				String c = Character.toString(removeChars.charAt(j));
				//can't use a char class. how do we know what to escape?
				att = att.replaceAll("\\Q"+c+"\\E", ""); 
			}
		}
		return att;
	}

	private String getRemoveChars() {
		Properties props = this.getProperties();
		if (props.containsKey(PROPKEY_REMOVE))
			return props.getProperty(PROPKEY_REMOVE, null);
		return null;
	}

	private String getToUnderscoreChars() {
		Properties props = this.getProperties();
		if (props.containsKey(PROPKEY_TOUNDERSCORE))
			return props.getProperty(PROPKEY_TOUNDERSCORE, null);
		return null;
	}

	protected Vector<String> createTmpPaths(String attachments, String gparent) {
		return createTmpPaths(attachments, gparent, SMFExporter.Data.ATTACH_DELIM);
	}
	protected Vector<String> createTmpPaths(String attachments, String gparent, String delim) {
		if (!gparent.endsWith(File.separator)) gparent += File.separator;
		String parent = gparent + "attachments";
		File parentFile = new File(parent);
		if (!parentFile.exists()) parentFile.mkdir();
		return createPaths(attachments, parent, delim);
	}
	
	protected void copyToTmp(Vector<String> filepaths, Vector<String> tmppaths) {
		if (filepaths == null || tmppaths == null) {
			String error = "Attachments vectors are null. Cannot copy attachments to tmp.";
			log.error(error);
			addError(Feedback.CONVERTER_ERROR, error, true);
			return;
		}
		if (filepaths.size() != tmppaths.size()) {
			String error = "Attachments vectors must be the same size. Cannot copy attachments to tmp.";
			log.error(error);
			addError(Feedback.CONVERTER_ERROR, error, true);
			return;
		}
		for (int i = 0; i < filepaths.size(); i++) {
			String from = filepaths.get(i);
			String to = tmppaths.get(i);
			if (from == null || to == null) {
				log.error("Problem copying attachment file: '" + from + "' to '" + to + "'. Skipping.");
				continue;
			}
			try {
				copyFile(new File(from), new File(to));
			} catch (IOException e) {
				log.warn("Problem copying attachment file: '" + from + "' to '" + to + "'." +
						" Attempting to find alternate attachment file.");
				//try to find the right attachment using the unique file id from the attachments dir
				try {
					from = getAlternateFilepath(from);
					copyFile(new File(from), new File(to));
					log.info("Correctly copied alternate attachment file: " + from);
					continue;
				} catch (IOException e1) {
					log.error("Could not find attachment file. Skipping.");
					e1.printStackTrace();
				}
				e.printStackTrace();
				continue;
			}
		}
	}
	

	/**
	 * copies file to newFile
	 * Note: copied from Jspwiki converter's ImageConverter
	 * @param file
	 * @param newFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void copyFile(File file, File newFile) throws FileNotFoundException, IOException {
		log.debug("Copying '" + file.getAbsolutePath() + "' to '" + newFile.getAbsolutePath() + "'");
		if (!file.exists()) log.debug("File doesn't exist. Cannot copy: " + file.getAbsolutePath());
		// Create channel on the source
		FileChannel srcChannel = new FileInputStream(file.getAbsolutePath()).getChannel();
   
		// Create channel on the destination
		FileChannel dstChannel = new FileOutputStream(newFile.getAbsolutePath()).getChannel();
   
		// Copy file contents from source to destination
		int buffersize = -1;
		try {
			//see if the user specified a buffer size
			String buffersizeStr = getProperties().getProperty("buffer-size", null);
			if (buffersizeStr != null) {
				try { 
					buffersize = Integer.parseInt(buffersizeStr); 
				}
				catch (NumberFormatException en) {
					log.error("Property buffer-size is not an integer. Using filesize.");
				}
			}
			if (buffersize > 0) { //user set buffersize - see Michael Grove's code in UWC-349
				long size = srcChannel.size();
	            long position = 0;
	            while (position < size) {
	               position += srcChannel.transferTo(position, buffersize, dstChannel);
	            }
			}
			else { //if no user specified buffer size, use filesize
				dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e2) {
			throw e2;
		} catch (RuntimeException e3) {
			throw e3;
		} finally {
			// Close the channels
			srcChannel.close(); 
			dstChannel.close();
			if (!newFile.exists()) log.error("Copying file unsuccessful. New file does not exist: " + newFile.getAbsolutePath());
		}
	}

	Pattern fileid = Pattern.compile("\\/(\\d+)[^\\/]+$");
	private String getAlternateFilepath(String path) throws IOException {
		Matcher idFinder = fileid.matcher(path);
		final String fileid = (idFinder.find())?idFinder.group(1):null;
		if (fileid == null) throw new IOException("Couldn't find fileId in: " + path);
		File dir = new File(this.getAttachmentDirectory());
		File[] files = dir.listFiles(new FileFilter() {
			public boolean accept(File candidate) {
				return candidate.getName().startsWith(fileid + "_");
			}
		});
		if (files == null || files.length < 1) throw new IOException("Couldn't find fileId in: " + path);
		if (files.length > 1) log.warn("Found multiple files with unique id! Using first one.");
		return files[0].getAbsolutePath();
	}

	
	protected void attach(Page page, Vector<String> filepaths) {
		for (String path : filepaths) {
			File file = new File(path);
			if (!file.exists()) log.warn("Could not find attachment at location: " + path);
			if (!file.isFile()) log.warn("Attachment is not a file: " + path);
			page.addAttachment(file);
		}
	}

}
