package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

public class CodeBlockConverterTest extends TestCase {

	CodeBlockConverter tester = null;
	protected void setUp() throws Exception {
		tester = new CodeBlockConverter();
		super.setUp();
	}

	public void testConvertCodeBlockOneLine() {
		String input = "{CODE()}System.out.println(\"This is code\");{CODE}";
		String expected = "{code}\n" +
				"System.out.println(\"This is code\");\n" +
				"{code}";
		String actual = tester.convertCodeBlock(input);
		assertEquals(expected, actual);
	}
	public void testConvertCodeBlockManyLine() {
		String input = "{CODE()}System.out.println(\"This is code\");\n" +
				"System.out.println();\n" +
				"{CODE}";
		String expected = "{code}\n" +
				"System.out.println(\"This is code\");\n" +
				"System.out.println();\n" +
				"{code}";
		String actual = tester.convertCodeBlock(input);
		assertEquals(expected, actual);
	}
	public void testConvertCodeBlockManyLineVariant() {
		String input = "{CODE()}\n" +
				"System.out.println(\"This is code\");\n" +
				"System.out.println();\n" +
				"{CODE}";
		String expected = "{code}\n" +
				"System.out.println(\"This is code\");\n" +
				"System.out.println();\n" +
				"{code}";
		String actual = tester.convertCodeBlock(input);
		assertEquals(expected, actual);
	}
	public void testHandleWhitespace() {
		String input = "{code}\nsome stuff\n{code}";
		String expected = input;
		String actual = tester.handleWhitespace(input);
		assertEquals(expected, actual);
		
		input = "{code}some stuff\n{code}";
		actual = tester.handleWhitespace(input);
		assertEquals(expected, actual);

		input = "{code}\nsome stuff{code}"; 
		actual = tester.handleWhitespace(input);
		assertEquals(expected, actual);

		input = "{code}\n\n\nsome stuff\n\n\n{code}";
		actual = tester.handleWhitespace(input);
		assertEquals(expected, actual);

	}

	public void testMultipleCodeBlocks() {
		String input = "Before\n" +
			"* Listone\n" +
			"** {CODE()}\n" +
			"This is a code block\n" +
			"{CODE}\n" +
			"** Some stuff\n" +
			"** {CODE()}\n" +
			"Another code block!\n" +
			"{CODE}\n" ;
		String expected = "Before\n" +
			"* Listone\n" +
			"** {code}\n" +
			"This is a code block\n" +
			"{code}\n" +
			"** Some stuff\n" +
			"** {code}\n" +
			"Another code block!\n" +
			"{code}\n" ;
		String actual = tester.convertCodeBlock(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleWhitespace_MultCodeBlocks() {
		String input = "Before\n" +
			"* Listone\n" +
			"** {code}\n" +
			"This is a code block\n" +
			"{code}\n" +
			"** Some stuff\n" +
			"** {code}\n" +
			"Another code block!\n" +
			"{code}\n" ;
		String expected = input;
		String actual = tester.handleWhitespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testConvertCodeBlockWithBrackets() {
		String input = "{CODE()} blah [[blah]] blah {CODE}";
		String expected = "{code}\n" +
				"blah [[blah]] blah\n" + 
				"{code}";
		String actual = tester.convertCodeBlock(input);
		assertEquals(expected, actual);
	}
}
