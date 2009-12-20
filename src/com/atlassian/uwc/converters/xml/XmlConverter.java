package com.atlassian.uwc.converters.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Used to parse xml documents. If no .xmlevent properties have been set up, all tags will be
 * parsed with the DefaultXmlParser.
 * Optional properties include: Xml Fragments Feature and Use HtmlTidy Feature
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a> 
 */
public class XmlConverter extends BaseConverter {


	private static final String PROP_XMLFRAGMENTS_ROOT = "xml-fragments-root";
	private static final String DEFAULT_DOCTYPE = "strict";
	private static final String DEFAULT_USERAGENT = "Universal Wiki Converter";
	
	/**
	 * logging object
	 */
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * default xml declaration (used by Xml Fragments feature)
	 */
	private static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
	/**
	 * misc property key for Xml Fragments Feature
	 */
	public final static String PROP_XMLFRAGMENTS = "xml-fragments";
	/**
	 * misc property key for Use HtmlTidy Feature
	 */
	public final static String PROP_USE_HTMLTIDY = "xml-use-htmltidy";
	
	public void convert(Page page) {
		log.debug("Xml Parser - Starting");
		
		XMLReader reader = getXmlReader();
		if (reader == null) return;
		//make sure incoming text is parsable
		page.setOriginalText(enforceValidity(page.getOriginalText()));
		
		//prepare parser
		DefaultXmlEvents eventshandler = null;
		if (getProperties().containsKey("xmlevents")) { //get custom event handlers
			eventshandler = getEventsHandler();
		}
		DefaultXmlParser parser = new DefaultXmlParser(eventshandler, page); //we pass the page for things like labels
		parser.setProperties(getProperties());
		reader.setContentHandler(parser);
		reader.setErrorHandler(parser);
		
		//parse - this will change the page object's contents directly
		parse(page.getOriginalText(), reader, parser); 
		log.debug("Xml Parser - Completed");
	}

	/**
	 * @return the events handler, either a custom one, or the default if no custom one has been
	 * configured
	 */
	private DefaultXmlEvents getEventsHandler() {
		Class eventsClass;
		String xmleventsclass = this.getProperties().getProperty("xmlevents");
		try {
			eventsClass = Class.forName(xmleventsclass);
			DefaultXmlEvents events = (DefaultXmlEvents) eventsClass.newInstance();
			return events;
		} catch (Exception e) { 
			log.error("Problem instantiating custom XmlEvents class: " + xmleventsclass +
					"Using DefaultXmlEvents.");
		}
		return new DefaultXmlEvents();
	}

	/**
	 * @return the object that will be used to drive the parsing
	 */
	private XMLReader getXmlReader() {
		try {
			return XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			String message = "Could not load XmlReader. Skipping.";
			log.error(message);
			e.printStackTrace();
			addError(Feedback.CONVERTER_ERROR, message, true);
			return null;
		}
	}

	/**
	 * parse the input using the given reader and parser
	 * @param input
	 * @param reader
	 * @param parser
	 * @return the resulting output
	 */
	private String parse(String input, XMLReader reader, DefaultXmlParser parser) {
		InputSource source = new InputSource(new StringReader(input));
		System.setProperty( "http.agent", getUserAgent());
		try {
			reader.parse(source);
		} catch (Exception e) {
			String message = "Error while parsing xml. Skipping";
			log.error(message);
			addError(Feedback.CONVERTER_ERROR, message, true);
			throw new RuntimeException(e); //Skipping
		}
		return parser.getOutput(); //for junit tests purposes
	}
	
	private String getUserAgent() {
		Properties props = this.getProperties();
		if (!props.containsKey("user-agent"))
			return DEFAULT_USERAGENT;
		return props.getProperty("user-agent", DEFAULT_USERAGENT);
	}
	
	/**
	 * uses optional features Xml Fragments Feature or Use Htmltidy Feature 
	 * to fix problematic xml documents. root node can be set with misc property:
	 * xml-fragments-root
	 * @param input original xml doc content
	 * @return fixed xml
	 */
	protected String enforceValidity(String input) {
		String root = "uwc-xml-outer-tag";
		if (getProperties().containsKey(PROP_XMLFRAGMENTS_ROOT)) {
			String rootCandidate = getProperties().getProperty(PROP_XMLFRAGMENTS_ROOT);
			if (rootCandidate != null && !"".equals(rootCandidate)) root = rootCandidate;
			log.debug("Using xml fragment root: " + root);
		}
		if (getProperties().containsKey(PROP_USE_HTMLTIDY) && 
				Boolean.parseBoolean(getProperties().getProperty(PROP_USE_HTMLTIDY))) {
			log.debug(PROP_USE_HTMLTIDY + " property was detected. Using htmltidy feature.");
			input = cleanWithJTidy(input);
		}
		if (getProperties().containsKey(PROP_XMLFRAGMENTS) && 
				Boolean.parseBoolean(getProperties().getProperty(PROP_XMLFRAGMENTS))) {
			if (getProperties().containsKey(PROP_USE_HTMLTIDY) && 
					Boolean.parseBoolean(getProperties().getProperty(PROP_USE_HTMLTIDY))) {
				log.debug(PROP_XMLFRAGMENTS + " property was detected, but cannot be used with use-htmltidy option. Skipping.");
			}
			else {
				log.debug(PROP_XMLFRAGMENTS + " property was detected. Document will be treated as containing xml fragments.");
				String enforced = "";
				if (!input.startsWith("<?xml ")) {
					enforced = XML_DECLARATION; 
				}
				enforced += "<" + root + ">\n" +
				input +
				"\n</" + root + ">";
				input = enforced;
			}
		}
//		log.debug("Validated:\n" + input);
		return input;
	}

	/**
	 * @param input
	 * @return input that has been scrubbed by HtmlTidy
	 */
	private String cleanWithJTidy(String input) {
		log.info("Cleaning HTML with JTidy: Starting. (This may take a while...)");
		Tidy tidy = new Tidy();
		tidy.setTidyMark(false);
		tidy.setDropEmptyParas(true);
		tidy.setXmlOut(true);
		tidy.setDropFontTags(false);
		tidy.setDocType(getDoctype());
		InputStream in = null;
		String encoding = "utf-8";
		try {
			in = new ByteArrayInputStream(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			log.error("Could not use encoding: " + encoding);
			e.printStackTrace();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tidy.parseDOM(in, out);
		log.info("Cleaning HTML with JTidy: Completed.");
		return out.toString();
	}
	private String getDoctype() {
		Properties props = getProperties();
		if (!props.containsKey("doctype"))
			return DEFAULT_DOCTYPE;
		return props.getProperty("doctype", DEFAULT_DOCTYPE);
	}
}
