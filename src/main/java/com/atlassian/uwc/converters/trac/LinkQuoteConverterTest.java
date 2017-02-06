package com.atlassian.uwc.converters.trac;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LinkQuoteConverterTest extends TestCase {

	LinkQuoteConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		
		tester = new LinkQuoteConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLinkQuote() {
		String input, expected, actual;
		input = "[\"Foobar\"]\n" +
				"[\"Foo Bar\" \"FB\"] \n" + 
				"[Foo \"FB\"] \n" + 
				"[\"Foo Bar\" FB] \n" + 
				"";
		expected = "[Foobar]\n" +
				"[Foo Bar|FB] \n" + //alias is in wrong place because we'll deal with that later 
				"[Foo|FB] \n" + 
				"[Foo Bar|FB] \n" + 
				"";
		actual = tester.convertLinkQuote(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
