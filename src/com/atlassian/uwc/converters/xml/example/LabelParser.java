package com.atlassian.uwc.converters.xml.example;

import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Transforms the characters in a tag to labels, and attaches the label to the 
 * associated page
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class LabelParser extends DefaultXmlParser {
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		String content = String.copyValueOf(ch, start, length);
		if (content != null) {
			getPage().addLabel(content);
		}
	}
}
