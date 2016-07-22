package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SeperateSyntaxesTest extends TestCase {
	SeperateSyntaxes tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SeperateSyntaxes();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testSeperateSyntaxes1() {
		String input, expected, actual;
		input = "_*abc*_def";
		expected = "_*abc*_ def";
		actual = tester.seperateSyntaxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testSeperateSyntaxes2() {
		String input, expected, actual;
		input = "+_abc_+def";
		expected = "+_abc_+ def";
		actual = tester.seperateSyntaxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testSeperateSyntaxes3() {
		String input, expected, actual;
		input = "*+abc+*def";
		expected = "*+abc+* def";
		actual = tester.seperateSyntaxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testSeperateSyntaxes4() {
		String input, expected, actual;
		input = "UWC_TOKEN_START";
		expected = input;
		actual = tester.seperateSyntaxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSeperateSyntaxes5() {
		String input, expected, actual;
		input = "[somebody_else@email.com|mailto:somebody_else@email.com]";
		expected = input;
		actual = tester.seperateSyntaxes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
