package com.atlassian.uwc.converters.mediawiki;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class UserDateConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	UserDateConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new UserDateConverter();
	}
	
	public void testConvert() {
		String input, expected, actual;
		input = "{user:foobar}\n" +
		"{timestamp:20011231235959}\n" +
		"Testing 123";
		expected = "Testing 123";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = page.getAuthor();
		expected = "foobar";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Date timestamp = page.getTimestamp();
		Date expTime = new Date(2001-1900, 11, 31, 23, 59, 59);
		assertEquals(expTime.getTime(), timestamp.getTime());
	}
	public void testCleanUserDate() {
		String input, expected, actual;
		input = "{user:foobar}\n" +
				"{timestamp:20011231235959}\n" +
				"Testing 123";
		expected = "Testing 123";
		actual = tester.cleanUserDate(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
