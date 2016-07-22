package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ListConverterTest extends TestCase {

	ListConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	
	protected void setUp() throws Exception {
		tester = new ListConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLists_Num() {
		String input, expected, actual;
		input = "1. Item 1\n" + 
				"11. Item 2\n" + 
				"111. Item 3\n" + 
				"1. Item 4\n"; 
		expected = "# Item 1\n" + 
				"## Item 2\n" + 
				"### Item 3\n" + 
				"# Item 4\n";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists_Bullet() {
		String input, expected, actual;
		input = "* Item 1\n" + 
				"** Item 2\n" + 
				"*** Item 3\n" + 
				"* Item 4\n";
		expected = input;
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists_Mixed1() {
		String input, expected, actual;
		input = "1. Item 1\n" + 
				"1*. Item 2\n" + 
				"1*. Item 3\n" + 
				"1. Item 4\n";
		expected = "# Item 1\n" + 
				"#* Item 2\n" + 
				"#* Item 3\n" + 
				"# Item 4\n";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testConvertLists_Mixed2() {
		String input, expected, actual;
		input = "* Item 1\n" + 
				"*1. Item 2\n" + 
				"*1. Item 3\n" + 
				"* Item 4\n";
		expected = "* Item 1\n" + 
				"*# Item 2\n" + 
				"*# Item 3\n" + 
				"* Item 4\n";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists_NumberOther() {
		String input, expected, actual;
		input = "a. Alphabetical enumerated list\n" + 
				"A. Uppercase alphabetical enumerated list\n" + 
				"i. Roman enumerated list\n" + 
				"I. Uppercase roman enumerated list\n" + 
				"g. Greek enumerated list\n" + 
				"h. Hirigana (jap.) enumerated list\n" + 
				"k. Katakana (jap.) enumerated list\n";
		expected = "# Alphabetical enumerated list\n" + 
				"# Uppercase alphabetical enumerated list\n" + 
				"# Roman enumerated list\n" + 
				"# Uppercase roman enumerated list\n" + 
				"# Greek enumerated list\n" + 
				"# Hirigana (jap.) enumerated list\n" + 
				"# Katakana (jap.) enumerated list\n";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists_MixedOther() {
		String input, expected, actual;
		input = "a. Alphabetical enumerated list\n" + 
				"A*. Uppercase alphabetical enumerated list\n" + 
				"i**. Roman enumerated list\n" + 
				"I. Uppercase roman enumerated list\n" + 
				"g1. Greek enumerated list\n" + 
				"h*. Hirigana (jap.) enumerated list\n" + 
				"k. Katakana (jap.) enumerated list\n";
		expected = "# Alphabetical enumerated list\n" + 
				"#* Uppercase alphabetical enumerated list\n" + 
				"#** Roman enumerated list\n" + 
				"# Uppercase roman enumerated list\n" + 
				"## Greek enumerated list\n" + 
				"#* Hirigana (jap.) enumerated list\n" + 
				"# Katakana (jap.) enumerated list\n";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDelim() {
		String input, expected, actual;
		input = "1";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "*";
		expected = "*";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "A";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "i";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "I";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "g";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "h";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
		input = "k";
		expected = "#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "1**1";
		expected = "#**#";
		actual = tester.convertNums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
}
