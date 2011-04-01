package com.atlassian.uwc.converters;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class CreateSpacesTest extends TestCase {

	CreateSpaces tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new CreateSpaces();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "title:Home\n" + 
				"spacekey:Foo\n" + 
				"spacename:Foo Bar\n" + 
				"";
		page.setOriginalText(input);
		tester.convert(page);
		expected = "Home";
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "Foo";
		actual = page.getSpacekey();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "Foo Bar";
		actual = page.getSpaceData("Foo")[0];
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
