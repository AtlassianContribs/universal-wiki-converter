package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converters xwiki numbered list syntax and mixed list syntax to
 * Confluence lists syntax.
 * Note: Xwiki has many specialized numbered list syntaxes (alphabetical,
 * greek, etc.) These are all converted to Confluence simple numbering syntax.
 */
public class ListConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Lists -- starting");
		String input = page.getOriginalText();
		String converted = convertLists(input);
		page.setConvertedText(converted);
		log.info("Converting Lists -- complete");
	}

	public static final String LIST_DELIM_CHARCLASS = "1*aAiIghk";
	Pattern listPattern = Pattern.compile("" +
			"(?<=^|\n)" +
			"([" +
				LIST_DELIM_CHARCLASS +
			"]+)" +
			"\\.?" +
			"(?=\\s)"
			);
	/**
	 * converts instances of numbered and mixed lists
	 * in the given input
	 * @param input
	 * @return
	 */
	protected String convertLists(String input) {
		Matcher listFinder = listPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (listFinder.find()) {
			found = true;
			String listDelim = listFinder.group(1);
			listDelim = convertNums(listDelim);
			listDelim = RegexUtil.handleEscapesInReplacement(listDelim);
			listFinder.appendReplacement(sb, listDelim);
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	/**
	 * converts xwiki numbered list delimiters to 
	 * confluence numbered list delimiters
	 * @param input
	 * @return
	 */
	protected String convertNums(String input) {
		return input.replaceAll("[1aAiIghk]", "#");
	}

}
