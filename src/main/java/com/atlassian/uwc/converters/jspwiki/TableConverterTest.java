package com.atlassian.uwc.converters.jspwiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TableConverterTest extends TestCase {

	TableConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props_escbsoptin = new Properties();
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new TableConverter();
		props_escbsoptin.setProperty("table-converter-escbs", "true"); //default is false
	}

	public void testConvertTables() {
		tester.setProperties(props_escbsoptin);
		String input = "Stuff before the table\n" +
				"|| Heading 1 || Heading 2\n" +
				"| r1c1 | r1c2 \\\\ r1c2\n" +
				"| r2c1 | r2c2\n" +
				"Stuff After the Table\n";
		String expected = "Stuff before the table\n" +
				"|| Heading 1 || Heading 2 ||\n" +
				"| r1c1 | r1c2 r1c2 |\n" +
				"| r2c1 | r2c2 |\n" +
				"Stuff After the Table\n";
		String actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertRows() {
		tester.setProperties(props_escbsoptin);
		String input = "|| Heading 1 || Heading 2\n" +
			"| r1c1 | r1c2 \\\\ r1c2\n" +
			"| r2c1 | r2c2\n";
		String expected = "|| Heading 1 || Heading 2 ||\n" +
			"| r1c1 | r1c2 r1c2 |\n" +
			"| r2c1 | r2c2 |\n";
		String actual = tester.convertRows(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertRowsKeepNewline() {
		String input = "| a | b \n";
		String expected = "| a | b |\n";
		String actual = tester.convertRows(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders() {
		String input = "|| Heading 1 || Heading 2\n";
		String expected = "|| Heading 1 || Heading 2 ||\n";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "| r1c1 | r1c2 \\\\ r1c2\n";
		expected = input;
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertCells() {
		tester.setProperties(props_escbsoptin);
		String input = "| r1c1 | r1c2 \\\\ r1c2\n";
		String expected = "| r1c1 | r1c2 r1c2 |\n";
		String actual = tester.convertCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "| r2c1 | r2c2\n";
		expected = "| r2c1 | r2c2 |\n";
		actual = tester.convertCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testRemoveBackslashes() {
		String input = "A \\\\ B";
		String expected = "A B";
		String actual = tester.removeBackslashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapeStar() {
		//one line ends in newline
		String input = "|* |zero or more times |? |zero or one time\n";
		String expected = "| \\* | zero or more times | ? |zero or one time |\n";
		String actual = tester.convertTables(input);
		assertEquals(expected, actual);

		//one line no newline
		input = "|* |zero or more times |? |zero or one time";
		expected = "| \\* | zero or more times | ? |zero or one time |\n";
		actual = tester.convertTables(input);
		assertEquals(expected, actual);
		
		//before and after table text
		input = "Before\n" +
				"|* |zero or more times |? |zero or one time\n" +
				"After";
		expected = "Before\n" +
				"| \\* | zero or more times | ? |zero or one time |\n" +
				"After";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapePipe() {
		String input = "|~| |pipe: {{a~|b}} matches a or b ";
		String expected = "|\\|  |pipe: {{a\\|b}} matches a or b |\n";
		String actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapeDash() {
		String input = "| - |hyphen, match a range of chars ";
		String expected = "| \\- |hyphen, match a range of chars |\n";
		String actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testProblemSample() {
		String input = "|| ~~~Date~~~ || User || Action\n" + 
				"| 2010-09-16 | Marco | *Migration Spain --> Rural* \\\\- 16:00 es.migrated.phase1=false set on production \\\\Database restore point (if needed) / last committed Spain (ES) transaction = *2010-09-16-16.30.00.000000* \\\\ Last logfile needed: *S0018824.LOG*\n" + 
				"";
		String expected = "|| ~~~Date~~~ || User || Action ||\n" + 
				"| 2010-09-16 | Marco | *Migration Spain --> Rural* \\\\- 16:00 es.migrated.phase1=false set on production \\\\Database restore point (if needed) / last committed Spain (ES) transaction = *2010-09-16-16.30.00.000000* \\\\ Last logfile needed: *S0018824.LOG* |\n" + 
				"";
		String actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
