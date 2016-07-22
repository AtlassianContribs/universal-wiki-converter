package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.soap.SOAPException;

import junit.framework.TestCase;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.confluence.extra.sharepoint.wrapper.SharePointWebServicesWrapper;
import com.atlassian.uwc.exporters.SharepointExporter.SharepointPage;
import com.microsoft.sharepoint.webservices.lists.GetListItems;
import com.microsoft.sharepoint.webservices.lists.GetListItemsQuery;
import com.microsoft.sharepoint.webservices.lists.GetListItemsQueryOptions;
import com.microsoft.sharepoint.webservices.lists.GetListItemsViewFields;
import com.microsoft.sharepoint.webservices.lists.ListsSoap12Stub;

public class SharepointExporterTest extends TestCase {
	private static final String JUNIT_TESTDIR = "sampleData/sharepoint/junit_resources/";
	private static final String propsPath = "conf" + File.separator + SharepointExporter.DEFAULT_PROPERTIES_LOCATION;
	Logger log = Logger.getLogger(this.getClass());	
	SharepointExporter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new SharepointExporter();
	}
	public void testConnection() {
		Map properties = tester.getProperties(propsPath);
		ListsSoap12Stub service = tester.getListsSoapService(properties);
		String testMessage = SharePointWebServicesWrapper.testSharePointConnection(service);
		if (!testMessage.equals(
				SharePointWebServicesWrapper.TEST_RESULT_SUCCESS)) {
			fail("Can't connect to Sharepoint Server right now! " + testMessage);
		}
	}
	
	public void testValidateProperties() {
		Map properties = tester.getProperties(propsPath);
		//legal
		try {
			tester.validateProperties(properties);
		} catch (IllegalArgumentException e) {
			fail();
		}
		
		//illegal
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, "");
		try {
			tester.validateProperties(properties);
			fail();
		} catch (IllegalArgumentException e) {}
		
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_SERVER, "");
		try {
			tester.validateProperties(properties);
			fail();
		} catch (IllegalArgumentException e) {}

	}

	public void testCreateParents() {
		//create directories
		String existing = JUNIT_TESTDIR;
		String justparents = "sharepointexporter/a/b/c/";
		String parents = existing +
						justparents;
		String tmpDirPath = parents + "d";
		//make sure some parent directories don't exists
		deleteJustParents(existing, tmpDirPath);

		File tmpFile = new File(tmpDirPath);
		
		assertFalse(tmpFile.exists());
		assertFalse(new File(parents).exists());
		
		//try creating them recursively
		assertTrue(tester.createParents(tmpDirPath));
		
		assertTrue(tmpFile.exists());
		
		//clean up for further testing
		deleteJustParents(existing, tmpDirPath);
		
		//try with a trailing slash delim
		tmpDirPath = tmpDirPath + "/";
		tmpFile = new File(tmpDirPath);
		
		assertFalse(tmpFile.exists());
		assertFalse(new File(parents).exists());
		
		assertTrue(tester.createParents(tmpDirPath));
		
		assertTrue(tmpFile.exists());
		deleteJustParents(existing, tmpDirPath);

		//test what happens with a restricted file or something
		//in this case the sharepointexporter-test-error directory is owned by a different user
		String nocreateperms = existing + "sharepointexporter-test-error/";
		tmpDirPath = nocreateperms + "a";
		tmpFile = new File(tmpDirPath);
		
		assertFalse(tmpFile.exists());
		
		assertFalse(tester.createParents(tmpDirPath));
		
		assertFalse(tmpFile.exists());

	}

	public void testCleanOutputDir() {
		tester.startRunning();
		Map<String, String> props = new HashMap<String, String>();
		String testoutput = "/Users/laura/test/sharepointexporter-output-test";
		props.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, testoutput);
		File output = new File(testoutput);
		assertFalse(output.exists());
		
		tester.cleanOutputDir(props);
		
		assertTrue(output.exists());
		String export = testoutput + File.separator + SharepointExporter.EXPORT_DIR;
		File exportFile = new File(export);
		assertTrue(exportFile.exists());
		File tmp = new File(export + File.separator  + "abc");
		
		try {
			tmp.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		assertTrue(tmp.exists());
		
		tester.cleanOutputDir(props);
		assertTrue(output.exists());
		assertFalse(tmp.exists());

		//clean up for unit test
		exportFile.delete();
		output.delete();
	}
	
	public void testCleanOutputDir_WithProblem() {
		tester.startRunning();
		Map<String, String> props = new HashMap<String, String>();
		String testoutput = JUNIT_TESTDIR + "sharepointexporter-test-error2/"; //dir not owned by this user
		props.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, testoutput);
		File output = new File(testoutput);
		assertTrue(output.exists());

		try {
			tester.cleanOutputDir(props);
			fail();
		} catch (IllegalArgumentException e) {}
	}
	
	public void testGetSharepointPages() {
		// get all the pages from all the wikis
		Map properties = tester.getProperties(propsPath);
		HashMap<String, Vector<SharepointPage>> actual = tester.getSharepointPages(properties);
		HashMap expectedSizes = new HashMap();
		expectedSizes.put("Empty Wiki", "0");
		expectedSizes.put("my test wiki", "9");
		expectedSizes.put("test wiki", "2");
		expectedSizes.put("Testing Site Creation With Template", "8");
		int expectedWikiSize = expectedSizes.keySet().size();
		
		assertNotNull(actual);
		assertTrue(actual.keySet().size() == expectedWikiSize);
		for (String wiki: actual.keySet()) {
			assertTrue(expectedSizes.containsKey(wiki));
			assertTrue(expectedSizes.get(wiki).equals(actual.get(wiki).size() + ""));
		}
		//look for a 'my test wiki' page
		boolean found = false;
		for (SharepointPage page : actual.get("my test wiki")) {
			String expTitle = "Test Table"; //XXX This might change
			String expContent = "What sort of tables does SP support?"; //XXX This might change
			if (expTitle.equals(page.getTitle())) {
				if (expContent.contains(expContent))
					found = true;
			}
		}
		assertTrue(found);

		//get pages from a select set of wikis
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "my test wiki, Empty Wiki");
		actual = null;
		actual = tester.getSharepointPages(properties);
		expectedSizes.remove("test wiki");
		expectedSizes.remove("Testing Site Creation With Template");
		assertNotNull(actual);
		expectedWikiSize = expectedSizes.keySet().size();
		assertTrue(actual.keySet().size() == expectedWikiSize);
		for (String wiki: actual.keySet()) {
			assertTrue(expectedSizes.containsKey(wiki));
			assertTrue(expectedSizes.get(wiki).equals(actual.get(wiki).size() + ""));
		}
		found = false;
		boolean dontfind = true; //to test that a page from the entire collection is appropriately missing in the filtered one
		for (String wiki : actual.keySet()) {
			for (SharepointPage page: actual.get(wiki)) {
				//look for a 'my test wiki' page
				String expTitle = "Test Table"; //XXX This might change
				String expContent = "What sort of tables does SP support?"; //XXX This might change
				if (expTitle.equals(page.getTitle())) {
					if (expContent.contains(expContent))
						found = true;
				}
				expTitle = "test page";
				if (expTitle.equals(page.getTitle())) dontfind = false;
			}
		}
		assertTrue(found);
		assertTrue(dontfind);
		
		//FIXME handle errors
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "doesntexist");
		actual = null;
		actual = tester.getSharepointPages(properties);
		assertTrue(actual.isEmpty());
		
	}
	
	public void testGetWikis() {
		String[] expected = {
				"Empty Wiki",
				"my test wiki",
				"test wiki", 
				"Testing Site Creation With Template"
		};
		String filename = propsPath;
		Map properties = tester.getProperties(filename );

		Vector<String> actual = tester.getWikis(properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.length);
		for (int i = 0; i < expected.length; i++) {
			String expWiki = expected[i];
			assertTrue(actual.contains(expWiki));
		}
		
		//test getting specific wikis
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "my test wiki, Empty Wiki");
		String[] expected2 = {"Empty Wiki", "my test wiki"};
		actual = null;
		actual = tester.getWikis(properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected2.length);
		for (int i = 0; i < expected2.length; i++) {
			String expWiki = expected2[i];
			assertTrue(actual.contains(expWiki));
		}
	}
	public void testGetWikisSubsite() {
		String filename = propsPath;
		Map properties = tester.getProperties(filename );

		//test getting subsite wikis
		properties = null;
		properties = tester.getProperties(filename );
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_SERVER, "artemis.sharepointspace.com/subsite2");
		String[] expected3 = {"Subsite Test Wiki", "Wiki Pages"};
		Vector<String> actual = null;
		actual = tester.getWikis(properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected3.length);
		for (int i = 0; i < expected3.length; i++) {
			String expWiki = expected3[i];
			assertTrue(actual.contains(expWiki));
		}
		
	}
	public void testGetWikisError() {
		String filename = propsPath;
		Map properties = tester.getProperties(filename );

		//test failure of binding
		properties = null;
		properties = tester.getProperties(filename );
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_SERVER, "artemis.sharepointspace.com/doesntexist");
		Vector<String> actual = null;
		actual = tester.getWikis(properties);
		assertNull(actual);
	}

	public void testFilterWithProps() {
		Vector<String> input = new Vector<String>();
		input.add("Empty Wiki");
		input.add("my test wiki");
		input.add("test wiki");
		Map properties = tester.getProperties(propsPath);
		//no wikis
		Vector<String> expected = new Vector<String>();
		expected.addAll(input);
		Vector<String> actual = tester.filterWithProps(input, properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			String act = (String) iter.next();
			assertTrue(expected.contains(act));
		}
		//one wiki
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "my test wiki");
		expected = new Vector<String>();
		expected.add("my test wiki");
		actual = null;
		actual = tester.filterWithProps(input, properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			String act = (String) iter.next();
			assertTrue(expected.contains(act));
		}
		
		//multiple wikis
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "my test wiki, Empty Wiki");
		expected = new Vector<String>();
		expected.add("my test wiki");
		expected.add("Empty Wiki");
		actual = null;
		actual = tester.filterWithProps(input, properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			String act = (String) iter.next();
			assertTrue(expected.contains(act));
		}
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "my test wiki,Empty Wiki");
		actual = null;
		actual = tester.filterWithProps(input, properties);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			String act = (String) iter.next();
			assertTrue(expected.contains(act));
		}
		
		//no wiki listed are found
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, "doesn't exist");
		actual = null;
		actual = tester.filterWithProps(input, properties);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}
	
	public void testGetWikiNames() {
		String[] expected = {
				"Empty Wiki",
				"my test wiki",
				"test wiki", 
				"Testing Site Creation With Template"
		};
		MessageElement[] input = new MessageElement[1];
		try {
			input[0] = createTestMessageElement_Collections();
		} catch (SOAPException e) {
			e.printStackTrace();
			fail();
		}
		Vector<String> actual = tester.getWikiNames(input);
		assertNotNull(actual);
		for (int i = 0; i < expected.length; i++) {
			String expWiki = expected[i];
			assertTrue(actual.contains(expWiki));
		}
	}
	
	public void testGetPages() {
		String wiki = "my test wiki";
		Map properties = tester.getProperties(propsPath);
		Vector<SharepointPage> pages = tester.getPages(wiki, properties);
		assertNotNull(pages);
		//check # of pages
		int expectedSize = 9; //XXX This might change.
		assertTrue(pages.size() == expectedSize);
		//look for a particular page
		boolean found = false;
		for (Iterator iter = pages.iterator(); iter.hasNext();) {
			SharepointPage page = (SharepointPage) iter.next();
			if ("Test Page 72".equals(page.getTitle())) { //XXX This might change
				if (page.getContents().contains("For Link Testing"))  // This might change
					found = true;
			}
			//handle problem chars
			if (page.getTitle().contains("Annoying")) { //Page name starts as  Testing @ Annoying Chars
				String expected = "Testing  Annoying Chars";
				String actual = page.getTitle();
				assertEquals(expected, actual);
			}
		}
		assertTrue(found);
		
		//handle errors
		wiki = "doesntexist";
		try {
			pages = tester.getPages(wiki, properties);
			fail();
		} catch (IllegalArgumentException e) {}
		
		
	}
	
	public void testGetListsSoapService() {
		String filename = propsPath;
		Map props = tester.getProperties(filename );
		ListsSoap12Stub service = tester.getListsSoapService(props);
		String result = SharePointWebServicesWrapper.testSharePointConnection(service);
		assertEquals(SharePointWebServicesWrapper.TEST_RESULT_SUCCESS, result);
	}
	
	public void testGetItemParameters() {
		String input = "my test wiki";
		String listName = input;
		String viewName = null; //default view
		GetListItemsQuery query = null;
		GetListItemsViewFields viewFields = null; //filters returned fields
		String rowLimit = "9";
		GetListItemsQueryOptions queryOptions = tester.getEmptyQueryOptions();
		String webId = null; 
		GetListItems expected = new GetListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webId);
		Map properties = tester.getProperties(propsPath);
		GetListItems actual = tester.getItemParameters(input, properties );
		assertNotNull(actual);
		assertEquals(expected.getListName(), actual.getListName());
		assertEquals(expected.getViewName(), actual.getViewName());
		assertEquals(expected.getViewFields(), actual.getViewFields());
		assertEquals(expected.getRowLimit(), actual.getRowLimit());
		assertEquals(expected.getQueryOptions(), actual.getQueryOptions());
		assertEquals(expected.getWebID(), actual.getWebID());
	}
	
	public void testGetItemCount() {
		String input = "my test wiki";
		String expected = "9"; //XXX This might change
		Map properties = tester.getProperties(propsPath);
		String actual = tester.getItemCount(input, properties);
		assertNotNull(actual);
		assertEquals(expected, actual);
		input = "Shared Documents";
		expected = "3"; //XXX This might change
		actual = tester.getItemCount(input, properties);
		assertNotNull(actual);
		assertEquals(expected, actual);
		input = "doesntexist";
		try {
			actual = tester.getItemCount(input, properties);
			fail();
		} catch (IllegalArgumentException e) {}
	}
	
	public void testGetPagesFromXml() throws SOAPException {
		MessageElement[] xml = {createTestMessageElement_Pages()};
		String[] expNames = {"Home", "How To Use This Wiki Library", "Sarah\'s page"};
		String[] expContentsContains = {"Information that is usually traded in e-mail messages, gleaned from",
				" For example, type [[Home]] to create a link to the pa",
				"But even so the editor is not bad.&lt;br&gt;&lt;br&gt;&lt;font size=5 "};
		
		Vector<SharepointPage> actual = tester.getPagesFromXml(xml);
		assertNotNull(actual);
		assertTrue(actual.size() == expNames.length);
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			SharepointPage page = (SharepointPage) iter.next();
			String actTitle = page.getTitle();
			String actContents = page.getContents();
			assertNotNull(actTitle);
			boolean found = false;
			for (int i = 0; i < expNames.length; i++) {
				String expTitle = expNames[i];
				if (expTitle.equals(actTitle)) {
					found = true;
					assertTrue(actContents.contains(expContentsContains[i]));
				}
			}
			assertTrue(found);
		}
	}
	
	public void testCreateFilesLocally() {
		HashMap<String, Vector<SharepointPage>> input = new HashMap<String, Vector<SharepointPage>>();
		String testpage = "TestSPPage";
		String expContents = "Testing";
		SharepointPage page = tester.new SharepointPage(testpage, expContents);
		Vector<SharepointPage> pages = new Vector<SharepointPage>();
		pages.add(page);
		String wiki = "testwiki";
		input.put(wiki, pages);
		
		Map properties = tester.getProperties(propsPath);
		String testout = "/Users/laura/test/spexporter-test-output/";
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, testout);
		
		assertTrue(new File(testout).exists());
		
		testout += SharepointExporter.EXPORT_DIR + File.separator;
		File file = new File(testout + wiki + "/" + testpage);
		assertFalse(file.exists());
		
		tester.startRunning();
		tester.createFilesLocally(input, properties);
		
		assertTrue(file.exists());
		String actContents = readFile(testout + wiki + "/" + testpage);
		assertNotNull(actContents);
		assertEquals(expContents + "\n", actContents);
		
		file.delete();
	}
	
	public void testGetParentDir() {
		Map properties = tester.getProperties(propsPath);
		String testdir = "/Users/laura/test/spexporter-test-output";
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, testdir);
		String expected = testdir  +  File.separator  + SharepointExporter.EXPORT_DIR;
		String actual = tester.getParentDir(properties);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		testdir += File.separator;
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, testdir);
		actual = tester.getParentDir(properties);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testCreateFileLocally() {
		String testdir = "/Users/laura/test/spexporter-test-output/";
		assertTrue(new File(testdir).exists());
		
		testdir += "parent/";
		assertFalse(new File(testdir).exists());
		
		String testfile = "testing123";
		String expContents = "Testing 123\nABC\n";
		
		File test = new File(testdir + testfile);
		assertFalse(test.exists());
		
		tester.createFileLocally(testfile, testdir, expContents);

		//test parent creation
		assertTrue(new File(testdir).exists());
		//test file creation
		assertTrue(test.exists());
		
		//test file contents
		String actContents = readFile(testdir + testfile);
		assertNotNull(actContents);
		assertEquals(expContents, actContents);
		
		//clean up
		test.delete();
		new File(testdir).delete();
	}
	
	public void testWriteFile() {
		String testdir = "/Users/laura/test/spexporter-test-output/";
		assertTrue(new File(testdir).exists());
		
		String testfile = testdir + "testing123";
		File test = new File(testfile);
		assertFalse(test.exists());
		
		String expContents = "Testing 123\nABC\n";
		tester.writeFile(testfile, expContents);
		
		assertTrue(test.exists());
		
		//check that the contents was set
		String contents = readFile(testfile);
		assertNotNull(contents);
		assertEquals(expContents, contents);
		
		test.delete();
	}

	
	
	public void testExportSharepoint() {
		String expectedWikis = "my test wiki, Testing Site Creation With Template";
		String[] expected = expectedWikis.split(", ");
		String[] expectedPages = {
				"Home",
				"How To Use This Wiki Library",
				"Sarah's page", 
				"Sharepoint Converter Links",
				"Test Lists", 
				"Test Page 42",
				"Test Page 72",
				"Test Table"
		};
		Map properties = tester.getProperties(propsPath);
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, expectedWikis);
		
		tester.startRunning();
		tester.cleanOutputDir(properties);
		
		String outputParentPath = (String) properties.get(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR);
		File outputParent = new File(outputParentPath);
		assertTrue(outputParent.exists());
		String outputExport = tester.getParentDir(properties);
		File output = new File(outputExport);
		
		tester.exportSharepoint(properties);
		
		assertTrue(output.exists());
		String[] actual = output.list();
		assertTrue(actual.length == 2); //one directory for each string in expected
		for (String wikidir : actual) {
			boolean found = false;
			for (int i = 0; i < expected.length; i++) {
				String dir = expected[i];
				if (wikidir.equals(dir)) found = true;
			}
			assertTrue(found);
		}
		for (String wikidir : actual) {
			File actFile = new File(outputExport + File.separator + wikidir);
			assertFalse(actFile.isFile());
			assertTrue(actFile.isDirectory());
			String[] children = actFile.list();
			assertNotNull(children);
			int expectedChildren = (wikidir.startsWith("my")?9:8); //XXX This could change
			assertTrue(children.length == expectedChildren);
			for (int i = 0; i < children.length; i++) {
				String child = children[i];
				boolean foundChild = false;
				for (int j = 0; j < expectedPages.length; j++) {
					String expPage = expectedPages[j];
					if (child.equals(expPage)) 
						foundChild = true;
				}
				if (wikidir.startsWith("my") && child.equals("Testing  Annoying Chars")) {
					foundChild = true;
				}
				assertTrue(foundChild);
			}
		}
	}
	
	public void testExportSharepointContents() {
		String expectedWikis = "my test wiki";
		Map properties = tester.getProperties(propsPath);
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_WIKIS, expectedWikis);

		tester.startRunning();
		tester.cleanOutputDir(properties);
		String outputExport = tester.getParentDir(properties);
		File output = new File(outputExport);
		assertTrue(output.list().length == 0);
		
		tester.exportSharepoint(properties);

		String testpath = outputExport + File.separator + expectedWikis + File.separator  + "Test Page 72";
		File testfile = new File(testpath);
		assertTrue(testfile.exists());
		String contents = readFile(testpath);
		assertTrue(contents.contains("For Link Testing. We\'re going to link to this from"));
		
	}
	
	public void testGetDirectories() {
		HashMap<String, Vector<SharepointPage>> input = new HashMap<String, Vector<SharepointPage>>();
		input.put("abc", null);
		input.put("def", new Vector<SharepointPage>());
		Vector<String> expected = new Vector<String>();
		expected.add("abc");
		expected.add("def");
		
		Vector<String> actual = tester.getDirectories(input);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (String act : actual) {
			assertTrue(expected.contains(act));
		}
		for (String exp : expected) {
			assertTrue(actual.contains(exp));
		}

		
		input = new HashMap<String, Vector<SharepointPage>>();
		input.put("test wiki", null);
		input.put("testwiki", null);
		expected = new Vector<String>();
		expected.add("testwiki2");
		expected.add("testwiki");
		actual = tester.getDirectories(input);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (String act : actual) {
			assertTrue(expected.contains(act));
		}
		for (String exp : expected) {
			assertTrue(actual.contains(exp));
		}
		
		input.put("testwiki2", null);
		expected.add("testwiki3");
		actual = tester.getDirectories(input);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (String act : actual) {
			assertTrue(expected.contains(act));
		}
		for (String exp : expected) {
			assertTrue(actual.contains(exp));
		}
		
		input.put("test wiki2", null);
		expected.add("testwiki4");
		actual = tester.getDirectories(input);
		assertNotNull(actual);
		assertTrue(actual.size() == expected.size());
		for (String act : actual) {
			assertTrue(expected.contains(act));
		}
		for (String exp : expected) {
			assertTrue(actual.contains(exp));
		}
		
	}
	
	public void testCreateDirectoriesLocally() {
		Vector<String> dir = new Vector<String>();
		dir.add("test1");
		dir.add("test2");
		
		Map properties = tester.getProperties(this.propsPath);
		tester.startRunning();
		tester.cleanOutputDir(properties);
		
		String path = tester.getParentDir(properties);
		assertTrue(new File(path).list().length == 0);
		
		tester.createDirectoriesLocally(dir, properties);
		assertTrue(new File(path).list().length == 2);
		
		File test1 = new File(path + File.separator + "test1");
		File test2 = new File(path + File.separator + "test2");
		assertTrue(test1.exists());
		assertTrue(test2.exists());
		assertTrue(test1.isDirectory());
		assertTrue(test2.isDirectory());
		
		tester.cleanOutputDir(properties);
		
		//handle bad chars in wiki names
		dir.remove("test2");
		dir.add("test@");
		
		path = tester.getParentDir(properties);
		assertTrue(new File(path).list().length == 0);
		
		tester.createDirectoriesLocally(dir, properties);
		assertTrue(new File(path).list().length == 2);
		
		test1 = new File(path + File.separator + "test1");
		test2 = new File(path + File.separator + "test");
		assertTrue(test1.exists());
		assertTrue(test2.exists());
		assertTrue(test1.isDirectory());
		assertTrue(test2.isDirectory());
		
		tester.cleanOutputDir(properties);
		
		//handle errors in out directory creation
		properties.put(SharepointExporter.EXPORTER_PROPERTIES_OUTPUTDIR, "/Users/laura/test/sharepointexporter-test-error");
		assertTrue(new File(path).list().length == 0);
	
		try {
			tester.createDirectoriesLocally(dir, properties);
			fail();
		} catch (IllegalArgumentException e) {}	
	}
	
	public void testFilterBadChars_String() {
		String input = "abc123";
		String actual, expected;
		expected = input;
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc 123";
		expected = input;
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String filteredinput = "abc123";
		input = "abc\n123";
		expected = filteredinput;
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc-123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc_123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc:123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc'123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc;123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc+123";
		actual = tester.filterBadChars(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testFilterBadChars_Vector() {
		Vector<SharepointPage> inputVec;

		String input = "legal";
		SharepointPage page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		Vector<SharepointPage> actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(input, actual.get(0).getTitle());

		input = "legal'";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(input, actual.get(0).getTitle());
		
		input = "legal$";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(input, actual.get(0).getTitle());
		
		//:, @, /, \, |, ^, #, ;, [, ], {, }, <, >) or start with ($, .., ~
		String expIllegal = "illegal";
		input = "illegal:";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal@";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal/";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal\\";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal|";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal^";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal#";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal;";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal[";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal]";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal{";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal}";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal<";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "illegal>";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "$illegal";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "..illegal";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
		input = "~illegal";
		page = tester.new SharepointPage(input, "");
		inputVec = new Vector<SharepointPage>();
		inputVec.add(page);
		actual = tester.filterBadChars(inputVec);
		assertNotNull(actual);
		assertEquals(expIllegal, actual.get(0).getTitle());
		
	}
	/* Helper methods */
	
	/**
	 * convenience method for deleting some directories
	 * @param existing
	 * @param justparents
	 */
	private void deleteJustParents(String existing, String parents) {
		while (!parents.equals("")) {
			if (parents.equals(existing) || (parents + "/").equals(existing)) {
				log.debug("We've deleted enough. Leaving as: " + parents);
				break;
			}
			File file = new File (parents);
			boolean b = file.delete();
			if (!b) {
				log.debug("Couldn't delete. Suspicious. Leaving as: " + parents);
				break;
			}
			log.debug("Deleting: " + parents + " " + b);
			parents = parents.replaceFirst("(.*?)\\/[^\\/]*.$", "$1");
			log.debug("new parent = " + parents);
		}
	}

	public void testDeleteJustParents() {
		String exists = JUNIT_TESTDIR;
		String noexist = "sharepointexporter123/a/"; //should exist at beginning of test. We'll delete it.
		tester.createParents(exists + noexist); //XXX This is kinda self-referential
		
		File existsFile = new File(exists);
		File noexistFile = new File(exists + noexist);
		assertTrue(existsFile.exists());
		assertTrue(noexistFile.exists());
		
		deleteJustParents(exists, exists + noexist);
		
		assertTrue(existsFile.exists());
		assertFalse(noexistFile.exists());
		
	}
	
	private MessageElement createTestMessageElement_Collections() throws SOAPException {
		MessageElement element = new MessageElement();
		element.setName("ns1:Lists");
		element.setAttribute("xmlns:ns1", "http://schemas.microsoft.com/sharepoint/soap/");
		addListItem(element, "Announcements", "104");
		addListItem(element, "Calendar", "106");
		addListItem(element, "Empty Wiki", "119");
		addListItem(element, "Links", "103");
		addListItem(element, "List Template Gallery", "114");
		addListItem(element, "Master Page Gallery", "116");
		addListItem(element, "my test wiki", "119");
		addListItem(element, "Shared Documents", "101");
		addListItem(element, "Site Template Gallery", "111");
		addListItem(element, "Tasks", "107");
		addListItem(element, "Team Discussion", "108");
		addListItem(element, "test wiki", "119");
		addListItem(element, "Testing Site Creation With Template", "119");
		addListItem(element, "User Information List", "112");
		addListItem(element, "Web Part Gallery", "113");

		return element;
		
	}

	private void addListItem(MessageElement parent, String title, String template) throws SOAPException {
		MessageElement el = new MessageElement();
		el.setName("ns1:List");
		el.setAttribute("Title",title);
		el.setAttribute("ServerTemplate",template);
		parent.addChild(el);
	}

	private void addPageItem(MessageElement parent, String title, String contents) throws SOAPException {
		MessageElement el = new MessageElement();
		el.setName("ns1:row");
		el.setAttribute("ows_LinkFilename",title);
		el.setAttribute("ows_MetaInfo",contents);
		parent.addChild(el);
	}
	private MessageElement createTestMessageElement_Pages() throws SOAPException {
		MessageElement element = new MessageElement();
		MessageElement dataEl = new MessageElement();
		element.addChild(dataEl);
		
		addPageItem(dataEl, "Home.aspx", "2;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_metatags:VR|CollaborationServer SharePoint\\ Team\\ Web\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:SX|DocumentTemplates\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|false WikiField:SW|&lt;div class=ExternalClass9541BD829320479298010E00414B0954&gt;Welcome to your wiki library!\r\n  You can get started and add content to this page by clicking Edit at the top of this page, or you can learn more about wiki libraries by clicking &lt;a class=ms-wikilink href=&quot;/my%20test%20wiki/How%20To%20Use%20This%20Wiki%20Library.aspx&quot;&gt;How To Use This Wiki Library&lt;/a&gt;.\r\n  \r\n  What is a wiki library?sdfasfasdfsdfsadf sadfsadfsadf\r\n  Wikiwiki means quick in Hawaiian. A wiki library is a document library in which users can easily edit any page. The library grows organically by linking existing pages together or by creating links to new pages. If a user finds a link to an uncreated page, he or she can follow the link and create the page.\r\n  \r\n  In business environments, a wiki library provides a low-maintenance way to record knowledge. Information that is usually traded in e-mail messages, gleaned from hallway conversations, or written on paper can instead be recorded in a wiki library, in context with similar knowledge.\r\n  \r\n  Other example uses of wiki libraries include brainstorming ideas, collaborating on designs, creating an instruction guide, gathering data from the field, tracking call center knowledge, and building an encyclopedia of knowledge.&lt;/div&gt; vti_modifiedby:SR|PUBLIC36\\brendanpatterson vti_cachedhastheme:BR|false "); 
		addPageItem(dataEl, "How To Use This Wiki Library.aspx", "1;#vti_parserversion:SR|12.0.0.6219 vti_metatags:VR|CollaborationServer SharePoint\\ Team\\ Web\\ Site vti_charset:SR|utf-8 ContentTypeId:SW|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_author:SR| vti_setuppath:SX|DocumentTemplates\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|false WikiField:SW|&lt;div class=ExternalClassAD688BB3445042539B1486DE41F98CF2&gt;&lt;div&gt;&lt;strong&gt;&lt;font size=3&gt;How to use this wiki library&lt;/font&gt;&lt;/strong&gt;&lt;br&gt; \r\n  You can use this wiki library to share knowledge, brainstorm ideas, collaborate with your team on a design, create an instruction guide, build an encyclopedia of knowledge, or just write down daily information in an easily accessible and modifiable format.&lt;br&gt; &lt;br&gt;&lt;br&gt;&lt;strong&gt;Editing wiki pages&lt;/strong&gt;&lt;br&gt;\r\n  This wiki library provides what-you-see-is-what-you-get (WYSIWYG) editing. To edit a page, click &lt;strong&gt;Edit&lt;/strong&gt; at the top of the page. You can insert tables and pictures with the click of a button. When you are happy with your changes you can click &lt;strong&gt;OK&lt;/strong&gt; to update the page.&lt;br&gt;&lt;br&gt;&lt;strong&gt;Creating links to pages&lt;/strong&gt;&lt;br&gt;\r\n  You can link to another page in this wiki library by enclosing the name of the page in double brackets on the edit form. For example, type [[Home]] to create a link to the page named Home and [[How To Use This Wiki Library]] to create a link to this page.&lt;br&gt;\r\n  &lt;br&gt;\r\n  To create a link to a page and have the link display different text than the page name, type a pipe character (|) after the page name, and then type the display text. For example, type [[Home|Home Page]] to create the link labeled Home Page that points to the page named Home.&lt;br&gt;\r\n  &lt;br&gt;\r\n  To display double opening or closing brackets without making a link, type a backslash before the two brackets. For example, \\[[ or \\]].&lt;br&gt;  &lt;br&gt;&lt;strong&gt;Creating pages&lt;/strong&gt;&lt;br&gt;\r\n  There are two main ways to create a new page in your wiki library:\r\n  &lt;br&gt;\r\n      &lt;ol&gt;\r\n          &lt;li&gt;\r\n              &lt;u&gt;Create a forward link to another page and then click on it to create the page&lt;/u&gt;:&lt;br&gt;\r\n              This is the recommended way to create a page because it is easier for people to find the page when another page links to it.&lt;br&gt;&lt;br&gt;\r\n              Forward links to pages that do not exist have a dashed underline. Start by adding the link (follow the &lt;strong&gt;Creating links to pages&lt;/strong&gt; process earlier on this page). Click the link to go to the Create Page form where you can start typing your content.&lt;br&gt;&lt;br&gt;\r\n          &lt;/li&gt;\r\n          &lt;li&gt;\r\n              &lt;u&gt;Create a page that is not linked to any other&lt;/u&gt;:&lt;br&gt;\r\n              In the &lt;strong&gt;Recent Changes&lt;/strong&gt; section, click &lt;strong&gt;View All Pages&lt;/strong&gt;. Then, on the &lt;strong&gt;New&lt;/strong&gt; menu, click &lt;strong&gt;New Wiki Page&lt;/strong&gt;. This takes you to the Create Page form where you can start typing your content.\r\n          &lt;/li&gt;\r\n      &lt;/ol&gt;\r\n  &lt;br&gt;&lt;strong&gt;Managing your wiki library&lt;/strong&gt;&lt;br&gt;\r\n  You can manage the pages in your wiki library by clicking &lt;strong&gt;View All Pages&lt;/strong&gt; in the &lt;strong&gt;Recent Changes&lt;/strong&gt; section.&lt;br&gt;\r\n  &lt;br&gt;&lt;strong&gt;Restoring a page&lt;/strong&gt;&lt;br&gt;\r\n  If you need to restore a previous version of a page, click &lt;strong&gt;History&lt;/strong&gt; at the top of the page. You can then click on any of the dates to view the page as it existed on that date. When you find the version that you want to restore, click &lt;strong&gt;Restore this version&lt;/strong&gt; on the toolbar.&lt;br&gt;\r\n  &lt;br&gt;&lt;strong&gt;Viewing incoming links&lt;/strong&gt;&lt;br&gt;\r\n  You can see which pages link to the current page by clicking &lt;strong&gt;Incoming Links&lt;/strong&gt; at the top of the page.&lt;br&gt;\r\n  &lt;br&gt;&lt;br&gt;\r\n  For more information about using Windows SharePoint Services-based wiki libraries, click Help on any Windows SharePoint Services page.\r\n  &lt;/div&gt;&lt;/div&gt; vti_modifiedby:SR|PUBLIC36\\brendanpatterson vti_cachedhastheme:BR|false "); 
		addPageItem(dataEl, "Sarah's page.aspx", "3;#vti_parserversion:SR|12.0.0.6219 ContentType:SW|Wiki Page ContentTypeId:SW|0x010108008586D6569CB93B489CBCF8B01B47B952 vti_metatags:VR|CollaborationServer SharePoint\\ Team\\ Web\\ Site vti_charset:SR|utf-8 vti_author:SR| vti_setuppath:SX|DocumentTemplates\\wkpstd.aspx vti_cachedzones:VR|Bottom vti_cachedneedsrewrite:BR|false WikiField:SW|&lt;div class=ExternalClassEDB2E14EA0AB40A387DAA9A275FA7671&gt;&lt;div class=ExternalClass5D95B22ED5A24EA9B978EEFA0E4B93E8&gt;&lt;div class=ExternalClass4063AE12DA0E4183BD4E180737A1F772&gt;&lt;div class=ExternalClassE46829A67B9B4352BC85EC9C52A8676F&gt;&lt;font size=7 color=&quot;#8b0000&quot;&gt;&lt;span style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;It's my &lt;/span&gt;&lt;strong style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;party &lt;/strong&gt;&lt;span style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;and I'll &lt;/span&gt;&lt;em style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;cry &lt;/em&gt;&lt;span style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;if I want to.&lt;/span&gt;&lt;br style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;&lt;/font&gt;&lt;div align=left dir=ltr style=&quot;background-color:rgb(255, 192, 203)&quot;&gt;&lt;ul&gt;&lt;li&gt;&lt;font size=5 color=&quot;#8b0000&quot;&gt;Cry if I want to&lt;/font&gt;&lt;/li&gt;&lt;li&gt;&lt;font size=7 color=&quot;#8b0000&quot;&gt;Cry &lt;font size=4&gt;if I want to&lt;/font&gt;&lt;/font&gt;&lt;/li&gt;&lt;/ul&gt;&lt;blockquote&gt;&lt;font size=7 color=&quot;#8b0000&quot;&gt;&lt;font size=4 face=&quot;Times New Roman&quot;&gt;Formatting oddities of SharePoint have not changed!&lt;/font&gt;&lt;br&gt;&lt;br&gt;&lt;/font&gt;&lt;/blockquote&gt;&lt;font size=7 color=&quot;#8b0000&quot;&gt;But even so the editor is not bad.&lt;br&gt;&lt;br&gt;&lt;font size=5 color=&quot;#000000&quot;&gt;Let's create a &lt;a class=ms-wikilink href=&quot;/my%20test%20wiki/child%20page.aspx&quot;&gt;child page&lt;/a&gt;.&lt;/font&gt;&lt;br&gt;&lt;br&gt;&lt;/font&gt;&lt;/div&gt;&lt;/div&gt;&lt;/div&gt;&lt;/div&gt;&lt;/div&gt; vti_modifiedby:SR|PUBLIC36\\brendanpatterson vti_cachedhastheme:BR...");
		return element;
	}

	private String readFile(String testfile) {
		String contents = "";
        try {
        	FileInputStream stream = new FileInputStream(testfile);
        	InputStreamReader instream = new InputStreamReader(stream, "utf-8");
        	BufferedReader in = new BufferedReader(instream);
        	String line = null;
        	while((line = in.readLine()) != null) {
        		contents += line + "\n";
        	}
		} catch (IOException e) { 
			fail();
		}
		return contents;
	}
}
