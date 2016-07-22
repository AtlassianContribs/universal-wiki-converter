package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class WinNewlinesConverterTest extends TestCase {

	WinNewlinesConverter tester = null;
	
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new WinNewlinesConverter();
	}

	public void testConvertWinNewlines() {
		String input = "one\n" +
				"two\n";
		String expected = input;
		String actual = tester.convertWinNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "one\r\n" +
				"two\r\n";
		actual = tester.convertWinNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
