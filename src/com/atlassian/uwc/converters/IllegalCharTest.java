package com.atlassian.uwc.converters;

import junit.framework.TestCase;

/**
 * tests IllegalChar object
 */
public class IllegalCharTest extends TestCase {

	IllegalChar tester = null;
	protected void setUp() throws Exception {
		tester = new IllegalChar("a", "b", IllegalChar.Type.ANYWHERE);
	}

	public void testEquals() {
		IllegalChar input = new IllegalChar("a", "b", IllegalChar.Type.ANYWHERE);
		boolean expected = true;
		boolean actual = tester.equals(input);
		assertEquals(expected, actual);
		
		input = new IllegalChar(null, "b", IllegalChar.Type.ANYWHERE);
		expected = false;
		actual = tester.equals(input);
		assertEquals(expected, actual);
		
		input = new IllegalChar("c", "b", IllegalChar.Type.ANYWHERE);
		expected = false;
		actual = tester.equals(input);
		assertEquals(expected, actual);
		
		input = new IllegalChar("a", "c", IllegalChar.Type.ANYWHERE);
		actual = tester.equals(input);
		assertEquals(expected, actual);
		
		input = new IllegalChar("a", "b", IllegalChar.Type.START_ONLY);
		actual = tester.equals(input);
		assertEquals(expected, actual);
	}
	
	public void testGetReplacement() {
		String input = "aaa";
		String expected = "bbb";
		String actual = tester.getReplacement(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		IllegalChar testStart = new IllegalChar("a", "b", IllegalChar.Type.START_ONLY);
		input = "aaa";
		expected = "baa";
		actual = testStart.getReplacement(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
