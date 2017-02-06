package com.atlassian.uwc.filters;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TimestampFilterTest extends TestCase {

	TimestampFilter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	String testdir = "sampleData/socialtext/SampleSocialtext-InputFilter/";
	protected void setUp() throws Exception {
		tester = new TimestampFilter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testAccept() {
		File file  = null;
		//not the highest timestamp
		file = new File(testdir + "20090805120000.txt");
		assertTrue(file.exists());
		assertFalse(tester.accept(file));
		
		file = new File(testdir + "20090805170912.txt");
		assertTrue(file.exists());
		assertFalse(tester.accept(file));
		
		//highest timestamp
		file = new File(testdir + "20090806120000.txt");
		assertTrue(file.exists());
		assertTrue(tester.accept(file));
		
		//index.txt is never right
		file = new File(testdir + "index.txt");
		assertTrue(file.exists());
		assertFalse(tester.accept(file));
		
		//other filenames are always ok (for test purposes)
		file = new File("sampleData/socialtext/SampleSocialtext-InputLists.txt");
		assertTrue(file.exists());
		assertTrue(tester.accept(file));
	}

	public void testGetMostRecent() {
		File file  = null;
		long expected, actual;
		expected = Long.parseLong("20090806120000");
		//not the highest timestamp
		file = new File(testdir + "20090805120000.txt");
		actual = tester.getMostRecent(file);
		assertEquals(expected, actual);
		
		file = new File(testdir + "20090805170912.txt");
		actual = tester.getMostRecent(file);
		assertEquals(expected, actual);
		
		//highest timestamp
		file = new File(testdir + "20090806120000.txt");
		actual = tester.getMostRecent(file);
		assertEquals(expected, actual);
		
	}
}
