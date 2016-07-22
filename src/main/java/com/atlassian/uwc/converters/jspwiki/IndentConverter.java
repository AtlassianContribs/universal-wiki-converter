package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class IndentConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertIndent(input);
		page.setConvertedText(converted);
	}

	Pattern altstyle = Pattern.compile("" +
			"%%\\(([^)]+)\\)(.*?)%%", Pattern.DOTALL);
	protected String convertIndent(String input) {
		Matcher altFinder = altstyle.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (altFinder.find()) {
			//only worry about alt styles with margin or text-indent 
			String attributes = altFinder.group(1);
			if (!attributes.contains("margin") && !attributes.contains("text-indent")) continue;

			found = true;
			String content = altFinder.group(2);
			String newattributes = removeIndent(attributes);
			String replacement = "{indent}\n" +
					"%%(" + newattributes + ")" +
					content +
					"%%\n" +
					"{indent}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			altFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			altFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern indent = Pattern.compile("" +
			"((margin[^;]+)|(text-indent[^;]+))(;|$)");
	protected String removeIndent(String input) {
		Matcher indentFinder = indent.matcher(input);
		if (indentFinder.find()) {
			return indentFinder.replaceAll("");
		}
		return input;
	}

}
