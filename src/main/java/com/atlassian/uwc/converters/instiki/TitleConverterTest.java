package com.atlassian.uwc.converters.instiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TitleConverterTest extends TestCase {

	TitleConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Page page = new Page(null);
	protected void setUp() throws Exception {
		tester = new TitleConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	
	public void testTitleHandling() {
		String input, expected, actual;
		input = "foo+bar.xhtml";
		page.setName(input);
		tester.convert(page);
		expected = "Foo Bar";
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testTitleURIEncoding() {
		String input, expected, actual;
		input = "foo+bar%3F.xhtml";
		page.setName(input);
		tester.convert(page);
		expected = "Foo Bar?";
		actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
