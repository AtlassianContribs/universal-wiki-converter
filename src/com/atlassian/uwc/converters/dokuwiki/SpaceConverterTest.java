package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class SpaceConverterTest extends TestCase {

	SpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SpaceConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties properties = tester.getProperties();
		properties.setProperty("space-foo", "sampleData/dokuwiki,sampleData/engine");
		properties.setProperty("space-bar", "bin");
	}
	
	public void testConvert() {
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expected = "foo";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expected, page.getSpacekey());

		path = "bin/log4j.properties";
		expected = "bar";
		file = new File(path);
		assertTrue(file.exists());
		page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expected, page.getSpacekey());
		
		path = "sampleData/engine/README.txt";
		expected = "foo";
		file = new File(path);
		assertTrue(file.exists());
		page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expected, page.getSpacekey());
		
		path = "sampleData/mediawiki/readme.txt";
		file = new File(path);
		assertTrue(file.exists());
		page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNull(page.getSpacekey());
	}

}
