package com.atlassian.uwc.converters.mindtouch;

import com.atlassian.uwc.converters.xml.SimpleParser;


/**
 * Used to replace tags with Confluence code macro syntax
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class CodeParser extends SimpleParser {
	/**
	 * assigns appropriate delimiter when creating
	 */
	public CodeParser () {
		this.delim = "{code}"; //uses SimpleParser's methods
	}
}
