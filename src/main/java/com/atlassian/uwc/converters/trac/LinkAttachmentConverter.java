package com.atlassian.uwc.converters.trac;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class LinkAttachmentConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertAttachmentLink(input);
		page.setConvertedText(converted);
	}

	Pattern attlink = Pattern.compile("\\[attachment:([^\\]]+)\\]");
	Pattern quotefile = Pattern.compile("\"([^\"]+)\"[ |]+(?!\")(.*)");
	Pattern quotelink = Pattern.compile("(?<!\")([^ |]+)[ |]+\"([^\"]+)\"");
	Pattern quoteboth = Pattern.compile("\"([^\"]+)\"[ |]+\"([^\"]+)\"");
	protected String convertAttachmentLink(String input) {
		Matcher attlinkFinder = attlink.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (attlinkFinder.find()) {
			found = true;
			String link = attlinkFinder.group(1).trim();
			String[] parts = link.split("[ |]");
			String alias = "";
			if (parts.length == 1) { //no alias
				link = link.replaceAll("\"", "");
			} else if (parts.length == 2 && !link.contains("\"")) { //alias, no quotes
				link = parts[0];
				alias = parts[1];
			} else if (link.contains("\"")) {
				Matcher bothFinder = quoteboth.matcher(link);
				Matcher fileFinder = quotefile.matcher(link);
				Matcher linkFinder = quotelink.matcher(link);
				if (bothFinder.matches()) {
					link = bothFinder.group(1);
					alias = bothFinder.group(2);
				} 
				else if (fileFinder.matches()) {
					link = fileFinder.group(1);
					alias = fileFinder.group(2);
				} 
				else if (linkFinder.matches()) {
					link = linkFinder.group(1);
					alias = linkFinder.group(2);
				}
				else {
					log.debug("Problem parsing link with quotes: " + link);
					continue;
				}
			} else {
				log.debug("Problem parsing link: " + link); 
				continue;
			}
			if (!"".equals(alias)) link += "|";
			//Note: alias is in wrong place for confluence because we'll do all link aliases later
			String replacement = "[" + "^" + link + alias + "]"; 
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			attlinkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			attlinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
