package com.atlassian.uwc.converters.dokuwiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HierarchyLinkConverterTest extends TestCase {

	HierarchyLinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new HierarchyLinkConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple,Fruit"); 
		props.setProperty("collision-titles-otherspace", "Testing 123");
		props.setProperty("space-food","food,drink");
		props.setProperty("space-otherspace","otherspace");
		props.setProperty("space-abcdef", "abc_def");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
	}
	
	public void testConvertLink() {
		String input, expected, actual;
		input = "[[drink:start]]\n" + 
				"[[:drink:start]]\n" +
				"[[.drink|Alias]]\n" + 
				"[[drink:juice:start|Alias for the juice link]]\n" + 
				"[[drink:juice:apple]]\n" + 
				"[[drink:water]]\n" + 
				"[[food:start|Food]]\n" + 
				"[[food:Baklava]]\n" + 
				"[[food:fruit:start]]\n" + 
				"[[food:fruit:apple]]\n" + 
				"[[food:pie:start]]\n" + 
				"[[food:pie:apple]]\n" + 
				"[[food:pie:fruit:start]]\n" + 
				"[[food:pie:fruit:apple]]\n" + 
				"[[food:pie:start:apple]]\n" +
				"[[food:pie:fruit:apple:chiffon]]\n" + 
				"[[otherspace:Testing 123]]\n" +
				"[[ food:pie:fruit | Alias ]]\n" +
				"[[ http://abc.com| alias]]\n" +
				"[[abc_def:tada]]\n" +
				"";
		expected = "[food:Drink]\n" + 
				"[food:Drink]\n" +
				"[Alias|food:Drink]\n" + 
				"[Alias for the juice link|food:Juice]\n" + 
				"[food:Juice Apple]\n" + 
				"[food:Water]\n" + 
				"[Food|food:Start]\n" + 
				"[food:Baklava]\n" + 
				"[food:Fruit]\n" + 
				"[food:Fruit Apple]\n" + 
				"[food:Pie]\n" + 
				"[food:Pie Apple]\n" + 
				"[food:Pie Fruit]\n" + 
				"[food:Pie Fruit Apple]\n" + 
				"[food:Start Apple]\n" +
				"[food:Chiffon]\n" +
				"[otherspace:Testing 123]\n" +
				"[Alias|food:Pie Fruit]\n" +
				"[alias|http://abc.com]\n" +
				"[abcdef:Tada]\n";
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_wCurrentPath() {
		String input, expected, actual;
		input = 
				"[[.drink|Alias]]\n" + 
				"[[.fruit|Fruit Drink]]\n" +
				"[[.cranberry]]" + 
				"";
		expected = 
				"[Alias|food:Drink]\n" + 
				"[Fruit Drink|food:Drink Fruit]\n" +
				"[food:Cranberry]";
		actual = tester.convertLink(input, "drink/");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertUNC() {
		String input, expected, actual;
		input = "[[\\\\path\\to\\unc\\file.jpg]]\n";
		expected = input;
		actual = tester.convertLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}
}
