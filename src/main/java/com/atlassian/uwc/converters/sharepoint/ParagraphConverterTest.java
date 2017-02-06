package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

public class ParagraphConverterTest extends TestCase {

	ParagraphConverter tester = null;
	protected void setUp() throws Exception {
		tester = new ParagraphConverter();
	}
	
	public void testConvertParagraph() {
		String input = "<html>abc <p>def</p> ghi</html>";
		String expected = "<html>abc \n" +
				"def\n" +
				" ghi</html>";
		String actual = tester.convertParas(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"...\n" +
				"<p/>\n" +
				"<p>\n" +
				"blah\n" +
				"<em>emp</em></p>" +
				"</html>";
		expected = "<html>\n" +
		"...\n" +
		"\n\n" +
		"\n" +
		"blah\n" +
		"<em>emp</em>\n" +
		"</html>";
		actual = tester.convertParas(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
