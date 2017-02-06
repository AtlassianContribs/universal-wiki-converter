package com.atlassian.uwc.converters.pmwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class PmWikiLinkAdjusterTest extends TestCase {

	PmWikiLinkAdjuster tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PmWikiLinkAdjuster();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String input, actual, expected;
		input = "Feel free to use this page to experiment with the [_UWC_LINK_START_PmWiki.Text Formatting Rules_UWC_LINK_END_].  Just click the \"Edit Page\" link at the bottom of the page.\n" + 
				"----\n" + 
				"\n" + 
				"Testing 123 --LK\n" + 
				"\n" + 
				"Groups\n" + 
				"\n" + 
				"[_UWC_LINK_START_OtherGroup._UWC_LINK_END_] -- other groups are like Confluence spaces\n" + 
				"\n" + 
				"[_UWC_LINK_START_OtherGroup-_UWC_LINK_END_]\n" + 
				"\n" + 
				"[_UWC_LINK_START_OtherGroup.Sandbox_UWC_LINK_END_] -- a sandbox in the other group\n" + 
				"\n" + 
				"Attachments\n" + 
				"\n" + 
				"[_UWC_LINK_START_Testing Attachments_UWC_LINK_END_]\n" + 
				"";
		Page page = new Page(new File("sampleData/pmwiki/junit_resources/Main/WikiSandbox"));
		page.setName(page.getFile().getName());
		page.setPath(page.getFile().getPath());
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		expected = "Feel free to use this page to experiment with the [Text Formatting Rules|PmWiki:TextFormattingRules].  " +
				"Just click the \"Edit Page\" link at the bottom of the page.\n" + 
				"----\n" + 
				"\n" + 
				"Testing 123 --LK\n" + 
				"\n" + 
				"Groups\n" + 
				"\n" + 
				"[OtherGroup:] -- other groups are like Confluence spaces\n" + 
				"\n" + 
				"[OtherGroup:]\n" + 
				"\n" + 
				"[OtherGroup:Sandbox] -- a sandbox in the other group\n" + 
				"\n" + 
				"Attachments\n" + 
				"\n" + 
				"[Testing Attachments|TestingAttachments]\n" + 
				""; 
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "This is a page in a non Main group\n" + 
				"\n" + 
				"These are the same:\n" + 
				"* [_UWC_LINK_START_Another Page_UWC_LINK_END_]\n" + 
				"* [_UWC_LINK_START_OtherGroup.Another Page_UWC_LINK_END_]\n" + 
				"";
		expected = "This is a page in a non Main group\n" + 
				"\n" + 
				"These are the same:\n" + 
				"* [Another Page|AnotherPage]\n" + 
				"* [Another Page|OtherGroup:AnotherPage]\n";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
