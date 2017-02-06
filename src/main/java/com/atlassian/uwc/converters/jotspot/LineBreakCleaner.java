package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * turns linebreaks &lt;br /&gt; into newlines,
 * and converts horizontal rules &lt;hr /&gt;
 * @author Laura Kolker
 */
public class LineBreakCleaner extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Line Break Cleaner - starting");

		String input = page.getOriginalText();
		String converted = convertBreaks(input);
		converted = convertRules(converted);
		
		page.setConvertedText(converted);
		log.debug("Line Break Cleaner - complete");
	}
	
	String breaks = "\\<br[^>]*?\\/\\>";
	Pattern breakPattern = Pattern.compile(breaks);
	private String convertBreaks(String input) {
		String converted = input;
		String replacement = "\n"; //newlines were giving me trouble in the properties file

		Matcher m = breakPattern.matcher(input);
		if (m.find()) {
			log.debug("found breaks " + m.group());
			converted = m.replaceAll(replacement);
		}
		return converted;
	}
	
	String horizRule = "<hr\\s*\\/>";
	Pattern hrPattern = Pattern.compile(horizRule);
	private String convertRules(String input) {
		String converted = input;
		String replacement = "\n----\n"; //newlines are troublesome in props file
		
		Matcher ruleFinder = hrPattern.matcher(input);
		if (ruleFinder.find()) {
			log.debug("found HR");
			converted = ruleFinder.replaceAll(replacement);
		}
		return converted;
	}
}
