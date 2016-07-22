package com.atlassian.uwc.converters.jive;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class SpaceConverterTest extends TestCase {

	SpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = null;
	protected void setUp() throws Exception {
		tester = new SpaceConverter();
		PropertyConfigurator.configure("log4j.properties");
		props = new Properties();
		props.setProperty("spacemap-200-600", "testconf");
		props.setProperty("spacemap-201-14", "testconf2");
		props.setProperty("spacemap-202-14", "testconf3");
		props.setProperty("spacedata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_containerdata.txt");
		tester.setProperties(props);
	}

	public void testSpaceConverter_test1() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=600|containerid=200}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "testconf";
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertTrue(page.hasSpace(expected));
		String[] data = page.getSpaceData(expected);
		assertNotNull(data);
		assertEquals(2, data.length);
		assertEquals("Test Project 1", data[0]);
		assertEquals("Test Description 1", data[1]);
	}
	
	public void testSpaceConverter_test2() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=14|containerid=201}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "testconf2";
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertTrue(page.hasSpace(expected));
		String[] data = page.getSpaceData(expected);
		assertNotNull(data);
		assertEquals(2, data.length);
		assertEquals("Test Space 2", data[0]);
		assertEquals("Test Description 2", data[1]);
	}
	
	public void testSpaceConverter_test3() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=14|containerid=202}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "testconf3";
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertTrue(page.hasSpace(expected));
		String[] data = page.getSpaceData(expected);
		assertNotNull(data);
		assertEquals(2, data.length);
		assertEquals("Test Space 3", data[0]);
		assertEquals("", data[1]);
	}
	
	public void testSpaceConverter_testNoSpace() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=14|containerid=200}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		assertNull(actual);
	}
	
	public void testSpaceConverter_isPersonalSpace() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=BLOG|containertype=2020|containerid=300|usercontainername=admin}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "~admin";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String[] spaceData = page.getSpaceData(expected);
		assertNotNull(spaceData);
		assertEquals("admin", spaceData[0]);
		assertEquals("New Personal Space!", spaceData[1]);
		assertTrue(page.isPersonalSpace());
		assertEquals("admin", page.getPersonalSpaceUsername());
	}
	
	public void testSpaceConverter_DOCinPersonalSpace() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=2020|containerid=300}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "~admin";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String[] spaceData = page.getSpaceData(expected);
		assertNotNull(spaceData);
		assertEquals("admin", spaceData[0]);
		assertEquals("New Personal Space!", spaceData[1]);
		assertTrue(page.isPersonalSpace());
		assertEquals("admin", page.getPersonalSpaceUsername());
	}
	
	public void testSpaceConverter_testInit() {
		String input, expected, actual;
		input = "{jive-export-meta:id=1001|version=2|type=DOC|containertype=600|containerid=200}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		actual = page.getSpacekey();
		expected = "testconf";
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertTrue(page.hasSpace(expected));
		String[] data = page.getSpaceData(expected);
		assertNotNull(data);
		assertEquals(2, data.length);
		assertEquals("Test Project 1", data[0]);
		assertEquals("Test Description 1", data[1]);
		
		tester.convert(page);
	}
}
