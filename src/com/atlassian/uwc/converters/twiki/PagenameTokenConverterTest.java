package com.atlassian.uwc.converters.twiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class PagenameTokenConverterTest extends TestCase {

	PagenameTokenConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PagenameTokenConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertPagenameToken() {
		String input, expected, actual;
		input = "~UWCTOKENCURRENTPAGE~";
		String pagetitle = "Testin123";
		expected = pagetitle;
		Page page = new Page(null);
		page.setName(pagetitle);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertInLink() {
		String input, expected, actual;
		input = "This is a sample for handling attachments, including edge cases such as UWC-286\n" + 
				"\n" + 
				"        *  Testing\n" + 
				"                *  UWC_TOKEN_OL2007 OS/Sybase Testing Spreadsheet|http://uwctest:~UWCTOKENCURRENTPAGE~^2007-OS-sybase-testing-matrix.xlsUWC_TOKEN_CL\n" + 
				"";
		String title = "SampleTwiki-InputAttachments";
		expected = "This is a sample for handling attachments, including edge cases such as UWC-286\n" + 
				"\n" + 
				"        *  Testing\n" + 
				"                " +
				"*  UWC_TOKEN_OL2007 OS/Sybase Testing Spreadsheet|http://uwctest:" + title +
				"^2007-OS-sybase-testing-matrix.xlsUWC_TOKEN_CL\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(title);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
