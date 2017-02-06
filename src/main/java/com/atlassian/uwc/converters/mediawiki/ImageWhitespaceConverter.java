package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class ImageWhitespaceConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Image Whitespace - starting");
		
		String input = page.getOriginalText();
		String converted = convertWhitespace(input);
		page.setConvertedText(converted);
		
		log.info("Converting Image Whitespace - complete");
	}

	protected String convertWhitespace(String input) {
		String converted = convertImageWhitespace(input);
		converted = convertAttachWhitespace(converted);
		return converted;
	}

	String image = "![^!]+!";
	Pattern imagePattern = Pattern.compile(image);
	protected String convertImageWhitespace(String input) {
		Matcher imageFinder = imagePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String image = imageFinder.group();
			String replacement = image.replaceAll(" ", "_");
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern attachPattern = Pattern.compile("(\\[.*?\\^)([^\\]]+)(\\])");
	protected String convertAttachWhitespace(String input) {
		Matcher attachFinder = attachPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (attachFinder.find()) {
			found = true;
			String pre = attachFinder.group(1);
			String attachment = attachFinder.group(2);
			String post = attachFinder.group(3);
			String replacement = pre + attachment.replaceAll(" ", "_") + post;
			attachFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			attachFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
}
