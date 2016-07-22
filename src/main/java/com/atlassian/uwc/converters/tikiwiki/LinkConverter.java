package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class LinkConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Links - starting");
		String input = page.getOriginalText();
		String converted = getConfluenceLinks(input);
		page.setConvertedText(converted);
		
		log.debug("Converting Links - complete");
	}

	protected String getConfluenceLinks(String input) {
		String output = input;
		output = transformInternalLink(output);
		output = transformNoCache(output);
		output = transformAliases(output);
		output = transformSectionLinks(output);
		return output;
	}

	Pattern internalLinks = Pattern.compile("\\(\\((.*?)\\)\\)");
	private String transformInternalLink(String output) {
		Matcher linkFinder = internalLinks.matcher(output);
		String replacement = "[{group1}]";
		return RegexUtil.loopRegex(linkFinder, output, replacement);
	}
	
	Pattern nocachePattern = Pattern.compile("\\|nocache");
	private String transformNoCache(String output) {
		Matcher nocacheFinder = nocachePattern.matcher(output);
		if (nocacheFinder.find()) {
			output = nocacheFinder.replaceAll("");
		}
		return output;
	}
	
	Pattern aliasPattern = Pattern.compile("\\[([^|\\]]*)\\|([^\\]]*)\\]");
	private String transformAliases(String output) {
		Matcher aliasFinder = aliasPattern.matcher(output);
		String replacement = "[{group2}|{group1}]";
		return RegexUtil.loopRegex(aliasFinder, output, replacement);
	}
	
	Pattern anchorLink = Pattern.compile("\\{ALINK\\(aname=([^)]*)\\)\\}(.*?)\\{ALINK\\}");
	private String transformSectionLinks(String output) {
		Matcher anchorFinder = anchorLink.matcher(output);
		String replacement = "[{group2}|#{group1}]";
		return RegexUtil.loopRegex(anchorFinder, output, replacement);
	}
	

}
