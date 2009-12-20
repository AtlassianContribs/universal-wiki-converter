package com.atlassian.uwc.converters.socialtext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * transforms socialtext table syntax to confluence table syntax
 */
public class TableConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Tables - start");
		String input = page.getOriginalText();
		String converted = convertTables(input);
		page.setConvertedText(converted);
		log.info("Converting Tables - complete");
	}
	
	protected String convertTables(String input) {
		input = removeProperties(input);
		input = condenseLines(input);
		return input;
	}

	Pattern tableprops = Pattern.compile("" +
			"(?<=^|\n)" +
			"\\|\\| *" +
			"((sort[^\n]+\n)|(border[^\n]+\n))");
	/**
	 * removes socialtext table properties like sort and border
	 * @param input
	 * @return
	 */
	protected String removeProperties(String input) {
		Matcher propFinder = tableprops.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (propFinder.find()) {
			String abc = propFinder.group();
			found = true;
			propFinder.appendReplacement(sb, "");
		}
		if (found) {
			propFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern append = Pattern.compile("" +
			"([|][^|\n\\]]+?)\n");
	/**
	 * handles whitespace in table syntax 
	 * @param input
	 * @return
	 */
	protected String condenseLines(String input) {
		Matcher appendFinder =  append.matcher(input);
		return RegexUtil.loopRegex(appendFinder, input, "{group1}");
	}

}
