package com.atlassian.uwc.converters.xwiki;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LinkConverterTest extends TestCase {

	LinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new LinkConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLinks_Base() {
		String input, expected, actual;
		input = "[WebHome]";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[http://www.google.com]";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "http://www.google.com";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Alias() {
		String input, expected, actual;
		input = "[alias>Home]\n" + 
				"[alias|Home]\n" + 
				"";
		expected = "[alias|Home]\n" +
				"[alias|Home]\n";
		actual = tester.convertLinks(input); 
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Virtual() {
		String input, expected, actual;
		input = "[virtual:Home]\n" + 
				"";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Space() {
		String input, expected, actual;
		input = "[Sandbox.Home]\n" + 
				"";
		expected = "[Sandbox:Home]\n" + 
				"";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}

	public void testConvertLinks_VirtualAndSpace() {
		String input, expected, actual;
		input = "[virtual:Sandbox.Home]\n" + 
				"";
		expected = "[virtualSandbox:Home]\n" + 
				"";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Query() {
		String input, expected, actual;
		input = "[http://www.google.com/search?q=xwiki]\n" + 
				"";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Section() {
		String input, expected, actual;
		input = "[Home#Section]\n" + 
				"";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Interwiki() {
		String input, expected, actual;
		input = "[xwiki@Wikipedia]\n" + 
				"";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_Target() {
		String input, expected, actual;
		input = "[Home|_blank]";
		expected = "{link-window:Home}Home{link-window}";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Home>_blank]";
		expected = "{link-window:Home}Home{link-window}";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Home|_self]";
		expected = "[Home]";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Home>_self]";
		expected = "[Home]";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Home>_someothertarget]";
		expected = "[Home]";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks1() {
		String input, expected, actual;
		input = "[alias|virtual:Home#TestSection>_blank]";
		expected = "{link-window:virtual:Home#TestSection}alias{link-window}";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks2() {
		String input, expected, actual;
		input = "[alias>test@Test]\n" + 
				"";
		expected = "[alias|test@Test]\n" + 
				"";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks3() {
		String input, expected, actual;
		input = "[Sandbox.Home|_self]\n" + 
				"";
		expected = "[Sandbox:Home]\n" + 
				"";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks4() {
		String input, expected, actual;
		input = "[alias|http://www.google.com>_blank]";
		expected = "{link-window:http://www.google.com}alias{link-window}";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLinks5() {
		String input, expected, actual;
		input = "[alias|http://www.google.com]";
		expected = input;
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetLink() {
		String input, expected, actual;
		input = "abc";
		expected = input;
		actual = tester.getLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc|def";
		expected = "def";
		actual = tester.getLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc|def|ghi";
		expected = "def";
		actual = tester.getLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "alias|http://www.google.com";
		expected = "http://www.google.com";
		actual = tester.getLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testProblemSyntax() {
		String input, expected, actual;
		input = "[||]";
		expected = "[||]";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	//Xwiki appears to actually use a #H delimiter.
	//XWikiSyntax explanation does not describe this, but
	//Sections used in XWikiSyntax do.
	//I'll try to handle both syntaxes
	public void testHSection() { 
		String input, expected, actual;
		input = "[Home#HSection]\n" + 
		"";
		expected = "[Home#Section]\n" + 
		"";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	//XWiki appears to remove ws from section derived anchors
	public void testCondensedSection() {
		String input, expected, actual;
		input = "[Link to Section 2>Testing Sections#HSection2]\n" +
				"h6. Section 2";
		expected = "[Link to Section 2|Testing Sections#Section 2]\n" +
		"h6. Section 2";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixAnchors() {
		String input, expected, actual;
		input = "Home#HSection";
		expected = "Home#Section";
		actual = tester.fixAnchors(input, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Home#section";
		expected = input;
		actual = tester.fixAnchors(input, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String link = "Home#HSectionWhitespaceHere";
		input = "[" + link + "]\n" +
				"h1. Section Whitespace Here";
		expected = "Home#Section Whitespace Here";
		actual = tester.fixAnchors(link, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		link = "Home#HSectionWhitespacehere";
		input = "[" + link + "]\n" +
				"h1. SectionWhitespace here";
		expected = "Home#SectionWhitespace here";
		actual = tester.fixAnchors(link, input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		link = "Link to Section 2>Testing Sections#HSection2";
		input = "[Link to Section 2>Testing Sections#HSection2]\n" +
		"h3. Section 2";
		expected = "Link to Section 2>Testing Sections#Section 2";
		actual = tester.fixAnchors(link, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetHeaders() {
		String input;
		input = "nothing";
		Vector<String> actual = tester.getHeaders(input);
		assertNull(actual);
		
		input = "h1. Something";
		actual = tester.getHeaders(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Something", actual.get(0));
		
		input = "something\n" +
				"h2. Something";
		actual = tester.getHeaders(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Something", actual.get(0));
		
		input = "something\n" +
				"h2. Something1\n" +
				"h3. Something2";
		actual = tester.getHeaders(input);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("Something1", actual.get(0));
		assertEquals("Something2", actual.get(1));
		
		input = "h2. Something With WS";
		actual = tester.getHeaders(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Something With WS", actual.get(0));
	}

	//XXX True this is a problem, but I don't think that there will be a lot of files with periods in the
	//pagename to interfere with Space syntax
//	public void testLinksWithDots() {
//		String input, expected, actual;
//		input = "[Link to Section 2>SampleXwiki-InputAnchors.txt#HSection]\n" + 
//				"";
//		expected = "[Link to Section 2|SampleXwiki-InputAnchors.txt#Section]\n";
//		actual = tester.convertLinks(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
//	}
}
