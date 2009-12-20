package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HeaderConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	HeaderConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new HeaderConverter();
	}

	public void testConvertHeadersLevel3() {
		String input = "Not a header\n" +
				"! A header\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h3. A header\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeadersLevel2() {
		String input = "Not a header\n" +
				"!! A header\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h2. A header\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeadersLevel1() {
		String input = "Not a header\n" +
				"!!! A header\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h1. A header\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}

	public void testNoPrecedingWhitespace() {
		String input = "Not a header\n" +
				"!!!A header\n" +
				"!!A header\n" +
				"!A header\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h1. A header\n" +
				"h2. A header\n" +
				"h3. A header\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	
	public void testMultipleHeaders() {
		String input = "Not a header\n" +
				"!!! A header\n" +
				"!! A header\n" +
				"! A header\n" +
				"!! A header\n" +
				"!!! A header\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h1. A header\n" +
				"h2. A header\n" +
				"h3. A header\n" +
				"h2. A header\n" +
				"h1. A header\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);

	}
	
	public void testHasExclamation() {
		String input = "Not a header\n" +
				"! A header!!\n" +
				"More not a header";
		String expected = "Not a header\n" +
				"h3. A header!!\n" +
				"More not a header";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	
	public void testSwitchDepth() {
		int input = 1;
		int expected = 3;
		int actual = tester.switchOuterDepth(input);
		assertEquals(expected, actual);
		
		input = 2;
		expected = 2;
		actual = tester.switchOuterDepth(input);
		assertEquals(expected, actual);
		
		input = 3;
		expected = 1;
		actual = tester.switchOuterDepth(input);
		assertEquals(expected, actual);
		
		input = 0;
		try {
			actual = tester.switchOuterDepth(input);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) { 
			String expectedMsg = "argument: " + input + "\nswitchOuterDepth only handles levels 1 through 3.";
			String actualMsg = e.getMessage();
			assertEquals(expectedMsg, actualMsg);
		}
		
		input = 4;
		try {
			actual = tester.switchOuterDepth(input);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			String expectedMsg = "argument: " + input + "\nswitchOuterDepth only handles levels 1 through 3.";
			String actualMsg = e.getMessage();
			assertEquals(expectedMsg, actualMsg);
		}

		input = -1;
		try {
			actual = tester.switchOuterDepth(input);
			fail("Should have thrown IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			String expectedMsg = "argument: " + input + "\nswitchOuterDepth only handles levels 1 through 3.";
			String actualMsg = e.getMessage();
			assertEquals(expectedMsg, actualMsg);
		}
	}

	public void testConvert_uwc81() {
		String input = "! {{$TEST}} ";
		String expected = "h3. {{$TEST}} \n";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testConvertStartingWithNewline() { //uwc-210
		String input = "\n" +
				"!!Testing\n" +
				"123\n" +
				"!Testing 2\n" +
				"456\n" +
				"";
		String expected = "\n" +
				"h2. Testing\n" +
				"123\n" +
				"h3. Testing 2\n" +
				"456\n" +
				"";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
