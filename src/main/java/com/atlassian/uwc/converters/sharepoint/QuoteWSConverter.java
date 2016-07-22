package com.atlassian.uwc.converters.sharepoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.ui.Page;

/**
 * Makes sure quote macros are preceded and followed by newlines.
 */
public class QuoteWSConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Handling Quoting Whitespace");
		String input = page.getOriginalText();
		String converted = convertQuoteWS(input);
		page.setConvertedText(converted);
	}

	String quoteWsNoPreNL= "(?<!\n)" +
		"\\{quote\\}";
	Pattern quoteWsNoPreNLPattern = Pattern.compile(quoteWsNoPreNL);
	String quoteWsNoPostNL = "\\{quote\\}" +
		"(?!\n)";
	Pattern quoteWsNoPostNLPattern = Pattern.compile(quoteWsNoPostNL);

	/**
	 * adds newlines as appropriate
	 * @param input
	 * @return
	 */
	protected String convertQuoteWS(String input) {
		String output = input;
		Matcher quoteWsNoPreNLMatcher = quoteWsNoPreNLPattern.matcher(output);
		if (quoteWsNoPreNLMatcher.find()) {
			if (quoteWsNoPreNLMatcher.start() > 0) //not beginning of string
				output = quoteWsNoPreNLMatcher.replaceAll("\n{quote}");
		}
		Matcher quoteWsNoPostNLMatcher = quoteWsNoPostNLPattern.matcher(output);
		if (quoteWsNoPostNLMatcher.find()) {
			if (quoteWsNoPostNLMatcher.end() < output.length()) //not end of string
				output = quoteWsNoPostNLMatcher.replaceAll("{quote}\n");
		}
		return output;
	}

}
