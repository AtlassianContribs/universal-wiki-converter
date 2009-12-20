package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * removes the extension from the filename, so that the pages
 * are imported with the same pagename that the links will use
 */
public class PagenameConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Pagenames - start");
		
		String currentName = page.getName();
		String convertedName = convertPagename(currentName);
		page.setName(convertedName);
		
		log.info("Converting Pagenames - complete");
	}

	String extension = "^" +		//beginning of string
						"(.*?)" +	//everything until...
						"\\." +		//period
						"\\w+" +	//wordcharacters
						"$";		//end of string
	Pattern extPattern = Pattern.compile(extension);
	protected String convertPagename(String input) {
		input = input.replaceAll("[+]", " ");
		Matcher extFinder = extPattern.matcher(input);
		if (extFinder.matches()) {
			return extFinder.group(1);
		}
		return input;
	}

}
