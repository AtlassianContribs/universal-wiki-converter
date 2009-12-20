package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * transforms multi line mono syntax from:
 * <pre>
 * {{
 * foo
 * bar
 * }}
 * </pre>
 * to
 * <pre>
 * {{foo}}
 * {{bar}}
 * </pre>
 */
public class MultiLineMonoConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertMultiLineMono(input);
		page.setConvertedText(converted);
		
	}

	Pattern mono = Pattern.compile("" +
			"[{]{2,2}" +
			"(.*?)[}]{2,2}", Pattern.DOTALL);
	protected String convertMultiLineMono(String input) {
		Matcher monoFinder = mono.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (monoFinder.find()) {
			found = true;
			String content = monoFinder.group(1).trim();
			content = content.replaceAll("[\r]?\n", "}}\n{{");
			String replacement = "{{" + RegexUtil.handleEscapesInReplacement(content) + "}}";
			monoFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			monoFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
