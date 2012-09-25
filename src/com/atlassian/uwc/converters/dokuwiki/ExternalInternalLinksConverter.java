package com.atlassian.uwc.converters.dokuwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ExternalInternalLinksConverter extends
		com.atlassian.uwc.converters.mediawiki.ExternalInternalLinksConverter {

	public void convert(Page page) {
		String identifier = getExternalLinkIdentifier();
		log.debug("Converting External Internal Links containing: " + identifier);
		if (identifier == null) {
			log.info("Must set " + PROPKEY_IDENTIFIER + " property to use this converter. Skipping.");
			return;
		}
		String input = page.getOriginalText();
		String converted = convertExternalInternalLinks(input);
		page.setConvertedText(converted);
	}

	Pattern id = Pattern.compile("id=([^|&\\]]+)");
	Pattern alias = Pattern.compile("[|](.*)");
	protected String convertExternalInternalLinks(String input) {
		String identifier = getExternalLinkIdentifier();
		String completePattern = identifier + "doku\\.php[?]" + "(.*?)(?=\\]\\])";
		Pattern dokuExternLinks = Pattern.compile(completePattern);
		Matcher linkFinder = dokuExternLinks.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String args = linkFinder.group(1);
			Matcher aliasFinder = alias.matcher(args);
			String alias = "";
			if (aliasFinder.find()) {
				alias = aliasFinder.group();
			}
			Matcher idFinder = id.matcher(args);
			if (idFinder.find()) {
				String ns = idFinder.group(1);
				String replacement = ns + alias;
				log.debug("Replacing ExternalInternal link: " + replacement);
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				linkFinder.appendReplacement(sb, replacement);
			}
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
}
