package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BoldConverterTest extends TestCase {

	BoldConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new BoldConverter();
	}
	
	public void testConvertBold() {
		String input = "<html>Not bold <strong>bold</strong> not</html>";
		String expected = "<html>Not bold *bold* not</html>";
		String actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"...\n" +
				"<strong>\n" +
				"blah</strong>\n" +
				"<em>emp</em>" +
				"</html>";
		expected = "<html>\n" +
		"...\n" +
		"*blah*\n" +
		"<em>emp</em>" +
		"</html>";
		actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	
	public void testConvertBold2()  {
		String input = "<html>Testing <strong><br/></strong> 123</html>";
		String expected = "<html>Testing <br/> 123</html>";
		String actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBold3() {
		String input, expected, actual;
		input = "<html><p><strong>abc<i>def</i>ghi</strong></p></html>";
		expected = "<html><p>*abc<i>def</i>ghi*</p></html>";
		actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBold4() {
		String input, expected, actual;
		input = "<html><p><b>abc</b></p></html>";
		expected = "<html><p>*abc*</p></html>";
		actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBold5() {
		String input, expected, actual;
		input = "<html>" +
				"<b>$1</b><br/><strong>$2.99</strong><br/>" + 
				"</html>";
		expected = "<html>" +
				"*$1*<br/>" +
				"*$2.99*<br/>" + 
				"</html>";
		actual = tester.removeWhitespaceOnlyConversions(expected, "*");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertBold6() {
		String input, expected, actual;
		input = "<html><p>abc<strong/>def</p></html>";
		expected = "<html><p>abcdef</p></html>";
		actual = tester.convertBold(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
