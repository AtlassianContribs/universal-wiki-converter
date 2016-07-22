package com.atlassian.uwc.converters.twiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class AutoNumberListConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertNumberedList(input);
		page.setConvertedText(converted);
	}

	String delim = "";
	String origDelim = "";
	Pattern autoNumber = Pattern.compile("" +
			"(^|(?:\n?\n))" +
			"( {3,})" +
			"([1aiAI])\\.? " +
			"([^\n]+)");
	protected String convertNumberedList(String input) {
		Matcher autoFinder = autoNumber.matcher(input);
		boolean found = false;
		StringBuffer sb = new StringBuffer();
		while (autoFinder.find()) {
			found = true;
			String pre = autoFinder.group(1);
			int numSpaces = autoFinder.group(2).length();
			String type = autoFinder.group(3);
			String content = autoFinder.group(4);
			if ((numSpaces % 3) != 0) continue;
			if (pre.length() > 1 && "\n\n".equals(pre)) this.delim = this.origDelim = "";
			this.delim = getCurrentDelim(type);
			String replacement = pre + delim + " " + content;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			autoFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			autoFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	protected String getCurrentDelim(String type) {
		if (this.origDelim.endsWith(type)) return this.delim;
		if (this.origDelim.contains(type)) {
			this.origDelim = this.origDelim.replaceFirst(type + ".*", type);
			this.delim = this.delim.substring(0, this.origDelim.length());
			return this.delim;
		}
		this.origDelim += type;
		this.delim += "#";
		return this.delim;
	}

}
