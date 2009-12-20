package com.atlassian.uwc.converters.mediawiki;

import java.util.Properties;

import junit.framework.TestCase;

import com.atlassian.uwc.ui.Page;

public class ConvertUnderscoresInTitleTest extends TestCase {

	ConvertUnderscoresInTitle tester = null;
	protected void setUp() throws Exception {
		super.setUp();
		tester = new ConvertUnderscoresInTitle();
	}

	public void testConvertUnderscores() {
		String input = "One_Underscore";
		String expected = "One Underscore";
		String actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
		
		input = "Two__Underscores";
		expected = input;
		actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
		
		input = "_At Beginning";
		expected = " At Beginning";
		actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
		
		input = "____Too many";
		expected = input;
		actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
		
		input = "Each_space_has_an_underscore";
		expected = "Each space has an underscore";
		actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
		
		input = "Each_space__has_an_underscore";
		expected = "Each space__has an underscore";
		actual = tester.convertUnderscores(input);
		assertEquals(expected, actual);
	}


	public void testUnderscoresInContent() {
		String input = "[Copyright after 1978|Photo_Research___Copyright_after_1978]\n";
		String title = "Foo_Bar";
		String expected = "[Copyright after 1978|Photo Research___Copyright after 1978]\n";
		String expTitle = "Foo Bar";
		
		Page page = new Page(null);
		page.setName(title);
		page.setOriginalText(input);
		page.setConvertedText(input);
		Properties props = new Properties();
		props.setProperty("underscore2space-links", "true");
		tester.setProperties(props);
		
		tester.convert(page);
		
		assertNotNull(page);
		String actContent = page.getConvertedText();
		String actTitle = page.getName();
		assertNotNull(actTitle);
		assertNotNull(actContent);
		assertEquals(expTitle, actTitle);
		assertEquals(expected, actContent);
	}
	
	public void testProperties() {
		String input = "[Copyright after 1978|Photo_Research___Copyright_after_1978]\n";
		String expected = "[Copyright after 1978|Photo Research___Copyright after 1978]\n";
		
		//no props
		Page page = new Page(null);
		page.setName("Foo Bar");
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		
		assertNotNull(page);
		String actContent = page.getConvertedText();
		assertNotNull(actContent);
		assertEquals(input, actContent);

		//prop is false
		page = new Page(null);
		page.setName("Foo Bar");
		page.setOriginalText(input);
		page.setConvertedText(input);
		Properties props = new Properties();
		props.setProperty("underscore2space-links", "false");
		tester.setProperties(props);
		tester.convert(page);
		
		assertNotNull(page);
		actContent = page.getConvertedText();
		assertNotNull(actContent);
		assertEquals(input, actContent);

		//prop is true
		page = new Page(null);
		page.setName("Foo Bar");
		page.setOriginalText(input);
		page.setConvertedText(input);
		props = new Properties();
		props.setProperty("underscore2space-links", "true");
		tester.setProperties(props);
		tester.convert(page);
		
		assertNotNull(page);
		actContent = page.getConvertedText();
		assertNotNull(actContent);
		assertEquals(expected, actContent);
	}
}
