package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class ImageWhitespaceConverterTest extends TestCase {

	ImageWhitespaceConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ImageWhitespaceConverter();
	}

	public void testNoWhitespace() {
		//no whitespace
		String input = "!Wiki_file.png|thumb!";
		String expected = input;
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testWithWhitespace() {
		//with whitespace in one place
		String input = "!Wiki file.png|thumb!";
		String expected = "!Wiki_file.png|thumb!";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testNoArgs() {
		//with whitepsace and no args
		String input = "!Wiki file.png!";
		String expected = "!Wiki_file.png!";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testSeveralPlaces() {
		//with ws in several places
		String input = "!Wiki input file.gif!";
		String expected = "!Wiki_input_file.gif!";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testLargerContext() {
		//within a larger context
		String input = "Before\n" +
				"!Wiki file.png!\n" +
				"After";
		String expected = "Before\n" +
				"!Wiki_file.png!\n" +
				"After";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testSeveralImagesWithProb() {
		//several images have this problem
		String input = "Before\n" +
			"has: !Wiki file.png! this one too: !some other file.jpg! tada\n" +
			"After";
		String expected = "Before\n" +
				"has: !Wiki_file.png! this one too: !some_other_file.jpg! tada\n" +
				"After";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}
	public void testSeveralImages() {
		//several images some with some without problem;
		String input = "Before\n" +
			"has: !Wiki file.png!\n" +
			"hasn't: !Wiki_file.png! yes: !some other.gif!\n" +
			"no: !some_other.gif!\n" +
			"After";
		String expected = "Before\n" +
			"has: !Wiki_file.png!\n" +
			"hasn't: !Wiki_file.png! yes: !some_other.gif!\n" +
			"no: !some_other.gif!\n" +
			"After";
		String actual = tester.convertImageWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAttachments() {
		String input, expected, actual;
		input = "[^some file.doc]";
		expected = "[^some_file.doc]";
		actual = tester.convertWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[al ias|^some file.doc]";
		expected = "[al ias|^some_file.doc]";
		actual = tester.convertWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

}
