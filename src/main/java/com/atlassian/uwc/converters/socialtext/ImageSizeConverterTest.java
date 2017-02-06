package com.atlassian.uwc.converters.socialtext;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ImageSizeConverterTest extends TestCase {

	ImageSizeConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ImageSizeConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertImageSize() {
		String input, expected, actual;
		input = "!ed.jpeg size=scaled!\n" + 
				"!ed.jpeg size=large!\n" + 
				"!ed.jpeg size=small!\n" + 
				"!ed.jpeg size=medium!\n" + 
				"!ed.jpeg size=200!\n" + 
				"!ed.jpeg size=0x10!\n" + 
				"";
		expected = "!ed.jpeg|thumbnail!\n" + 
				"!ed.jpeg|width=50%!\n" + 
				"!ed.jpeg|width=5%!\n" + 
				"!ed.jpeg|width=20%!\n" + 
				"!ed.jpeg|width=200px!\n" + 
				"!ed.jpeg|width=0px!\n" + 
				"";
		actual = tester.convertImageSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertValue() {
		String input, expected, actual;
		input = "scaled";
		expected = "thumbnail";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "large";
		expected = "width=50%";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "medium";
		expected = "width=20%";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "small";
		expected = "width=5%";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "200";
		expected = "width=200px";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "0x10";
		expected = "width=0px";
		actual = tester.convertValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testThumbnailNotBmp() {
		String input, expected, actual;
		input = "!ed.bmp size=scaled!\n" + 
				"";
		expected = "!ed.bmp!\n" + 
				"";
		actual = tester.convertImageSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
