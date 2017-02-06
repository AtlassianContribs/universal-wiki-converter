package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class FileLinkBackslashConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertFileLinkBackslashes(input);
		page.setConvertedText(converted);
	}
	
	Pattern filelink = Pattern.compile("" +
			"([\\[|]) *file:([^\\]]+)");
	protected String convertFileLinkBackslashes(String input) {
		Matcher filelinkFinder = filelink.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (filelinkFinder.find()) {
			found = true;
			String delim = filelinkFinder.group(1);
			String link = filelinkFinder.group(2);
			String replacement = delim + "file:" + link.replaceAll("\\\\", "\\/");
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			filelinkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			filelinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
