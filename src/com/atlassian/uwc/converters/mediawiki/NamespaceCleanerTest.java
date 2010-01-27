package com.atlassian.uwc.converters.mediawiki;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class NamespaceCleanerTest extends TestCase {

	NamespaceCleaner tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new NamespaceCleaner();
	}
	
	public void testCleanNamespace_Simple() {
		String input, expected, actual;
		input = "[[Testing:Something]]";
		expected = "[[Testing__Something]]";
		actual = tester.cleanNamespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanNamespace_ExtraWS() { //uwc-187
		String input, expected, actual;
		input = "[[Testing: Withws]]";
		expected = "[[Testing___Withws]]";
		actual = tester.cleanNamespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testNotForImages() {
		String input = "[image:abcd.png]";
		String expected = input;
		Page page = new Page (new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		assertEquals(expected,actual);
		
		input = "[[Image:Abcd.png]\n" +"\n" +
			"uwc-101: Mediawiki image conversion syntax needs to be case insensitive\n" +
			"[[image:abcd.png]]\n" +
			"[[Image:abcd.png]]\n";
		expected = input;
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected,actual);
		
	}

	public void testNotExternal() {
		String input, expected, actual;
		input = "[[http://www.hub.slb.com/display/index.do?id=id2588770]]";
		expected = input;
		actual = tester.cleanNamespace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
