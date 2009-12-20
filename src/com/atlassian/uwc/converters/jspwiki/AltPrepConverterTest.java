package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AltPrepConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	AltPrepConverter tester = null;
	protected void setUp() throws Exception {
		tester = new AltPrepConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testPrepareAltSyntax_RidClosingNewlines() {
		String input, expected, actual;
		input = "%%(text-decoration: underline)\n" + 
				"underlined text 4\n" + 
				"%%\n"; 
		expected = "%%(text-decoration: underline)\n" + 
				"underlined text 4%%\n";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_KeepGoodNL() {
		String input, expected, actual;
		input = "%%(text-decoration: underline) underlined text%%\n" + 
				"%%(text-decoration: underline)underlined text 2%%\n" + 
				"%%(text-decoration: underline)\n" + 
				"underlined text 3%%\n" + 
				"%%(text-decoration: underline)\n" + 
				"underlined text 4\n" + 
				"%%\n" + 
				"";
		expected = "%%(text-decoration: underline) underlined text%%\n" + 
				"%%(text-decoration: underline)underlined text 2%%\n" + 
				"%%(text-decoration: underline)\n" + 
				"underlined text 3%%\n" + 
				"%%(text-decoration: underline)\n" + 
				"underlined text 4%%\n";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_SingleNewlines() {
		String input, expected, actual;
		input = "%%(text-decoration: underline)foo\n" + 
				"bar\n" + 
				"gah\n" + 
				"%%\n" + 
				"";
		expected = "%%(text-decoration: underline)foo bar gah%%\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_MultNewlines() {
		String input, expected, actual;
		input = "%%(font-style: italic)foo\n" + 
				"\n" + 
				"bar\n" + 
				"\n" + 
				"gah\n" + 
				"%%\n" + 
				"";
		expected = "%%(font-style: italic)foo%%\n" +
				"\n" + 
				"%%(font-style: italic)bar%%\n" +
				"\n" + 
				"%%(font-style: italic)gah%%\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "%%(font-style: italic)foo\n" + 
				"\n" + 
				"bar\n" + 
				"\n" + 
				"gah\n" + 
				"%%";
		expected = "%%(font-style: italic)foo%%\n" +
				"\n" + 
				"%%(font-style: italic)bar%%\n" +
				"\n" + 
				"%%(font-style: italic)gah%%";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testPrepareAltSyntax_Percents() {
		String input, expected, actual;
		input = "%%(font-style: italic)foo\n" + 
				"\n" + 
				"bar%blah\n" + 
				"\n" + 
				"gah\n" + 
				"%%\n" + 
				"";
		expected = "%%(font-style: italic)foo%%\n" +
				"\n" + 
				"%%(font-style: italic)bar%blah%%\n" +
				"\n" + 
				"%%(font-style: italic)gah%%\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_Dollars() {
		String input, expected, actual;
		input = "%%(font-style: italic)foo\n" + 
				"\n" + 
				"bar$1 $15\n" + 
				"\n" + 
				"gah\n" + 
				"%%\n" + 
				"";
		expected = "%%(font-style: italic)foo%%\n" +
				"\n" + 
				"%%(font-style: italic)bar$1 $15%%\n" +
				"\n" + 
				"%%(font-style: italic)gah%%\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_CR() {
		String input, expected, actual;
		input = "%%(text-decoration: underline)foo\r\n" + 
				"bar\r\n" + 
				"gah\r\n" + 
				"%%\r\n" + 
				"";
		expected = "%%(text-decoration: underline)foo bar gah%%\r\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "%%(font-style: italic)foo\r\n" + 
				"\r\n" + 
				"bar\r\n" + 
				"\r\n" + 
				"gah\r\n" + 
				"%%\r\n" + 
				"";
		expected = "%%(font-style: italic)foo%%\r\n" +
				"\r\n" + 
				"%%(font-style: italic)bar%%\r\n" +
				"\r\n" + 
				"%%(font-style: italic)gah%%\r\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepareAltSyntax_Interference() {
		String input, expected, actual;
		input = "This is for testing CSS style conversions\n" + 
				"\n" + 
				"%%sub This should be subscript%% not sub\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%sup This should be superscript %% not super\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%strike\n" + 
				"This should be strikethroughed\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%commentbox\n" + 
				"floating right margin comment box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%information\n" + 
				"This is an info box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%warning\n" + 
				"This is warning box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%error\n" + 
				"Error box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%warning what about inline? %%\n" + 
				"\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"Some CSS next:\n" + 
				"%%( font-name:Times New Roman; color:blue; background-color:#999999; border:2px dashed #999900;)\n" + 
				"This panel could be weird\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%small\n" + 
				"Something we\'re not supporting\n" + 
				"%%\n" + 
				"\n" + 
				"%%sortable\n" + 
				"Something else we\'re not supporting\n" + 
				"%%";
		expected = "This is for testing CSS style conversions\n" + 
				"\n" + 
				"%%sub This should be subscript%% not sub\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%sup This should be superscript %% not super\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%strike\n" + 
				"This should be strikethroughed\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%commentbox\n" + 
				"floating right margin comment box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%information\n" + 
				"This is an info box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%warning\n" + 
				"This is warning box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%error\n" + 
				"Error box\n" + 
				"%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%warning what about inline? %%\n" + 
				"\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"Some CSS next:\n" + 
				"%%( font-name:Times New Roman; color:blue; background-color:#999999; border:2px dashed #999900;)\n" + 
				"This panel could be weird%%\n" + 
				"\n" + 
				"normal\n" + 
				"\n" + 
				"%%small\n" + 
				"Something we\'re not supporting\n" + 
				"%%\n" + 
				"\n" + 
				"%%sortable\n" + 
				"Something else we\'re not supporting\n" + 
				"%%";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testComboNewlines() {
		String input, expected, actual;
		input = "%%(text-decoration: underline;)\n" + 
				"foo\n" + 
				"bar\n" + 
				"\n" + 
				"gah\n" + 
				"\n" + 
				"jupiter\n" + 
				"%%\n" + 
				"";
		expected = "%%(text-decoration: underline;)foo bar%%\n" +
				"\n" + 
				"%%(text-decoration: underline;)gah%%\n" +
				"\n" + 
				"%%(text-decoration: underline;)jupiter%%\n" + 
				"";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMaintainBackslash() {
		String input, expected, actual;
		input = "{color:red}\n" + 
				"    resolutionmanager=15.2.113.21UWC_TOKEN_DBBS\n" + 
				"    ZMASTER_ZIPADDR=15.2.113.21UWC_TOKEN_DBBS\n" + 
				"    SAP_URI=tcp://15.2.113.21:3464\n" + 
				"{color}\n" + 
				"\n" + 
				"%%(text-decoration: underline)\n" + 
				"    resolutionmanager=15.2.113.21UWC_TOKEN_DBBS\n" + 
				"    ZMASTER_ZIPADDR=15.2.113.21UWC_TOKEN_DBBS\n" + 
				"    SAP_URI=tcp://15.2.113.21:3464\n" + 
				"%%\n" + 
				"";
		expected = "{color:red}\n" + 
				"    resolutionmanager=15.2.113.21\n" + 
				"    ZMASTER_ZIPADDR=15.2.113.21\n" + 
				"    SAP_URI=tcp://15.2.113.21:3464\n" + 
				"{color}\n\n" +
				"%%(text-decoration: underline)\n" + 
				"    resolutionmanager=15.2.113.21UWC_TOKEN_2DBBS     ZMASTER_ZIPADDR=15.2.113.21UWC_TOKEN_2DBBS     SAP_URI=tcp://15.2.113.21:3464%%\n";
		actual = tester.prepareAltSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
