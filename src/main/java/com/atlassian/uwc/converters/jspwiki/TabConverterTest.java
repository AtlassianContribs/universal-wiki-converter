package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class TabConverterTest extends TestCase {

	TabConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TabConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.clearUnique();
	}

	public void testConvertTabs() {
		String input, expected, actual;
		input = "before tabbed section\n" + 
				"\n" + 
				"%%tabbedSection\n" + 
				"%%tab-tab1\n" + 
				"content on tab 1\n" + 
				"/%\n" + 
				"%%tab-tab2\n" + 
				"content on tab 2\n" + 
				"/%\n" + 
				"/%\n" + 
				"\n" + 
				"outside of tabbed section\n" + 
				"";
		expected = "before tabbed section\n" + 
				"\n" + 
				"{composition-setup}\n" + 
				"{deck:id=1}\n" + 
				"{card:label=tab1}\n" + 
				"content on tab 1\n" + 
				"{card}\n" + 
				"{card:label=tab2}\n" + 
				"content on tab 2\n" + 
				"{card}\n" + 
				"{deck}\n" + 
				"\n" + 
				"outside of tabbed section\n" + 
				"";
		actual = tester.convertTabs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testInsertCompositionPluginOnce() {
		String input, expected, actual;
		input = "before tabbed section\n" + 
				"\n" + 
				"%%tabbedSection\n" + 
				"%%tab-tab1\n" + 
				"content on tab 1\n" + 
				"/%\n" + 
				"%%tab-tab2\n" + 
				"content on tab 2\n" + 
				"/%\n" + 
				"/%\n" + 
				"\n" + 
				"outside of tabbed section\n" + 
				"\n" + 
				"And a second one:\n" + 
				"\n" + 
				"%%tabbedSection\n" + 
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"/%" +
				"";
		expected = "before tabbed section\n" + 
				"\n" + 
				"{composition-setup}\n" + 
				"{deck:id=1}\n" + 
				"{card:label=tab1}\n" + 
				"content on tab 1\n" + 
				"{card}\n" + 
				"{card:label=tab2}\n" + 
				"content on tab 2\n" + 
				"{card}\n" + 
				"{deck}\n" + 
				"\n" + 
				"outside of tabbed section\n" + 
				"\n" + 
				"And a second one:\n" + 
				"\n" + 
				"{deck:id=2}\n" + 
				"{card:label=foo}\n" + 
				"abc" +
				"{card}\n" + 
				"{card:label=bar}\n" +
				"def\n" +
				"{card}\n" +
				"{deck}";
		actual = tester.convertTabs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testWhitespace() {
		String input, expected, actual;
		input = "%%tabbedSection\n" +
				"\n" + 
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"\n" +
				"\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"\n" +
				"\n" +
				"\n" +
				"/%";
		expected = 	"{composition-setup}\n" +
				"{deck:id=1}\n" +
				"\n" + 
				"{card:label=foo}\n" + 
				"abc" +
				"{card}\n" +
				"\n" +
				"\n" + 
				"{card:label=bar}\n" +
				"def\n" +
				"{card}\n" +
				"\n" +
				"\n" +
				"\n" +
				"{deck}";
		actual = tester.convertTabs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testNeedsSetup() {
		String input;
		input = "testing";
		boolean actual = tester.needsSetup(input);
		assertTrue(actual);
		
		input = "{composition-setup}";
		actual = tester.needsSetup(input);
		assertFalse(actual);
		
		input = "testing 123\n" +
				"{composition-setup}" +
				"foobar";
		assertFalse(actual);
	}
	public void testAddSetup() {
		String input, expected, actual;
		input = "testing";
		expected = input;
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{composition-setup}";
		expected = input;
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "testing 123\n" +
				"{composition-setup}" +
				"foobar";
		expected = input;
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "%%tabbedSection";
		expected = "{composition-setup}\n" + input;
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "before\n" +
				"%%tabbedSection";
		expected = "before\n" +
				"{composition-setup}\n" +
				"%%tabbedSection";
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "one two three\n" +
				"%%tabbedSection\n" +
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"\n" +
				"\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"\n" +
				"\n" +
				"\n" +
				"/%" +
				"\n" +
				"testing\n" +
				"%%tabbedSection\n" +
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"\n" +
				"\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"\n" +
				"\n" +
				"\n" +
				"/%" +
				"\n" +
				"testing\n";
		expected = "one two three\n" +
				"{composition-setup}\n" +
				"%%tabbedSection\n" +
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"\n" +
				"\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"\n" +
				"\n" +
				"\n" +
				"/%" +
				"\n" +
				"testing\n" +
				"%%tabbedSection\n" +
				"%%tab-foo\n" + 
				"abc" +
				"/%\n" +
				"\n" +
				"\n" +
				"%%tab-bar\n" +
				"def\n" +
				"/%\n" +
				"\n" +
				"\n" +
				"\n" +
				"/%" +
				"\n" +
				"testing\n";
		actual = tester.addSetup(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetTabPart() {
		boolean inDeck, inCard, startTabs, startCard, endDelim;
		
		inDeck = startTabs = true;
		inCard = startCard = endDelim = false;
		String tabname = null, tabcontents = null;
		String expected = "{deck:id=1}";
		String actual = tester.getTabPart(inDeck , inCard, startTabs, startCard, endDelim, tabname, tabcontents);
		assertNotNull(actual);
		assertEquals(expected, actual);
		//check uniqueness handling
		expected = "{deck:id=2}";
		actual = tester.getTabPart(inDeck , inCard, startTabs, startCard, endDelim, tabname, tabcontents);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		inDeck = inCard = startCard = true;
		startTabs = endDelim = false;
		tabname = "foo";
		tabcontents = "testing 123\nabcdef";
		expected = "{card:label=foo}" + tabcontents;
		actual = tester.getTabPart(inDeck , inCard, startTabs, startCard, endDelim, tabname, tabcontents);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		inDeck = inCard = endDelim = true;
		startCard = startTabs = false;
		tabname = tabcontents = null;
		expected = "{card}";
		actual = tester.getTabPart(inDeck , inCard, startTabs, startCard, endDelim, tabname, tabcontents);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		inDeck = endDelim = true;
		inCard = startCard = startTabs = false;
		expected = "{deck}";
		actual = tester.getTabPart(inDeck , inCard, startTabs, startCard, endDelim, tabname, tabcontents);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testSetDeckState() {
		boolean inDeck, inCard, startTabs, startCard, endDelim;
		inDeck = startTabs = startCard = endDelim = inCard = false;
		assertFalse(tester.setDeckState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startTabs = true;
		assertTrue(tester.setDeckState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startTabs = inCard = endDelim = false;
		startCard = inDeck = true;
		assertTrue(tester.setDeckState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startCard = false;
		endDelim = inCard = true;
		assertTrue(tester.setDeckState(inDeck, inCard, startTabs, startCard, endDelim));
		
		inCard = false;
		assertFalse(tester.setDeckState(inDeck, inCard, startTabs, startCard, endDelim));
	}
	public void testSetCardState() {
		boolean inDeck, inCard, startTabs, startCard, endDelim;
		inDeck = startTabs = startCard = endDelim = inCard = false;
		assertFalse(tester.setCardState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startTabs = true;
		assertFalse(tester.setCardState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startTabs = inCard = endDelim = false;
		startCard = inDeck = true;
		assertTrue(tester.setCardState(inDeck, inCard, startTabs, startCard, endDelim));
		
		startCard = false;
		endDelim = inCard = true;
		assertFalse(tester.setCardState(inDeck, inCard, startTabs, startCard, endDelim));
		
		inCard = false;
		assertFalse(tester.setCardState(inDeck, inCard, startTabs, startCard, endDelim));
	}

	public void testOtherSameEndDelims() {
		String input, expected, actual;
		input = "This is a sample file for quote blocks - as described in UWC-333\n" + 
				"\n" + 
				"%%quote \n" + 
				"quoted text\n" + 
				"more quoted text\n" + 
				"\n" + 
				"even more\n" + 
				"/%\n" + 
				"";
		expected = input;
		actual = tester.convertTabs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
