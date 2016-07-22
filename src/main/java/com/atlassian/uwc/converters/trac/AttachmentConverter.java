package com.atlassian.uwc.converters.trac;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Prepares Trac attachments for uploading to Confluence. All attachments for a
 * page are uploaded, even if they are not referenced in the page.
 * 
 * @author Laura Kolker
 * @author Stefan Gybas
 */
public class AttachmentConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass()); 

	public void convert(Page page) {
		log.info("Converting Trac Attachments -- starting");
        // scan the attachment directory and create a list of attachments
        addAttachmentsToPage(page, this.getAttachmentDirectory());
		log.info("Converting Trac Attachments -- complete");

	}

	/**
     * determines which attachments are present for the specified page in the
     * Trac attachments directory and attaches them all
     * @param page object to attach pages to
     */
    protected void addAttachmentsToPage(Page page, String attachmentDir) {
        String pathtopage = page.getName();
        if (pathtopage.contains("%")) pathtopage = urldecode(pathtopage);
		File attachmentPageDir = new File(attachmentDir + File.separator + pathtopage);
        log.debug("Examining attachment directory: "
                + attachmentPageDir.getAbsolutePath());
        File attachments[] = attachmentPageDir.listFiles();
        if (attachments == null || attachments.length == 0) {
            log.info("No attachments found in directory: "
                    + attachmentPageDir.getAbsolutePath());
            return;
        }

        for (File file : attachments) {
        	if (file.isFile()) {
        		String name = urldecode(file.getName());
        		page.addAttachment(file, name);
        	}
        }
    }

	private String urldecode(String pathtopage) {
		IllegalLinkNameConverter converter = new IllegalLinkNameConverter(); 
		return converter.decodeUrl(pathtopage);
	}
}
