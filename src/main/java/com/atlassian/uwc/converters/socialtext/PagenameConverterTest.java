package com.atlassian.uwc.converters.socialtext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class PagenameConverterTest extends TestCase {

	PagenameConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PagenameConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String input, expected, actual;
		input = "Something: Something\n" +
				"Subject: Testing Foo Bar \n" + 
				"From: abc@def.org\n" +
				"Date: 2009-08-03 12:00:00 EDT\n" +
				"Received: from 127.0.0.1\n" +
				"Revision: 1\n" +
				"Type: wiki\n" + 
				"Summary: Testing 123\n" + 
				"Category: testlabel\n" + 
				"Encoding: utf8\n" + 
				"Page Content Here\n";
		expected = "Testing Foo Bar";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testing 123";
		page.setName("filename.txt");
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getName();
		expected = "filename.txt";
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
