package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class LeadingSpacesConverterTest extends TestCase {

	LeadingSpacesConverter tester;
	Page page;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new LeadingSpacesConverter();
		page = new Page(null);
	}

	public void testConvert() {
		String input = " simple";
		String expected = "{{simple}}";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);
	}

	public void testLines() {
		String input = "before\n" +
				" monospaced\n" +
				"after";
		String expected = "before\n" +
				"{{monospaced}}\n" +
				"after";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);
	}
	
	public void testManyLines() {
		String input = "before\n" +
				" several\n" +
				" monospaced\n" +
				" lines\n" +
				"after";
		String expected = "before\n" +
				"{{several}}\n" +
				"{{monospaced}}\n" +
				"{{lines}}\n" +
				"after";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);		
	}
	
	public void testAtBeginning() {
		String input = 
				" several\n" +
				" monospaced\n" +
				" lines\n" +
				"after";
		String expected = 
				"{{several}}\n" +
				"{{monospaced}}\n" +
				"{{lines}}\n" +
				"after";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);				
	}
	
	public void testExtraLeadingWhitespace() {
		String input = "before\n" +
				"      monospaced\n" +
				"after";
		String expected = "before\n" +
				"{{monospaced}}\n" +
				"after";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);
	}
	
	public void testEscapedBrackets() {
		String input = "   ALTER TABLE tblname ADD colname type \\[NOT NULL\\] \\[DEFAULT value\\];";
		String expected = "{{ALTER TABLE tblname ADD colname type \\[NOT NULL\\] \\[DEFAULT value\\];}}";
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testHTML() {
		String input = "   <tr>";
		String expected = "{{<tr>}}";
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testBadDollarSigns() {
		String input = "  This costs $2.99";
		page.setOriginalText(input);
		try {
			tester.convert(page);
		} catch (IndexOutOfBoundsException e) {
			fail("out of bounds exception.");
		}
	}

	public void testBadDollarSigns2() {
		String input = "  This code has a $varible";
		page.setOriginalText(input);
		try {
			tester.convert(page);
		} catch (IllegalArgumentException e2) {
			fail("illegal arg exception");
		}
		System.out.println(page.getConvertedText());
 	}

	public void testWithWindowsNewlines() {
		String input = " simple\r\n" +
				"After\r\n";
		String expected = "{{simple}}\r\n" +
				"After\r\n";
		
		page.setOriginalText(input);
		tester.convert(page);
		
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);	
	}
}
