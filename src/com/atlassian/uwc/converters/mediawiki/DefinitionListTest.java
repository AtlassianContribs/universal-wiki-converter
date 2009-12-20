package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DefinitionListTest extends TestCase {

	DefinitionList tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new DefinitionList();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertDefList_Simple() {
		String input, expected, actual;
		input = ";Abc:Def\n";
		expected = "* Abc\n" +
				"Def\n";
		actual = tester.convertDefList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDefList_MultiLine() {
		String input, expected, actual;
		input = ";Definition:list1\n" + 
				":list2\n" + 
				":list3\n" + 
				":list4\n" + 
				"\n"; 
		expected = "* Definition\n" +
				"list1\n" +
				"list2\n" +
				"list3\n" +
				"list4\n" +
				"\n";
		actual = tester.convertDefList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDefList_ItemNextLine() {
		String input, expected, actual;
		input = ";Definition\n" + 
				":list5 \n" + 
				":list6 \n" + 
				":list7\n" + 
				":list8\n" + 
				"";
		expected = "* Definition\n" +
				"list5 \n" +
				"list6 \n" +
				"list7\n" +
				"list8\n";
		actual = tester.convertDefList(input);
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}

	public void testConvertDefList_NonListText() {
		String input, expected, actual;
		input = "Before\n" +
				"\n" +
				";abc:def\n" +
				":ghi\n" +
				"\n" +
				"After" +
				"Some more : colons" +
				": colons at the beginning of a line";
		expected = "Before\n" +
				"\n" +
				"* abc\n" +
				"def\n" +
				"ghi\n" +
				"\n" +
				"After" +
				"Some more : colons" +
				": colons at the beginning of a line";
		actual = tester.convertDefList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDefList_MediawikiHelpEditing() {
		String input, expected, actual;
		input = "; Word : Definition of the word\n" + 
				"; A longer phrase needing definition\n" + 
				": Phrase defined\n" + 
				"; A word : Which has a definition\n" + 
				": Also a second one\n" + 
				"";
		expected = "* Word\n" + 
				"Definition of the word\n" + 
				"* A longer phrase needing definition\n" + 
				"Phrase defined\n" + 
				"* A word\n" + 
				"Which has a definition\n" + 
				"Also a second one\n" + 
				"";
		actual = tester.convertDefList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanWS() {
		String input, expected, actual;
		input = "\nok";
		expected = input;
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "\n ok";
		actual = tester.cleanWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
