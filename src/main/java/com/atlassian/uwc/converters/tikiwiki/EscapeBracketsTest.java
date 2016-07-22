package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

public class EscapeBracketsTest extends TestCase {

	EscapeBrackets tester = null;
	
	protected void setUp() throws Exception {
		super.setUp();
		tester = new EscapeBrackets();
	}

	public void testEscapeBrackets() {
		String input = "ALTER TABLE tblname ADD colname type [[NOT NULL] [[DEFAULT value];";
		String expected = "ALTER TABLE tblname ADD colname type \\[NOT NULL\\] \\[DEFAULT value\\];";
		String actual = tester.escapeBrackets(input);
		assertEquals(expected, actual);
	}
	
	public void testDoNotEscapeBrackets() {
	 	String input = "This is a [link]";
		String expected = input;
		String actual = tester.escapeBrackets(input);
		assertEquals(expected, actual);
	}

	/*
	 * Base test 
	 	String input = "";
		String expected = "";
		String actual = tester.escapeBrackets(input);
		assertEquals(expected, actual);
	 */
}
