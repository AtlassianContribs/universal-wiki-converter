package com.atlassian.uwc.converters.trac;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.trac.UserDateConverter.UserDate;
import com.atlassian.uwc.ui.Page;

public class UserDateConverterTest extends TestCase {

	UserDateConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		tester = new UserDateConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = new Properties();
		props.setProperty("userdata-filepath", "/Users/laura/Code/Git/uwc/sampleData/trac/testuserdata.txt");
		tester.setProperties(props);
	}
	
	public void testUser() {
		File file = new File("Testpage");
		Page page = new Page(file);
		page.setName(file.getName());
		assertNull(page.getAuthor());
		tester.convert(page);
		assertNotNull(page.getAuthor());
		assertEquals("user1", page.getAuthor());
		assertNotNull(page.getComments());
		assertTrue(page.getComments().isEmpty());
	}
	
	public void testDateSimple() {
		File file = new File("Testpage");
		Page page = new Page(file);
		page.setName(file.getName());
		assertNull(page.getTimestamp());
		tester.convert(page);
		Date actual = page.getTimestamp();
		assertNotNull(actual);
		assertEquals(new Date(1324663500L*1000), actual);
	}
	
	public void testDateLongEpochs() {
		File file = new File("Milliepoch");
		Page page = new Page(file);
		page.setName(file.getName());
		assertNull(page.getTimestamp());
		tester.convert(page);
		Date actual = page.getTimestamp();
		assertNotNull(actual);
		assertEquals(new Date(1324663500000L), actual);
		
		File file2 = new File("Really Long Epoch");
		Page page2 = new Page(file2);
		page2.setName(file2.getName());
		assertNull(page2.getTimestamp());
		tester.convert(page2);
		actual = page2.getTimestamp();
		assertNotNull(actual);
		assertEquals(new Date(1324663500000000L/1000), actual);
	}
	
	public void testHierarchyStylePagename() {
		File file = new File("Parent%2FChild");
		Page page = new Page(file);
		page.setName(file.getName());
		assertNull(page.getAuthor());
		tester.convert(page);
		assertNotNull(page.getAuthor());
		assertEquals("user4", page.getAuthor());
	}
	
	public void testTimestampCreation() {
		String seconds = "1324663500";
		UserDate secUD = new UserDateConverter().new UserDate("user", seconds);
		Date actual = secUD.timestamp;
		Date expected = new Date(1324663500L*1000);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String milli = "1324663500100";
		UserDate milliUD = new UserDateConverter().new UserDate("user", milli);
		actual = milliUD.timestamp;
		expected = new Date(1324663500100L);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String more = "1324663500100200";
		UserDate moreUD = new UserDateConverter().new UserDate("user", more);
		actual = moreUD.timestamp;
		expected = new Date(1324663500100200L/1000);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String unexpected = "13246635001009";
		UserDate unexpUD = new UserDateConverter().new UserDate("user", unexpected);
		actual = unexpUD.timestamp;
		expected = new Date(1324663500100L);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		unexpected = "132466350010";
		unexpUD = new UserDateConverter().new UserDate("user", unexpected);
		actual = unexpUD.timestamp;
		expected = new Date(1324663500100L);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testArchiveCreator() {
		Properties props = tester.getProperties();
		props.setProperty("archivecreator-filepath", "/Users/laura/Code/Git/uwc/sampleData/trac/testuserdata.txt");
		props.setProperty("archivecreator-comment", "Testing %username%");
		tester.setProperties(props);
		
		File file = new File("Testpage");
		Page page = new Page(file);
		page.setName(file.getName());
		assertNull(page.getAuthor());
		tester.convert(page);
		assertNotNull(page.getAuthor());
		assertEquals("user1", page.getAuthor());
		
		Vector<String> actual = page.getComments();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		assertEquals(1, actual.size());
		String comment = actual.get(0);
		assertNotNull(comment);
		assertEquals("Testing user1", comment);
	}
}
