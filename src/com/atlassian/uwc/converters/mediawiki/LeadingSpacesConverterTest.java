package com.atlassian.uwc.converters.mediawiki;

import java.util.Properties;
import java.util.Stack;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.LeadingSpacesBaseConverter;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.TokenMap;

public class LeadingSpacesConverterTest extends TestCase {

	LeadingSpacesBaseConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new LeadingSpacesConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties properties = new Properties();
		//old behavior for purposes of not having to update the tests
		properties.setProperty(LeadingSpacesConverter.PROPKEY_TOKENIZE, "false"); 
		tester.setProperties(properties);
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
		Properties props = tester.getProperties();
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
		Properties props = tester.getProperties();
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
	
	public void testIgnoreListsWithBold() {
		Properties props = tester.getProperties();
		props.setProperty("leading-spaces-delim", "code");
		props.setProperty("leading-spaces-noformat", "false");
		tester.setProperties(props);
		String input, expected, actual;
		input = "h1. *Foobar*\n" + 
				"* [Foobar]\n" + 
				"\n" + 
				"* *[Foo Bar Meh]*\n" + 
				"* *[Something & Another]*\n" + 
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
	
	public void testConvertPage_tokenized() {
		tester.getProperties().clear(); //is same as: props.setProperty(LeadingSpacesConverter.PROPKEY_TOKENIZE, "true"); \
		Properties props = tester.getProperties();
		props.setProperty("leading-spaces-delim", "code");

		tester.setProperties(props);
		String input, expected, actual;
		input = "  abc\n" +
				"  def\n";
		String startswith = "\n" + 
						"~UWCTOKENSTART~";
		String endswith = "~UWCTOKENEND~\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertTrue(actual, actual.startsWith(startswith));
		assertTrue(actual, actual.endsWith(endswith));
		
		expected = "\n" +
				"{code}\n" +
				"  abc\n" +
				"  def\n" +
				"{code}\n";
		Stack<String> keys = TokenMap.getKeys();
		assertNotNull(keys);
		assertFalse(keys.isEmpty());
		String detokenizeText = TokenMap.detokenizeText(actual);
		assertNotNull(actual);
		assertEquals(expected, detokenizeText);
	}
	public void testConvertPage_problem1() {
		Properties props = tester.getProperties();
		props.setProperty("leading-spaces-delim", "code");
		tester.setProperties(props);
		String input, expected, actual;
		input = "Tralala\n" + 
				"\n" + 
				" asldkjas: http://lakdjlaskjd/\n" + 
				" aslkdjasd:  http://amsdkjahsd/\n" + 
				"\n" + 
				" <problem>\n" + 
				" \n" + 
				"  <ok>\n" + 
				"  </ok>\n" + 
				"";
		expected = "Tralala\n" + 
				"\n" + 
				"{code}\n" + 
				" asldkjas: http://lakdjlaskjd/\n" + 
				" aslkdjasd:  http://amsdkjahsd/\n" + 
				"{code}\n" + 
				"\n" + 
				"{code}\n" + 
				" <problem>\n" + 
				"{code}\n" + 
				" \n" + 
				"{code}\n" + 
				"  <ok>\n" + 
				"  </ok>\n" + 
				"{code}\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
