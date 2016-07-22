package com.atlassian.uwc.converters.socialtext;

import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class InlineExternalImagesConverterTest extends TestCase {

	InlineExternalImagesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new InlineExternalImagesConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertImages() {
		String input, expected, actual;
		input = "[http://localhost:8082/download/attachments/426023/double+facepalm.jpg]\n" +
				"[http://www.google.com]";
		expected = input;
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Properties props = new Properties();
		props.setProperty("extensions", "gif,jpg,jpeg,bmp,png");
		tester.setProperties(props);
		expected = "!http://localhost:8082/download/attachments/426023/double+facepalm.jpg!\n" +
				"[http://www.google.com]";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testGetExtensions() {
		Properties props = new Properties();
		props.setProperty("extensions", "gif,jpg,jpeg,bmp,png");
		tester.setProperties(props);
		Vector<String> actual = tester.getExtensions();
		assertNotNull(actual);
		assertEquals(5, actual.size());
		String ext0 = actual.get(0);
		String ext1 = actual.get(1);
		String ext2 = actual.get(2);
		String ext3 = actual.get(3);
		String ext4 = actual.get(4);
		
		assertEquals("gif", ext0);
		assertEquals("jpg", ext1);
		assertEquals("jpeg", ext2);
		assertEquals("bmp", ext3);
		assertEquals("png", ext4);
		
	}
}
