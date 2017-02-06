package com.atlassian.uwc.converters.xml;


/**
 * Used to replace tags with Confluence italic syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class ItalicParser extends SimpleParser {
	/**
	 * assigns appropriate delimiter when creating
	 */
	public ItalicParser () {
		this.delim = "_"; //uses SimpleParser's methods
	}
}
