package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AttachmentSpaceConverterTest extends TestCase {

	AttachmentSpaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new AttachmentSpaceConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String input, expected, actual;
		input = "[This is a foo bar|foo bar.pdf]";
		expected = "[This is a foo bar|foo+bar.pdf]";
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_Mult() {
		String input, expected, actual;
		input = "Testing 123\n" +
				"[something wicked.doc]\n" +
				"Foo bar\n" +
				"[this|way comes.pdf]\n";
		expected = "Testing 123\n" +
				"[something+wicked.doc]\n" +
				"Foo bar\n" +
				"[this|way+comes.pdf]\n";;
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDollars() {
		String input, expected, actual;
		input = "[file://myserver/hiddenshare$/folder]";
		expected = "[file://myserver/hiddenshare$/folder]";
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFile() {
		String input, expected, actual;
		input = "[alias| file:\\\\testing123.doc]";
		expected = "[alias|file:\\\\testing123.doc]";
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPagenameWithWS() {
		String input, expected, actual;
		tester.getProperties().put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		input = "* [a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"* [SampleJspwiki-Input WithSpace]\n" + 
				"* [a link to a page with (parens)|nospacepage]\n" + 
				"* [a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" +
				"* [a link to an attachment|Testing 123.jpg]\n" +
				"" ; 
		expected = "* [a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"* [SampleJspwiki-Input WithSpace]\n" + 
				"* [a link to a page with (parens)|nospacepage]\n" + 
				"* [a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" +
				"* [a link to an attachment|Testing+123.jpg]\n";
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPagenameWithDot() {
		String input, expected, actual;
		tester.getProperties().put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		input = "* [a.link.to.a.page|SampleJspwiki-Input.WithDots]\n" + 
				"* [a.link.to.a.page|a.page]";
		expected = input;
		actual = tester.convertAttachmentSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
