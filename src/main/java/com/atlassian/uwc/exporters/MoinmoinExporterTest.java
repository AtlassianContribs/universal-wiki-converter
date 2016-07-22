package com.atlassian.uwc.exporters;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FileUtils;

public class MoinmoinExporterTest extends TestCase {

	MoinmoinExporter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new MoinmoinExporter();
		tester.setRunning(true);
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testValidDirectories() {
		Map properties = new HashMap<String,String>();
		//no properties
		tester.setProperties(properties);
		assertFalse(tester.validDirectories());
		
		//missing src
		properties.put("out", "sampleData/moinmoin/sample/out");
		assertFalse(tester.validDirectories());
		
		//missing out
		properties.put("src", "sampleData/moinmoin/sample/orig");
		properties.remove("out");
		assertFalse(tester.validDirectories());
		
		//src is an existing directory
		properties.put("out", "sampleData/moinmoin/sample/out");
		assertTrue(tester.validDirectories());
		
		//src is a file
		properties.put("src", "sampleData/moinmoin/sample/orig/FrontPage/edit-log");
		assertFalse(tester.validDirectories());
		
		//out is a file
		properties.put("src", "sampleData/moinmoin/sample/orig");
		properties.put("out", "sampleData/moinmoin/sample/orig/FrontPage/edit-log");
		assertFalse(tester.validDirectories());
		
		//cannot create out directory (permissions issue)
		properties.put("out", "sampleData/moinmoin/sample/nocreate/test");
		assertFalse(tester.validDirectories());
	}
	
	public void testExport() throws ClassNotFoundException, SQLException {
		Map properties = new HashMap<String,String>();
		String outdir = "sampleData/moinmoin/sample/out";

		properties.put("src", "sampleData/moinmoin/sample/orig");
		properties.put("out", outdir);
		tester.setProperties(properties);
		
		File out = new File(outdir);
		if (out.exists()) FileUtils.deleteDir(out);
		try {
			tester.export(properties);
			String[] actual = out.list();
			assertNotNull(actual);
			assertEquals(3, actual.length);
			for (String act : actual) {
				assertTrue(act.equals("TestPage.txt")||act.equals("FrontPage.txt")||act.equals("TestPage(2f)Subpage.txt"));
			}
			
		} finally {
			FileUtils.deleteDir(out);
		}
		
	}
	
	public void testHistory() throws ClassNotFoundException, SQLException, IOException {
		Map properties = new HashMap<String,String>();
		String outdir = "sampleData/moinmoin/sample/out";

		properties.put("src", "sampleData/moinmoin/sample/orig");
		properties.put("out", outdir);
		properties.put("history", "true");
		tester.setProperties(properties);
		File out = new File(outdir);
		if (out.exists()) FileUtils.deleteDir(out);
		try {
			tester.export(properties);
			String[] actual = out.list();
			assertNotNull(actual);
			assertEquals(5, actual.length);
			for (String act : actual) {
				assertTrue(act.equals("TestPage-1.txt")||
						act.equals("TestPage-2.txt")||
						act.equals("FrontPage-1.txt")||
						act.equals("FrontPage-2.txt")||
						act.equals("TestPage(2f)Subpage-1.txt"));
			}
			
			for (File file : out.listFiles()) {
				String contents = FileUtils.readTextFile(file);
				assertNotNull(contents);
				assertTrue(contents.startsWith("{orig-title:"));
				assertFalse(contents.contains("(2f)"));
			}
			
		} finally {
			FileUtils.deleteDir(out);
		}
		
	}
}
