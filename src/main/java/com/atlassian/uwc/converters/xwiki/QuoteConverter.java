package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Xwiki quote syntax to Confluence quote syntax.
 */
public class QuoteConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Quote Syntax -- starting");
		String input = page.getOriginalText();
		String converted = convertQuotes(input);
		page.setConvertedText(converted);
		log.info("Converting Quote Syntax -- complete");
	}

	Pattern quotePattern = Pattern.compile("" +
			"\\{quote:([^}]+)\\}" +
			"(.*?)" +
			"\\{quote\\}",
			Pattern.DOTALL
			);
	protected String convertQuotes(String input) {
		Matcher quoteFinder = quotePattern.matcher(input);
		String replacement = "{quote}" +
				"{group2}" +
				"[Source|{group1}]\n" +
				"{quote}";
		return RegexUtil.loopRegex(quoteFinder, input, replacement);
	}

}
