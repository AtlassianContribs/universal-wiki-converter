package com.atlassian.uwc.converters.socialtext;

import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SpaceConverterTest extends TestCase {

	SpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SpaceConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = new Properties();
		props.setProperty("space-abc", "def");
		props.setProperty("space-foo", "bar");
		props.setProperty("space-foo-bar", "tada");
		tester.setProperties(props);
	}
	
	public void testConvertSpace() {
		String input, expected, actual;
		input = "[Alias Tada|abc:Some Pagename]";
		expected = "[Alias Tada|def:Some Pagename]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Alias Tada|foo:Some Pagename#anchor]";
		expected = "[Alias Tada|bar:Some Pagename#anchor]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Alias Tada|foo-bar:Some Pagename#anchor]";
		expected = "[Alias Tada|tada:Some Pagename#anchor]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[foo:Some Pagename#anchor]";
		expected = "[bar:Some Pagename#anchor]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[foo:Some Pagename]";
		expected = "[bar:Some Pagename]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Testing 123\n" +
				"[foo:Test] and then later [foo-bar:Test2]";
		expected = "Testing 123\n" +
				"[bar:Test] and then later [tada:Test2]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[No Space Here foo] and then later \"Alias\"[Nothing Here Either foo]";
		expected = input;
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[No Space Here] and [foo:Test]";
		expected = "[No Space Here] and [bar:Test]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testGetSpaceProperties() {
		HashMap actual = tester.getSpaceProperties();
		assertNotNull(actual);
		assertEquals(3, actual.size());
		
		assertEquals("def", actual.get("abc"));
		assertEquals("bar", actual.get("foo"));
		assertEquals("tada", actual.get("foo-bar"));
	}
	
	public void testAttachments() {
		String input, expected, actual;
		input = "!abc:pagename^cow.jpg!\n" +
				"!foo-bar:pagename^double facepalm.jpg|width=20%!\n" + 
				"";
		expected = "!def:pagename^cow.jpg!\n" +
				"!tada:pagename^double facepalm.jpg|width=20%!\n" + 
				"";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testRecentMacro() {
		String input, expected, actual;
		input = "{recent_changes: foo}\n" +
				"";
		expected = "{recent_changes: bar}\n" + 
				"";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIncludeMacro() {
		String input, expected, actual;
		input = "{include: foo [pagename]}";
		expected = "{include: bar [pagename]}";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
