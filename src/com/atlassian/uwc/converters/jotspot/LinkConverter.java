package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class LinkConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Links -- starting");
		
		String input = page.getOriginalText();
		log.debug("Link input = " + input);
		
		String converted = convertInternalLinks(input);
		converted = convertExternalLinks(converted);
		
		page.setConvertedText(converted);
		
		log.info("Converting Links -- completed");
	}

	Pattern internalLink = Pattern.compile("<a .*?href=\"wiki:.*?([^\\/\"]+)\"[^>]*>(.*?)</a>");
	/**
	 * converts internal jotspot links to internal Confluence links
	 * @param input conversion file contents
	 * @return conversion file contents with internal Confluence links substituted in
	 */
	protected String convertInternalLinks(String input) {
		Matcher linkFinder = internalLink.matcher(input);
		return convertLinks(input, linkFinder);
	}

	Pattern externalLink = Pattern.compile("<a .*?href=\"([^\"]*)\"[^>]*>(.*?)</a>");
	/**
	 * converts external jotspot links to external Confluence links
	 * @param input conversion file contents
	 * @return conversion file contents with external Confluence links substituted in
	 */
	protected String convertExternalLinks(String input) {
		Matcher linkFinder = externalLink.matcher(input);
		return convertLinks(input, linkFinder);
	}
	
	/**
	 * converts links using the given pattern Matcher object
	 * @param input conversion file contents
	 * @param linkFinder Matcher object that has at least 2 groups
	 * group 1 should represent the link, group2 should represent the alias
	 * @return conversion file contents with Confluence links substituted in
	 */
	private String convertLinks(String input, Matcher linkFinder) {
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find() ) {
			found = true;
			String link = linkFinder.group(1);
			String alias = linkFinder.group(2);
			alias = clearSpans(alias);
			String replacement = "[" + alias + "|" + link + "]";
			if (link.equals(alias)) {
				replacement = "[" + link + "]";
			}
			linkFinder.appendReplacement(sb, replacement);
		}
		linkFinder.appendTail(sb);
		if (found)
			input = sb.toString();
		return input;
	}
	
	Pattern span = Pattern.compile("<span[^>]*>(.*?)<\\/span>");
	protected String clearSpans(String input) {
		Matcher spanFinder = span.matcher(input);
		if (spanFinder.find()) {
			return spanFinder.replaceAll("$1");
		}
		return input;
	}

}
