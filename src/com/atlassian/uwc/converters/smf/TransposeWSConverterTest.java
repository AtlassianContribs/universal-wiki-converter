package com.atlassian.uwc.converters.smf;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TransposeWSConverterTest extends TestCase {

	TransposeWSConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TransposeWSConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testTransposeWS() {
		String input, expected, actual;
		input = "[b]   \n" +
				"    \n" +
				"finally abc\ndef\n\n\t\t\r\n" +
				"[/b]";
		expected = "   \n" +
				"    \n" +
				"[b]finally abc def[/b]\n\n\t\t\r\n" +
				"";
		actual = tester.transposeWs(input, "b");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
