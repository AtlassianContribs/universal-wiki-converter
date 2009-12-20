package com.atlassian.uwc.exporters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.FileUtils;

public class PmwikiExporterTest extends TestCase {
	PmwikiExporter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties testprops = null;
	protected void setUp() throws Exception {
		tester = new PmwikiExporter();
		PropertyConfigurator.configure("log4j.properties");
		testprops = loadSettingsFromFile("exporter.pmwiki.properties");
		tester.setProperties(testprops);
	}
	
	public void testExport() {
		String exportpath = testprops.get("out") + File.separator + PmwikiExporter.EXPORTDIR;
		File exportdir = new File(exportpath);
		assertFalse(exportdir.exists());
		
		try {
			tester.export(testprops);
			assertTrue(exportdir.exists());
			File main = new File(exportpath + File.separator + "Main");
			assertTrue(main.exists());
			File[] pages = main.listFiles(new NoSvnFilter());
			assertNotNull(pages);
			assertTrue(pages.length >= 3);

			int counter = 0;
			for (File page : pages) {
				String name = page.getName();
				assertNotNull(name);
				if (name.equals("WikiSandbox") || name.equals("RecentChanges") || name.equals("TestingAttachments"))
					counter++;
			}
			assertTrue(counter == 3);
			
		} finally {
			FileUtils.deleteDir(exportdir);
		}
	}
	
	public void testGetFileFromProperty() {
		File actual = tester.getFileFromProperty("out");
		assertNotNull(actual);
		assertEquals("/Users/laura/tmp", actual.getPath());
		
		testprops.setProperty("out", "sampleData/pmwiki/junit_resources/doesnotexist");
		tester.setProperties(testprops);
		try {
			actual = tester.getFileFromProperty("out");
			fail();
		} catch (IllegalArgumentException e) {}
		
		try {
			actual = tester.getFileFromProperty("doesnotexist");
			fail();
		} catch (IllegalArgumentException e) {}
		
	}


	private Properties loadSettingsFromFile(String testpropslocation) {
		Properties props = new Properties();
		String filepath = "sampleData/pmwiki/junit_resources/" + testpropslocation;
		try {
			props.load(new FileInputStream(filepath));
		} catch (IOException e) {
			String message = "Make sure that the file '" + filepath + "' exists, and contains" +
					" db properties for test database. ";
			log.error(message);
			e.printStackTrace();
			fail(message);
		}
		return props;
	}	
	
}
