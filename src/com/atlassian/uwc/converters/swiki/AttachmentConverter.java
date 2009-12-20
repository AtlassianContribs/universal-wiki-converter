/**
 * AttachmentProcessor.java
 *
 * $Revision$
 * Aug 24, 2007
 */
package com.atlassian.uwc.converters.swiki;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.HashSet;
import java.io.File;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.ConfluenceSettingsForm;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.UWCForm2;
import com.atlassian.uwc.util.PropertyFileManager;

/**
 * For processing attachments that have been migrated from the siwki format.
 * This will attach the attachments to the proper pages.
 * @author Kelly Meese
 * @version 1.0
 */
public class AttachmentConverter extends BaseConverter
{
	private static final String FILE_SEP = System.getProperty("file.separator");
	
	ConfluenceSettingsForm confSettings = null;
	Pattern pattern = null;
	Matcher matcher = null;	
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page)
	{
		//confSettings = UWCForm2.getInstance().getConfluenceSettingsForm();
		//String attachDir = confSettings.getAttachmentDirectory();
		String attachDir = this.getAttachmentDirectory();
		log.info("Attaching Attachments -- starting");

		page = addAttachment(page, attachDir);

		log.info("Attaching Attachments -- completed");
	}
	
	/**
	 * Adds attachments to a page.
	 * @param page object to attach to
	 * @param attachDir location of attachments
	 * @return Page
	 */
	public Page addAttachment(Page page, String attachDir){
		boolean found = false;
		StringBuffer sb = new StringBuffer();
		String replacement="";
		String fileName="";
		File attachFile;
		String pageText = page.getOriginalText();
		Set<File> pageAttachments = new HashSet<File>();
		pattern = Pattern.compile("\\*\\+([^\\*\\+]*)\\+\\*");
		matcher = pattern.matcher(pageText);
		while(matcher.find())
		{
			found = true;
			fileName = matcher.group(1);
			int index=fileName.indexOf('>');
			if (index >=0 )
			{
				String temp=fileName.substring(0, index);
				fileName=fileName.substring(index + 1);
				replacement="[" + temp + "|^" + fileName + "]";
				
			}
			else
				replacement = "[^" + fileName + "]";
			matcher.appendReplacement(sb, replacement);
			
			// TODO: Find that file
			attachFile = new File(attachDir + FILE_SEP + fileName);
			pageAttachments.add(attachFile);
			
		}
		matcher.appendTail(sb);

		if(found)
			pageText = sb.toString();
		
		//now add picture files
		pattern = Pattern.compile("\\s\\!(.*?)\\!\\s");
		matcher = pattern.matcher(pageText);
		while(matcher.find())
		{
			found = true;
			fileName = matcher.group(1);
			attachFile = new File(attachDir + FILE_SEP + fileName);
			pageAttachments.add(attachFile);
			
		}
		// Attach the files to the page
		page.setAttachments(pageAttachments);
		// Set the converted Text
		page.setConvertedText(pageText);

		return page;
	}
}
