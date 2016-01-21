package com.atlassian.uwc.converters.mediawiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ExternalInternalLinksConverterTest extends TestCase {

	private static final String TEST_IDENTIFIER = "https?:\\/\\/mw\\.wiki\\.org\\/";
	ExternalInternalLinksConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ExternalInternalLinksConverter();
		Properties props = new Properties();
		props.setProperty("external-internal-links-identifier", TEST_IDENTIFIER);
		tester.setProperties(props);
	}

	public void testGetExternalLinkIdentifier() {
		String input, expected, actual;
		expected = TEST_IDENTIFIER;
		actual = tester.getExternalLinkIdentifier();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertExternalInternalLinks() {
		String input, expected, actual;
		input = "[http://mw.wiki.org/index.php/Some_Page1]\n" + 
				"[display text|http://mw.wiki.org/index.php/Some_Page2]\n" + 
				"https://mw.wiki.org/index.php/Some_Page5<br/>\n" +
				"[Testing 123 (PDF)|https://mw.wiki.org/images/5/56/Somefile.pdf]" +
				"";
		expected = "[Some Page1]\n" +
				"[display text|Some Page2]\n" +
				"[Some Page5]<br/>\n" +
				"[Testing 123 (PDF)|^Somefile.pdf]";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternalInternalLinks_anchor() {
		String input, expected, actual;
		
		input = "http://mw.wiki.org/index.php/ABC_DEF_abc_def#an_anchor\n"; 
		expected = "[ABC DEF abc def#an anchor]\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternalInternalLinks_combo() {
		String input, expected, actual;
		input = "(*[foo bar lorem-ipsum|http://mw.wiki.org/index.php/" +
				"foo_bar_lorem_ipsum#abc_def_ghi]*)\n"; 
		expected = "(*[foo bar lorem-ipsum|foo bar lorem ipsum#abc def ghi]*)\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertExternalImageLinks_bracket() {
		String input, expected, actual;
		input = "[here|http://mw.wiki.org/images/x/x3/Xxxxxx_1.x_xxxxxxxxxx.xls]\n" +
				"[Here|https://mw.wiki.org/images/x/x8/Xxxxxxxxx.tar]" + 
				"";
		expected = "[here|^Xxxxxx_1.x_xxxxxxxxxx.xls]\n" +
				"[Here|^Xxxxxxxxx.tar]";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternalImageLinks_none() {
		String input, expected, actual;
		input = "http://mw.wiki.org/images/x/x8/XxxxXxxxXxxxxx.zip";
		expected = "[^XxxxXxxxXxxxxx.zip]";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternal_ImageNoSyntax() {
		String input, expected, actual;
		input = "https://mw.wiki.org/index.php/Image:Some_1_file.png\n" + 
				"";
		expected = "[^Some_1_file.png]\n" +
				"";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternal_ImageAlias() {
		String input, expected, actual;
		input = "[We have an alias|https://mw.wiki.org/index.php/Image:Slides_and_more_slides.pptx]\n" + 
				"";
		expected = "[We have an alias|^Slides_and_more_slides.pptx]\n" +
				"";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternal_UriEncodedPlus() {
		String input, expected, actual;
		input = "[https://mw.wiki.org/index.php/Test%2BPlus]\n" + 
				"";
		expected = "[Test Plus]\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertExternal_UriEncodedImages() {
		String input, expected, actual;
		input = "[abc|https://mw.wiki.org/index.php/Image:foobar" +
				"%E5%BC%80.docx" +
				"]\n";
		expected = "[abc|^foobarå¼.docx]\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testConvertExternal_NoCloseBrackets() {
		String input, expected, actual;
		input = "h1. abc deff jig\n" + 
				"https://mw.wiki.org/index.php/Image:abc_def_ghi.pdf\n" + 
				"\n" +
				"*george washington - http://some.url.com/12345.html\n" + 
				""; 
		expected = "h1. abc deff jig\n" + 
			"[^abc_def_ghi.pdf]\n" + 
			"\n" +
			"*george washington - http://some.url.com/12345.html\n" + 
			"";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
