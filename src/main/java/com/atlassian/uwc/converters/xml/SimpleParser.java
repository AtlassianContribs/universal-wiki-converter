package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Subclasses that set the delim field will replace tags with that delim.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class SimpleParser extends DefaultXmlParser {
	protected String delim = "";

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput(delim);
	}
	
	public void endElement(String uri, String localName, String qName) {
		appendOutput(delim);
	}
}
