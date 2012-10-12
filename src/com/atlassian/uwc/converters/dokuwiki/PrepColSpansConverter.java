package com.atlassian.uwc.converters.dokuwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class PrepColSpansConverter extends BaseConverter {

	public static final String TOKENKEY = "UWCTOKENCOLSPANS:";
	private static final String DELIM = "::";
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = prep(input);
		page.setConvertedText(converted);
	}

	Pattern colspan = Pattern.compile("[|^]{2,}");
	protected String prep(String input) {
		input = removeEmptyColspanLines(input);
		Matcher spanFinder = colspan.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (spanFinder.find()) {
			found = true;
			String spans = spanFinder.group();
			int spanlength = spans.length();
			String replacement = DELIM +TOKENKEY+spanlength+DELIM +spans.substring(0, 1);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			spanFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			spanFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern emptycolspan = Pattern.compile("(\n{2,})\\|+(?=\n)");
	private String removeEmptyColspanLines(String input) {
		Matcher emptyFinder = emptycolspan.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (emptyFinder.find()) {
			found = true;
			String replacement = emptyFinder.group(1);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			emptyFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			emptyFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


}
