package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

public class HeaderConverterTest extends TestCase {

	HeaderConverter tester = null;
	protected void setUp() throws Exception {
		tester = new HeaderConverter();
		super.setUp();
	}

	public void testConvertHeader1() {
		String input = "pretext\n" +
				"! A Header\n" +
				"Some posttext\n";
		String expected = "pretext\n" +
				"h1. A Header\n" +
				"Some posttext\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	public void testConvertHeader2() {
		String input = "pretext\n" +
				"!! A Header\n" +
				"Some posttext\n";
		String expected = "pretext\n" +
				"h2. A Header\n" +
				"Some posttext\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	public void testConvertHeader3() {
		String input = "pretext\n" +
				"!!! A Header\n" +
				"Some posttext\n";
		String expected = "pretext\n" +
				"h3. A Header\n" +
				"Some posttext\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	public void testConvertHeader4() {
		String input = "pretext\n" +
				"!!!! A Header\n" +
				"Some posttext\n";
		String expected = "pretext\n" +
				"h4. A Header\n" +
				"Some posttext\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	public void testConvertHeader5() {
		String input = "pretext\n" +
				"!!!!! A Header\n" +
				"Some posttext\n";
		String expected = "pretext\n" +
				"h5. A Header\n" +
				"Some posttext\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertManyHeaders() {
		String input = "pretext\n" +
				"!!!!! A Header\n" +
				"Some posttext\n" +
				"! A bigger header\n" +
				"Some more text\n" +
				"!!A slightly smaller header.\n" +
				"blah blah blah\n";
		String expected = "pretext\n" +
				"h5. A Header\n" +
				"Some posttext\n" +
				"h1. A bigger header\n" +
				"Some more text\n" +
				"h2. A slightly smaller header.\n" +
				"blah blah blah\n";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
		
	}
}
