package com.atlassian.uwc.converters.xml;


/**
 * Used to replace tags with Confluence strike syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class StrikeParser extends SimpleParser {
	/**
	 * assigns appropriate delimiter when creating
	 */
	public StrikeParser () {
		this.delim = "-"; //uses SimpleParser's methods
	}
}
