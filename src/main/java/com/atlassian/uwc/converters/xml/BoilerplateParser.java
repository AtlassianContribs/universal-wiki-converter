package com.atlassian.uwc.converters.xml;

import java.util.Properties;

import org.xml.sax.SAXException;

/**
 * Used to set up boilerplate text at the beginnings and endings of pages.
 * If using the XmlConverter, you must invoke the xmlevent with the ">doc" tag. 
 * You can set the start and end text by setting the miscellaneous properties associated
 * with the keys boilerplate-start and boilerplate-end.
 * It has default text if invoked without start and end properties.
 * If you want start text, but no end text, set the boilerplate-end property to the empty string. Example:
 * <tt>
 * <br/>
 * Xmltest.0123.boilerplate-start.property=foo
 * <br/>
 * Xmltest.0123.boilerplate-end.property=
 * </tt>
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class BoilerplateParser extends DefaultXmlParser {

	
	/**
	 * miscellaneous property key used to configure end text
	 */
	private static final String PROP_END = "boilerplate-end";
	/**
	 * miscellaneous property key used to configure start text
	 */
	private static final String PROP_START = "boilerplate-start";
	/**
	 * default text used to for start text if not configured
	 */
	private static final String DEFAULT_END = "{tip}\n" +
					"You can set the start and end text as properties in your converter properties file.\n" +
					"Mywiki.1234.boilerplate-start.property=start text\n" +
					"Mywiki.1234.boilerplate-end.property=end text\n" +
					"{tip}\n";
	/**
	 * default text used to for end text if not configured
	 */
	private static final String DEFAULT_START = "{info}\n" +
					"Every page running the BoilerplateParser will have start and end text like this.\n" +
					"{info}";

	public void startDocument() throws SAXException {
		String start = DEFAULT_START;
		Properties props = getProperties();
		if (props != null && props.containsKey(PROP_START))
			start = props.getProperty(PROP_START);
		appendOutput(start + "\n");
	}
	
	public void endDocument() throws SAXException {
		String end = DEFAULT_END;
		Properties props = getProperties();
		if (props != null && props.containsKey(PROP_END))
			end = props.getProperty(PROP_END);
		appendOutput("\n" + end);
	}
}
