package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PostListItemConverterTest extends TestCase {
	Logger log = Logger.getLogger(this.getClass());
	PostListItemConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new PostListItemConverter();
	}
	
	public void testConvert() {
		String input = "* item 1\n" +
				"* item 2\n" +
				"Not a list item\n";
		String expected = "* item 1\n" +
				"* item 2\n" +
				"\n" + 
				"Not a list item\n";
		String actual = tester.convertPostListItems(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
			
	}
	public void testMultLists() {
		String input = "* a\n" +
				"* b\n" +
				"C\n" +
				"* d\n" +
				"E\n";
		String expected = "* a\n" +
				"* b\n" +
				"\n" + 
				"C\n" +
				"* d\n" +
				"\n" + 
				"E\n";
		String actual = tester.convertPostListItems(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testFollowedByNewline() {
		String input = "* item 1\n" +
				"* item 2\n" +
				"\n";
		String expected = "* item 1\n" +
				"* item 2\n" +
				"\n"; 
		String actual = tester.convertPostListItems(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testOrderedLists() {
		String input = "# item 1\n" +
				"# item 2\n" +
				"Not a list item\n";
		String expected = "# item 1\n" +
				"# item 2\n" +
				"\n" + 
				"Not a list item\n";
		String actual = tester.convertPostListItems(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testMixedLists() {
		String input = "* item 1\n" +
				"# item 2\n" +
				"Not a list item\n";
		String expected = "* item 1\n" +
				"# item 2\n" +
				"\n" + 
				"Not a list item\n";
		String actual = tester.convertPostListItems(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
