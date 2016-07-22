package com.atlassian.uwc.converters.xml;

import java.util.HashMap;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Interface that must be implemented for xml event handling.
 * Xmlevents are managed by classes that implement this interface.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public interface XmlEvents {
	
	/**
	 * adds an event to be managed.
	 * @param tag xml tag (element) that this event is associated with
	 * @param eventClass name of class (including package) that will be called when the associated
	 * tag is encountered during parsing
	 */
	public void addEvent(String tag, String eventClass);

	/**
	 * removes all events currently being managed
	 */
	public void clearAll() ;

	/**
	 * @return a map of all events currently being managed. Keys
	 * are associated xml tags (elements).  Example: "div" for &lt;div&gt; tags. 
	 */
	public HashMap<String, DefaultHandler> getEvents();
	
	/**
	 * @param tag
	 * @return gets the event handler associated with the given xml tag/element.
	 */
	public DefaultHandler getEvent(String tag);
}
