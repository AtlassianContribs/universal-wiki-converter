package com.atlassian.uwc.converters.jive;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserDateConverter extends
	com.atlassian.uwc.converters.mediawiki.UserDateConverter {
	//need a slightly different username pattern
	Pattern username = Pattern.compile("(?<=\n)\\{user:([^}]*)\\}\n");
	public String getUser(String input) {
		Matcher userFinder = username.matcher(input);
		if (userFinder.find()) {
			return userFinder.group(1);
		}
		return null;
	}
	
	//need a different get date, because the format is different
	protected Date getDate(String input) {
		Matcher dateFinder = date.matcher(input);
		if (dateFinder.find()) {
			String timestamp = dateFinder.group(1);
			return new Date(Long.parseLong(timestamp));
		}
		return null;
	}
	
	public String getDateAsString(String input) {
		Date epoch = getDate(input);
		if (epoch == null) return null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:SS");
		return dateFormat.format(epoch); 
	}

}
