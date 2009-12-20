package com.atlassian.uwc.converters.mindtouch;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class TitleParserTest extends TestCase {
	XmlConverter tester = null;

	Logger log = Logger.getLogger(this.getClass());

	DefaultXmlEvents events = null;

	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
		events.addEvent("content", "com.atlassian.uwc.converters.mindtouch.TitleParser");

	}

	public void testConvert_testname() {
		String input = "<pagedata><content type=\"text/html\" title=\"Foo Bar\"><body>\n" + 
			"<p><strong>Bold</strong></p>\n" + 
			"<p><em>Italics</em></p>\n" + 
			"<p><u>Underline</u></p>\n" + 
			"</body></content></pagedata>";
		String expected = "Foo Bar";
		String actual = parseAndGetTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	private String parseAndGetTitle(String input) {
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getName();
		return actual;
	}
}
