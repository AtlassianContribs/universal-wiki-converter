package com.atlassian.uwc.converters.socialtext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.IllegalNameConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts illegal pagename characters in include macro references.
 */
public class IncludeIllegalConverter extends IllegalNameConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Illegal Chars in Include - start");
		String input = page.getOriginalText();
		String converted = convertIllegalChars(input);
		page.setConvertedText(converted);
		log.info("Converting Illegal Chars in Include - complete");
	}
	Pattern include = Pattern.compile("(\\{include:[^\\[]*)\\[([^\\]]+)\\]");
	protected String convertIllegalChars(String input) {
		Matcher includeFinder = include.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (includeFinder.find()) {
			found = true;
			String pre = includeFinder.group(1);
			String pagename = includeFinder.group(2);
			pagename = this.convertIllegalName(pagename);
			String replacement = pre + "[" + pagename + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			includeFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			includeFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
