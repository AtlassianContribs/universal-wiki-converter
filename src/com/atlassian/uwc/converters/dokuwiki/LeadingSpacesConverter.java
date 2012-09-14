package com.atlassian.uwc.converters.dokuwiki;

import com.atlassian.uwc.converters.LeadingSpacesBaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * transforms sets of lines starting with two ws into code blocks
 */
public class LeadingSpacesConverter extends LeadingSpacesBaseConverter {

	protected String initialspacedelim = "  (?! *?[-*])"; //two spaces!

	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertLeadingSpaces(input);
		page.setConvertedText(converted);
	}

	protected String convertLeadingSpaces(String input) {
		String replacement = getReplacementLoopUtil("<code>", "</code>");
		String regex = generateLeadingPattern(this.initialspacedelim);
		return convertLeadingSpacesLoop(input, regex, replacement);
	}

}
