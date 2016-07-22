package com.atlassian.uwc.converters.jive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.jive.SpaceConverter.ContainerInfo;
import com.atlassian.uwc.ui.Page;

public class BlogConverter extends BaseConverter {
	
	
	Pattern jivemeta = Pattern.compile("\\{jive-export-meta:([^}]+)\\}");
	Pattern typePattern = Pattern.compile("type=(\\w+)");

	public void convert(Page page) {
		Matcher jivemetaFinder = jivemeta.matcher(page.getOriginalText());
		if (jivemetaFinder.find()) {
			String params = jivemetaFinder.group(1);
			Matcher typeFinder = typePattern.matcher(params);
			String type = (typeFinder.find())?typeFinder.group(1):null;
			if ("BLOG".equals(type)) page.setIsBlog(true);
		}
	}
}
