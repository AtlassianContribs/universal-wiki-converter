package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class NestedListNewlineRemoverTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	NestedListNewlineRemover tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new NestedListNewlineRemover();
	}
	
	public void testConvert() {
		String input = "* item a\n" +
			"** item a1\n" +
			"\n" +
			"** item a2 (this should only have the inner bullet, not both bullets)\n";
		String expected = "* item a\n" +
			"** item a1\n" +
			"** item a2 (this should only have the inner bullet, not both bullets)\n";
		String actual = tester.removeNestedListNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testButNotLevel1() {
		String input = "* item c\n" +
			"\n" +
			"* item d\n";
		String expected = "* item c\n" +
			"\n" +
			"* item d\n";
		String actual = tester.removeNestedListNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testOrderedLists() {
		String input = "# item a\n" +
			"## item a1\n" +
			"\n" +
			"## item a2 (this should only have the inner bullet, not both bullets)\n";
		String expected = "# item a\n" +
			"## item a1\n" +
			"## item a2 (this should only have the inner bullet, not both bullets)\n";
		String actual = tester.removeNestedListNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMixedLists() {
		String input = "* item a\n" +
			"*# item a1\n" +
			"\n" +
			"*# item a2 (this should only have the inner bullet, not both bullets)\n";
		String expected = "* item a\n" +
			"*# item a1\n" +
			"*# item a2 (this should only have the inner bullet, not both bullets)\n";
		String actual = tester.removeNestedListNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
