package com.atlassian.uwc.converters.trac;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class LinkQuoteConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertLinkQuote(input);
		page.setConvertedText(converted);
	}
	Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]");
	Pattern quotefile = Pattern.compile("\"([^\"]+)\"[ |]+(?!\")(.*)");
	Pattern quotelink = Pattern.compile("(?<!\")([^ |]+)[ |]+\"([^\"]+)\"");
	Pattern quoteboth = Pattern.compile("\"([^\"]+)\"[ |]+\"([^\"]+)\"");
	Pattern aliasPattern = Pattern.compile("[ |]");
	protected String convertLinkQuote(String input) {
		Matcher linkFinder = linkPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String link = linkFinder.group(1);
			if (!link.contains("\"")) continue; 
			String replacement = "";
			String alias = "";
			if (aliasPattern.matcher(link).find()) {
				Matcher bothFinder = quoteboth.matcher(link);
				Matcher fileFinder = quotefile.matcher(link);
				Matcher quotelinkFinder = quotelink.matcher(link);
				if (bothFinder.matches()) {
					link = bothFinder.group(1);
					alias = bothFinder.group(2);
				} 
				else if (fileFinder.matches()) {
					link = fileFinder.group(1);
					alias = fileFinder.group(2);
				} 
				else if (quotelinkFinder.matches()) {
					link = quotelinkFinder.group(1);
					alias = quotelinkFinder.group(2);
				}
				else {
					log.debug("Problem parsing link with quotes: " + link);
					continue;
				}
			}
			else {
				link = link.replaceAll("\"", "");
			}
			if (!"".equals(alias)) link += "|";
			//Note: alias is in wrong place for confluence because we'll do all link aliases later
			replacement = "[" + link + alias + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
