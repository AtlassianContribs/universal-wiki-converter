package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class IndentConverterTest extends TestCase {

	IndentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new IndentConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertIndent_textindent() { //uwc-378
		String input, expected, actual;
		input = "%%(text-indent:4em; font-size: 100%; color:purple)\n" +
				"testing\n" +
				"%%";
		expected = "{indent}\n" +
				"%%( font-size: 100%; color:purple)\n" +
				"testing\n" +
				"%%\n" +
				"{indent}";
		actual = tester.convertIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testConvertIndent_margin() {
		String input, expected, actual;
		input = "%%(margin-left:4em; font-size: 100%; color:purple)\n" +
				"testing\n" +
				"%%";
		expected = "{indent}\n" +
				"%%( font-size: 100%; color:purple)\n" +
				"testing\n" +
				"%%\n" +
				"{indent}";
		actual = tester.convertIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertIndent_order() {
		String input, expected, actual;
		input = "%%(color:red; margin-left:2in;border:1px solid grey)\n" +
				"foo\n" +
				"\n" +
				"bar\n" +
				"%%\n";
		expected = "{indent}\n" +
				"%%(color:red; border:1px solid grey)\n" +
				"foo\n" +
				"\n" +
				"bar\n" +
				"%%\n" +
				"{indent}\n";
		actual = tester.convertIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveIndent() {
		String input, expected, actual;
		input = "color:red";
		expected = input;
		actual = tester.removeIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "margin-left:4em;color:red";
		actual = tester.removeIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "text-indent:2in; color:red";
		expected = " color:red";
		actual = tester.removeIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "color:blue; margin-left:2in; background-color:black;";
		expected = "color:blue;  background-color:black;";
		actual = tester.removeIndent(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
