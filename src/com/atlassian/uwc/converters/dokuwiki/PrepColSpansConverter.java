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

	Pattern colspan = Pattern.compile("[|]{2,}");
	protected String prep(String input) {
		Matcher spanFinder = colspan.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (spanFinder.find()) {
			found = true;
			String spans = spanFinder.group();
			int spanlength = spans.length();
			String replacement = DELIM +TOKENKEY+spanlength+DELIM +"|";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			spanFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			spanFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


}
