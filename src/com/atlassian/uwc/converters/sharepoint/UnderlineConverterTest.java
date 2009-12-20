package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

public class UnderlineConverterTest extends TestCase {

	UnderlineConverter tester = null;
	protected void setUp() throws Exception {
		tester = new UnderlineConverter();
	}
	
	public void testConvertUnderline() {
		String input = "<html>abc <u>def</u> ghi</html>";
		String expected = "<html>abc +def+ ghi</html>";
		String actual = tester.convertUnderline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"...\n" +
				"<u>\n" +
				"blah</u>\n" +
				"<em>emp</em>" +
				"</html>";
		expected = "<html>\n" +
		"...\n" +
		"+blah+\n" +
		"<em>emp</em>" +
		"</html>";
		actual = tester.convertUnderline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
