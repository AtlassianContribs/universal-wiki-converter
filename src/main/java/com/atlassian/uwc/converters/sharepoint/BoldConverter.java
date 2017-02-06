package com.atlassian.uwc.converters.sharepoint;

import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Bold Sharepoint syntax (strong or b tags) 
 * to Confluence syntax (surrounding *s)
 */
public class BoldConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Bold Syntax");
		String input = page.getOriginalText();
		String converted = convertBold(input);
		page.setConvertedText(converted);

	}

	/** converts Sharepoint bold syntax in the given input
	 * to Confluence bold syntax 
	 * @param input
	 * @return
	 */
	protected String convertBold(String input) {
		//get the elements
		Element root = getRootElement(input, false);
		String search = "strong";
		String replace = "*";
		//look the through the elements, and make changes
		Element changed = simpleTransform(root, search, replace);
		changed = simpleTransform(changed, "b", replace);
		//turn elements back into a string
		String converted = toString(changed);
		//ignore whitespace
		converted = removeWhitespaceOnlyConversions(converted, replace);
		return converted;
	}

}
