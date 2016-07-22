package com.atlassian.uwc.converters.tikiwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class WinNewlinesConverter extends BaseConverter{

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Windows Newlines -- start");
		
		String input = page.getOriginalText();
		String converted = convertWinNewlines(input);
		page.setConvertedText(converted);
		
		log.info("Converting Windows Newlines -- complete");
	}
	String winNewline = "\r\n";
	protected String convertWinNewlines(String input) {
		return input.replaceAll(winNewline, "\n");
	}

}
