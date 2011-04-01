package com.atlassian.uwc.converters.jive;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TitleConverterTest extends TestCase {

	TitleConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TitleConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTitle() {
		String input, expected, actual;
		Page page = new Page(null);
		input = "Testing" +
				"{{title: Testing 123 }}\n" +
				"Testing";
		page.setOriginalText(input);
		page.setConvertedText(input);
		page.setName("oldname");
		expected = "Testing 123";
		actual = tester.convertTitle(input, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTitle_StripTitleMacro() {
		String input, expected, actual;
		Page page = new Page(null);
		input = "Testing\n" +
				"{{title: Testing 123 }}\n" +
				"Testing";
		page.setOriginalText(input);
		page.setConvertedText(input);
		page.setName("oldname");
		expected = "Testing\n" +
				"\n" +
				"Testing";
		tester.convert(page);
		String acttitle = page.getName();
		String exptitle = "Testing 123";
		assertNotNull(acttitle);
		assertEquals(exptitle, acttitle);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
