package com.atlassian.uwc.converters.mediawiki;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class UserDateConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Adding User and Date metadata - Starting");
		String input = page.getOriginalText();
		String username = getUser(input);
		Date timestamp = getDate(input);
		String converted = cleanUserDate(input);
		log.debug("author: " + username);
		log.debug("timestamp: " + timestamp);
		
		if (username != null) page.setAuthor(username);
		if (timestamp != null) page.setTimestamp(timestamp);
		log.debug("page.getAuthor: " + page.getAuthor());
		page.setConvertedText(converted);
		log.debug("Adding User and Date metadata - Complete");
	}
	
	Pattern username = Pattern.compile("^\\{user:([^}]*)\\}\n");
	protected String getUser(String input) {
		Matcher userFinder = username.matcher(input);
		if (userFinder.find()) {
			return userFinder.group(1);
		}
		return null;
	}

	Pattern date = Pattern.compile("(?:\n|^)\\{timestamp:([^}]*)\\}\n");
	protected Date getDate(String input) {
		Matcher dateFinder = date.matcher(input);
		if (dateFinder.find()) {
			String timestamp = dateFinder.group(1);
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				return dateFormat.parse(timestamp);
			} catch (ParseException e) {
				log.error("Couldn't format date: " + timestamp);
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	protected String cleanUserDate(String input) {
		Matcher userFinder = username.matcher(input);
		if (userFinder.find()) {
			input = userFinder.replaceFirst("");
		}
		Matcher dateFinder = date.matcher(input);
		if (dateFinder.find()) {
			input = dateFinder.replaceFirst("");
		}
		return input;
	}

	
}
