package com.atlassian.uwc.converters.sharepoint;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

public class BreakConverterTest extends TestCase {

	BreakConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new BreakConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvertBreaks1() {
		String input = "<html>abc <br/>def ghi</html>";
		String expected = "<html>abc \n" +
				"def ghi</html>";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"...<br/>\n" +
				"\n" +
				"<br/>\n" +
				"\n" +
				"blah\n" +
				"<em>emp</em><br/>" +
				"</html>";
		expected = "<html>\n" +
		"...\n" +
		"\n" +
		"blah\n" +
		"<em>emp</em>\n" +
		"</html>";
		actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>For Link Testing. We're going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\">Test Page 42</a>. <br/></html>";
		expected = "<html>For Link Testing. We're going to link to this from <a href=\"/my%20test%20wiki/Test%20Page%2042.aspx\">Test Page 42</a>. " +
				"\n</html>";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertBreaks2() {
		String input = "<html>\n" + 
				"\n" + 
				"Testing 123<br/>\n" + 
				"<br/>\n" + 
				"<font size=\"5\">\n" + 
				"Links<br/>\n" + 
				"<font size=\"2\">\n" + 
				"<a href=\"http://www.google.com\" title=\"Search Engine Extraordinaire\">\n" + 
				"Google</a>\n" + 
				"<br/>\n" + 
				"<a href=\"#Lorem Ipsum 2\">\n" + 
				"Lorem Ipsum 2 in page</a>\n" + 
				"<br/>\n" + 
				"How are you supposed to link internally? Link UI implies http link?<br/>\n" + 
				"<a href=\"/my%20test%20wiki/Test%20Page%2072.aspx\">\n" + 
				"Test Page 72</a>\n" + 
				"<br/>\n" + 
				"<a href=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\">\n" + 
				"Sharepoint Converter Links</a>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"<font size=\"7\">\n" + 
				"Lorem Ipsum 1</font>\n" + 
				"<br/>\n" + 
				"<br/>\n" + 
				"</font>\n" + 
				"</font>\n" + 
				"</html>";
		String expected = "<html>\n" + 
				"\n" + 
				"Testing 123\n" + 
				"\n" + 
				"<font size=\"5\">\n" + 
				"Links\n" + 
				"<font size=\"2\">\n" + 
				"<a href=\"http://www.google.com\" title=\"Search Engine Extraordinaire\">\n" + 
				"Google</a>\n" + 
				"<a href=\"#Lorem Ipsum 2\">\n" + 
				"Lorem Ipsum 2 in page</a>\n" + 
				"How are you supposed to link internally? Link UI implies http link?\n" + 
				"<a href=\"/my%20test%20wiki/Test%20Page%2072.aspx\">\n" + 
				"Test Page 72</a>\n" + 
				"<a href=\"/my%20test%20wiki/Sharepoint%20Converter%20Links.aspx\">\n" + 
				"Sharepoint Converter Links</a>\n" + 
				"\n" + 
				"<font size=\"7\">\n" + 
				"Lorem Ipsum 1</font>\n" + 
				"\n" + 
				"</font>\n" + 
				"</font>\n" + 
				"</html>";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBreaks3() {
		String input = "Testing 123<br/>\n";
		String expected = "Testing 123\n";
		String actual = tester.convertBreaks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
