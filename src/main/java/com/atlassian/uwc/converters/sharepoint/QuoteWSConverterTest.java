package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class QuoteWSConverterTest extends TestCase {

	QuoteWSConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new QuoteWSConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertQuoteWS() {
		String input, expected, actual;
		input = "a{quote}b";
		expected = "a\n" +
				"{quote}\n" +
				"b";
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "\n{quote}something";
		expected = "\n" +
				"{quote}\n" +
				"something";
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "sometihng{quote}\n";
		expected = "sometihng\n" +
				"{quote}\n";
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "\n{quote}\n";
		expected = input;
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{quote}";
		expected = input;
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{quote}blah";
		expected = "{quote}\n" +
				"blah";
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{quote}\n";
		expected = input;
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testExample() {
		String input, expected, actual;
		input = "\n" + 
				"{quote}h4. ";
		expected = "\n" + 
				"{quote}\n" +
				"h4. ";
		actual = tester.convertQuoteWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
