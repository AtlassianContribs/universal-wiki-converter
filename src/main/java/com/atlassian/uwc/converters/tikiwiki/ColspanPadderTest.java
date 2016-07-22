package com.atlassian.uwc.converters.tikiwiki;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class ColspanPadderTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	ColspanPadder tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ColspanPadder();
	}
	
	
	public void testPaddingColspans() {
		String input = 	
			"| A | B |\n" +
			"| a | b | c |\n";
		String expected = 
			"| A | B | |\n" +
			"| a | b | c |\n";
		String actual = tester.padColspansInOneTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontPad() {
		String input = 	
			"| A | B |\n" +
			"| a | b |\n";
		String expected = input;
		String actual = tester.padColspansInOneTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testInContext() {
		//one table in a larger context
		String input = "Before\n" +
		"| A | B |\n" +
		"| a | b | c | e | f |\n" +
		"| 1 | 2 | 3 |\n" +
		"After\n";
		String expected = "Before\n" +
		"| A | B | | | |\n" +
		"| a | b | c | e | f |\n" +
		"| 1 | 2 | 3 | | |\n" +
		"After\n";
		String actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultTables() {
		//several tables in one string
		String input = "Before\n" +
		"| A | B | |\n" +
		"| a | b |\n" +
		"Between\n" +
		"| D | E | F |\n" +
		"| d | e | f | g |\n" +
		"After\n";
		String expected = "Before\n" +
		"| A | B | |\n" +
		"| a | b | |\n" +
		"Between\n" +
		"| D | E | F | |\n" +
		"| d | e | f | g |\n" +
		"After\n";
		String actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFindTables() {
		String input = "Before\n" +
				"| A | B |\n" +
				"| a | b |\n" +
				"Between\n" +
				"| D | E | F | G |\n" +
				"| d | e | f | g |\n" +
				"After\n";
		String expected = input;
		String actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetMaxNumColumns() {
		String input = 
		"| A | B |\n" +
		"| a | b |\n";
		String[] rows = input.split("\n");
		int expected = 2;
		int actual = tester.getMaxNumColumns(rows);
		assertEquals(expected, actual);
		
		input = 
		"| D | E | F | G |\n" +
		"| d | e | f |\n";
		rows = input.split("\n");
		expected = 4;
		actual = tester.getMaxNumColumns(rows);
		assertEquals(expected, actual);
		
		input = 
			"| D | E | F | G |\n" +
			"| d | e | f | | |\n";
		rows = input.split("\n");
		expected = 5;
		actual = tester.getMaxNumColumns(rows);
		assertEquals(expected, actual);
		
		input = 
			"| a |\n" +
			"| D | E | F | G | a | b | c |\n" +
			"| d | e | f |\n";
		rows = input.split("\n");
		expected = 7;
		actual = tester.getMaxNumColumns(rows);
		assertEquals(expected, actual);
		
		input = 
			"|| a || b ||\n" +
			"| A |\n";
		rows = input.split("\n");
		expected = 2;
		actual = tester.getMaxNumColumns(rows);
		assertEquals(expected, actual);
		
	}
	
	public void testWithHeaders() {
		String input = 	
			"|| A || B ||\n" +
			"| a | b | c |\n";
		String expected = 
			"|| A || B || |\n" +
			"| a | b | c |\n";
		String actual = tester.padColspansInOneTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = 	
			"|| A || B || C ||\n" +
			"| a | b |\n";
		expected = 
			"|| A || B || C ||\n" +
			"| a | b | |\n";
		actual = tester.padColspans(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddColumnstoRows() {
		String input = 
			"| A | B |\n" +
			"| a | b |\n";
		String[] rows = input.split("\n");
		String expected = input;
		String actual = tester.addColumnsToRows(rows, 2);
		assertEquals(expected, actual);
		
		input = 
			"| D | E | F | G |\n" +
			"| d | e | f |\n";
		rows = input.split("\n");
		expected = 
			"| D | E | F | G |\n" +
			"| d | e | f | |\n";
		actual = tester.addColumnsToRows(rows, 4);
		assertEquals(expected, actual);
		
		input = 
				"| D | E | F | G |\n" +
				"| d | e | f | | |\n";
		rows = input.split("\n");
		expected = 
			"| D | E | F | G | |\n" +
			"| d | e | f | | |\n";
		actual = tester.addColumnsToRows(rows, 5);
		assertEquals(expected, actual);
		
		input = 
				"| a |\n" +
				"| D | E | F | G | a | b | c |\n" +
				"| d | e | f |\n";
		rows = input.split("\n");
		expected = "| a | | | | | | |\n" +
		"| D | E | F | G | a | b | c |\n" +
		"| d | e | f | | | | |\n";
		actual = tester.addColumnsToRows(rows, 7);
		assertEquals(expected, actual);
		
		input = 
				"|| a || b ||\n" +
				"| A |\n";
		rows = input.split("\n");
		expected = 
			"|| a || b ||\n" +
			"| A | |\n";
		actual = tester.addColumnsToRows(rows, 2);
		assertEquals(expected, actual);
		
		input = 
				"|| a ||\n" +
				"| A | B |\n";
		rows = input.split("\n");
		expected = 
			"|| a || |\n" +
			"| A | B |\n";
		actual = tester.addColumnsToRows(rows, 2);
		assertEquals(expected, actual);
		
	}
}
