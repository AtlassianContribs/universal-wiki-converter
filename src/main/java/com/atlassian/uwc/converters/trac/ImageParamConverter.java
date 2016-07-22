package com.atlassian.uwc.converters.trac;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ImageParamConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass()); 

	public void convert(Page page) {
		log.info("Converting Trac Image Parameters -- starting");
        String input = page.getOriginalText();
		String converted = convertImageParams(input);
		page.setConvertedText(converted);
		log.info("Converting Trac Image Parameters -- complete");

	}
	
	Pattern image = Pattern.compile("(?<!\\\\)!([^!|\n]+\\|)([^!\n]+)(?<!\\\\)!");
    protected String convertImageParams(String input) {
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String filename = imageFinder.group(1);
			String params = imageFinder.group(2);
			params = params.replaceAll(" +", ",");
			params = handleNoKeyWidth(params);
			String replacement = "!" + filename + params +"!";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
    
    Pattern nokey = Pattern.compile("(?<=^|,)\\s*\\d+%?(?=,|$)");
	private String handleNoKeyWidth(String input) {
		Matcher nokeyFinder = nokey.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (nokeyFinder.find()) {
			found = true;
			String replacement = "thumbnail";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			nokeyFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			nokeyFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
