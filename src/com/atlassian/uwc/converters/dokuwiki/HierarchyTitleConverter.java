package com.atlassian.uwc.converters.dokuwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class HierarchyTitleConverter extends BaseConverter {

	public void convert(Page page) {
		String name = page.getName();
		// Strip trailing file name extension.
		name = fixTitle(name);
		page.setName(name);
	}

	public static String fixTitle(String name) {
		if (name.endsWith(".txt")) {
			name = name.substring(0, name.length()-4);
		}
		// Replace underscores with spaces
		name = name.replaceAll("_", " ");

		// Casify the name
		name = casify(name);
		return name;
	}

	static Pattern word = Pattern.compile("(?<= |^)([a-z])");
	public static String casify(String name) {
		Matcher wordFinder = word.matcher(name);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (wordFinder.find()) {
			found = true;
			String first = wordFinder.group(1);
			String replacement = first.toUpperCase();
			wordFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			wordFinder.appendTail(sb);
			return sb.toString();
		}
		return name;
	}

}
