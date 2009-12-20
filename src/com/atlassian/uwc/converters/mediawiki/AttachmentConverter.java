package com.atlassian.uwc.converters.mediawiki;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.Page;

/**
 * Prepares mediawiki attachments for uploading to Confluence.
 * NOTE: This class was heavily influenced by TwikiPrepareAttachmentFilesConverter
 * @author Laura Kolker
 */
public class AttachmentConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass()); 
	ConfluenceSettingsForm confSettings = null;

	public void convert(Page page) {
		log.info("Converting Mediawiki Attachments -- starting");
        // scan the page and create a list of attachments
        addAttachmentsToPage(page, this.getAttachmentDirectory());
		log.info("Converting Mediawiki Attachments -- complete");

	}
	
    /**
     * determines which attachments are sought by the page, and attaches them
     * @param page object to attach pages to
     */
    protected void addAttachmentsToPage(Page page, String attachmentDir) {
    	//what attachments are we looking for?
    	log.debug("Finding Attachments");
    	Vector soughtAttachments = getSoughtAttachmentNames(page);
    	if (soughtAttachments == null || soughtAttachments.size() == 0)
    		 return;
    	
    	//get filelist from the attachment directory
    	log.debug("Examining File Directory");
        File attachmentPageDir = new File(attachmentDir);
        File files[] = attachmentPageDir.listFiles();
        if (files==null) {
            log.info("no attachment files found in directory: "+attachmentDir);
            return;
        }
        
        // add sought after attachments. This is a recursive method.
        log.debug("Attaching files to page");
        addAttachments(page, files, soughtAttachments);
    }

	/**
	 * recursively look through the images directory for the desired files
	 * and attach them to the given page
	 * @param page object files will be attached to
	 * @param files Array of a directory's files
	 * @param soughtFilenames filenames we are looking to attach
	 */
	private void addAttachments(Page page, File[] files, Vector soughtFilenames) {
		for (File file : files) {
			//check for existence
            if (!file.exists()) continue;
            //check for recursion
            if (file.isDirectory()) {
            	File moreFiles[] = file.listFiles();
            	if (moreFiles == null)
            		continue;
            	addAttachments(page, moreFiles, soughtFilenames);
            	continue;
            }
            //existing non-Directory file?
            String filename = file.getName();
            //is it a file we want to attach?
            if (foundFile(soughtFilenames, filename)) {
            	//attach the file
	            log.debug("adding attachment: " + file.getName());
	            page.addAttachment(file);
            }
        }
	}

	/**
	 * checks if the given filename can be found in the given vector.
	 * The search is case insensitive. 
	 * @param soughtFilenames
	 * @param filename
	 * @return true if filename is found to be in soughtFilenames
	 */
	protected boolean foundFile(Vector<String> soughtFilenames, String filename) {
		boolean found = soughtFilenames.contains(filename); 
		if (found) return found;
		// check for case insensitivity 
		Pattern caseInsensitiveFilename = Pattern.compile(filename, Pattern.CASE_INSENSITIVE);
		for (String soughtFile : soughtFilenames) {
			Matcher fileFinder = caseInsensitiveFilename.matcher(soughtFile);
			if (fileFinder.matches()) return true;
		}
		return false;
	}

	/**
	 * Determine which images the page is going to need.
	 * Mediawiki images are not associated with a particular page.
	 * @param page the given page
	 * @return String Vector of image names
	 */
	protected Vector<String> getSoughtAttachmentNames(Page page) {
		Vector<String> names = new Vector<String>();
		Set<String> nameSet = new TreeSet<String>();
		String pageText = page.getOriginalText();
		nameSet = getNamesFromImageSyntax(nameSet, pageText);
		nameSet = getNamesFromLinkSyntax(nameSet, pageText);
		names.addAll(nameSet);
		log.debug("found attachment names: " +names.toString());
		return names;
	}

	protected Set<String> getNamesFromImageSyntax(Set<String> nameSet, String pageText) {
		Pattern image = Pattern.compile("!([^!|]+)(?:\\|[^!]+)?!");
		Matcher imageFinder = image.matcher(pageText);
		while (imageFinder.find()) {
			String name = imageFinder.group(1);
			nameSet.add(name);
		}
		return nameSet;
	}
	
	String linkSyntax = 
		"\\[" +			//opening left bracket
		"(" +			//start group
			"?:" +		//but don't capture it
			"[^^]" +	//not a carat
			"[^|]+" +	//anything but a pipe until
			"\\|" +		//a pipe
		")" +			//end group
		"?" +			//and make that group optional
		"\\^" +			//a carat as a part of the string
		"(" +			//start capturing (group1)
			"[^\\]]+" +	//anything but a right bracket until
		")" +			//end capturing (group1)
		"\\]";			//right bracket
	protected Set<String> getNamesFromLinkSyntax(Set<String> nameSet, String pageText) {
		Pattern image = Pattern.compile(linkSyntax);
		Matcher imageFinder = image.matcher(pageText);
		while (imageFinder.find()) {
			String name = imageFinder.group(1);
			nameSet.add(name);
		}
		return nameSet;
	}
}
