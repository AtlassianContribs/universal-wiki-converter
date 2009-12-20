package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * transforms existing xml page title into expected page title.
 * For example, if filename is "My+File.xml", turns the page title to "My File"
 * @author Laura Kolker
 */
public class TitleConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Title -- starting");
		String currentTitle = page.getName();
		log.debug("current title = " + currentTitle);
		
		String confluenceTitle = getTitle(currentTitle);
		log.debug("new title = " + confluenceTitle);		
		page.setName(confluenceTitle);
		
		log.debug("Converting Title -- complete");

	}
	
	Pattern extension = Pattern.compile("(.*)\\.\\w+$");
	/**
	 * gets rid of extension and transforms '+' to ' '
	 * @param current current title. Could be My+File.xml
	 * @return new title. My File
	 */
	private String getTitle(String current) {
		String newTitle = "";
		Matcher extFinder = extension.matcher(current);
		if (extFinder.lookingAt()) {
			newTitle = extFinder.replaceAll("$1");
		}
		newTitle = newTitle.replaceAll("\\+", " ");
		
		return newTitle;
	}

}
