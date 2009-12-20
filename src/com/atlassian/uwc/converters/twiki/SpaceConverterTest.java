package com.atlassian.uwc.converters.twiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SpaceConverterTest extends TestCase {

	SpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SpaceConverter();
		Properties props = new Properties();
		props.setProperty("space-Sandbox", "uwctest");
		tester.setProperties(props);
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertSpaces_Image() {
		String input, expected, actual;
		input = "!Sandbox:SampleFooBar^cow.jpg!";
		expected = "!uwctest:SampleFooBar^cow.jpg!";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSpaces_ImageProps() {
		String input, expected, actual;
		input = "!Sandbox:SampleFooBar^cow.jpg|alt=\"test\",width=\"400\"!";
		expected = "!uwctest:SampleFooBar^cow.jpg|alt=\"test\",width=\"400\"!";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertSpaces_Ignore() {
		String input, expected, actual;
		input = "!Sandbox.jpg!";
		expected = "!Sandbox.jpg!";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertSpaces_Ignore2() {
		String input, expected, actual;
		input = "!Sandbox^cow.jpg!";
		expected = "!Sandbox^cow.jpg!";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSpaces_Links() {
		String input, expected, actual;
		input = "[Sandbox:SampleFooBar^cow.jpg]";
		expected = "[uwctest:SampleFooBar^cow.jpg]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertSpaces_LinksAlias() {
		String input, expected, actual;
		input = "[Testing|Sandbox:SampleFooBar^cow.jpg]";
		expected = "[Testing|uwctest:SampleFooBar^cow.jpg]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}	
	
	public void testConvertSpaces_Ignore3() {
		String input, expected, actual;
		input = "[Sandbox.jpg]";
		expected = "[Sandbox.jpg]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertSpaces_Ignore4() {
		String input, expected, actual;
		input = "[Sandbox^cow.jpg]";
		expected = "[Sandbox^cow.jpg]";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
