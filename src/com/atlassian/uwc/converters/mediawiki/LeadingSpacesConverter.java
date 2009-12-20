package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts lines starting with a space to Panel syntax.
 * ex: 
 * Mediawiki input: 
 *  This should be treated like pre text with other wiki conversions allowed
 *  This line should be part of the previous line's panel. 
 *  Not seperated.
 * 
 * Confluence output:
 * {panel}
 *  This should be treated like pre text with other wiki conversions allowed
 *  This line should be part of the previous line's panel. 
 *  Not seperated.
 * {panel}
 * 
 * @author Laura Kolker
 *
 */
public class LeadingSpacesConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());

	//regex parts
	String newline = "\n";
	String space = " ";
	String noNewlines = "[^\n]+";
	String leadingSpaceLine = "(" + space + noNewlines + newline + ")"; //2nd group
	String manyLeadingSpaceLines = "(" + leadingSpaceLine + "+)"; //1st group
	String optNoSpace = "([^ ]?)"; //3rd group
	//is equivalent to: String regex = "\n(( [^\n]+\n)+)([^ ])?";
	String regex = newline + manyLeadingSpaceLines + optNoSpace;
	Pattern p = Pattern.compile(regex);
	String replacement = "\n{panel}\n$1{panel}\n$3"; //newlines were giving me trouble in the properties file

	Pattern leadingspaces = Pattern.compile("" +
			"(?<=\n|^) +[^\n]+");
	public void convert(Page page) {
		log.debug("Converting Leading Spaces - starting");

		String input = page.getOriginalText();
		String converted = input;

		
		if (getProperties().containsKey("leading-spaces-noformat") &&
				Boolean.parseBoolean(getProperties().getProperty("leading-spaces-noformat", "false"))) {
			log.debug("leading spaces -> noformat");
			Matcher lsFinder = leadingspaces.matcher(input);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			while (lsFinder.find()) {
				found = true;
				String replacement = "{noformat}" + lsFinder.group() + "{noformat}";
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				lsFinder.appendReplacement(sb, replacement);
			}
			if (found) {
				lsFinder.appendTail(sb);
				converted = sb.toString();
			}
			else converted = input;
		}
		else {
			log.debug("leading spaces -> panel");
			try {
				Matcher m = p.matcher(input);
				if (m.find()) {
					log.debug("Leading Spaces - regex found: " + m.group());
					converted = m.replaceAll(replacement);
				}
			} catch (StackOverflowError e) {
				log.debug("Too much backtracking. Skipping.");
				return;
			}
		}
		
		page.setConvertedText(converted);
		log.debug("Converting Leading Spaces - complete");
	}

}
