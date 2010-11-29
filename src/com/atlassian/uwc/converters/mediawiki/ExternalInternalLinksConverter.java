package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class ExternalInternalLinksConverter extends BaseConverter {

	private static final String PROPKEY_IDENTIFIER = "external-internal-links-identifier";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String identifier = getExternalLinkIdentifier();
		log.debug("Converting External Internal Links containing: " + identifier);
		if (identifier == null) {
			log.info("Must set " + PROPKEY_IDENTIFIER + " property to use this converter. Skipping.");
			return;
		}
		String input = page.getOriginalText();
		String converted = convertExternalInternalLinks(input);
		page.setConvertedText(converted);
	}
	protected String getExternalLinkIdentifier() {
		return getProperties().getProperty(PROPKEY_IDENTIFIER, null);
	}
	protected String convertExternalInternalLinks(String input) {
		return convertLinks(convertImages(input));
	}
	private String convertLinks(String input) {
		Pattern pattern = Pattern.compile(
				getExternalLinkIdentifier() + "index\\.php\\/" + "([^\\] <\\s]+)");
		Matcher baseFinder = pattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (baseFinder.find()) {
			found = true;
			String pageinfo = baseFinder.group(1);
			pageinfo = pageinfo.replaceAll("_", " ");
			String replacement = pageinfo;
			if (baseFinder.start() < 1 || //first thing in string
					(input.charAt(baseFinder.start()-1) != '|'  //previous char is not pipe
				&& input.charAt(baseFinder.start()-1) != '[')) {//previous char is not bracket
				replacement = "[" + replacement + "]"; 
			}
			baseFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			baseFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern noslashes = Pattern.compile("[^\\/]+$");
	private String convertImages(String input) {
		Pattern pattern = Pattern.compile(
				getExternalLinkIdentifier() + "(?:(?:images\\/)|(?:index.php\\/Image:))" + "([^\\] <\\s]+)");
		Matcher baseFinder = pattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (baseFinder.find()) {
			found = true;
			String att = baseFinder.group(1);
			Matcher noslashesFinder = noslashes.matcher(att);
			if (noslashesFinder.find()) {
				att = noslashesFinder.group();
			}
			String replacement = "^" + att;
			if (baseFinder.start() < 1 || 
					(input.charAt(baseFinder.start()-1) != '|'
				&& input.charAt(baseFinder.start()-1) != '[')) {
				replacement = "[" + replacement + "]"; //mediawiki syntax
			}
			baseFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			baseFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
