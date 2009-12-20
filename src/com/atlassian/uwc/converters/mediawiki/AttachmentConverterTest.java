package com.atlassian.uwc.converters.mediawiki;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	AttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new AttachmentConverter();
	}

	public void testFoundFileBasic() {
		String filename = "page.txt";
		Vector<String> files = new Vector<String>();
		files.add(filename);
		boolean expected = true;
		boolean actual = tester.foundFile(files, filename);
		assertEquals(expected, actual);
		
		String otherFilename = "page2.txt";
		expected = false;
		actual = tester.foundFile(files, otherFilename);
		assertEquals(expected, actual);
	}
	
	public void testFoundFileCaseInsensitive() {
		String filename = "page.txt";
		String caseFilename = "Page.txt";
		Vector<String> files = new Vector<String>();
		files.add(filename);
		boolean expected = true;
		boolean actual = tester.foundFile(files, caseFilename);
		assertEquals(expected, actual);
	}
	public void testFoundFileMedia() {
		Page page = new Page(new File(""));
		String input = "[^Wiki.png]";
		page.setOriginalText(input);
		Vector<String> actual = tester.getSoughtAttachmentNames(page);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Wiki.png", actual.get(0));
		
		input = "[alias|^Wiki.png]";
		page.setOriginalText(input);
		actual = tester.getSoughtAttachmentNames(page);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Wiki.png", actual.get(0));
	}	

	public void testGetNamesFromLinkSyntax() {
		String input = "[^Wiki.png]";
		Set<String> set = new TreeSet<String>();
		Set<String> actualSet = tester.getNamesFromLinkSyntax(set, input);
		assertNotNull(actualSet);
		assertEquals(1, actualSet.size());
		String actualName = null;
		for (String name : actualSet) {
			actualName = name;
		}
		assertEquals("Wiki.png", actualName);
		
		input = "[alias|^Wiki.png]";
		actualSet = new TreeSet<String>();
		actualSet = tester.getNamesFromLinkSyntax(set, input);
		assertNotNull(actualSet);
		assertEquals(1, actualSet.size());
		actualName = null;
		for (String name : actualSet) {
			actualName = name;
		}
		assertEquals("Wiki.png", actualName);
	}
	
	public void testAttachingMedia() {
		String input = "[^Wiki.png]";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		String attachmentDirectory = "/Users/laura/Code/Workspace/mediawiki/images";
		tester.addAttachmentsToPage(page, attachmentDirectory);
		
		Set<File> attachments = page.getAttachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.size());
		File file = null;
		for (File attachment : attachments) {
			file = attachment;
		}
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertEquals("Wiki.png", file.getName());
	}
	public void testAttachingImage() {
		String input = "!Wiki.png!";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		String attachmentDirectory = "/Users/laura/Code/Workspace/mediawiki/images";
		tester.addAttachmentsToPage(page, attachmentDirectory);
		
		Set<File> attachments = page.getAttachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.size());
		File file = null;
		for (File attachment : attachments) {
			file = attachment;
		}
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertEquals("Wiki.png", file.getName());
	}
	
	public void testAttachingImageWithContext() {
		String input = "uwc-100: attachment converter for mediawiki images is case sensitive when the syntax isn't\n" +
			"!Wiki.png!\n" +
			"!wiki.png!\n" +
			"\n" +
			"uwc-101: Mediawiki image conversion syntax needs to be case insensitive\n" +
			"!Wiki.png!\n" +
			"!Wiki.png!\n";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		String attachmentDirectory = "/Users/laura/Code/Workspace/mediawiki/images";
		tester.addAttachmentsToPage(page, attachmentDirectory);
		
		Set<File> attachments = page.getAttachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.size());
		File file = null;
		for (File attachment : attachments) {
			file = attachment;
		}
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertEquals("Wiki.png", file.getName());
	}
	
	public void testGettingAttachmentPathsWithAliases() {
		String input = "uwc-100: attachment converter for mediawiki images is case sensitive when the syntax isn't\n" +
		"!Wiki.png!\n" +
		"!wiki.png!\n" +
		"\n" +
		"uwc-101: Mediawiki image conversion syntax needs to be case insensitive\n" +
		"!Wiki.png!\n" +
		"!Wiki.png|thumbnail!\n";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		Vector actual = tester.getSoughtAttachmentNames(page);
		//can be two here, as only the one with the correct 
		//caps on the filesystem will get uploaded
		assertEquals(2, actual.size()); 
		assertNotNull(actual);
		assertEquals("Wiki.png", actual.get(0));
	}
	
}
