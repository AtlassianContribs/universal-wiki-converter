package com.atlassian.uwc.converters.xml;

import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

/**
 * Used to turn any tag and it's attributes into Confluence Macro style syntax.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class MacroParser extends DefaultXmlParser {

	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String attString = getAttString(attributes);
		String macro = (attString == null)?				//if no attstring
				getSimpleMacro(qName):					//then just create the tag {macro}
				"{" + qName + ":" + attString + "}";	//otherwise, create the tag with attributes {macro:att=att}
		appendOutput(macro);
	}
	
	/**
	 * @param attributes
	 * @return a Confluence style parameter string for each attribute represented in the
	 * given attributes object
	 */
	private String getAttString(Attributes attributes) {
		if (attributes == null) return null;
		String attstring = "";
		for (int i = 0; i < attributes.getLength(); i++) {
			if (i > 0) attstring += "|";
			String name = attributes.getQName(i);
			String value = attributes.getValue(i);
			attstring += name + "=" + value;
		}
		if ("".equals(attstring)) return null;
		return attstring;
	}

	public void endElement(String uri, String localName, String qName) {
		appendOutput(getSimpleMacro(qName));
	}

	/**
	 * @param qName
	 * @return Confluence style macro tag
	 */
	private String getSimpleMacro(String qName) {
		return "{" + qName + "}";
	}
}
