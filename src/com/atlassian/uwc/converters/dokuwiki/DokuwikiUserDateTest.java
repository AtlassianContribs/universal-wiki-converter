package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class DokuwikiUserDateTest extends TestCase {

	DokuwikiUserDate tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new DokuwikiUserDate();
		PropertyConfigurator.configure("log4j.properties");
		tester.getProperties().setProperty("meta-dir", "sampleData/dokuwiki/junit_resources/meta");
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "sampleData/dokuwiki/junit_resources/pages");
	}
	
	public void testConvert() {
		String pagedir = "sampleData/dokuwiki/junit_resources/pages";
		Page page = new Page(new File(pagedir+"/start.txt"));
		page.setPath(page.getFile().getPath().replaceFirst("\\/[^\\/]*$", ""));
		
		assertNotNull(page);
		assertNull(page.getTimestamp());
		assertNull(page.getAuthor());
		
		tester.convert(page);
		assertNotNull(page);
		
		Date date = page.getTimestamp();
		assertNotNull(date);
		assertEquals(new Date(1263490649*1000), date);

		String author = page.getAuthor();
		assertNotNull(author);
		assertEquals("notmod", author);
		
	}
	
	public void testConvert_anon() {
		String pagedir = "sampleData/dokuwiki/junit_resources/pages";
		Page page = new Page(new File(pagedir+"/test_link.txt"));
		page.setPath(page.getFile().getPath().replaceFirst("\\/[^\\/]*$", ""));
		
		assertNotNull(page);
		assertNull(page.getTimestamp());
		assertNull(page.getAuthor());
		
		tester.convert(page);
		assertNotNull(page);
		
		Date date = page.getTimestamp();
		assertNotNull(date);
		assertEquals(new Date(1263490771*1000), date);

		String author = page.getAuthor();
		assertNull(author);

	}
	
}
