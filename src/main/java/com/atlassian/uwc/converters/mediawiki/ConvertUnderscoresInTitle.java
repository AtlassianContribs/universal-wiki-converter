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
		log.debug("Converting Underscores in filename -- complete. New pagename: '" + filename + "'");

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
			String alias = "", page = link, attachment = "";//page part is link by default
			if (link.contains("|")) { //if we have an alias, set up the alias and page parts
				String[] parts = link.split("\\|");
				alias = parts[0] + "|";
				link = page = parts[1];
			}
			if (link.contains("^")) {//if we have an attachment, adjust page and attachment parts
				String[] parts = link.split("\\^");
				page = parts[0];
				attachment = "^" + parts[1];
			}
			
			Matcher underscoreFinder = singleUnderscore.matcher(page);//replace underscores just for page
			if (underscoreFinder.find()) {
			 	String replacement = alias + underscoreFinder.replaceAll(" ") + attachment;
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
