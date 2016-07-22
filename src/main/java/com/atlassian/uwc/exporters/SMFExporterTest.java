package com.atlassian.uwc.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.exporters.SMFExporter.Attachment;
import com.atlassian.uwc.exporters.SMFExporter.Data;
import com.atlassian.uwc.exporters.SMFExporter.Message;
import com.atlassian.uwc.ui.FileUtils;

/**
 * requires sample test.output.properties and test.basic.properties
 * with correct Mysql settings to work
 */
public class SMFExporterTest extends TestCase {

	SMFExporter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties testprops = null;
	protected void setUp() throws Exception {
		tester = new SMFExporter();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = loadSettingsFromFile("test.basic.properties");
		tester.setProperties(props);
		tester.start();
		tester.connectToDB();
		tester.clearAttachments();
		tester.setEncoding();
	}
	
	protected void tearDown() {
		tester.closeDB();
	}
	
	public void testExportData() {
		Properties props = loadSettingsFromFile("test.output.properties");
		tester.setProperties(props);

		String outdir = props.getProperty("output.dir") + "/" + SMFExporter.OUTDIR;
		File out = new File(outdir);
		if (out.exists()) FileUtils.deleteDir(out);
		assertFalse(out.exists());
		
		tester.exportData();
		
		assertTrue(out.exists());
		String[] files = out.list();
		assertNotNull(files);
		assertEquals(6, files.length);

		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			assertTrue(file, file.equals("WelcometoSMF_top1.txt") ||
					file.equals("WelcometoSMF_top1.meta") ||
					file.equals("GeneralCategory_cat1.txt") ||
					file.equals("GeneralCategory_cat1.meta") ||
					file.equals("GeneralDiscussion_brd1.txt") ||
					file.equals("GeneralDiscussion_brd1.meta")
					);
		}
		
		String filestring = read(outdir+"/"+"WelcometoSMF_top1.txt");
		assertNotNull(filestring);
		assertEquals("Welcome to Simple Machines Forum!<br /><br />" +
				"We hope you enjoy using your forum.&nbsp; " +
				"If you have any problems, please feel free to " +
				"[url=http://www.simplemachines.org/community/index.php]" +
				"ask us for assistance[/url].<br /><br />Thanks!<br />Simple Machines\n", 
				filestring);
		
		
		FileUtils.deleteDir(out);
	}

	public void testExportData_Attachments() {
		Properties props = loadSettingsFromFile("test.attachments.properties");
		tester.setProperties(props);

		String outdir = props.getProperty("output.dir") + "/" + SMFExporter.OUTDIR;
		File out = new File(outdir);
		if (out.exists()) FileUtils.deleteDir(out);
		assertFalse(out.exists());
		
		tester.exportData();
		
		assertTrue(out.exists());
		String[] files = out.list();
		assertNotNull(files);
		assertEquals(6, files.length);

		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			assertTrue(file, file.equals("ImagesandAttachments_re16.txt") ||
					file.equals("ImagesandAttachments_re16.meta") ||
					file.equals("GeneralCategory_cat1.txt") ||
					file.equals("GeneralCategory_cat1.meta") ||
					file.equals("GeneralDiscussion_brd1.txt") ||
					file.equals("GeneralDiscussion_brd1.meta")
					);
		}
		
		Properties meta = new Properties();
		try {
			meta.load(new FileInputStream(outdir + "/" + "ImagesandAttachments_re16.meta"));
		} catch (IOException e) {
		}
		String attachString = meta.getProperty("attachments.location");
		assertNotNull(attachString);
		assertEquals("1_1369c5b06a8394704b9bd20d8e6e9191eda82494," + 
				"2_df23fe19b1dc2e9c406e1ac5aaf62d2820ed3126", attachString);
		String attachNames = meta.getProperty("attachments.name");
		assertNotNull(attachNames);
		assertEquals("1_cow.jpg," + 
				"2_cow.jpg_thumb.jpg", attachNames);
		FileUtils.deleteDir(out);
	}
	
	public void testExportCategories() throws ClassNotFoundException, SQLException {
		List<SMFExporter.Category> actual = tester.exportCategories();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());

		SMFExporter.Category first = actual.get(0);
		assertEquals("1", first.id);
		assertEquals("General Category", first.name);
				
		SMFExporter.Category second = actual.get(1);
		assertEquals("2", second.id);
		assertEquals("New Category", second.name);
	}
	
	public void testExportBoards() throws ClassNotFoundException, SQLException {
		List<SMFExporter.Board> actual = tester.exportBoards();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		
		SMFExporter.Board first = actual.get(0);
		assertEquals("1", first.id);
		assertEquals("1", first.category);
		assertEquals("0", first.level);
		assertEquals("0", first.parent);
		assertEquals(SMFExporter.Type.CATEGORY, first.parenttype);
		assertEquals("General Discussion", first.name);
		assertEquals("Feel free to talk about anything and everything in this board.", first.description);
		
		SMFExporter.Board third = actual.get(4);
		assertEquals("3", third.id);
		assertEquals("1", third.category);
		assertEquals("1", third.level);
		assertEquals("2", third.parent);
		assertEquals(SMFExporter.Type.BOARD, third.parenttype);
		assertEquals("Child Board", third.name);
		assertEquals("Testing Hierarchy", third.description);
		
	}

	public void testExportMessages() throws ClassNotFoundException, SQLException {
		List<SMFExporter.Message> actual = tester.exportMessages();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		
		SMFExporter.Message first = actual.get(0);
		assertEquals("1", first.id);
		assertEquals("1", first.topic);
		assertEquals("1", first.board);
		assertEquals("0", first.userid);
		assertEquals("Simple Machines", first.username);
		assertEquals("info@simplemachines.org", first.useremail);
		assertEquals("1245692231", first.time);
		assertEquals("Welcome to SMF!", first.title);
		assertTrue(first.content.startsWith("Welcome to Simple Machines Forum!"));
		assertEquals(true, first.isfirst);
		
		SMFExporter.Message third = actual.get(2);
		assertEquals("3", third.id);
		assertEquals("2", third.topic);
		assertEquals("1", third.board);
		assertEquals("1", third.userid);
		assertEquals("admin", third.username);
		assertEquals("laura.kolker@gmail.com", third.useremail);
		assertEquals("1245693435", third.time);
		assertEquals("Hierarchy Test", third.title);
		assertTrue(third.content.startsWith("New Topic<br /><br />Topic Root"));
		assertEquals(true, third.isfirst);		

		SMFExporter.Message tenth = actual.get(9);
		assertEquals("10", tenth.id);
		assertEquals("1", tenth.topic);
		assertEquals("1", tenth.board);
		assertEquals("1", tenth.userid);
		assertEquals("admin", tenth.username);
		assertEquals("laura.kolker@gmail.com", tenth.useremail);
		assertEquals("1245706817", tenth.time);
		assertEquals("Re: Welcome to SMF!", tenth.title);
		assertTrue(tenth.content.startsWith("Basic reply to original topic"));
		assertEquals(false, tenth.isfirst);
	}
	
	public void testExportAttachments() {
		List<SMFExporter.Attachment> actual = tester.exportAttachments();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		
		SMFExporter.Attachment first = actual.get(0);
		assertEquals("1", first.id);
		assertEquals("2", first.thumb);
		assertEquals("16", first.message);
		assertEquals("cow.jpg", first.name);
		assertEquals("1369c5b06a8394704b9bd20d8e6e9191eda82494", first.hash);
		
		SMFExporter.Attachment third = actual.get(2);
		assertEquals("3", third.id);
		assertEquals("0", third.thumb);
		assertEquals("20", third.message);
		assertEquals("ed.jpeg", third.name);
		assertEquals("0e1b4e5b7a679b7da4325e8725602f9c179a782a", third.hash);
		
		SMFExporter.Attachment fifth = actual.get(4);
		assertEquals("5", fifth.id);
		assertEquals("0", fifth.thumb);
		assertEquals("21", fifth.message);
		assertEquals("doublefacepalm.jpg_thumb", fifth.name);
		assertEquals("1e9446775ba7fee1326d070a546cc0762ce976f4", fifth.hash);
	}
	
	public void testCreateOutData() {
		Properties props = loadSettingsFromFile("test.output.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Category> categories = tester.exportCategories();
		List<SMFExporter.Board> boards = tester.exportBoards();
		List<SMFExporter.Message> messages = tester.exportMessages();
		
		List<Data> actual = tester.createOutData(categories, boards, messages);
		
		assertNotNull(actual);
		assertEquals(3, actual.size());
		
		Data cat = actual.get(0);
		Data brd = actual.get(1);
		Data msg = actual.get(2);
		
		assertNotNull(cat);
		assertEquals("1", cat.id);
		assertEquals("General Category", cat.title);
		assertEquals(SMFExporter.Type.CATEGORY, cat.type);
		assertEquals(null, cat.parentid);
		assertEquals(null, cat.parenttype);
		assertEquals(null, cat.ancestors);
		assertEquals(null, cat.time);
		assertEquals(null, cat.useremail);
		assertEquals(null, cat.username);
		assertEquals(null, cat.userid);
		assertEquals(null, cat.content);
		assertEquals(null, cat.attachments);
		assertEquals(null, cat.attachmentnames);
		
		assertNotNull(brd);
		assertEquals("1", brd.id);
		assertEquals("General Discussion", brd.title);
		assertEquals(SMFExporter.Type.BOARD, brd.type);
		assertEquals("1", brd.parentid);
		assertEquals(SMFExporter.Type.CATEGORY, brd.parenttype);
		assertEquals("cat1", brd.ancestors);
		assertEquals(null, brd.time);
		assertEquals(null, brd.useremail);
		assertEquals(null, brd.username);
		assertEquals(null, brd.userid);
		assertEquals("Feel free to talk about anything and everything in this board.", brd.content);
		assertEquals(null, brd.attachments);
		assertEquals(null, brd.attachmentnames);

		assertNotNull(msg);
		assertEquals("1", msg.id);	
		assertEquals("Welcome to SMF!", msg.title);
		assertEquals(SMFExporter.Type.TOPIC, msg.type);
		assertEquals("1", msg.parentid);
		assertEquals(SMFExporter.Type.BOARD, msg.parenttype);
		assertEquals("cat1:brd1", msg.ancestors);
		assertEquals("1245692231", msg.time);
		assertEquals("info@simplemachines.org", msg.useremail);
		assertEquals("Simple Machines", msg.username);
		assertEquals("0", msg.userid);
		assertEquals("Welcome to Simple Machines Forum!<br /><br />We hope you " +
				"enjoy using your forum.&nbsp; If you have any problems, please " +
				"feel free to [url=http://www.simplemachines.org/community/index." +
				"php]ask us for assistance[/url].<br /><br />Thanks!<br />Simple" +
				" Machines", msg.content);
		assertEquals("", msg.attachments);
		assertEquals("", msg.attachmentnames);
	}

	public void testCreateOutData_Attachments() {
		Properties props = loadSettingsFromFile("test.attachments.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Category> categories = tester.exportCategories();
		List<SMFExporter.Board> boards = tester.exportBoards();
		List<SMFExporter.Message> messages = tester.exportMessages();
		tester.exportAttachments();
		
		List<Data> actual = tester.createOutData(categories, boards, messages);
		
		assertNotNull(actual);
		assertEquals(3, actual.size());
		
		Data msg = actual.get(2);
		
		assertNotNull(msg);
		assertEquals("16", msg.id);	
		assertEquals("Images and Attachments", msg.title);
		assertEquals(SMFExporter.Type.REPLY, msg.type);
		assertEquals("6", msg.parentid);
		assertEquals(SMFExporter.Type.TOPIC, msg.parenttype);
		assertEquals("brd2:top8", msg.ancestors); //no cat 'cause I don't want to export all the categories for this one test
		assertEquals("1246561983", msg.time);
		assertEquals("laura.kolker@gmail.com", msg.useremail);
		assertEquals("admin", msg.username);
		assertEquals("1", msg.userid);
		assertEquals("Inline:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image[/img]<br />Thumbnail:<br />[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/img]<br /><br />Link<br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image[/url]<br />[url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/url]<br /><br />Note: There&#039;s an image attached to this message? topic? and I don&#039;t necessarily have to refer to it in the page content.", msg.content);
		assertEquals("1_1369c5b06a8394704b9bd20d8e6e9191eda82494," + 
				"2_df23fe19b1dc2e9c406e1ac5aaf62d2820ed3126", msg.attachments);
		assertEquals("1_cow.jpg," + 
				"2_cow.jpg_thumb.jpg", msg.attachmentnames);
	}

	public void testCreateOutData_CorrectTopicId() {
		Properties props = loadSettingsFromFile("test.basic.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Category> categories = tester.exportCategories();
		List<SMFExporter.Board> boards = tester.exportBoards();
		List<SMFExporter.Message> messages = tester.exportMessages();
		tester.exportAttachments();
		
		List<Data> actual = tester.createOutData(categories, boards, messages);
		
		assertNotNull(actual);
		
		for (int i = 0; i < actual.size(); i++) {
			Data msg = (Data) actual.get(i);
			if (msg.id.equals("16")) {
				assertEquals("cat1:brd2:top8", msg.ancestors); //catnull 'cause I don't want to export all the categories for this one test
			}
		}
	}
	
	public void testCreateOutData_ChildBoards() {
		Properties props = loadSettingsFromFile("test.childboards.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Category> categories = tester.exportCategories();
		List<SMFExporter.Board> boards = tester.exportBoards();
		List<SMFExporter.Message> messages = tester.exportMessages();
		tester.exportAttachments();
		
		List<Data> actual = tester.createOutData(categories, boards, messages);
		
		assertNotNull(actual);
		
		for (int i = 0; i < actual.size(); i++) {
			Data msg = (Data) actual.get(i);
			if (msg.id.equals("28")) {
				assertEquals("cat1:brd2:brd5", msg.ancestors); 
			}
		}
	}
	
	public void testCreateOutData_GrandchildBoard() {
		Properties props = loadSettingsFromFile("test.gchildboards.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Category> categories = tester.exportCategories();
		List<SMFExporter.Board> boards = tester.exportBoards();
		List<SMFExporter.Message> messages = tester.exportMessages();
		tester.exportAttachments();
		
		List<Data> actual = tester.createOutData(categories, boards, messages);
		
		assertNotNull(actual);
		
		for (int i = 0; i < actual.size(); i++) {
			Data msg = (Data) actual.get(i);
			if (msg.id.equals("7")) { //grandchild board
				assertEquals("cat1:brd2:brd3", msg.ancestors); 
			}
			if (msg.id.equals("31")) {
				assertEquals("cat1:brd2:brd3:brd7", msg.ancestors);
			}
		}
	}
	
	public void testOutputData() {
		Properties props = loadSettingsFromFile("test.output.properties");
		tester.setProperties(props);

		String outdir = props.getProperty("output.dir") + "/" + SMFExporter.OUTDIR;
		File out = new File(outdir);
		if (out.exists()) FileUtils.deleteDir(out);
		assertFalse(out.exists());
		
		SMFExporter.Data data = tester.new Data();
		data.title="Testing";
		data.type=SMFExporter.Type.REPLY;
		data.id="45";
		data.content="Lorem Ipsum Testing 123 foobar";
		data.parentid="112";
		data.parenttype=SMFExporter.Type.TOPIC;
		data.ancestors="cat1:brd1:top12";
		data.time="123456789000";
		data.useremail="abc@def.org";
		data.userid="2";
		data.username="testuser";
		data.attachments="4_3ba8c5523756e7113c4bb5e5a06abf69f05223bb:5_1e9446775ba7fee1326d070a546cc0762ce976f4";
		data.attachmentnames="4_doublefacepalm.jpg:5_doublefacepalm.jpg_thumb.jpg";
		data.attachmentdelim=":";
		
		Vector<Data> input = new Vector<Data>();
		input.add(data);

		tester.outputData(input);
		
		assertTrue(out.exists());
		String contentpath = outdir +"/" + "Testing_re45.txt";
		assertTrue(new File(contentpath).exists());
		String metapath = outdir +"/" + "Testing_re45.meta";
		assertTrue(new File(metapath).exists());
		String content = read(contentpath);
		assertEquals(data.content+"\n", content);
		String expMeta = "id=45\n" +
				"type=re\n" +
				"title=Testing\n" +
				"parentid=112\n" +
				"parenttype=top\n" +
				"ancestors=cat1:brd1:top12\n" +
				"time=123456789000\n" +
				"userid=2\n" +
				"username=testuser\n" +
				"useremail=abc@def.org\n" +
				"attachments.location=4_3ba8c5523756e7113c4bb5e5a06abf69f05223bb:5_1e9446775ba7fee1326d070a546cc0762ce976f4\n" +
				"attachments.name=4_doublefacepalm.jpg:5_doublefacepalm.jpg_thumb.jpg\n" +
				"attachments.delim=:\n";
		String meta = read(metapath);
		assertEquals(expMeta, meta);
		
		FileUtils.deleteDir(out);
	}

	public void testAddParentDir() {
		String input, expected, actual;
		input = "testing";
		expected = loadSettingsFromFile("test.basic.properties").getProperty("output.dir") + 
					"/" + SMFExporter.OUTDIR + "/testing";
		actual = tester.addParentDir(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "/testing";
		actual = tester.addParentDir(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetContentFilename() {
		SMFExporter.Data data = new SMFExporter().new Data();
		data.title="Testing";
		data.type=SMFExporter.Type.REPLY;
		data.id="43";
		
		String input, expected, actual;
		expected = "Testing_re43.txt";
		actual = data.getContentFilename();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		data.type=SMFExporter.Type.CATEGORY;
		expected = "Testing_cat43.txt";
		actual = data.getContentFilename();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetContentBody() {
		SMFExporter.Data data = new SMFExporter().new Data();
		data.title="Testing";
		data.type=SMFExporter.Type.REPLY;
		data.id="43";
		data.content="Lorem Ipsum Testing 123 foobar";
		
		String expected, actual;
		expected = data.content;
		actual = data.getContentBody();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetMetaFilename() {
		SMFExporter.Data data = new SMFExporter().new Data();
		data.title="Testing";
		data.type=SMFExporter.Type.REPLY;
		data.id="43";
		
		String input, expected, actual;
		expected = "Testing_re43.meta";
		actual = data.getMetaFilename();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		data.type=SMFExporter.Type.CATEGORY;
		expected = "Testing_cat43.meta";
		actual = data.getMetaFilename();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetMetaBody() {
		SMFExporter.Data data = new SMFExporter().new Data();
		data.title="Testing";
		data.type=SMFExporter.Type.REPLY;
		data.id="43";
		data.content="Lorem Ipsum Testing 123 foobar";
		data.parentid="12";
		data.parenttype=SMFExporter.Type.TOPIC;
		data.ancestors="cat1:brd1:top12";
		data.time="123456789";
		data.useremail="abc@def.org";
		data.userid="2";
		data.username="testuser";
		data.attachments="3_0e1b4e5b7a679b7da4325e8725602f9c179a782a";
		data.attachmentnames="3_ed.jpeg";
		
		String expected, actual;
		expected = "id=43\n" +
				"type=re\n" +
				"title=Testing\n" +
				"parentid=12\n" +
				"parenttype=top\n" +
				"ancestors=cat1:brd1:top12\n" +
				"time=123456789\n" +
				"userid=2\n" +
				"username=testuser\n" +
				"useremail=abc@def.org\n" +
				"attachments.location=3_0e1b4e5b7a679b7da4325e8725602f9c179a782a\n" +
				"attachments.name=3_ed.jpeg\n" +
				"attachments.delim=,\n";
		actual = data.getMetaBody();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCondenseTitle() {
		String input, expected, actual;
		Data data = tester.new Data();
		input = "test";
		expected = "test";
		actual = data.condenseTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "test 123";
		expected = "test123";
		actual = data.condenseTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "test !@#$%^^&**()(*<>';.,{}\\][-=+\t_123";
		expected = "test_123";
		actual = data.condenseTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testMd5() throws NoSuchAlgorithmException {
		//make sure this works the same way as the PHP functions in both versions of SMF
		String input, expected, actual;
		input = "wget.exe";
		expected = "5e8509b45f7d472fdc99042f64270ff4";
		actual = tester.getMd5(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//have to use db hash, because in later versions the hash is salted.
	}

	public void testGetAttachmentList() {
		Attachment att1 = tester. new Attachment("1", "2", "16", "cow.jpg", "1369c5b06a8394704b9bd20d8e6e9191eda82494");
		Attachment att2 = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","1e9446775ba7fee1326d070a546cc0762ce976f4");
		Attachment att3 = tester. new Attachment("4", "5", "21", "doublefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.clearAttachments();
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		
		String input, expected, actual;
		input = "16";
		expected = "1_1369c5b06a8394704b9bd20d8e6e9191eda82494";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = "5_1e9446775ba7fee1326d070a546cc0762ce976f4," +
				"4_3ba8c5523756e7113c4bb5e5a06abf69f05223bb";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "5_1e9446775ba7fee1326d070a546cc0762ce976f4;" +
		"4_3ba8c5523756e7113c4bb5e5a06abf69f05223bb";
		actual = tester.getAttachmentList(input, ";");
		
		input = "1";
		expected = "";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = null;
		expected = "";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	

	public void testGetAttachmentDelimiter() {
		Attachment att1 = tester. new Attachment("1", "2", "16", "cow.jpg", "1369c5b06a8394704b9bd20d8e6e9191eda82494");
		Attachment att2 = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","1e9446775ba7fee1326d070a546cc0762ce976f4");
		Attachment att3 = tester. new Attachment("4", "5", "21", "doublefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.clearAttachments();
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		
		String input, expected, actual;
		input = "16";
		expected = ",";
		actual = tester.getAttachmentDelim(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = ",";
		actual = tester.getAttachmentDelim(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);

		//bad chars
		
		Attachment att4 = tester. new Attachment("11", "2", "21", "co,w.jpg", "1369c5b06a8394704b9bd20d8e6e9191eda82494");
		tester.saveAttachmentWithId(att4);
		expected = ";";
		actual = tester.getAttachmentDelim("21", ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Attachment att5 = tester. new Attachment("15","0","21","double;facepalm.jpg_thumb","1e9446775ba7fee1326d070a546cc0762ce976f4");
		tester.saveAttachmentWithId(att5);
		expected = ":";
		actual = tester.getAttachmentDelim("21", ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Attachment att6 = tester. new Attachment("14", "5", "21", "doub:lefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.saveAttachmentWithId(att6);
		expected = "?";
		actual = tester.getAttachmentDelim("21", ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Attachment att7 = tester. new Attachment("14", "5", "21", "doub?lefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.saveAttachmentWithId(att7);
		expected = "`";
		actual = tester.getAttachmentDelim("21", ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Attachment att8 = tester. new Attachment("14", "5", "21", "doub`lefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.saveAttachmentWithId(att8);
		actual = tester.getAttachmentDelim("21", ",");
		assertNull(actual);
	}
	
	public void testHandleHashWithProperty() throws NoSuchAlgorithmException {
		Properties props = loadSettingsFromFile("test.hashsqlfalse.properties");
		tester.setProperties(props);
		
		Attachment att1 = tester. new Attachment("1", "2", "16", "cow.jpg", "");
		Attachment att2 = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","");
		Attachment att3 = tester. new Attachment("4", "5", "21", "doublefacepalm.jpg", "");
		Attachment att4 = tester. new Attachment("95", "0", "873", "wget.exe", "");
		
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		tester.saveAttachmentWithId(att4);
		
		String input, expected, actual;
		input = "873";
		expected = "95_wget_exe5e8509b45f7d472fdc99042f64270ff4";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "16";
		expected = "1_cow_jpg" + tester.getMd5("cow.jpg");
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = "5_doublefacepalm_jpg_thumb"+tester.getMd5("doublefacepalm.jpg_thumb") + "," +
				"4_doublefacepalm_jpg" + tester.getMd5("doublefacepalm.jpg");
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "1";
		expected = "";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = null;
		expected = "";
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleHashWithProperty_BadChars() throws NoSuchAlgorithmException {
		Properties props = loadSettingsFromFile("test.hashsqlfalse.properties");
		tester.setProperties(props);
		
		Attachment att1 = tester. new Attachment("1", "2", "16", "abc[def.txt", "");
		Attachment att2 = tester. new Attachment("5","0","21","abc def.txt","");
		Attachment att3 = tester. new Attachment("4", "5", "21", "abc,def.gif", "");
		Attachment att4 = tester. new Attachment("95", "0", "21", "abcdef", "");
		Attachment att5 = tester. new Attachment("100", "0", "21", "abc_def", "");
		Attachment att6 = tester. new Attachment("101", "0", "21", "abc.def", "");
		
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		tester.saveAttachmentWithId(att4);
		tester.saveAttachmentWithId(att5);
		tester.saveAttachmentWithId(att6);
		
		String input, expected, actual;
		input = "16";
		expected = "1_abcdef_txt" + tester.getMd5("abcdef.txt"); //remove chars, but don't toUnderscore 
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = "5_abc_def_txt"+tester.getMd5("abc_def.txt") + "," +
				"4_abcdef_gif" + tester.getMd5("abcdef.gif") + "," +
				"95_abcdef" + tester.getMd5("abcdef") + "," + 
				"100_abc_def" + tester.getMd5("abc_def") + "," +
				"101_abc_def" + tester.getMd5("abc.def");
		actual = tester.getAttachmentList(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testGetAttachmentNames() {
		Attachment att1 = tester. new Attachment("1", "2", "16", "cow.jpg", "1369c5b06a8394704b9bd20d8e6e9191eda82494");
		Attachment att2 = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","1e9446775ba7fee1326d070a546cc0762ce976f4");
		Attachment att3 = tester. new Attachment("4", "5", "21", "doublefacepalm.jpg", "3ba8c5523756e7113c4bb5e5a06abf69f05223bb");
		tester.clearAttachments();
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		
		String input, expected, actual;
		input = "16";
		expected = "1_cow.jpg";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = "5_doublefacepalm.jpg_thumb.jpg," +
				"4_doublefacepalm.jpg";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "5_doublefacepalm.jpg_thumb.jpg?" +
				"4_doublefacepalm.jpg";
		actual = tester.getAttachmentNames(input, "?");
		assertNotNull(actual);
		assertEquals(expected, actual);

		
		input = "1";
		expected = "";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = null;
		expected = "";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleNamesWithProperty() throws NoSuchAlgorithmException {
		Properties props = loadSettingsFromFile("test.hashsqlfalse.properties");
		tester.setProperties(props);
		
		Attachment att1 = tester. new Attachment("1", "2", "16", "cow.jpg", "");
		Attachment att2 = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","");
		Attachment att3 = tester. new Attachment("4", "5", "21", "doublefacepalm.jpg", "");
		Attachment att4 = tester. new Attachment("95", "0", "873", "wget.exe", "");
		
		tester.saveAttachmentWithId(att1);
		tester.saveAttachmentWithId(att2);
		tester.saveAttachmentWithId(att3);
		tester.saveAttachmentWithId(att4);
		
		String input, expected, actual;
		input = "873";
		expected = "95_wget.exe";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "16";
		expected = "1_cow.jpg";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "21";
		expected = "5_doublefacepalm.jpg_thumb.jpg" + "," +
				"4_doublefacepalm.jpg";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "1";
		expected = "";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = null;
		expected = "";
		actual = tester.getAttachmentNames(input, ",");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetSqlHashFilename() {
		String expected, actual;
		Attachment input = tester. new Attachment("95", "0", "873", "wget.exe", "123456");
		expected = "95_123456";
		actual = tester.getSqlHashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetMd5HashFilename() {
		String expected, actual;
		//new Attachment(String id, String thumb, String message, String name, String hash)
		Attachment input = tester. new Attachment("95", "0", "873", "wget.exe", "");
		expected = "95_wget_exe5e8509b45f7d472fdc99042f64270ff4";
		actual = tester.getMd5HashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//make sure the md5 is properly padded
		input = tester. new Attachment("8", "0", "123", "zxcvbnm", "");
		expected = "8_zxcvbnm02c75fb22c75b23dc963c7eb91a062cc";
		actual = tester.getMd5HashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//bad chars - XXX
		Map props = tester.getProperties();
		props.put("attachment-chars-remove", "[]:");
		tester.setProperties(props);
		
		input = tester. new Attachment("100", "0", "123", "Foo[Bar].doc", "");
		expected = "100_FooBar_doc" + tester.getMd5("FooBar.doc");
		actual = tester.getMd5HashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = tester. new Attachment("100", "0", "123", "Foo Bar.doc", "");
		expected = "100_Foo_Bar_doc" + tester.getMd5("Foo_Bar.doc");
		actual = tester.getMd5HashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = tester. new Attachment("100", "0", "123", "Foo(Bar).doc", "");
		expected = "100_FooBar_doc" + tester.getMd5("FooBar.doc");
		actual = tester.getMd5HashFilename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetRealname() {
		String expected, actual;
		Attachment input = tester. new Attachment("95", "0", "873", "wget.exe", "");
		expected = "95_wget.exe";
		actual = tester.getRealname(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = tester. new Attachment("5","0","21","doublefacepalm.jpg_thumb","");
		expected = "5_doublefacepalm.jpg_thumb.jpg";
		actual = tester.getRealname(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEncode() {
		Properties props = loadSettingsFromFile("test.basic.properties");
		tester.setProperties(props);
		
		List<SMFExporter.Message> actual = tester.exportMessages();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		String utf8Encoded = null;
		String asciiEncoded = null;
		for (int i = 0; i < actual.size(); i++) {
			Message message = (Message) actual.get(i);
			if (message.id.equals("23")) {
				utf8Encoded = message.content; 
			}
		}
		
		props.put("db.encoding", "ascii");
		tester.setEncoding();
		actual = null;
		actual = tester.exportMessages();
		assertNotNull(actual);
		assertFalse(actual.isEmpty());
		for (int i = 0; i < actual.size(); i++) {
			Message message = (Message) actual.get(i);
			if (message.id.equals("23")) {
				asciiEncoded = message.content; 
			}
		}
		
		assertNotNull(utf8Encoded);
		assertNotNull(asciiEncoded);
		assertFalse(utf8Encoded.equals(asciiEncoded));
		
	}

	public void testGetAllAncestors() {
		HashMap<String,String> parentRelationships = new HashMap<String, String>();
		String relString = "brd7=brd3, brd6=brd2, brd1=cat1, brd3=brd2, brd5=brd2, brd4=cat2, brd2=cat1";
		String[] relPairs = relString.split(",");
		for (int i = 0; i < relPairs.length; i++) {
			String pair = relPairs[i];
			String[] kv = pair.split("=");
			parentRelationships.put(kv[0].trim(), kv[1].trim());
		}
		//category -> board -> topic
		Message message = new SMFExporter().new Message();
		message.board = "1";
		message.content = "testing";
		message.firstid = "1";
		message.id = "1";
		message.isfirst = true;
		message.time = "1245692232";
		message.title = "Welcome to SMF!";
		message.topic = "1";
		message.useremail = "info@simplemachines.org";
		message.userid = "0";
		message.username = "Simple Machines";
		Vector<String> actualVec = tester.getAllAncestors(parentRelationships, message.board);
		String expected = "brd1:cat1";
		String actual = vec2String(actualVec, ":");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//category -> board -> board -> topic
		message = new SMFExporter().new Message();
		message.board = "6";
		message.content = "Testing";
		message.firstid = "29";
		message.id = "29";
		message.isfirst = true;
		message.time = "125351398";
		message.title = "BoardSameName Topic2";
		message.topic = "12";
		message.useremail = "laura.kolker@gmail.com";
		message.userid = "1";
		message.username = "admin";
		actualVec = tester.getAllAncestors(parentRelationships, message.board);
		expected = "brd6:brd2:cat1";
		actual = vec2String(actualVec, ":");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		message = new SMFExporter().new Message();
		message.board = "5";
		message.content = "Testing";
		message.firstid = "28";
		message.id = "28";
		message.isfirst = true;
		message.time = "1253651323";
		message.title = "BoardSameName Topic1";
		message.topic = "11";
		message.useremail = "laura.kolker@gmail.com";
		message.userid = "1";
		message.username = "admin";
		actualVec = tester.getAllAncestors(parentRelationships, message.board);
		expected = "brd5:brd2:cat1";
		actual = vec2String(actualVec, ":");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//category -> board -> board -> topic -> reply
		message = new SMFExporter().new Message();
		message.board = "6";
		message.content = "Testing";
		message.firstid = "29";
		message.id = "30";
		message.isfirst = false;
		message.time = "1253651406";
		message.title = "Re: BoardSameName Topic2";
		message.topic = "12";
		message.useremail = "laura.kolker@gmail.com";
		message.userid = "1";
		message.username = "admin";
		actualVec = tester.getAllAncestors(parentRelationships, message.board);
		expected = "brd6:brd2:cat1";
		actual = vec2String(actualVec, ":");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	
	/* XXX Convenience Methods */
	private String read(String filepath) {
		String line,filestring = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			while ((line = reader.readLine()) != null) {
				filestring += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filestring;
	}
	
	private Properties loadSettingsFromFile(String testpropslocation) {
		Properties props = new Properties();
		String filepath = "sampleData/smf/junit_resources/" + testpropslocation;
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
	
	private String vec2String(Vector<String> vector, String delim) {
		String string = "";
		boolean first = true;
		for (Iterator iter = vector.iterator(); iter.hasNext();) {
			String part = (String) iter.next();
			if (!first) string += delim;
			string += part;
			first = false;
		}
		return string;
	}
	

}
