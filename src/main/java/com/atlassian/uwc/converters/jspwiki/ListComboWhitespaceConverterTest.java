package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ListComboWhitespaceConverterTest extends TestCase {

	ListComboWhitespaceConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ListComboWhitespaceConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertListComboWs() {
		String input, expected, actual;
		input = "*_*A B C.*_\n" + 
				"*_*D E F.*_\n" + 
				"*_*G H I.*_\n" + 
				"";
		expected = "* _*A B C.*_\n" + 
				"* _*D E F.*_\n" + 
				"* _*G H I.*_\n";
		actual = tester.convertListComboWs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDoNothingIfNoList() {
		String input, expected, actual;
		input = "*bold*";
		expected = "*bold*";
		actual = tester.convertListComboWs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
