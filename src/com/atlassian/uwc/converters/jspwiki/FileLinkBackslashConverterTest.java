package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FileLinkBackslashConverterTest extends TestCase {

	FileLinkBackslashConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new FileLinkBackslashConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertFileLinkBackslashes() {
		String input, expected, actual;
		input = "[file:\\\\c:\\foo\\bar.txt]";
		expected = "[file://c:/foo/bar.txt]";
		actual = tester.convertFileLinkBackslashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_alias() {
		String input, expected, actual;
		input = "[alias|file:\\\\c:\\foo\\bar.txt]";
		expected = "[alias|file://c:/foo/bar.txt]";
		actual = tester.convertFileLinkBackslashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_ws() {
		String input, expected, actual;
		input = "[alias | file:\\\\c:\\foo\\bar.txt]";
		expected = "[alias |file://c:/foo/bar.txt]";
		actual = tester.convertFileLinkBackslashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_real() {
		String input, expected, actual;
		input = "* [DSP Track Attributes | file:\\\\Filesrv11\\public\\09_Projects\\9.1_Active\\DSP_Track_Attributes\\DSP_Track_Attributes_Project_Workbook.xls]\n" + 
				"* [Content Initiatives 2007/2008 | file:\\\\Filesrv11\\public\\09_Projects\\9.1_Active\\2007_Content_Initiative\\2007_Content_Initiative_Project_Workbook.xls]\n" + 
				"* [Music Enrichment 2.5.1 | file:\\\\FILESRV11\\PUBLIC\\09_Projects\\9.1_Active\\Music_Enrichment\\ME_Project_Workbook.xls]\n" + 
				"* [Device 2.5.1 | file:\\\\FILESRV11\\PUBLIC\\09_Projects\\9.1_Active\\Device\\Device_2.5\\Device_2.5_Project_Workbook.xls]\n" + 
				"";
		expected = "* [DSP Track Attributes |file://Filesrv11/public/09_Projects/9.1_Active/DSP_Track_Attributes/DSP_Track_Attributes_Project_Workbook.xls]\n" + 
				"* [Content Initiatives 2007/2008 |file://Filesrv11/public/09_Projects/9.1_Active/2007_Content_Initiative/2007_Content_Initiative_Project_Workbook.xls]\n" + 
				"* [Music Enrichment 2.5.1 |file://FILESRV11/PUBLIC/09_Projects/9.1_Active/Music_Enrichment/ME_Project_Workbook.xls]\n" + 
				"* [Device 2.5.1 |file://FILESRV11/PUBLIC/09_Projects/9.1_Active/Device/Device_2.5/Device_2.5_Project_Workbook.xls]\n";
		actual = tester.convertFileLinkBackslashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
