package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class HierarchyLinkConverterTest extends TestCase {

	HierarchyLinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new HierarchyLinkConverter();
		tester.clear();
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

		tester.getProperties().setProperty("meta-dir", HierarchyTitleConverterTest.METADIR);
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", HierarchyTitleConverterTest.PAGESDIR);
	}

	
	protected void tearDown() {
		tester.getProperties().setProperty("page-history-load-as-ancestors-dir", "");
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
				"[food:Food Fruit]\n" + 
				"[food:Food Fruit Apple]\n" + 
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
		String spacekey = "food";
		actual = tester.convertLink(input, "drink/", spacekey, "");
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

	public void testConvertWithPageByPageSpaces() {
		Page page = new Page(new File(HierarchyTitleConverterTest.PAGESDIR+"/SampleDokuwiki-InputTitle.txt"));
		page.setOriginalText("[[.:home]]\n" +
				"[[drink:start]]\n");
		String spacekey = "otherspace";
		page.setSpacekey(spacekey);//default spacekey is 'food'
		tester.convert(page);
		String actual = page.getConvertedText();
		String expected = "[" + spacekey + ":Home]\n" + //this one users the current home
				"[food:Drink]\n"; //this one uses the mapping (drink points to food)
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertWithPageByPageSpaces_2() {
		tester.getProperties().setProperty("space-lala","ns/tada");
		tester.getProperties().setProperty("space-foo","ns/tada/other");
		Page page = new Page(new File(HierarchyTitleConverterTest.PAGESDIR+"/SampleDokuwiki-InputTitle.txt"));
		page.setOriginalText("[[.:home]]\n" +
				"[[ns:tada]]\n" +
				"[[ns:tada:subchild]]\n" +
				"[[ns:tada:other:subchild]]\n" +
				"[[ns:tada:Other:subchild]]\n" +
				"[[ns:tada:other:sub:subsubchild]]\n");
		String spacekey = "otherspace";
		page.setSpacekey(spacekey);//default spacekey is 'food'
		tester.convert(page);
		String actual = page.getConvertedText();
		String expected = "[" + spacekey + ":Home]\n" + //this one users the current home
				"[lala:Tada]\n" +
				"[lala:Subchild]\n" +
				"[foo:Subchild]\n" +
				"[foo:Subchild]\n" +
				"[foo:Subsubchild]\n"; //this one uses the mapping (drink points to food)
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertWithMetaTitle() throws IOException {
		String input, expected, actual;
		input = "[[.:foo]]\n" +
				"[[:foo:bar]]\n";
		expected = "[xyz:Foo Tralala]\n" +
				"[xyz:Harumph BAr]\n";
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages");
		String pretendthispagepath = "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages/test.txt";
		actual = tester.convertLink(input, "", "xyz", pretendthispagepath);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertWithMetaTitle_Ancestor() throws IOException {
		tester.getProperties().setProperty("page-history-load-as-ancestors-dir", "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/attic/");
		String input, expected, actual;
		input = "[[.:foo]]\n" +
				"[[:foo:bar]]\n";
		expected = "[xyz:Foo Tralala]\n" +
				"[xyz:Harumph BAr]\n";
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages");
		String pretendthispagepath = "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages/test.txt";
		actual = tester.convertLink(input, "", "xyz", pretendthispagepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testGetTargetMetaFilename() {
		String input, expected, actual;
		input = "foo";
		String filename = HierarchyTitleConverterTest.METADIR+"/home.meta";
		expected = HierarchyTitleConverterTest.METADIR+"/foo.meta";
		actual = tester.getTargetMetaFilename(input, filename, true);
		assertNotNull(actual);
		assertEquals(expected, actual);


		input = "abc:def:ghi::jkl";
		filename = HierarchyTitleConverterTest.METADIR+"/abc/def/ghi/home.meta";
		expected = HierarchyTitleConverterTest.METADIR+"/abc/def/ghi/jkl.meta";
		actual = tester.getTargetMetaFilename(input, filename, false);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertWithCollision() {
		tester.getProperties().setProperty("collision-titles-xyz", "Harumph BAr");
		String input, expected, actual;
		input = "[[.:foo]]\n" +
				"[[:foo:bar]]\n";
		expected = "[xyz:Foo Tralala]\n" +
				"[xyz:Foo Tralala Harumph BAr]\n";
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages");
		String pretendthispagepath = "/Users/laura/Code/Git/uwc/sampleData/dokuwiki/junit_resources/pages/test.txt";
		actual = tester.convertLink(input, "", "xyz", pretendthispagepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
