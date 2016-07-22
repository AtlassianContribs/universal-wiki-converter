package com.atlassian.uwc.converters.xml.example;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Uses a tag's attribute named 'title' to set the Title of the page.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class TitleParser extends DefaultXmlParser {

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		String title = attributes.getValue("title");
		if (getPage() != null) getPage().setName(title); 
	}
}
