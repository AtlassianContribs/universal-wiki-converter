package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

/**
 * Test class for TableConverter
 * 
 * @author Laura Kolker
 *
 */
public class TableConverterTest extends TestCase {
	TableConverter tester;
	String input1 = "\n|+caption\n" +
			"!Header1 || Header2 || Header3\n" +
			"|-\n" +
			"||r1c1||r1c2||r1c3\n" +
			"|-\n" +
			"|r2c1\n" +
			"|r2c2\n" +
			"|r2c3\n";
	
	String input2 = " border=\"1\" class=\"wikitable\" style=\"text-align:center\" cellpadding=\"2\"\n" +
			"|+caption\n" +
			"! Header1 ||Header2 || Header3 \n" +
			"|-\n" +
			"! Row1 Header \n" +
			"|r1c2|| r1c3 \n" +
			"|-\n" +
			"!Row2 Header \n" +
			"| r2c2 \n" +
			"| r2c3\n";

	String replacement1 = "{panel:title=caption|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" +
			"|| Header1 || Header2 || Header3 ||\n" +
			"| r1c1 | r1c2 | r1c3 |\n" +
			"| r2c1 | r2c2 | r2c3 |\n" +
			"{panel}";
	
	String replacement2 = "{panel:title=caption|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" +
			"|| Header1 || Header2 || Header3 ||\n" +
			"|| Row1 Header | r1c2 | r1c3 |\n" +
			"|| Row2 Header | r2c2 | r2c3 |\n" +
			"{panel}";
	
	String input3 = " border=\"1\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\"\n" +
			"|+*An example table*\n" +
			"|-\n" +
			"! style=\"background:#efefef;\" | First header\n" +
			"! colspan=\"2\" style=\"background:#ffdead;\" | Second header\n" +
			"|-\n" +
			"| upper left\n" +
			"|  \n" +
			"| rowspan=2 style=\"border-bottom:3px solid grey;\" valign=\"top\" |\n" +
			"right side\n" +
			"|-\n" +
			"| style=\"border-bottom:3px solid grey;\" | lower left\n" +
			"| style=\"border-bottom:3px solid grey;\" | lower middle\n" +
			"|-\n" +
			"| colspan=\"3\" align=\"center\" |\n" +
			"{| border=\"0\"\n" +
			"|+_A table in a table_\n" +
			"|-\n" +
			"| align=\"center\" width=\"150px\" | [[Image:wiki.png]]\n" +
			"| align=\"center\" width=\"150px\" | [[Image:wiki.png]]\n" +
			"|-\n" +
			"| align=\"center\" colspan=\"2\" style=\"border-top:1px solid red; border-right:1px solid red; border-bottom:2px solid red; border-left:1px solid red;\" | Two Wikipedia logos\n" +
			"|}\n" +
			"";
	
	String replacement3 = "{panel:title=*An example table*|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" +
			"|| First header || Second header ||\n" +
			"| upper left | | right side |\n" +
			"| lower left | lower middle |\n" +
			"| _A table in a table_ |\n" +
			"| [[Image:wiki.png]] | [[Image:wiki.png]] |\n" +
			"| Two Wikipedia logos |\n" +
			"{panel}";
	
	String input4 = " border=\"1\" cellpadding=\"5\" cellspacing=\"0\"\n" +
			"|-\n" +
			"! Column 1 || Column 2 || Column 3\n" +
			"|-\n" +
			"| A\n" +
			"| colspan=\"2\" align=\"center\"| B\n" +
			"|-\n" +
			"| C\n" +
			"| D\n" +
			"|-\n" +
			"| E\n" +
			"| colspan=\"2\" align=\"center\"| F\n" +
			"|- \n" +
			"| G\n" +
			"| H\n" +
			"| I\n" +
			"|- \n" +
			"| J\n" +
			"| K\n" +
			"|-\n" +
			"| colspan=\"2\" align=\"center\"| L\n";
	
	String replacement4 = "|| Column 1 || Column 2 || Column 3 ||\n" +
			"| A | B |\n" +
			"| C | D |\n" +
			"| E | F |\n" +
			"| G | H | I |\n" +
			"| J | K |\n" +
			"| L |";
	
	String input5 = "{| cellpadding=7 border=0 cellspacing=0\n" +
			"! Service\n" +
			"! Host\n" +
			"! Admin\n" +
			"! URL\n" +
			"! Description\n" +
			"|-\n" +
			"| style=\"border-bottom:1px solid grey;\" | SVN\n" +
			"| style=\"border-bottom:1px solid grey;\" | SourceForge\n" +
			"| style=\"border-bottom:1px solid grey;\" | [[WicketCore]]\n" +
			"| style=\"border-bottom:1px solid grey;\" | http://svn.sourceforge.net/viewvc/wicket/\n" +
			"| style=\"border-bottom:1px solid grey;\" | Subversion\n" +
			"|-\n" +
			"| Woogle\n" +
			"| [[Frankbille]]\n" +
			"| [[Frankbille]]\n" +
			"| http://woogle.billen.dk/\n" +
			"| Wicket search engine\n" +
			"|}";
	String replacement5 = "|| Service || Host || Admin || URL || Description ||\n" +
			"| SVN | SourceForge | [[WicketCore]] | http://svn.sourceforge.net/viewvc/wicket/ | Subversion |\n" +
			"| Woogle | [[Frankbille]] | [[Frankbille]] | http://woogle.billen.dk/ | Wicket search engine |";
	
	protected void setUp() throws Exception {
		tester = new TableConverter();
		PropertyConfigurator.configure("log4j.properties");
		super.setUp();
	}

	public void testIsAttribute() {
		String input = "style=\"color:#fffff;width=100%;\"";
		boolean expected = true;
		boolean actual = tester.isAttribute(input);
		assertEquals(expected, actual);
		
		input = "*A very _interesting_ caption.*";
		expected = false;
		actual = tester.isAttribute(input);
		assertEquals(expected, actual);
		
		input = "border=1";
		expected = true;
		actual = tester.isAttribute(input);
		assertEquals(expected, actual);
		
	}
	
	public void testGetTokenizedData_1() {
		String expected = replacement1;
		String actual = tester.getReplacement(input1);
		assertEquals(expected, actual);
	}
	public void testGetTokenizedData_2() {
		String expected, actual;
		expected = replacement2;
		actual = tester.getReplacement(input2);
		assertEquals(expected, actual);
	}
	public void testGetTokenizedData_3() {
		String expected, actual;
		expected = replacement3;
		actual = tester.getReplacement(input3);
		assertEquals(expected, actual);
	}
	public void testGetTokenizedData_4() {
		String expected, actual;
		expected = replacement4;
		actual = tester.getReplacement(input4);
		assertEquals(expected, actual);
	}

	public void testConvert() {
		Page page = new Page(null);
		page.setOriginalText("{|\n"+input1+"\n|}\n\n{|"+input2+"\n|}");
		tester.convert(page);
		String actual = page.getConvertedText();
		String expected = replacement1 + "\n\n"+replacement2;
		assertEquals(expected, actual);
		
		page = new Page(null);
		page.setOriginalText("{| " + input1 + "|}");
		tester.convert(page);
		actual = page.getConvertedText();
		expected = replacement1;
		assertEquals(expected, actual);
		
		page = new Page(null);
		page.setOriginalText("{| " + input2 + "|}");
		tester.convert(page);
		actual = page.getConvertedText();
		expected = replacement2;
		assertEquals(expected, actual);
		
		page = new Page(null);
		page.setOriginalText("{| " + input3 + "|}");
		tester.convert(page);
		actual = page.getConvertedText();
		expected = replacement3 + "\n";
		assertEquals(expected, actual);
		
		page = new Page(null);
		page.setOriginalText("{| " + input4 + "|}");
		tester.convert(page);
		actual = page.getConvertedText();
		expected = replacement4;
		assertEquals(expected, actual);
	
		page = new Page(null);
		page.setOriginalText(input5);
		tester.convert(page);
		actual = page.getConvertedText();
		expected = replacement5;
		assertEquals(expected, actual);
	}

	public void testRemoveOpeningNewlines() {
		String expected = "|+caption\n" +
		"!Header1 || Header2 || Header3\n" +
		"|-\n" +
		"||r1c1||r1c2||r1c3\n" +
		"|-\n" +
		"|r2c1\n" +
		"|r2c2\n" +
		"|r2c3\n";
		String actual = tester.removeOpeningNewlines(input1);
		assertEquals(expected, actual);
		
		actual = tester.removeOpeningNewlines(" " + input1);
		assertEquals(" " + expected, actual);
	}


	public void testConvertTables_extraHeaderDelims() {
		String input, expected, actual;
		input = "{| border=\"1\"\n" + 
				"! lorem !! ipsum !! lorem !! ipsum\n" + 
				"|-\n" + 
				"! 1\n" + 
				"| r1c1 || r1c2 || r1c3\n" + 
				"|-\n" + 
				"! 2\n" + 
				"| r2c1 || r2c2 || r2c3\n" + 
				"|}\n" + 
				"";
		expected = "|| lorem || ipsum || lorem || ipsum ||\n" +
				"|| 1 | r1c1 | r1c2 | r1c3 |\n" +
				"|| 2 | r2c1 | r2c2 | r2c3 |\n" +
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables_Lists() {
		String input, expected, actual;
		input =  "{|\n" + 
				"|Orange\n" + 
				"|Apple\n" + 
				"|-\n" + 
				"#Bread\n" + 
				"# Pie\n" + 
				"|-\n" + 
				"|*Butter\n" + 
				"*Ice cream \n" + 
				"|}\n" + 
				"";
		expected = "| Orange | Apple |\n" +
				"| # Bread\n# Pie |\n" + //not trying to add columns, just to maintain lists
				"| * Butter\n* Ice cream |\n" +
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables_ListsCombo() {
		String input, expected, actual;
		input = "{| border=\"1\" cellspacing=\"0\" cellpadding=\"5\" align=\"left\"\n" + 
				"! align=\"left\" colspan=\"2\"| \n" + 
				"== ABC DEF == \n" + 
				"|- \n" + 
				"! align=\"left\"| A \n" + 
				"! align=\"left\"| B \n" + 
				"|- \n" + 
				"! valign=\"top\" align=\"left\"| \n" + 
				"=== Something else ===\n" + 
				"| valign=\"top\" align=\"left\"| \n" + 
				"* testing \n" + 
				"*#*123 *\n" + 
				"* blah blah\n" + 
				"***_Something, here_*\n" + 
				"* abcdef\n" + 
				"|- \n" + 
				"|}";
		expected = "|| == ABC DEF == ||\n" + 
				"|| A || B ||\n" + 
				"|| === Something else === | * testing \n" + 
				"*#* 123 *\n" + //whitespace handled externally with list converters 
				"* blah blah\n" + 
				"*** _Something, here_*\n" + //whitespace handled externally with list converters
				"* abcdef |\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsListItem() {
		String input;
		input = "* testing";
		boolean actual = tester.isListItem(input);
		assertTrue(actual);
		
		input = "# test";
		actual = tester.isListItem(input);
		assertTrue(actual);
		
		input = "** abc";
		actual = tester.isListItem(input);
		assertTrue(actual);
		
		input = "#* def";
		actual = tester.isListItem(input);
		assertTrue(actual);
		
		input = "***foobar";
		actual = tester.isListItem(input);
		assertTrue(actual);
		
		input = "***foobar*";
		actual = tester.isListItem(input);
		assertTrue(actual);
		
		//not a list item
		input = "not a list item";
		actual = tester.isListItem(input);
		assertFalse(actual);
		
		input = "*bold*";
		actual = tester.isListItem(input);
		assertFalse(actual);
	}
	
	//tests corresponding with sampleData regression tests
	public void testConvertTables_Basic() {
		String input, expected, actual;
		input = "{|\n" + 
				"|+caption\n" + 
				"!Header1 || Header2 || Header3\n" + 
				"|-\n" + 
				"||r1c1||r1c2||r1c3\n" + 
				"|-\n" + 
				"|r2c1\n" + 
				"|r2c2\n" + 
				"|r2c3\n" +
				"|}";
		expected = "{panel:title=caption|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" + 
				"|| Header1 || Header2 || Header3 ||\n" + 
				"| r1c1 | r1c2 | r1c3 |\n" + 
				"| r2c1 | r2c2 | r2c3 |\n" + 
				"{panel}";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertTables_Basic2() {
		String input, expected, actual;
		input = "{| border=\"1\" class=\"wikitable\" style=\"text-align:center\" cellpadding=\"2\"\n" + 
				"|+caption\n" + 
				"!Header1||Header2||Header3\n" + 
				"|-\n" + 
				"!Row1 Header\n" + 
				"|r1c2||r1c3\n" + 
				"|-\n" + 
				"!Row2 Header\n" + 
				"|r2c2\n" + 
				"|r2c3\n" + 
				"|}\n" + 
				"";
		expected = "{panel:title=caption|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" + 
				"|| Header1 || Header2 || Header3 ||\n" + 
				"|| Row1 Header | r1c2 | r1c3 |\n" + 
				"|| Row2 Header | r2c2 | r2c3 |\n" + 
				"{panel}\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	

	public void testConvertTables_Basic3() {
		String input, expected, actual;
		input = "{| border=\"1\"\n" + 
				"! lorem !! ipsum !! lorem !! ipsum\n" + 
				"|-\n" + 
				"! 1\n" + 
				"| r1c1 || r1c2 || r1c3\n" + 
				"|-\n" + 
				"! 2\n" + 
				"| r2c1 || r2c2 || r2c3\n" + 
				"|}";
		expected = "|| lorem || ipsum || lorem || ipsum ||\n" + 
				"|| 1 | r1c1 | r1c2 | r1c3 |\n" + 
				"|| 2 | r2c1 | r2c2 | r2c3 |";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertTablesLines() { //see UWC-376
		String input, expected, actual;
		input = "{|\n" + 
				"|-\n" + 
				"!hello \n" + 
				"I\'m the header\n" + 
				"number 1 \n" + 
				"! I\'m the\n" + 
				"header 2\n" + 
				"|-\n" + 
				"| line 1\n" + 
				"and box 1 again\n" + 
				"| line 1 \n" + 
				"and box 2\n" + 
				"|}";
		expected = "|| hello \n" + 
				"I\'m the header\n" +
				"number 1 || I\'m the\n" + 
				"header 2 ||\n" + 
				"| line 1\n" + 
				"and box 1 again | line 1 \n" + 
				"and box 2 |";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	

	public void testConvertTablesCurlyBrace() { //see UWC-399
		String input, expected, actual;
		input = "{| border=\"1\" cellspacing=\"0\" cellpadding=\"5\" align=\"left\"\n" + 
				"|+ Caption in {color:red}color{color}\n" + 
				"! {color:blue}header1{color} !! header 2\n" + 
				"|- \n" + 
				"| {color:green}r1c1{color} || {color:orange}r1c2{color}\n" + 
				"|}\n" + 
				"";
		expected = "{panel:title= Caption in color|borderStyle=dashed|borderColor=#ccc|bgColor=#fff}\n" + 
				"|| {color:blue}header1{color} || header 2 ||\n" + 
				"| {color:green}r1c1{color} | {color:orange}r1c2{color} |\n" + 
				"{panel}\n" + 
				"";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testCleanMacros() {
		String input, expected, actual;
		input = "abcdef";
		expected = "abcdef";
		actual = tester.cleanMacros(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{color:red}in color{color}";
		expected = "in color";
		actual = tester.cleanMacros(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "just has a right curly brace}";
		expected = "just has a right curly brace";
		actual = tester.cleanMacros(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
