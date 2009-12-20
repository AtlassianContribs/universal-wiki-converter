package com.atlassian.uwc.converters;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class JavaRegexConverterTest extends TestCase {

	Converter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
	}
	protected void tearDown() throws Exception {
		tester = null;
	}

	public void testConvert_Basic() {
		String input, expected, actual, value;
		value = "test{replace-with}foobar";
		input = "testing";
		expected = "foobaring";
		tester = JavaRegexConverter.getConverter(value);
		tester.setValue(value);
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_Basic2() {
		String input, expected, actual, value;
		value = "__([^_]+)__{replace-with}+$1+";
		input = "before __testing__ after";
		expected = "before +testing+ after";
		tester = JavaRegexConverter.getConverter(value);
		tester.setValue(value);
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_EscapeDollarsReplacement() {
		String input, expected, actual, value;
		value = "__([^_]+)__{replace-with}+$1+";
		input = "$before $1 __testing $1__ $after$1";
		expected = "$before $1 +testing $1+ $after$1";
		tester = JavaRegexConverter.getConverter(value);
		tester.setValue(value);
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
