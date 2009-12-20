package com.atlassian.uwc.converters.jspwiki;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

import com.atlassian.uwc.ui.Page;

public class PagenameConverterTest extends TestCase {

	PagenameConverter tester = null;
	protected void setUp() throws Exception {
		tester = new PagenameConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertPagename() {
		String input = "something.txt";
		String expected = "something";
		String actual = tester.convertPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPagename2() {
		String input = "something";
		String expected = input;
		String actual = tester.convertPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert() {
		String input = "OtherThing.txt";
		String expected = "OtherThing";
		Page page = new Page(new File(""));
		page.setName(input);
		tester.convert(page);
		String actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandlePlus() {
		String input, expected, actual;
		input = "Sample+WithSpace.txt";
		expected = "Sample WithSpace";
		actual = tester.convertPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
