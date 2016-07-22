package com.atlassian.uwc.converters.tikiwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * gets rid of extra newlines between nested list items
 */
public class NestedListNewlineRemover extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Removing Nested List Newlines - start");
		String input = page.getOriginalText();
		String converted = removeNestedListNewlines(input);
		page.setConvertedText(converted);
		log.info("Removing Nested List Newlines - complete");
	}
	
	String listChars = "[*#]";
	String nestedListNL = 
		"(?<=^|\n)" +		//zero-width look behind beginning of string or newline
		"(" +				//start capture (group 1)
			listChars +	"{2,}" +		//two or more list chars
			"[^\n]+" +		//anything not a newline until
		")" +				//end capture (group 1)
		"(?:" +				//start non-capture group
			"\n{2,}" +		//two or more newlines
		")" +				//end non-capture group
		"(" +				//start capture (group 2)
			listChars + "{2,}" +		//two or more list chars
		")";				//end capture (group 2)
	protected String removeNestedListNewlines(String input) {
		String replacement = "{group1}\n{group2}";
		return RegexUtil.loopRegex(input, nestedListNL, replacement);
	}

}
