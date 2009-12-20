package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class ConvertUnderscoresInTitle extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Underscores in filename -- start");
		String filename = page.getName();
		filename = convertUnderscores(filename);
		page.setName(filename);
		log.debug("Converting Underscores in filename -- complete");

		if (this.getProperties() == null) {
			log.debug("this.getProperties() is null");
			return;
		}
		
		String linkConvertOn = (String) this.getProperties().getProperty("underscore2space-links");
		log.debug("linkConvertOn = " +linkConvertOn);
		if ("true".equals(linkConvertOn)) {
			log.debug("Converting Underscores in links -- start");
			String input = page.getOriginalText();
			String converted = convertLinkUnderscores(input);
			page.setConvertedText(converted);
			log.debug("Converting Underscores in links -- complete");
		}
	}
	Pattern singleUnderscore = Pattern.compile("(?<!_)_(?!_)");
	protected String convertUnderscores(String input) {
		Matcher underscoreFinder = singleUnderscore.matcher(input);
		if (underscoreFinder.find()) {
			return underscoreFinder.replaceAll(" ");
		}
		return input;
	}
	Pattern links = Pattern.compile("\\[[^\\]]+\\]");
	protected String convertLinkUnderscores(String input) {
		Matcher linkFinder = links.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String link = linkFinder.group();
			Matcher underscoreFinder = singleUnderscore.matcher(link);
			if (underscoreFinder.find()) {
			 	String replacement = underscoreFinder.replaceAll(" ");
				linkFinder.appendReplacement(sb, replacement);
			}
			else continue;
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
		
	}
}
