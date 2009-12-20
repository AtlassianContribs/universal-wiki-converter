package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.util.TokenMap;

public class EscapeBracesConverterTest extends TestCase {

	EscapeBracesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new EscapeBracesConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testEscapeBraces_Simple() {
		String input, expected, actual;
		input = "Just a couple { of braces  } ";
		expected = "Just a couple \\{ of braces  \\} ";
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapeBraces_NotMath() {
		String input, expected, actual;
		input = "<math>f(x) = \\int_0^1 e^{-t} g(t) \\, dt.</math>";
		expected = input;
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEscapeBraces_NotPre() {
		String input, expected, actual;
		input = "<pre>f(x) = \\int_0^1 e^{-t} g(t) \\, dt.</pre>";
		expected = input;
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testEscapeBraces_NotTables() {
		String input, expected, actual;
		input = "{|\n" + 
				"|+caption\n" + 
				"!This | is | a\n" + 
				"|-\n" + 
				"||table||.||It\n" + 
				"|-\n" + 
				"|should be\n" + 
				"|left\n" + 
				"|alone\n" + 
				"|}\n" + 
				"\n" +
				"{| border=\"1\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\"\n" + 
				"! This\n" + 
				"! is\n" + 
				"|-\n" + 
				"| a\n" + 
				"| table\n" + 
				"|}\n" + 
				""; 
		expected = input;
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapeBraces_NotAlreadyEscaped() {
		String input, expected, actual;
		input = "escape these { } not these \\{ \\}";
		expected = "escape these \\{ \\} not these \\{ \\}";
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEscapeBraces_GeneralMacros() {
		String input, expected, actual;
		input = "Don't {{escape}} this";
		expected = input;
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{{listen\n" + 
				" |title    = Flow my tears\n" + 
				" |filename = Flow my tears.ogg\n" + 
				" |filesize = 583KB\n" + 
				"}}\n" + 
				"\n"; 
		expected = input;
		actual = tester.escapeBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	
	public void testTokenizeMath() {
		String input, expected, actual;
		input = "<math>f(x) = \\int_0^1 e^{-t} g(t) \\, dt.</math>";
		actual = tester.tokenizeMath(input);
		assertNotNull(actual);
		assertTrue(actual.startsWith(TokenMap.TOKEN_START));
		assertTrue(actual.endsWith(TokenMap.TOKEN_END));
		actual = tester.detokenize(actual);
		assertNotNull(actual);
		assertEquals(input, actual);
	}

	public void testTokenizeDoubleBraceSyntax() {
		String input, expected, actual;
		input = "{{abc}}";
		actual = tester.tokenizeDoubleBraceSyntax(input);
		assertNotNull(actual);
		assertTrue(actual.startsWith(TokenMap.TOKEN_START));
		assertTrue(actual.endsWith(TokenMap.TOKEN_END));
		actual = tester.detokenize(actual);
		assertNotNull(actual);
		assertEquals(input, actual);
	}

	public void testTokenizeTables() {
		String input, expected, actual;
		input = "{| border=\"1\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\"\n" + 
				"! This\n" + 
				"! is\n" + 
				"|-\n" + 
				"| a\n" + 
				"| table\n" + 
				"|}";
		actual = tester.tokenizeTables(input);
		assertNotNull(actual);
		assertTrue(actual.startsWith(TokenMap.TOKEN_START));
		assertTrue(actual.endsWith(TokenMap.TOKEN_END));
		actual = tester.detokenize(actual);
		assertNotNull(actual);
		assertEquals(input, actual);
	}

	public void testEscapeSingleBraces() {
		String input, expected, actual;
		input = "Testing 123 { 456 }";
		expected = "Testing 123 \\{ 456 \\}";
		actual = tester.escapeSingleBraces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testDetokenize() {
		//This is getting tested in the testTokenize* methods
	}
	

}
