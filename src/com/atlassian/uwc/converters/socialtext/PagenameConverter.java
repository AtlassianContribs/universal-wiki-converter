package com.atlassian.uwc.converters.socialtext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Uses "Subject: " metadata to set the page title 
 */
public class PagenameConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Pagenames - start");
		String input = page.getOriginalText();
		String title = convertTitle(input);
		if (title != null)
			page.setName(title);
		log.info("Converting Pagenames - complete");
	}

	Pattern subject = Pattern.compile("" +
			"^Subject: ([^\n]+)", Pattern.MULTILINE);
	/**
	 * gets the title from the input metadata
	 * @param input
	 * @return title
	 */
	private String convertTitle(String input) {
		Matcher subjectFinder = subject.matcher(input);
		if (subjectFinder.find()) {
			return subjectFinder.group(1).trim();
		}
		return null;
	}
	
}