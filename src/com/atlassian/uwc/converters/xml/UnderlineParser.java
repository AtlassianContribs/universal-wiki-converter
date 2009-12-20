package com.atlassian.uwc.converters.xml;


/**
 * Transforms tags into Confluence underline syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class UnderlineParser extends SimpleParser {
	/**
	 * sets the delim to Confluence's underline delimiter
	 */
	public UnderlineParser () {
		this.delim = "+"; //uses SimpleParser's methods
	}
}
