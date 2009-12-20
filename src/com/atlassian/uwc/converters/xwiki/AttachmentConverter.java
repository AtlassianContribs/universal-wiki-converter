package com.atlassian.uwc.converters.xwiki;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Adds xwiki attachments to page object. 
 * (Note: Uploading occurs later in the ConverterEngine)
 */
public class AttachmentConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Adding attachments to page -- starting");
		String input = page.getOriginalText();
		//attach files
		Vector<String> attachmentXmlFragments = getAllAttachmentXml(input);
		if (attachmentXmlFragments == null) {
			log.debug("No attachments were found");
			log.info("Adding attachments to page -- complete");
			return;
		}
		//get attachment directory and check for validity
		String attdir = getAttachmentDirectory();
		if (!validDir(attdir)) {
			log.info("Adding attachments to page -- complete");
			return;
		}
		for (int i = 0; i < attachmentXmlFragments.size(); i++) {
			String attachmentXml = attachmentXmlFragments.get(i);
			String name = getAttachmentName(attachmentXml);
			if (name == null) {
				log.debug("Could not find filename for attachment candidate. Skipping.");
				continue;
			}
			String contents = getAttachmentContents(attachmentXml);
			if (contents == null) {
				log.debug("Could not find file contents for attachment named: '" + name + "'. Skipping.");
				continue;
			}
			File attachment = createFile(name, contents, page.getName(), attdir);
			if (attachment != null) {
				log.debug("Attaching file '" + name + "'");
				page.addAttachment(attachment);
			}
		}
		log.info("Adding attachments to page -- complete");
	}

	/**
	 * @param dir
	 * @return false if dir is not set or does not represent 
	 * a valid directory on the filesystem
	 */
	private boolean validDir(String dir) {
		if (dir == null) {
			String message = "Attachment Directory was not set. Cannot attach files.";
			log.error(message);
			addError(Feedback.BAD_OUTPUT_DIR, message, true);
			return false;
		}
		File file = new File(dir);
		if (!file.exists() || !file.isDirectory()) {
			String message = "Attachment Directory is not a valid directory on the file " +
					"system. Cannot attach files.";
			log.error(message);
			addError(Feedback.BAD_OUTPUT_DIR, message, true);
			return false;
		}
		return true;
	}

	Pattern attachment = Pattern.compile("" +
			"<attachment>.*?<\\/attachment>", Pattern.DOTALL
			);
	/**
	 * @param input 
	 * @return creates a Vector of the xml fragments representing the
	 * attachments associated with the page.
	 */
	protected Vector<String> getAllAttachmentXml(String input) {
		Vector<String> xml = new Vector<String>();
		Matcher attFinder = attachment.matcher(input);
		boolean found = false;
		while (attFinder.find()) {
			found = true;
			String attXml = attFinder.group();
			xml.add(attXml);
		}
		if (found) {
			return xml;
		}
		return null;
	}

	Pattern filename = Pattern.compile("" +
			"<filename>(.*?)<\\/filename>"
			);
	/**
	 * @param input
	 * @return the filename for the attachment
	 */
	protected String getAttachmentName(String input) {
		Matcher nameFinder = filename.matcher(input);
		if (nameFinder.find()) {
			return nameFinder.group(1);
		}
		return null;
	}

	Pattern contents = Pattern.compile("" +
			"<content>(.*?)<\\/content>"
			);
	/**
	 * @param input
	 * @return the xmlfragment representing the contents of the file
	 */
	protected String getAttachmentContents(String input) {
		Matcher contentFinder = contents.matcher(input);
		if (contentFinder.find()) {
			return contentFinder.group(1);
		}
		return null;
	}

	/**
	 * XXX
	 * creates a File on the file system with the given name
	 * and the given contents 
	 * @param name
	 * @param contents
	 * @param attdir
	 * @return
	 */
	protected File createFile(String name, String contents, String pagename, String attdir) {
		if (!attdir.endsWith(File.separator)) attdir += File.separator;
		File pagedir = new File(attdir + pagename);
		pagedir.mkdir();
		if (!pagedir.exists()) {
			String message = "Could not create directory for pagename '" + pagename + "'" +
					" at location: '" + attdir + pagename + "'." +
					" Cannot attach files for this page.";
			log.error(message);
			return null;
		}
		String filepath = attdir + pagename + File.separator + name;
		byte[] bytes = decodeBase64(contents);
		writeFile(filepath, bytes);
		File file = new File(filepath);
		if (!file.exists()) {
			String message = "Could not create file at '" + filepath +"'. Skipping attachment.";
			log.error(message);
			return null;
		}
		return file;
	}
	
	private byte[] decodeBase64(String input) {
		// TODO Auto-generated method stub
		return Base64.decode(input);
	}

	/**
	 * writes the given bytes to the file at the given path
	 * @param path string, filepath where text will be written
	 * @param bytes byte array, btyes to write to filepath
	 */
	protected void writeFile(String path, byte[] bytes) {
		try {
			FileOutputStream fw = new FileOutputStream(path);
			BufferedOutputStream out = new BufferedOutputStream(fw);
			out.write(bytes);
			out.close();
		} catch (IOException e) {
			log.error("Problem writing to file: " + path);
			e.printStackTrace();
		}
	}

}
