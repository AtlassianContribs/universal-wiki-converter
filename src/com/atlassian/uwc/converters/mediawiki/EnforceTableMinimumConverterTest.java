package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class EnforceTableMinimumConverterTest extends TestCase {

	EnforceTableMinimumConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new EnforceTableMinimumConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTableMinimums_needsmorecolumns() {
		String input, expected, actual;
		input = "{| class=\"wikitable\"\n" +
				"|-\n" +
				"! a \n" +
				"! b \n" +
				"! c \n" +
				"|-\n" +
				"| a\n" +
				"|-\n" +
				"| a \n" +
				"| b \n" +
				"|-\n" +
				"| a \n" +
				"| b \n" +
				"| c \n" +
				"|}";
		expected = "{| class=\"wikitable\"\n" +
				"|-\n" +
				"! a \n" +
				"! b \n" +
				"! c \n" +
				"|-\n" +
				"| a\n" +
				"| \n" +
				"| \n" +
				"|-\n" +
				"| a \n" +
				"| b \n" +
				"| \n" +
				"|-\n" +
				"| a \n" +
				"| b \n" +
				"| c \n" +
				"|}";
		actual = tester.convertTableMinimums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTableMinimums_alreadyextra() {
		String input, expected, actual;
		input = "{| class=\"wikitable\"\n" +
				"|-\n" +
				"! a \n" +
				"! b \n" +
				"! c \n" +
				"|-\n" +
				"| a\n" +
				"| b\n" +
				"| c\n" +
				"| d\n" +
				"|}";
		expected = input;
		actual = tester.convertTableMinimums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTableMinimums_extraheaders() {
		String input, expected, actual;
		input = "{| border=\"1\" class=\"wikitable\" style=\"text-align:center\" cellpadding=\"2\"\n" + 
				"|+caption\n" + 
				"!Header1||Header2||Header3\n" + 
				"|-\n" + 
				"!Row1 Header\n" + 
				"|r1c2||r1c3\n" + 
				"|-\n" + 
				"!Row2 Header\n" + 
				"|r2c2\n" + 
				"|r2c3\n" + 
				"|}\n" + 
				"";
		expected = "{| border=\"1\" class=\"wikitable\" style=\"text-align:center\" cellpadding=\"2\"\n" + 
				"|+caption\n" + 
				"!Header1||Header2||Header3\n" + 
				"|-\n" + 
				"!Row1 Header\n" + 
				"|r1c2||r1c3\n" + 
				"|-\n" + 
				"!Row2 Header\n" + 
				"|r2c2\n" + 
				"|r2c3\n" + 
				"|}\n" + 
				"";
		actual = tester.convertTableMinimums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testNoConvertMultilineCells() {
		String input, expected, actual;
		input = "{|\n" +
				"|-\n" +
				"! a \n" +
				"! b \n" +
				"|-\n" +
				"| a\n" +
				"| * list\n" +
				"* list item\n" +
				"* list item2\n" +
				"|}";
		expected = input;
		actual = tester.convertTableMinimums(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetMin() {
		String input;
		int expected, actual;
		input = "! a \n" +
				"! b \n";
		expected = 2;
		actual = tester.getMin(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetNumCells() {
		String input;
		int expected, actual;
		input = "| a \n" +
				"| a \n" +
				"| b \n";
		expected = 3;
		actual = tester.getNumCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "| a \n" +
				"| b\n" +
				"* aksjdh\n" +
				"| c\n ";
		expected = 3;
		actual = tester.getNumCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddCells() {
		String input, expected, actual;
		input = "| a\n";
		expected = "| a\n" +
				"| \n" +
				"| \n";
		actual = tester.addCells(input, 1, 3);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
