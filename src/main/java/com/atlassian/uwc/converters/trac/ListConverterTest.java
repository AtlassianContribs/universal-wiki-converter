package com.atlassian.uwc.converters.trac;

import java.util.Stack;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class ListConverterTest extends TestCase {

	ListConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		tester = new ListConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	
	public void testEscapeNonListDashes() {
		String input, expected, actual;
		input = "- list item\n" +
				"Not-a-list-item --tada--\n" +
				"- list item!\n" +
				"-- list item!!\n" +
				"- but not -this one-\n" +
				"----\n" +
				"";
		expected = "- list item\n" +
				"Not\\-a\\-list\\-item \\-\\-tada\\-\\-\n" +
				"- list item!\n" +
				"-- list item!!\n" +
				"- but not \\-this one\\-\n" +
				"----\n" +
				"";
		actual = tester.escapeNonListContextDashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEscapeDashes() {
		String input, expected, actual;
		input = "-";
		expected = "\\-";
		actual = tester.escapeDashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "---";
		expected = "\\-\\-\\-";
		actual = tester.escapeDashes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetConfDelim() {
		Stack<String> delims = new Stack<String>();
		delims.push(" *");
		String expected = "*";
		String actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		delims.push("   *");
		expected = "**";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		delims.push("     *");
		expected = "***";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		delims.clear();
		delims.push(" 1.");
		expected = "#";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
	
		delims.clear();
		delims.push("  1.");
		expected = "#";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		delims.push("     *");
		expected = "#*";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
		delims.clear();
		delims.push("-");
		expected = "-";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		delims.push(" -");
		expected = "--";
		actual = tester.getConfDelim(delims);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertListUnordered() {
		String input, expected, actual;
		input = " * Item 1\n" + 
				"   * Item 1.1\n" + 
				"     * Item 1.1.1\n" + 
				"     * Item 1.1.2     * Item 1.1.3   * Item 1.2\n" + 
				" * Item 2\n" + 
				"";
		expected = "* Item 1\n" + 
				"** Item 1.1\n" + 
				"*** Item 1.1.1\n" + 
				"*** Item 1.1.2     * Item 1.1.3   * Item 1.2\n" + 
				"* Item 2\n" + 
				"";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertListOrdered() {
		String input, expected, actual;
		input = " 1. Item 1\n" + 
				" 1. Item 2\n" + 
				" 1. Item 3\n" + 
				"";
		expected = "# Item 1\n" + 
				"# Item 2\n" + 
				"# Item 3\n";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertMixed() {
		String input, expected, actual;
		input = "  1. foo\n" + 
				"     * foo 2\n" + 
				"  1. bar\n" + 
				"";
		expected = "# foo\n" + 
				"#* foo 2\n" + 
				"# bar\n" + 
				"";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertDashes() {
		String input, expected, actual;
		input = "- foo\n" + 
				" - bar \n" + 
				" - aslkdj\n" + 
				"- foo2\n";
		expected = "- foo\n" + 
				"-- bar \n" +
				"-- aslkdj\n" +
				"- foo2\n";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "- foo\n" + 
				" - bar - abcdef\n" + 
				" - aslkdj\n" + 
				"- abc def\n" + 
				" - abcdef\n" + 
				"";
		expected = "- foo\n" + 
				"-- bar \\- abcdef\n" + 
				"-- aslkdj\n" + 
				"- abc def\n" + 
				"-- abcdef\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
