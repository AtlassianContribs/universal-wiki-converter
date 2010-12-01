package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class RedirectConverter extends BaseConverter {
	IllegalLinkNameConverter illegalConverter = new IllegalLinkNameConverter();

	//{replace-with}{redirect:$1}
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertRedirect(input);
		page.setConvertedText(converted);
	}

	Pattern redirect = Pattern.compile("#REDIRECT\\s*\\[\\[(.*?)\\]\\]");
	protected String convertRedirect(String input) {
		Matcher matcherFinder = redirect.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (matcherFinder.find()) {
			found = true;
			String pagetitle = matcherFinder.group(1);
			pagetitle = illegalConverter.convertIllegalName(pagetitle);
			String replacement = "{redirect:" + pagetitle+ "}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			matcherFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			matcherFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
