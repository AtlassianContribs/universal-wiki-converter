package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class BlogConverterTest extends TestCase {

	BlogConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new BlogConverter();
		tester.clear();
		PropertyConfigurator.configure("log4j.properties");
		tester.getProperties().setProperty("blog-namespaces", "sampleData/dokuwiki");
		tester.getProperties().setProperty("space-foo", "sampleData/dokuwiki");
	}

	public void testConvert() throws IOException {
		File file = new File ("sampleData/dokuwiki/SampleDokuwiki-InputLists.txt");
		Page page = new Page(file);
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		assertTrue(page.isBlog());
		
	}

	public void testNamespaceIsBlog() {
		File file = new File ("sampleData/dokuwiki/SampleDokuwiki-InputLists.txt");
		assertTrue(tester.namespaceIsBlog("sampleData/dokuwiki"));
		assertTrue(tester.namespaceIsBlog(file.getPath()));
		
		file = new File ("sampleData/engine/README.txt");
		assertFalse(tester.namespaceIsBlog(file.getPath()));
		
	}

	public void testConvertBlogMacro() {
		String input, expected, actual;
		String max = "7";
		input = "{{blog>sampleData:dokuwiki?" +
				max +
				"}}" + 
				"";
		expected = getExpected(max, "foo");
		actual = tester.convertBlogMacro(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBlogMacro_case() {
		tester.clear();
		tester.getProperties().setProperty("blog-namespaces", "sampledata/dokuwiki");
		tester.getProperties().setProperty("space-foo", "sampledata/dokuwiki");
		String input, expected, actual;
		String max = "7";
		input = "{{blog>SampleData:DOkuwiki?" +
				max +
				"}}" + 
				"";
		expected = getExpected(max, "foo");
		actual = tester.convertBlogMacro(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBlogMacro_subdir() {
		tester.clear();
		tester.getProperties().setProperty("blog-namespaces", "sampledata/dokuwiki");
		tester.getProperties().setProperty("space-foo", "sampledata/dokuwiki");
		String input, expected, actual;
		String max = "7";
		input = "{{blog>SampleData:DOkuwiki:foobar?" +
				max +
				"}}" + 
				"";
		expected = getExpected(max, "foo");
		actual = tester.convertBlogMacro(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public String getExpected(String max, String space) {
		return "" + 
				"<p>" +
				"<ac:macro ac:name=\"blog-posts\">" +
				"<ac:parameter ac:name=\"spaces\">" +
				space +
				"</ac:parameter>" +
				"<ac:parameter ac:name=\"reverse\">true</ac:parameter>" +
				"<ac:parameter ac:name=\"sort\">creation</ac:parameter>" +
				"<ac:parameter ac:name=\"max\">" +max+ "</ac:parameter></ac:macro></p>";
	}

	public void testSameNamespace() {
		assertTrue(tester.sameNamespace("/Users/laura/foo/bar.txt", "foo/bar"));
		assertTrue(tester.sameNamespace("/Users/laura/foo/bar/something.txt", "foo/bar"));
		assertTrue(tester.sameNamespace("/Users/laura/foo/bar/something/else.txt", "foo/bar"));
		
		assertFalse(tester.sameNamespace("bar/foo.txt", "foo/bar"));
		assertFalse(tester.sameNamespace("bar/foo/bah.txt", "foo/bar"));
	}
}
