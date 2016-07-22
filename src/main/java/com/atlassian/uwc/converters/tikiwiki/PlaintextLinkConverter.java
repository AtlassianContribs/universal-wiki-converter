package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles unlinkifying plain text links that otherwise would be rendered as links
 * by Confluence. It does tThis is feature is not turned on by default in the tikiwiki converter
 * properties
 */
public class PlaintextLinkConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Plain Text Links - start");
		String input = page.getOriginalText();
		String converted = convertPlainLinks(input);
		page.setConvertedText(converted);
		log.info("Converting Plain Text Links - complete");
	}
	
	String plaintextLinks = 
		"(" +				//start capture (group1)
			"[^|\"\\[]" +	//not a pipe, quote, or left bracket
			"https?" +		//http or https
		")" +				//end capture (group1)
		"(" +				//start capture (group2)
				":" +		//a colon
		")";				//end captrue (group2)
	protected String convertPlainLinks(String input) {
		String replacement = "{group1}&#58;";
		String converted = RegexUtil.loopRegex(input, plaintextLinks, replacement);
		converted = notInCodeBlock(converted);
		return converted;
	}
	
	String inCodeBlock = "(\\{CODE[^\\}]*\\})(.*?)(\\{CODE\\})";
	Pattern inCodePattern = Pattern.compile(inCodeBlock, Pattern.DOTALL);
	/**
	 * changes the colon entities back to original character if found in a code block
	 * @param input 
	 * @return
	 */
	protected String notInCodeBlock(String input) {
		Matcher inCodeFinder = inCodePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (inCodeFinder.find()) {
			found = true;
			String pre = inCodeFinder.group(1);
			String code = inCodeFinder.group(2);
			String post = inCodeFinder.group(3);
			log.debug("Found code block: " + code);
			String replacement = pre + 
				code.replaceAll("(https?)&#58;", "$1:") +
				post;
			inCodeFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			inCodeFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
