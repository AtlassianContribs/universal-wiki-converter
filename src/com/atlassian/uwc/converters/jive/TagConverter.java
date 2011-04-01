package com.atlassian.uwc.converters.jive;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class TagConverter extends BaseConverter {

	public static Pattern tag = Pattern.compile("\\{tags: ([^}]+)\\}");
	public void convert(Page page) {
		Matcher tagFinder = tag.matcher(page.getOriginalText());
		if (tagFinder.find()) {
			String all = tagFinder.group(1);
			all = all.trim();
			String[] tagarray = all.split(", ");
			for (String tag : tagarray) {
				page.addLabel(tag);
			}
		}
	}

}
