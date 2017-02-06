package com.atlassian.uwc.converters.twiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AutoNumberListConverterTest extends TestCase {

	AutoNumberListConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new AutoNumberListConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertNumberedList_Numbers() {
		String input, expected, actual;
		input = "\n" + 
				"   1. Sushi\n" + 
				"   1. Dim Sum\n" + 
				"   1. Fondue\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertNumberedList_Letters() {
		String input, expected, actual;
		input = "   A. Sushi\n" + 
				"   A. Dim Sum\n" + 
				"   A. Fondue\n" + 
				"";
		expected = "# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertNumberedList_lowercase() {
		String input, expected, actual;
		input = "\n" + 
				"   a. Sushi\n" + 
				"   a. Dim Sum\n" + 
				"   a. Fondue\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertNumberedList_Roman() {
		String input, expected, actual;
		input = "\n" + 
				"   i. Sushi\n" + 
				"   i. Dim Sum\n" + 
				"   i. Fondue\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertNumberedList_Combo1() {
		String input, expected, actual;
		input = "   1. Provide means to allow for a configurable IUnknown.\n" + 
				"   a. Unsafe but fast reference counting\n" + 
				"   a. Thread safe reference counting via spin locks\n" + 
				"   a. No reference counting.\n" + 
				"   1. Support enumeration of factories supporting a given interface.\n" + 
				"   1. \"Slicing\" of components.";
		expected = "# Provide means to allow for a configurable IUnknown.\n" + 
				"## Unsafe but fast reference counting\n" + 
				"## Thread safe reference counting via spin locks\n" + 
				"## No reference counting.\n" + 
				"# Support enumeration of factories supporting a given interface.\n" + 
				"# \"Slicing\" of components." + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testConvertNumberedList_Combo2() {
		String input, expected, actual;
		input = "   i. Provide means to allow for a configurable IUnknown.\n" + 
				"   1. Unsafe but fast reference counting\n" + 
				"   1. Thread safe reference counting via spin locks\n" + 
				"   1. No reference counting.\n" + 
				"   i. Support enumeration of factories supporting a given interface.\n" + 
				"   i. \"Slicing\" of components.\n";
		expected = "# Provide means to allow for a configurable IUnknown.\n" + 
				"## Unsafe but fast reference counting\n" + 
				"## Thread safe reference counting via spin locks\n" + 
				"## No reference counting.\n" + 
				"# Support enumeration of factories supporting a given interface.\n" + 
				"# \"Slicing\" of components.\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertNumberedList_BacktoBack() {
		String input, expected, actual;
		input = "\n" + 
				"   1. Sushi\n" + 
				"   1. Dim Sum\n" + 
				"   1. Fondue\n" +
				"\n" +
				"   a. Apple\n" +
				"   a. Orange\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" +
				"\n" +
				"# Apple\n" +
				"# Orange\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConverterNumbered_NeedsThreeSpaces() {
		String input, expected, actual;
		input = "\n" + 
				"1. Sushi\n" + 
				" 1. Dim Sum\n" + 
				"  1. Fondue\n" + 
				"";
		expected = input;
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}
	
	public void testConverterNumbered_MultiplesOfThreeOk() {
		String input, expected, actual;
		input = "\n" + 
				"      1. Sushi\n" + 
				"      1. Dim Sum\n" + 
				"      1. Fondue\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}

	public void testBadChars() {
		String input, expected, actual;
		input = "\n" + 
				"   1. $1Sushi\n" + 
				"   1. \\\\Dim Sum\n" + 
				"";
		expected = "\n" +
				"# $1Sushi\n" + 
				"# \\\\Dim Sum\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetCurrentDelim() {
		String input, expected, actual;
		expected = "#";
		actual = tester.getCurrentDelim("1");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.getCurrentDelim("1");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.getCurrentDelim("a");
		expected = "##";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.getCurrentDelim("a");
		expected = "##";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "#";
		actual = tester.getCurrentDelim("1");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "##";
		actual = tester.getCurrentDelim("I");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "###";
		actual = tester.getCurrentDelim("i");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "#";
		actual = tester.getCurrentDelim("1");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertNumberedList_Numbers_NoDot() {
		String input, expected, actual;
		input = "\n" + 
				"   1 Sushi\n" + 
				"   1 Dim Sum\n" + 
				"   1 Fondue\n" + 
				"";
		expected = "\n" +
				"# Sushi\n" + 
				"# Dim Sum\n" + 
				"# Fondue\n" + 
				"";
		actual = tester.convertNumberedList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
