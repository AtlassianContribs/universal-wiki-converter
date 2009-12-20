package com.atlassian.uwc.ui;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.xml.sax.helpers.DefaultHandler;

import biz.artemis.confluence.xmlrpcwrapper.AttachmentForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.CommentForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.ConfluenceServerSettings;
import biz.artemis.confluence.xmlrpcwrapper.PageForXmlRpc;
import biz.artemis.confluence.xmlrpcwrapper.RemoteWikiBroker;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.IllegalPageNameConverter;
import com.atlassian.uwc.converters.xml.DefaultXmlEvents;
import com.atlassian.uwc.converters.xml.XmlEvents;
import com.atlassian.uwc.converters.xml.example.TestCustomXmlEvents;
import com.atlassian.uwc.hierarchies.FilepathHierarchy;
import com.atlassian.uwc.hierarchies.HierarchyBuilder;
import com.atlassian.uwc.hierarchies.HierarchyNode;
import com.atlassian.uwc.ui.ConverterErrors.ConverterError;
import com.atlassian.uwc.ui.listeners.TestSettingsListener;
import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;
/**
 * Test methods for the ConverterEngine.
 * XXX 7 tests will fail without user intervention: 
 * * The testSendPage_ssl/testCheckConfluenceSettings ones must be run exclusively,
 * one at a time, in their own test run, or the ssl properties won't be setup correctly
 * in the JVM. In order to remind you to run the exclusive methods seperately, 
 * I've set those methods up to fail with a special error message if they're run 
 * incorrectly.
 * * The testRemoteApiError test will only succeed if the remote api is off 
 * (since it's testing the behavior when the remote api is off).
 */
public class ConverterEngineTest extends TestCase {

	private static final String TEST_OUTPUT_DIR = "output/output/";
	private static final String TEST_INPUT = "SampleEngine-Input1.txt";
	private static final String TEST_INPUT_DIR = "sampleData/engine/";
	private static final String TEST_PROPS = "test.basic.properties";
	private static final String TEST_SETTING_DIR = "sampleData/engine/";
	private static final String TEST_CONVERTER_PROPS = "converter.testing-convert.properties";
	private static final String NONCONVERTER_PAGEHISTORY_SUFFIX = "Tikiwiki.0001-suffix.page-history-preservation=-v#.txt";
	private static final String NONCONVERTER_PAGEHISTORY = "Tikiwiki.0000-switch.page-history-preservation=true";
	private static final String NONCONVERTER_HIERARCHY = "Tikiwiki.5000-something.hierarchy-builder=com.some.class.BlahBlahBlah";
	private static final String NONCONVERTER_ILLEGAL = "Sharepoint.0005.illegal-handling=false";
	private static final String CONVERTER3 = "Tikiwiki.0200-links.class=com.atlassian.uwc.converters.tikiwiki.LinkConverter";
	private static final String CONVERTER2 = "Tikiwiki.0140-underline.java-regex=={3}(.*?)={3}{replace-with}+$1+";
	private static final String CONVERTER1 = "Tikiwiki.0130-colors.java-regex=~~#([^:]{6}):(.*?)~~{replace-with}{color:#$1}$2{color}";
	private static final String CONVERTER_ILLEGALPAGENAMES = "MyWiki.9999.illegal-names.class=com.atlassian.uwc.converters.IllegalPageNameConverter";
	private static final String CONVERTER_ILLEGALLINKNAMES = "MyWiki.9999.illegal-links.class=com.atlassian.uwc.converters.IllegalLinkNameConverter";
	private static final String CONVERTER_BOLD = "MyWiki.0.bold.java-regex=_{2}(.*?)_{2}{replace-with}*$1*";
	ConverterEngine tester = null;
	Logger log = Logger.getLogger(this.getClass());
	private UWCUserSettings basesettings;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ConverterEngine();
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		basesettings = new UWCUserSettings(location);
		tester.getState(basesettings);
		tester.setRunning(true);
		tester.setSettings(basesettings);
	}
	
	private static int numExclusiveTests = 0;
	
	/* Converter Engine Test from Version 3 */
	
	public void testConvert() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page = TEST_INPUT_DIR + TEST_INPUT;
		File file = new File(page);
		assertTrue(file.exists());
		List<File> files = new Vector<File>();
		files.add(file);
		
		String converterPath = TEST_INPUT_DIR + TEST_CONVERTER_PROPS;
		String converter = null;
		try {
			converter = FileUtils.readTextFile(new File(converterPath));
		} catch (IOException e) {
			fail("Can't load converters from file: " + converterPath);
			e.printStackTrace();
		}
		assertNotNull(converter);
		List<String> converters = new Vector<String>();
		converters.add(converter);
		
		tester.convert(files, converters, settings);
		
		String actualPath = TEST_OUTPUT_DIR + TEST_INPUT;
		String actual = null;
		try {
			actual = FileUtils.readTextFile(new File(actualPath));
		} catch (IOException e) {
			fail("Could not read output file.");
			e.printStackTrace();
		}
		assertNotNull(actual);
		actual = actual.trim();
		String expected = "*BOLD*";
		assertEquals(expected, actual);
	}
	
	public void testConvertPages() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		tester.setRunning(true);
		//check they're loaded
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		assertEquals(csettings.spaceKey, settings.getSpace());

		String pagepath = TEST_INPUT_DIR + TEST_INPUT;
		File file = new File(pagepath);
		assertTrue(file.exists());
		Page page = new Page(file);
		List<Page> pages= new Vector<Page>();
		pages.add(page);

		String converterPath = TEST_INPUT_DIR + TEST_CONVERTER_PROPS;
		String converterStr = null;
		try {
			converterStr = FileUtils.readTextFile(new File(converterPath));
		} catch (IOException e) {
			fail("Can't load converters from file: " + converterPath);
			e.printStackTrace();
		}
		Converter converter = tester.getConverterFromString(converterStr);
		assertNotNull(converter);
		List<Converter> converters = new Vector<Converter>();
		converters.add(converter);
		
		boolean result = tester.convertPages(pages, converters);
		assertTrue(result);
		
		String actualPath = TEST_OUTPUT_DIR + TEST_INPUT;
		String actual = null;
		try {
			actual = FileUtils.readTextFile(new File(actualPath));
		} catch (IOException e) {
			fail("Could not read output file.");
			e.printStackTrace();
		}
		actual = actual.trim();
		assertNotNull(actual);
		String expected = "*BOLD*";
		assertEquals(expected, actual);
	}
	
	public void testConvertPages_ErrorHandling() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		tester.setRunning(true);
		//check they're loaded
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		assertEquals(csettings.spaceKey, settings.getSpace());
		
		File goodFile = new File((TEST_INPUT_DIR + TEST_INPUT));
		assertTrue(goodFile.exists());
		Page goodPage = new Page(goodFile);
		
		File badFile = new File(TEST_INPUT_DIR + "SampleEngine-InputBroken.txt");
		assertTrue(badFile.exists());
		Page badPage = new Page(badFile);
		
		List<Page> pages= new Vector<Page>();
		pages.add(badPage);
		pages.add(goodPage);
		
		String boldStr = "Test.0110-bold.java-regex=_{2}(.*?)_{2}{replace-with}*$1*";
		String badStr = "Test.0200-bad.class=com.atlassian.uwc.ui.test.BadConverter";
		
		Converter converter = tester.getConverterFromString(boldStr);
		Converter converterBad = tester.getConverterFromString(badStr);
		assertNotNull(converter);
		assertNotNull(converterBad);
		List<Converter> converters = new Vector<Converter>();
		converters.add(converter);
		converters.add(converterBad);

		String actualPath = TEST_OUTPUT_DIR + TEST_INPUT;
		File actFile = new File(actualPath);
		if (actFile.exists()) actFile.delete();
		
		boolean result = tester.convertPages(pages, converters);
		assertTrue(result);
		

	}
	
	public void testGetNumberOfSteps() {
		//create stubs
		List<File> files = new Vector<File>();
		List<String> converters = new Vector<String>();
		
		//pages
		String page = TEST_INPUT_DIR + TEST_INPUT;
		File file = new File(page);
		String page2 = TEST_INPUT_DIR + TEST_INPUT;
		File file2 = new File(page);
		String page3 = TEST_INPUT_DIR + TEST_INPUT;
		File file3 = new File(page);
		
		files.add(file);
		files.add(file2);
		files.add(file3);
		
		//converters (tikiwiki has 35)
		UWCGuiModel model = new UWCGuiModel();
		//FIXME set propsPath to a different directory. conf-local isn't right for
		String propsPath = "conf/converter.tikiwiki.properties";
		File props = new File(propsPath);
		//test-specific properties. Maybe have a test directory?
		assertNotNull(props);
		assertTrue(props.exists());
		try {
			converters = model.getConverters(propsPath);
		} catch (IOException e) {
			fail("Should not have caused exception. path = " + propsPath);
			e.printStackTrace();
		}

		//test with uploading
		//pages = 3, converters = 34, uploading = true
		boolean sendToConfuence = true;
		int expected = 34 + 3 + (34 * 3) + 2 + (2*3) + 3 + 3 ;
		int actual = tester.getNumberOfSteps(files, converters, sendToConfuence);
		assertEquals(expected, actual);

		//test with no uploading 
		//pages = 3, converters = 34, uploading = false
		sendToConfuence = false;
		expected -= 3;
		actual = tester.getNumberOfSteps(files, converters, sendToConfuence);
		assertEquals(expected, actual);
		
		//test with illegal handling disabled
		//pages = 3, converters = 35, uploading = false, illegal handling = false
		tester.handleNonConverterProperty(NONCONVERTER_ILLEGAL);
		assertFalse(tester.isIllegalHandlingEnabled());
		expected -= 8; //2 + (2*3) = conv + (conv * pages)
		actual = tester.getNumberOfSteps(files, converters, sendToConfuence);
		assertEquals(expected, actual);
		
	}
	
	public void testSendPage_basic() {
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Home"; //Might as well use "Home". There's always a home.
		page.setName(title);
		updatePageForTest(page, parentid, settings, title);	
	}

	public void testSendPage_comments() {
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Home"; //Might as well use "Home". There's always a home.
		page.setName(title);
		String input = "test comment abcdef";
		page.addComment(input);
		
		//the test
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		try {
			String id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			startPage = broker.getPage(settings, id);
			page.setConvertedText(startPage.getContent());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		tester.sendPage(page, parentid, settings);
		
		//get page again
		PageForXmlRpc endPage = null;
		try {
			String id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			endPage = broker.getPage(settings, id);
			//test that a comment has been set
			XmlRpcClient client = new XmlRpcClient(settings.url + "/rpc/xmlrpc");
			String loginToken = broker.getLoginToken(settings);
			Vector paramsVector = new Vector();
			paramsVector.add(loginToken);
			paramsVector.add(id);
			Vector actual = (Vector) client.execute("confluence1.getComments", paramsVector);
			assertNotNull(actual);
			assertEquals(1, actual.size());
			Hashtable commenttable = (Hashtable) actual.get(0);
			assertNotNull(commenttable);
			CommentForXmlRpc comment = new CommentForXmlRpc();
			comment.setCommentParams(commenttable);
			assertNotNull(comment);
			assertNotNull(comment.getTitle());
			assertEquals("Re: Home", comment.getTitle());
			assertNotNull(comment.getContent());
			assertEquals(input, comment.getContent());
			assertNotNull(comment.getCreator());
			assertEquals(settings.login, comment.getCreator());
			String commentid = comment.getId();
			//get rid of comment
			paramsVector.clear();
			paramsVector.add(loginToken);
			paramsVector.add(commentid);
			client.execute("confluence1.removeComment", paramsVector);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testSendPage_ErrorHandling() { //See uwc-341
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		List<Page> pages = new Vector<Page>();
		Page badpage = new Page(null);
		badpage.setName("$BAD NAME"); //This should cause an exception.
		badpage.setConvertedText("testing");
		
		Page goodpage = new Page(null);
		goodpage.setName("Good page");
		goodpage.setConvertedText("testing");
		
		pages.add(badpage);
		pages.add(goodpage); 
		tester.writePages(pages , settings.spaceKey);
		//firstly - if we do this wrong, the above throws an exception.
		ConverterErrors errors = tester.getErrors();
		assertFalse(errors.getErrors().isEmpty());
		ConverterError error = (ConverterError) errors.getErrors().get(0);
		assertEquals(Feedback.REMOTE_API_ERROR, error.type);
		assertEquals("REMOTE_API_ERROR The Remote API threw an exception when it tried to upload page: \"$BAD NAME\".\n", error.getFeedbackWindowMessage());
		
		tester.getErrors().clear();
		pages.clear();
		badpage.setName("$BAD NAME 2");
		goodpage.setName("Good page 2");
		pages.add(badpage);
		pages.add(goodpage);
		
		FilepathHierarchy hierarchy = new FilepathHierarchy();
		HierarchyNode root = hierarchy.buildHierarchy(pages);
		tester.writeHierarchy(root, 0, settings.spaceKey);
		error = (ConverterError) errors.getErrors().get(0);
		assertEquals(Feedback.REMOTE_API_ERROR, error.type);
		assertEquals("REMOTE_API_ERROR The Remote API threw an exception when it tried to upload page: \"$BAD NAME 2\".\n", error.getFeedbackWindowMessage());
		
		//what happens if confluence fails in a way that does not throw an exception. See UWC-404
		tester.getErrors().clear();
		pages.clear();
		Page badcontent = new Page(null);
		badcontent.setName("OK Pagename");
		String badtext = "\u001F";
		badcontent.setConvertedText(badtext);
		pages.add(badcontent);
		tester.writePages(pages, settings.spaceKey);
		assertFalse(errors.getErrors().isEmpty());
		error = (ConverterError) errors.getErrors().get(0);
		assertEquals(Feedback.REMOTE_API_ERROR, error.type);
		assertEquals("REMOTE_API_ERROR Unknown problem occured while sending page \'OK Pagename\'. See atlassian-confluence.log for more details.\n", error.getFeedbackWindowMessage());
		
	}
	
	public void testSendPage_MovePage() throws XmlRpcException, IOException {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		
		//create a unique id
		String unique = String.valueOf((new Date()).getTime());
		String newtitle = "test-sendpage-move-" + unique;
		//send page
		
		Hashtable pageTable = new Hashtable();
    	pageTable.put("content", "testing");
    	pageTable.put("title", newtitle); 
		tester.sendPage(broker, pageTable, settings);
		
		XmlRpcClient client = getXmlRpcClient(settings);
		//note parent id
		Hashtable page = getPage(client, newtitle, settings.spaceKey, settings);
		String parentid = (String) page.get("parentId");
		assertNotNull(parentid);
		assertEquals("0", parentid);
		
		//get Home's id
		Hashtable home = getPage(client, "Home", settings.spaceKey, settings);
		String homeId = (String) home.get("id");
		assertNotNull(homeId);
		assertFalse("0".equals(homeId));
		
		//send page with Home as parent id
		pageTable.put("parentId", homeId);
		tester.sendPage(broker, pageTable, settings);
		
		//get page
		try {
			page = null;
			page = getPage(client, newtitle, settings.spaceKey, settings);
			parentid = (String) page.get("parentId");
			assertNotNull(parentid);
			assertEquals(homeId, parentid);
		} finally {
			//clean up
			deletePage((String) page.get("id"), settings);
		}
	}

	
	public void testSendComments_ErrorHandlingPerms() {
		//user doesn't have permission to add comment
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.comment.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Home"; //Might as well use "Home". There's always a home.
		page.setName(title);
		String input = "test comment abcdef";
		page.addComment(input);
		
		//the test
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		try {
			String id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			startPage = broker.getPage(settings, id);
			page.setConvertedText(startPage.getContent());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		tester.sendPage(page, parentid, settings);
		Vector actual = tester.getErrors().getErrors();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		String error = actual.get(0).toString();
		assertNotNull(error);
		assertTrue(error.contains("User is not permitted"));
	}
	public void testSendComments_ErrorHandlingNoPage() {
		//page doesn't exist
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Testing Send Comments Error Handling No Pge"; 
		page.setName(title);
		String input = "test comment abcdef";
		page.addComment(input);
		
		//the test
		page.setConvertedText("testing123");
		tester.sendComments(page, RemoteWikiBroker.getInstance(), "0", settings);
		Vector actual = tester.getErrors().getErrors();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		String error = actual.get(0).toString();
		assertNotNull(error);
		assertTrue(error.contains("does not exist"));
	}

	public void testSendPage_Author_NoVersion() throws XmlRpcException, IOException {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		
		//create a unique id
		String unique = String.valueOf((new Date()).getTime());
		String newtitle = "test-sendpage-author-" + unique;
		//send page
		
		Page page = new Page(null);
		page.setName(newtitle);
		page.setOriginalText("testing");
		page.setConvertedText("testing");
		page.setAuthor("notmod");
		tester.sendPage(page, null, settings);
		
		//get page
		XmlRpcClient client = getXmlRpcClient(settings);
		Hashtable testpage = null;
		try {
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			String testid = (String) testpage.get("id");
			assertNotNull(testid);
			String actual = (String) testpage.get("creator");
			String expected = "notmod";
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			actual = (String) testpage.get("modifier");
			assertNotNull(actual);
			assertEquals(expected, actual);
			
		} finally {
			//clean up
			deletePage((String) testpage.get("id"), settings);
		}
	}
	
	public void testSendPage_Author_Versions() throws XmlRpcException, IOException {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		
		//create a unique id
		String unique = String.valueOf((new Date()).getTime());
		String newtitle = "test-sendpage-author-" + unique;
		//send page
		
		Page page = new Page(null);
		page.setName(newtitle);
		page.setOriginalText("testing");
		page.setConvertedText("testing");
		page.setAuthor("notmod");
		page.setVersion(1);
		tester.sendPage(page, null, settings);
		
		//get page
		XmlRpcClient client = getXmlRpcClient(settings);
		Hashtable testpage = null;
		try {
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			String testid = (String) testpage.get("id");
			assertNotNull(testid);
			String actual = (String) testpage.get("creator");
			String expected = "notmod";
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			actual = (String) testpage.get("modifier");
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			//send it again with version 2
			page.setOriginalText("testing 2");
			page.setConvertedText("testing 2");
			page.setVersion(2);
			page.setAuthor("test");
			tester.sendPage(page, null, settings);
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			testid = (String) testpage.get("id");
			assertNotNull(testid);
			actual = (String) testpage.get("creator");
			expected = "notmod";
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			actual = (String) testpage.get("modifier");
			expected = "test";
			assertNotNull(actual);
			assertEquals(expected, actual);
			
		} finally {
			//clean up
			deletePage((String) testpage.get("id"), settings);
		}
	}
	
	public void testSendPage_Timestamp_NoVersion() throws ParseException, XmlRpcException, IOException {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		
		//create a unique id
		String unique = String.valueOf((new Date()).getTime());
		String newtitle = "test-sendpage-date-" + unique;
		//send page
		
		Page page = new Page(null);
		page.setName(newtitle);
		page.setOriginalText("testing");
		page.setConvertedText("testing");
		DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:SS");
		String timestamp = "1901:11:30:13:49:44:59";
		Date date = dateFormat.parse(timestamp);
		page.setTimestamp(date);
		tester.sendPage(page, null, settings);
		
		//get page
		XmlRpcClient client = getXmlRpcClient(settings);
		Hashtable testpage = null;
		try {
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			String testid = (String) testpage.get("id");
			assertNotNull(testid);
			Date actual = (Date) testpage.get("created");
			Date expected = date;
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
			actual = (Date) testpage.get("modified");
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
		} finally {
			//clean up
			deletePage((String) testpage.get("id"), settings);
		}
	}
	
	public void testSendPage_Timestamp_Versions() throws XmlRpcException, IOException, ParseException {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		
		//create a unique id
		String unique = String.valueOf((new Date()).getTime());
		String newtitle = "test-sendpage-author-" + unique;
		//send page
		
		Page page = new Page(null);
		page.setName(newtitle);
		page.setOriginalText("testing");
		page.setConvertedText("testing");
		DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss:SS");
		String timestamp = "1901:11:30:13:49:44:59";
		Date date = dateFormat.parse(timestamp);
		page.setTimestamp(date);
		page.setVersion(1);
		tester.sendPage(page, null, settings);
		
		//get page
		XmlRpcClient client = getXmlRpcClient(settings);
		Hashtable testpage = null;
		try {
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			String testid = (String) testpage.get("id");
			assertNotNull(testid);
			Date actual = (Date) testpage.get("created");
			Date expected = date;
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
			actual = (Date) testpage.get("modified");
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
			//send it again with version 2
			page.setOriginalText("testing 2");
			page.setConvertedText("testing 2");
			page.setVersion(2);
			String timestamp2 = "2001:11:30:13:49:44:59";
			Date date2 = dateFormat.parse(timestamp);
			page.setTimestamp(date2);
			tester.sendPage(page, null, settings);
			testpage = getPage(client, newtitle, settings.spaceKey, settings);
			testid = (String) testpage.get("id");
			assertNotNull(testid);
			actual = (Date) testpage.get("created");
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
			actual = (Date) testpage.get("modified");
			expected = date2;
			assertNotNull(actual);
			assertEquals(getTimestamp(DateFormat.FULL, expected),
		               getTimestamp(DateFormat.FULL, actual));
			
		} finally {
			//clean up
			deletePage((String) testpage.get("id"), settings);
		}
	}
	private String getTimestamp(int format, Date date) {
	      DateFormat dateFormat = DateFormat.getDateTimeInstance(format, format);
	      return (dateFormat.format(date));
	   }
	
	private Properties loadSettingsFromFile(ConfluenceServerSettings settings, String testpropslocation) {
		Properties props = new Properties();
		String filepath = "sampleData/engine/" + testpropslocation;
		try {
			props.load(new FileInputStream(filepath));
        } catch (IOException e) {
        	String message = "Make sure that the file '" + filepath + "' " +
			        			"exists and contains the following " +
			        			"settings: login, password, url, space. Also: " +
			        			"truststore and trustpass if you are testing an SSL " +
			        			"protected confluence.";
			log.error(message);
        	e.printStackTrace();
        	fail(message);
        }
		settings.login = props.getProperty("login");
		settings.password = props.getProperty("password");
		settings.url = props.getProperty("url");
		settings.spaceKey = props.getProperty("space");
		settings.truststore = props.getProperty("truststore");
		settings.trustpass = props.getProperty("trustpass");
		settings.trustallcerts = props.getProperty("trustall");
		
		return props; //in case we need other properties
	}


	private void updatePageForTest(Page page, String parentid, ConfluenceServerSettings settings, String title) {
		//get page
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		try {
			String id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			startPage = broker.getPage(settings, id);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		//change content
		String content = startPage.getContent();
		String ending = "+ adding some content to test";
		
		//send page with changed content 
		page.setConvertedText(content + ending);
		tester.sendPage(page, parentid, settings);
		
		//get page again
		PageForXmlRpc endPage = null;
		try {
			String id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			endPage = broker.getPage(settings, id);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		//test that content is the new content
		String newcontent = endPage.getContent();
		assertNotNull(newcontent);
		assertEquals(content + ending, newcontent);
		
		//return page to normal
		page.setConvertedText(content);
		tester.sendPage(page, parentid, settings);
	}
	
	//XXX Does this test interfere with the following one?
	public void testSendPage_ssl() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		//set up settings XXX Should be from a local file. 
		String testpropslocation = "test.ssl.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Home"; //Might as well use "Home". There's always a home.
		page.setName(title);
		//get page
		updatePageForTest(page, parentid, settings, title);	
	}
	
	public void testSendPage_ssl_trustall() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		Page page = new Page(null);
		String parentid = "0"; //XXX I don't think this settings currently matters?
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		//set up settings XXX Should be from a local file. 
		String testpropslocation = "test.ssl.trustall.properties";
		loadSettingsFromFile(settings, testpropslocation);
		String title = "Home"; //Might as well use "Home". There's always a home.
		page.setName(title);
		//get page
		updatePageForTest(page, parentid, settings, title);	
	}
	
	public void testCheckConfluenceSettings() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		
		//works
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		try {
			tester.checkConfluenceSettings(settings);
		} catch (IllegalArgumentException e) {
			fail();
		}
		assertTrue(testListener.testConnectionSetting(settings).contains("SUCCESS"));
		
		//bad login
		settings.login += "foobar";
		try {
			tester.checkConfluenceSettings(settings);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("BAD_LOGIN"));
		}
		assertTrue(testListener.testConnectionSetting(settings).contains("BAD_LOGIN"));
		
		//ssl server
		testpropslocation = "test.ssl.properties";
		settings = new ConfluenceServerSettings();
		loadSettingsFromFile(settings, testpropslocation);
		try {
			tester.checkConfluenceSettings(settings);
		} catch (IllegalArgumentException e) {
			fail();
		}
		assertTrue(testListener.testConnectionSetting(settings).contains("SUCCESS"));
		settings.password += "12345";
		try {
			tester.checkConfluenceSettings(settings);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("BAD_PASSWORD"));
		}
		assertTrue(testListener.testConnectionSetting(settings).contains("BAD_PASSWORD"));
		
	}
	
	public void testCheckConfluenceSettings_NoTruststore() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		
		//none of the trust properties are set up
		String testpropslocation = "test.ssl.notrust.properties";
		loadSettingsFromFile(settings, testpropslocation);
		settings = new ConfluenceServerSettings();
		loadSettingsFromFile(settings, testpropslocation);
		try {
			tester.checkConfluenceSettings(settings);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("BAD_SETTING"));
		}
		
		
	}
	
	public void testCheckConfluenceSettings_BadTruststore() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		
		//none of the trust properties are set up
		String testpropslocation = "test.ssl.badtrust.properties";
		loadSettingsFromFile(settings, testpropslocation);
		settings = new ConfluenceServerSettings();
		loadSettingsFromFile(settings, testpropslocation);
		try {
			tester.checkConfluenceSettings(settings);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("BAD_SETTING"));
		}
	}
	
	public void testCheckConfluenceSettings_BadTruststore2() {
		if (numExclusiveTests++ > 0) fail("Only one 'exclusive' test can be run at a time.");
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		
		//none of the trust properties are set up
		String testpropslocation = "test.ssl.badtrust2.properties";
		loadSettingsFromFile(settings, testpropslocation);
		settings = new ConfluenceServerSettings();
		loadSettingsFromFile(settings, testpropslocation);
		try {
			tester.checkConfluenceSettings(settings);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("BAD_SETTING"));
		}
	}
	
	public void testCheckConfluenceSettings_AutoDetect() {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();

		String testpropslocation = "test.autodetect.properties";
		loadSettingsFromFile(settings, testpropslocation);
		tester.handleAutoDetectSpacekeys("xyz", "true");
		try {
			tester.checkConfluenceSettings(settings);
		} catch (IllegalArgumentException e) {
			fail();
		}
		
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		
		assertTrue(testListener.testConnectionSetting(settings, true).contains("SUCCESS"));
	}
	
	public void testCheckConfluenceSettings_AutoDetect_PermissionToAddSpace() {
		ConfluenceServerSettings settings = new ConfluenceServerSettings();

		//If the user does not have permission, the Test Connection Button can't tell us that 
		//there's a problem, so that test will succeed
		String testpropslocation = "test.autodetect-perms.properties";
		loadSettingsFromFile(settings, testpropslocation);
		tester.handleAutoDetectSpacekeys("xyz", "true");
		try {
			tester.checkConfluenceSettings(settings);
		} catch (IllegalArgumentException e) {
			fail();
		}
		
		TestSettingsListener testListener = new TestSettingsListener(null, new UWCGuiModel(), null);
		assertTrue(testListener.testConnectionSetting(settings, true).contains("SUCCESS"));
		
		//but we can give the user useful info when we try to create the space
		settings.spaceKey = "uwctestfoobar";
		assertFalse(tester.createSpace(settings));
		assertEquals(1, tester.getErrors().getErrors().size());
	}

	public void testUploadOrphanAttachments() {
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		
		//works
		String testpropslocation = "test.orphan.properties";
		Properties props = loadSettingsFromFile(settings, testpropslocation);

		String title = "Orphan attachments";

		//run orphan feature
		String dir = (String) props.get("attachments");
		UWCUserSettings usettings = new UWCUserSettings();
		usettings.setLogin(settings.login);
		usettings.setPassword(settings.password);
		usettings.setUrl(settings.url);
		usettings.setSpace(settings.spaceKey);
		usettings.setTruststore(settings.truststore);
		usettings.setTrustpass(settings.trustpass);
		tester.setSettings(usettings);
		ArrayList<File> orphanAttachments = tester.findOrphanAttachments(dir);
		tester.uploadOrphanAttachments(orphanAttachments);
		
		//test results
		String id = null;
		PageForXmlRpc page = null;
		try {
			id = broker.getPageIdFromConfluence(settings, settings.spaceKey, title);
			page = broker.getPage(settings, id);
		} catch (Exception e) {
			fail("No orphan attachments page.");
		}
		XmlRpcClient client = null;
		String loginToken = null;
		try {
			client = new XmlRpcClient(settings.url);
			loginToken = broker.getLoginToken(settings);
		} catch (MalformedURLException e1) {
			fail("Could not create XmlRpcClient for url: " + settings.url);
		} catch (Exception e) {
			fail("Could not get login token to url: " + settings.url);
		}
		
		try {
			Vector<String> paramsVector = new Vector<String>();
			paramsVector.add(loginToken);
			paramsVector.add(id);
			//FIXME xmlrpc call not working
//			Vector attachments = (Vector) client.execute("confluence1.getAttachments", paramsVector);
//			assertNotNull(attachments);
//			assertFalse(attachments.isEmpty());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Problem getting attachments for orphan attachments page");
		}
		
		//remove Orphan attachments page
		settings.url = settings.url.replaceFirst("http://", "");
		deletePage(title, settings.spaceKey, settings);
	}

	public void testXmlEventsProperties() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		String path = "sampleData/xml-framework/converter.testxml.properties";
		String convertersdata = FileUtils.readTextFile(new File(path));
		List<String> converterStrings = new Vector<String>();
		for (String converter : convertersdata.split("\n")) {
			if (converter.startsWith("#")) continue; //comment
			converterStrings.add(converter);
		}
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		assertNotNull(converters);
		assertEquals(1, converters.size());
		
		//test misc properties
		Properties props = tester.handleMiscellaneousProperties("a", "b");
		assertNotNull(props);
		assertTrue(props.containsKey("xmlevents"));
		String xmleventsClass = props.getProperty("xmlevents");
		assertEquals("com.atlassian.uwc.converters.xml.DefaultXmlEvents", xmleventsClass);
		
		//test xml events map
		Class eventsClass = Class.forName(xmleventsClass);
		XmlEvents events2 = (XmlEvents) eventsClass.newInstance();
		HashMap<String, DefaultHandler> actual = events2.getEvents();
		assertNotNull(actual);
		assertEquals(3, actual.size());
	}


	public void testXmlEvents_DefaultXmlEvents() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		String path = "sampleData/xml-framework/converter.testxml-noeventprop.properties";
		String convertersdata = FileUtils.readTextFile(new File(path));
		List<String> converterStrings = new Vector<String>();
		for (String converter : convertersdata.split("\n")) {
			if (converter.startsWith("#")) continue; //comment
			converterStrings.add(converter);
		}
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		assertNotNull(converters);
		assertEquals(1, converters.size());
		
		//test misc properties
		Properties props = tester.handleMiscellaneousProperties("a", "b");
		assertNotNull(props);
		assertFalse(props.containsKey("xmlevents"));
		
		//test xml events map
		HashMap<String, DefaultHandler> actual = events.getEvents();
		assertNotNull(actual);
		assertEquals(4, actual.size());
	}
	
	public void testXmlEvents_BadXmlEvents() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		String path = "sampleData/xml-framework/converter.testxml-badeventprop.properties";
		String convertersdata = FileUtils.readTextFile(new File(path));
		List<String> converterStrings = new Vector<String>();
		for (String converter : convertersdata.split("\n")) {
			if (converter.startsWith("#")) continue; //comment
			converterStrings.add(converter);
		}
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		assertNotNull(converters);
		assertEquals(1, converters.size());
		
		//test misc properties
		Properties props = tester.handleMiscellaneousProperties("a", "b");
		assertNotNull(props);
		assertFalse(props.containsKey("xmlevents"));
		
		//test xml events map
		HashMap<String, DefaultHandler> actual = events.getEvents();
		assertNotNull(actual);
		assertEquals(4, actual.size());
	
	}
	
	public void testXmlEvents_CommaDelimEvents() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		String path = "sampleData/xml-framework/converter.testxmlcommadelimevents.properties";
		String convertersdata = FileUtils.readTextFile(new File(path));
		List<String> converterStrings = new Vector<String>();
		for (String converter : convertersdata.split("\n")) {
			if (converter.startsWith("#")) continue; //comment
			converterStrings.add(converter);
		}
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		assertNotNull(converters);
		assertEquals(1, converters.size());
		
		//test xml events map
		HashMap<String, DefaultHandler> actual = events.getEvents();
		assertNotNull(actual);
		assertEquals(6, actual.size());
		assertTrue(actual.containsKey("h1"));
		assertTrue(actual.containsKey("h6"));
	}
	
	public void testXmlEvents_CustomEventsHandler() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		String path = "sampleData/xml-framework/converter.testxml.properties";
		String convertersdata = FileUtils.readTextFile(new File(path));
		List<String> converterStrings = new Vector<String>();
		for (String converter : convertersdata.split("\n")) {
			if (converter.startsWith("#")) continue; //comment
			if (converter.startsWith("XmlTest.0001.xmlevents.property=")) {
				converterStrings.add("XmlTest.0001.xmlevents.property=com.atlassian.uwc.converters.xml.example.TestCustomXmlEvents");
				continue;
			}
			converterStrings.add(converter);
		}
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		assertNotNull(converters);
		assertEquals(1, converters.size());
		
		//test misc properties
		Properties props = tester.handleMiscellaneousProperties("a", "b");
		assertNotNull(props);
		assertTrue(props.containsKey("xmlevents"));
		String xmleventsClass = props.getProperty("xmlevents");
		assertEquals("com.atlassian.uwc.converters.xml.example.TestCustomXmlEvents", xmleventsClass);
		
		//test xml events map
		Class eventsClass = Class.forName(xmleventsClass);
		TestCustomXmlEvents events2 = (TestCustomXmlEvents) eventsClass.newInstance();
		DefaultHandler actual = events2.getEvent("label");
		assertNotNull(actual);
		
		assertNotNull(events2.getTest());
		assertEquals("Testing123", events2.getTest());
	}
	
	public void testXmlEvents_ClearingEvents() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		DefaultXmlEvents events = new DefaultXmlEvents();
		events.clearAll();
		assertTrue(events.getEvents().isEmpty());
		ConfluenceServerSettings settings = new ConfluenceServerSettings();
		String testpropslocation = "test.basic.properties";
		loadSettingsFromFile(settings, testpropslocation);
		
		List<String> converterStrings = new Vector<String>();
		converterStrings.add("XmlTest.0001.xmlevents.property=com.atlassian.uwc.converters.xml.example.TestCustomXmlEvents");
		converterStrings.add("XmlTest.0100.title.xmlevent={tag}page{class}com.atlassian.uwc.converters.xml.example.TitleParser");
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		//test converters
		
		//test misc properties
		Properties props = tester.handleMiscellaneousProperties("a", "b");
		assertNotNull(props);
		assertTrue(props.containsKey("xmlevents"));
		
		//test xml events map
		HashMap<String, DefaultHandler> actual = events.getEvents();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		
		converterStrings = new Vector<String>();
		converterStrings.add("XmlTest.0200.content.xmlevent={tag}content{class}com.atlassian.uwc.converters.xml.DefaultXmlParser");
		converters = tester.createConverters(converterStrings);
		//test xml events map
		actual = events.getEvents();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		
		//XXX if we uncomment (and fix) this, we'll have to figure out a new way to pass properties in unit tests
//		props = tester.handleMiscellaneousProperties("a", "b");
//		assertNotNull(props);
//		assertFalse(props.containsKey("xmlevents"));
		
	}
	
	public void testGetXmlEventTag() {
		String input, expected, actual;
		input = "{tag}page{class}com.atlassian.uwc.converters.xml.example.TitleParser";
		expected = "page";
		actual = tester.getXmlEventTag(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "foobar";
		try {
			actual = tester.getXmlEventTag(input);
			fail();
		} catch (Exception e) {}
	}
	
	public void testGetXmlEventClassname() {
		String input, expected, actual;
		input = "{tag}page{class}com.atlassian.uwc.converters.xml.example.TitleParser";
		expected = "com.atlassian.uwc.converters.xml.example.TitleParser";
		actual = tester.getXmlEventClassname(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "foobar";
		try {
			actual = tester.getXmlEventClassname(input);
			fail();
		} catch (Exception e) {}
	}
	
	public void testConvert_ListCollisions() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/hierarchy/case-sensitivity/A/foo.txt";
		String page2 = "sampleData/hierarchy/case-sensitivity/B/Foo.txt";
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		File file2 = new File(page2);
		assertTrue(file2.exists());
		files.add(file2);
		
		String converterPath = TEST_INPUT_DIR + TEST_CONVERTER_PROPS;
		String converter = null;
		try {
			converter = FileUtils.readTextFile(new File(converterPath));
		} catch (IOException e) {
			fail("Can't load converters from file: " + converterPath);
			e.printStackTrace();
		}
		assertNotNull(converter);
		List<String> converters = new Vector<String>();
		converters.add(converter);
		
		tester.convert(files, converters, settings);
		
		assertEquals(1, tester.getErrors().getErrors().size());
		assertEquals("NAMESPACE_COLLISION " +
				"Potential namespace collision detected for pages: " +
				"sampleData/hierarchy/case-sensitivity/A/foo.txt, " +
				"sampleData/hierarchy/case-sensitivity/B/Foo.txt\n" + 
				"", tester.getErrors().getAllErrorMessages());
	}
	
	public void testConvert_ListCollisions_PropOff() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String collisionDir = "sampleData/hierarchy/case-sensitivity/";
		File file = new File(collisionDir);
		assertTrue(file.exists());
		List<File> files = new Vector<File>();
		files.add(file);
		
		String converterPath = TEST_INPUT_DIR + TEST_CONVERTER_PROPS;
		String converter = null;
		try {
			converter = FileUtils.readTextFile(new File(converterPath));
		} catch (IOException e) {
			fail("Can't load converters from file: " + converterPath);
			e.printStackTrace();
		}
		assertNotNull(converter);
		List<String> converters = new Vector<String>();
		converters.add(converter);
		converters.add("testing.1234.list-collisions.property=false");
		
		tester.convert(files, converters, settings);
		
		assertEquals(0, tester.getErrors().getErrors().size());
	}

	public void testConvert_ListCollisions_Sorting() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/hierarchy/case-sensitivity";
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		List<String> converters = new Vector<String>();
		converters.add("TestHierarchy.0001.nosvn.filter=oo.txt"); //avoiding .svn files
		converters.add("TestHierarchy.0002.filepath-hierarchy-ext.property="); 
		converters.add("TestHierarchy.0003.switch.hierarchy-builder=UseBuilder"); 
		converters.add("TestHierarchy.0005.classname.hierarchy-builder=com.atlassian.uwc.hierarchies.FilepathHierarchy"); 
		converters.add("TestHierarchy.1005-remove-extension.class=com.atlassian.uwc.converters.ChopPageExtensionsConverter"); 
		
		tester.convert(files, converters, settings);
		
		assertEquals("NAMESPACE_COLLISION " +
				"Potential namespace collision detected for pages: " +
				"sampleData/hierarchy/case-sensitivity/A/foo, " +
				"sampleData/hierarchy/case-sensitivity/B/Foo\n" + 
				"", tester.getErrors().getErrors().get(0).toString());
		assertEquals(1, tester.getErrors().getErrors().size());
	}
	
	public void testListCollisions_Basic() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("sampleData/test/");
		Page page2 = new Page(null);
		page2.setName("Foo");
		page2.setPath("sampleData/test2/");
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("sampleData/test/Foo, sampleData/test2/Foo", actual.get(0));
	}
	
	public void testListCollisions_CaseSens() {
		Page page1 = new Page(null);
		page1.setName("FOO");
		page1.setPath("sampleData/test/");
		Page page2 = new Page(null);
		page2.setName("foo");
		page2.setPath("sampleData/test/");
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("sampleData/test/FOO, sampleData/test/foo", actual.get(0));
	}
	
	public void testListCollisions_PropOff() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("sampleData/test/");
		Page page2 = new Page(null);
		page2.setName("Foo");
		page2.setPath("sampleData/test2/");
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);

		Properties properties = tester.handleMiscellaneousProperties("", "");
		properties.put("list-collisions", "false");
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(0, actual.size());
	}
	
	public void testListCollisions_Multiple() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("sampleData/test/");
		Page page2 = new Page(null);
		page2.setName("Foo");
		page2.setPath("sampleData/test2/");
		Page page3 = new Page(null);
		page3.setName("foo");
		page3.setPath("sampleData/test2/");
		Page page4 = new Page(null);
		page4.setName("Bar");
		page4.setPath("abc/");
		Page page5 = new Page(null);
		page5.setName("testing 123");
		page5.setPath("abc/");
		Page page6 = new Page(null);
		page6.setName("testing 123");
		page6.setPath("abc/");
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page4);
		pages.add(page5);
		pages.add(page6);
		
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("sampleData/test/Foo, sampleData/test2/Foo, sampleData/test2/foo", actual.get(0));
		assertEquals("abc/testing 123, abc/testing 123", actual.get(1));
	}
	
	public void testListCollisions_WithAutoDetect() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("space1/");  
		Page page2 = new Page(null);
		page2.setName("Foo");
		page2.setPath("space2/");
		Page page3 = new Page(null);
		page3.setName("foo");
		page3.setPath("space2/");
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);

		tester.handleAutoDetectSpacekeys("", "true");
		
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("space2/Foo, space2/foo", actual.get(0));
	}

	public void testListCollisions_WithPageHistory() {
		Page page1 = new Page(null); //version 1
		page1.setName("Foo");
		page1.setPath("space1/"); 
		page1.setVersion(1);
		Page page2 = new Page(null); //version 2 of the same page as page 1
		page2.setName("Foo");
		page2.setPath("space2/");
		page2.setVersion(2);
		Page page3 = new Page(null); //version 1 of what would have been a different page
		page3.setName("Foo");
		page3.setPath("space3/");
		page3.setVersion(1);
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);

		tester.handlePageHistoryProperty("Mediawiki.0050.switch.page-history-preservation","true");
		tester.handlePageHistoryProperty("Mediawiki.0051.suffix.page-history-preservation","-#.txt");
		
		Vector<String> actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("space1/Foo, space3/Foo", actual.get(0));
		
		
		//more pages so we can test the double sorting more completely
		Page page4 = new Page(null);
		page4.setName("Bar");
		page4.setPath("a/"); 
		page4.setVersion(1);
		Page page5 = new Page(null);
		page5.setName("Bar");
		page5.setPath("a/");
		page5.setVersion(2);
		Page page6 = new Page(null);
		page6.setName("Bar");
		page6.setPath("a/");
		page6.setVersion(3);
		Page page7 = new Page(null);
		page7.setName("Bar");
		page7.setPath("B/");
		page7.setVersion(1);
		
		pages.clear();
		pages.add(page1);
		pages.add(page4);
		pages.add(page2);
		pages.add(page5);
		pages.add(page6);
		pages.add(page3);
		pages.add(page7);
		actual = tester.listCollisions(pages);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("a/Bar, B/Bar", actual.get(0));
		assertEquals("space1/Foo, space3/Foo", actual.get(1));

	}
	
	public void testGetCollisionComparisonString() {
		//basic
		Page page1 = new Page(null);
		page1.setName("TESTing");
		page1.setPath("ABC/");
		String expected = "testing";
		String actual = tester.getCollisionComparisonString(page1);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with auto detect
		tester.handleAutoDetectSpacekeys("", "true");
		expected = "abc/testing";
		actual = tester.getCollisionComparisonString(page1);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testSendAttachmentRemoteAPI () {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/engine/attachments/SampleEngine-InputAttachments.txt"; 
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		List<String> converters = new Vector<String>();
		converters.add("Test.0001.nosvn.filter=com.atlassian.uwc.filters.NoSvnFilter"); //avoiding .svn files
		converters.add("Test.0002.addattachment.class=com.atlassian.uwc.converters.test.TestAttachments");
		tester.convert(files, converters, settings);

		//get page
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		String pageTitle = "SampleEngine-InputAttachments.txt";
		String id = null;
		try {
			id = broker.getPageIdFromConfluence(csettings, csettings.spaceKey, pageTitle);
			startPage = broker.getPage(csettings, id);
			List<AttachmentForXmlRpc> attachments = broker.getAttachments(csettings, id);
			assertNotNull(attachments);
			assertEquals(1, attachments.size());
			assertEquals("cow.jpg", attachments.get(0).getFileName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			//clean up after the test
			deletePage(id, csettings);
		}

	}
	
	public void testSendAttachmentWebdav() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/engine/attachments/SampleEngine-InputAttachments.txt"; 
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		List<String> converters = new Vector<String>();
		converters.add("Test.0001.nosvn.filter=com.atlassian.uwc.filters.NoSvnFilter"); //avoiding .svn files
		converters.add("Test.0002.addattachment.class=com.atlassian.uwc.converters.test.TestAttachments");
		converters.add("Test.0003.attachments-use-webdav.property=true");
		converters.add("Test.0004.webdav-path.property=plugins/servlet/confluence/default/Global/");
		tester.convert(files, converters, settings);

		//get page
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		String pageTitle = "SampleEngine-InputAttachments.txt";
		String id = null;
		try {
			id = broker.getPageIdFromConfluence(csettings, csettings.spaceKey, pageTitle);
			startPage = broker.getPage(csettings, id);
			List<AttachmentForXmlRpc> attachments = broker.getAttachments(csettings, id);
			assertNotNull(attachments);
			assertEquals(1, attachments.size());
			assertEquals("cow.jpg", attachments.get(0).getFileName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			//clean up after the test
			deletePage(id, csettings);
		}
	}
	
	public void testSendAttachmentWebdav_QuestionMarkTitle() { //UWC-407
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/engine/attachments/SampleEngine-InputAttachments.txt"; 
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		List<String> converters = new Vector<String>();
		converters.add("Test.0001.nosvn.filter=com.atlassian.uwc.filters.NoSvnFilter"); //avoiding .svn files
		converters.add("Test.0002.addattachment.class=com.atlassian.uwc.converters.test.TestAttachments");
		converters.add("Test.0003.attachments-use-webdav.property=true");
		converters.add("Test.0004.webdav-path.property=plugins/servlet/confluence/default/Global/");
		converters.add("Test.0005.addQMtoTitle.class=com.atlassian.uwc.converters.test.TestQMTitle");
		tester.convert(files, converters, settings);

		//get page
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		PageForXmlRpc startPage = null;
		String pageTitle = "SampleEngine-InputAttachments?";
		String id = null;
		try {
			id = broker.getPageIdFromConfluence(csettings, csettings.spaceKey, pageTitle);
			startPage = broker.getPage(csettings, id);
			List<AttachmentForXmlRpc> attachments = broker.getAttachments(csettings, id);
			assertNotNull(attachments);
			assertEquals(1, attachments.size());
			assertEquals("cow.jpg", attachments.get(0).getFileName());
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		} finally {
			//clean up after the test
			deletePage(id, csettings);
		}
	}
	
	public void testUsingWebdav() {
		tester.initConversion();
		Properties props = tester.handleMiscellaneousProperties("wiki.1234.attachments-use-webdav.property", "true");
		assertTrue(tester.usingWebdav());
		tester.handleMiscellaneousProperties("wiki.1234.attachments-use-webdav.property", "false");
		assertFalse(tester.usingWebdav());
		tester.handleMiscellaneousProperties("wiki.1234.attachments-use-webdav.property", "notabool123");
		assertFalse(tester.usingWebdav());
		tester.initConversion();
		assertFalse(tester.usingWebdav());
	}
	
	public void testGetWebdavPath() {
		tester.initConversion();
		Properties props = tester.handleMiscellaneousProperties("wiki.1234.webdav-path.property", "plugins/servlet/confluence/default/Global/");
		String actual = tester.getWebdavPath();
		assertNotNull(actual);
		assertEquals("plugins/servlet/confluence/default/Global/", actual);
		
		tester.handleMiscellaneousProperties("wiki.1234.webdav-path.property", "foobar");
		actual = tester.getWebdavPath();
		assertNotNull(actual);
		assertEquals("foobar", actual);
		
		tester.initConversion();
		actual = tester.getWebdavPath();
		assertNotNull(actual);
		assertEquals("plugins/servlet/webdav/Global/", actual);
		
	}
	
	public void testRemoteApiError() {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		String page1 = "sampleData/engine/SampleEngine-Input1.txt"; 
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		List<String> converters = new Vector<String>();
		try {
			tester.convert(files, converters, settings);
			fail("The Remote API must be off for this test to work.");
		} catch (IllegalArgumentException e) {
			String actual = e.getMessage();
			String expected = "Testing Connection Settings... FAILURE:\n" + 
					"The API returned a 403 (Forbidden) error.\n" + 
					"Make sure the Remote API is turned on.";
			assertNotNull(actual);
			assertTrue(actual.contains(expected));
		}
		
	}
	
	public void testSpacekeyProperty() {
		List<File> pages = new Vector<File>();
		List<String> converterStrings = new Vector<String>();
		pages.add(new File("sampleData/engine/SampleEngine-Input1.txt"));
		converterStrings.add("Testing.1234.test.java-regex=test{replace-with}foobar");
		Properties props = tester.handleMiscellaneousProperties("test", "test");
		UWCUserSettings settings = new UWCUserSettings();
		settings.setSpace("fooBAR");
		tester.setSettings(settings );
		
		assertFalse(props.containsKey("spacekey"));
		tester.convert(pages, converterStrings, false, null);
		props = null;
		props = tester.handleMiscellaneousProperties("test", "test");
		assertTrue(props.containsKey("spacekey"));
		assertEquals("fooBAR", props.getProperty("spacekey"));
	}
	
	
	public void testAttachUploadComment() throws XmlRpcException, IOException {
		String location = TEST_SETTING_DIR + TEST_PROPS;	
		UWCUserSettings settings = new UWCUserSettings(location);
		tester.getState(settings);
		ConfluenceServerSettings csettings = new ConfluenceServerSettings();
		Properties props = loadSettingsFromFile(csettings, "test.basic.properties");
		//check they're loaded
		assertEquals(csettings.spaceKey, settings.getSpace());
		settings.setWikitype("Unit Test");
		
		//add the page to the server, so we can get the page id
		String page1 = "sampleData/engine/SampleEngine-Input1.txt"; 
		List<File> files = new Vector<File>();
		File file = new File(page1);
		assertTrue(file.exists());
		files.add(file);
		
		String converterPath = TEST_INPUT_DIR + "converter.testing-comment.properties";
		String converter = null;
		try {
			converter = FileUtils.readTextFile(new File(converterPath));
		} catch (IOException e) {
			fail("Can't load converters from file: " + converterPath);
			e.printStackTrace();
		}
		assertNotNull(converter);
		List<String> converters = new Vector<String>();
		String[] each = converter.split("\n");
		for (String c : each) {
			converters.add(c);
		}

		tester.convert(files, converters, settings);

		//try just sending an attachment and examine the comment used
		XmlRpcClient client = getXmlRpcClient(csettings);
		Hashtable page = getPage(client, "SampleEngine-Input1.txt", csettings.spaceKey, csettings);
		String pageId = (String) page.get("id");
		RemoteWikiBroker broker = RemoteWikiBroker.getInstance();
		File attfile = new File("sampleData/engine/attachments/cow.jpg");
		AttachmentForXmlRpc attachment = tester.sendAttachment(attfile, broker, pageId, csettings);
		String actual = attachment.getComment();
		String expected = "Foo Bar 123!";
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testDetermineSpacekey() {
		tester.handleMiscellaneousProperties("Mywiki.0001.auto-detect-ignorable-ancestors.property", 
				"/Users/laura/Code/Subversion/uwc-current/devel/sampleData/engine/autodetect/");
		
		String path = "sampleData/engine/autodetect/foo/test.txt";
		File file = new File(path);
		Page page = new Page(file);
		page.setPath(path);
		page.setName(file.getName());
		String actual = tester.determineSpaceKey(page);
		String expected = "foo";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		path = "sampleData/engine/autodetect/foo/subspace/test.txt";
		file = new File(path);
		page = new Page(file);
		page.setPath(path);
		page.setName(file.getName());
		actual = tester.determineSpaceKey(page);
		expected = "foosubspace";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testCreateFilter_class() throws InstantiationException, IllegalAccessException {
		ConverterEngine tester = new ConverterEngine(); //we need to reset the filters
		tester.handleFilters("test.123.something.filter", "com.atlassian.uwc.filters.NoSvnFilter");
		FileFilter actual = tester.createFilter(null);
		
		List<File> pages = new Vector<File>();
		File dir = new File("sampleData/filter/junit_resources/");
		File[] files = dir.listFiles(actual);
		assertNotNull(files);
		assertTrue(files.length == 3);
		int count = 0;
		for (File file : files) {
			if (file.getName().equals("foo.txt") || 
					file.getName().equals("foo.xml") ||
					file.getName().equals("foo.jpg")) count++;
		}
		assertEquals(3, count);
	}
	
	public void testCreateFilter_endswith() throws InstantiationException, IllegalAccessException {
		ConverterEngine tester = new ConverterEngine(); //we need to reset the filters
		//endswith patterns accept files ending in the pattern and all directories
		tester.handleFilters("test.123.something.filter", ".txt"); 
		FileFilter actual = tester.createFilter(null);
		
		List<File> pages = new Vector<File>();
		File dir = new File("sampleData/filter/junit_resources/");
		File[] files = dir.listFiles(actual);
		assertNotNull(files);
		assertTrue(files.length == 2);
		int count = 0;
		for (File file : files) {
			if (file.getName().equals(".svn") || file.getName().equals("foo.txt")) count++;
		}
		assertEquals(2, count);
	}
	
	public void testCreateFilter_settings() { //endswith pattern in the confluenceSettings
		ConverterEngine tester = new ConverterEngine(); //we need to reset the filters
		FileFilter actual = tester.createFilter(".xml");
		
		List<File> pages = new Vector<File>();
		File dir = new File("sampleData/filter/junit_resources/");
		File[] files = dir.listFiles(actual);
		assertNotNull(files);
		assertTrue(files.length == 2);
		int count = 0;
		for (File file : files) { 
			if (file.getName().equals("foo.xml") || file.getName().equals(".svn")) count++;
		}
		assertEquals(2, count);
	}
	
	public void testCreateFilter_multiple() throws InstantiationException, IllegalAccessException {
		ConverterEngine tester = new ConverterEngine(); //we need to reset the filters
		tester.handleFilters("test.123.something.filter", "com.atlassian.uwc.filters.NoSvnFilter");
		tester.handleFilters("test.123.something.filter", ".txt");
		FileFilter actual = tester.createFilter(".xml");
		
		List<File> pages = new Vector<File>();
		File dir = new File("sampleData/filter/junit_resources/");
		File[] files = dir.listFiles(actual);
		assertNotNull(files);
		assertTrue(files.length == 2);
		int count = 0;
		for (File file : files) {
			if (file.getName().equals("foo.xml") || file.getName().equals("foo.txt")) count++;
		}
		assertEquals(2, count);
	}
	
	
	
	/* XXX Converter Engine Tests from Version 2 */

	public void testGetPageName() {
		//converting a file
		
		String input = "Main_Page_Discussion.txt";
		String expected = "Main_Page_Discussion.txt";
		String actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
			
		//converting a directory, one level up
		input = "Pages/Main_Page_Discussion.txt";
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//converting a directory, two levels up
		input = "exportedpages/Pages/Main_Page_Discussion.txt";
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	
	public void testCreateConverters() {
		//this is just a simple size check
		
		//normal list
		List<String> input = new ArrayList<String>();
		input.add(CONVERTER1);
		input.add(CONVERTER2);
		input.add(CONVERTER3);

		List<Converter> actual = tester.createConverters(input);
		assertNotNull(actual);
		int expectedSize = 3; //I've put three in
		//expectedSize += 2; //plus the required ones
		int actualSize = actual.size();
		assertEquals(expectedSize, actualSize);
		
		//list with a non-converter property: hierarchy-builder, or page-history-preservation
		input = new ArrayList<String>();
		input.add(CONVERTER1);
		input.add(CONVERTER2);
		input.add(CONVERTER3);
		input.add(NONCONVERTER_HIERARCHY);
		actual = tester.createConverters(input);
		assertNotNull(actual);
		actualSize = actual.size();
		assertEquals(expectedSize, actualSize);

		input = new ArrayList<String>();
		input.add(CONVERTER1);
		input.add(CONVERTER2);
		input.add(CONVERTER3);
		input.add(NONCONVERTER_PAGEHISTORY);
		actual = tester.createConverters(input);
		assertNotNull(actual);
		actualSize = actual.size();
		assertEquals(expectedSize, actualSize);

	}
	
	public void testGetConverterFromString() {
		String input = CONVERTER1;
		String[] expected = CONVERTER1.split("=");
		Converter actual = tester.getConverterFromString(input);
		assertNotNull(actual);
		assertEquals(expected[0], actual.getKey());
		assertEquals(expected[1], actual.getValue());
		
		input = NONCONVERTER_PAGEHISTORY;
		actual = tester.getConverterFromString(input);
		assertNull(actual);
	}
	
	public void testHandlePageHistoryProperty() {
		//switch test
		String truehistory = NONCONVERTER_PAGEHISTORY;
		String input[] = truehistory.split("=");
		tester.handlePageHistoryProperty(input[0], input[1]);
		boolean expected = true;
		boolean actual = tester.isHandlingPageHistories();
		assertEquals(expected, actual);

		String falsehistory = NONCONVERTER_PAGEHISTORY.replaceAll("true", "false");
		String input2[] = falsehistory.split("=");
		tester.handlePageHistoryProperty(input2[0], input2[1]);
		expected = false;
		actual = tester.isHandlingPageHistories();
		assertEquals(expected, actual);

		//suffix test
		assertNull(tester.getPageHistorySuffix());
		String input3[] = NONCONVERTER_PAGEHISTORY_SUFFIX.split("=");
		tester.handlePageHistoryProperty(input3[0], input3[1]);
		String expString = "-v#.txt";
		String actString = tester.getPageHistorySuffix();
		assertEquals(expString, actString);
	}
	
	public void testIsNonConverterProperty() {
		String input = CONVERTER1;
		boolean expected = false;
		boolean actual = tester.isNonConverterProperty(input);
		assertEquals(expected, actual);
		
		input = CONVERTER2;
		actual = tester.isNonConverterProperty(input);
		assertEquals(expected, actual);
		
		input = NONCONVERTER_HIERARCHY;
		expected = true;
		actual = tester.isNonConverterProperty(input);
		assertEquals(expected, actual);

		input = NONCONVERTER_PAGEHISTORY;
		actual = tester.isNonConverterProperty(input);
		assertEquals(expected, actual);

		input = NONCONVERTER_ILLEGAL;
		expected = true;
		actual = tester.isNonConverterProperty(input);
		assertEquals(expected, actual);
	}
	
	public void testPreserveHistories() {
		//create some stub objects
		String pagename = "pagename";
		String filename1 = pagename+"-v1.txt";
		String filename2 = pagename+"-v2.txt";
		File file1 = new File(filename1);
		File file2 = new File(filename2);
		Page page1 = new Page(file1);
		Page page2 = new Page(file2);

		//assert that both pages are using the default version
		int expectedDefaultVersion = 1;
		assertEquals(expectedDefaultVersion, page1.getVersion());
		assertEquals(expectedDefaultVersion, page2.getVersion());
		
		//test what happens when suffix is null
		Page newPage1 = tester.preserveHistory(page1, filename1);
		Page newPage2 = tester.preserveHistory(page2, filename2);
		assertNotNull(newPage1);
		assertNotNull(newPage2);
		assertEquals(1, newPage1.getVersion());
		assertEquals(1, newPage2.getVersion());
		
		
		//now test that histories are preserved
		tester.handlePageHistoryProperty("wiki.suffix.page-history-preservation", "-v#.txt");
		newPage1 = tester.preserveHistory(page1, filename1);
		newPage2 = tester.preserveHistory(page2, filename2);
		assertNotNull(newPage1);
		assertNotNull(newPage2);
		assertEquals(1, newPage1.getVersion());
		assertEquals(2, newPage2.getVersion());
		//in actuality newPageN and pageN are the same, so test that
		assertEquals(1, page1.getVersion());
		assertEquals(2, page2.getVersion());
		
	}
	public void testSortByHistory() {
//		create some stub objects
		String pagename = "pagename";
		String filename1 = pagename+"-1.tiki";
		String filename2 = pagename+"-2.tiki";
		String filename3 = pagename+"-3.tiki";
		String filename4 = pagename+"-4.tiki";
		String suffix = "-#.tiki";
		File file1 = new File(filename1);
		File file2 = new File(filename2);
		File file3 = new File(filename3);
		File file4 = new File(filename4);
		Page page1 = new Page(file1);
		Page page2 = new Page(file2);
		Page page3 = new Page(file3);
		Page page4 = new Page(file4);
		page1.setName(filename1);
		page2.setName(filename2);
		page3.setName(filename3);
		page4.setName(filename4);
		
		//preserve the history so we can test this
		tester.handlePageHistoryProperty("wiki.suffix.page-history-preservation", suffix);
		tester.preserveHistory(page1, filename1);
		tester.preserveHistory(page2, filename2);
		tester.preserveHistory(page3, filename3);
		tester.preserveHistory(page4, filename4);

		//create unsorted list
		List<Page> unsorted = new ArrayList<Page>();
		unsorted.add(page3);
		unsorted.add(page4);
		unsorted.add(page1);
		unsorted.add(page2);
		
		
		List<Page> sorted = tester.sortByHistory(unsorted);
		assertNotNull(sorted);
		
		//test size - should be the same
		assertEquals("Size of lists isn't the same.", unsorted.size(), sorted.size());
		
		for (int i = 1; i <= sorted.size(); i++) {
			int index = i-1;
			Page page = sorted.get(index);
			int expected = i;
			int actual = page.getVersion();
			assertEquals(
					"Sort out of order. Was: " + actual + ", but expected " + expected, 
					expected, actual);
			String expName = pagename;
			String actName = page.getName();
			assertEquals(
					"Pagename not the same!", expName, actName);
			
		}
		
	}
	
	public void testSetPageHistorySuffix() {
		//valid
		String input = "-#.txt";
		boolean expected = true;
		boolean actual = tester.setPageHistorySuffix(input);
		assertEquals(expected, actual);
		String actString = tester.getPageHistorySuffix();
		assertEquals(input, actString);
		//invalid
		input = ".txt"; //no number!
		expected = false;
		actual = tester.setPageHistorySuffix(input);
		assertEquals(expected, actual);
		actString = tester.getPageHistorySuffix();
		assertNull(actString);
		
	}
	
	public void testPreserveHistoriesOver10() {
		//create some stub objects
		String pagename = "pagename";
		String filename1 = pagename+"-v1.txt";
		String filename2 = pagename+"-v2.txt";
		String filename3 = pagename+"-v3.txt";
		String filename4 = pagename+"-v4.txt";
		String filename5 = pagename+"-v5.txt";
		String filename6 = pagename+"-v6.txt";
		String filename7 = pagename+"-v7.txt";
		String filename8 = pagename+"-v8.txt";
		String filename9 = pagename+"-v9.txt";
		String filename10 = pagename+"-v10.txt";
		String filename11 = pagename+"-v11.txt";
		String filename12 = pagename+"-v12.txt";
		String filename130 = pagename+"-v130.txt";
		File file1 = new File(filename1);
		File file2 = new File(filename2);
		File file3 = new File(filename3);
		File file4 = new File(filename4);
		File file5 = new File(filename5);
		File file6 = new File(filename6);
		File file7 = new File(filename7);
		File file8 = new File(filename8);
		File file9 = new File(filename9);
		File file10 = new File(filename10);
		File file11 = new File(filename11);
		File file12 = new File(filename12);
		File file130 = new File(filename130);
		
		Page page1 = new Page(file1);
		Page page2 = new Page(file2);
		Page page3 = new Page(file3);
		Page page4 = new Page(file4);
		Page page5 = new Page(file5);
		Page page6 = new Page(file6);
		Page page7 = new Page(file7);
		Page page8 = new Page(file8);
		Page page9 = new Page(file9);
		Page page10 = new Page(file10);
		Page page11 = new Page(file11);
		Page page12 = new Page(file12);
		Page page130 = new Page(file130);
		

		//assert that both pages are using the default version
		int expectedDefaultVersion = 1;
		assertEquals(expectedDefaultVersion, page1.getVersion());
		assertEquals(expectedDefaultVersion, page2.getVersion());
		assertEquals(expectedDefaultVersion, page3.getVersion());
		assertEquals(expectedDefaultVersion, page4.getVersion());
		assertEquals(expectedDefaultVersion, page5.getVersion());
		assertEquals(expectedDefaultVersion, page6.getVersion());
		assertEquals(expectedDefaultVersion, page7.getVersion());
		assertEquals(expectedDefaultVersion, page8.getVersion());
		assertEquals(expectedDefaultVersion, page9.getVersion());
		assertEquals(expectedDefaultVersion, page10.getVersion());
		assertEquals(expectedDefaultVersion, page11.getVersion());
		assertEquals(expectedDefaultVersion, page12.getVersion());
		assertEquals(expectedDefaultVersion, page130.getVersion());
		
		//test that histories were preserved
		tester.handlePageHistoryProperty("wiki.switch.page-history-preservation", "true");
		tester.handlePageHistoryProperty("wiki.suffix.page-history-preservation", "-v#.txt");
		Page newPage1 = tester.preserveHistory(page1, filename1);
		Page newPage2 = tester.preserveHistory(page2, filename2);
		Page newPage3 = tester.preserveHistory(page3, filename3);
		Page newPage4 = tester.preserveHistory(page4, filename4);
		Page newPage5 = tester.preserveHistory(page5, filename5);
		Page newPage6 = tester.preserveHistory(page6, filename6);
		Page newPage7 = tester.preserveHistory(page7, filename7);
		Page newPage8 = tester.preserveHistory(page8, filename8);
		Page newPage9 = tester.preserveHistory(page9, filename9);
		Page newPage10 = tester.preserveHistory(page10, filename10);
		Page newPage11 = tester.preserveHistory(page11, filename11);
		Page newPage12 = tester.preserveHistory(page12, filename12);
		Page newPage130 = tester.preserveHistory(page130, filename130);
		
//		now test that histories are preserved
		
		//should not be null
		assertNotNull(newPage1);
		assertNotNull(newPage2);
		assertNotNull(newPage3);
		assertNotNull(newPage4);
		assertNotNull(newPage5);
		assertNotNull(newPage6);
		assertNotNull(newPage7);
		assertNotNull(newPage8);
		assertNotNull(newPage9);
		assertNotNull(newPage10);
		assertNotNull(newPage11);
		assertNotNull(newPage12);
		assertNotNull(newPage130);
		
		//check versions
		assertEquals(1, newPage1.getVersion());
		assertEquals(2, newPage2.getVersion());
		assertEquals(3, newPage3.getVersion());
		assertEquals(4, newPage4.getVersion());
		assertEquals(5, newPage5.getVersion());
		assertEquals(6, newPage6.getVersion());
		assertEquals(7, newPage7.getVersion());
		assertEquals(8, newPage8.getVersion());
		assertEquals(9, newPage9.getVersion());
		assertEquals(10, newPage10.getVersion());
		assertEquals(11, newPage11.getVersion());
		assertEquals(12, newPage12.getVersion());
		assertEquals(130, newPage130.getVersion());

		//in actuality newPageN and pageN are the same, so test that
		assertEquals(1, page1.getVersion());
		assertEquals(2, page2.getVersion());
		assertEquals(3, page3.getVersion());
		assertEquals(4, page4.getVersion());
		assertEquals(5, page5.getVersion());
		assertEquals(6, page6.getVersion());
		assertEquals(7, page7.getVersion());
		assertEquals(8, page8.getVersion());
		assertEquals(9, page9.getVersion());
		assertEquals(10, page10.getVersion());
		assertEquals(11, page11.getVersion());
		assertEquals(12, page12.getVersion());
		assertEquals(130, page130.getVersion());

	}
	
	public void testIsHierarchy() {
		String input = "Mywiki.0001.switch.hierarchy-builder=UseBuilder";
		String[] inputs = input.split("=");
		boolean expected = true;
		boolean actual = tester.isHierarchySwitch(inputs[0]);
		assertEquals(expected, actual);
		
		input = "Mywiki.0001.switch.hierarchy-builder=Default";
		inputs = input.split("=");
		expected = true;
		actual = tester.isHierarchySwitch(inputs[0]);
		assertEquals(expected, actual);
		
		input = "Mywiki.0001.switch.hierarchy-builder=UsePagenames";
		inputs = input.split("=");
		expected = true;
		actual = tester.isHierarchySwitch(inputs[0]);
		assertEquals(expected, actual);

	}
	
	public void testHandleNonConverterHierarchyBuilder() {
		//test use builder switch
		String input = "Mywiki.0001.switch.hierarchy-builder=UseBuilder";
		//haven't set handler yet, so should be default
		ConverterEngine.HierarchyHandler handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.DEFAULT, handler);
		//setting the handler
		tester.handleNonConverterProperty(input);
		handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.HIERARCHY_BUILDER, handler);
		
		//test set builder object
		HierarchyBuilder actualBuilder = tester.getHierarchyBuilder();
		assertNull(actualBuilder);
		
		input = "Mywiki.0001.blah.hierarchy-builder=com.atlassian.uwc.hierarchies.BaseHierarchy";
		tester.handleNonConverterProperty(input);
		actualBuilder = tester.getHierarchyBuilder();
		assertNotNull(actualBuilder);
		if (actualBuilder instanceof com.atlassian.uwc.hierarchies.BaseHierarchy) 
			log.info("Is the correct object");
		else 
			fail("Not a BaseHierarchy!");
	}
	
	public void testHandleNonConverterDisableIllegalHandling() {
//		test use switch
		String input = "Mywiki.0001.switch.illegal-handling=false";
		//haven't set handler yet, so should be default
		ConverterEngine.HierarchyHandler handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.DEFAULT, handler);
		//setting the handler
		assertTrue(tester.isIllegalHandlingEnabled());
		tester.handleNonConverterProperty(input);
		assertFalse(tester.isIllegalHandlingEnabled());
	}
	
	public void testUsingPagenameHierarchy() {
		ConverterEngine.HierarchyHandler handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.DEFAULT, handler);

		String input = "Mywiki.0001.switch.hierarchy-builder=Default";
		tester.handleNonConverterProperty(input);
		handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.DEFAULT, handler);
		
		input = "Mywiki.0001.switch.hierarchy-builder=UsePagenames";
		tester.handleNonConverterProperty(input);
		handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.PAGENAME_HIERARCHIES, handler);

		input = "Mywiki.0001.switch.hierarchy-builder=UseBuilder";
		tester.handleNonConverterProperty(input);
		handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.HIERARCHY_BUILDER, handler);

	}
	
	public void testGetPagename() {
		String input = "Parent/Page";
		
		//default
		String model = "Mywiki.0001.switch.hierarchy-builder=Default";
		tester.handleNonConverterProperty(model);
		String expected = "Page";
		String actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//pagename hierarchy
		model = "Mywiki.0001.switch.hierarchy-builder=UsePagenames";
		tester.handleNonConverterProperty(model);
		expected = "Parent -- Page";
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//hierarchy builder
		model = "Mywiki.0001.switch.hierarchy-builder=UseBuilder";
		tester.handleNonConverterProperty(model);
		expected = "Page"; 
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPagesWithIllegalNameChecking() {
		String pagename1 = "Page 1";
		String pagenameBad2 = "Page<2";
		String expectedBadName = "Page_2";
		
		String pageContents1 = "Before:\n" +
				"[Page_2]\n" +
				"After";
		String pageContents2 = "Testing";
		String expectedGoodContents = "Before:\n" +
			"[Page_2]\n" +
			"After";
		
		Page page1 = new Page(new File(""));
		page1.setName(pagename1);
		page1.setOriginalText(pageContents1);
		Page page2 = new Page(new File(""));
		page2.setName(pagenameBad2);
		page2.setOriginalText(pageContents2);
		
		List<Page> inputPages = new ArrayList<Page>();
		inputPages.add(page1);
		inputPages.add(page2);

		//XXX Why twice?
		tester.convertWithRequiredConverters(inputPages);
		tester.convertWithRequiredConverters(inputPages);
		
		String act2Name = page2.getName();
		String act1Content = page1.getConvertedText();
		
		assertNotNull(act2Name);
		assertNotNull(act1Content);
		
		assertEquals(expectedBadName, act2Name);
		assertEquals(expectedGoodContents, act1Content);
		
	}
	
	public void testExternalLink_WithIllegalPageNames() {
		String input = "External link (to other websites)\n" +
		"[http://www.example.com]\n" +
		"[display text|http://www.example.com]\n" +
		"[display text|http://www.example.com]\n" +
		"[Secure|https://security.com]\n";
		
		String expected = input; 
		
		Page page1 = new Page(new File(""));
		page1.setName("something");
		page1.setOriginalText(input);
		List<Page> inputPages = new ArrayList<Page>();
		inputPages.add(page1);

		List<String> converterStrings = new ArrayList<String>(); 
		List<Converter> converters = tester.createConverters(converterStrings);

		Page act1Page = tester.convertPage(converters, page1);
		String act1Content = act1Page.getConvertedText();
		assertNotNull(act1Content);
		assertEquals(expected, act1Content);
	}
	

	public void testConvertWithRequiredConverters() {
		//creating stubs
		Page page1 = new Page(null); //arg must be NULL, otherwise engine fails on read
		Page page2 = new Page(null);
		Page page3 = new Page(null);
		Page page4 = new Page(null);
		Page page5 = new Page(null);
		Page page6 = new Page(null);
		Page page7 = new Page(null); // test real hashes
		Page page8 = new Page(null); // test real carats
		
		String name1 = "Testing";
		String name2 = "Testing:1#2;3";
		String name3 = "Testing Escaped Brackets";
		String name4 = "Too [ManyBrackets";
		String name5 = "Testing%20Entities";
		String name6 = "Testing Code Blocks";
		String name7 = "Testing Hashes";
		String name8 = "Testing Carats";
		String content1 = "See [Testing:1#2;3]"; //needs to change
		String content2 = "See [alias|Testing]"; //needs to _not_ change
		String content3 = "Testing \\[Escaped [brackets]";
		String content4 = "Testing [Too [ManyBrackets]";
		String content5 = "[Testing%20Entities]";
		String content6 = "[Testing Escaped Brackets] and {code} [[don't replace here]] {code}";
		String content7 = "[Testing Escaped Brackets#anchor]";
		String content8 = "[^file.gif]";
		
		page1.setName(name1);
		page1.setOriginalText(content1);
		page1.setConvertedText(content1); //ConverterEngine expects this
		page2.setName(name2);
		page2.setOriginalText(content2);
		page2.setConvertedText(content2);
		page3.setName(name3);
		page3.setOriginalText(content3);
		page3.setConvertedText(content3);
		page4.setName(name4);
		page4.setOriginalText(content4);
		page4.setConvertedText(content4);
		page5.setName(name5);
		page5.setOriginalText(content5);
		page5.setConvertedText(content5);
		page6.setName(name6);
		page6.setOriginalText(content6);
		page6.setConvertedText(content6);
		page7.setName(name7);
		page7.setOriginalText(content7);
		page7.setConvertedText(content7);
		page8.setName(name8);
		page8.setOriginalText(content8);
		page8.setConvertedText(content8);


		ArrayList<Page> pages = new ArrayList<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page4);
		pages.add(page5);
		pages.add(page6);
		pages.add(page7);
		pages.add(page8);
		
		//create expectations
		int expectedSize = pages.size();
		String expName1 = name1;
		String expName2 = "Testing.1No.2.3";
		String expCon1 = "See [" + expName2 + "]";
		String expCon2 = content2;
		String expName3 = name3;
		String expCon3 = content3;
		String expName4 = "Too (ManyBrackets";
		String expCon4 = "Testing [Too (ManyBrackets]";
		String expName5 = "Testing Entities";
		String expCon5 = "[Testing Entities]";
		String expName6 = name6;
		String expCon6 = content6;
		String expName7 = name7;
		String expCon7 = content7;
		String expName8 = name8;
		String expCon8 = content8;
		
		//set urldecoding property
		tester.handleNonConverterProperty("abc.0000.illegalnames-urldecode.property=true");
		
		//do the tests
		tester.convertWithRequiredConverters(pages);

		assertNotNull(pages);
		assertEquals(expectedSize, pages.size());

		Page actPage1 = pages.remove(0);
		Page actPage2 = pages.remove(0);
		Page actPage3 = pages.remove(0);
		Page actPage4 = pages.remove(0);
		Page actPage5 = pages.remove(0);
		Page actPage6 = pages.remove(0);
		Page actPage7 = pages.remove(0);
		Page actPage8 = pages.remove(0);
		
		assertTrue(pages.isEmpty());

		String actName1 = actPage1.getName();
		String actCon1 = actPage1.getConvertedText();
		String actName2 = actPage2.getName();
		String actCon2 = actPage2.getConvertedText();
		String actName3 = actPage3.getName();
		String actCon3 = actPage3.getConvertedText();
		String actName4 = actPage4.getName();
		String actCon4 = actPage4.getConvertedText();
		String actName5 = actPage5.getName();
		String actCon5 = actPage5.getConvertedText();
		String actName6 = actPage6.getName();
		String actCon6 = actPage6.getConvertedText();
		String actName7 = actPage7.getName();
		String actCon7 = actPage7.getConvertedText();
		String actName8 = actPage8.getName();
		String actCon8 = actPage8.getConvertedText();
		
		assertNotNull(actName1);
		assertNotNull(actCon1);
		assertNotNull(actName2);
		assertNotNull(actCon2);
		assertNotNull(actName3);
		assertNotNull(actCon3);
		assertNotNull(actName4);
		assertNotNull(actCon4);
		assertNotNull(actName5);
		assertNotNull(actCon5);
		assertNotNull(actName6);
		assertNotNull(actCon6);
		assertNotNull(actName7);
		assertNotNull(actCon7);
		assertNotNull(actName8);
		assertNotNull(actCon8);
		
		assertEquals(expName1, actName1);
//		assertEquals(expCon1, actCon1); //This one has mutilple problems FIXME
		assertEquals(expName2, actName2);
		assertEquals(expCon2, actCon2);
		assertEquals(expName3, actName3);
		assertEquals(expCon3, actCon3);
		assertEquals(expName4, actName4);
		assertEquals(expCon4, actCon4);
		assertEquals(expName5, actName5);
		assertEquals(expCon5, actCon5);
		assertEquals(expName6, actName6);
		assertEquals(expCon6, actCon6);
		assertEquals(expName7, actName7);
		assertEquals(expCon7, actCon7);
		assertEquals(expName8, actName8);
		assertEquals(expCon8, actCon8);
		
		//test when illegal handling has been disabled
		String props = "Mywiki.0001.switch.illegal-handling=false";
		//haven't set handler yet, so should be default
		ConverterEngine.HierarchyHandler handler = tester.getHierarchyHandler();
		assertEquals(ConverterEngine.HierarchyHandler.DEFAULT, handler);
		//setting the handler
		tester.handleNonConverterProperty(props);
		Page page9 = new Page(null);
		String name9 = "Testing:1#2;3";
		String content9 = "See [Testing:1#2;3]"; 
		page9.setName(name9);
		page9.setOriginalText(content9);
		page9.setConvertedText(content9); //ConverterEngine expects this
		ArrayList<Page> pagesB = new ArrayList<Page>();
		pagesB.add(page9);
		String expName9 = name9;
		String expCon9 = content9;
		tester.convertWithRequiredConverters(pagesB);

		assertNotNull(pagesB);

		Page actPage9 = pagesB.remove(0);
		String actName9 = actPage9.getName();
		String actCon9 = actPage9.getConvertedText();
		assertNotNull(actName9);
		assertNotNull(actCon9);
		assertEquals(expName9, actName9);
		assertEquals(expCon9, actCon9);
		
	}

	public void testEntireConversion() {
		Page page = new Page(null);
		page.setName("Test:ing");
		String expName = "Test.ing";
		String content = "Testing ... __bold__ [alias|Test:ing]";
		String expCon1 = "Testing ... *bold* [alias|Test:ing]";
		String expCon2 = "Testing ... *bold* [alias|Test.ing]"; //with state based illegal handling
		
		page.setOriginalText(content);
		page.setConvertedText(content);
		String basicConverter = CONVERTER_BOLD;
		ArrayList<Converter> converters = tester.createOneConverter(basicConverter);
		ArrayList<Page> pages = new ArrayList<Page>();
		pages.add(page);
		boolean useUI = false;
		tester.convertPages(pages, converters, "Converting page files");
		assertNotNull(pages);
		assertEquals(1, pages.size());
		Page actual = pages.get(0);
		assertNotNull(actual);
		assertEquals(expCon1, actual.getConvertedText());
		
		tester.convertWithRequiredConverters(pages, useUI);
		assertNotNull(pages);
		assertEquals(1, pages.size());
		actual = pages.get(0);
		assertNotNull(actual);
		assertEquals(expName, actual.getName());
//		assertEquals(expCon2, actual.getConvertedText()); //FIXME This would work with the state based illegal handling
		assertEquals(expCon1, actual.getConvertedText());
		
		//trying out an illegal handling conversion that is fairly straightforward (ie. not colon)
		page = new Page(null);
		String title = "No good reason to have a semi-colon ;";
		page.setName(title);
		expName = "No good reason to have a semi-colon .";
		content = "Testing ... __bold__ [alias|" + title + "]";
		expCon1 = "Testing ... *bold* [alias|" + title + "]";
		expCon2 = "Testing ... *bold* [alias|" + expName + "]"; 
		
		page.setOriginalText(content);
		page.setConvertedText(content);
		basicConverter = CONVERTER_BOLD;
		converters = tester.createOneConverter(basicConverter);
		pages = new ArrayList<Page>();
		pages.add(page);
		useUI = false;
		tester.convertPages(pages, converters, "Converting page files");
		assertNotNull(pages);
		assertEquals(1, pages.size());
		actual = pages.get(0);
		assertNotNull(actual);
		assertEquals(expCon1, actual.getConvertedText());
		
		tester.convertWithRequiredConverters(pages, useUI);
		assertNotNull(pages);
		assertEquals(1, pages.size());
		actual = pages.get(0);
		assertNotNull(actual);
		assertEquals(expName, actual.getName());
		assertEquals(expCon2, actual.getConvertedText()); 
	}
	
	public void testCreateOneConverter() {
		String pagenameProp = CONVERTER_ILLEGALPAGENAMES;
		ArrayList<Converter> actual = tester.createOneConverter(pagenameProp);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(IllegalPageNameConverter.class, actual.get(0).getClass());
		
		String linknameProp = CONVERTER_ILLEGALLINKNAMES;
		actual = tester.createOneConverter(linknameProp);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(IllegalLinkNameConverter.class, actual.get(0).getClass());
	}
	
	public void testGetRequiredConverterFromString() {
		String pagenameProp = CONVERTER_ILLEGALPAGENAMES;
		Converter actual = tester.getConverterFromString(pagenameProp);
		assertNotNull(actual);
		assertEquals(IllegalPageNameConverter.class, actual.getClass());
		
		
		String linknameProp = CONVERTER_ILLEGALLINKNAMES;
		actual = tester.getConverterFromString(linknameProp);
		assertNotNull(actual);
		assertEquals(IllegalLinkNameConverter.class, actual.getClass());
	}
	public void testConvertPagesWithActualFile() {
		
		String name = "SampleTikiwiki-Input21.txt";
		String path = "sampleData/tikiwiki/" + name;
		Page page = new Page(new File(path), path);
		page.setName(name);
		String expected = "*Hello*\n";
		
		ArrayList<Page> pages = new ArrayList<Page>();
		pages.add(page);
		ArrayList<String> converterStrings = new ArrayList<String>();
		converterStrings.add(CONVERTER_BOLD);
		ArrayList<Converter> converters = tester.createConverters(converterStrings);
		boolean useUI = false;
		tester.convertPages(pages, converters, "Converting page files");
		tester.convertWithRequiredConverters(pages, useUI);
		assertNotNull(pages);
		assertEquals(1, pages.size());
		Page actual = pages.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual.getConvertedText());
	}
	

	public void testAlreadyAttached() {
		String filename = "hobbespounce.gif";
		String dir = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/";
		String absPath = dir + filename;
		File file = new File(absPath);
		
		//not attached
		tester = new ConverterEngine();
		Page page = new Page(null);
		page.setName("testPage");
		assertFalse(tester.alreadyAttached(page, file));
		
		//empty
		tester = new ConverterEngine();
		Set<File> attachments = new HashSet<File>(); 
		page.setAttachments(attachments);
		assertFalse(tester.alreadyAttached(page, file));
		
		//other file
		tester = new ConverterEngine();
		File newAttachment = new File(dir + "testimage.png");
		page.addAttachment(newAttachment);
		assertFalse(tester.alreadyAttached(page, file));
		
		//this file
		tester = new ConverterEngine();
		page.addAttachment(file);
		Set<File> attachments2 = page.getAttachments();
		File actual  = null;
		for (File attach2 : attachments2) {
			actual = attach2;
		}
		assertNotNull(actual);
		assertEquals(2, attachments2.size());
		assertFalse(tester.alreadyAttached(page, file));
		assertTrue(tester.alreadyAttached(page, file));
	}
	
	public void testTooBig() {
		//empty
		File file = new File("");
		assertFalse(tester.tooBig(file));
		
		//existing, but not too big 
		String filename = "hobbespounce.gif";
		String dir = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/";
		String absPath = dir + filename;
		file = new File(absPath);
		assertTrue(file.exists()); //no point if the file doesn't exist.
		assertFalse(tester.tooBig(file)); //this file is 6K in size
		
		filename = "confluence-2.2-std.zip";
		dir = "/Users/laura/Spike/Work/Confluence/Zips/";		
		absPath = dir + filename;
		file = new File(absPath);
		assertTrue(file.exists()); //no point if the file doesn't exist.
		assertTrue(tester.tooBig(file)); //this file is 42M in size
		
		//how do we test if the property is bad? FIXME
	}
	
	public void testGetAsBytes() {
		int multiplier = 1024;

		String input = "20B";
		double expected = 20;
		double actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
		
		input = "5K";
		expected = 5 * multiplier;
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
		
		input = "104M";
		expected = 104 * multiplier * multiplier;
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
		
		input = "2G";
		expected = 2 * multiplier * multiplier * multiplier;
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);

		//default unit
		input = "101";
		expected = 101;
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
	
		//misformed
		expected = -1;

		input = "";
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
		
		input = "10H";
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);

		input = "B101";
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);

		input = "abc";
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);
		
		//case issues
		input = "1k";
		expected = 1024;
		actual = tester.getAsBytes(input);
		assertEquals(expected, actual);

	}
	

	public void testSavePage() {
		String pagepath = TEST_INPUT_DIR + TEST_INPUT;
		File file = new File(pagepath);
		Page page = new Page(file);
		String content = null;
		assertTrue(file.exists());
		try {
			content = FileUtils.readTextFile(file);
		} catch (IOException e) {
			fail();
		}
		page.setConvertedText(content);
		String space = "uwctest";
		String pageTitle = TEST_INPUT;
		page.setName(pageTitle);
		
		ConfluenceServerSettings confSettings = new ConfluenceServerSettings();
		loadSettingsFromFile(confSettings, "test.basic.properties");
		confSettings.spaceKey = space;
        confSettings.url="localhost:8082";

        if (pageExists(pageTitle, space, confSettings)) {
        	deletePage(pageTitle, space, confSettings);
        }
		
        tester.sendPage(page, null, confSettings);
		
        assertTrue(pageExists(pageTitle, space, confSettings));
	}

	public void testHandleMiscellaneousProperties_Creation() {
		//test that it creates the properties object
		assertNotNull(tester.handleMiscellaneousProperties("badkey", ""));
		assertNotNull(tester.handleMiscellaneousProperties("Xwiki.0002.allow-at-in-links.property", ""));
	}
	public void testHandleMiscellaneousProperties_KeyValue() {
		//test that it figures out the right key
		String input, expected, value, key;
		input = "Xwiki.0002.allow-at-in-links.property";
		key = "allow-at-in-links";
		value = "foo";
		expected = value;
		Properties actual = tester.handleMiscellaneousProperties(input, value);
		assertNotNull(actual);
		assertNotNull(actual.get(key));
		assertNotNull(actual.getProperty(key));
		assertEquals(expected, (String) actual.get(key));
		assertEquals(expected, actual.getProperty(key));
		
		input = "Mediawiki.1021.underscore2space-links.property";
		key = "underscore2space-links";
		value = "bar";
		expected = value;
		actual = tester.handleMiscellaneousProperties(input, value);
		assertNotNull(actual);
		assertNotNull(actual.get(key));
		assertNotNull(actual.getProperty(key));
		assertEquals(expected, (String) actual.get(key));
		assertEquals(expected, actual.getProperty(key));

	}

	public void testCreatePages() {
		FileFilter filter = null;
		String page = TEST_INPUT_DIR + TEST_INPUT;
		File file = new File(page);
		assertTrue(file.exists());
		List<File> files = new Vector<File>();
		files.add(file);
		List<Page> pages = tester.createPages(filter, files);
		assertNotNull(pages);
		assertEquals(1, pages.size());
		assertEquals(TEST_INPUT, pages.get(0).getName());
	}
	
	public void testCreatePages_utf8Titles() {
		FileFilter filter = null;
		String page = TEST_INPUT_DIR;
		File file = new File(page);
		assertTrue(file.exists());
		List<File> files = new Vector<File>();
		files.add(file);
		List<Page> pages = tester.createPages(filter, files);
		assertNotNull(pages);
		for (Page actual : pages) {
			String title = actual.getName();
			if (title.startsWith("SampleEngine-InputUTF8")) {
				//should be testing for korean characters, 
				//but Eclipse won't allow them in the editor. doh!
				assertFalse(title.contains("?")); 
			}
		}
	}
	
	private void deletePage(String pageTitle, String space, ConfluenceServerSettings confSettings) {
		confSettings.url = confSettings.url.replaceFirst("https?://", "");
		XmlRpcClient client = getXmlRpcClient(confSettings);

		Hashtable page = null;
		try {
			page = getPage(client, pageTitle, space, confSettings);
		} catch (Exception e1) {
			e1.printStackTrace();
			fail();
		}
		Vector paramsVector = new Vector();
		try {
			paramsVector.add(RemoteWikiBroker.getInstance().getLoginToken(confSettings));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		paramsVector.add(page.get("id"));
		try {
			client.execute("confluence1.removePage", paramsVector );
		} catch (XmlRpcException e) {
		} catch (IOException e) {
			fail("Shouldn't have IO exception");
		}
	}
	
	private void deletePage(String id, ConfluenceServerSettings confSettings) {
		confSettings.url = confSettings.url.replaceFirst("http://", "");
		XmlRpcClient client = getXmlRpcClient(confSettings);
		Vector paramsVector = new Vector();
		try {
			paramsVector.add(RemoteWikiBroker.getInstance().getLoginToken(confSettings));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		paramsVector.add(id);
		try {
			client.execute("confluence1.removePage", paramsVector );
		} catch (XmlRpcException e) {
		} catch (IOException e) {
			fail("Shouldn't have IO exception");
		}
	}


	private boolean pageExists(String pageTitle, String space, 
			ConfluenceServerSettings confSettings) {
		XmlRpcClient client = getXmlRpcClient(confSettings);
		
		try {
			Hashtable page = getPage(client, pageTitle, space, confSettings);
		} catch (XmlRpcException e) {
			return false;
		} catch (IOException e) {
			fail("Shouldn't have IO exception");
		}
		return true;
	}


	private Hashtable getPage(XmlRpcClient client, String pageTitle, String space, ConfluenceServerSettings confSettings) throws XmlRpcException, IOException {
		confSettings.url = confSettings.url.replaceFirst("http://", "");
		Vector paramsVector = new Vector();
		paramsVector.add(RemoteWikiBroker.getInstance().getLoginToken(confSettings));
		paramsVector.add(space);
		paramsVector.add(pageTitle);
		Hashtable page = (Hashtable) client.execute("confluence1.getPage", paramsVector );
		return page;
	}


	private XmlRpcClient getXmlRpcClient(ConfluenceServerSettings confSettings) {
		confSettings.url = confSettings.url.replaceFirst("http://", "");
		String connectionURL = "http://" + confSettings.url + "/rpc/xmlrpc";
		XmlRpcClient clientConnection = null;
        try {
			clientConnection = new XmlRpcClient(connectionURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return clientConnection;
	}

	
}
