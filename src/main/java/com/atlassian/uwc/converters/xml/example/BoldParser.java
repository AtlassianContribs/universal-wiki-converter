package com.atlassian.uwc.converters.xml.example;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Transforms tags into Confluence bold syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class BoldParser extends DefaultXmlParser {
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput("*");
	}
	
	public void endElement(String uri, String localName, String qName) {
		appendOutput("*");
	}
}
