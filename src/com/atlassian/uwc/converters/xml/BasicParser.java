package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BasicParser extends DefaultXmlParser {
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String content = " " + getDelim(qName); 
		appendOutput(content);
	}
	
	public void endElement(String uri, String localName, String qName) {
		String content = getDelim(qName);
		appendOutput(content);
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		   String content = String.copyValueOf(ch, start, length);
		   content = content.replaceAll("\\s+", " ");
		   content = content.trim();
		   appendOutput(content);
	}
	
	private String getDelim(String qName) {
		if ("b".equals(qName) || "strong".equals(qName)) return "*";
		if ("i".equals(qName) || "em".equals(qName)) return "_";
		if ("u".equals(qName)) return "+";
		if ("s".equals(qName)) return "-";
		return "";
	}

}
