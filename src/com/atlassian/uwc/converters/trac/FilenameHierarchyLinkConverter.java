package com.atlassian.uwc.converters.trac;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class FilenameHierarchyLinkConverter extends BaseConverter {

	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertLink(input);
		page.setConvertedText(converted);
	}

	Pattern link = Pattern.compile("\\[([^\\]]+)\\]");
	protected String convertLink(String input) {
		Matcher linkFinder = link.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String link = linkFinder.group(1);
			String alias = "";
			if (link.contains("|")) {
				String[] parts = link.split("\\|");
				alias = parts[0];
				link = parts[1];
			}
			if (link.startsWith("#")) continue; //anchor, skip
			if (link.startsWith("^")) continue; //attachment, skip
			if (!link.contains("/")) continue; //link will be fine with no changes
			if (link.startsWith("http") ||
	                  link.startsWith("mailto:") ||
	                  link.startsWith("file:")) continue; //external link: skip

			
			String[] nodes = link.split("\\/");
			link = nodes[nodes.length-1];
			if (!"".equals(alias)) alias += "|";
			String replacement = "[" + alias + link + "]";
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
