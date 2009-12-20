package com.atlassian.uwc.converters.twiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.DetokenizerConverter;
import com.atlassian.uwc.ui.Page;

public class JavaRegexAndTokenizerConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert_math() {
		String convStr = "((?s)<math>.*?<\\/math>){replace-with}$1\n";
		Converter converter = JavaRegexAndTokenizerConverter.getConverter(convStr);
		Page page = new Page(null);
		String input = "testing\n" +
				"<math>foobar</math>\n" +
				"after";
		page.setOriginalText(input);
		converter.convert(page);
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertTrue(actual.startsWith("testing\n~UWC_TOKEN_START~"));
		assertTrue(actual.contains("~UWC_TOKEN_START~"));
		assertTrue(actual.endsWith("~UWC_TOKEN_END~\nafter"));
		assertFalse(actual.contains("<math>foobar</math>"));
	}

	public void testConvert_code() {
		String convStr = "((?s) usage:.*?)(?=\n=)" +
				"{replace-with}" +
				"{code}NEWLINE$1NEWLINE{code}NEWLINE\n";
		Converter converter = JavaRegexAndTokenizerConverter.getConverter(convStr);
		Page page = new Page(null);
		String input = "before\n" +
				" usage: some usage message" + 
				"\n" + 
				"== next header ==";
		page.setOriginalText(input);
		converter.convert(page);
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertTrue(actual.startsWith("before\n~UWC_TOKEN_START~"));
		assertTrue(actual.contains("~UWC_TOKEN_START~"));
		assertTrue(actual.endsWith("~UWC_TOKEN_END~\n== next header =="));
		assertFalse(actual.contains("usage: some usage message"));
	}
	
	public void testConvert_Stack() { //UWC-398 
		String convStr = "<abc>(.*?)<\\/abc>" +
				"{replace-with}" +
				"ABC: $1";
		Converter abcTokenizer = JavaRegexAndTokenizerConverter.getConverter(convStr);
		abcTokenizer.setValue(convStr);
		
		String convStr2 = "([$]){replace-with}$1";
		Converter dollarTokenizer = JavaRegexAndTokenizerConverter.getConverter(convStr2);
		dollarTokenizer.setValue(convStr2);

		Converter detokenizer = new DetokenizerConverter();
		
		Page page = new Page(null);
		String input = "<abc>Testing $123</abc>";
		String expected = "ABC: Testing $123";
		page.setOriginalText(input);
		dollarTokenizer.convert(page); //tokenize dollars
		page.setOriginalText(page.getConvertedText());
		abcTokenizer.convert(page); //tokenize abc tags
		page.setOriginalText(page.getConvertedText());
		detokenizer.convert(page); //detokenize everybody in the right order
		
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);	
	}
}
