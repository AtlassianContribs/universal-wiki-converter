package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LinkParser extends DefaultXmlParser {

	private String target = "";
	private String alias = "";
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		target = attributes.getValue("href");
	}
	
	public void endElement(String uri, String localName, String qName) {
		String link = "[" + alias + "|" + target + "]";
		appendOutput(link);
		alias = target = "";
		
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		alias += String.copyValueOf(ch, start, length);
	}
}
