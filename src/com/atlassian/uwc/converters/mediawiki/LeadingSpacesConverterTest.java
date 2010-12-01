package com.atlassian.uwc.converters.mediawiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class LeadingSpacesConverterTest extends TestCase {

	LeadingSpacesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new LeadingSpacesConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertPage() {
		String input, expected, actual;
		input = "123\n" +
				"  abc\n" +
				"  def\n";
		expected = "" +
				"123\n" +
				"{panel}\n" +
				"  abc\n" +
				"  def\n" +
				"{panel}\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPage_codedelim() {
		Properties props = new Properties();
		props.setProperty("leading-spaces-delim", "code");
		tester.setProperties(props);
		String input, expected, actual;
		input = "123\n" +
				"  abc\n" +
				"  def\n";
		expected = "" +
				"123\n" +
				"{code}\n" +
				"  abc\n" +
				"  def\n" +
				"{code}\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}	
	

	public void testConvertPage_startsatbeginning() {
		Properties props = new Properties();
		props.setProperty("leading-spaces-delim", "code");
		tester.setProperties(props);
		String input, expected, actual;
		input = "  abc\n" +
				"  def\n";
		expected = "\n" +
				"{code}\n" +
				"  abc\n" +
				"  def\n" +
				"{code}\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPage_butNotEverything() {
		String input, expected, actual;
		input = "h1. ABC DEF GHI (HIJ)\n" + 
				"\n" + 
				"h2. KJL\n" + 
				"";
		expected = input; 
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
