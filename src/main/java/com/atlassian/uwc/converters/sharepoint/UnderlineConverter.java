package com.atlassian.uwc.converters.sharepoint;

import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint underline syntax into Confluence
 * underline syntax 
 */
public class UnderlineConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Underline Syntax");
		String input = page.getOriginalText();
		String converted = convertUnderline(input);
		page.setConvertedText(converted);

	}

	/**
	 * converts underline syntax
	 * @param input
	 * @return
	 */
	protected String convertUnderline(String input) {
		//get the elements
		Element root = getRootElement(input, false);
		String search = "u";
		String replace = "+";
		//look the through the elements, and make changes
		Element changed = simpleTransform(root, search, replace);
		//turn elements back into a string
		String converted = toString(changed);
		//ignore whitespace
		converted = removeWhitespaceOnlyConversions(converted, replace);
		return converted;
	}


}
