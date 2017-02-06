package com.atlassian.uwc.converters.trac;

import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ListConverter extends BaseConverter {

	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertList(input);
		converted = escapeNonListContextDashes(converted);
		page.setConvertedText(converted);
	}

	Pattern dashes = Pattern.compile("(?<![-\n]|^)([-]+)");
	protected String escapeNonListContextDashes(String input) {
		Matcher dashFinder = dashes.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (dashFinder.find()) {
			found = true;
			String dashes = dashFinder.group(1);
			String replacement = escapeDashes(dashes);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			dashFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			dashFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected String escapeDashes(String input) {
		String out = "";
		for (char c : input.toCharArray()) {
			out += "\\-";
		}
		return out;
	}

	Pattern list = Pattern.compile("(?<=\n|^)( *([-*]|(?:1\\.))[^\n]*\n)+");
	Pattern listline = Pattern.compile("(?m)^( *([-*]|(?:1\\.)))");
	protected String convertList(String input) {
		Matcher listFinder = list.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		
		while (listFinder.find()) {
			found = true;
			String list = listFinder.group();
			Matcher lineFinder = listline.matcher(list);
			StringBuffer sb2 = new StringBuffer();
			boolean found2 = false;
			String replacement = "";
			Stack<String> delims = new Stack<String>();
			while (lineFinder.find()) {
				found2 = true;
				String thislist = lineFinder.group(1);
				boolean repeat = delims.contains(thislist);
				if (repeat && !delims.peek().equals(thislist)) {
					while (!delims.peek().equals(thislist)) delims.pop();
				}
				else if (!repeat) delims.push(thislist);
				lineFinder.appendReplacement(sb2, getConfDelim(delims));
			}
			if (found2) {
				lineFinder.appendTail(sb2);
				replacement = sb2.toString();
			}
			else continue;
			delims.clear();
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			listFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected String getConfDelim(Stack<String> delims) {
		Vector<String> current = new Vector<String>();
		current.addAll(delims);
		String out = "";
		for (String delim : current) {
			if (delim.contains("-")) {
				out += "-";
			}
			else if (delim.contains("*")) {
				out += "*";
			}
			else if (delim.contains("1")) {
				out += "#";
			}
		}
		return out;
	}

}
