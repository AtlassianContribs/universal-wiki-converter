package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ListConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	ListConverter tester = null;
	protected void setUp() throws Exception {
		tester = new ListConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testUnorderedList() {
		String input, expected, actual;
		input = "Unordered Lists\n" + 
				"  * list item 1\n" + 
				"  * list item 2\n" + 
				"    * list item a\n" + 
				"    * list item b\n" + 
				"  * list item 3\n" + 
				"    * list item c\n" + 
				"      * foo\n" + 
				"        * bar\n" + 
				"      * foo2\n" + 
				"    * list item d\n" + 
				"";
		expected = "Unordered Lists\n" + 
				"* list item 1\n" + 
				"* list item 2\n" + 
				"** list item a\n" + 
				"** list item b\n" + 
				"* list item 3\n" + 
				"** list item c\n" + 
				"*** foo\n" + 
				"**** bar\n" + 
				"*** foo2\n" + 
				"** list item d\n" + 
				"";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testOrderedList() {
		String input, expected, actual;
		input = "Ordered Lists\n" + 
				"  - list item 1\n" + 
				"  - list item 2\n" + 
				"    - list item a\n" + 
				"    - list item b\n" + 
				"  - list item 3\n" + 
				"    - list item c\n" + 
				"      - foo\n" + 
				"        - bar\n" + 
				"      - foo2\n" + 
				"    - list item d\n" + 
				"";
		expected = "Ordered Lists\n" + 
				"# list item 1\n" + 
				"# list item 2\n" + 
				"## list item a\n" + 
				"## list item b\n" + 
				"# list item 3\n" + 
				"## list item c\n" + 
				"### foo\n" + 
				"#### bar\n" + 
				"### foo2\n" + 
				"## list item d\n";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMixedList() {
		String input, expected, actual;
		input = "Mixed Lists\n" + 
				"  * list item 1\n" + 
				"  * list item 2\n" + 
				"    - list item a\n" + 
				"    - list item b\n" + 
				"  * list item 3\n" + 
				"    - list item c\n" + 
				"      * foo\n" + 
				"        - bar\n" + 
				"      * foo2\n" + 
				"    - list item d\n" + 
				"";
		expected = "Mixed Lists\n" + 
				"* list item 1\n" + 
				"* list item 2\n" + 
				"*# list item a\n" + 
				"*# list item b\n" + 
				"* list item 3\n" + 
				"*# list item c\n" + 
				"*#* foo\n" + 
				"*#*# bar\n" + 
				"*#* foo2\n" + 
				"*# list item d\n";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testBadWS() {
		String input, expected, actual;
		input = "  *nows\n" +
				"  -nows";
		expected = "* nows\n" +
				"# nows";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSkipDepth() { //dokuwiki syntax says sets of two, but sometimes users ignore that
		String input, expected, actual;
		input = "  * foo\n" + 
				"    * bar\n" + 
				"      * meh\n" + 
				"  * foo2\n" + 
				"";
		expected = "* foo\n" + 
				"** bar\n" + 
				"*** meh\n" + 
				"* foo2\n" + 
				"";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNotSetsOfTwo() { //dokuwiki syntax says sets of two, but sometimes users ignore that
		String input, expected, actual;
		input = "   * foo\n" + 
				"      * bar\n" + 
				"      * bar2\n" + 
				"        * meh\n" + 
				"        * meh2\n" + 
				"        * meh3\n" + 
				"\n" + 
				"NEW LIST:\n" + 
				"   * FOO!\n" + 
				"";
		expected = "* foo\n" + 
				"** bar\n" + 
				"** bar2\n" + 
				"*** meh\n" + 
				"*** meh2\n" + 
				"*** meh3\n" + 
				"\n" + 
				"NEW LIST:\n" + 
				"* FOO!\n" + 
				"";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testTwoListsNotSetsOfTwo() {
		String input, expected, actual;
		input = "   * three\n" +
				"      - six\n" +
				"        * eight\n" +
				"Not a list\n" +
				"  * two\n" +
				"    * four\n" +
				"      - six\n" +
				"        * eight\n" +
				"      - six\n" +
				"Not a list";
		expected = "* three\n" +
				"*# six\n" +
				"*#* eight\n" +
				"Not a list\n" +
				"* two\n" +
				"** four\n" +
				"**# six\n" +
				"**#* eight\n" +
				"**# six\n" +
				"Not a list";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testManyLists() {
		String input, expected, actual;
		input = "Unordered Lists\n" + 
				"  * list item 1\n" + 
				"  * list item 2\n" + 
				"    * list item a\n" + 
				"    * list item b\n" + 
				"  * list item 3\n" + 
				"    * list item c\n" + 
				"      * foo\n" + 
				"        * bar\n" + 
				"      * foo2\n" + 
				"    * list item d\n" + 
				"\n" + 
				"Ordered Lists\n" + 
				"  - list item 1\n" + 
				"  - list item 2\n" + 
				"    - list item a\n" + 
				"    - list item b\n" + 
				"  - list item 3\n" + 
				"    - list item c\n" + 
				"      - foo\n" + 
				"        - bar\n" + 
				"      - foo2\n" + 
				"    - list item d\n" + 
				"\n" + 
				"Mixed Lists\n" + 
				"  * list item 1\n" + 
				"  * list item 2\n" + 
				"    - list item a\n" + 
				"    - list item b\n" + 
				"  * list item 3\n" + 
				"    - list item c\n" + 
				"      * foo\n" + 
				"        - bar\n" + 
				"      * foo2\n" + 
				"    - list item d";
		expected = "Unordered Lists\n" + 
				"* list item 1\n" + 
				"* list item 2\n" + 
				"** list item a\n" + 
				"** list item b\n" + 
				"* list item 3\n" + 
				"** list item c\n" + 
				"*** foo\n" + 
				"**** bar\n" + 
				"*** foo2\n" + 
				"** list item d\n" + 
				"\n" + 
				"Ordered Lists\n" + 
				"# list item 1\n" + 
				"# list item 2\n" + 
				"## list item a\n" + 
				"## list item b\n" + 
				"# list item 3\n" + 
				"## list item c\n" + 
				"### foo\n" + 
				"#### bar\n" + 
				"### foo2\n" + 
				"## list item d\n" + 
				"\n" + 
				"Mixed Lists\n" + 
				"* list item 1\n" + 
				"* list item 2\n" + 
				"*# list item a\n" + 
				"*# list item b\n" + 
				"* list item 3\n" + 
				"*# list item c\n" + 
				"*#* foo\n" + 
				"*#*# bar\n" + 
				"*#* foo2\n" + 
				"*# list item d";
		actual = tester.convertList(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
