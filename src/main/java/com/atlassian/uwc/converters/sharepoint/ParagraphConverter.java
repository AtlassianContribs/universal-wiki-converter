package com.atlassian.uwc.converters.sharepoint;

import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint paragraph syntax to Confluence paragraph syntax.
 */
public class ParagraphConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Paragraph Syntax");
		String input = page.getOriginalText();
		String converted = convertParas(input);
		page.setConvertedText(converted);

	}

	/**
	 * converts paragraph syntax
	 * @param input
	 * @return
	 */
	protected String convertParas(String input) {
		//get the elements
		Element root = getRootElement(input, false);
		String search = "p";
		String replace = "\n";
		//look the through the elements, and make changes
		Element changed = simpleTransform(root, search, replace);
		//turn elements back into a string
		String converted = toString(changed);
		return converted;
	}


}
