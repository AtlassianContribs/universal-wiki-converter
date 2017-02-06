package com.atlassian.uwc.converters.trac;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//at this stage we're done alot of link handling already, so this is really confluence link handling
public class FilenameHierarchyLinkConverterTest extends TestCase {

	FilenameHierarchyLinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		tester =  new FilenameHierarchyLinkConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvertLinks_simple() {
		String input, expected, actual;
		input = "[WikiPage/SubWikiPage]"; 
		expected = "[SubWikiPage]";
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_alias() {
		String input, expected, actual;
		input = "[alias|WikiPage/SubWikiPage]";
		expected = "[alias|SubWikiPage]";
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_sibling() {
		String input, expected, actual;
		input = "[../Sibling]\n" +
				"[alias|../Sibling]";
		expected = "[Sibling]\n" +
				"[alias|Sibling]";
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_notattachment() {
		String input, expected, actual;
		input = "[alias/with dangerous chars|^file/wouldthisevenhappen.pdf]";
		expected = input;
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_notanchor() {
		String input, expected, actual;
		input = "[alias/foobar|#foo/bar]";
		expected = input;
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testConvertLinks_notexternal() {
		String input, expected, actual;
		input = "[alias/foobar|http://lalala.com]";
		expected = input;
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLinks_handleimage() {
		String input, expected, actual;
		input = "!PageTitle/file.png!\n" + 
				"!PageTitle/Subpage/file.png!\n" + 
				"";
		expected = "!PageTitle^file.png!\n" + 
				"!Subpage^file.png!\n" +
				"";
		actual = tester.handleImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertLinks_nothtmlcomment() {
		String input, expected, actual;
		input = "<!--PageTitle/file.png-->\n" + 
				"<!--lalala-->";
		expected = input;
		actual = tester.handleImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
}
