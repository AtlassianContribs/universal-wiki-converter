package com.atlassian.uwc.converters.tikiwiki;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TableConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	TableConverter tester;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new TableConverter();
	}

	public void testConvertTable() {
		String input = "|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||";
		String expected = "|| Header1 || Header2 || Header3 ||\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |\n" ;
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}

	public void testRowHeaders() {
		String input = "|| __Header1__ | __Header2__ | __Header3__ " +
				"|| __Row1 Header__ | r1c2 | r1c3 " +
				"|| __Row2 Header__ | r2c2 | r2c3 ||";
		String expected = "|| Header1 || Header2 || Header3 ||\n" +
				"|| Row1 Header | r1c2 | r1c3 |\n" +
				"|| Row2 Header | r2c2 | r2c3 |\n";
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testAltSyntax() {
		String input = "||row1-column1|row1-column2\n" +
				"row2-column1|row2-column2||\n" ;
		String expected = "| row1-column1 | row1-column2 |\n" +
				"| row2-column1 | row2-column2 |\n";
//		String actual = tester.convertTable(input);
//		assertEquals(expected, actual);
	}
	
	public void testNoHeaders() {
		String input = "||r1c1|r1c2||r2c1|r2c2||";
		String expected = "| r1c1 | r1c2 |\n" +
				"| r2c1 | r2c2 |\n";
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testRowColSpans() {
		String input = "||row1-column1|row1-column2|row1-column3||row2-columns123||row3-column1|row3-columns23||";
		String expected = "| row1-column1 | row1-column2 | row1-column3 |\n" +
		"| row2-columns123 | | |\n" +
		"| row3-column1 | row3-columns23 | |\n";
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);		
	}
	
	public void testConvertRow() {
		String input = "|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||";
		String expected = "| __Header1__ | __Header2__ | __Header3__ |\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |\n";
		String actual = tester.convertRows(input);
		assertEquals(expected, actual);		
		
		input = "||row1-column1|row1-column2\n" +
		"row2-column1|row2-column2||\n" ;
		expected = "| row1-column1 | row1-column2 |\n" +
		"| row2-column1 | row2-column2 |\n" ;
//		actual = tester.convertRows(input);
//		assertEquals(expected, actual);	
	}
	
	public void testConvertHeader() {
		String input = "| __Header1__ | __Header2__ | __Header3__ |\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |";
		String expected = "|| Header1 || Header2 || Header3 ||\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |";
		String actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
		
		input = "|row1-column1|row1-column2|\n" +
		"|row2-column1|row2-column2|\n" ;
		expected = input;
		actual = tester.convertHeaders(input);
		assertEquals(expected, actual);
		
		input = "| __Header1__ | __Header2__ | __Header3__ |\n" +
				"| __Row1 Header__ | r1c2 | r1c3 |\n" +
				"| __Row2 Header__ | r2c2 | r2c3 |\n";
		expected = "|| Header1 || Header2 || Header3 ||\n" +
				"|| Row1 Header | r1c2 | r1c3 |\n" +
				"|| Row2 Header | r2c2 | r2c3 |\n";
		actual = tester.convertHeaders(input);
		assertEquals(expected, actual);

	}
	
	public void testConvertCells() {
		String 	input = "|row1-column1|row1-column2|\n" +
			"|row2-column1|row2-column2|\n" ;
		String expected = "| row1-column1 | row1-column2 |\n" +
		"| row2-column1 | row2-column2 |\n" ;
		String actual = tester.convertCells(input);
		assertEquals(expected, actual);
		
	}
	
	public void testRemoveLastDelim() {
		String input = "|abc|\n||";
		String expected = "|abc|\n";
		String actual = tester.removeFinalDelim(input);
		assertEquals(expected, actual);
		
		input = "|a|b|c|\n|d|e|f|\n||";
		expected = "|a|b|c|\n|d|e|f|\n";
		actual = tester.removeFinalDelim(input);
		assertEquals(expected, actual);
		
	}
	public void testRemoveLastDelimWindowsNewlines() {
		
		//and with windows newlines
		String input = "|abc|\r\n||";
		String expected = "|abc|\r\n";
		String actual = tester.removeFinalDelim(input);
		assertEquals(expected, actual);
		
		input = "|a|b|c|\r\n|d|e|f|\r\n||";
		expected = "|a|b|c|\r\n|d|e|f|\r\n";
		actual = tester.removeFinalDelim(input);
		assertEquals(expected, actual);
		
	}
		
	public void testNotATable() {
		String input = "Monospace\n" +
				"stuff\n";
		String expected = input;
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
		
		input += "((link|alias))\n";
		expected = input;
		actual = tester.convertTable(input);
		assertEquals(expected, actual);
		
		input = "Tables\n\n";
		expected = input;
		actual = tester.convertTable(input);
		assertEquals(expected, actual);
		
		String aTable = "|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||\n";
		input += aTable;
		expected += tester.convertTable(aTable);
		actual = tester.convertTable(input);
		assertEquals(expected, actual);

	}
	
	public void testNoExtraWhitespace() {
		String input = "Tables\n" +
		"\n" +
		"|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||\n" +
		"\n" +
		"|| __Header1__ | __Header2__ | __Header3__ || __Row1 Header__ | r1c2 | r1c3 || __Row2 Header__ | r2c2 | r2c3 ||\n" +
		"\n" +
		"\n" +
		"Alternate table syntax:\n";
		String expected = "Tables\n" +
		"\n" +
		"|| Header1 || Header2 || Header3 ||\n" +
		"| r1c1 | r1c2 | r1c3 |\n" +
		"| r2c1 | r2c2 | r2c3 |\n" +
		"\n" +
		"|| Header1 || Header2 || Header3 ||\n" +
		"|| Row1 Header | r1c2 | r1c3 |\n" +
		"|| Row2 Header | r2c2 | r2c3 |\n" +
		"\n" +
		"\n" +
		"Alternate table syntax:\n";
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
		
		input = "Alternate table syntax:\n" +
		"\n" +
		"||row1-column1|row1-column2\n" +
		"row2-column1|row2-column2||\n" +
		"\n" +
		"No headers:\n";
		
		expected = "Alternate table syntax:\n" +
		"\n" +
		"| row1-column1 | row1-column2 |\n" +
		"| row2-column1 | row2-column2 |\n" +
		"\n" +
		"No headers:\n";
//		actual = tester.convertTable(input);
//		assertEquals(expected, actual);
		
	}

	public void testAll() {
		String input = "Tables\n" +
		"\n" +
		"|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||\n" +
		"\n" +
		"|| __Header1__ | __Header2__ | __Header3__ || __Row1 Header__ | r1c2 | r1c3 || __Row2 Header__ | r2c2 | r2c3 ||\n" +
		"\n" +
		"\n" +
		"No headers:\n" +
		"\n" +
		"||r1c1|r1c2||r2c1|r2c2||\n" +
		"\n" +
		"Row and Col spans:\n" +
		"\n" +
		"||row1-column1|row1-column2|row1-column3||row2-columns123||row3-column1|row3-columns23||\n" ;
		
		String expected = "Tables\n" +
		"\n" +
		"|| Header1 || Header2 || Header3 ||\n" +
		"| r1c1 | r1c2 | r1c3 |\n" +
		"| r2c1 | r2c2 | r2c3 |\n" +
		"\n" +
		"|| Header1 || Header2 || Header3 ||\n" +
		"|| Row1 Header | r1c2 | r1c3 |\n" +
		"|| Row2 Header | r2c2 | r2c3 |\n" +
		"\n" +
		"\n" +
		"No headers:\n" +
		"\n" +
		"| r1c1 | r1c2 |\n" +
		"| r2c1 | r2c2 |\n" +
		"\n" +
		"Row and Col spans:\n" +
		"\n" +
		"| row1-column1 | row1-column2 | row1-column3 |\n" +
		"| row2-columns123 | | |\n" +
		"| row3-column1 | row3-columns23 | |\n";
		
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testTwoTablesWithNewlines() {
		String input = "\n|| __Header1__ | __Header2__ | __Header3__ || __Row1 Header__ | r1c2 | r1c3 || __Row2 Header__ | r2c2 | r2c3 ||\n" +
		"\n" +
		"\n" +
		"\n" +
		"|| __Header1__ | __Header2__ | __Header3__ || __Row1 Header__ | r1c2 | r1c3 || __Row2 Header__ | r2c2 | r2c3 ||\n" +
		"\n" +
		"\n" ;
		String expected = "\n|| Header1 || Header2 || Header3 ||\n" +
		"|| Row1 Header | r1c2 | r1c3 |\n" +
		"|| Row2 Header | r2c2 | r2c3 |\n" +
		"\n" +
		"\n" +
		"\n" +
		"|| Header1 || Header2 || Header3 ||\n" +
		"|| Row1 Header | r1c2 | r1c3 |\n" +
		"|| Row2 Header | r2c2 | r2c3 |\n" +
		"\n\n";
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testTableWithSpaceEnder() {
		String input = 	"||row1-column1|row1-column2\n" +
		"row2-column1|row2-column2||\n" +
		" \n" +
		"Later\n" ;
		String expected = 	"| row1-column1 | row1-column2 |\n" +
		"| row2-column1 | row2-column2 |\n" +
		" \n" +
		"Later\n";
//		String actual = tester.convertTable(input);
//		assertEquals(expected, actual);
		
		input = 	"||row1-column1|row1-column2\n" +
		"row2-column1|row2-column2|| \n" +
		"\n" +
		"Later\n" ;
		expected = 	"| row1-column1 | row1-column2 |\n" +
		"| row2-column1 | row2-column2 |\n" +
		"\n" +
		"Later\n";
//		actual = tester.convertTable(input);
//		assertEquals(expected, actual);
		

	}
	
	public void testRemoveFinalWithinContext() {
		String input = "|| __Header1__ | __Header2__ | __Header3__ || r1c1 | r1c2 | r1c3 || r2c1 | r2c2 | r2c3 ||\n" +
				"After\n";
		String expected = "|| Header1 || Header2 || Header3 ||\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |\n" +
				"After\n" ;
		String actual = tester.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testEscapeDashes() {
		String 	input = "||-|row1-column2|" +
			"|row2-column1|row2-column2||\n";
		String expected = "| \\- | row1-column2 |\n" +
			"| row2-column1 | row2-column2 |\n" ;
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testEscapeSpecialCharacter() {
		String input = "|-|";
		String expected = "| \\-|";
		String actual = tester.escapeSpecialCharacters(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "| - |";
		expected = "| \\- |";
		actual = tester.escapeSpecialCharacters(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testNewlineDelimTables_Header() {
		String input = "Before\n" +
				"||\n" +
				"A|B|C|D|E\n" +
				"abc|def|ghi|jkl|mno\n" +
				"||\n" +
				"After\n";
		String expected = "Before\n" +
				"| A | B | C | D | E |\n" +
				"| abc | def | ghi | jkl | mno |\n" +
				"After\n";
		
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testRemoveOpeningExtraPipes() {
		String input = "Before\n" +
			"||| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | mno |\n" +
			"\n" +
			"||\n" +
			"After\n" ;
		String expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | mno |\n" +
			"\n" +
			"||\n" +
			"After\n" ;
		String actual = tester.removeOpeningExtraPipes(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testRemoveFinalDelim_NewlineDelimTables_Header() {
		String input = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | mno |\n" +
			"\n" +
			"||\n" +
			"After\n";
		String expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | mno |\n" +
			"After\n";
		String actual = tester.removeFinalDelim(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testRemoveFinalDelimNeedsNewlines() {
		String input = "| A | B | C |\n" +
			"| stuff | stuff | stuff |\n" +
			"\n" +
			"||\n" +
			"| Date: | a/2/b |\n" +
			"| stuff | stuff | stuff |\n" +
			"\n" +
			"||\n";
		String expected = 
			"| A | B | C |\n" +
			"| stuff | stuff | stuff |\n" +
			"\n" +
			"| Date: | a/2/b |\n" +
			"| stuff | stuff | stuff |\n";
		Vector<Boolean> needsNewlines = new Vector<Boolean>(2);
		needsNewlines.add(new Boolean(false));
		needsNewlines.add(new Boolean(true));
		String actual = tester.removeFinalDelim(input, needsNewlines);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testNewlineDelimTables_EndWithDash() {
		String input = "Before\n" +
			"||\n" +
			"A|B|C|D|E\n" +
			"abc|def|ghi|jkl|-\n" +
			"||\n" +
			"After\n";
		String expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | \\- |\n" +
			"After\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNewlineDelimTables_EndWithSpace() {
		String input = "Before\n" +
			"||\n" +
			"A|B|C|D|E\n" +
			"abc|def|ghi|jkl|\n" +
			"||\n" +
			"After\n";
		String expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | |\n" +
			"After\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Before\n" +
			"||\n" +
			"A|B|C|D|E\n" +
			"abc|def|ghi|jkl|\n" +
			"||\n" +
			"After\n";
		expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | jkl | |\n" +
			"After\n";
		actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "Before\n" +
			"||\n" +
			"A|B|C|D|E\n" +
			"abc|def|ghi| |\n" +
			"||\n" +
			"After\n";
		expected = "Before\n" +
			"| A | B | C | D | E |\n" +
			"| abc | def | ghi | | |\n" +
			"After\n";
		actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testGetNumberOfColumns() {
		String input = "| A | B | C |";
		int expected = 3;
		int actual = tester.getNumberOfColumns(input);
		assertEquals(expected, actual);
		
		input = "|| A || B ||";
		expected = 2;
		actual = tester.getNumberOfColumns(input);
		assertEquals(expected, actual);
	}
	
	public void testEnforceColumnNumbering() {
		//no change needed
		String input = "| A | B | C |";
		int num = 3;
		String expected = input;
		String actual = tester.enforceColumnNumbering(num, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		//num is higher
		num = 4;
		expected = input + " |";
		actual = tester.enforceColumnNumbering(num, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		num = 5;
		expected = input + " | |";
		actual = tester.enforceColumnNumbering(num, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//and with a newline
		input = "| A | B | C |\n";
		expected = "| A | B | C | | |\n";
		actual = tester.enforceColumnNumbering(num, input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertEmptyCells() {
		String input = "|abc|def|ghi| ||";
		String expected = "| abc | def | ghi | | |";
		String actual = tester.convertCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testExpandEmptyCells() {
		String input = "| A | B | C ||";
		String expected = "| A | B | C | |";
		String actual = tester.expandEmptyCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//don't screw up headers
		input = "|| A || B || C ||";
		expected = input;
		actual = tester.expandEmptyCells(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultipleTablesNewlineDelim() {
		String input = "^\n" +
			"Pre text: ABC, DEFs.\n" +
			"||__ABC__|__DEF__|__GHI JKL__|__MNO PQR__\n" +
			"http://www.google.com/|Ac|11/29/2005|11/29/2005\n" +
			"http://www.google.com/|Ac|1/6/2006|1/6/2006\n" +
			"||\n" +
			"^\n" +
			"\n" +
			"\n" +
			"__header__\n" +
			"^\n" +
			"Home Site:[http://www.google-google.com/|http://www.google-google.com/]\n" +
			"Download Link:[http://www.google-google.com/download.shtml|http://www.google-google.com/download.shtml]\n" +
			"\n" +
			"||Date:|1/4/2006\n" +
			"Version:|1.0\n" +
			"Purchase URL:|http://www.google-google.com/buynow.shtml\n" +
			"Purchase Price:|$19.99\n" +
			"Functionality:|Time Limit (15 day trial version)\n" +
			"Rabc:|ABC\n" +
			"Common Components:|\n" +
			"To Adds:|installer\n" +
			"Notes:|\n" +
			"||\n" +
			"\n" +
			"^\n" ;
		String expected = "^\n" +
			"Pre text: ABC, DEFs.\n" +
			"|| ABC || DEF || GHI JKL || MNO PQR ||\n" +
			"| http://www.google.com/ | Ac | 11/29/2005 | 11/29/2005 |\n" +
			"| http://www.google.com/ | Ac | 1/6/2006 | 1/6/2006 |\n" +
			"^\n" +
			"\n" +
			"\n" +
			"__header__\n" +
			"^\n" +
			"Home Site:[http://www.google-google.com/|http://www.google-google.com/]\n" +
			"Download Link:[http://www.google-google.com/download.shtml|http://www.google-google.com/download.shtml]\n" +
			"\n" +
			"| Date: | 1/4/2006 |\n" +
			"| Version: | 1.0 |\n" +
			"| Purchase URL: | http://www.google-google.com/buynow.shtml |\n" +
			"| Purchase Price: | $19.99 |\n" +
			"| Functionality: | Time Limit (15 day trial version) |\n" +
			"| Rabc: | ABC |\n" +
			"| Common Components: | |\n" +
			"| To Adds: | installer |\n" +
			"| Notes: | |\n" +
			"\n" +
			"^\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testManyTablesInContextAndPanel() {
		String input = "^\n" +
		"Home Site:[http://www.e-spy-software.com/|http://www.e-spy-software.com/]\n" +
		"Download Link:[http://www.e-spy-software.com/download.shtml|http://www.e-spy-software.com/download.shtml]\n" +
		"\n" +
		"||Date|Researcher|Version|What was preexisting in the defs?|Where there any common components?|What did you add to a def or whitelist?|Notes\n" +
		"20061108|ED|3.873|UFP 007 Spy|MSComctl, vb, misc microsoft|Installer|None\n" +
		"20060907|ED|3.87|UFP 007 Spy|mscomctl, vb5.0 |Installer, running .exe source URL|None\n" +
		"03/29/2006|jack|3.85|folder trace - C:\\\\Program Files\\Common Files\\Microsoft Shared\\DAO\\ssdata|ijl11pro.DLL, MSINET.OCX, VB5STKIT.DLL|installer, svchost.exe|added screenies to the wiki page\n" +
		"030606|csb|3.81|-|mscomm |isntaller, help file | |\n" +
		"022406|DJP|3.83|-|MS Stuff, Intel Jpg Library|Nothing added - complete removal| |\n" +
		" | | | | | |\n" +
		"||\n" +
		"\n" +
		"||Date:|1/4/2006\n" +
		"Version:|3.8\n" +
		"Purchase URL:|http://www.e-spy-software.com/buynow.shtml\n" +
		"Purchase Price:|$39.95\n" +
		"Functionality:|Time Limit (15 day trial version)\n" +
		"Researcher:|CGR\n" +
		"Common Components:|\n" +
		"To Adds:|installer\n" +
		"Notes:|\n" +
		"||\n" +
		"||Date:|11/29/2005\n" +
		"Version:|3.8\n" +
		"Purchase URL:|https://www.plimus.com/jsp/buynow.jsp?contractId=1633080\n" +
		"Purchase Price:|$39.95\n" +
		"Functionality:|Time Limit\n" +
		"Researcher:|EHD\n" +
		"Common Components:|\n" +
		"To Adds:|installer, .lnk, svchost.exe\n" +
		"Notes:|\n" +
		"||\n" +
		"||Date:|10/25/2005\n" +
		"Version:|3.73\n" +
		"Purchase URL:|[https://www.plimus.com/jsp/buynow.jsp?contractId=1633080]\n" +
		"Purchase Price:|$39.95\n" +
		"Funcitonality|Trial\n" +
		"Researcher:|BJG\n" +
		"Common Components:|Microsoft, VB\n" +
		"To Adds:|-\n" +
		"Notes:|-\n" +
		"||\n" +
		"||Date:|08/22/2005\n" +
		"Version:|3.73\n" +
		"Purchase URL:|[https://www.plimus.com/jsp/buynow.jsp?contractId=1633080]\n" +
		"Purchase Price:|$39.95\n" +
		"Funcitonality|Trial\n" +
		"Researcher:|Scott\n" +
		"Common Components:|Microsoft, VB\n" +
		"To Adds:|Installer\n" +
		"Notes:|Folders did not detect correctly even with being in the definition.\n" +
		"||\n" +
		"^\n" ;
		String expected = "^\n" +
		"Home Site:[http://www.e-spy-software.com/|http://www.e-spy-software.com/]\n" +
		"Download Link:[http://www.e-spy-software.com/download.shtml|" +
		"http://www.e-spy-software.com/download.shtml]\n" +
		"\n" +
		"| Date | Researcher | Version | What was preexisting in the defs? | Where there any common components? | What did you add to a def or whitelist? | Notes |\n" +
		"| 20061108 | ED | 3.873 | UFP 007 Spy | MSComctl, vb, misc microsoft | Installer | None |\n" +
		"| 20060907 | ED | 3.87 | UFP 007 Spy | mscomctl, vb5.0 | Installer, running .exe source URL | None |\n" +
		"| 03/29/2006 | jack | 3.85 | folder trace - C:\\\\Program Files\\Common Files\\Microsoft Shared\\DAO\\ssdata | ijl11pro.DLL, MSINET.OCX, VB5STKIT.DLL | installer, svchost.exe | added screenies to the wiki page |\n" +
		"| 030606 | csb | 3.81 | \\- | mscomm | isntaller, help file | |\n" + 
		"| 022406 | DJP | 3.83 | \\- | MS Stuff, Intel Jpg Library | Nothing added - complete removal | |\n" +
		"| | | | | | | |\n" +
		"\n" +
		"| Date: | 1/4/2006 |\n" +
		"| Version: | 3.8 |\n" +
		"| Purchase URL: | http://www.e-spy-software.com/buynow.shtml |\n" +
		"| Purchase Price: | $39.95 |\n" +
		"| Functionality: | Time Limit (15 day trial version) |\n" +
		"| Researcher: | CGR |\n" +
		"| Common Components: | |\n" +
		"| To Adds: | installer |\n" +
		"| Notes: | |\n" +
		"\n" +
		"| Date: | 11/29/2005 |\n" +
		"| Version: | 3.8 |\n" +
		"| Purchase URL: | https://www.plimus.com/jsp/buynow.jsp?contractId=1633080 |\n" +
		"| Purchase Price: | $39.95 |\n" +
		"| Functionality: | Time Limit |\n" +
		"| Researcher: | EHD |\n" +
		"| Common Components: | |\n" +
		"| To Adds: | installer, .lnk, svchost.exe |\n" +
		"| Notes: | |\n" +
		"\n" +
		"| Date: | 10/25/2005 |\n" +
		"| Version: | 3.73 |\n" +
		"| Purchase URL: | [https://www.plimus.com/jsp/buynow.jsp?contractId=1633080] |\n" +
		"| Purchase Price: | $39.95 |\n" +
		"| Funcitonality | Trial |\n" +
		"| Researcher: | BJG |\n" +
		"| Common Components: | Microsoft, VB |\n" +
		"| To Adds: | \\- |\n" +
		"| Notes: | \\- |\n" +
		"\n" +
		"| Date: | 08/22/2005 |\n" +
		"| Version: | 3.73 |\n" +
		"| Purchase URL: | [https://www.plimus.com/jsp/buynow.jsp?contractId=1633080] |\n" +
		"| Purchase Price: | $39.95 |\n" +
		"| Funcitonality | Trial |\n" +
		"| Researcher: | Scott |\n" +
		"| Common Components: | Microsoft, VB |\n" +
		"| To Adds: | Installer |\n" +
		"| Notes: | Folders did not detect correctly even with being in the definition. |\n" +
		"^\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testManyTablesInContextCondensed() {
		String input = "||A|B|C\n" +
		"stuff|stuff|stuff\n" +
		"||\n" +
		"||Date:|a/2/b\n" +
		"stuff|stuff|stuff\n" +
		"||\n" ;
		String expected = "| A | B | C |\n" +
		"| stuff | stuff | stuff |\n" +
		"\n" +
		"| Date: | a/2/b |\n" +
		"| stuff | stuff | stuff |\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddNL() {
		String input = "abc";
		Boolean bool = new Boolean(false);
		String expected = input;
		String actual = tester.addNewline(input, bool);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		bool = new Boolean (true);
		expected = "\nabc";
		actual = tester.addNewline(input, bool);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testColspanReduction() {
		String input = "||A|B|C\n" +
		"something| | |\n" +
		"||\n";
		String expected = "| A | B | C |\n" +
		"| something | | |\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEnforceColumnNumbering_ColspanReduction() {
		String input = "| something | | | |";
		int num = 3;
		String expected = "| something | | |";
		String actual = tester.enforceColumnNumbering(num, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testReduceColspans() {
		String input = "| something | | | |";
		int difference = 1;
		String expected = "| something | | |";
		String actual = tester.reduceColspans(input, difference);
		assertNotNull(actual);
		assertEquals(expected, actual);
				
		input = "! something ! else !";
		difference = 1;
		expected = input;
		actual = tester.reduceColspans(input, difference);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		difference = 0;
		try {
			actual = tester.reduceColspans(input, difference);
			fail();
		} catch (IllegalArgumentException e) {}
		
		difference = -1;
		try {
			actual = tester.reduceColspans(input, difference);
			fail();
		} catch (IllegalArgumentException e) {}

	}
	
	public void testColspanPruningVerySimple() {
		String input = "||A|B\n" +
				"a|b||\n" +
				"||\n";
		String expected = "| A | B |\n" +
				"| a | b |\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveFinalDelims() {
		String input = "| A | B |\n" +
				"| a | b |\n" +
				"||";
		String expected = "| A | B |\n" +
			"| a | b |\n";
		String actual = tester.removeFinalDelims(input, null);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input += "\n||\n";
		actual = tester.removeFinalDelims(input, null);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveFinalDelimManyNewlines() {
		String input = "| A | B |\n" +
				"| a | b |\n" +
				"||\n" +
				"||\n";
		String expected = "| A | B |\n" +
				"| a | b |\n" +
				"||\n"; 
		String actual = tester.removeFinalDelim(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testFirstCellIsEmpty() {
		String input = "||A|B\n" +
				"|b\n" +
				"||\n";
		String expected = "| A | B |\n" +
				"| | b |\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testColspanPruningPlusAfter() {
		String input = "Before(a-b)\n" +
		"||A|B\n" +
		"a|b||\n" +
		"||\n" +
		"\n" +
		"After(c-d)\n" ;
		
		String expected = "Before(a-b)\n" +
		"| A | B |\n" +
		"| a | b |\n" +
		"\n" +
		"After(c-d)\n" ;
		
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
	}
	public void testSomeExtraCasesFor115_118_133() { 
		String input = "Tables Back to Back (uwc-115)\n" +
		"||A|B\n" +
		"a|b\n" +
		"||\n" +
		"||C|D\n" +
		"c|d\n" +
		"||\n" +
		"\n" +
		"Tables with Colspans that can be pruned (uwc-133)\n" +
		"||A|B\n" +
		"a|b| |\n" +
		"||\n" +
		"\n" +
		"Tables starting with an empty cell (uwc-118)\n" +
		"||A|B\n" +
		"|b\n" +
		"||\n" ;

		String expected = "Tables Back to Back (uwc-115)\n" +
		"| A | B |\n" +
		"| a | b |\n" +
		"\n" +
		"| C | D |\n" +
		"| c | d |\n" +
		"\n" +
		"Tables with Colspans that can be pruned (uwc-133)\n" +
		"| A | B |\n" +
		"| a | b |\n" +
		"\n" +
		"Tables starting with an empty cell (uwc-118)\n" +
		"| A | B |\n" +
		"| | b |\n";
		
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 	}
	
	public void testTwoBackToBackTables() {
		String input = "Tables Back to Back (uwc-115)\n" +
		"||A|B\n" +
		"a|b\n" +
		"||\n" +
		"||C|D\n" +
		"c|d\n" +
		"||\n" +
		"\n";
		String expected = "Tables Back to Back (uwc-115)\n" +
		"| A | B |\n" +
		"| a | b |\n" +
		"\n" +
		"| C | D |\n" +
		"| c | d |\n" +
		"\n";
		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	

	public void testPruningEmptyColspansPlusExtra() {
		String input = "Tables with Colspans that can be pruned (uwc-133)\n" +
		"||A|B\n" +
		"a|b| |\n" +
		"||\n" +
		"\n" +
		"Tables starting with an empty cell (uwc-118)\n" +
		"||A|B\n" +
		"|b\n" +
		"||\n" ;
		String expected = "Tables with Colspans that can be pruned (uwc-133)\n" +
		"| A | B |\n" +
		"| a | b |\n" +
		"\n" +
		"Tables starting with an empty cell (uwc-118)\n" +
		"| A | B |\n" +
		"| | b |\n";

		String actual = tester.convertTable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
}
