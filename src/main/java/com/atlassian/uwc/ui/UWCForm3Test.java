package com.atlassian.uwc.ui;

import junit.framework.TestCase;
import java.awt.event.KeyEvent;

public class UWCForm3Test extends TestCase {
	UWCForm3 tester = null;
	public void setUp() {
		tester = new UWCForm3();
	}
	
	public void testGetAvailableMnemonic() {
		String input = "abc";
		int expected = KeyEvent.VK_A;
		int actual = tester. getAvailableMnemonic(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		expected = KeyEvent.VK_B;
		actual = tester.getAvailableMnemonic(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = KeyEvent.VK_C;
		actual = tester.getAvailableMnemonic(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = -1;
		actual = tester.getAvailableMnemonic(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester = new UWCForm3();
		input = "uwc";
		expected = KeyEvent.VK_W; //ignores u, as already in use
		actual = tester.getAvailableMnemonic(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetFirstCharUpperCase() {
		String input = "abc";
		String expected = "Abc";
		String actual = tester.getFirstCharUpperCase(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Abc";
		actual = tester.getFirstCharUpperCase(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetWikiDocLink() {
		String input, expected, actual;
		input = "mediawiki";
		expected = UWCForm3.UWC_DOC_URL + "UWC+mediawiki+Notes";
		actual = tester.getWikiDocLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testxml";
		actual = tester.getWikiDocLink(input);
		assertNull(actual);
		
		input = "swiki-earlier";
		actual = tester.getWikiDocLink(input);
		assertNull(actual);
	}
}
