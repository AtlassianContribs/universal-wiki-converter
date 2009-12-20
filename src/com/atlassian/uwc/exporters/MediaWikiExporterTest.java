package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.FileUtils;

public class MediaWikiExporterTest extends TestCase {

	MediaWikiExporter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	private Properties props = null;
	private static final String TESTDIR = "sampleData/mediawiki/junit_resources/";
	protected void setUp() throws Exception {
		tester = new MediaWikiExporter();
		PropertyConfigurator.configure("log4j.properties");
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.mediawiki.properties"));
	}
	
	public void testBasicExport() throws ClassNotFoundException, SQLException, IOException {
		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			String pagepath = outpath + File.separator + "Pages" + File.separator + "UWC_-_Mediawiki_-_Test_Pages.txt";
			File page = new File(pagepath);
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.startsWith("* [[Wikipedia QuickGuide]]"));
			assertTrue(contents.contains("* [[Testing Tables With Lists]]"));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	
	/* test history methods START */
	public void testHistoryExport() throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
		//load history properties
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.history.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			//history pages - oldest
			String pagepath = outpath + File.separator + "Pages" + File.separator + "UWC_-_Mediawiki_-_Test_Pages-1.txt";
			File page = new File(pagepath);
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.contains("[[Wikipedia QuickGuide]]"));
			assertFalse(contents.contains("* [[Testing Tables With Lists]]"));
			//history pages - most recent
			String parentpath = outpath + File.separator + "Pages";
			File parent = new File(parentpath);
			File[] pages = parent.listFiles(new NoSvnFilter() {
				public boolean accept(File file) {
					boolean nosvn = super.accept(file);
					if (file.getName().contains("UWC_-_Mediawiki_-_Test_Pages-")) return true;
					return false;
				}
			});
			assertTrue(pages.length >= 24);
			int mostrecent = 0;
			Pattern p = Pattern.compile("UWC_-_Mediawiki_-_Test_Pages-(\\d+)");
			for (File file : pages) {
				Matcher versionFinder = p.matcher(file.getName());
				if (versionFinder.find()) {
					String version = versionFinder.group(1);
					int current = Integer.parseInt(version);
					if (current > mostrecent) mostrecent = current;
				}
			}
			
			pagepath = outpath + File.separator + "Pages" + File.separator + 
				"UWC_-_Mediawiki_-_Test_Pages-" + mostrecent +
				".txt";
			page = new File(pagepath);
			assertTrue(page.exists());
			contents = readFile(pagepath);
			assertTrue(contents.startsWith("* [[Wikipedia QuickGuide]]"));
			assertTrue(contents.contains("* [[Testing Tables With Lists]]"));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}

	public void testHistoryExportWithSql() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		//load history properties
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.sql.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(1, outdir.listFiles(new NoSvnFilter()).length);
			//history pages - just the three we asked for in the sql
			String parentpath = outpath + File.separator + "Pages";
			File parent = new File(parentpath);
			File[] pages = parent.listFiles(new NoSvnFilter() {
				public boolean accept(File file) {
					boolean nosvn = super.accept(file);
					if (file.getName().contains("BR_tags-")) return true;
					return false;
				}
			});
			assertTrue(pages.length >= 3);

			String pagepath = outpath + File.separator + "Pages" + File.separator + "BR_tags-1.txt";
			File page = new File(pagepath);
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			String expected = "Line 5";
			assertFalse(contents.contains(expected));
			
			pagepath = outpath + File.separator + "Pages" + File.separator + "BR_tags-2.txt";
			page = new File(pagepath);
			assertTrue(page.exists());
			contents = readFile(pagepath);
			expected = "Line5";
			assertTrue(contents.contains(expected));
			
			pagepath = outpath + File.separator + "Pages" + File.separator + "BR_tags-3.txt";
			page = new File(pagepath);
			assertTrue(page.exists());
			contents = readFile(pagepath);
			expected = "Line 5";
			assertTrue(contents.contains(expected));
			
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	public void testHistoryExportWithRevSql() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		//load history properties
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.revsql.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(1, outdir.listFiles(new NoSvnFilter()).length);
			//history pages - just the three we asked for in the sql
			String parentpath = outpath + File.separator + "Pages";
			File parent = new File(parentpath);
			File[] pages = parent.listFiles(new NoSvnFilter() {
				public boolean accept(File file) {
					boolean nosvn = super.accept(file);
					if (file.getName().contains("BR_tags-")) return true;
					return false;
				}
			});
			assertTrue(pages.length == 2); //the custom sql uses limit 2

			String pagepath = outpath + File.separator + "Pages" + File.separator + "BR_tags-1.txt";
			File page = new File(pagepath);
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			String expected = "Line 5";
			assertFalse(contents.contains(expected));
			
			pagepath = outpath + File.separator + "Pages" + File.separator + "BR_tags-2.txt";
			page = new File(pagepath);
			assertTrue(page.exists());
			contents = readFile(pagepath);
			expected = "Line5";
			assertTrue(contents.contains(expected));
			
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	
	
	/* test history methods ENDS */
	
	/* test UDMF methods START */
	public void testUdmfExport() throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.udmf.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			//examine a page
			String pagepath = outpath + File.separator + "Pages" + File.separator + "UWC_-_Mediawiki_-_Test_Pages.txt";
			File page = new File(pagepath);
			//should have user and date data at the beginning
			String expected = "{user:192.168.2.114}\n{timestamp:20090914204159}\n";
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.startsWith(expected));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	
	public void testUdmfExportWithSql() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.udmfsql.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			//examine a page
			String pagepath = outpath + File.separator + "Pages" + File.separator + "UWC_-_Mediawiki.txt";
			File page = new File(pagepath);
			//should have user and date data at the beginning
			String expected = "{user:192.168.2.114}\n{timestamp:20090326165249}\n";
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.startsWith(expected));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	
	public void testUdmfExportWithHistory() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.udmfhistory.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			//examine a page
			String pagepath = outpath + File.separator + "Pages" + File.separator + "UWC_-_Mediawiki_-_Test_Pages-1.txt";
			File page = new File(pagepath);
			//should have user and date data at the beginning
			String expected = "{user:192.168.2.115}\n{timestamp:20060928173850}\n";
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.startsWith(expected));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	/* test UDMF methods END */
	public void testGetTitle() throws FileNotFoundException, IOException {
		String input, expected, actual;
		
		tester.setEncoding("Cp1252");
		
		input = readFile("sampleData/engine/encoding/euro-cp1252.txt");
		byte[] bytes = input.getBytes();
		expected = new String(bytes, "Cp1252");
		actual = tester.getTitle(bytes);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetTitle_UrlEncoding() throws FileNotFoundException, IOException {
		String input, expected, actual;
		
		input = expected = "foo:";
		byte[] bytes = input.getBytes();
		actual = tester.getTitle(bytes);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.setUrlEncoding("true");
		expected = "foo%3A";
		bytes = input.getBytes();
		actual = tester.getTitle(bytes);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testOriginalTitleOptions() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException {
		props = new Properties();
		props.load(new FileInputStream(TESTDIR + "exporter.origtitle.properties"));

		//look for exported directory - shouldn't be there
		String outpath = props.getProperty(MediaWikiExporter.EXPORTER_PROPERTIES_OUTPUTDIR) + 
			"exported_mediawiki_pages";
		File outdir =  new File(outpath);
		assertFalse(outdir.exists());
		try {
			//export
			tester.export(props);
			//look for exported directory, and examine general numbers of pages, and a specific page
			assertTrue(outdir.exists());
			assertEquals(5, outdir.listFiles(new NoSvnFilter()).length);
			//examine a page
			String pagepath = outpath + File.separator + "Discussions" + File.separator + "Main_Page_Discussion.txt";
			File page = new File(pagepath);
			//should have user and date data at the beginning
			String expected = "{orig-title:Main_Page}\n";
			assertTrue(page.exists());
			String contents = readFile(pagepath);
			assertTrue(contents.contains(expected));
		} finally {
			//delete exported directory
			FileUtils.deleteDir(outdir);
		}
	}
	
	private String readFile(String pagepath) throws FileNotFoundException, IOException {
		String contents = "";
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(pagepath));
		while ((line = reader.readLine()) != null) {
			contents += line + "\n";
		}
		reader.close();
		return contents;
	}

}
