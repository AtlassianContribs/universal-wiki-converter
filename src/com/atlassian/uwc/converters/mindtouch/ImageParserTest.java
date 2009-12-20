package com.atlassian.uwc.converters.mindtouch;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class ImageParserTest extends TestCase {
	XmlConverter tester = null;

	Logger log = Logger.getLogger(this.getClass());

	DefaultXmlEvents events = null;

	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
		events.addEvent("img", "com.atlassian.uwc.converters.mindtouch.ImageParser");

	}

	public void testConvert_ImageSyntax() {
		String input = "<content>" + "" +
				"<img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg\" />" + 
				"</content>";
		String expected = "!cow.jpg!";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_NonMTImage() {
		String input = "<content>" + "" +
				"<img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://localhost:8082/images/logo/confluence_48_white.png\" />" + 
				"</content>";
		String expected = "!http://localhost:8082/images/logo/confluence_48_white.png!";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_ImageSyntaxSameParent() {
		String input = "<content>" + "" +
				"<img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg?parent=Test Attachments\" />" + 
				"</content>";
		String expected = "!cow.jpg!";
		String actual = parse(input, "Test Attachments");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_ImageSyntaxDifferentParent() {
		String input = "<content>" + "" +
				"<img alt=\"cow.jpg\" class=\"internal default\" " +
				"src=\"http://192.168.2.247/@api/deki/files/1/=cow.jpg?parent=Some Other Page\" />" + 
				"</content>";
		String expected = "!Some Other Page^cow.jpg!";
		String actual = parse(input);
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
