package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class MetaHierarchyTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	TestMetaHierarchy tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new TestMetaHierarchy();
	}

	public void testGetMeta() throws FileNotFoundException, IOException {
		String path = "sampleData/smf/SampleSmf-InputHierarchy.txt";
		File file = new File(path);
		assertTrue(file.exists());
		assertTrue(new File("sampleData/smf/SampleSmf-InputHierarchy.meta").exists());
		Page page = new Page(file, path);
		
		Properties actual = tester.getMeta(page);
		assertNotNull(actual);
		assertEquals("8", actual.getProperty("id"));
		assertEquals("top",actual.getProperty("type"));
		assertEquals("Syntax Test Page", actual.getProperty("title"));
		assertEquals("2", actual.getProperty("parentid"));
		assertEquals("brd", actual.getProperty("parenttype"));
		assertEquals("cat1:brd2", actual.getProperty("ancestors"));
		assertEquals("1245696721", actual.getProperty("time"));
		assertEquals("1", actual.getProperty("userid"));
		assertEquals("admin", actual.getProperty("username"));
		assertEquals("laura.kolker@gmail.com", actual.getProperty("useremail"));

	}
	
	public void testGetMetaPath() {
		String input, expected, actual;
		String path = "sampleData/smf/SampleSmf-InputHierarchy.txt";
		File file = new File(path);
		assertTrue(file.exists());
		assertTrue(new File("sampleData/smf/SampleSmf-InputHierarchy.meta").exists());
		Page page = new Page(file, path);
		expected = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/smf/SampleSmf-InputHierarchy.meta";
		actual = tester.getMetaPath(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
//	stub class for testing the metahierarchy methods. 
	public class TestMetaHierarchy extends MetaHierarchy {

		@Override
		protected HierarchyNode buildRelationships(Page page, HierarchyNode root) {
			return null;
		} 

	}

}
