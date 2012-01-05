package com.atlassian.uwc.converters.trac;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LinkAttachmentConverterTest extends TestCase {

	LinkAttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		tester = new LinkAttachmentConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertAttachmentLinks() {
		String input, expected, actual;
		input = "[attachment:Attached_File.pdf]. \n"; 
		expected = "[^Attached_File.pdf]. \n" ;
		actual = tester.convertAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertAttachmentLinks_Alias() {
		String input, expected, actual;
		input = "[attachment:foo.gif alias]\n" + 
				"[attachment:foo.gif|alias]\n";
		expected = "[^foo.gif|alias]\n" + //aliases must be backwards at this stage 
				"[^foo.gif|alias]\n";
		actual = tester.convertAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertAttachmentLinks_Quotes() {
		String input, expected, actual;
		input = "[attachment:\"abc.gif\"]\n"+
				"[attachment:\"a b c.gif\" \"alias alias\"]\n" +
				"[attachment:foo.gif \"alias alias\"]\n" +
				"[attachment:\"a b c.gif\"|alias]\n";
		expected = "[^abc.gif]\n"+
				"[^a b c.gif|alias alias]\n" +
				"[^foo.gif|alias alias]\n" +
				"[^a b c.gif|alias]\n";
		actual = tester.convertAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
