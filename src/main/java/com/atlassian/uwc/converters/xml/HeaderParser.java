package com.atlassian.uwc.converters.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

/**
 * Used to transform html style headers to Confluence style syntax.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class HeaderParser extends DefaultXmlParser {

	/**
	 * regex looks for strings that do not have a newline at the end of the string.
	 * Useful for determining if we need to add an additional newline before the header.
	 */
	Pattern preNL = Pattern.compile("" +
	"([^\n])$");
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Matcher finder = preNL.matcher(getOutput());
		String delim = qName + ". ";
		if (finder.find()) {
			delim = "\n" + delim;
		}
		appendOutput(delim);
	}

}
