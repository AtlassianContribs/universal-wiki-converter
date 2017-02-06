package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class UnnestedTableHtmlParamsTest extends TestCase {

	UnnestedTableHtmlParams tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new UnnestedTableHtmlParams();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTableParams_table() {
		String input, expected, actual;
		input = "<table border=\"0\" cellspacing=\"1\">";
		expected = "{table:border=0|cellspacing=1}";
		actual = tester.convertTableParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTableParams_tr() {
		String input, expected, actual;
		input = "<tr style=\"background:green\">";
		expected = "{tr:bgcolor=#00ff00}";
		actual = tester.convertTableParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTableParams_td() {
		String input, expected, actual;
		input = "<td width=\"40\" height=\"40\" style=\"background:green\">";
		expected = "{td:width=40px|height=40px|bgcolor=#00ff00}";
		actual = tester.convertTableParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertParams() {
		String input, expected, actual;
		input = "width=\"40\" style=\"background:green\"";
		expected = ":width=40px|bgcolor=#00ff00";
		actual = tester.convertParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetColor() {
		String input, expected, actual;
		input = "red";
		expected = "#ff0000";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "green";
		expected = "#00ff00";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "blue";
		expected = "#0000ff";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "yellow";
		expected = "#ffff00";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "grey";
		expected = "#999999";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "#090c2d";
		expected = input;
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "white";
		expected = "#ffffff";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		
		input = "lime";
		expected = "#aadd00";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		
		input = "dodgerblue";
		expected = "#1e90ff";
		actual = tester.getColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

}
