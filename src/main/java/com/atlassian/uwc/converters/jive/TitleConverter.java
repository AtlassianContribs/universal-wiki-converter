package com.atlassian.uwc.converters.jive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class TitleConverter extends BaseConverter{

	public void convert(Page page) {
		String input = page.getOriginalText();
		String title = convertTitle(input, page);
		if (title != null)
			page.setName(title);
	}

	Pattern title = Pattern.compile("\\{\\{title:(.*?)\\}\\}");
	protected String convertTitle(String input, Page page) {
		Matcher titleFinder = title.matcher(input);
		if (titleFinder.find()) {
			String contents = titleFinder.group(1);
			contents = contents.trim();
			String replacement = titleFinder.replaceAll("");
			page.setConvertedText(replacement);
			return contents;
		}
		return null;
	}
}
