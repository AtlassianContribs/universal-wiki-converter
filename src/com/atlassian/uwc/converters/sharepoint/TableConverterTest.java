package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TableConverterTest extends TestCase {

	TableConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TableConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertTables1() { //simple
		String input, expected, actual;
		input = "<html>" +
				"<table><tbody><tr><td rowspan=\"1\" colspan=\"1\">r1c1</td><td rowspan=\"1\" colspan=\"1\">r1c2</td></tr><tr><td rowspan=\"1\" colspan=\"1\">r2c1</td><td rowspan=\"1\" colspan=\"1\">r2c2" + 
				"</td></tr></tbody></table>\n" + 
				"</html>";
		expected = "<html>\n" +
				"| r1c1 | r1c2 |\n" + 
				"| r2c1 | r2c2 |\n" + 
				"</html>";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables2() { //headers
		String input, expected, actual;
		input = "<html>" +
				"<table><tbody><tr><th rowspan=\"1\" colspan=\"1\">r1c1</th><th rowspan=\"1\" colspan=\"1\">r1c2</th></tr><tr><td rowspan=\"1\" colspan=\"1\">r2c1</td><td rowspan=\"1\" colspan=\"1\">r2c2" + 
				"</td></tr></tbody></table>\n" + 
				"</html>";
		expected = "<html>\n" +
				"|| r1c1 || r1c2 ||\n" + 
				"| r2c1 | r2c2 |\n" + 
				"</html>";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables3() { //handle [col|row]spans
		String input, expected, actual;
		input = "<html>" +
				"<table><tbody><tr><td rowspan=\"1\" colspan=\"1\">r1c1</td><td colspan=\"2\" rowspan=\"1\">colspan -\n" + 
				"r1c2</td></tr><tr><td rowspan=\"1\" colspan=\"1\">r2c1</td><td rowspan=\"2\" colspan=\"1\">r2c2 -\n" + 
				"rowspan\n" + 
				"</td><td rowspan=\"1\" colspan=\"1\">r2c3\n" + 
				"</td></tr><tr><td colspan=\"1\" rowspan=\"1\">r3c1</td><td colspan=\"1\" rowspan=\"1\">r3c3\n" + 
				"</td></tr></tbody></table>\n" + 
				"</html>";
		expected = "<html>\n" +
				"| r1c1 | colspan -\\\\r1c2 |\n" + 
				"| r2c1 | r2c2 -\\\\rowspan | r2c3 |\n" + 
				"| r3c1 | r3c3 |\n" + 
				"</html>";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables4() { //other formatting
		String input, expected, actual;
		input = "<html>" +
					"<table><tbody>" +
						"<tr>" +
							"<td rowspan=\"1\" colspan=\"1\">*r1c1*</td>" +
							"<td rowspan=\"1\" colspan=\"1\">{color:#00ff00}r1c2{color}</td>" +
						"</tr>" +
						"<tr>" +
							"<td colspan=\"1\" rowspan=\"1\">{panel:bgColor=#ffd700}Œær2c1{panel}</td>" +
							"<td colspan=\"1\" rowspan=\"1\">h2. r2c2Œæ</td>" +
						"</tr>" +
					"</tbody></table>\n" + 
				"</html>";
		expected = "<html>\n" +
				"| *r1c1* | {color:#00ff00}r1c2{color} |\n" + 
				"| {panel:bgColor=#ffd700}r2c1{panel} | h2. r2c2 |\n" + 
				"</html>";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTables5() { //lists
		String input, expected, actual;
		input = "<html>" +
					"<table><tbody>" +
						"<tr>" +
							"<td rowspan=\"1\" colspan=\"1\">r1c1</td>" +
							"<td rowspan=\"1\" colspan=\"1\">r1c2</td>" +
							"<td rowspan=\"1\" colspan=\"1\">r2c1</td>" +
							"<td rowspan=\"1\" colspan=\"1\">r2c2\n" + 
							"</td>" +
						"</tr>" +
						"<tr>" +
							"<td colspan=\"1\" rowspan=\"1\">\n" + 
							"* Œæa\n" + 
							"* b\n" + 
							"* c\n" + 
							"\n" + 
							"</td>" +
							"<td colspan=\"1\" rowspan=\"1\">\n" + 
							"# one\n" + 
							"# two\n" + 
							"## threeŒæ\n" + 
							"\n" + 
							"</td>" +
							"<td colspan=\"1\" rowspan=\"1\">\n" + 
							"* Œæone\n" + 
							"# b\n" + 
							"* c\n" + 
							"\n" + 
							"</td>" +
							"<td colspan=\"1\" rowspan=\"1\">Œæ</td>" +
						"</tr>" +
					"</tbody></table>" + 
				"</html>";
		expected = "<html>\n" +
				"| r1c1 | r1c2 | r2c1 | r2c2 |\n" + 
				"| * a\\\\* b\\\\* c | # one\\\\# two\\\\## three | * one\\\\# b\\\\* c |  |" + 
				"</html>";
		actual = tester.convertTables(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRemoveWSBeforeNL() {
		String input, expected, actual;
		
		input = "| a | b | \n" +
				"| c | d | \n";
		expected = "| a | b |\n" +
				"| c | d |\n";
		actual = tester.removeWSBeforeNL(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "| a | b |\n" +
				"| c | d |\n";
		expected = "| a | b |\n" +
				"| c | d |\n";
		actual = tester.removeWSBeforeNL(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

}
