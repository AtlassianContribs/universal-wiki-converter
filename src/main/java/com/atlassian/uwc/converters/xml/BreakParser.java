package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Used to add transform &lt;br/&gt; to newlines syntax.
 */
public class BreakParser extends DefaultXmlParser {

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput("\n");
	}
	
}
