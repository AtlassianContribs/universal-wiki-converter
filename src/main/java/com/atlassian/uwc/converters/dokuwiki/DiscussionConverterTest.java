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

public class DiscussionConverterTest extends TestCase {

	private static final String METADIR = "sampleData/dokuwiki/junit_resources/meta";
	private static final String SAMPLEMETA = METADIR + "/SampleDokuwiki-InputDiscussion.comments";
	private static final String PAGESDIR = "sampleData/dokuwiki/junit_resources/pages";
	private static final String SAMPLEPAGE = PAGESDIR+"/SampleDokuwiki-InputDiscussion.txt";
	DiscussionConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new DiscussionConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.getProperties().setProperty("meta-dir", METADIR);
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", PAGESDIR);
		super.setUp();
	}


	public void testHasDiscussion() {
		String input;
		boolean expected, actual;
		input = "foobar\n" +
				"~~DISCUSSION~~\n";
		expected = true;
		actual = tester.hasDiscussion(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testing\n";
		expected = false;
		actual = tester.hasDiscussion(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetMetaFilepath() {
		Page page = new Page(new File(SAMPLEPAGE));
		String expected = "sampleData/dokuwiki/junit_resources/meta/SampleDokuwiki-InputDiscussion.comments";
		String actual = tester.getMetaFilename(page.getFile().getPath(), ".comments");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetCommentData() {
		Page page = new Page(new File(SAMPLEPAGE));
		String actual = tester.getCommentData(page.getFile());
		assertNotNull(actual);
		assertTrue(actual.contains("Foo Bar"));
		assertTrue(actual.contains("foo"));
		assertTrue(actual.contains("Testing 123"));
	}

	public void testGetNumComments() throws IOException {
		String input = FileUtils.readTextFile(new File(SAMPLEMETA));
		int expected = 1;
		int actual = tester.getNumComments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = FileUtils.readTextFile(new File(METADIR+"/SampleDokuwiki-InputDiscussion_mult.comments"));
		expected = 2;
		actual = tester.getNumComments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = FileUtils.readTextFile(new File(METADIR+"/SampleDokuwiki-InputDiscussion_none.comments"));
		expected = 0;
		actual = tester.getNumComments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a:2:{s:5:\"title\";N;s:6:\"status\";i:1;}\n";
		expected = 0;
		actual = tester.getNumComments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testSeperateComments() throws IOException {
		String input, expected, actual;
		input = FileUtils.readTextFile(new File(SAMPLEMETA));
		Vector<String> all = tester.seperateComments(input);
		assertNotNull(all);
		assertEquals(1, all.size());
		expected = "s:32:\"4df1fa69356ef92dd48fca5f004b3dc5\";a:8:{s:4:\"user\";a:5:{s:2:\"id\";s:3:\"foo\";s:4:\"name\";s:15:\"Foo Bar\";s:4:\"mail\";s:15:\"foo@bar.org\";s:7:\"address\";s:0:\"\";s:3:\"url\";s:0:\"\";}s:4:\"date\";a:1:{s:7:\"created\";i:1333093999;}s:4:\"show\";b:1;s:3:\"raw\";s:65:\"Testing 123\";s:5:\"xhtml\";s:77:\"\n" + 
				"<p>\n" + 
				"Testing 123\n" + 
				"\n" + 
				"</p>\n" + 
				"\";s:6:\"parent\";N;s:7:\"replies\";a:0:{}s:3:\"cid\";s:32:\"4df1fa69356ef92dd48fca5f004b3dc5\";}";
		actual = all.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		all.clear();
		input = FileUtils.readTextFile(new File(METADIR+"/SampleDokuwiki-InputDiscussion_mult.comments"));
		all = tester.seperateComments(input);
		assertNotNull(all);
		assertEquals(2, all.size());
		expected = "s:32:\"5efeab0c9a77feb0248e8d84029b345f\";a:8:{s:4:\"user\";a:5:{s:2:\"id\";s:3:\"foo\";s:4:\"name\";s:11:\"Foo Bar\";s:4:\"mail\";s:22:\"foo@bar.org.br\";s:7:\"address\";s:0:\"\";s:3:\"url\";s:0:\"\";}s:4:\"date\";a:1:{s:7:\"created\";i:1253800808;}s:4:\"show\";b:1;s:3:\"raw\";s:110:\"tralala\";s:5:\"xhtml\";s:122:\"\n" + 
				"<p>\n" + 
				"tralala\n" + 
				"\n" + 
				"</p>\n" + 
				"\";s:6:\"parent\";N;s:7:\"replies\";a:0:{}s:3:\"cid\";s:32:\"5efeab0c9a77feb0248e8d84029b345f\";}";
		actual = all.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "s:32:\"daa35c667fbb6b1c369a711aeb5adad6\";a:8:{s:4:\"user\";a:5:{s:2:\"id\";s:3:\"ella\";s:4:\"name\";s:11:\"Ella Fitzgerald\";s:4:\"mail\";s:24:\"ella@jazz.org\";s:7:\"address\";s:0:\"\";s:3:\"url\";s:0:\"\";}s:4:\"date\";a:1:{s:7:\"created\";i:1254911413;}s:4:\"show\";b:1;s:3:\"raw\";s:122:\"bedoobedoo whoa!\";s:5:\"xhtml\";s:212:\"\n" + 
				"<p>\n" + 
				"bedoobedoo <b>whoa!</b>\n" + 
				"\n" + 
				"</p>\n" + 
				"\";s:6:\"parent\";N;s:7:\"replies\";a:0:{}s:3:\"cid\";s:32:\"daa35c667fbb6b1c369a711aeb5adad6\";}";
		actual = all.get(1);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testParseComment() {
		String input, expected;
		String xhtml = "<p>\n" + 
						"bedoobedoo <b>whoa!</b>\n" + 
						"\n" + 
						"</p>\n";
		input = "s:32:\"daa35c667fbb6b1c369a711aeb5adad6\";a:8:{s:4:\"user\";a:5:{s:2:\"id\";s:3:\"ella\";s:4:\"name\";s:11:\"Ella Fitzgerald\";s:4:\"mail\";s:24:\"ella@jazz.org\";s:7:\"address\";s:0:\"\";s:3:\"url\";s:0:\"\";}s:4:\"date\";a:1:{s:7:\"created\";i:1254911413;}s:4:\"show\";b:1;s:3:\"raw\";s:122:\"bedoobedoo whoa!\";s:5:\"xhtml\";s:212:\"\n" + xhtml + "\";s:6:\"parent\";N;s:7:\"replies\";a:0:{}s:3:\"cid\";s:32:\"daa35c667fbb6b1c369a711aeb5adad6\";}";
		
		Comment actual = tester.parseComment(input);
		
		expected = "ella";
		assertNotNull(actual);
		assertEquals(expected, actual.creator);
		
		expected = "\n"+xhtml;
		assertNotNull(actual);
		assertEquals(expected, actual.text);
		
		expected = "2009:10:07:06:30:13:00";
		assertNotNull(actual);
		assertEquals(expected, actual.timestamp);
		
		assertTrue(actual.isXhtml());
	}

	public void testConvert_One() throws IOException {
		File file = new File(SAMPLEPAGE);
		Page page = new Page(file);
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		Vector<Comment> actual = page.getAllCommentData();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("foo", actual.get(0).creator);
		assertTrue(actual.get(0).text.contains("<p>\nTesting 123"));
	}
	
	public void testConvert_Mult() throws IOException {
		String path = PAGESDIR+"/SampleDokuwiki-InputDiscussion_mult.txt";
		File file = new File(path);
		Page page = new Page(file);
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		
		Vector<Comment> actual = page.getAllCommentData();
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("foo", actual.get(0).creator);
		assertTrue(actual.get(0).text.contains("tralala"));
		assertEquals("ella", actual.get(1).creator);
		assertTrue(actual.get(1).text.contains("<b>whoa!</b>"));
	}
	
	public void testConvert_None() throws IOException {
		String path = PAGESDIR+"/SampleDokuwiki-InputDiscussion_none.txt";
		File file = new File(path);
		Page page = new Page(file);
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		
		assertNotNull(page.getAllCommentData());
		assertEquals(0, page.getAllCommentData().size());
	}	
	
	public void testConvert_NoID() throws IOException {
		String path = PAGESDIR+"/SampleDokuwiki-InputDiscussion_noid.txt";
		File file = new File(path);
		Page page = new Page(file);
		page.setOriginalText(FileUtils.readTextFile(file));
		tester.convert(page);
		Vector<Comment> actual = page.getAllCommentData();
		assertNotNull(actual);
		for (Comment comment : actual) {
			assertNotNull(comment);
			assertNull(comment.creator);
			assertEquals("\n" + 
					"<p>\n" + 
					"Asta!\n" + 
					"\n" + 
					"</p>\n" +
					"<p><b>Original Dokuwiki Commenter:</b> myrnaloy@thinman.org</p>\n", comment.text);
		}
	}
}
