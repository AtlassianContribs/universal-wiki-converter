package com.atlassian.uwc.converters.sharepoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.DetokenizerConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Adds back in whitespace that had been removed due to complicated multiple syntaxes.
 * Example: *_abc_*xxxx becomes *_abc_* xxxx
 */
public class SeperateSyntaxes extends SharepointConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = seperateSyntaxes(input);
		page.setConvertedText(converted);
	}

	/**
	 * adds whitespace between syntaxes as necessary
	 * @param input
	 * @return
	 */
	protected String seperateSyntaxes(String input) {
		//handle bold
		input = handleBold(input);
		//tokenize email underscores
		input = tokenizeEmailUnderscores(input);
		//handle emphasis (unless UWC_TOKEN_MAP)
		input = handleEmph(input);
		//handle underline
		input = handleUnder(input);
		//detokenize
		input = detokenize(input);
		return input;
	}

	Pattern boldPattern = Pattern.compile("" +
			"(\\*{1,1}[^ *\n][^*\n]+\\*)([^_+\\s])"
			);
	/**
	 * adds ws when syntax like the following occurs:
	 * '_*something*_else'
	 * output = '_*something*_ else'
	 * @param input
	 * @return
	 */
	protected String handleBold(String input) {
		Matcher boldFinder = boldPattern.matcher(input);
		String replacement = "{group1} {group2}";
		return RegexUtil.loopRegex(boldFinder, input, replacement);
	}

	/**
	 * tokenizes instances of underscores in email address. 
	 * Otherwise the addresses get broken by handleEmph
	 * @param input
	 * @return
	 */
	protected String tokenizeEmailUnderscores(String input) {
		String regex = "(\\[\\w+@[\\w\\.:|@]+\\]){replace-with}$1";
		JavaRegexAndTokenizerConverter tokenizer = 
			(JavaRegexAndTokenizerConverter) JavaRegexAndTokenizerConverter.getConverter(regex);
		return convertTmpPage(input, tokenizer);
	}

	

	Pattern emphPattern = Pattern.compile("" +
			"(_[^_\n]+_)([^*+\\s])"
			);
	/**
	 * 	adds ws when syntax like the following occurs:
	 * '+_something_+else'
	 * output = '+_something_+ else'
	 * @param input
	 * @return
	 */
	protected String handleEmph(String input) {
		Matcher emphFinder = emphPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (emphFinder.find()) {
			found = true;
			String group1 = emphFinder.group(1);
			String group2 = emphFinder.group(2);
			if (group1.equals("_TOKEN_")) continue; // don't "fix" _ in UWC_TOKEN_START/END
			String replacement = group1 + " " + group2;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			emphFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			emphFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern underPattern = Pattern.compile("" +
			"(\\+[^+\n]+\\+)([^_*\\s])"
			);
	/**
	 * adds ws when syntax like the following occurs:
	 * '_+something+_else'
	 * output = '_+something+_ else'
	 * @param input
	 * @return
	 */
	protected String handleUnder(String input) {
		Matcher underFinder = underPattern.matcher(input);
		String replacement = "{group1} {group2}";
		return RegexUtil.loopRegex(underFinder, input, replacement);
	}

	/**
	 * detokenizes email underscores
	 * @param input
	 * @return
	 */
	protected String detokenize(String input) {
		DetokenizerConverter detokenizer = new DetokenizerConverter();
		return convertTmpPage(input, detokenizer);
	}

	/**
	 * converts input (used by tokenizer and detokenizer) using the
	 * given converter
	 * @param input
	 * @param converter
	 * @return
	 */
	private String convertTmpPage(String input, BaseConverter converter) {
		Page tmp = new Page(null);
		tmp.setOriginalText(input);
		tmp.setConvertedText(input);
		converter.convert(tmp);
		return tmp.getConvertedText();
	}
}
