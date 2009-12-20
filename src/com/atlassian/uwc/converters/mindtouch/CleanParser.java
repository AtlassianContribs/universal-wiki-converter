package com.atlassian.uwc.converters.mindtouch;

import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class CleanParser extends DefaultXmlParser {
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		//do nothing - which clears any extra tag data
	}
}
