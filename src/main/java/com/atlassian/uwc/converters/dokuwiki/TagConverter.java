package com.atlassian.uwc.converters.dokuwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class TagConverter extends BaseConverter {

	public static Pattern tag = Pattern.compile("\\{\\{tag[>](.*?)\\}\\}");
	public static Pattern quotes = Pattern.compile("\"([^\"]+)\"");
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		Matcher tagFinder = tag.matcher(page.getOriginalText());
		if (tagFinder.find()) {
			String all = tagFinder.group(1);
			all = all.trim();
			Matcher quoteFinder = quotes.matcher(all);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			while (quoteFinder.find()) {
				found = true;
				String content = quoteFinder.group(1);
				content = content.replaceAll("\\s", "_");
				String replacement = content;
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				quoteFinder.appendReplacement(sb, replacement);
			}
			if (found) {
				quoteFinder.appendTail(sb);
				all = sb.toString();
			}
			String[] tagarray = all.split(" ");
			for (String tag : tagarray) {
				if ("".equals(tag.trim())) continue;
				log.debug("adding label: " + tag);
				page.addLabel(tag);
			}
			page.setConvertedText(tagFinder.replaceAll(""));
		}
	}

}
