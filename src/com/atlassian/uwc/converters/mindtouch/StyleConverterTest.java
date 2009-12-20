package com.atlassian.uwc.converters.mindtouch;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class StyleConverterTest extends TestCase {

	XmlConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	DefaultXmlEvents events = null;
	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();   
		events.addEvent("span", "com.atlassian.uwc.converters.mindtouch.StyleParser");
		
	}

	public void testConvert_Colorrgb() {
		String input = "<content>" +
				"<p><span style=\"color: rgb(51, 102, 255);\">Color Blue</span></p>" + 
				"</content>";
		String expected = "{color:#3366ff}Color Blue{color}";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Colorbasic() {
		String input = "<content>" +
				"<p><span style=\"color: red;\">Color</span></p>" + 
				"</content>";
		String expected = "{color:red}Color{color}";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_Colorhex() {
		String input = "<content>" +
				"<p><span style=\"color:#ffff66;\">foobar</span></p>" + 
				"</content>";
		String expected = "{color:#ffff66}foobar{color}";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_otherstyles() {
		String input = "<content>" +
				"<p><span style=\"color:#ffff66;border=1px solid grey\" alt=\"tada\">foobar</span></p>" + 
				"</content>";
		String expected = "{color:#ffff66}foobar{color}";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_nocolor() {
		String input = "<content>" +
				"<p><span alt=\"bah\">foobar</span></p>" + 
				"</content>";
		String expected = "foobar";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	private String parse(String input) {
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		return actual;
	}

}
