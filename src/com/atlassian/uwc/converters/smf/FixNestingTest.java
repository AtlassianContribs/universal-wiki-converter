package com.atlassian.uwc.converters.smf;

import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class FixNestingTest extends TestCase {

	FixNesting tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new FixNesting();
		PropertyConfigurator.configure("log4j.properties");
	}
	public void testConvertNesting() {
		String input, expected, actual;
		input = "<ul>\n" + 
				"\n" + 
				"<li>a\n" + 
				"\n" + 
				"\n" + 
				"<ul><li>\n" + 
				"a</li>\n" + 
				"\n" + 
				"<li>b</li>\n" + 
				"</ul>\n" + 
				"";
		expected = "<ul>\n" + 
				"\n" + 
				"<li>a\n" + 
				"\n" + 
				"\n" + 
				"<ul><li>\n" + 
				"a</li>\n" + 
				"\n" + 
				"<li>b</li>\n" + 
				"</ul>\n" + 
				"</li>" +
				"</ul>";
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetIndexes() {
		String input, expected, actual;
		input = "<a>testing" +
				"	<a>123</a>" +
				"</a>";
		int[] expStart = {0, 11};
		int[] expEnd = {input.length() - 8, input.length() - 4};
		TreeMap<Integer, String> actStart = tester.getIndexes("<a>", input);
		TreeMap<Integer, String> actEnd = tester.getIndexes("<\\/a>", input);
		assertNotNull(actStart);
		assertNotNull(actEnd);
		assertTrue(actStart.containsKey(expStart[0]));
		assertTrue(actStart.containsKey(expStart[1]));
		assertTrue(actEnd.containsKey(expEnd[0]));
		assertTrue(actEnd.containsKey(expEnd[1]));

		input = "<a>testing" +
				"	<b>123</b>" +
				"</a>";
		actStart = tester.getIndexes("<[ab]>", input);
		actEnd = tester.getIndexes("<\\/[ab]>", input);
		assertNotNull(actStart);
		assertNotNull(actEnd);
		assertTrue(actStart.containsKey(expStart[0]));
		assertTrue(actStart.containsKey(expStart[1]));
		assertTrue(actEnd.containsKey(expEnd[0]));
		assertTrue(actEnd.containsKey(expEnd[1]));

	}
	
	public void testAddClosing() {
		String input, expected, actual;
		input = "<ol><li>test</ol>"; 
		expected = "<ol><li>test</li></ol>";
		TreeMap<Integer, String> start = tester.getIndexes("<((ol)|(li))>", input);
		TreeMap<Integer, String> end = tester.getIndexes("<\\/((ol)|(li))>", input);
		actual = tester.addClosing(start, end, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<ol>test<li>ing"; //use tags that are represented in NestingOrderComparator
		expected = "<ol>test<li>ing</li></ol>";
		TreeMap<Integer, String> start2 = tester.getIndexes("<((ol)|(li))>", input);
		TreeMap<Integer, String> end2 = tester.getIndexes("<\\/((ol)|(li))>", input);
		actual = tester.addClosing(start2, end2, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<ol>abc</ol></ol>";
		expected = "<ol>abc</ol>";
		TreeMap<Integer, String> start3 = tester.getIndexes("<((ol)|(li))>", input);
		TreeMap<Integer, String> end3 = tester.getIndexes("<\\/((ol)|(li))>", input);
		actual = tester.addClosing(start3, end3, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testSame() {
		TreeMap<Integer, String> a = new TreeMap<Integer, String>();
		TreeMap<Integer, String> b = new TreeMap<Integer, String>();
		a.put(1, "<a>");
		a.put(4, "<b>");
		a.put(12, "<a>");
		b.put(7, "</a>");
		b.put(33, "</b>");
		
		assertFalse(tester.same(a,b));
		b.put(43, "</a>");
		assertTrue(tester.same(a,b));
	}
	
	public void testFixNesting_ex1() {
		String input, expected, actual;
		input = "abcdef:\n" + 
				"<ul>\n" +   
				"\n" + 
				"<li>foobar\n" + 
				"abcdef\n" + 
				"\n" + 
				"<ul><li>foobar\n" + 
				"lorem</li>\n" + 
				"\n" + 
				"<li>ipsum</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"testing\n" + 
				"";
		expected = "abcdef:\n" + 
				"<ul>\n" +  
				"\n" + 
				"<li>foobar\n" + 
				"abcdef\n" + 
				"\n" + 
//				"</li><ul><li>foobar\n" +  //option 1
				"<ul><li>foobar\n" +  //option 2
				"lorem</li>\n" + 
				"\n" + 
				"<li>ipsum</li>\n" + 
				"</ul>\n" + 
				"\n" + 
//				"testing\n</ul>" + //option 1
				"testing\n</li></ul>" + //option 2
				"";
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixNesting_ex2() {
		String input, expected, actual;
		input = "abc\n" + 
				"<ul><li>abcdef</li></ul>\n" + 
				"<b>bold</b>\n" + 
				"</li>\n" +//XXX Problem starts here? 
				"</ul>\n" + 
				"\n" + 
				"";
		expected = "abc\n" + 
		"<ul><li>abcdef</li></ul>\n" + 
		"<b>bold</b>\n" + 
		"\n\n\n" +  //removed last two lines
		"";
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixNesting_ex3() {
		String input, expected, actual;
		input = 
				"<ul>\n" + 
				"<li>abc</li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>lorem - ipsum \n" + 
				"</li><li>lorem @ ipsum </li>\n" + 
				"<li>foobar </li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>tada </li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"</li>\n" +  //XXX This is the problem
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>abc </li>\n" + 
				"<li>def. </li>\n" + 
				"</ul> \n" + 
				"";
		expected = 				"<ul>\n" + 
				"<li>abc</li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>lorem - ipsum \n" + 
				"</li><li>lorem @ ipsum </li>\n" + 
				"<li>foobar </li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>tada </li>\n" + 
				"</ul> \n" + 
				"<ul>" + 
				"<li>\n" +
				"</li>\n" + 
				"</ul> \n" + 
				"<ul>\n" + 
				"<li>abc </li>\n" + 
				"<li>def. </li>\n" + 
				"</ul> \n" + 
				"";
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixNesting_nochanges1() {
		String input, expected, actual;
		input = "<ul>\n" + 
				"<li>abc</li>\n" + 
				"<li>\n" + 
				"def</li>\n" + 
				"<li> efg</li>\n" + 
				"<li>\n" + 
				"hig</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li> \n" + 
				" .</li>\n" + 
				"<li>testing</li>\n" + 
				"<li>\n" + 
				"    ( 123, 456, 789,\n" + 
				"000)\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"";
		expected = input;
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixNesting_nochange2() {
		String input, expected, actual;
		input = "*Original Poster:* [~admin]\n" + 
				"*Original Timestamp:* 2009-07-02 15:06:22\n" + 
				"\n" + 
				"Simple list:\n" + 
				"<ul>\n" + 
				"<li>Testing</li>\n" + 
				"<li>123</li>\n" + 
				"<li>abc</li>\n" + 
				"<li>def</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"Nesting:\n" + 
				"<ul>\n" + 
				"<li>level1\n" + 
				"   <ul>\n" + 
				"     <li>level 2</li>\n" + 
				"   </ul>\n" + 
				"</li>\n" + 
				"<li>foobar</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"\n" + 
				"{gallery:title=Attached Images}\n" + 
				"";
		expected = input;
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testFixNesting_nochange3() {
		String input, expected, actual;
		input = "*Original Poster:* [~admin]\n" + 
				"*Original Timestamp:* 2009-07-02 15:06:22\n" + 
				"\n" + 
				"Simple list:\n" + 
				"<ul>\n" + 
				"<li>Testing</li>\n" + 
				"<li>123</li>\n" + 
				"<li>abc</li>\n" + 
				"<li>def</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"Nesting:\n" + 
				"<ul>\n" + 
				"<li>level1\n" + 
				"   <ul>\n" + 
				"     <li>level 2</li>\n" + 
				"   </ul>\n" + 
				"</li>\n" + 
				"<li>foobar</li>\n" + 
				"</ul>\n" + 
				"No closing delimiter\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"no delim\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"With Links:\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"test <a href=\"http://abc.com\">http://abc.com</a>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"No LIs\n" + 
				"<ul>\n" + 
				"<li>* circle\n" + 
				"* circle\n" + 
				"* circle\n" + 
				"* disc\n" + 
				"* disc\n" + 
				"# square\n" + 
				"# square\n" + 
				"# square\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"\n" + 
				"{gallery:title=Attached Images}\n" + 
				"";
		expected = input;
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFixNesting_nochange4() {
		String input, expected, actual;
		input = "*Original Poster:* [~admin]\n" + 
				"*Original Timestamp:* 2009-07-02 16:11:24\n" + 
				"\n" + 
				"Probably do:\n" + 
				"[s]strike[/s]\n" + 
				"[pre]pre\n" + 
				"    testing\n" + 
				"[/pre]\n" + 
				"[hr]\n" + 
				"{color:red}color{color}\n" + 
				"[sup]sup[/sup]\n" + 
				"[sub]sub[/sub]\n" + 
				"[tt]mono[/tt]\n" + 
				"[code]code[/code]\n" + 
				"{quote}quote{quote}\n" + 
				"[quote=Author link=http://somesite/]text{quote}\n" + 
				"[nobbc] [/nobbc]\n" + 
				"\n" + 
				"Probably don&#039;t:\n" + 
				"[glow=red,2,300]test[/glow]\n" + 
				"[shadow=red,left]test[/shadow]\n" + 
				"[move]test[/move]\n" + 
				"[left]left[/left]\n" + 
				"[center]center[/center]\n" + 
				"[right]right[/right]\n" + 
				"[size=10pt]fontsize[/size]\n" + 
				"[flash=200,200][/flash]\n" + 
				"[font=Verdana]fontface[/font]\n" + 
				"<ul>\n" + 
				"<li>* circle\n" + 
				"* circle\n" + 
				"* circle\n" + 
				"* disc\n" + 
				"* disc\n" + 
				"# square\n" + 
				"# square\n" + 
				"# square\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"[abbr=exemlpi gratia]eg[/abbr]\n" + 
				"[acronym=Simple Machines Forum]SMF[/acronym]\n" + 
				"[html]\n" + 
				"[/html]\n" + 
				"[time]1132812640[/time]\n" + 
				"\n" + 
				"{gallery:title=Attached Images}\n" + 
				"";
		expected = input;
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testFixNesting_nochange5() {
		String input, expected, actual;
		input = "{color:red}Red{color}\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"abc\n" + 
				"<a href=\"http://www.abc.com/\">abc\n.com</a>\n" + 
				"{quote}loremipsum{quote}\n" + 
				"\n" + 
				"foobar\n" + 
				"<a href=\"http://www.foobar.com\">http://www.foobar.com</a>\n" + 
				"{quote}testing{quote}\n" + 
				"</li>\n" + 
				"</ul>";
		expected = input;
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	
	public void testNestingOrderComparator() {
		TreeSet<String> sorted = new TreeSet<String>(tester.new NestingOrderComparator());
		sorted.add("<ul>");
		sorted.add("<ol>");
		sorted.add("<li>");
		sorted.add("</ul>");
		sorted.add("</li>");

		int index = 0;
		for (String s : sorted) {
			assertNotNull(s);
			switch (index) {
			case 0:
				assertEquals("</li>", s);
				break;
			case 1:
				assertEquals("<li>", s);
				break;
			case 2:
				assertEquals("<ul>", s);
				break;
			case 3:
				assertEquals("<ol>", s);
				break;
			case 4:
				assertEquals("</ul>", s);
				break;
			}
			index++;
		}
	}
	
	public void testFixNesting_AddMissingOpeningLi() {
		String input, expected, actual;
		input = "<ul>\n" + 
				"\n" + 
				"a\n" + 
				"\n" + 
				"<ul><li>b</li>\n" + 
				"\n" + 
				"<li>c</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"d\n" + 
				"\n" + 
				"</ul>";
		expected = "<ul><li>\n" + 
				"\n" + 
				"a\n" + 
				"\n" + 
				"<ul><li>b</li>\n" + 
				"\n" + 
				"<li>c</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"d\n" + 
				"\n" + 
				"</ul>";
		actual = tester.addMissingLi(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		expected = "<ul><li>\n" + 
				"\n" + 
				"a\n" + 
				"\n" + 
				"<ul><li>b</li>\n" + 
				"\n" + 
				"<li>c</li>\n" + 
				"</ul>\n" + 
				"\n" + 
				"d\n" + 
				"\n" + 
				"</li></ul>";
		actual = tester.convertNesting(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsJustAdd() {
		TreeSet<String> input = new TreeSet<String>(tester.new NestingOrderComparator());
		input.add("li");
		assertFalse(tester.isJustAdd(input));
		
		input.clear();
		input.add("ul");
		assertTrue(tester.isJustAdd(input));
		
		input.clear();
		input.add("li");
		input.add("ul");
		assertTrue(tester.isJustAdd(input));
		
	}
}
