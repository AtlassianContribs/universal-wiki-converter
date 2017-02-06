package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * converts Jspwiki header syntax to Confluence syntax (h1.)
 */
public class HeaderConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Headers - start");
		
		String input = page.getOriginalText();
		
		String converted = convertHeaders(input);
		
		page.setConvertedText(converted);
		
		log.info("Converter Headers - complete");
	}
	
	String jspHeader = "(?<=^)" + 	//zero-width uncaptured beginning of string OR newline - (we compile with MULTILINE mode)
			"(!{1,3})" +			//1-3 exclamation points
			"\\s*" +				//0 or more whitespace
			"([^\n]+)" +			//anything not a newline (group 1)
			"(\n|$)";				//newline OR end of string (group 2, but we don't use captured group)
	Pattern jspHeaderPattern = Pattern.compile(jspHeader, Pattern.MULTILINE);
	protected String convertHeaders(String input) {
		Matcher jspHeaderFinder = jspHeaderPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (jspHeaderFinder.find()) {
			found = true;
			int jspDepth = jspHeaderFinder.group(1).length();
			int confDepth = switchOuterDepth(jspDepth);
			String header = jspHeaderFinder.group(2);
			String replacement = "h" + confDepth + ". " + header;
			if (!replacement.endsWith("\n")) {
				replacement += "\n";
			}
			replacement = escapePatternChars(replacement);
			jspHeaderFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			jspHeaderFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	

	/**
	 * returns Confluence header level for given JSP header level. 
	 * Note: Confluence and JSPWiki have opposite ordered headers.
	 * @param jspDepth integer representing JSPwiki header level
	 * @return comparable Confluence header
	 */
	protected int switchOuterDepth(int jspDepth) {
		switch (jspDepth) {
			case (1): return 3;
			case (2): return 2;
			case (3): return 1;
		}
		throw new IllegalArgumentException("argument: " + jspDepth + "\nswitchOuterDepth only handles levels 1 through 3.");
	}

	String zerowidth_notabackslash = "(?<=[^\\\\])";
	String patternChars = "([$])";
	Pattern pcPattern = Pattern.compile(zerowidth_notabackslash+patternChars);
	protected String escapePatternChars(String input) {
		Matcher pcFinder = pcPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (pcFinder.find()) {
			found = true;
			String patternChar = pcFinder.group(1);
			log.info("patternChar = " + patternChar);
			String replacement = "\\\\\\" + patternChar;
			log.info("replacement = " + replacement);
			pcFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			pcFinder.appendTail(sb);
			log.info(sb.toString());
			return sb.toString();
		}
		return input;
	}

	
}
