package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PrepColSpansConverterTest extends TestCase {

	PrepColSpansConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PrepColSpansConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testPrep() {
		String input, expected, actual;
		input = "^ Header ^ Header ^ Header ^ Header ^ Header ^ Header ^\n" +
				"^ Header | 2 | 3 | 4 | 5 | 6 |\n" +
				"^ Header | next colspans |||||\n";
		expected ="^ Header ^ Header ^ Header ^ Header ^ Header ^ Header ^\n" +
				"^ Header | 2 | 3 | 4 | 5 | 6 |\n" + 
				"^ Header | next colspans ::UWCTOKENCOLSPANS:5::|\n";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepComplicated() {
		String input, expected, actual;
		input = "^ Heading 1      ^ Heading 2       ^ Heading 3          ^\n" + 
				"| Row 1 Col 1    | Row 1 Col 2     | Row 1 Col 3        |\n" + 
				"| Row 2 Col 1    | some colspan (note the double pipe) ||\n" + 
				"| Row 3 Col 1    | Row 3 Col 2     | Row 3 Col 3        |\n" + 
				"| Row 1 Col 1    | this cell spans vertically | Row 1 Col 3        |\n" + 
				"| Row 2 Col 1    | :::                        | Row 2 Col 3        |\n" + 
				"| Row 3 Col 1    | :::                        | Row 2 Col 3        |\n" + 
				"| Row 1 Col 1    | Row 1 Col 2     | Row 1 Col 3        |\n" + 
				"| some colspan (note the double pipe) |||\n" + 
				"| :::                        | Row 2 Col 3        ||\n" + 
				"| :::                        | Row 2 Col 3        ||\n" + 
				"";
		expected = "^ Heading 1      ^ Heading 2       ^ Heading 3          ^\n" + 
				"| Row 1 Col 1    | Row 1 Col 2     | Row 1 Col 3        |\n" + 
				"| Row 2 Col 1    | some colspan (note the double pipe) ::UWCTOKENCOLSPANS:2::|\n" + 
				"| Row 3 Col 1    | Row 3 Col 2     | Row 3 Col 3        |\n" + 
				"| Row 1 Col 1    | this cell spans vertically | Row 1 Col 3        |\n" + 
				"| Row 2 Col 1    | :::                        | Row 2 Col 3        |\n" + 
				"| Row 3 Col 1    | :::                        | Row 2 Col 3        |\n" + 
				"| Row 1 Col 1    | Row 1 Col 2     | Row 1 Col 3        |\n" + 
				"| some colspan (note the double pipe) ::UWCTOKENCOLSPANS:3::|\n" + 
				"| :::                        | Row 2 Col 3        ::UWCTOKENCOLSPANS:2::|\n" + 
				"| :::                        | Row 2 Col 3        ::UWCTOKENCOLSPANS:2::|\n" + 
				"";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testPrepExtraColspanRow() {
		String input, expected, actual;
		input = "^ H1  ^ H2 ^ H3 ^ H4 ^ H5 ^ H6 ^\n" + 
				"^ rowspan starts      |  r1c1 | r1c2 | r1c3  | r1c4  | r1c5 |\n" + 
				"^ :::          |                           |                               |                                |                                | tada  |\n" + 
				"^ :::          |                           |                               |                                |                                |  foo |\n" + 
				"|              |                           |                               |                                |                                | bar  |\n" + 
				"| last row | tada |                               |                                |                                |                                                 |\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"|||||\n" + 
				"\n" + 
				"^ H1-2 ^ H2-2^\n" + 
				"^ Simple table | http://backoffice.do.dev.euc  |\n"; 
		expected = "^ H1  ^ H2 ^ H3 ^ H4 ^ H5 ^ H6 ^\n" + 
				"^ rowspan starts      |  r1c1 | r1c2 | r1c3  | r1c4  | r1c5 |\n" + 
				"^ :::          |                           |                               |                                |                                | tada  |\n" + 
				"^ :::          |                           |                               |                                |                                |  foo |\n" + 
				"|              |                           |                               |                                |                                | bar  |\n" + 
				"| last row | tada |                               |                                |                                |                                                 |\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"^ H1-2 ^ H2-2^\n" + 
				"^ Simple table | http://backoffice.do.dev.euc  |\n"; 
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPrepHeader() {
		String input, expected, actual;
		input = "^ h1             ^^^^^^ \n" +
				"^ a1 ^  a2 ^  a3 ^  a4 ^  a5 ^ a6 ^\n" + 
				"| r1 | r2 | r3 | r4  |r5 |r6 |\n";
		expected ="^ h1             ::UWCTOKENCOLSPANS:6::^ \n" +
				"^ a1 ^  a2 ^  a3 ^  a4 ^  a5 ^ a6 ^\n" + 
				"| r1 | r2 | r3 | r4  |r5 |r6 |\n";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
