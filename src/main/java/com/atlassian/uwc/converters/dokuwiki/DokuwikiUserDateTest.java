package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

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
		Date exp = new Date(new Long(1263490649)*1000);
		assertEquals(exp, date);

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
		assertEquals(new Date(new Long(1263490771)*1000), date);

		String author = page.getAuthor();
		assertNull(author);

	}
	
	public void testVersionPage() {
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "sampleData/engine/history/pages");
		tester.getProperties().setProperty("meta-dir", "sampleData/engine/history/meta");
		String pagedir = "sampleData/engine/history/ancestors/foo/page.1234567892.txt";
		Page page = new VersionPage(new File(pagedir));
		page.setPath(page.getFile().getPath().replaceFirst("\\/[^\\/]*$", ""));
		page.setName("page");
		page.setTimestamp(new Date(new Long(1234567892)*1000)); //we set this in the engine for VersionPage
		page.setParent(new Page(new File("sampleData/engine/history/pages/foo/page.txt")));
		
		assertNotNull(page);
		assertNull(page.getAuthor());
		
		tester.convert(page);
		assertNotNull(page);
		assertNotNull(page.getAuthor());
		
		String author = page.getAuthor();
		assertNotNull(author);
		assertEquals("authortest", author);
	}
	
	
	public void testCreateChangeFilename_Basic() {
		String input, expected, actual;
		
		String pagedir = "sampleData/dokuwiki/junit_resources/pages";
		Page page = new Page(new File(pagedir+"/start.txt"));

		expected = "sampleData/dokuwiki/junit_resources/meta/start.changes";
		actual = tester.createChangeFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateChangeFilename_WithNS() {
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "sampleData/engine/history/pages");
		tester.getProperties().setProperty("meta-dir", "sampleData/engine/history/meta");
		tester.getProperties().setProperty("page-history-load-as-ancestors-dir", "sampleData/engine/history/ancestors");
		
		String input, expected, actual;
		String pagepath = "sampleData/engine/history/pages/foo/page.txt";
		Page page = new Page(new File(pagepath));

		expected = "sampleData/engine/history/meta/foo/page.changes";
		actual = tester.createChangeFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateChangeFilename_Ancestor() {
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "sampleData/engine/history/pages");
		tester.getProperties().setProperty("meta-dir", "sampleData/engine/history/meta");
		tester.getProperties().setProperty("page-history-load-as-ancestors-dir", "sampleData/engine/history/ancestors");
		
		String input, expected, actual;
		String pagepath = "sampleData/engine/history/ancestors/foo/page.1234567892.txt";
		Page page = new VersionPage(new File(pagepath));
		page.setParent(new Page(new File("sampleData/engine/history/pages/foo/page.txt")));

		expected = "sampleData/engine/history/meta/foo/page.changes";
		actual = tester.createChangeFilename(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetHistoryLine() {
		String input, expected, actual;
		input = "1233490141  127.0.0.1   C  start laurakolker created\n" + 
				"1234567892  127.0.0.1   E  start authortest\n" + 
				"1263490629  127.0.0.1   E  start notmod   comment\n" + 
				"1263490649  127.0.0.1   E  start notmod\n" + 
				"";
		Date timestamp = new Date(new Long(1234567892)*1000);
		expected = "1234567892  127.0.0.1   E  start authortest";
		actual = tester.getHistoryLine(input, timestamp);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		timestamp = new Date(new Long(1263490629)*1000);
		expected = "1263490629  127.0.0.1   E  start notmod   comment";
		actual = tester.getHistoryLine(input, timestamp);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
