package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MultiLineMonoConverterTest extends TestCase {

	MultiLineMonoConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	
	protected void setUp() throws Exception {
		tester = new MultiLineMonoConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertMultiLineMono() {
		String input, expected, actual;
		input = "{{\n" + 
				"a\n" + 
				"b\n" + 
				"c\n" + 
				"}}\n" + 
				"";
		expected = "{{a}}\n" + 
				"{{b}}\n" + 
				"{{c}}\n" + 
				"";
		actual = tester.convertMultiLineMono(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_VariedNL() {
		String input, expected, actual;
		input = "{{foo\n" + 
				"bar}}\n" + 
				"";
		expected = "{{foo}}\n" + 
				"{{bar}}\n";
		actual = tester.convertMultiLineMono(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_PrePost() {
		String input, expected, actual;
		input = "Testing {{foo\n" +
				"bar}} 123\n" +
				"";
		expected = "Testing {{foo}}\n" +
				"{{bar}} 123\n";
		actual = tester.convertMultiLineMono(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Carriage() {
		String input, expected, actual;
		input = "Testing {{foo\r\n" +
				"bar}} 123\n" +
				"";
		expected = "Testing {{foo}}\n" +
				"{{bar}} 123\n";
		actual = tester.convertMultiLineMono(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
