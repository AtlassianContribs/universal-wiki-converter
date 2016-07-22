package com.atlassian.uwc.converters.xml.example;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Example of a parser that replace tags with a Confluence macro called {test}
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class TestParser extends DefaultXmlParser {

	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput("{test}");
	}
	
	public void endElement(String uri, String localName, String qName) {
		appendOutput("{test}");
	}
}
