package com.atlassian.uwc.converters.mindtouch;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlConverter;
import com.atlassian.uwc.ui.Page;

public class TableParserTest extends TestCase {

	XmlConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	DefaultXmlEvents events = null;
	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();   
		events.addEvent("table", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("thead", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("tbody", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("th", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.mindtouch.TableParser");
		events.addEvent("caption", "com.atlassian.uwc.converters.mindtouch.TableParser");
		
	}

	public void testConvert() {
		String input = "<content>" +
				"<table cellspacing=\"1\" cellpadding=\"1\" border=\"1\" summary=\"summary text tralala\" " +
				"style=\"width: 100%; table-layout: fixed;\"> <thead> <tr> <th scope=\"col\">Header 1</th> " +
				"<th scope=\"col\">Header 2</th> </tr> </thead> <caption>caption text goes here</caption> " +
				"<tbody> <tr> <td>r1c1</td> <td>r1c2</td> </tr> <tr> <td>r2c1</td> <td>r2c2</td> </tr> " +
				"</tbody>\n" + 
				"</table>" + 
				"</content>";
		String expected = "|| Header 1 || Header 2 ||\n" +
				"| r1c1 | r1c2 |\n" +
				"| r2c1 | r2c2 |\n" +
				"^caption text goes here^";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_TablesBold() {
		events.addEvent("strong", "com.atlassian.uwc.converters.xml.example.BoldParser");
		events.addEvent("em", "com.atlassian.uwc.converters.xml.ItalicParser");
		events.addEvent("span", "com.atlassian.uwc.converters.mindtouch.StyleParser");
		
		String input = "<table cellspacing=\"1\" cellpadding=\"1\" border=\"1\" style=\"width:" +
				" 100%; table-layout: fixed;\"> " +
				"<tbody> " +
					"<tr> " +
						"<td>Testing</td> " +
						"<td>234</td> " +
					"</tr> " +
					"<tr> " +
						"<td><strong>123</strong></td> " +
						"<td><em>234</em></td> " +
					"</tr> " +
					"<tr> " +
						"<td><span style=\"color: rgb(255, 0, 0);\">foo</span></td> " +
						"<td><span style=\"color: rgb(153, 204, 0);\">bar</span></td> " +
					"</tr> " +
				"</tbody>\n" + 
				"</table>";
		String expected = "| Testing | 234 |\n" + 
				"| *123* | _234_ |\n" + 
				"| {color:#ff0000}foo{color} | {color:#99cc00}bar{color} |\n";
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
