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
				"^ Header | 2 | 3 | 4 | 5 | 6 |^\n" +
				"^ Header | next colspans |||||\n";
		expected ="^ Header ^ Header ^ Header ^ Header ^ Header ^ Header ^\n" +
				"^ Header | 2 | 3 | 4 | 5 | 6 |^\n" + 
				"^ Header | next colspans ::UWCTOKENCOLSPANS:5::|\n";
		actual = tester.prep(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPropComplicated() {
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

}
