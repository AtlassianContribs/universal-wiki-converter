package com.atlassian.uwc.converters.tikiwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Converts lines starting with a space to mono syntax.
 * ex: 
 * Tikiwiki input: 
 *  This should be treated like pre text with other wiki conversions allowed
 *  This line should be part of the previous line's panel. 
 *  Not seperated.
 * 
 * Confluence output:
 * {{This should be treated like pre text with other wiki conversions allowed}}
 * {{This line should be part of the previous line's panel.}} 
 * {{Not seperated.}}
 * 
 * @author Laura Kolker
 *
 */
public class LeadingSpacesConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());

	//regex parts
	String newline = "\r?\n";
	String space = " +";
	String noNewlines = "[^\r\n]+";

	public void convert(Page page) {
		log.debug("Converting Leading Spaces - starting");

		String input = page.getOriginalText();
		String converted = convertLeadingSpaces(input);
		page.setConvertedText(converted);
		log.debug("Converting Leading Spaces - complete");
	}

	String lineStarter = "(^|" + newline + ")";
	String leadingSpaceLine = space + "(" + noNewlines + ")"; 
	/**
	 * @param input tikiwiki input
	 * @return Confluence syntax replacement for the given input
	 */
	private String convertLeadingSpaces(String input) {
		String regex = lineStarter + leadingSpaceLine;
		String replacement = "{group1}{{{group2}}}";
		return RegexUtil.loopRegex(input, regex, replacement);
	}
	
}
