package com.atlassian.uwc.converters.trac;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ImageParamConverterTest extends TestCase {

	ImageParamConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		tester = new ImageParamConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertImageParams() {
		String input, expected, actual;
		input = "!pagename:filename.png|width=230px align=right!\n" + 
				"";
		expected = "!pagename:filename.png|width=230px,align=right!\n";
		actual = tester.convertImageParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageParams_WidthNoKey() {
		String input, expected, actual;
		input = "\n!pagename:filename.png|50%!\n" + 
				"";
		expected = "\n!pagename:filename.png|thumbnail!\n";
		actual = tester.convertImageParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "!pagename:filename.png|align=right 50%!\n" + 
				"";
		expected = "!pagename:filename.png|align=right,thumbnail!\n";
		actual = tester.convertImageParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImageParams_AvoidProblemCase() {
		String input, expected, actual;
		//problem with newline in there was sidestepped by adding newline to neg char class
		input = "!abc.gif! random \n | pipe !def.gif! tada ";
		expected = input;
		actual = tester.convertImageParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		//problem with no newlines is harder and less likely
//		input = "!abc.gif! random  | pipe !def.gif! tada ";
//		expected = input;
//		actual = tester.convertImageParams(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);

		//don't match
		input = "abc \\!abc.gif|50%!";
		expected = input;
		actual = tester.convertImageParams(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
}
