package com.atlassian.uwc.converters.xml;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default events handler which will be used by the Xml Framework, if custom one isn't specified.
 * .xmlevent properties will be managed by this class. .xmlevent properties that don't set the associated class
 * as a subclass of DefaultHandler will be ignored.
 * .xmlevent properties that are being managed by this class will be available to all instantiations of this class.
 * If you wish to remove an events, you must invoke clearAll.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/org/xml/sax/helpers/DefaultHandler.html">DefaultHandler</a>
 */
public class DefaultXmlEvents implements XmlEvents {

	static Logger log = Logger.getLogger(DefaultXmlEvents.class);
	/**
	 * all events managed by this class are kept here. This object is static, so that
	 * all instantiations of the class can access any added events. This means that new instantiations
	 * will not necessarily have an empty events object. Use clearAll to empty it.
	 */
	private static HashMap<String, DefaultHandler> events;
	
	public void addEvent(String tag, String eventClass) {
		try {
			DefaultHandler event = createHandler(eventClass);
			log.debug("adding event: tag=" + tag + " class=" + eventClass);
			getEvents().put(tag, event);
		} catch (InstantiationException e) {
			log.error("Could not instantiate class: " + eventClass + ". " +
					"Class must be of type DefaultHandler. Skipping.");
			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			log.error("Could not instantiate class: " + eventClass + ". " +
					"Class constructor must be publicly accessible. Skipping.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.error("Could not find class: " + eventClass + ". " +
					"Class does not exist. Skipping.");
			e.printStackTrace();
		}
	}

	/**
	 * creates a handler object for the given classname
	 * @param eventClass classname, including package, for an event class.
	 * Exceptions are thrown if the classname does not represent a class that
	 * exists, and is a subclass of DefaultHandler
	 * @return created handler object.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	private DefaultHandler createHandler(String eventClass)  
		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class c = Class.forName(eventClass);
		return (DefaultHandler) c.newInstance();
	}

	public void clearAll() {
		getEvents().clear();
	}

	public HashMap<String, DefaultHandler> getEvents() {
		if (events == null) events = new HashMap<String, DefaultHandler>();
		return events;
	}
	
	/**
	 * @param tag xml tag associated with this event
	 * @return The event handler associated with the given xml tag/element, or 
	 * null if there's no event handler for this tag. 
	 */
	public DefaultHandler getEvent(String tag) {
		HashMap<String, DefaultHandler> ev = getEvents();
		if (!ev.containsKey(tag)) return null;
		return ev.get(tag);
	}

}
