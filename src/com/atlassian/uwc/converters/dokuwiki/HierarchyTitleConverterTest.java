package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Comment;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class HierarchyTitleConverterTest extends TestCase {

	HierarchyTitleConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	
	static final String METADIR = "sampleData/dokuwiki/junit_resources/meta";
	private static final String SAMPLEMETA = METADIR + "/SampleDokuwiki-InputTitle.meta";
	static final String PAGESDIR = "sampleData/dokuwiki/junit_resources/pages";
	private static final String SAMPLEPAGE = PAGESDIR+"/SampleDokuwiki-InputTitle.txt";
	
	protected void setUp() throws Exception {
		tester = new HierarchyTitleConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.getProperties().setProperty("meta-dir", METADIR);
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", PAGESDIR);
	}

	public void testFixTitleWithMeta() throws IOException {
		String path = SAMPLEPAGE;
		File file = new File(path);
		Page page = new Page(file);
		page.setName(file.getName());
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		String expected = "Sample Dokuwiki Input Title";
		String actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixTitle() {
		String input, expected, actual;
		input = "test_a123";
		expected = "Test A123";
		actual = tester.fixTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCasify() {
		String input, expected, actual;
		input = "test a123";
		expected = "Test A123";
		actual = tester.casify(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	

}
