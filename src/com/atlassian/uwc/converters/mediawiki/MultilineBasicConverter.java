package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class MultilineBasicConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertMultiline(input);
		page.setConvertedText(converted);
	}

	Pattern basic = Pattern.compile("(?s)('{2,5}|<([bis]|(?:tt)|(?:strong))>)(.*?)(\\1|(?:<\\/\\2>))");
	Pattern nl = Pattern.compile("([^\n]*)\n\\s*");
	protected String convertMultiline(String input) {
		Matcher basicFinder = basic.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (basicFinder.find()) {
			found = true;
			String type = basicFinder.group(1);
			String delim = getDelim(type);
			String enddelim = getEndDelim(delim);
			String content = basicFinder.group(3);
			int end = basicFinder.end();
			if (!content.contains("\n")) continue;
			Matcher nlFinder = nl.matcher(content);
			StringBuffer inner = new StringBuffer();
			boolean first = true;
			while (nlFinder.find()) {
				String innercontent = nlFinder.group(1).trim(); //CONTINUE
				if (first && "".equals(innercontent)) {    //if no content on first line
					nlFinder.appendReplacement(inner, ""); //replaces first newline with nothing
					continue;
				}
				if (first) inner.append(delim);
				nlFinder.appendReplacement(inner, innercontent + enddelim + "\n" + delim);
				first = false;
			}
			nlFinder.appendTail(inner);
			String innerString = inner.toString().trim();
			if (!innerString.endsWith(enddelim)) innerString += enddelim;
			else innerString = innerString.replaceFirst(".$", ""); //remove last char
			if (innerString.endsWith(delim+enddelim)) //edge case: *__* 
				innerString = innerString.replaceFirst("\n\\Q"+delim+enddelim+"\\E$", "");
			String replacement = RegexUtil.handleEscapesInReplacement(innerString);
			basicFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			basicFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	private String getEndDelim(String delim) {
		if ("{{".equals(delim)) return "}}";
		if ("*_".equals(delim)) return "_*";
		return delim;
	}
	private String getDelim(String type) {
		if ("''".equals(type) || "<i>".equals(type)) return "_";
		if ("'''".equals(type) || "<b>".equals(type) || "<strong>".equals(type)) return "*";
		if ("<s>".equals(type)) return "-";
		if ("<tt>".equals(type)) return "{{";
		return "*_";
	}

}
