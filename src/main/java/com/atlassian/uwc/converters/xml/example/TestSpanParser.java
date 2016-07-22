package com.atlassian.uwc.converters.xml.example;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Example of a test class that uses attributes when transforming output.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class TestSpanParser extends DefaultXmlParser {
	Logger log = Logger.getLogger(this.getClass());
	private static Stack<String> delims = new Stack<String>(); 
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String val = attributes.getValue("att");
		delims.push(val);
		addDelim(val);
	}

	public void endElement(String uri, String localName, String qName) {
		addDelim(delims.pop());
	}

	/**
	 * appends the given val to the output
	 * @param val
	 */
	private void addDelim(String val) {
		appendOutput(val);
	}
	
}
