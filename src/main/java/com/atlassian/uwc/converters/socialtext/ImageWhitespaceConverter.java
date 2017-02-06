package com.atlassian.uwc.converters.socialtext;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**  
 * Transforms references to images where the corresponding file uses %20 for whitespace
 * but the page text uses a space character.
 */
public class ImageWhitespaceConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Image Whitespace - start");
		
		String input = page.getOriginalText();
		String converted = convertImageWhitespace(input, page.getAttachments());
		page.setConvertedText(converted);

		log.info("Converting Image Whitespace - complete");
	}

	protected String convertImageWhitespace(String input, Set<File> attachments) {
		for (Iterator iter = attachments.iterator(); iter.hasNext();) {
			File att = (File) iter.next();
			String name = att.getName();
			if (!name.contains("%20")) continue;
			String newname = name.replaceAll("%20", " ");
			String regex = "\\Q" + newname + "\\E" + 	//quoted name with space 
				"(" +									//group 1 starts
					"([|][^!]+)?" +						//optional pipe with non! characters
					"!|\\]" +							//! or ]
				")";									//group 1 ends
			String replacement = name + "{group1}";
			input = RegexUtil.loopRegex(input, regex, replacement);
		}
		return input;
	}

}
