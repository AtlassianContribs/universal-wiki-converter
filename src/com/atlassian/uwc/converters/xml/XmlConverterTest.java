package com.atlassian.uwc.converters.xml;

import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

/**
 * Test class for the XmlConverter.
 * Primary test class for the UWC Xml Framework.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class XmlConverterTest extends TestCase {

	XmlConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	DefaultXmlEvents events = null;
	protected void setUp() throws Exception {
		tester = new XmlConverter();
		PropertyConfigurator.configure("log4j.properties");
		events = new DefaultXmlEvents();
		events.clearAll();
	}

	public void testConvert_Simple() {
		String input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
				"<test>abcdef</test>\n";
		String expected = "abcdef";
		String actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testConvert_MultTags() {
		String input, expected, actual;
		input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
		"<test>" +
		"<tag1>abcdef\n</tag1>" +
		"<tag2>lalala\n</tag2>" +
		"</test>\n";
		expected = "abcdef\nlalala\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_WithParser() {
		String input, expected, actual;
		events.addEvent("test", "com.atlassian.uwc.converters.xml.example.TestParser");
		input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
		"<test>abcdef</test>\n";
		expected = "{test}abcdef{test}";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Labels() {
		String input, expected, actual;
		events.addEvent("label", "com.atlassian.uwc.converters.xml.example.LabelParser");
		input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
		"<label>abcdef</label>\n";
		expected = "";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		Set<String> labels = page.getLabels();
		assertFalse(labels.isEmpty());
		assertEquals(1, labels.size());
		assertEquals("abcdef", page.getLabelsAsString());
	}

	public void testConvert_Nesting() {
		String input, expected, actual;
		events.addEvent("span", "com.atlassian.uwc.converters.xml.example.TestSpanParser");
		input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + 
		"<test>" +
		"<span att=\"--\">Dashes tralala <span att=\"!\">Exclamation</span> lala</span>" +
		"</test>\n";
		expected = "--Dashes tralala !Exclamation! lala--";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Fragments() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.example.BoldParser");
		input = "Mediawiki has *bold* syntax and allows <b>bold</b> html tags.";
		expected = "Mediawiki has *bold* syntax and allows *bold* html tags.";
		Properties properties = new Properties();
		properties.setProperty("xml-fragments", "true");
		tester.setProperties(properties);
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_SimpleParser() {
		String input, expected, actual;
		events.addEvent("i", "com.atlassian.uwc.converters.xml.ItalicParser");
		input = "<outer><i>italics</i></outer>";
		expected = "_italics_";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Html2Macro() {
		String input, expected, actual;
		events.addEvent("span", "com.atlassian.uwc.converters.xml.MacroParser");
		input = "<span class=\"someclass\" style=\"color:red\">This is a span tag</span>";
		expected = "{span:class=someclass|style=color:red}This is a span tag{span}";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Headers() {
		String input, expected, actual;
		events.addEvent("h1", "com.atlassian.uwc.converters.xml.HeaderParser");
		events.addEvent("h2", "com.atlassian.uwc.converters.xml.HeaderParser");
		input = "<uwc>" +
				"<h1>Header</h1>\n" +
				"Something" +
				"<h2>Header2</h2><h2>abcdef</h2><h2>testing</h2>\n" +
				"</uwc>";
		expected = "h1. Header\n" +
				"Something\n" +
				"h2. Header2\n" +
				"h2. abcdef\n" +
				"h2. testing\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_Lists_Ordered() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		input = "<uwc>" +
				"Ordered List\n" + 
				"<ol>\n" + 
				"<li>item1</li>\n" + 
				"<li>item2</li>\n" + 
				"<ol>\n" + 
				"<li>a</li>\n" + 
				"<li>b</li>\n" + 
				"</ol>\n" + 
				"</ol>\n" + 
				"</uwc>";
		expected = "Ordered List\n" + 
				"# item1\n" + 
				"# item2\n" + 
				"## a\n" + 
				"## b\n" +
				"\n" + 
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_UnOrdered() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		input = "<uwc>" +
				"Unordered List\n" + 
				"<ul>\n" + 
				"<li>item1</li>\n" + 
				"<li>item2</li>\n" + 
				"<ul>\n" + 
				"<li>a</li>\n" + 
				"<li>b</li>\n" + 
				"</ul>\n" + 
				"</ul>" + 
				"</uwc>";
		expected = "Unordered List\n" + 
				"* item1\n" + 
				"* item2\n" + 
				"** a\n" + 
				"** b\n" + 
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_Both() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		input = "<uwc>" +
				"Both\n" + 
				"<ol>\n" + 
				"<li>item1</li>\n" + 
				"<li>item2</li>\n" + 
				"<ul>\n" + 
				"<li>a</li>\n" + 
				"<li>b</li>\n" + 
				"</ul>\n" + 
				"</ol>" + 
				"</uwc>";
		expected = "Both\n" + 
				"# item1\n" + 
				"# item2\n" + 
				"#* a\n" + 
				"#* b\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_NoWs() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		input = "<uwc>" +
				"Both - NO WS <ol> <li>item1</li> <li>item2</li> <ul> <li>a</li> <li>b</li> </ul> </ol>" + 
				"</uwc>";
		expected = "Both - NO WS \n" + 
				"# item1 \n" + 
				"# item2 \n" + 
				"#* a \n" + 
				"#* b  ";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<uwc>" +
				"Both - NO WS\n" + 
				"<ol> <li>item1</li> <li>item2</li> <ul> <li>a</li> <li>b</li> </ul> </ol>" +
				"</uwc>";
		expected = "Both - NO WS\n\n" +
				"# item1 \n" + 
				"# item2 \n" + 
				"#* a \n" + 
				"#* b  ";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_NoNl() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		input = "<uwc>" +
				"<ul>\n" + 
				"<li>level1\n" + 
				"   <ul>\n" + 
				"     <li>level 2</li>\n" + 
				"   </ul>\n" + 
				"</li>\n" + 
				"<li>foobar</li>\n" + 
				"</ul>\n" + 
				"</uwc>";
		expected = "* level1\n" +
				"** level 2\n" +
				"* foobar\n" +
				"\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_Combo() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("a", "com.atlassian.uwc.converters.xml.LinkParser");
		input = "<uwc>" +
				"<ul>\n" + 
				"<li>\n" + 
				"Testing 123\n" + 
				"<a href=\"http://www.google.com/\">http://www.google.com/</a>\n" + 
				"{quote}lorem ipsum{quote}\n" + 
				"\n" + 
				"Abcdef\n" + 
				"<a href=\"http://www.abcdef.com\">http://www.abcdef.com</a>\n" + 
				"{quote}ghijkl{quote}\n" + 
				"</li>\n" + 
				"</ul>" + 
				"</uwc>";
		expected = "* Testing 123\n" +
				"[http://www.google.com/|http://www.google.com/]\n" +
				"{quote}lorem ipsum{quote}\n" +
				"Abcdef\n" +
				"[http://www.abcdef.com|http://www.abcdef.com]\n" +
				"{quote}ghijkl{quote}\n" +
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Lists_Empty() {
		String input, expected, actual;
		events.addEvent("ul", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("ol", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("li", "com.atlassian.uwc.converters.xml.ListParser");
		events.addEvent("a", "com.atlassian.uwc.converters.xml.LinkParser");
		input = "<uwc>" +
				"<ul> <li>  </li>\n" + 
				"</ul>\n" + 
				"</uwc>";
		expected = "";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testStartEndDocument() {
		String input, expected, actual;
		events.addEvent(">doc", "com.atlassian.uwc.converters.xml.BoilerplateParser");
		input = "<uwc>" +
				"Testing 123" +
				"</uwc>";
		expected = "{info}\n" +
				"Every page running the BoilerplateParser will have start and end text like this.\n" +
				"{info}\n" +
				"Testing 123\n" +
				"{tip}\n" +
				"You can set the start and end text as properties in your converter properties file.\n" +
				"Mywiki.1234.boilerplate-start.property=start text\n" +
				"Mywiki.1234.boilerplate-end.property=end text\n" +
				"{tip}\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Properties properties = new Properties();
		properties.setProperty("boilerplate-start", "foo");
		properties.setProperty("boilerplate-end", "bar");
		tester.setProperties(properties);
		
		expected = "foo\n" +
			"Testing 123\n" +
			"bar";
			
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEnforceValidity_Fragments() {
		//fragments
		Properties properties = new Properties();
		properties.setProperty("xml-fragments", "true");
		tester.setProperties(properties);
		
		String input, expected, actual;
		input = "testing123";
		expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<uwc-xml-outer-tag>\n" +
				"testing123\n" +
				"</uwc-xml-outer-tag>";
		actual = tester.enforceValidity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testEnforceValidity_Htmltidy() {
		//htmltidy
		Properties properties = new Properties();
		properties.setProperty("xml-use-htmltidy", "true");
		tester.setProperties(properties);
		
		String input, expected, actual;
		input = "<html><body>\n" +
				"<span attribute=noquotes>" +
				"testing123\n" +
				"<br>" +
				"</span>" +
				"</body></html>";
		expected = "<html>\n" + 
				"<head>\n" + 
				"<title></title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"<span attribute=\"noquotes\">testing123<br />\n" + 
				"</span>\n" + 
				"</body>\n" + 
				"</html>\n" + 
				"\n";
		actual = tester.enforceValidity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testSetRootXmlWithProperty() {
//		fragments
		Properties properties = new Properties();
		properties.setProperty("xml-fragments", "true");
		properties.setProperty("xml-fragments-root", "test123");
		tester.setProperties(properties);
		
		String input, expected, actual;
		input = "testing123";
		expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<test123>\n" +
				"testing123\n" +
				"</test123>";
		actual = tester.enforceValidity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testBothOptions() {
		Properties properties = new Properties();
		properties.setProperty("xml-fragments", "true");
		properties.setProperty("xml-use-htmltidy", "true");
		properties.setProperty("xml-fragments-root", "html");
		tester.setProperties(properties);
		
		String input, expected, actual;
		input = "Some mediawiki text: *bold*\n" +
				"testing123\n" +
				"Some html fragments: <b>bold</b>\n" +
				"Some screwed up html: <span att=noquotes>test</span>";
		expected = 
				"<html>\n" + 
				"<head>\n" + 
				"<title></title>\n" + 
				"</head>\n" + 
				"<body>\n" + 
				"Some mediawiki text: *bold* testing123 Some html fragments:\n" + 
				"<b>bold</b> Some screwed up html: <span att=\"noquotes\">test</span>\n" + 
				"</body>\n" + 
				"</html>\n" + 
				"\n" + 
				"";
		actual = tester.enforceValidity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "This is a sample for testing what happens when the mediawiki text has invalid html in it\n" + 
				"\n" + 
				"This single line entity has no closing forward slash. It needs to be tidied.\n" + 
				"<hr>";
		tester.enforceValidity(input);
	}
	
	public void testConvert_Links() {
		String input, expected, actual;
		events.addEvent("a", "com.atlassian.uwc.converters.xml.LinkParser");
		input = "<uwc>" +
				"<a href=\"http://www.google.com\">Alias</a>" + 
				"</uwc>";
		expected = "[Alias|http://www.google.com]";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_SimpleTables() {
		String input, expected, actual;
		events.addEvent("table", "com.atlassian.uwc.converters.xml.SimpleTableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.xml.SimpleTableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.xml.SimpleTableParser");
		input = "<uwc>" +
				"<table><tr><td>a</td><td>b</td>\n" +
				"<td>c</td>\n" +
				"</tr><tr><td>Testing</td></tr>\n" +
				"</table>" + 
				"</uwc>";
		expected = "| a | b | c |\n" +
				"| Testing | | |\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

//		TODO Not sure why this isn't passing here. Similar regression test seems to behave better.
//		input = "<uwc>" +
//				"<table> \n" + 
//				"<tr><td>r1c1</td><td>r1c2</td></tr>\n" + 
//				"<tr><td>r2c1</td><td>r2c2</td></tr>\n" + 
//				"</table>\n" + 
//				"</uwc>";
//		expected = "| r1c1 | r2c2 |\n" +
//				"| r2c1 | r2c2 |\n";
//		actual = parse(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);

	}
	
	private String parse(String input) {
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		return actual;
	}
	
}
