package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class CategoryConverter extends BaseConverter {

	Pattern category = Pattern.compile("(?i)\\[\\[category:([^\\]]+)\\]\\]");
	public void convert(Page page) {
		String input = page.getOriginalText();
		Matcher catFinder = category.matcher(input);
		while (catFinder.find()) {
			String label = catFinder.group(1);
			label = label.trim();
			label = label.replaceAll("[():]", "");
			page.addLabel(label);
		}
	}

}
