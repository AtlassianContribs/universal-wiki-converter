package com.atlassian.uwc.converters.twiki.cleaners;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.converters.twiki.ContentCleaner;

public class TableHeader implements ContentCleaner
{

	Pattern tablelines = Pattern.compile("" +
			"(?<=\n|^)([|][^\n]+)"); 
	
	public String clean(String input) {
		Matcher tablelinesFinder = tablelines.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tablelinesFinder.find()) {
			found = true;
			String line = tablelinesFinder.group(1);
			line = line.replaceAll("\\* *\\| *\\*", "||");
			line = line.replaceAll("\\| *\\*", "||");
			line = line.replaceAll("\\* *\\|", "||");
			String replacement = line;
			
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tablelinesFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tablelinesFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
}
