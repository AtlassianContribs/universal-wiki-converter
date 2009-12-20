package com.atlassian.uwc.converters.socialtext;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.twiki.cleaners.RegularExpressionCleaner;
import com.atlassian.uwc.ui.Page;

public class ImageWhitespaceConverterTest extends TestCase {

	ImageWhitespaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	File testfile_nows = new File("abc.jpg");
	File testfile_space = new File("foo bar.jpg");
	File testfile_perc = new File("lorem%20ipsum.gif");
	Page page = new Page(null);
	protected void setUp() throws Exception {
		tester = new ImageWhitespaceConverter();
		PropertyConfigurator.configure("log4j.properties");
		page.addAttachment(testfile_nows);
		page.addAttachment(testfile_space);
		page.addAttachment(testfile_perc);
	}

	public void testConvertImageWhitespace_basicImage() {
		String input, expected, actual;
		
		input = "!abc.jpg!\n" +
				"!foo bar.jpg!\n" +
				"!lorem ipsum.gif!\n" +
				"";
		expected = "!abc.jpg!\n" +
				"!foo bar.jpg!\n" +
				"!lorem%20ipsum.gif!\n";
		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageWhitespace_basicLink() {
		String input, expected, actual;
		
		input = "[^abc.jpg]\n" +
				"[^foo bar.jpg]\n" +
				"[^lorem ipsum.gif]\n" +
				"";
		expected = "[^abc.jpg]\n" +
				"[^foo bar.jpg]\n" +
				"[^lorem%20ipsum.gif]\n";
		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageWhitespace_workspacePagename() {
		String input, expected, actual;
		
		input = "[space:page^abc.jpg]\n" +
				"[space:page^foo bar.jpg]\n" +
				"[space:page^lorem ipsum.gif]\n" +
				"!space:page^abc.jpg!\n" +
				"!space:page^foo bar.jpg!\n" +
				"!space:page^lorem ipsum.gif!\n" +
				"";
		expected = 	"[space:page^abc.jpg]\n" +
				"[space:page^foo bar.jpg]\n" +
				"[space:page^lorem%20ipsum.gif]\n" +
				"!space:page^abc.jpg!\n" +
				"!space:page^foo bar.jpg!\n" +
				"!space:page^lorem%20ipsum.gif!\n";

		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageWhitespace_params() {
		String input, expected, actual;
		
		input = "!abc.jpg|thumbnail!\n" +
				"!foo bar.jpg|width=50%!\n" +
				"!lorem ipsum.gif|width=200px!\n" +
				"";
		expected = "!abc.jpg|thumbnail!\n" +
				"!foo bar.jpg|width=50%!\n" +
				"!lorem%20ipsum.gif|width=200px!\n";
		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertImageWhitespace_alias() {
		String input, expected, actual;
		
		input = "[Alias|^abc.jpg]\n" +
				"[Alias|^foo bar.jpg]\n" +
				"[Alias|^lorem ipsum.gif]\n" +
				"";
		expected = "[Alias|^abc.jpg]\n" +
				"[Alias|^foo bar.jpg]\n" +
				"[Alias|^lorem%20ipsum.gif]\n";
		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImageWhitespace_notimagesyntax() {
		String input, expected, actual;
		
		input = "abc.jpg\n" +
				"foo bar.jpg\n" +
				"lorem ipsum.gif\n" +
				"";
		expected = "abc.jpg\n" +
				"foo bar.jpg\n" +
				"lorem ipsum.gif\n";
		actual = tester.convertImageWhitespace(input, page.getAttachments());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
