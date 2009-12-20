package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BreakConverterTest extends TestCase {
	Logger log = Logger.getLogger(this.getClass());
	BreakConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new BreakConverter();
	}

	public void testConvertSimple() {
		String input = "A<br>B";
		String expected = "A\nB";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertBreakWithSlash() {
		String input = "A<br/>B";
		String expected = "A\nB";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertBreakWithSpace() {
		String input = "A<br >B";
		String expected = "A\nB";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	public void testConvertWithSlashAndSpace() {
		String input = "A<br />B";
		String expected = "A\nB";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertWithClear() {
		String input = "A<br clear=\"all\">B";
		String expected = "A\nB";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testWithMoreWhitespace() {
		String input = "Testing\n" +
				"\n" +
				"Line 1   <br>   Line2<br/>Line3";
		String expected = "Testing\n" +
				"\n" +
				"Line 1   \n" +
				"   Line2\n" +
				"Line3";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
}
