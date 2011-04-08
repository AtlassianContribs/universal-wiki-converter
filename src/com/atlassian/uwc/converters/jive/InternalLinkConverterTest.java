package com.atlassian.uwc.converters.jive;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.jive.filters.TagFilter;
import com.atlassian.uwc.converters.jive.filters.UserContainerFilter;
import com.atlassian.uwc.ui.Page;

public class InternalLinkConverterTest extends TestCase {

	InternalLinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = null;
	protected void setUp() throws Exception {
		tester = new InternalLinkConverter();
		tester.titledata = null;
		PropertyConfigurator.configure("log4j.properties");
		props = new Properties();
		props.put("internaljivedomain", "http://wiki.abc.com");
		props.put("titledata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_titles.txt");
		props.setProperty("spacemap-200-600", "testconf");
		props.setProperty("spacemap-201-14", "testconf2");
		props.setProperty("spacemap-202-14", "testconf3");
		props.setProperty("spacedata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_containerdata.txt");
		tester.setProperties(props);
		HashMap<String, String> keys = new HashMap<String, String>();
		keys.put("200-600", "testconf");
		keys.put("201-14", "testconf2");
		keys.put("202-14", "testconf3");
		SpaceConverter.setSpacekeys(keys );
	}
	
	public void testConvertLink() {
		String input, expected, actual;
		input = "Internal link: <a _jive_internal=\"true\" href=\"/clearspace/docs/DOC-1000\" target=\"_blank\">http://wiki.abc.com/clearspace/docs/DOC-1000</a>\n" + 
				"Internal link 2: <a __default_attr=\"10001\" __jive_macro_name=\"document\" class=\"jive_macro jive_macro_document\" href=\"\" title=\"Testing 123 Title\"></a>\n" + 
				"Internal link 3: <a _jive_internal=\"true\" href=\"/docs/DOC-10002\" target=\"_blank\">http://foobar.com/DOC-10002#Section_</a>\n" + 
				"Internal link 4: <a href=\"http://abc.def.com/cgi-bin/FooBarFOO.pl?foo_bar=1234%1w23%3e89&amp;bar=123&amp;SOMETHIG_N=value\"> Foobar </a>\n" + 
				"<a href=\"http://foobar.com/test/123-456\" id=\"test123-456\"><strong>Test-123456</strong></a>\n" +
				"<a href=\"http://foobar.com/test/123-456\" id=\"test123-456\">Problem <em>here</em> <strong>Test-123456</strong></a>\n" +
				"Internal link 6: <a ___default_attr=\"20001\" jivemacro=\"document\">lalalala</a>\n" +
				"Link to page we've moved 7: <a ___default_attr=\"1101\" jivemacro=\"document\">LinkText</a>\n" +
				"Link to page we've moved 8: <a _jive_internal=\"true\" href=\"/clearspace/docs/DOC-2002\" target=\"_blank\">testing</a>\n" +
				"Link to page we've moved 9: <a __default_attr=\"1101\" __jive_macro_name=\"document\" class=\"jive_macro jive_macro_document\" href=\"\" title=\"Tralala\"></a>\n" +
				"Link to an internal doc, but using a span! <span __jive_macro_name=\"document\" id=\"1101\" title=\"Spans, really?\"/>\n" +
				"\n" + 
				"";
		expected = "Internal link: [http://wiki.abc.com/clearspace/docs/DOC-1000]\n" +
				"Internal link 2: [Testing 123 Title|http://wiki.abc.com/docs/10001]\n" +
				"Internal link 3: [http://foobar.com/DOC-10002#Section_|http://wiki.abc.com/docs/DOC-10002]\n" +
				"Internal link 4: [Foobar|http://abc.def.com/cgi-bin/FooBarFOO.pl?foo_bar=1234%1w23%3e89&amp;bar=123&amp;SOMETHIG_N=value]\n" +
				"[Test-123456|http://foobar.com/test/123-456]\n" +
				"[Problem here Test-123456|http://foobar.com/test/123-456]\n" +
				"Internal link 6: [lalalala|http://wiki.abc.com/docs/20001]\n" +
				"Link to page we've moved 7: [LinkText|testconf:Testing Titles]\n" +
				"Link to page we've moved 8: [testing|testconf2:Testing Titles With Bad ;Characters]\n" + //this should actually be handled by the standard character handling
				"Link to page we've moved 9: [Tralala|testconf:Testing Titles]\n" +
				"Link to an internal doc, but using a span! [Spans, really?|testconf:Testing Titles]\n" +
				"\n";
		actual = tester.convertAll(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLink_blanktarget() {
		String input, expected, actual;
		input = "<a _jive_internal=\"true\"\n" + 
				"target=\"_blank\">http://google.com</a>";
		expected = "[http://google.com]";
		actual = tester.convertAll(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLink_blogpost() {
		String input, expected, actual;
		input = "Link to blogpost: <span __jive_macro_name=\"blogpost\" id=\"3003\" title=\"test\"/>\n" +
		"Link to blogpost: <a __default_attr=\"3003\" __jive_macro_name=\"blog\" class=\"jive_macro jive_macro_blog\" href=\"\" title=\"Testing 123\"></a>\n"+
		"Link to blogpost that wasn't migrated: <a __default_attr=\"4000\" __jive_macro_name=\"blog\" class=\"jive_macro jive_macro_blog\" href=\"\" title=\"Not Migrated\"></a>\n";
		expected = "Link to blogpost: [test|testconf3:/2011/1/31/Foo Bar]\n" +
				"Link to blogpost: [Testing 123|testconf3:/2011/1/31/Foo Bar]\n"+
				"Link to blogpost that wasn't migrated: [Not Migrated|http://wiki.abc.comblog4000]\n"; //we are checking this one to test logging only. This is not really the right link
		actual = tester.convertAll(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLink_Filtered() {
		String input, expected, actual;
		input = "Link to migrated page: <a ___default_attr=\"1101\" jivemacro=\"document\">migrated</a>\n" +
				"Link to not exported page: <a ___default_attr=\"8999\" jivemacro=\"document\">not exported</a>\n" +
				"Link to filtered page (doc in UC): <a ___default_attr=\"9050\" jivemacro=\"document\">filtered doc from User Container</a>\n" +
				"Link to filtered page (by tag): <a ___default_attr=\"9051\" jivemacro=\"document\">filtered doc by tag</a>\n" +
				"";
		expected = "Link to migrated page: [migrated|testconf:Testing Titles]\n" +
				"Link to not exported page: [not exported|http://wiki.abc.com/docs/8999]\n" +
				"Link to filtered page (doc in UC): [filtered doc from User Container|null:Doc in User Container]\n" +
				"Link to filtered page (by tag): [filtered doc by tag|testconf:Filtered By Tag]\n" +
				"";
		
		Page pageWithLinks = new Page(null);
		pageWithLinks.setOriginalText(input);
		pageWithLinks.setConvertedText(input);
		pageWithLinks.setName("Testing 123");
		pageWithLinks.setSpacekey("testconf");
		
		tester.convert(pageWithLinks);
		
		actual = pageWithLinks.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

		//prep system for filtering
		props.setProperty("filterbytag-includeregex", "^migrate(-cleanup)?$");
		UserContainerFilter ucFilter = new UserContainerFilter();
		ucFilter.setProperties(props);
		TagFilter tagFilter = new TagFilter();
		tagFilter.setProperties(props);
		
		File sampleDir = new File("sampleData/jive/junit_resources/filter-sample/600-200");
		sampleDir.listFiles(ucFilter); //runs the filter on all files in the directory
		sampleDir.listFiles(tagFilter); //runs the filter on all files in the directory
		sampleDir = new File("sampleData/jive/junit_resources/filter-sample/2020-100");
		sampleDir.listFiles(ucFilter); //runs the filter on all files in the directory
		sampleDir.listFiles(tagFilter); //runs the filter on all files in the directory
		
		expected = "Link to migrated page: [migrated|testconf:Testing Titles]\n" +
		"Link to not exported page: [not exported|http://wiki.abc.com/docs/8999]\n" +
		"Link to filtered page (doc in UC): [filtered doc from User Container|http://wiki.abc.com/docs/9050]\n" +
		"Link to filtered page (by tag): [filtered doc by tag|http://wiki.abc.com/docs/9051]\n" +
		"";

		pageWithLinks = new Page(null);
		pageWithLinks.setOriginalText(input);
		pageWithLinks.setConvertedText(input);

		tester.convert(pageWithLinks);

		actual = pageWithLinks.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLink_Filtered_JustTags() {
		String input, expected, actual;
		input = "Link to migrated page: <a ___default_attr=\"1101\" jivemacro=\"document\">migrated</a>\n" +
		"Link to not exported page: <a ___default_attr=\"8999\" jivemacro=\"document\">not exported</a>\n" +
		"Link to filtered page (doc in UC): <a ___default_attr=\"9050\" jivemacro=\"document\">filtered doc from User Container</a>\n" +
		"Link to filtered page (by tag): <a ___default_attr=\"9051\" jivemacro=\"document\">filtered doc by tag</a>\n" +
		"";

		//same but don't filter docs by user container
		props.setProperty("internallink-usercontainerfilter", "false");
		props.setProperty("filterbytag-includeregex", "^migrate(-cleanup)?$");
		
		TagFilter tagFilter = new TagFilter();
		tagFilter.setProperties(props);
		
		File sampleDir = new File("sampleData/jive/junit_resources/filter-sample/600-200");
		sampleDir.listFiles(tagFilter); //runs the filter on all files in the directory
		sampleDir = new File("sampleData/jive/junit_resources/filter-sample/2020-100");
		sampleDir.listFiles(tagFilter); //runs the filter on all files in the directory
		
		expected = "Link to migrated page: [migrated|testconf:Testing Titles]\n" +
		"Link to not exported page: [not exported|http://wiki.abc.com/docs/8999]\n" +
		"Link to filtered page (doc in UC): [filtered doc from User Container|null:Doc in User Container]\n" +
		"Link to filtered page (by tag): [filtered doc by tag|http://wiki.abc.com/docs/9051]\n" +
		"";

		Page pageWithLinks = new Page(null);
		pageWithLinks.setName("Testing 123");
		pageWithLinks.setSpacekey("testconf");
		pageWithLinks.setOriginalText(input);
		pageWithLinks.setConvertedText(input);

		tester.convert(pageWithLinks);

		actual = pageWithLinks.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
