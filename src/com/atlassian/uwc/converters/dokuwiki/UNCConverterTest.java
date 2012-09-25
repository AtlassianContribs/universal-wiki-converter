package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class UNCConverterTest extends TestCase {

	UNCConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new UNCConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertUNCBasic() { 
		String input, expected, actual;
		input = "[[\\\\path\\to\\file.txt]]\n" + 
				"";
		expected = "[\\\\path\\to\\file.txt|file://path/to/file.txt]\n";
		actual = tester.convertUNC(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertUNCAlias() {
		String input, expected, actual;
		input = "[[\\\\path\\to\\file.txt|alias]]\n"; 
		expected = "[alias|file://path/to/file.txt]\n";
		actual = tester.convertUNC(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertUNC2() {
		String input, expected, actual;
		input =  "[[\\\\path\\to\\file with ws.txt|alias]]\n";  
		expected = "[alias|file://path/to/file with ws.txt]\n";
		actual = tester.convertUNC(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
