package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PlaintextLinkConverterTest extends TestCase {

	String entity = "&#58;";
	Logger log = Logger.getLogger(this.getClass());
	PlaintextLinkConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new PlaintextLinkConverter();
	}
	
	public void testConvertPlainLinks() {
		String input = "Before http://www.google.com After";
		String expected = "Before http" + entity + "//www.google.com After";
		String actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Before https://www.google.com/context.txt After";
		expected = "Before https" + entity + "//www.google.com/context.txt After";
		actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testDontConvertRealLinks() {
		String input = "Before [http://www.google.com] After";
		String expected = input;
		String actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[alias|http://www.google.com/index.something.txt]";
		expected = input;
		actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/img/wiki_up/hobbespounce.gif\"}";
		expected = input;
		actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testDontConvertInCodeBlocks() {
		String input = "{CODE()}\n" +
				"http://www.google.com\n" +
				"{CODE}";
		String expected = input;
		String actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNotInCodeBlock() {
		String input = "{CODE()}\n" +
				"http&#58;//www.google.com\n" +
				"{CODE}";
		String expected = "{CODE()}\n" +
			"http://www.google.com\n" +
			"{CODE}";
		String actual = tester.convertPlainLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
