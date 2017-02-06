package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Attachment;
import com.atlassian.uwc.ui.Page;

public class DokuwikiAttachmentConverterTest extends TestCase {

	DokuwikiAttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	String attachmentdirectory = "sampleData/dokuwiki/junit_resources/attachments";
	protected void setUp() throws Exception {
		tester = new DokuwikiAttachmentConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.setAttachmentDirectory(attachmentdirectory);
	}

	public void testConvertAttachments() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "{{wiki:dokuwiki-128.png}}\n" +
				"{{:cow.jpg|}}\n";
		expected = "!dokuwiki-128.png!\n" +
				"!cow.jpg!\n"; 
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(2, page.getAllAttachmentData().size());
		Vector<File> testatt = new Vector<File>(page.getAttachments());
		assertEquals("cow.jpg", testatt.get(0).getName());
		assertEquals("dokuwiki-128.png", testatt.get(1).getName());

	}
	public void testConvertAttachments2() {
		tester.setAttachmentDirectory("sampleData/hierarchy/doku-media/");
		Page page = new Page(null);
		String input, expected, actual;
		input = 
				"{{:images:cows:cow.jpg}} - render\n" + 
				"{{images:hobbespounce.gif|}}\n" + 
				"{{images:hobbespounce.gif|Alias?}}\n" + 
				"{{images:cow.pdf}}\n" +
				"{{images:cow.pdf|}}\n" +
				"{{images:cow.pdf|Alias}}\n";
		expected = "!cow.jpg! - render\n" +
				"!hobbespounce.gif!\n" +
				"!hobbespounce.gif!\n" +
				"[^cow.pdf]\n"+
				"[^cow.pdf]\n"+
				"[Alias|^cow.pdf]\n";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(3, page.getAllAttachmentData().size());
		Vector<File> testatt = new Vector<File>(page.getAttachments());
		assertEquals("cow.pdf", testatt.get(0).getName());
		assertEquals("hobbespounce.gif", testatt.get(1).getName());
		assertEquals("cow.jpg", testatt.get(2).getName());
	}
	

	public void testConvertImages_wSize() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "{{images:cow.jpg?50}} - render\n" + 
			"{{:images:cow.jpg?50}} - render\n" +
			"{{images:cow.jpg?50x100}} - render\n" + 
			"{{:images:cow.jpg?50x100}} - render\n" +
			"{{images:cow.jpg?50}}\n" + //what do we do if no extension?
			"{{images:cow.jpg?words&50}}\n" + //what do we do if non-number params?
			"{{images:cow.jpg?words&50x100}}"; //what do we do if non-number params?
		expected = "!cow.jpg|width=50px! - render\n" +
			"!cow.jpg|width=50px! - render\n" +
			"!cow.jpg|width=50px,height=100px! - render\n" +
			"!cow.jpg|width=50px,height=100px! - render\n" +
			"!cow.jpg|width=50px!\n" +
			"!cow.jpg|width=50px!\n" +
			"!cow.jpg|width=50px,height=100px!";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(1, page.getAllAttachmentData().size());
		Vector<File> testatt = new Vector<File>(page.getAttachments());
		assertEquals("cow.jpg", testatt.get(0).getName());
	}
	public void testCreateRelativePath() {
		String input, expected, actual;
		input = "wiki:dokuwiki-128.png";
		expected = "/wiki/dokuwiki-128.png";
		actual = tester.createRelativePath(input, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = ":cow.jpg"; //parent's cow.jpg?
		String rel = "foo/";
		expected = "/foo/cow.jpg";
		actual = tester.createRelativePath(input, rel);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/foo/bar/");
		input = ":cow.jpg"; //parent's cow.jpg?
		rel = "/foo/bar/foo";
		expected = "/foo/cow.jpg";
		actual = tester.createRelativePath(input, rel);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/foo/bar/");
		input = ":notrel:cow.jpg"; //not that relative
		rel = "/foo/bar/foo";
		expected = "/notrel/cow.jpg";
		actual = tester.createRelativePath(input, rel);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testCreateRelativePath_case() {
		String input, expected, actual;
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/foo/bar/");
		input = ":Notrel:cow.jpg"; //case in the path needs to be lowercased
		String rel = "/foo/bar/foo";
		expected = "/notrel/cow.jpg";
		actual = tester.createRelativePath(input, rel);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("filepath-hierarchy-ignorable-ancestors", "/foo/bar/");
		input = ":Notrel:Cow.jpg"; //but the case in the filename needs to be maintained
		rel = "/foo/bar/foo";
		expected = "/notrel/Cow.jpg";
		actual = tester.createRelativePath(input, rel);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAltCase() {
		File file = new File("sampleData/dokuwiki/sampledokuwiki-inputbasic.txt");
		File actual = tester.altCase(file);
		String expected = "SampleDokuwiki-InputBasic.txt";
		assertNotNull(actual);
		assertEquals(expected, actual.getName());
		assertTrue(actual.exists());
	}
}
