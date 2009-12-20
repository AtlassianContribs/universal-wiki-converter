package com.atlassian.uwc.converters.mindtouch;

import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class TagParserTest extends TestCase {
	XmlConverter tester = null;

	Logger log = Logger.getLogger(this.getClass());

	DefaultXmlEvents events = null;

	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
		events.addEvent("tag", "com.atlassian.uwc.converters.mindtouch.TagParser");
		
		//these aren't used by the TagParser. We include them so that their content is cleaned, but
		//in the conf/converter.mindtouch.properties that will be handled by the CleanParser
		events.addEvent("tags", "com.atlassian.uwc.converters.mindtouch.TagParser");
		events.addEvent("type", "com.atlassian.uwc.converters.mindtouch.TagParser");
		events.addEvent("uri", "com.atlassian.uwc.converters.mindtouch.TagParser");
		events.addEvent("title", "com.atlassian.uwc.converters.mindtouch.TagParser");
	}

	public void testConvert_Tags() {
		String input = "<content>" + 
				"<tags count=\"4\" href=\"http://192.168.2.247/@api/deki/pages/43/tags\"><tag value=\"test\" " +
				"id=\"3\" href=\"http://192.168.2.247/@api/deki/site/tags/3\"><type>text</type><uri>http://" +
				"192.168.2.247/Special:Tags?tag=test</uri><title>test</title></tag><tag value=\"tag with ws\"" +
				" id=\"4\" href=\"http://192.168.2.247/@api/deki/site/tags/4\"><type>text</type><uri>http://" +
				"192.168.2.247/Special:Tags?tag=tag+with+ws</uri><title>tag with ws</title></tag><tag value=\"" +
				"tag,punctuation;\" id=\"5\" href=\"http://192.168.2.247/@api/deki/site/tags/5\"><type>text</t" +
				"ype><uri>http://192.168.2.247/Special:Tags?tag=tag,punctuation;</uri><title>tag,punctuation;<" +
				"/title></tag><tag value=\"CAPS\" id=\"6\" href=\"http://192.168.2.247/@api/deki/site/tags/6\"" +
				"><type>text</type><uri>http://192.168.2.247/Special:Tags?tag=CAPS</uri><title>CAPS</title></t" +
				"ag></tags>" 
				+ "</content>";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		Set<String> actual = page.getLabels();
		assertNotNull(actual);
		assertEquals(4, actual.size());
		for (String label : actual) {
			assertNotNull(label);
			assertTrue(label.equals("tagpunctuation") ||
						label.equals("test") ||
						label.equals("tagwithws") ||
						label.equals("caps"));
		}
		
		String text = page.getConvertedText();
		String expected = "";
		assertNotNull(actual);
		assertEquals(expected, text);
	}

}
