package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class EmphasisConverterTest extends TestCase {

	EmphasisConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new EmphasisConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertEmphasis() {
		String input = "<html>abc <em>def</em> ghi</html>";
		String expected = "<html>abc _def_ ghi</html>";
		String actual = tester.convertEmphasis(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html>\n" +
		"...\n" +
		"<strong>" +
		"blah</strong>\n" +
		"<em>emp</em>" +
		"</html>";
		expected = "<html>\n" +
		"...\n" +
		"<strong>blah</strong>\n" +
		"_emp_" +
		"</html>";
		actual = tester.convertEmphasis(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertEmphasis2() {
		String input = "<html>abc <i>def</i> ghi</html>";
		String expected = "<html>abc _def_ ghi</html>";
		String actual = tester.convertEmphasis(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
