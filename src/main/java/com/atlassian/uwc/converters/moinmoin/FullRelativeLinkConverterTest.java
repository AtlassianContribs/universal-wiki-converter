package com.atlassian.uwc.converters.moinmoin;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FullRelativeLinkConverterTest extends TestCase {

	FullRelativeLinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new FullRelativeLinkConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLinks() {
		String input, expected, actual;
		String title = "TestPage";
		input = "[[/Subpage]]\n" + 
				"[[/Subpage|alias]]";
		expected = "[TestPage Subpage]\n" +
				"[alias|TestPage Subpage]";
		actual = tester.convertLinks(input, title);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinksNoExt() {
		String input, expected, actual;
		String title = "TestPage.txt";
		input = "[[/Subpage]]\n" + 
				"[[/Subpage|alias]]";
		expected = "[TestPage Subpage]\n" +
				"[alias|TestPage Subpage]";
		actual = tester.convertLinks(input, title);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
