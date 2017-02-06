package com.atlassian.uwc.converters.dokuwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class UNCConverter extends BaseConverter {

	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertUNC(input);
		page.setConvertedText(converted);
		
	}

	Pattern unc = Pattern.compile("\\[\\[\\\\([^\\]|]*)\\]\\]");
	protected String convertUNC(String input) {
		input = convertWithAlias(input);
		Matcher uncFinder = unc.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (uncFinder.find()) {
			found = true;
			String link = uncFinder.group(1);
			String filelink = link.replaceAll("[\\\\]", "/");
			String replacement = "[\\"+link+"|file:/" + filelink + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			uncFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			uncFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern uncalias = Pattern.compile("\\[\\[\\\\([^\\]]*)\\s*\\|\\s*([^\\]]*)\\]\\]");
	public String convertWithAlias(String input) {
		Matcher uncAliasFinder = uncalias.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (uncAliasFinder.find()) {
			found = true;
			String alias = uncAliasFinder.group(2);
			String link = uncAliasFinder.group(1);
			link = "file:/" + link.replaceAll("[\\\\]", "/");
			String replacement = "[" + alias + "|" + link + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			uncAliasFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			uncAliasFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	
}
