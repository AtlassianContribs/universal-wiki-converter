package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class RedirectConverterTest extends TestCase {

	RedirectConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new RedirectConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertRedirect() {
		String input, expected, actual;
		input = "#REDIRECT [[Some Page]]";
		expected = "{redirect:Some Page}";
		actual = tester.convertRedirect(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	//use conf/settings.illegalcharmap.properties
	public void testConvertRedirect_IllegalCharTitles() {
		String input, expected, actual;
		input = "#REDIRECT [[Some/Page]]";
		expected = "{redirect:Some-Page}";
		actual = tester.convertRedirect(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

}
