package com.atlassian.uwc.converters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.ui.Page;

/**
 * For use in creating a lot of spaces programmatically
 * @author Laura Kolker
 *
 */
public class CreateSpaces extends BaseConverter {

	Pattern data = Pattern.compile("^title:([^\n]+)\n" + 
			"spacekey:([^\n]+)\n" + 
			"spacename:([^\n]+)\n" + 
			"");
	public void convert(Page page) {
		String input = page.getOriginalText();
		Matcher dataFinder = data.matcher(input);
		if (dataFinder.find()) {
			String title = dataFinder.group(1);
			String key = dataFinder.group(2);
			String name = dataFinder.group(3);
			page.setName(title);
			page.setSpace(key, name, "");
			page.setConvertedText("");
		}
	}

}
