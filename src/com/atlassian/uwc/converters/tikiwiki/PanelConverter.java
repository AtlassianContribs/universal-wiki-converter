package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * converts tikiwiki panel syntax to Confluence panel syntax:
 * 
 * Example in:
 * ^
 * Something
 * ^
 * 
 * Example out:
 * {panel}
 * Something
 * {panel}
 * 
 * See PanelConverterTest for further examples.
 * @author Laura Kolker
 *
 */
public class PanelConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());

	public void convert(Page page) {
		log.debug("Converting Panels - starting");
		String input = page.getOriginalText();
		String converted = convertPanel(input);
		page.setConvertedText(converted);

		log.debug("Converting Panels - complete");
	}
	
	String caretOnly = "(?<=^|\n)\\^(?=\n|$)";

	/**
	 * @param input tikiwiki input
	 * @return Confluence syntax replacement for the given input
	 */
	protected String convertPanel(String input) {
		String regex = caretOnly + "(.*?)" + caretOnly; 
		Pattern p = Pattern.compile(regex, Pattern.DOTALL);
		Matcher m = p.matcher(input);
		String replacement = "{panel}{group1}{panel}";
		return RegexUtil.loopRegex(m, input, replacement);
	}
	
}
