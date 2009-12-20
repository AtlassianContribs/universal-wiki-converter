package com.atlassian.uwc.converters.twiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = new Properties();
	protected void setUp() throws Exception {
		tester = new ImageConverter();
		props.setProperty("vars-url", "http://localhost:8092");
		props.setProperty("vars-attachurl", "~UWCTOKENURL~/display/~UWCTOKENCURRENTSPACE~/~UWCTOKENCURRENTPAGE~");
		props.setProperty("vars-attachurlpath", "~UWCTOKENCURRENTSPACE~:~UWCTOKENCURRENTPAGE~");
		props.setProperty("vars-puburl", "~UWCTOKENURL~/plugins/servlet/confluence/default/Global");
		props.setProperty("vars-web", "~UWCTOKENCURRENTSPACE~");
		props.setProperty("spacekey", "foobar"); //will be provided by converter engine, not converter properties
		props.setProperty("remove-twiki-path-1", "http://192.168.2.104/twiki/bin/viewfile/");
		props.setProperty("extensions-image", "bmp,jpg,jpeg,png,gif");
		props.setProperty("extensions-file", "xls,zip,doc,ppt,pdf,tar.gz,tar");
		tester.setProperties(props);
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvertImage_basic() {
		String input, expected, actual;
		input = "<img src=\"uwctest:SampleTwiki-InputAttachments2/cow.jpg\" alt=\"cow.jpg\" width=\'450\' height=\'319\' />";
		expected = "!uwctest:SampleTwiki-InputAttachments2^cow.jpg|alt=\"cow.jpg\", width=\"450\", height=\"319\"!";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_fullpath() {
		String input, expected, actual;
		input = "[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/SampleTwiki-InputAttachments2/cow.jpg]]";
		expected = "[[uwctest:SampleTwiki-InputAttachments2^cow.jpg]]" + 
				"";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_NotLink() {
		String input, expected, actual;
		input = "this one:\n" +
				"[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" +
				"SampleTwiki-InputAttachments2/cow.jpg]]\n" + 
				"but not this:\n" +
				"[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" +
				"SampleTwiki-InputAttachments2]]\n" +
				"[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" + 
				"SampleTwiki-InputAttachments2][alias]";
		expected = "this one:\n" +
				"[[uwctest:SampleTwiki-InputAttachments2^cow.jpg]]\n" + 
				"but not this:\n" +
				"[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" +
				"SampleTwiki-InputAttachments2]]\n" +
				"[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" + 
				"SampleTwiki-InputAttachments2][alias]";

		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageLink_alias() {
		String input, expected, actual;
		input = "[[http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/" + 
				"SampleTwiki-InputAttachments2/cow.jpg][alias]";
		expected = "[[uwctest:SampleTwiki-InputAttachments2^cow.jpg][alias]";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImageLink_file_alias() {
		String input, expected, actual;
		input = "[[http://localhost:8092/display/uwctest/SampleTwiki-InputAttachments/2007-OS-sybase-testing-matrix.xls][2007 OS/Sybase Testing Spreadsheet]]";
		expected = "[[uwctest:SampleTwiki-InputAttachments^2007-OS-sybase-testing-matrix.xls][2007 OS/Sybase Testing Spreadsheet]]";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_fulltwikipath() {
		String input, expected, actual;
		input = "http://192.168.2.104/twiki/bin/viewfile/uwctest/SampleTwiki-InputAttachments2?rev=1;filename=cow.jpg";
		expected = "!uwctest:SampleTwiki-InputAttachments2^cow.jpg!";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_fullpath_nolink() {
		String input, expected, actual;
		input = "http://localhost:8092/plugins/servlet/confluence/default/Global/uwctest/SampleTwiki-InputAttachments2/cow.jpg";
		expected = "!uwctest:SampleTwiki-InputAttachments2^cow.jpg!";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_imghtml() {
		String input, expected, actual;
		input = "<img src=\"http://localhost:8092/plugins/servlet/confluence/default/Global/" +
				"uwctest/SampleTwiki-InputAttachments2/cow.jpg\" width=\"88\" height=\"31\" border=\"0\" " +
				"alt=\"logo\" />";
		expected = "!uwctest:SampleTwiki-InputAttachments2^cow.jpg|width=\"88\", height=\"31\", border=\"0\", alt=\"logo\"!";
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImage_shouldignore1() {
		String input, expected, actual;
		input = "This is a sample for automatically choosing numbers for a twiki numbered list syntax as described in UWC-339 and here:\n" + 
				"http://www.the-data-mine.com/bin/view/TWiki/TextFormattingRules\n" + 
				"\n" + 
				"   1. Sushi\n" + 
				"   1. Dim Sum\n" + 
				"   1. Fondue\n" + 
				"\n" + 
				"   A. Sushi\n" + 
				"   A. Dim Sum\n" + 
				"   A. Fondue\n" + 
				"\n" + 
				"   i. Sushi\n" + 
				"   i. Dim Sum\n" + 
				"   i. Fondue\n" + 
				"\n" + 
				"   1. Provide means to allow for a configurable IUnknown.\n" + 
				"   a. Unsafe but fast reference counting\n" + 
				"   a. Thread safe reference counting via spin locks\n" + 
				"   a. No reference counting.\n" + 
				"   1. Support enumeration of factories supporting a given interface.\n" + 
				"   1. \"Slicing\" of components.\n" + 
				"";
		expected = input;
		actual = tester.convertImage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
