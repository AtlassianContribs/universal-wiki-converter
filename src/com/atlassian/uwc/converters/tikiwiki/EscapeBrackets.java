package com.atlassian.uwc.converters.tikiwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles escaping tikiwiki brackets so that they are
 * correctly formatted, as opposed to being formatted as links
 */
public class EscapeBrackets extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Escaping Brackets - start");

		String input = page.getOriginalText();
		String converted = escapeBrackets(input);
		page.setConvertedText(converted);
		
		log.debug("Escaping Brackets - complete");
	}
	
	String doubleBrackets = "\\[\\[([^\\]]*?)\\]"; 
	/**
	 * escapes tikiwiki brackets that have no syntactical meaning
	 * @param input
	 * @return
	 */
	protected String escapeBrackets(String input) {
		String replacement = "\\\\[{group1}\\\\]";
		return RegexUtil.loopRegex(input, doubleBrackets, replacement);
	}

}
