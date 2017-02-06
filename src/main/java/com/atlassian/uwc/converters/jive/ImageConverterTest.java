package com.atlassian.uwc.converters.jive;

import java.util.HashMap;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ImageConverter();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = new Properties();
		props.put("attachmentdata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_attachments.txt");
		props.put("titledata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_titles.txt");
		props.put("internaljivedomain", "http://wiki.abc.com");
		tester.setProperties(props);
		HashMap<String, String> keys = new HashMap<String, String>();
		keys.put("200-600", "testconf");
		keys.put("201-14", "testconf2");
		keys.put("202-14", "testconf3");
		SpaceConverter.setSpacekeys(keys );
	}
	
	public void testConvertImage() {
		String input, expected, actual;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"<img __jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" src=\"cow.jpg\" width=\"70\"/>";
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"!cow.jpg|width=70px! ";
		actual = tester.convertImage(input, "9009-102");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageByName() {
		String input, expected, actual;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"<img src=\"cow.jpg\" />"; //sometimes they don't use the id
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"!cow.jpg! ";
		actual = tester.convertImage(input, "9009-102");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetParams() {
		String input, expected, actual;
		input =  "__jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" src=\"cow.jpg\" width=\"70\"/";
		expected = "|width=70px";
		actual = tester.getParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testImageWithThumbnail() {
		String input, expected, actual;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"<img __jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" class=\"jive-image-thumbnail jive-image\" " +
				"src=\"cow.jpg\" width=\"500\"/>\n" +
				"<img __jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" width=\"500\"" + //width param before thumbnail
				"class=\"jive-image-thumbnail jive-image\" src=\"cow.jpg\"/>";
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
					"!cow.jpg|thumbnail! \n" +
					"!cow.jpg|thumbnail! ";
		actual = tester.convertImage(input, "9009-102");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageLinks() {
		String input, expected, actual;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"<a _jive_internal=\"true\" href=\"/servlet/JiveServlet/download/9009-28-1003/test.txt\">Test File</a>\n" +
				"<a _jive_internal=\"true\" href=\"/servlet/JiveServlet/download/9010-28-1005/test.txt\">Test File on Other Page</a>\n";
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"[Test File|^test.txt]\n" +
				"[Test File on Other Page|testconf2:Other Page^foo.jpg]\n";
		tester.initTitleData();
		actual = tester.convertAttachmentLinks(input, "9009-102");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertExternalImages() {
		String input, expected, actual;
		input ="{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
		"<img height=\"0\" src=\"http://localhost:1990/confluence/download/attachments/3867278/doublefacepalm.jpg\" width=\"0\"/>";
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" +
				"!http://localhost:1990/confluence/download/attachments/3867278/doublefacepalm.jpg|height=0px,width=0px! ";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testImagePostWS() {
		String input, expected, actual;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
		"<img __jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" class=\"jive-image-thumbnail jive-image\" " +
		"src=\"cow.jpg\" width=\"500\"/>" +
		"<img __jive_ID=\"1000\" __jive_id=\"1000\" alt=\"cow.jpg\" width=\"500\"" + //width param before thumbnail
		" src=\"cow.jpg\"/>" +
		"some text";
		expected = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
		"!cow.jpg|thumbnail! " +
		"!cow.jpg|width=500px! " +
		"some text";
		actual = tester.convertImage(input, "9009-102");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
