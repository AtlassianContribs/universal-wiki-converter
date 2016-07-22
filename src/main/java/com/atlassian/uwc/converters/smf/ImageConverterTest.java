package com.atlassian.uwc.converters.smf;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Set<File> attachments = null;
	protected void setUp() throws Exception {
		tester = new ImageConverter();
		PropertyConfigurator.configure("log4j.properties");
		attachments = new HashSet<File>();
		File file1 = new File("1_cow.jpg");
		File file2 = new File("2_cow.jpg_thumb.jpg");
		attachments.add(file1);
		attachments.add(file2);
	}

	public void testConvertImages() {
		String input, expected, actual;
		input = "[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image[/img]";
		expected = "!1_cow.jpg!";
		actual = tester.convertImages(input, attachments);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[img]http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image[/img]";
		expected = "!2_cow.jpg_thumb.jpg!";
		actual = tester.convertImages(input, attachments);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImages_Links() {
		String input, expected, actual;
		input = "[http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image|" +
				"http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=1;image]";
		expected = "[^1_cow.jpg]";
		actual = tester.convertImages(input, attachments);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[alias|url=http://localhost:8081/SMF/index.php?action=dlattach;topic=6.0;attach=2;image]";
		expected = "[alias|^2_cow.jpg_thumb.jpg]";
		actual = tester.convertImages(input, attachments);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks_NotImages() {
		String input, expected, actual;
		input = "[http://www.google.com]";
		expected = input;
		actual = tester.convertImages(input, attachments);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetFiles() {
		HashMap<String, File> actual = tester.getFiles(attachments);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertTrue(actual.containsKey("1"));
		assertTrue(actual.containsKey("2"));
		File file1 = actual.get("1");
		File file2 = actual.get("2");
		assertNotNull(file1);
		assertNotNull(file2);
		assertEquals("1_cow.jpg", file1.getName());
		assertEquals("2_cow.jpg_thumb.jpg", file2.getName());
	}
	
}
