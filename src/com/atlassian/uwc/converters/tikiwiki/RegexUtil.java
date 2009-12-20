package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

	/**
	 * does a looped search and replace. Use this one, if you don't mind recompiling your Pattern every time.
	 * @param input text to be searched
	 * @param regex String representation of the regex to be used for the search
	 * @param replacement String representation of the replacement text. You can use groups by inserting
	 * the following syntax where captured text needs to be used:
	 * {groupN}, where N is the number of the group.
	 * Example:
	 * "{group1}"
	 * @return replaced text
	 */
	public static String loopRegex(String input, String regex, String replacement) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		return loopRegex(m, input, replacement);
	}
	
	/**
	 * does a looped search and replace. Use this one, if you don't want to recompile your Pattern
	 * every time you use it.
	 * @param finder matcher object for this search
	 * @param original original text
	 * @param replacement replacement text, can include groups with syntax like:
	 * {group1}
	 * @param numGroups number of groups in the regex
	 * @return result of search and replace
	 */
	public static String loopRegex(Matcher finder, String original, String replacement) {
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (finder.find()) {
			found = true;
			String loopReplacement = replacement;
			for (int i = 1; i <= finder.groupCount(); i++) {
				String group = finder.group(i);
				if (group == null) continue;
				group = handleEscapesInRegex(group); //first time: handles backslashes and $
				Pattern p = Pattern.compile("\\{group"+i+"\\}");
				Matcher m = p.matcher(loopReplacement);
				if (m.find()) {
					loopReplacement = m.replaceAll(group);
				}
			}
			loopReplacement = handleEscapesInRegex(loopReplacement); //second time: just for $
			finder.appendReplacement(sb, loopReplacement);
		}
		if (found) {
			finder.appendTail(sb);
			return sb.toString();
		}
		return original;
	}

	static Pattern backslash = Pattern.compile("(?<!\\\\)\\\\(?!\\\\)");
	static Pattern dollar = Pattern.compile("\\$");
	public static String handleEscapesInRegex(String input) {
		input = handleBackslashesInRegex(input);
		input = handleDollarEscapes(input);
		return input;
	}

	public static String handleDollarEscapes(String input) {
		Matcher dollarFinder = dollar.matcher(input);
		if (dollarFinder.find()) {
			//must escape dollar signs or will get Exceptions
			//must escape both the backslash and the dollar sign
			input = dollarFinder.replaceAll("\\\\"+"\\$");
		}
		return input;
	}
	
	public static String handleEscapesInReplacement(String input) {
		input = handleBackslashesInReplacement(input);
		input = handleDollarEscapes(input);
		return input;
	}

	public static String handleBackslashesInRegex(String input) {
		Matcher bsFinder = backslash.matcher(input);
		if (bsFinder.find()) {
			//4 sets of backslashes - necessary or backslashes get lost in the shuffle
			input = bsFinder.replaceAll("\\\\\\\\\\\\\\\\");
		}
		return input;
	}
	
   /**
    * If I don't do this, appendReplacement loses the backslashes that were inserted 
	* to escape pipes in the escapePipes method. Using 8 backslashes
	* instead of 4 in the escapePipes method does not have the desired affect
	* XXX Hack Alert: Can this be improved/removed?
	* See JIRA issue: UWC-63 for comments
	* @param replacement
	* @return
	*/
	public static String handleBackslashesInReplacement(String replacement) {
		return replacement.replaceAll("\\\\", "\\\\\\\\");
	}

}
