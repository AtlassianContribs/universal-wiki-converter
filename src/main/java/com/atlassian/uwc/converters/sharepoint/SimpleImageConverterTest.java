package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Attribute;
import org.dom4j.tree.DefaultAttribute;

public class SimpleImageConverterTest extends TestCase {

	SimpleImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new SimpleImageConverter();
		String attachmentDir = "http://artemis2.sharepoint.com";
		tester.setAttachmentDirectory(attachmentDir);
	}

	public void testConvertImages() {
		String input = "<html><img src=\"/Shared%20Documents/10year.jpg\"/></html>";
		String expected = "<html>!http://artemis2.sharepoint.com/Shared%20Documents/10year.jpg!</html>";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImages2() {
		String input = "<html><img src=\"/Shared%20Documents/10year.jpg\" height=\"200\"/></html>";
		String expected = "<html>!http://artemis2.sharepoint.com/Shared%20Documents/10year.jpg|height=\"200\"!</html>";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImages3() {
		String input = "<html><img src=\"/Shared%20Documents/10year.jpg\" height=\"200\" alt=\"Test Image\" align=\"right\"/></html>";
		String expected = "<html>!http://artemis2.sharepoint.com/Shared%20Documents/10year.jpg|height=\"200\", alt=\"Test Image\", align=\"right\"!</html>";
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetSrc() {
		Attribute input = new DefaultAttribute("src", "abc.jpg");
		String expected = "abc.jpg";
		String actual = tester.getValue(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateAttributeString() {
		Attribute input1 = new DefaultAttribute("height", "200");
		Attribute input2 = new DefaultAttribute("alt", "Testing 123");
		Attribute input3 = new DefaultAttribute("align", "center");
		List<Attribute> atts = new Vector<Attribute>();
		atts.add(input1);
		atts.add(input2);
		atts.add(input3);
		
		String expected = "height=\"200\", alt=\"Testing 123\", align=\"center\"";
		String actual = tester.createAttributeString(atts);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateConfluenceImageString() {
		String src = "/Shared Documents/input.jpg";
		String attString = "height=\"200\"";
		String expected = "!http://artemis2.sharepoint.com/Shared Documents/input.jpg|height=\"200\"!";
		String actual = tester.createConfluenceImageString(src, attString);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		src = "http://www.alreadyhaveserver.com/somefile.jpg";
		attString = "";
		expected = "!" + src + "!";
		actual = tester.createConfluenceImageString(src, attString);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
