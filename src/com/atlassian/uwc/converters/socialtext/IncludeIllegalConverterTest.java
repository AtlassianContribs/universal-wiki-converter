package com.atlassian.uwc.converters.socialtext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class IncludeIllegalConverterTest extends TestCase {

	IncludeIllegalConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new IncludeIllegalConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertIllegalChars_base() {
		String input, expected, actual;
		input = "{include: space [pagename]}";
		expected = input;
		actual = tester.convertIllegalChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertIllegalChars_forward() {
		String input, expected, actual;
		input = "{include: space [foo/bar]}";
		expected = "{include: space [foo-bar]}";
		actual = tester.convertIllegalChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertIllegalnospace() {
		String input, expected, actual;
		input = "{include: [foo#bar]}";
		expected = "{include: [fooNo.bar]}";
		actual = tester.convertIllegalChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
