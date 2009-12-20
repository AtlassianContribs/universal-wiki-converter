package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BoxConverterTest extends TestCase {

	BoxConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new BoxConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertInfoBoxes() {
		String input, expected, actual;
		input = "#info(\"This is an information panel - corresponds to Confluence info box\")\n" + 
				"";
		expected = "{info}\n" +
				"This is an information panel - corresponds to Confluence info box\n" +
				"{info}\n" +
				"";
		actual = tester.convertBoxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertWarningBoxes() {
		String input, expected, actual;
		input = "#warning(\"This is a warning panel\")\n" + 
				"";
		expected = "{note}\n" +
				"This is a warning panel\n" +
				"{note}\n" +
				"";
		actual = tester.convertBoxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertErrorBoxes() {
		String input, expected, actual;
		input = "#error(\"This is an error panel\")\n" + 
				"";
		expected = "{warning}\n" +
				"This is an error panel\n" +
				"{warning}\n" +
				"";
		actual = tester.convertBoxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
