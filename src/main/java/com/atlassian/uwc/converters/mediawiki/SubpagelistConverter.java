package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class SubpagelistConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting splist to children - Start");
		String input = page.getOriginalText();
		String converted = convertSplist(input);
		page.setConvertedText(converted);
		log.debug("Converting splist to children - Complete");
	}
	
	Pattern splist = Pattern.compile("(?s)(?i)(?:<splist(.*?)>(<\\/splist>)?)");
	Pattern params = Pattern.compile("(?s)(\\S+)=(\\S+)");
	protected String convertSplist(String input) {
		Matcher splistFinder = splist.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (splistFinder.find()) {
			found = true;
			String foundparams = splistFinder.group(1).trim();
			Matcher paramFinder = params.matcher(foundparams);
			String pageval = null;
			while (paramFinder.find()) {
				String key = paramFinder.group(1);
				if ("parent".equals(key)) {
					String val = paramFinder.group(2);
					val = val.replaceFirst("^['\"]", "");
					val = val.replaceFirst("['\"]$", "");
					val = val.replaceAll("/", " ");
					if (Boolean.parseBoolean(getProperties().getProperty("underscore2space-links", "false"))){
						val = val.replaceAll("_", " ");
					}
					pageval = "page=" + val;
				}
			}
			String replacement = "{children:sort=title|all=true" +
				(pageval != null?"|":"") + pageval +
				"}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			splistFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			splistFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
