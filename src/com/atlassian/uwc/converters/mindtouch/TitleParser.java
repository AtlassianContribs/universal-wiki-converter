package com.atlassian.uwc.converters.mindtouch;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.ui.Page;

public class TitleParser extends DefaultXmlParser {

	Logger log = Logger.getLogger(this.getClass());
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String title = attributes.getValue("title");
		if (title == null) return;
		if ("".equals(title)) {
			log.warn("Title is empty! Not setting.");
			return;
		}
		Page page = getPage();
		page.setName(title);
	}
}
