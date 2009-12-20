package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Used to add Confluence HR syntax.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class HorizRuleParser extends DefaultXmlParser {

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String confSyntax = "----\n";
		if (!getOutput().endsWith("\n")) confSyntax = "\n" + confSyntax;
		appendOutput(confSyntax);
	}
	
}
