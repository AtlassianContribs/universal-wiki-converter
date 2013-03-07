package com.atlassian.uwc.converters.xml;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import biz.artemis.util.FileUtils;

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
	
	public void testConvert_SimpleParser_HandleNewlines() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("i", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("br", "com.atlassian.uwc.converters.xml.BasicParser");
		input = "<outer><b>italics\n" +
				"</b></outer>";
		expected = "*italics* ";
		actual = parse(input);
		assertNotNull(actual);
		
		input = "<outer><b>italics\n" +
				"<i>bah</i></b></outer>";
		expected = "*italics _bah_* ";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		input = "<outer><b>italics\n" +
				"bah blah</b></outer>";
		expected = "*italics bah blah* ";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_BasicParser_Breaks() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("i", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("s", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("u", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("blockquote", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("strong", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("em", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("br", "com.atlassian.uwc.converters.xml.BasicParser");
		input = "<outer><strong style=\"text-decoration: underline; \">Test Quote Header<br/>" +
				" </strong><br/>" +
				"<blockquote>Testing<br/>" +
				"1<br/>" +
				"</blockquote><br/>" +
				"<b><s>2</s><br/></b>" +
				"3<br/>" +
				"</outer>";
		expected = "*Test Quote Header* \n" +
				"{quote}\n" +
				"Testing\n" +
				"1\n" +
				"\n" +
				"{quote}\n" +
				"*-2-* " +
				"3\n" +
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Basic_Quotes() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("i", "com.atlassian.uwc.converters.xml.BasicParser");
		events.addEvent("blockquote", "com.atlassian.uwc.converters.xml.BasicParser");
		input = "<outer><blockquote><b>test</b></blockquote></outer>\n";
		expected = "{quote}\n" +
				"*test*\n" +
				"{quote} ";
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
		expected = "<html>" + 
				"<head>" + 
				"<title></title>" + 
				"</head>" + 
				"<body>\n" + 
				"<span attribute=\"noquotes\">testing123\n<br />" + 
				"</span>" + 
				"</body>" + 
				"</html>" + 
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
				"<html>" + 
				"<head>" + 
				"<title></title>" + 
				"</head>" + 
				"<body>" + 
				"Some mediawiki text: *bold*\n" +
				"testing123\n" +
				"Some html fragments: <b>bold</b>\n" +
				"Some screwed up html: <span att=\"noquotes\">test</span>" + 
				"</body>" + 
				"</html>" + 
				"\n";
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
	
	public void testConvert_LinksWithBreaks() {
		String input, expected, actual;
		events.addEvent("a", "com.atlassian.uwc.converters.xml.LinkParser");
		events.addEvent("br", "com.atlassian.uwc.converters.xml.BasicParser");
		input = "<uwc>" +
				"<a href=\"http://www.google.com/\">http://www.google.com<br/></a>" +
				"<a href=\"http://www.google.com/\"><br/></a>" +
				"<a href=\"http://www.google.com/\">Google</a>" +
				"</uwc>";
		expected = "[http://www.google.com|http://www.google.com/]\n" +
				"[http://www.google.com/]\n" +
				"[Google|http://www.google.com/]";
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
	}
	public void testConvert_SimpleTables2() {
		String input, expected, actual;
		events.addEvent("table", "com.atlassian.uwc.converters.xml.SimpleTableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.xml.SimpleTableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.xml.SimpleTableParser");
//		TODO Not sure why this isn't passing here. Similar regression test seems to behave better.
		input = "<uwc>" +
				"<table> \n" + 
				"<tr><td>r1c1</td><td>r1c2</td></tr>\n" + 
				"<tr><td>r2c1</td><td>r2c2</td></tr>\n" + 
				"</table>\n" + 
				"</uwc>";
		expected = "| r1c1 | r1c2 |\n" +
				"| r2c1 | r2c2 |\n\n";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvert_ContentFormattingTable() {
		String input, expected, actual;
		events.addEvent("table", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		input = "<uwc>" +
			"<table><tr><td>a</td><td>b</td>\n" +
			"<td>c</td>\n" +
			"</tr><tr><td colspan=\"2\">Testing</td></tr>\n" +
			"</table>" + 
			"</uwc>";
		expected = "{table:border=1}\n" +
				"{tr}" +
				"{td}a{td}" +
				"{td}b{td}\n" +
				"{td}c{td}\n" +
				"{tr}" +
				"{tr}" +
				"{td:colspan=2}Testing{td}" +
				"{tr}" +
				"{table}";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_ContentFormattingMacro2() {
		String input, expected, actual;
		events.addEvent("table", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("th", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");

		input = "<uwc>" +
				"<table>\n" + 
				" <tr>\n" + 
				"   <th>Column 1</th>\n" + 
				"   <th>Column 2</th>\n" + 
				"   <th>Column 3</th>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td>A</td>\n" + 
				"   <td colspan=\"2\" align=\"center\">B</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td>C</td>\n" + 
				"   <td>D</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td>E</td>\n" + 
				"   <td colspan=\"2\">F</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td>G</td>\n" + 
				"   <td>H</td>\n" + 
				"   <td>I</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td>J</td>\n" + 
				"   <td>K</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td colspan=\"2\">L</td>\n" + 
				" </tr>\n" + 
				"<tr>\n" + 
				"   <td rowspan=\"2\">M</td>\n" + 
				"   <td>N</td>\n" + 
				"   <td>O</td>\n" + 
				" </tr>\n" + 
				" <tr>\n" + 
				"   <td colspan=\"2\">P</td>\n" + 
				" </tr>\n" + 
				"</table>\n" + 
				"</uwc>";
		expected = "{table:border=1}\n" + 
				" {tr}\n" + 
				"   {th}Column 1{th}\n" + 
				"   {th}Column 2{th}\n" + 
				"   {th}Column 3{th}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td}A{td}\n" + 
				"   {td:colspan=2|align=center}B{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td}C{td}\n" + 
				"   {td}D{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td}E{td}\n" + 
				"   {td:colspan=2}F{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td}G{td}\n" + 
				"   {td}H{td}\n" + 
				"   {td}I{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td}J{td}\n" + 
				"   {td}K{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td:colspan=2}L{td}\n" + 
				" {tr}\n" + 
				"{tr}\n" + 
				"   {td:rowspan=2}M{td}\n" + 
				"   {td}N{td}\n" + 
				"   {td}O{td}\n" + 
				" {tr}\n" + 
				" {tr}\n" + 
				"   {td:colspan=2}P{td}\n" + 
				" {tr}" + 
				"{table}\n" + 
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testConvert_ContentFormattingTable_AndOtherStuff() {
		String input, expected, actual;
		events.addEvent("table", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("tr", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("td", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("th", "com.atlassian.uwc.converters.xml.ContentFormattingTableParser");
		events.addEvent("strong", "com.atlassian.uwc.converters.xml.BasicParser");
		input = "<uwc>" +
				"<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width: 311px;\">\n" + 
				"<tbody>\n" + 
				"<tr>\n" + 
				"<td height=\"20\" width=\"115\">\n" + 
				"<strong style=\"font-size: 10pt; \">\n" + 
				"HEADER_1</strong>\n" + 
				"</td>\n" + 
				"<td width=\"196\">\n" + 
				"<strong style=\"font-size: 10pt; \">\n" + 
				"HEADER_2</strong>\n" + 
				"</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td align=\"right\" height=\"20\">\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"1</span>\n" + 
				"</td>\n" + 
				"<td>\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"foobar</span>\n" + 
				"</td>\n" + 
				"</tr>\n" +
				"<tr>\n" + 
				"<td align=\"right\" height=\"20\">\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"2</span>\n" + 
				"</td>\n" + 
				"<td>\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"words</span>\n" + 
				"</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" +
				"<td align=\"right\" height=\"20\">\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"3</span>\n" + 
				"</td>\n" + 
				"<td>\n" + 
				"<span style=\"font-size: 10pt;\">\n" + 
				"abc</span>\n" + 
				"</td>\n" + 
				"</tr>\n" + 
				"</tbody>\n" + 
				"</table>" + 
				"</uwc>";
		expected = "{table:border=0|cellpadding=0|cellspacing=0|style=width: 311px;}\n" + 
				"{tr}\n" + 
				"{td:height=20|width=115}\n" + 
				"*HEADER_1* " + 
				"{td}\n" + 
				"{td:width=196}\n" + 
				"*HEADER_2* " + 
				"{td}\n" + 
				"{tr}\n" + 
				"{tr}\n" + 
				"{td:align=right|height=20}\n" + 
				"1" + 
				"{td}\n" + 
				"{td}\n" + 
				"foobar" + 
				"{td}\n" + 
				"{tr}\n" + 
				"{tr}\n" + 
				"{td:align=right|height=20}\n" + 
				"2" + 
				"{td}\n" + 
				"{td}\n" + 
				"words" + 
				"{td}\n" + 
				"{tr}\n" + 
				"{tr}\n" + 
				"{td:align=right|height=20}\n" + 
				"3" + 
				"{td}\n" + 
				"{td}\n" + 
				"abc" + 
				"{td}\n" + 
				"{tr}\n" + 
				"{table}" + 
				"";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDefaultConverter_NewlineHandling() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.example.BoldParser");
		
		input = "<uwc>" +
				"[foo bar|foo bar 2]\n" + 
				"\n" + 
				"h1. some heading\n" + 
				"\n" + 
				"xxxxxxxx xxxxxxx xxxx xxxxxx xxx xxxx xxxxxxxxxxx xxxxxx " +
				"xxxxxxxx xxxx xxxxx xx xxxxxxxxxx xxxxxxx xxx xxxxxxxxx xx " +
				"xxxx xxxxxxxxx xx xxxxxxxx xxxxxxxxxxxxx\n" + 
				"\n" + 
				"!test.jpg!\n" + 
				"\n" + 
				"\n" + 
				"xxx xxxx xxxx xxxxxxx xxxx xxxx xx xxx xxxx xxxxxxxxx xxxxx xxx" +
				" xxxxxx xxxx xxxxx xxxxxxxx xxxxxxxxx x xxxx xxxx xxxxxxx xxxx " +
				"xxxx xxxxx xxxxxx xxx xx xxx xxxxxxxx xxxxx xxxxxxxxx xxxxx xx " +
				"xxxxxxxxx xxx xxxxxx xx xxx xxxx x xxxxxxxxxxx xxxxx xxx xxxx xx" +
				" xxxxxx xxxxxxxxxx xxx xxx xxxxxxxxx xx xxx xxxxxxxxxxx xx xxxxx" +
				"xxx xxxx xx <b>xx</b> <b>xx</b> xx xxxxxxxx xxxxxxxxx xxxxxxxx" +
				" xxxx xxxxxxxxxxx xxxx x xxxx xxxxxxx xxxxxx" +
				"</uwc>";
		
		expected = "[foo bar|foo bar 2]\n" + 
		"\n" + 
		"h1. some heading\n" + 
		"\n" + 
		"xxxxxxxx xxxxxxx xxxx xxxxxx xxx xxxx xxxxxxxxxxx xxxxxx " +
		"xxxxxxxx xxxx xxxxx xx xxxxxxxxxx xxxxxxx xxx xxxxxxxxx xx " +
		"xxxx xxxxxxxxx xx xxxxxxxx xxxxxxxxxxxxx\n" + 
		"\n" + 
		"!test.jpg!\n" + 
		"\n" + 
		"\n" + 
		"xxx xxxx xxxx xxxxxxx xxxx xxxx xx xxx xxxx xxxxxxxxx xxxxx xxx" +
		" xxxxxx xxxx xxxxx xxxxxxxx xxxxxxxxx x xxxx xxxx xxxxxxx xxxx " +
		"xxxx xxxxx xxxxxx xxx xx xxx xxxxxxxx xxxxx xxxxxxxxx xxxxx xx " +
		"xxxxxxxxx xxx xxxxxx xx xxx xxxx x xxxxxxxxxxx xxxxx xxx xxxx xx" +
		" xxxxxx xxxxxxxxxx xxx xxx xxxxxxxxx xx xxx xxxxxxxxxxx xx xxxxx" +
		"xxx xxxx xx *xx* *xx* xx xxxxxxxx xxxxxxxxx xxxxxxxx" +
		" xxxx xxxxxxxxxxx xxxx x xxxx xxxxxxx xxxxxx";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDefaultConverter_NewlineHandling_WithTidy() {
		String input, expected, actual;
		events.addEvent("b", "com.atlassian.uwc.converters.xml.example.BoldParser");
		Properties properties = new Properties();
		properties.setProperty("xml-use-htmltidy", "true");
		tester.setProperties(properties);

		
		input = "[foo bar|foo bar 2]\n" + 
				"\n" + 
				"h1. some heading\n" + 
				"\n" + 
				"xxxxxxxx xxxxxxx xxxx xxxxxx xxx xxxx xxxxxxxxxxx xxxxxx " +
				"xxxxxxxx xxxx xxxxx xx xxxxxxxxxx xxxxxxx xxx xxxxxxxxx xx " +
				"xxxx xxxxxxxxx xx xxxxxxxx xxxxxxxxxxxxx\n" + 
				"\n" + 
				"!test.jpg!\n" + 
				"\n" + 
				"\n" + 
				"xxx xxxx xxxx xxxxxxx xxxx xxxx xx xxx xxxx xxxxxxxxx xxxxx xxx" +
				" xxxxxx xxxx xxxxx xxxxxxxx xxxxxxxxx x xxxx xxxx xxxxxxx xxxx " +
				"xxxx xxxxx xxxxxx xxx xx xxx xxxxxxxx xxxxx xxxxxxxxx xxxxx xx " +
				"xxxxxxxxx xxx xxxxxx xx xxx xxxx x xxxxxxxxxxx xxxxx xxx xxxx xx" +
				" xxxxxx xxxxxxxxxx xxx xxx xxxxxxxxx xx xxx xxxxxxxxxxx xx xxxxx" +
				"xxx xxxx xx <b>xx</b> <b>xx</b> xx xxxxxxxx xxxxxxxxx xxxxxxxx" +
				" xxxx xxxxxxxxxxx xxxx x xxxx xxxxxxx xxxxxx";
		
		expected = "[foo bar|foo bar 2]\n" + 
		"\n" + 
		"h1. some heading\n" + 
		"\n" + 
		"xxxxxxxx xxxxxxx xxxx xxxxxx xxx xxxx xxxxxxxxxxx xxxxxx " +
		"xxxxxxxx xxxx xxxxx xx xxxxxxxxxx xxxxxxx xxx xxxxxxxxx xx " +
		"xxxx xxxxxxxxx xx xxxxxxxx xxxxxxxxxxxxx\n" + 
		"\n" + 
		"!test.jpg!\n" + 
		"\n" + 
		"\n" + 
		"xxx xxxx xxxx xxxxxxx xxxx xxxx xx xxx xxxx xxxxxxxxx xxxxx xxx" +
		" xxxxxx xxxx xxxxx xxxxxxxx xxxxxxxxx x xxxx xxxx xxxxxxx xxxx " +
		"xxxx xxxxx xxxxxx xxx xx xxx xxxxxxxx xxxxx xxxxxxxxx xxxxx xx " +
		"xxxxxxxxx xxx xxxxxx xx xxx xxxx x xxxxxxxxxxx xxxxx xxx xxxx xx" +
		" xxxxxx xxxxxxxxxx xxx xxx xxxxxxxxx xx xxx xxxxxxxxxxx xx xxxxx" +
		"xxx xxxx xx *xx* *xx* xx xxxxxxxx xxxxxxxxx xxxxxxxx" +
		" xxxx xxxxxxxxxxx xxxx x xxxx xxxxxxx xxxxxx ";
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		tester.getProperties().clear();
	}
	
	public void testWhistespaceHandlingWithTidy() {
		String input, expected, actual;
		Properties properties = new Properties();
		properties.setProperty("xml-use-htmltidy", "true");
		properties.setProperty("xml-tidyopt-numeric-entities","true");
		properties.setProperty("xml-tidyopt-drop-proprietary-attributes","true");

		tester.setProperties(properties);
		
		input = "[a<sup>b</sup> xml in link aliases|http://abc.com]";
		expected = "[ab xml in link aliases|http://abc.com] ";
		
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().clear();
	}
	public void testWhistespaceHandlingWithTidy2() {
		String input, expected, actual;
		Properties properties = new Properties();
		properties.setProperty("xml-use-htmltidy", "true");
		properties.setProperty("xml-tidyopt-numeric-entities","true");
		properties.setProperty("xml-tidyopt-drop-proprietary-attributes","true");
		tester.setProperties(properties);
		
		input = "h1. Foo Bar\n" +
				"[a<sup>b</sup> xml|http://abc.com]\n";
		expected = "h1. Foo Bar\n" +
				"[ab xml|http://abc.com]\n ";
		
		actual = parse(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		tester.getProperties().clear();
	}
	
	public void testPreserveNL() {
		String input, expected, actual;
		input = "testing\n123";
		expected = "testing~UWCXMLNLTOKEN~123";
		actual = tester.preserveNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}
	
	public void testRemoveNL() {
		String input, expected, actual;
		input = "testing\n123";
		expected = "testing 123";
		actual = tester.removeNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<testing>\n<123>";
		expected = "<testing><123>";
		actual = tester.removeNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<sup>\nmeh</sup>";
		expected = "<sup>meh</sup>";
		actual = tester.removeNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testRevertNL() {
		String input, expected, actual;
		input = "testing~UWCXMLNLTOKEN~123";
		expected = "testing\n123";
		actual = tester.revertNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_Encoding() {
		try {
			byte[] bytesFromFile = FileUtils.getBytesFromFile(new File("sampleData/engine/encoding/mdash.txt"));
			String input = new String(bytesFromFile, "utf-8");
			String expected = input;
			input = "<body><p>" + input + "</p></body>";
			String actual = parse(input);
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			//with html tidy
			Properties properties = new Properties();
			properties.setProperty("xml-use-htmltidy", "true");
			tester.setProperties(properties);
			
			actual = parse(input);
			assertNotNull(actual);
			assertEquals(expected, actual);
			
		} catch (IOException e) {
			fail("Missing file?");
		}
	}


	private String parse(String input) {
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		return actual;
	}
	
	
}
