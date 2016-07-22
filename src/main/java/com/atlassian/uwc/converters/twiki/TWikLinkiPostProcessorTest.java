package com.atlassian.uwc.converters.twiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TWikLinkiPostProcessorTest extends TestCase {

	TWikLinkiPostProcessor tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TWikLinkiPostProcessor();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testFixLinksWithSpaces() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|Testing Images From Other PageUWC_TOKEN_CL\n" + 
				"with alias: UWC_TOKEN_OLalias|Testing Images From Other PageUWC_TOKEN_CL\n" + 
				"\n" + 
				"External:\n" + 
				"http://www.google.com\n" + 
				"laura.kolker@gmail.com\n" + 
				"UWC_TOKEN_OLGoogle Alias 1|http://www.google.comUWC_TOKEN_CL\n" + 
				"UWC_TOKEN_OLGoogle Alias 2|http://www.google.comUWC_TOKEN_CL\n" +
				"UWC_TOKEN_OLGoogle Alias 2|https://www.google.comUWC_TOKEN_CL\n" + 
				"";
		expected = "UWC_TOKEN_OLTesting Images From Other Page|TestingImagesFromOtherPageUWC_TOKEN_CL\n" + 
				"with alias: UWC_TOKEN_OLalias|TestingImagesFromOtherPageUWC_TOKEN_CL\n" + 
				"\n" + 
				"External:\n" + 
				"http://www.google.com\n" + 
				"laura.kolker@gmail.com\n" + 
				"UWC_TOKEN_OLGoogle Alias 1|http://www.google.comUWC_TOKEN_CL\n" + 
				"UWC_TOKEN_OLGoogle Alias 2|http://www.google.comUWC_TOKEN_CL\n" +
				"UWC_TOKEN_OLGoogle Alias 2|https://www.google.comUWC_TOKEN_CL\n" +
				"";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testIgnoreSpacekeyAndAttachments() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|uwctest:Pagename^cow.jpgUWC_TOKEN_CL\n" + 
				"";
		expected = input;
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFindAllLinkParts_Simple() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|Foo BarUWC_TOKEN_CL\n" +
				"";
		expected = "UWC_TOKEN_OLFoo Bar|FooBarUWC_TOKEN_CL\n";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testFindAllLinkParts_Alias() {
		String input, expected, actual;
		input = "UWC_TOKEN_OLalias|Foo BarUWC_TOKEN_CL\n" +
				"";
		expected = "UWC_TOKEN_OLalias|FooBarUWC_TOKEN_CL\n";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFindAllLinkParts_Spacekey() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|uwctest:Foo Bar2UWC_TOKEN_CL";
		expected = "UWC_TOKEN_OLFoo Bar2|uwctest:FooBar2UWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testFindAllLinkParts_File() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|Foo Bar2^cow.jpgUWC_TOKEN_CL";
		expected = "UWC_TOKEN_OL|FooBar2^cow.jpgUWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	

	public void testFindAllLinkParts_both() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|uwctest:Foo Bar2^cow.jpgUWC_TOKEN_CL";
		expected = "UWC_TOKEN_OL|uwctest:FooBar2^cow.jpgUWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	

	public void testFindAllLinkParts_all() {
		String input, expected, actual;
		input = "UWC_TOKEN_OLalias|uwctest:Foo Bar2^cow.jpgUWC_TOKEN_CL";
		expected = "UWC_TOKEN_OLalias|uwctest:FooBar2^cow.jpgUWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	

	public void testFindAllLinkParts_justfile() {
		String input, expected, actual;
		input = "UWC_TOKEN_OL|^cow.jpgUWC_TOKEN_CL";
		expected = "UWC_TOKEN_OL|^cow.jpgUWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFindAllLinkParts_aliasfile() {
		String input, expected, actual;
		input = "UWC_TOKEN_OLalias|^cow.jpgUWC_TOKEN_CL";
		expected = "UWC_TOKEN_OLalias|^cow.jpgUWC_TOKEN_CL";
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixLinksWithDollars() {
		String input, expected, actual;
		input = "UWC_TOKEN_OLDollar in Alias $10|http://www.google.comUWC_TOKEN_CL\n" + 
				"UWC_TOKEN_OLGoogle Alias 2|$dollarinlinkUWC_TOKEN_CL\n" + 
				"";
		expected = "UWC_TOKEN_OLDollar in Alias $10|http://www.google.comUWC_TOKEN_CL\n" + 
				"UWC_TOKEN_OLGoogle Alias 2|dollarinlinkUWC_TOKEN_CL\n" + 
				""; 
		actual = tester.fixLinksWithSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
