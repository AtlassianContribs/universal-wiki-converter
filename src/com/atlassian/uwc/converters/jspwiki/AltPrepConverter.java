package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Prepares Style syntax like 
 * <pre>
 * %%(text-decoration:underline)
 * foo
 * bar
 * %%
 * </pre>
 * for handling with the StyleConverter and other converters.
 * Mostly handles newline issues so that the output of the StyleConverter
 * more accurately reflects Jspwiki rendering.
 */
public class AltPrepConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = prepareAltSyntax(input);
		page.setConvertedText(converted);
	}

	Pattern altSyntax = Pattern.compile("" +
			"(?s)" +
			"(%%\\([^)]+\\))" +
			"(.*?)" +
			"(%%)");
	protected String prepareAltSyntax(String input) {
		Matcher altFinder = altSyntax.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (altFinder.find()) {
			found = true;
			String type = altFinder.group(1);
			String content = altFinder.group(2);
			String delim = altFinder.group(3);
			//remove newlines before final delim
			content = content.replaceFirst("[\r\n]+$", "");
			//replace single newlines with spaces
			content = replaceSingleNL(content);
			//replace double newlines with single lines of the alt syntax
			content = replaceDoubleNL(content, type, delim);
			if (!content.startsWith(type)) content = type + content;
			Pattern delimPattern = Pattern.compile(delim + "[\n\r]+");
			Matcher delimFinder = delimPattern.matcher(content);
			if (!delimFinder.find()) content += delim;
			else content = content.replaceFirst("[\n\r]+$", "");
			//transform explicit backslash token for future use
			content = content.replaceAll("UWC_TOKEN_DBBS", "UWC_TOKEN_2DBBS"); 
			content = RegexUtil.handleEscapesInReplacement(content);
			altFinder.appendReplacement(sb, content);
		}
		if (found) {
			altFinder.appendTail(sb);
			input = sb.toString();
			//removes unnecessary explicit backslash tokens (from 0049 converter) 
			input = input.replaceAll("UWC_TOKEN_DBBS", "");
			return input;
		}
		//removes unnecessary explicit backslash tokens (from 0049 converter) 
		input = input.replaceAll("UWC_TOKEN_DBBS", "");
		return input;
	}

	Pattern singleNL = Pattern.compile("([^\r\n])\r?\n([^\r\n])");
	/**
	 * @param input
	 * @return replaces single newlines with spaces
	 */
	private String replaceSingleNL(String input) {
		Matcher singleNLFinder = singleNL.matcher(input);
		return RegexUtil.loopRegex(singleNLFinder, input, "{group1} {group2}");
	}

	Pattern doubleNL = Pattern.compile("([^\n\r]+)(\r?\n){2}(?![\r\n])");
	/**
	 * @param input
	 * @param pre
	 * @param post
	 * @return replaces double newlines with lines of the releveant syntax.
	 * See AltPrepConverterTest.testPrepareAltSyntax_MultNewlines() for an example.
	 */
	private String replaceDoubleNL(String input, String pre, String post) {
		Matcher doubleNLFinder = doubleNL.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		String lastnl = "\n";
		while (doubleNLFinder.find()) {
			found = true;
			String line = doubleNLFinder.group(1);
			String nl = doubleNLFinder.group(2);
			String replacement = pre + line + post + nl + nl;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			doubleNLFinder.appendReplacement(sb, replacement);
			lastnl = nl;
		}
		if (found) {
			String before = sb.toString();
			doubleNLFinder.appendTail(sb);
			String after = sb.toString();
			String tail = after.substring(before.length());
			String replacement = pre + tail + post + lastnl + lastnl;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			return (before + replacement).trim();
		}
		return input;
	}

}
