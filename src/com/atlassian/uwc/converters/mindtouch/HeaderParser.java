package com.atlassian.uwc.converters.mindtouch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

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
		String delim = createDelim(qName);
		if (finder.find()) {
			delim = "\n" + delim;
		}
		appendOutput(delim);
	}
	Pattern digits = Pattern.compile("\\d+");
	private String createDelim(String qName) {
		Matcher digitFinder = digits.matcher(qName);
		int digit = 0;
		if (digitFinder.find()) {
			String digitString = digitFinder.group();
			digit = Integer.parseInt(digitString);
			digit--;
		}
		if (digit < 1) return qName + ". ";
		return "h" + digit + ". ";
	}

}
