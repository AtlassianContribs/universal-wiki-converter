package com.atlassian.uwc.converters.sharepoint;

import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint emphasis and italics syntax to Confluence
 * emphasis syntax
 */
public class EmphasisConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Emphasis Syntax");
		String input = page.getOriginalText();
		String converted = convertEmphasis(input);
		page.setConvertedText(converted);
	}
	
	/**
	 * converts Sharepoint emphasis tags (em and i) to 
	 * Confluence emphasis tags (_s)
	 * @param input
	 * @return
	 */
	protected String convertEmphasis(String input) {
//		get the elements
		Element root = getRootElement(input, false);
		String search = "em";
		String replace = "_";
		//look the through the elements, and make changes
		Element changed = simpleTransform(root, search, replace);
		changed = simpleTransform(changed, "i", replace);
		//turn elements back into a string
		String converted = toString(changed);

		converted = removeWhitespaceOnlyConversions(converted, replace);
		return converted;
	}

}
