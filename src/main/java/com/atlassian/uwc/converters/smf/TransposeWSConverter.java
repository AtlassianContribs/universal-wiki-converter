package com.atlassian.uwc.converters.smf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class TransposeWSConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertBasic(input);
		page.setConvertedText(converted);
	}

	protected String convertBasic(String input) {
		input = transposeWs(input, "[biu]");
		return input;
	}

	Pattern ws = Pattern.compile("(\\s+)(\\S*)");
	protected String transposeWs(String input, String tagCharClass) {
		//look for only whitespace after tag. transpose that
		String regex = "(?s)(\\["+tagCharClass+"\\])(\\s+)";
		input = RegexUtil.loopRegex(input, regex, "{group2}{group1}");
		//loog for only whitespace before end tag. transpose that
		regex = "(?s)(\\s+)(\\[\\/" + tagCharClass + "\\])";
		input = RegexUtil.loopRegex(input, regex, "{group2}{group1}");
		//look for whitespace between nested start and end blocks
		regex = "(?s)(\\[" + tagCharClass + "\\])(\\s+)(\\[" + tagCharClass + "\\])";
		input = RegexUtil.loopRegex(input, regex, "{group1}{group3}");
		regex = "(?s)(\\[\\/" + tagCharClass + "\\])(\\s+)(\\[\\/" + tagCharClass + "\\])";
		input = RegexUtil.loopRegex(input, regex, "{group1}{group3}");
		//look for newlines in the middle parts, and transform that to spaces
		regex = "(?s)(\\[(" + tagCharClass + ")\\])" + "(.*?)" + "(\\[\\/\\2\\])";
		Matcher nlFinder = Pattern.compile(regex).matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (nlFinder.find()) {
			found = true;
			String contents = nlFinder.group(3);
			String replacement = contents.replaceAll("[\n]", " ");
			replacement = nlFinder.group(1) + replacement + nlFinder.group(4);
			nlFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			nlFinder.appendTail(sb);
			input = sb.toString();
		}
		return input;
	}

}
