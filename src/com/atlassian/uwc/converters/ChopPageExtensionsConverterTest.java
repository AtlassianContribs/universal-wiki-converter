package com.atlassian.uwc.converters;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class ChopPageExtensionsConverterTest extends TestCase {

	ChopPageExtensionsConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ChopPageExtensionsConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert_Basic() {
		String input, expected, actual;
		input = "page.txt";
		expected = "page";
		Page page = new Page(null);
		page.setName(input);
		tester.convert(page);
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Noextenions() {
		String input, expected, actual;
		input = "page";
		expected = "page";
		Page page = new Page(null);
		page.setName(input);
		tester.convert(page);
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_HiddenFile() {
		String input, expected, actual;
		input = ".hidden";
		expected = ".hidden";
		Page page = new Page(null);
		page.setName(input);
		tester.convert(page);
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
