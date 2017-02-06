package com.atlassian.uwc.converters.socialtext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TableConverterTest extends TestCase {

	TableConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TableConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTables_Basic() {
		String input, expected, actual;
		input = "| Basic | Table | Testing |\n" + 
				"| foo | bar | meh |\n" + 
				"";
		expected = input;
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertTables_MultiLine() {
		String input, expected, actual;
		input = "| \n" + 
				"> testing | \n" + 
				"* one | \n" + 
				"# two | \n" + 
				"## three |\n" +
				"After";
		expected = "| > testing | * one | # two | ## three |\n" +
				"After";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables_props() {
		String input, expected, actual;
		input = "|| sort:off border:on\n" + 
				"| h2. r1c1\n" + 
				"| *r1c2* | _r1c3_ |\n" + 
				"| \n" + 
				"* list | \n" + 
				"# numbered | \n" + 
				"* Testing |\n" + 
				"| xyz | \n" + 
				"* test | \n" + 
				"# ing |\n" + 
				"";
		expected = "| h2. r1c1| *r1c2* | _r1c3_ |\n" + 
				"| * list | # numbered | * Testing |\n" + 
				"| xyz | * test | # ing |\n" + 
				"";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "|| sort:off\n" +
				"| a | b | c |\n";
		expected = "| a | b | c |\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "|| border:on\n" +
				"| a | b | c |\n";
		expected = "| a | b | c |\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "|| border:on sort:on\n" +
				"| a | b | c |\n";
		expected = "| a | b | c |\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConvertTables_lists() {
		String input, expected, actual;
		input = "| \n" + 
				"* one\n" + 
				"* two\n" + 
				"* three | \n" + 
				"# one\n" + 
				"# two\n" + 
				"# three | |\n" + 
				"";
		expected = "| * one\n" + 
				"* two\n" + 
				"* three | # one\n" + 
				"# two\n" + 
				"# three | |\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables_many() {
		String input, expected, actual;
		input = "|| sort:off\n" +
				"| a | b | c |\n" +
				"testing\n"+
				"|| border:on\n" +
				"| a | b | c |\n";
		expected = "| a | b | c |\n" +
				"testing\n" +
				"| a | b | c |\n";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testCondenseLines_notlinks() {
		String input, expected, actual;
		input = "| table | testing \n" +
				"| 123 | " +
				"456 |\n" +
				"\n" +
				"[Link|http://www.google.com]\n" +
				"[Another Link|Page Link]\n" +
				"";
		expected = "| table | testing | 123 | 456 |\n" +
				"\n" +
				"[Link|http://www.google.com]\n" +
				"[Another Link|Page Link]\n" +
				"";
		actual = tester.condenseLines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
