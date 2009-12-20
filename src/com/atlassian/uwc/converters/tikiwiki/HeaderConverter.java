package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class HeaderConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Headers - starting");
		String input = page.getOriginalText();
		String converted = convertHeaders(input);
		page.setConvertedText(converted);
		log.debug("Converting Header - complete");
	}
	
	Pattern headerPattern = Pattern.compile("(?<=^|\n)(!+)(.*)");
	protected String convertHeaders(String input) {
		Matcher headerFinder = headerPattern.matcher(input);
		
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (headerFinder.find()) {
			found = true;
			String marks = headerFinder.group(1);
			String contents = headerFinder.group(2);
			int level = marks.length();
			if (!contents.startsWith(" ")) contents = " " + contents;
			String replacement = "h" + level + "." + contents;
			headerFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			headerFinder.appendTail(sb);
			return sb.toString();
		}
		return input; 
	}

}
