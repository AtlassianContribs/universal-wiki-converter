package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LeadingSpacesConverterTest extends TestCase {

	LeadingSpacesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new LeadingSpacesConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLeadingSpaces() {
		String input, expected, actual;
		input = "testing 123\n" +
				"  needs to be in code block\n" +
				"  this part too\n" +
				"not in code block\n" +
				" not a code block2\n" +
				"  but this is!\n" +
				"  tralala\n" +
				"     tralalala\n" +
				"  tra!\n" +
				"end";
		expected = "testing 123\n" +
				"<code>\n" +
				"  needs to be in code block\n" +
				"  this part too\n" +
				"</code>\n" +
				"not in code block\n" +
				" not a code block2\n" +
				"<code>\n" +
				"  but this is!\n" +
				"  tralala\n" +
				"     tralalala\n" +
				"  tra!\n" +
				"</code>\n" +
				"end";
		actual = tester.convertLeadingSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLeadingSpaces_NotLists() {
		String input, expected, actual;
		input = "testing 123\n" +
				"  * this is a list not a code block\n" +
				"  * this part too\n" +
				"end of list \n";
		expected = "testing 123\n" +
				"  * this is a list not a code block\n" +
				"  * this part too\n" +
				"end of list \n";
		actual = tester.convertLeadingSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testing 123\n" +
				"  * this is a list not a code block\n" +
				"    * this part too\n" +
				"end of list \n";
		expected = "testing 123\n" +
				"  * this is a list not a code block\n" +
				"    * this part too\n" +
				"end of list \n";
		actual = tester.convertLeadingSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testing 123\n" +
				"  - this is a list not a code block\n" +
				"  - this part too\n" +
				"end of list \n";
		expected = "testing 123\n" +
				"  - this is a list not a code block\n" +
				"  - this part too\n" +
				"end of list \n";
		actual = tester.convertLeadingSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
