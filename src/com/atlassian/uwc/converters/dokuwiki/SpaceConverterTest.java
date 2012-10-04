package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class SpaceConverterTest extends TestCase {

	SpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SpaceConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties properties = tester.getProperties();
		tester.clear(); //important for the space-X settings
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


	public void testConvert_SpacenameRule_nocase() {
		tester.getProperties().setProperty("spacename-rule-regex", "en[g]");//if you see this regex in any directory
		tester.getProperties().setProperty("spacename-rule-prefix", "Prefixed "); //add this prefix to the spacename
		tester.getProperties().setProperty("spacename-rule-uppercase", "false"); //to upper case each first letter of a word 
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "foo";
		String expected = "Prefixed foo";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
 		assertTrue(page.hasSpace(page.getSpacekey()));
		String[] spaceData = page.getSpaceData(page.getSpacekey());
		assertEquals(expected, spaceData[0]);
		
	}
	public void testConvert_SpacenameRule_nomatch() {
		tester.getProperties().setProperty("spacename-rule-regex", "bar");//if you see this regex in any directory
		tester.getProperties().setProperty("spacename-rule-prefix", "Prefixed "); //add this prefix to the spacename
		tester.getProperties().setProperty("spacename-rule-uppercase", "false");
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "foo";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
		
	}
	
	public void testConvert_SpacenameRule_case() {
		tester.getProperties().setProperty("spacename-rule-regex", "en[g]");//if you see this regex in any directory
		tester.getProperties().setProperty("spacename-rule-prefix", "Prefixed "); //add this prefix to the spacename
		tester.getProperties().setProperty("spacename-rule-uppercase", "true"); //to upper case each first letter of a word 
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "foo";
		String expected = "Prefixed Foo";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
		assertTrue(page.hasSpace(page.getSpacekey()));
		String[] spaceData = page.getSpaceData(page.getSpacekey());
		assertEquals(expected, spaceData[0]);
	}
	
	
	public void testConvert_SpacenameRule_casenomatch() {
		tester.getProperties().setProperty("spacename-rule-regex", "bar");//if you see this regex in any directory
		tester.getProperties().setProperty("spacename-rule-prefix", "Prefixed "); //add this prefix to the spacename
		tester.getProperties().setProperty("spacename-rule-uppercase", "true"); //to upper case each first letter of a word 
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "foo";
		String expected = "Foo";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
	}
	
	public void testConvert_siblingfile() {
		tester.getProperties().setProperty("space-lala","sampleData/dokuwiki/SampleDokuwiki-InputLists");
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "lala";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
	}
	
	public void testConvert_VersionPage() {
		
		tester.getProperties().setProperty("space-lala","sampleData/dokuwiki/SampleDokuwiki-InputLists");
		String path = "sampleData/dokuwiki/SampleDokuwiki-InputLists.txt";
		String expkey = "tralala";
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new VersionPage(file);
		Page parent = new Page(new File("sampleData/dokuwiki/SampleDokuwiki-InputBasic.txt"));
		parent.setSpacekey(expkey);
		page.setParent(parent);
		assertNull(page.getSpacekey());
		tester.convert(page);
		assertNotNull(page.getSpacekey());
		assertEquals(expkey, page.getSpacekey());
	}
	
}
