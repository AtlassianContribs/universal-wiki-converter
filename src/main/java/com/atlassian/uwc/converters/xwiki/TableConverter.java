package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Xwiki table syntax to Confluence table syntax.
 */
public class TableConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Tables -- starting");
		String input = page.getOriginalText();
		String converted = convertTables(input);
		page.setConvertedText(converted);
		log.info("Converting Tables -- complete");
	}

	Pattern table = Pattern.compile("" +
			"\\{table\\}" +		//table delimiter
			"\r?\n" +			//newline 
			"(.*?)" +			//table contents, group 1
			"\\{table\\}",		//table delimiter
			Pattern.DOTALL		//dot includes newlines
			);
	/**
	 * converts table syntax
	 * @param input
	 * @return
	 */
	protected String convertTables(String input) {
		Matcher tableFinder = table.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			found = true;
			String contents = tableFinder.group(1);
			String[] lines = contents.split("\n");
			String replacement = "";
			for (int i = 0; i < lines.length; i++) {			//fix table lines
				String line = lines[i];
				line = "| " + line + " |";
				if (i == 0) line = line.replaceAll("\\|", "||"); // header row
				replacement += line;
				if (i < lines.length - 1) replacement += "\n"; 	//add back in newlines except for last one
			}
			replacement = removeUnnecessaryBackslashes(replacement);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern endOfLineBS = Pattern.compile("" +
			"(\\\\)+(\\s*)(?=[|\n]|$)"
			);
	protected String removeUnnecessaryBackslashes(String input) {
		Matcher bsFinder = endOfLineBS.matcher(input);
		return RegexUtil.loopRegex(bsFinder, input, "{group2}");
	}

}
