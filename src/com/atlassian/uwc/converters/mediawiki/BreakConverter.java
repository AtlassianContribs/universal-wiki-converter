package com.atlassian.uwc.converters.mediawiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class BreakConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converter Line Breaks - starting");
		String input = page.getOriginalText();
		String converted = convertBreaks(input);
		page.setConvertedText(converted);
		log.info("Converter Line Breaks - complete");
	}
	String linebreak = "<br[^>]*>";
	protected String convertBreaks(String input) {
		String replacement = "\n";
		return RegexUtil.loopRegex(input, linebreak, replacement);	
	}

}
