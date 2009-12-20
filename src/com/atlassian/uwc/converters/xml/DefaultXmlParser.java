package com.atlassian.uwc.converters.xml;

import java.util.Properties;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.atlassian.uwc.ui.Page;

/**
 * SAX Parser set up with XmlConverter to be the default event handler when parsing
 * Xml documents with the UWC Xml Framework. It will examine tags to see if any other events 
 * parsers should be invoked.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class DefaultXmlParser extends DefaultHandler {

	/* Fields */
	
	/**
	 * field maintaining the current output resulting from parsing
	 */
	private static String output = "";
	/**
	 * Page object associated with this parse. This is useful for associating metadata (labels, for example)
	 * with the page.
	 */
	private static Page page;
	/**
	 * Keeps track of which tag/element we're currently examining, and any enclosing tags.
	 */
	private static Stack<String> nested;
	/**
	 * event managing object
	 */
	private DefaultXmlEvents eventsHandler;
	/**
	 * Should be set to false when invoking other event parsers.
	 * This protects against looping by child classes.
	 */
	private static boolean isOriginating = true; //protects against looping by child classes
	/**
	 * logging object
	 */
	Logger log = Logger.getLogger(this.getClass());
	/**
	 * Miscellaneous properties are kept here.
	 */
	private static Properties properties;
	
	/* Constants */
	/**
	 * misc property key for a custom events handler
	 */
	private static final String PROP_EVENTSHANDLER = "xmlevents";
	/**
	 * misc property key for turning on the optional Xml Fragments Feature
	 */
	public final static String PROP_XMLFRAGMENTS = "xml-fragments";
	/**
	 * misc property key for turning on the optional Use Htmltidy Feature
	 */
	public final static String PROP_USE_HTMLTIDY = "xml-use-htmltidy";
	
	/* Constructors */
	
	/**
	 * Creates new DefaultXmlParser
	 */
	public DefaultXmlParser() {
		clearOutput();
	}
	
	/**
	 * Creates new DefaultXmlParser with given state handling objects
	 * @param eventsHandler manages events that will be used while parsing
	 * @param page page objected associated with this parse
	 */
	public DefaultXmlParser(DefaultXmlEvents eventsHandler, Page page) {
		this();
		this.eventsHandler = eventsHandler;
		this.page = page;
	}
	
	/* SAX Handling methods */
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//check for special handling
		this.eventsHandler = getEventsHandler();
		DefaultHandler handler = eventsHandler.getEvent(qName);
		if (handler != null && isOriginating) {
			isOriginating = false;
			handler.startElement(uri, localName, qName, attributes);
			isOriginating = true;
		} 
		//default start element code
		if (isOriginating) getNested().push(qName);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		//check for special handling
		eventsHandler = getEventsHandler();
		DefaultHandler handler = this.eventsHandler.getEvent(qName);
		if (handler != null && isOriginating) {
			isOriginating = false;
			handler.endElement(uri, localName, qName);
			isOriginating = true;
		}
		//default end element code
		if (isOriginating) getNested().pop();
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		//check for special handling
		this.eventsHandler = getEventsHandler();
		DefaultHandler handler = null;
		if (!getNested().isEmpty()) //we can only get eventhandlers associated with tagnames
			handler = this.eventsHandler.getEvent((String) getNested().peek());
		if (handler != null && isOriginating) { 
			isOriginating = false;
			handler.characters(ch, start, length);
		}
		else { //default handling
			String content = String.copyValueOf(ch, start, length);
			if (content != null) this.output += content;
		}
		isOriginating = true;
	}
	
	public void startDocument() throws SAXException {
		//check for special handling
		this.eventsHandler = getEventsHandler();
		DefaultHandler handler = this.eventsHandler.getEvent(">doc");
		if (handler != null && isOriginating) { 
			isOriginating = false;
			handler.startDocument();
		}
		isOriginating = true;
	}
	
	public void endDocument() throws SAXException {
		//check for special handling
		this.eventsHandler = getEventsHandler();
		DefaultHandler handler = this.eventsHandler.getEvent(">doc");
		if (handler != null && isOriginating) { 
			isOriginating = false;
			handler.endDocument();
		}
		isOriginating = true;
		//default behavior
		String out = getOutput();
		out = out.replaceFirst("^(\n)", "");
		if (hasFragments()) out = out.replaceFirst("(\n)$", "");
		page.setConvertedText(out);
	}
	
	/* Getters and Setters */
	
	/**
	 * @return saved output resulting from parse
	 */
	public String getOutput() {
		return output;
	}

	/**
	 * add string to saved output
	 * @param string
	 */
	protected void appendOutput(String string) {
		this.output += string;
	}
	
	/**
	 * delete saved output
	 */
	public void clearOutput() {
		output = "";
	}
	
	/**
	 * @return the current events handler
	 */
	private DefaultXmlEvents getEventsHandler() {
		if (eventsHandler == null) {
			if (getProperties() != null &&
					getProperties().containsKey(PROP_EVENTSHANDLER)) {
				this.eventsHandler = getCustomHandler();
			}
			else this.eventsHandler = new DefaultXmlEvents();
		}
		return this.eventsHandler;
	}
	
	/**
	 * Creates a custom handler using the PROP_EVENTSHANDLER property
	 * @return custom handler, or new DefaultXmlEvents if no custom handler exists
	 */
	private DefaultXmlEvents getCustomHandler() {
		String xmleventsclass = getProperties().getProperty(PROP_EVENTSHANDLER);
		try {
			Class eventsClass = Class.forName(xmleventsclass);
			DefaultXmlEvents events = (DefaultXmlEvents) eventsClass.newInstance();
			return events;
		} catch (Exception e) {
			log.warn("Using DefaultXmlEvents. Could not use custom xml handler: " + xmleventsclass);
		} 
		return new DefaultXmlEvents();
	}

	/**
	 * @return stack of nested tags
	 */
	private Stack getNested() {
		if (nested == null)
			nested = new Stack<String>();
		return nested;
	}
	
	/**
	 * @return page associated with this parse
	 */
	public Page getPage() {
		return page;
	}

	/**
	 * @return true if Xml Fragments feature has been invoked in the properties
	 */
	private boolean hasFragments() {
		return properties != null &&
			properties.containsKey(PROP_XMLFRAGMENTS) && 
			Boolean.parseBoolean(properties.getProperty(PROP_XMLFRAGMENTS));
	}
	
	/**
	 * sets the misc properties object
	 * @param properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * @return misc properties object
	 */
	public Properties getProperties() {
		return properties;
	}
}
