package com.atlassian.uwc.converters.mindtouch;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.mindtouch.CommentParser.Type;
import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class TagParser extends DefaultXmlParser {
	public enum Type {
		TAG,
		;
		static Type getType(String qName) {
			if ("tag".equals(qName)) return TAG;
			return null;
		}
	}
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		if (type == null) return;
		String tag = attributes.getValue("value");
		if (tag == null) return;
		getPage().addLabel(tag);
	}
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		//do nothing - which clears any extra tag data
	}
}
