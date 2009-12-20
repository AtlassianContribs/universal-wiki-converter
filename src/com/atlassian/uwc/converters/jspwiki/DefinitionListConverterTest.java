package com.atlassian.uwc.converters.jspwiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class DefinitionListConverterTest extends TestCase {

	DefinitionListConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new DefinitionListConverter();
	}

	public void testConvertDefinitionLists() {
		String input = ";term:def";
		String expected = "* _term_\n" +
				"def";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertDefListLotsOfWords() {
		String input = ";many words:even more words";
		String expected = "* _many words_\n" +
				"even more words";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertSeveralItems() {
		String input = ";1:one\n" +
				";2:two\n" +
				";3:three\n";
		String expected = "* _1_\n" +
				"one\n" +
				"* _2_\n" +
				"two\n" +
				"* _3_\n" +
				"three\n";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertInContext() {
		String input = "Before the list\n\n" +
						";start:list\n" +
						";second:item\n" +
						"After the list\n";
		String expected = "Before the list\n\n" +
						"* _start_\n" +
						"list\n" +
						"* _second_\n" +
						"item\n" +
						"After the list\n";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvertWithWhitespace() {
		String input = "; term with ws : definition with ws\n";
		String expected = "* _term with ws_\n" +
				"definition with ws\n";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testDoesNotConvertStyles() {
		String input = "Some CSS next:\n" +
			"%%( font-name:Times New Roman; color:blue; background-color:#999999; border:2px dashed #999900;)\n" +
			"This panel could be weird\n" +
			"%%";
		String expected = input;
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testUseIndentProperty() {
		String input = ";term:def";
		String expected = "* _term_\n" +
				"def";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("definition-lists-useindent", "true");
		expected = "* _term_\n" +
				"{indent}def{indent}";
		actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSetEmphCharProperty() {
		String input = ";term:def";
		String expected = "* _term_\n" +
				"def";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("definition-lists-emphchar", "*");
		expected = "* *term*\n" +
				"def";
		actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testUseBulletProperty() {
		String input = ";term:def";
		String expected = "* _term_\n" +
				"def";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("definition-lists-usebullet", "false");
		expected = "_term_\n" +
				"def";
		actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testComboProperties() {
		String input = ";term:def";
		String expected = "* _term_\n" +
				"def";
		String actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Properties properties = tester.getProperties();
		properties.setProperty("definition-lists-useindent", "true");
		properties.setProperty("definition-lists-emphchar", "+");
		properties.setProperty("definition-lists-usebullet", "false");
		
		expected = "+term+\n" +
				"{indent}def{indent}";
		actual = tester.convertDefinitionLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
