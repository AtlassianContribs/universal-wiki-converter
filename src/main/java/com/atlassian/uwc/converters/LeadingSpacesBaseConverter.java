package com.atlassian.uwc.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * Utility methods for transforming a syntax that starts each line into a block contained with outer delimiters.
 * See dokuwiki's LeadingSpacesConverter for example of usage.
 */
public abstract class LeadingSpacesBaseConverter extends BaseConverter {

	protected String initialspacedelim = " ";
	private String newline = "\n";
	private String noNewlines = "[^\n]+";
	private String optNoSpace = "(?=[^ ]?)";
	private String leadingSpaceLine = "(" + initialspacedelim + noNewlines + newline + ")";
	private String manyLeadingSpaceLines = "(" + leadingSpaceLine + "+)";
	private String regex = "(?:\n|^)" + manyLeadingSpaceLines + optNoSpace;
	/**
	 * assumes one leading space. If you want to customize the pattern, use
	 * generateLeadingPattern 
	 */
	protected Pattern leadingSpacesPattern = Pattern.compile(regex);
	
	public String convertLeadingSpacesReplaceAll(String input, Pattern pattern, String replacement) {
		try {
			Matcher m = pattern.matcher(input);
			if (m.find()) {
				log.debug("Leading Spaces - regex found: " + m.group());
				return m.replaceAll(replacement);
			}
		} catch (StackOverflowError e) {
			log.debug("Too much backtracking. Skipping.");
		}
		return input;
	}
	
	public String convertLeadingSpacesLoop(String input, String regex, String replacement) {
		return RegexUtil.loopRegex(input, regex, replacement);
	}
	
	public String getReplacement(String delim) {
		return getReplacement(delim, delim);
	}
	
	public String getReplacement(String delim, String enddelim) {
		return "\n" + delim +
				"\n$1" + enddelim +
				"\n";
//				"\n$3";
	}
	
	public String getReplacementLoopUtil(String delim, String enddelim) {
		return "\n" + delim +
				"\n{group1}" + enddelim + 
				"\n{group3}";
	}

	/**
	 * @param initialspacedelim initial ws delimiter that must be present to presume the line
	 * is a leading spaces line
	 * @return 
	 */
	protected String generateLeadingPattern(String initialspacedelim) {
		String leadingSpaceLine = "(" + initialspacedelim + noNewlines + newline + ")";
		String manyLeadingSpaceLines = "(" + leadingSpaceLine + "+)";
		String regex = "(?:\n|^)" + manyLeadingSpaceLines + optNoSpace;
		return regex;
	}
}
