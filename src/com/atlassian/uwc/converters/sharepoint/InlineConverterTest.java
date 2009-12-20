package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class InlineConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	InlineConverter tester = null;
	protected void setUp() throws Exception {
		tester = new InlineConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertInline() {
		String input = "<html>" +
				"<strong>Testing Table" +
				"<br/>" +
				"</strong>" +
				"<br/>Simple Table<br/></html>";
		String expected = "<html>" +
				"<strong>Testing Table" +
				"</strong>" +
				"<br/>Simple Table<br/></html>";
		String actual = tester.convertInline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertInline2() {
		String input = "<html><p>abc</p><strong><br/></strong></html>";
		String expected = "<html><p>abc</p></html>";
		String actual = tester.convertInline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testPreserveInlineWS() {
		String input, expected, actual;
		input = "<html>" +
				"<p>Each status has a <strong>severity color</strong> associated with it.</p>" +
				"</html>";
		expected = "<html><p>Each status has a <strong>severity color</strong> associated with it.</p></html>";
		actual = tester.convertInline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
				"<div class=\"ExternalClassEDE3A2A0FAF34C62B1E98D765C5078BF\">" +
				"<span style=\"FONT-FAMILY: Arial\"><font size=\"2\">" +
				"<strong>Severity: </strong>" +
				"Each status has a" +
				" " +
				"<strong>severity color</strong>" +
				" " +
				"associated with it. When a status is created dynamically, its color defaults to ~SYELLOW~T. The default color can be updated later to a different color (~SGREEN~T or ~SRED~T) via a GUI front-end." +
				"</font></span></div>\n" + 
				"</html>";
		expected = input;
		actual = tester.convertInline(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
