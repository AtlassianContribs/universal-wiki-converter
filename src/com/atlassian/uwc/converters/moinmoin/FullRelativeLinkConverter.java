package com.atlassian.uwc.converters.moinmoin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class FullRelativeLinkConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertLinks(input, page.getName());
		page.setConvertedText(converted);
	}

	Pattern relativelink = Pattern.compile("\\[\\[" +
			"\\/" +
			"([^\\]]*)" +
			"\\]\\]");
	protected String convertLinks(String input, String title) {
		Matcher linkFinder = relativelink.matcher(input);
		if (title.endsWith(".txt")) { //if we end with .txt, get rid of it
			title = title.replaceAll("\\.txt$", ""); 
		}
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String linkcontent = linkFinder.group(1);
			//check for alias
			String alias = "";
			if (linkcontent.contains("|")) {
				String[] parts = linkcontent.split("\\|");
				linkcontent = parts[0];
				alias = parts[1];
			}
			linkcontent = title + " " + linkcontent; //create a full relative link. 
			String replacement = ("".equals(alias))?
					"[" + linkcontent + "]": //no alias
					"[" + alias + "|" + linkcontent + "]" ; //has alias
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
