package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class QuoteConverterTest extends TestCase {

	QuoteConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new QuoteConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertQuotes() {
		String input, expected, actual;
		input = "{quote:http://link.com}\n" +
				"The quote\n" +
				"{quote}\n" +
				"";
		expected = "{quote}\n" +
				"The quote\n" +
				"[Source|http://link.com]\n" +
				"{quote}\n" +
				"";
		actual = tester.convertQuotes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertQuotes2() {
		String input, expected, actual;
		input = "{quote}\n" +
				"The quote\n" +
				"{quote}\n" +
				"";
		expected = "{quote}\n" +
				"The quote\n" +
				"{quote}\n" +
				"";
		actual = tester.convertQuotes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
