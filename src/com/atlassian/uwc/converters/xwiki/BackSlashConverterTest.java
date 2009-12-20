package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BackSlashConverterTest extends TestCase {

	BackSlashConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new BackSlashConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertBackSlash_Midline() {
		String input, expected, actual;
		input = "In a \\\\ line.";
		expected = "In a \n line.";
		actual = tester.convertBackSlash(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBackSlash_Endofline() {
		String input, expected, actual;
		input = "End of line \\\\\n";
		expected = "End of line \n";
		actual = tester.convertBackSlash(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBackSlash_IgnoreTables() { 
		String input, expected, actual;
		input = "| in a \\\\ table|";
		expected = input;
		actual = tester.convertBackSlash(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
