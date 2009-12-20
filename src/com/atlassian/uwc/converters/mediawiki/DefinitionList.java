package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class DefinitionList extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertDefList(input);
		page.setConvertedText(converted);
	}

	Pattern definitionList = Pattern.compile("" +
			"(^|\n)" +
			";([^:]+)" +
			":([^\n]+)" +
			"((\n:[^\n]+)*)"
			);
	protected String convertDefList(String input) {
		Matcher defListFinder = definitionList.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (defListFinder.find()) {
			found = true;
			String pre = defListFinder.group(1);
			String word = defListFinder.group(2).trim();
			if (word.endsWith("\n")) word = word.substring(0, word.length()-1);
			String definition = defListFinder.group(3);
			definition = cleanWS(definition);
			String optDefs = defListFinder.group(4);
			if (optDefs == null) optDefs = "";
			optDefs = optDefs.replaceAll("\n:", "\n");
			optDefs = cleanWS(optDefs);
			String replacement = pre + "* " + word + "\n" + definition + optDefs;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			defListFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			defListFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	String nlNoSpace = "(^|\n)( +)";
	protected String cleanWS(String input) {
		return RegexUtil.loopRegex(input, nlNoSpace, "{group1}");
	}

}
