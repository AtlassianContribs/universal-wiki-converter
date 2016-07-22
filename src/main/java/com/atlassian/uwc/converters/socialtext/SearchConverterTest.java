package com.atlassian.uwc.converters.socialtext;

import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class SearchConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	SearchConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new SearchConverter();
		
		Properties props = new Properties();
		props.setProperty("taglist-to-contentbylabel", "true");
		tester.setProperties(props);
		
	}
	
	public void testConvertSearch_keyword() { 
		String input, expected, actual;
		input = "*search with a keyword*\n" + 
				"{search: keyword}";
		expected = "*search with a keyword*\n" + 
				"{search: keyword}";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSearch_tags() {
		String input, expected, actual;
		input = "*search with a tag*\n" + 
				"{search: tag: category}\n" + 
				"{search: tag:loremipsum}\n" + 
				"{search: tag:\"something\"}\n" + 
				"";
		expected = "*search with a tag*\n" + 
				"{contentbylabel:labels=category}\n" + 
				"{contentbylabel:labels=loremipsum}\n" + 
				"{contentbylabel:labels=\"something\"}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSearch_and() {
		String input, expected, actual;
		input = "*search with booleans and tags*\n" + 
				"{search: tag: foo AND tag: bar}\n" + 
				"{search: tag: abc and tag: def}";
		expected = "*search with booleans and tags*\n" + 
				"{contentbylabel:labels=+foo,+bar}\n" + 
				"{contentbylabel:labels=+abc,+def}";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSearch_or() {
		String input, expected, actual;
		input = "{search: tag: abc OR tag: def}";
		expected = "{contentbylabel:labels=abc,def}";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertSearch_params() {
		String input, expected, actual;
		
		Properties props = new Properties();
		props.put("search-spaces","@self");
		tester.setProperties(props);
		input = "{search: tag: category}\n";
		expected = "{contentbylabel:labels=category|spaces=@self}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		props.clear();
		props.put("search-title","true");
		props.put("search-spaces","@self");
		props.put("search-sort","creation");
		props.put("search-reverse","true");
		
		input = "*search with a tag*\n" + 
				"{search: tag: category}\n" + 
				"\n" + 
				"*search with booleans and tags*\n" + 
				"{search: tag: foo AND tag: bar}\n" + 
				"";
		expected = "*search with a tag*\n" + 
				"{contentbylabel:labels=category|title=Category|reverse=true|spaces=@self|sort=creation}\n" + 
				"\n" + 
				"*search with booleans and tags*\n" + 
				"{contentbylabel:labels=+foo,+bar|title=Foo and Bar|reverse=true|spaces=@self|sort=creation}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testGetOptions() {
		HashMap<String, String> actual = tester.getOptions();
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		
		Properties props = new Properties();
		props.put("search-spaces","@self");
		tester.setProperties(props);
		actual = tester.getOptions();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("@self", actual.get("spaces"));
		
		props.clear();
		props.put("search-title","true");
		props.put("search-spaces","@self");
		props.put("search-sort","creation");
		props.put("search-reverse","true");
		
		actual = tester.getOptions();
		assertNotNull(actual);
		assertEquals(4, actual.size());
		assertEquals("true", actual.get("title"));
		assertEquals("@self", actual.get("spaces"));
		assertEquals("creation", actual.get("sort"));
		assertEquals("true", actual.get("reverse"));
		

	}
	public void testHasMultipleTags() {
		String input;
		boolean expected, actual;
		input = " category";
		expected = false;
		actual = tester.hasMultipleTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "loremipsum";
		expected = false;
		actual = tester.hasMultipleTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = " foo AND tag: bar";
		expected = true;
		actual = tester.hasMultipleTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = " abc and tag: def";
		expected = true;
		actual = tester.hasMultipleTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = " abc OR tag: def";
		expected = true;
		actual = tester.hasMultipleTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testBuildMultipleLabelsString() {
		String input, expected, actual;
		input = " foo AND tag: bar";
		expected = "+foo,+bar";
		actual = tester.buildMultipleLabelsString(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = " abc and tag: def";
		expected = "+abc,+def";
		actual = tester.buildMultipleLabelsString(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = " abc OR tag: def";
		expected = "abc,def";
		actual = tester.buildMultipleLabelsString(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	public void testBuildParams() {
		String input, expected, actual;
		HashMap<String,String> options = new HashMap<String, String>();
		input = "abc";
		expected = "";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		options.put("title", "true");
		expected = "|title=Abc";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "+abc,+def";
		expected = "|title=Abc and Def";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc,def";
		expected = "|title=Abc or Def";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc";
		options.put("spaces", "@self");
		expected = "|title=Abc|spaces=@self";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		options.put("spaces", "@all");
		expected = "|title=Abc|spaces=@all";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		options.put("sort", "creation");
		expected = "|title=Abc|spaces=@all|sort=creation";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		options.put("reverse", "true");
		expected = "|title=Abc|reverse=true|spaces=@all|sort=creation";
		actual = tester.buildParams(input, options);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
	}
	
	public void testConvertSearch_badchars() {
		String input, expected, actual;
		input = "{search: tag:no space}\n" + 
				"";
		expected = "{contentbylabel:labels=nospace}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with params
		Properties props = new Properties();
		props.put("label-trans-1", " =-");
		props.put("label-trans-2", "@=at");
		tester.setProperties(props);
		tester.clearOptions();
		
		expected = "{contentbylabel:labels=no-space}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//and mult tags
		tester.clearOptions();
		input = "{search: tag: foo@! AND tag:lor:em ipsum}\n";
		expected = "{contentbylabel:labels=+fooat,+loremipsum}\n";
		actual = tester.convertSearch(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert_Taglist() {
		Properties props = new Properties();
		props.setProperty("taglist-to-contentbylabel", "true");
		tester.setProperties(props);
		
		String input, expected, actual;
		input = "{tag_list: test}\n" + 
				"\n" + 
				"What if the label has illegalchars?\n" + 
				"{tag_list: abc!#&()*,.:;<>?@[]^def}\n" + 
				"{tag_list: has white space}\n" + 
				"(These chars are ok: but what about braces?)\n" + 
				"{tag_list: abc$%-_+={}\\|\"\'~}\n" + 
				"\n" + 
				"What if there\'s more than one label?\n" + 
				"{tag_list: a OR b}\n" + 
				"{tag_list: foo OR tag_list:bar OR tag_list:meh OR tag_list:abc}\n" + 
				"";
		expected = "{contentbylabel:labels=test}\n" + 
				"\n" + 
				"What if the label has illegalchars?\n" + 
				"{contentbylabel:labels=abcdef}\n" + 
				"{contentbylabel:labels=haswhitespace}\n" + 
				"(These chars are ok: but what about braces?)\n" + 
				"{contentbylabel:labels=abc$%-_+={}\\|\"\'~}\n" + 
				"\n" + 
				"What if there\'s more than one label?\n" + 
				"{contentbylabel:labels=a,b}\n" + 
				"{contentbylabel:labels=foo,bar,meh,abc}\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
