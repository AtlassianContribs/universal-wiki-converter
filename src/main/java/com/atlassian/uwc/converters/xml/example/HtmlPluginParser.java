package com.atlassian.uwc.converters.xml.example;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
public class HtmlPluginParser extends DefaultXmlParser {


	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		appendOutput("\n{html}\n" +
				"<"+qName+">");
	}

	public void endElement(String uri, String localName, String qName) {
		appendOutput("</"+qName+">\n" +
				""+"{html}\n");
	}
}