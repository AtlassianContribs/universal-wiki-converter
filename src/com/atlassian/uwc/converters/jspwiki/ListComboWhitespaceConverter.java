package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ListComboWhitespaceConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertListComboWs(input);
		page.setConvertedText(converted);
	}
	
	Pattern listCombo = Pattern.compile("" +
			"(^|\n)([*#]+)([^*#\\s])([^\n]*)");
	protected String convertListComboWs(String input) {
		Matcher comboFinder = listCombo.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (comboFinder.find()) {
			found = true;
			String nl = comboFinder.group(1);
			String list = comboFinder.group(2);
			String notspace = comboFinder.group(3);
			String rest = comboFinder.group(4);
			
			String onlybold = rest.replaceAll("[^*]", "");
			int length = onlybold.length();
			if (!((length % 2) == 0)) continue;
			String replacement = nl + list + " " + notspace + rest;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			comboFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			comboFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
