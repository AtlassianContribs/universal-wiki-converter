package com.atlassian.uwc.converters.socialtext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class CombineMultipleWorkspacesConverterTest extends TestCase {

	CombineMultipleWorkspacesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Page page;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new CombineMultipleWorkspacesConverter();
		//create the page like we do in ConverterEngine
		File file = new File("sampleData/socialtext/junit_resources/many2onetest/many2onepage/20100414012345.txt");
		page = new Page(file);
        String pagePath = page.getFile().getPath();
        String path = pagePath.substring(0, pagePath.lastIndexOf(File.separator));
        page.setPath(path);
        page.setName("SampleSocialtext-InputMany2OneLinks"); //this will have been set in the pagename converter
	}

	public void testGetNewTitle() {
		String expected = " - many2onetest";
		String actual = tester.getNewTitle(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertThisSpaceLinks() {
		String input, expected, actual;
		input = "[Sample Socialtext Links]\n" + 
				"[Alias for this page|Sample Socialtext Links]"; //by the time this is run, they're already confluence links
		expected = "[Sample Socialtext Links - many2onetest]\n" + 
				"[Alias for this page|Sample Socialtext Links - many2onetest]";
		actual = tester.convertThisSpaceLinks(input, " - many2onetest");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertOtherSpaceLinks() {
		String input, expected, actual;
		input = "[Alias Tada|space1:Some Pagename]\n" + 
				"[Alias Tada|space1:Some Pagename]";
		expected = "[Alias Tada|Some Pagename - space1]\n" + 
				"[Alias Tada|Some Pagename - space1]";
		actual = tester.convertOtherSpaceLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertAnchorsSectionsHeaders() {
		String input, expected, actual;
		input = "[#anchor]\n" + 
				"[#Header]\n" + 
				"[Other Page#section]\n" + 
				"[space2:Other Page#anchor]\n" + 
				"[Alias - Header|#Header]\n" + 
				"[Foo Bar|Something something something something#Something Something]\n" + 
				"";
		expected = "[#anchor]\n" + 
				"[#Header]\n" + 
				"[Other Page - many2onetest#section]\n" + 
				"[Other Page - space2#anchor]\n" + 
				"[Alias - Header|#Header]\n" + 
				"[Foo Bar|Something something something something - many2onetest#Something Something]\n" + 
				"";
		actual = tester.convertThisSpaceLinks(input, " - many2onetest");
		actual = tester.convertOtherSpaceLinks(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testExemptionProp() {
		tester.getProperties().setProperty("many2one-exemption", 
				"(What's the funny punctuation\\?)|(What if I make a mistake\\?)");
		//page titles
		assertFalse(tester.isExempt(page));
		page.setName("What if I make a mistake?");
		assertTrue(tester.isExempt(page));
		page.setName("What's the funny punctuation?");
		assertTrue(tester.isExempt(page));
		//links
		String input = "[Sample Socialtext Links]\n" +
				"[What if I make a mistake?]\n" +
				"[space2:Other Page#anchor]\n" +
				"[space2:What's the funny punctuation?#anchor]\n" +
				"!What if I make a mistake?^abc.gif!\n" +
				"[space2:What if I make a mistake?^abc.gif]\n";
		String expected = "[Sample Socialtext Links - many2onetest]\n" +
				"[What if I make a mistake?]\n" +
				"[Other Page - space2#anchor]\n" +
				"[What's the funny punctuation?#anchor]\n" +
				"!What if I make a mistake?^abc.gif!\n" +
				"[What if I make a mistake?^abc.gif]\n";
		String actual = tester.convertThisSpaceLinks(input, " - many2onetest");
		actual = tester.convertOtherSpaceLinks(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testDelimProp() {
		tester.getProperties().setProperty("many2one-delimiter", "--");
		String expected = " -- many2onetest";
		String actual = tester.getNewTitle(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("many2one-delimiter", "");
		expected = " many2onetest";
		actual = tester.getNewTitle(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testImages() {
		String input, expected, actual;
		input = "!abc.gif!\n" +
				"!pagename^cow.jpg!\n" + 
				"!space2:pagename^cow.jpg!\n" + 
				"[^cow.jpg]\n" + 
				"[pagename^cow.jpg]\n" + 
				"[space2:pagename^cow.jpg]\n" + 
				"[Alias|space2:pagename^double facepalm.jpg]\n" + 
				"!space2:pagename^double facepalm.jpg|width=20%!\n" + 
				"\n" + 
				"*External*\n" + 
				"!http://localhost:8082/download/attachments/426023/double+facepalm.jpg!\n" + 
				"";
		expected = "!abc.gif!\n" +
				"!pagename - many2onetest^cow.jpg!\n" +
				"!pagename - space2^cow.jpg!\n" + 
				"[^cow.jpg]\n" + 
				"[pagename - many2onetest^cow.jpg]\n" + 
				"[pagename - space2^cow.jpg]\n" + 
				"[Alias|pagename - space2^double facepalm.jpg]\n" + 
				"!pagename - space2^double facepalm.jpg|width=20%!\n" + 
				"\n" + 
				"*External*\n" + 
				"!http://localhost:8082/download/attachments/426023/double+facepalm.jpg!\n" + 
				"";
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMacros() { //macros are handled after this, so we need socialtext syntax
		String input, expected, actual;
		//include
		input = "{include: [Pagename]}\n" + 
				"\n" + 
				"{include: workspace [Pagename]}\n" + 
				"\n" + 
				"{include: [Foo:Bar]}\n" + 
				"\n" + 
				"Illegal Pagename Characters:\n" + 
				"[Foo/Bar]\n" + 
				"{include: [Foo/Bar]}\n" + 
				"";
		expected = "{include: [Pagename - abc]}\n" + 
				"\n" + 
				"{include: [Pagename - workspace]}\n" + 
				"\n" + 
				"{include: [Foo:Bar - abc]}\n" + 
				"\n" + 
				"Illegal Pagename Characters:\n" + 
				"[Foo/Bar]\n" + 
				"{include: [Foo/Bar - abc]}\n" + 
				"";
		actual = tester.convertMacros(input, " - abc");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//recent changes
		
		input = "{recent_changes: }\n" + 
				"\n" + 
				"{recent_changes: workspace}\n" + 
				"";
		expected = "{recent_changes: }\n" + 
				"\n" + 
				"{recent_changes: }\n" + 
				"";
		actual = tester.convertMacros(input, " -- def");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNoChangeExternalLinks() {
		String input, expected, actual;
		input = "http://www.google.com\n" + 
				"https://mail.google.com\n" + 
				"laura.kolker@gmail.com\n" + 
				"[Google|http://www.google.com]\n" + 
				"[Gmail|https://mail.google.com]\n" + 
				"[Email|mailto:laura.kolker@gmail.com]\n" + 
				"[# Foo Bar|http://www.something123.com/]"; 
		expected = input;
		
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIncludeProperty() {
		tester.getProperties().setProperty("many2one-exemption", "^A");
		tester.getProperties().setProperty("many2one-include", "^B");

		String testfile = "sampleData/socialtext/junit_resources/many2onetest/exemptiontest/20100414123456.txt";
		Page pageA = new Page(new File(testfile));
		Page pageB = new Page(new File(testfile));
		Page pageC = new Page(new File(testfile));
		pageA.setName("Abc");
		pageB.setName("Bar");
		pageC.setName("Foo");
		pageA.setPath(getPath(pageA));
		pageB.setPath(getPath(pageB));
		pageC.setPath(getPath(pageC));

		String input, expected, actual;
		input = "http://www.google.com\n" + 
		"https://mail.google.com\n" + 
		"laura.kolker@gmail.com\n" + 
		"[Google|http://www.google.com]\n" + 
		"[Gmail|https://mail.google.com]\n" + 
		"[Email|mailto:laura.kolker@gmail.com]\n" + 
		"[# Foo Bar|http://www.something123.com/]"; 
		expected = input;
		pageA.setOriginalText(input);
		pageB.setOriginalText(input);
		pageC.setOriginalText(input);

		
		tester.convert(pageA);
		tester.convert(pageB);
		tester.convert(pageC);
		assertEquals("Abc", pageA.getName());
		assertEquals("Bar - many2onetest", pageB.getName());
		assertEquals("Foo", pageC.getName());//if you're not included, you're exempted;
		assertEquals(expected, pageA.getConvertedText());
		assertEquals(expected, pageB.getConvertedText());
		assertEquals(expected, pageC.getConvertedText());
	}
	
	private String getPath(Page input) {
		String pagePath = input.getFile().getPath();
        String path = pagePath.substring(0, pagePath.lastIndexOf(File.separator));
		return path;
	}
}
