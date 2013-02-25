package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.LeadingSpacesBaseConverter;
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
public class LeadingSpacesConverter extends LeadingSpacesBaseConverter {
	public static final String PROPKEY_TOKENIZE = "leading-spaces-tokenize";
	private static final String DEFAULTVAL_TOKENIZE = "true";
	Logger log = Logger.getLogger(this.getClass());
	private Pattern leadingspaces = Pattern.compile("" +
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
			converted = convertLeadingSpacesReplaceAll(input, leadingSpacesPattern, getReplacement());
		}
		if (shouldTokenize()) {
			converted = tokenize(converted, getTokenizeRegex(), "leading spaces");
		}
		page.setConvertedText(converted);
		log.debug("Converting Leading Spaces - complete");
	}


	private String getTokenizeRegex() {
		return "(" +
					"\\Q" + getDelimiter() + "\\E" +
					".*?" +
					"\\Q" + getDelimiter() + "\\E" +
				")" +
				"{replace-multiline-with}$1";
	}


	protected boolean shouldTokenize() {
		return Boolean.parseBoolean(getProperties().getProperty(PROPKEY_TOKENIZE, DEFAULTVAL_TOKENIZE));
	}
	
	
	private String getReplacement() {
		String delim = getDelimiter();
		log.debug("Leading spaces replacement delim: " + delim);
		return getReplacement(delim);
	}


	protected String getDelimiter() {
		String delim = getProperties().getProperty("leading-spaces-delim", "panel");
		delim = "{"+delim+"}";
		return delim;
	}

}
