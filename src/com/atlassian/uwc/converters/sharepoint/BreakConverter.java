package com.atlassian.uwc.converters.sharepoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.ui.Page;

/**
 * converter breaks (&lt;br/&gt;) and surrounding whitespace to one newline
 */
public class BreakConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Whitespace");
		String input = page.getOriginalText();
		String converted = convertBreaks(input);
		page.setConvertedText(converted);
	}

	/**
	 * break regex: newlines then a break then newlines
	 */
	Pattern p = Pattern.compile("[\r\n]*<br[^>]*>[\r\n]*", Pattern.DOTALL);
	/**
	 * converts breaks
	 * @param input
	 * @return
	 */
	protected String convertBreaks(String input) {
		Matcher m = p.matcher(input);
		return m.replaceAll("\n");
	}

}
