package com.atlassian.uwc.converters.xml;


/**
 * Used to replace tags with Confluence quote macro syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class QuoteParser extends SimpleParser {
	/**
	 * assigns appropriate delimiter when creating
	 */
	public QuoteParser () {
		this.delim = "{quote}"; //uses SimpleParser's methods
	}
}
