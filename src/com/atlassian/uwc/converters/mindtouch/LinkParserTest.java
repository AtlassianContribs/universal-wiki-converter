package com.atlassian.uwc.converters.mindtouch;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class LinkParserTest extends TestCase {

	XmlConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	DefaultXmlEvents events = null;
	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();   
		events.addEvent("a", "com.atlassian.uwc.converters.mindtouch.LinkParser");
	}

	public void testConvert_Internal() {
		String input = "<content><a rel=\"internal\" href=\"http://192.168.2.247/Sandbox\">Sandbox</a>\n" + 
				"</content>";
		String expected = "[Sandbox]\n";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_External() {
		String input = "<content>" +
				"<a class=\" external\" title=\"http://google.com/\" rel=\"external nofollow\" href=\"http://google.com\" target=\"_blank\">http://google.com</a>\n" + 
				"</content>";
		String expected = "[http://google.com]\n";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Alias() {
		Properties props = new Properties();
		props.setProperty("exportdir", "sampleData/mindtouch/junit_resources/links");
		tester.setProperties(props);
		
		
		String input = "<content><p>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/Sandbox/Test_Comments\">Alias to Comments Page</a>" +
				"\n<a title=\"http://abc.com/\" class=\" external\" rel=\"external nofollow\" href=\"http://abc.com\" target=\"_blank\">Alias to external link</a>" +
				"</p></content>";
		String expected = "[Alias to Comments Page|Test Comments]\n" +
				"[Alias to external link|http://abc.com]";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Transformedchars() { //ws and urlencoding
		Properties props = new Properties();
		props.setProperty("exportdir", "sampleData/mindtouch/junit_resources/links");
		tester.setProperties(props);
		
		
		String input = "<content><p>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/Sandbox/Test_Comments\">Alias to Comments Page</a>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/Sandbox/Test_XYZ%3b_Bad_Chars\">Test XYZ; Bad Chars</a>" +
				"</p></content>";
		String expected = "[Alias to Comments Page|Test Comments]" +
				"[Test XYZ; Bad Chars|Test XYZ%3b Bad Chars]"; //the url encode will be permanently fixed by the illegal page handler
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Transformedchars_IgnoringMindtouch() { //ws and urlencoding
		Properties props = new Properties();
		props.setProperty("exportdir", "sampleData/mindtouch/junit_resources/links2");
		tester.setProperties(props);
		
		
		String input = "<content><p>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/Sandbox/Test_Comments\">Alias to Comments Page</a>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/Sandbox/Test_XYZ%3b_Bad_Chars\">Test XYZ; Bad Chars</a>" +
				"</p></content>";
		String expected = "[Alias to Comments Page|Test Comments]" +
				"[Test XYZ; Bad Chars|Test XYZ%3b Bad Chars]"; //the url encode will be permanently fixed by the illegal page handler
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_AttachmentLink() {
		String input = "<content>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt\" " +
				"class=\"iconitext-16 ext-txt \">abc.txt</a>" +
				"</content>";
		String expected = "[^abc.txt]";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_AttachmentLinkSameParent() {
		String input = "<content>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt?parent=Test Attachments\" " +
				"class=\"iconitext-16 ext-txt \">abc.txt</a>" +
				"</content>";
		String expected = "[^abc.txt]";
		String actual = parse(input, "Test Attachments");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_AttachmentLinkOtherParent() {
		String input = "<content>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt?parent=Some Other Page\" " +
				"class=\"iconitext-16 ext-txt \">abc.txt</a>" +
				"</content>";
		String expected = "[Some Other Page^abc.txt]";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_AttachmentLinkOtherParent_Alias() {
		String input = "<content>" +
				"<a rel=\"internal\" href=\"http://192.168.2.247/@api/deki/files/3/=abc.txt?parent=Some Other Page\" " +
				"class=\"iconitext-16 ext-txt \">Alias</a>" +
				"</content>";
		String expected = "[Alias|Some Other Page^abc.txt]";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsImage() {
		String input, expected, actual;
		LinkParser parser = new LinkParser();
		input = "http://192.168.2.247/@api/deki/files/3/=abc.txt";
		assertTrue(parser.isImage(input));
		
		input = "http://192.168.2.247/Sandbox";
		assertFalse(parser.isImage(input));
	}

	public void testGetImageTarget() {
		String input, expected, actual;
		LinkParser parser = new LinkParser();
		input = "http://192.168.2.247/@api/deki/files/3/=abc.txt";
		expected = "abc.txt";
		actual = parser.getImageTarget(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixUnderscore() {
		String input, href, expected, actual;
		LinkParser parser = new LinkParser();
		Page page = new Page(null);

		//no exportdir
		input = "Test_Comments";
		href = "http://192.168.2.247/Sandbox/Test_Comments";
		expected = "Test_Comments";
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Properties props = new Properties();
		props.setProperty("exportdir", "sampleData/mindtouch/junit_resources/links");
		parser.setProperties(props);
		
		input = "Sandbox";
		href = "http://192.168.2.247/Sandbox";
		expected = "Sandbox";
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Test_Comments";
		href = "http://192.168.2.247/Sandbox/Test_Comments";
		expected = "Test Comments";
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Test_Z_underscores";
		href = "http://192.168.2.247/Sandbox/Test_Z_underscores";
		expected = "Test Z_underscores";
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "foo_bar"; //doesn't exist
		href = "http://192.168.2.247/Sandbox/foo_bar";
		expected = input;
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "Test_XYZ%3b_Bad_Chars";//bad chars
		href = "http://192.168.2.247/Sandbox/Test_XYZ%3b_Bad_Chars";
		expected = "Test XYZ%3b Bad Chars"; //fixed by illegal page handling framework
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//bad exportdir
		props.setProperty("exportdir", "sampleData/mindtouch/junit_resources/linkity");
		parser.setProperties(props);
		input = "Test_Comments";
		href = "http://192.168.2.247/Sandbox/Test_Comments";
		expected = "Test_Comments";
		actual = parser.fixUnderscores(input, href, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testFixUnderscores_StringFile() {
		String input, expected, actual;
		LinkParser parser = new LinkParser();
		File file = new File("sampleData/mindtouch/junit_resources/links/21_Mindtouch_subpages/" +
				"39_Sandbox_subpages/42_TestComments.xml");
		input = "Test_Comments";
		expected = "Test Comments";
		actual = parser.fixUnderscores(input, file);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		file = new File("sampleData/mindtouch/junit_resources/links/21_Mindtouch_subpages/" +
				"39_Sandbox_subpages/54_TestZ_underscores.xml");
		input = "Test_Z_underscores";
		expected = "Test Z_underscores";
		actual = parser.fixUnderscores(input, file);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		file = new File("sampleData/mindtouch/junit_resources/links/21_Mindtouch_subpages/" +
				"39_Sandbox_subpages/49_TestXYZBadChars.xml");
		input = "Test_XYZ%3b_Bad_Chars";
		expected = "Test XYZ%3b Bad Chars";
		actual = parser.fixUnderscores(input, file);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	private String parse(String input) {
		return parse(input, "");
	}
	private String parse(String input, String pagename) {
		Page page = new Page(null);
		page.setName(pagename);
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		return actual;
	}

}
