package com.atlassian.uwc.converters.xwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class BackSlashConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Backslashes -- starting");
		String input = page.getOriginalText();
		String converted = convertBackSlash(input);
		page.setConvertedText(converted);
		log.info("Converting Backslashes -- complete");
	}
	
	protected String convertBackSlash(String input) {
		input = RegexUtil.loopRegex(input, "((\\\\)+)(?=\n|$)", "");
		input = RegexUtil.loopRegex(input, "(\\\\){2,2}(?!\n)", "\n");
		return input;
	}

}
