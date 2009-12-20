package com.atlassian.uwc.converters.xml.example;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;

/**
 * Example of a custom xml events object.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class TestCustomXmlEvents extends DefaultXmlEvents {

	/**
	 * logging object
	 */
	static Logger log = Logger.getLogger(TestCustomXmlEvents.class);
	public DefaultHandler getEvent(String tag) {
		log.debug("Same as normal DefaultXmlEvents - but this method has a different log message!");
		HashMap<String, DefaultHandler> ev = getEvents();
		if (!ev.containsKey(tag)) return null;
		return ev.get(tag);
	}
	
	/**
	 * @return test string
	 */
	public String getTest() {
		return "Testing123";
	}
}
