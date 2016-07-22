package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TableConverterTest extends TestCase {

	TableConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TableConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTables() {
		String input, expected, actual;
		input = "{table}\n" + 
				"Title 1 | Title 2\n" + 
				"Word 1 | Word 2\n" + 
				"{table}\n" + 
				"";
		expected = "|| Title 1 || Title 2 ||\n" + 
				"| Word 1 | Word 2 |\n" + 
				"";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertBackSl_EndOfLine() {
		String input, expected, actual;
		input = "{table}\n" + 
		"Title 1 | Title 2\\\\\n" + 
		"{table}\n" + 
		"";
		expected = "|| Title 1 || Title 2 ||\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}
	public void testConvertBackSl_EndOfCell() {
		String input, expected, actual;
		input = "{table}\n" + 
		"Title 1 \\\\| Title 2\n" + 
		"{table}\n" + 
		"";
		expected = "|| Title 1 || Title 2 ||\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);		
	}
}
