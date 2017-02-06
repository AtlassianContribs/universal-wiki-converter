package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Used to transform tags into Confluence mono syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class MonoParser extends DefaultXmlParser {

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput("{{");
	}
	
	public void endElement(String uri, String localName, String qName) {
		appendOutput("}}");
	}
}
