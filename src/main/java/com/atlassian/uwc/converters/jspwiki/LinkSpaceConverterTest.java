package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class LinkSpaceConverterTest extends TestCase {

	LinkSpaceConverter tester = null;
	
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new LinkSpaceConverter();
	}

	public void testConvertLinkSpaces() {
		String input = "[this is also a link] = " +
				"creates a hyperlink to an internal " +
				"WikiPage called 'ThisIsAlsoALink'.";
		String expected = "[this is also a link|ThisIsAlsoALink] = " +
				"creates a hyperlink to an internal " +
				"WikiPage called 'ThisIsAlsoALink'.";
		String actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinkSpaces2() {
		String input = "[JSPWiki Test Syntax]";
		String expected = "[JSPWiki Test Syntax|JSPWikiTestSyntax]";
		String actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[JSPWikiTestSyntax]";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[JSP Wiki Test Syntax]";
		expected = "[JSP Wiki Test Syntax|JSPWikiTestSyntax]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConvertLinkSpaces3_NoConvert() {
		String input = "[http://www.google.com]";
		String expected = input;
		String actual = tester.convertLinkSpaces(input);
		assertEquals(expected, actual);
		
		input = "[http://localhost:8083/JSPWiki/images/xmlCoffeeCup.png]";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertEquals(expected, actual);
	}
	
	public void testConverLinkSpaces_Dollars() {
		String input, expected, actual;
		input = "[file://myserver/hiddenshare$/folder]";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[file://myserver/hiddenshare$/folder]\n" + 
				"[file://myserver/hiddenshare/folder]\n";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinkSpaces_Question() {
		String input, expected, actual;
		input = "[What is a TagID?|WhatIsATagID?]\n" + 
				"";
		expected = "[What is a TagID?|WhatIsATagID]\n";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinkSpace_NonAttPeriods() {
		String input, expected, actual;
		input = "[Alias?|Upward Mapping vs. Downward Mapping]";
		expected = "[Alias?|UpwardMappingVs.DownwardMapping]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinkSpaces_Parens() { //UWC-357
		String input, expected, actual;
		input = "[Operational Process Engine (Opie)]\n" + 
				"";
		expected = "[Operational Process Engine (Opie)|OperationalProcessEngineOpie]\n";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinksSpace_NotAltImageSyntax() {
		String input, expected, actual;
		input = "[{Image src=\'Wiki.png\' align=\'center\'}]";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[{Image src = \'Wiki.png\' align=\'center\'}]";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertCaps() {
		String input = "a b";
		String expected = "A B";
		String actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "ab";
		expected = "Ab";
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
		input = "abc abc abc";
		expected = "Abc Abc Abc";
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "AB";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "A BB";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Ab AB";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "file://testing123.doc";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "http://testing123";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "https://testing123";
		expected = input;
		actual = tester.convertCaps(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertSpaces() {
		String input = "Ab Cd";
		String expected = "AbCd";
		String actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "ab cd";
		expected = "abcd";
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "AbCd";
		expected = input;
		actual = tester.convertSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontConvertImages() {
		String input = "[imagelink.jpg] and" +
					   "[attachlink.txt]";
		String expected = "[imagelink.jpg] and" +
		   				"[attachlink.txt]";
		String actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertWithAliases() {
		String input, expected, actual;
		input = "[This is my awesome page|Awesome Page]";
		expected = "[This is my awesome page|AwesomePage]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertWithAliases_External() {
		String input, expected, actual;
		input = "[Home | http://wiki.internal.gracenote.com/AETeamWiki/]";
		expected = "[Home|http://wiki.internal.gracenote.com/AETeamWiki/]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertWithQuery() {
		String input, expected, actual;
		input = "[foo|http://foo.com/foo.dll?bar=gah]\n" + 
				"";
		expected = input;
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertAliasWhitespace() {
		String input, expected, actual;
		input = "[TestT | http://testt.x.y.com/test/test.htm]\n" + 
				"[ MyLink ]";
		expected = "[TestT|http://testt.x.y.com/test/test.htm]\n" + 
				"[MyLink]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsImage() {
		String input;
		boolean actual;

		input = "imagelink.jpg";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
		
		input = "http://www.something.com/something.jpg";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
		
		input = "http://www.something.com/some/thing.jpeg";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
		
		input = "http://www.something.com";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertFalse(actual);
		
		input = "http://www.something.com/something";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertFalse(actual);
		
		input = "ed.jpeg";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
		
		input = "http://testt.x.y.com/test/test.htm";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertFalse(actual);
		
		//page with dot (UWC-382)
		input = "cons7.5faq";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
		
		Properties properties = tester.getProperties();
		properties.put(tester.JSPWIKI_EXTS, "gif,jpg,jpeg,bmp,png");
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertFalse(actual);
		
		input = "imagelink.jpg";
		actual = tester.isImage(input);
		assertNotNull(actual);
		assertTrue(actual);
	}
	
	public void testFile() {
		String input, expected, actual;
		input = "[alias| file://testing123.doc]";
		expected = "[alias|file://testing123.doc]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPagesWithSpaces() { //uwc-349
		String input, expected, actual;
		input = "[SampleJspwiki-Input WithSpace]\n" +
				"[a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" + 
				"[SampleJspwiki-InputWithSpace]\n" +
				"[a link to a page|SampleJspwiki-InputWithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-InputWithSpace]\n" +
				"[SampleJspwiki-Input+WithSpace]\n" +
				"[a link to a page|SampleJspwiki-Input+WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input+WithSpace]\n" +
				"[SampleJspwiki-I n p u t 1 9]\n" +
				"[a link to a page|SampleJspwiki-Input 19]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-In Put 19]\n" +
				"[we made this one up|Nonexistant Page]\n" +
				"[SampleJspwiki-Input(WithParens)]";
		//had to do a best guess on page names
		expected = "[SampleJspwiki-Input WithSpace|SampleJspwiki-InputWithSpace]\n" + 
				"[a link to a page|SampleJspwiki-InputWithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-InputWithSpace]\n" + 
				"[SampleJspwiki-InputWithSpace]\n" + 
				"[a link to a page|SampleJspwiki-InputWithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-InputWithSpace]\n" + 
				"[SampleJspwiki-Input+WithSpace]\n" + 
				"[a link to a page|SampleJspwiki-Input+WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input+WithSpace]\n" + 
				"[SampleJspwiki-I n p u t 1 9|SampleJspwiki-INPUT19]\n" + 
				"[a link to a page|SampleJspwiki-Input19]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-InPut19]\n" + 
				"[we made this one up|NonexistantPage]\n" +
				"[SampleJspwiki-Input(WithParens)|SampleJspwiki-InputWithParens]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//with pagedir property, we can examine the actual filenames
		Properties properties = tester.getProperties();
		properties.put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		expected = "[SampleJspwiki-Input WithSpace]\n" +
				"[a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" + 
				"[SampleJspwiki-InputWithSpace|SampleJspwiki-Input WithSpace]\n" +
				"[a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" +
				"[SampleJspwiki-Input+WithSpace|SampleJspwiki-Input WithSpace]\n" +
				"[a link to a page|SampleJspwiki-Input WithSpace]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input WithSpace]\n" +
				"[SampleJspwiki-I n p u t 1 9|SampleJspwiki-Input19]\n" +
				"[a link to a page|SampleJspwiki-Input19]\n" + 
				"[a link to a page with (parens)|SampleJspwiki-Input19]\n" +
				"[we made this one up|NonexistantPage]\n" +
				"[SampleJspwiki-Input(WithParens)|SampleJspwiki-InputWithParens]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testGetPagename() {
		String input, expected, actual, dir;
		input = "SampleJspwiki-I n p u t WithSpace";
		dir = "sampleData/jspwiki/";
		expected = "SampleJspwiki-Input WithSpace";
		actual = tester.getPagename(dir, input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
		
		input = "SampleJspwiki-Input+WithSpace";
		actual = tester.getPagename(dir, input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
		
	}
	
	public void testGetPagedir() {
		String input = "sampleData/jspwiki/";
		tester.getProperties().put(tester.JSPWIKI_PAGEDIR, input);
		assertNotNull(tester.getPageDir());
		
		tester.clearState();
		
		input = "sampleData123456";
		tester.getProperties().put(tester.JSPWIKI_PAGEDIR, input);
		assertNull(tester.getPageDir());
		
		tester.clearState();
		
		input = "sampleData/jspwiki/SampleJspwiki-Input19.txt";
		tester.getProperties().put(tester.JSPWIKI_PAGEDIR, input);
		assertNull(tester.getPageDir());
		
	}
	
	public void testCacheing() { //see JspwikiLinkConverter
		//finds files in the filter directory
		String dir = "sampleData/filter/";
		tester.getProperties().put(tester.JSPWIKI_PAGEDIR, dir);
		String[] actual = tester.getPageFiles(new File(tester.getPageDir()));
		assertNotNull(actual);
		boolean found = false;
		for (String file : actual) {
			if (file.startsWith("SampleFilter")) {
				found = true;
				break;
			}
		}
		assertTrue(found);
		
		//because it's now cached. Shouldn't find files in trac directory
		String otherdir = "sampleData/trac/";
		tester.getProperties().put(tester.JSPWIKI_PAGEDIR, dir);
		actual = tester.getPageFiles(new File(tester.getPageDir()));
		assertNotNull(actual);
		found = false;
		for (String file : actual) {
			if (file.startsWith("SampleTrac")) {
				found = true;
				break;
			}
		}
		assertFalse(found);
	}
	
	public void testHandlesCaseSensitivity() { //UWC-382
		String input, expected, actual;
		input = "[simple|SampleJspwiki-Inputlinkcase]\n" +
				"[CAS and CAE Faq|cons7.5faq]\n" + 
				"[CAS and CAE Faq|Cons7.5faq]\n" + 
				"[CAS and CAE Faq|CoNs7.5fAq]";
		//not trying to fix this without the the pagedir property
		expected = "[simple|SampleJspwiki-Inputlinkcase]\n" +
				"[CAS and CAE Faq|cons7.5faq]\n" + 
				"[CAS and CAE Faq|Cons7.5faq]\n" + 
				"[CAS and CAE Faq|CoNs7.5fAq]";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//with pagedir property, we can examine the actual filenames
		Properties properties = tester.getProperties();
		properties.put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		//Turns out ext prop was the important change for the sample data in UWC-382
		properties.put(tester.JSPWIKI_EXTS, "gif,jpg,jpeg,bmp,png"); 
		expected = "[simple|SampleJspwiki-InputLinkCase]\n" +
				"[CAS and CAE Faq|Cons7.5faq]\n" +
				"[CAS and CAE Faq|Cons7.5faq]\n" +
				"[CAS and CAE Faq|Cons7.5faq]" +
				"";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testHandlesFiles() { //extra edge cases as result of UWC-382
		String input, expected, actual;
		input = "[a link to another page attachment|awikipage/a wiki attachment.xls]\n" +
				"[Cross-Project Calendar | file://Filesrv11\\PUBLIC\\09_Projects\\9.3_Project_Management\\Cross_Product_Calendar\\All_Projects.xls]\n" + 
				"";
		expected = "[a link to another page attachment|awikipage/a wiki attachment.xls]\n" +
				"[Cross-Project Calendar|file://Filesrv11\\PUBLIC\\09_Projects\\9.3_Project_Management\\Cross_Product_Calendar\\All_Projects.xls]\n" + 
				"";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Properties properties = tester.getProperties();
		properties.put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		properties.put(tester.JSPWIKI_EXTS, "xls");
		expected = "[a link to another page attachment|awikipage/a wiki attachment.xls]\n" +
		"[Cross-Project Calendar | file://Filesrv11\\PUBLIC\\09_Projects\\9.3_Project_Management\\Cross_Product_Calendar\\All_Projects.xls]\n" + 
		"";
		actual = tester.convertLinkSpaces(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
}
