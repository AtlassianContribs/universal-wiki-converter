package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LinkConverterTest extends TestCase {

	LinkConverter tester;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		BasicConfigurator.configure();
		tester = new LinkConverter();
	}
	
	public void testLinksInternal() {
		String input = "((name of page))";
		String expected = "[name of page]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testLinksInternalWithAlias() {
		String input = "((name of page|display text))";
		String expected = "[display text|name of page]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testAnchorLink() {
		String input = "{ALINK(aname=section in this page)}label in this page{ALINK}";
		String expected = "[label in this page|#section in this page]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testExternalLink() {
		String input = "[http://www.example.com]";
		String expected = input;
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testExternalLinkWithAlias() {
		String input = "[http://www.example.com|display text]";
		String expected = "[display text|http://www.example.com]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testExternalLinkNoCache() {
		String input = "[http://www.example.com|display text|nocache]";
		String expected = "[display text|http://www.example.com]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testExternalSecureLink() {
		String input = "[https://security.com|Secure]";
		String expected = "[Secure|https://security.com]";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testWithNewlines() {
		String input = "((name of page))\n" +
				"((name of page|display text))\n" +
				"{ALINK(aname=section in this page)}label in this page{ALINK}\n";
		String expected = "[name of page]\n" +
				"[display text|name of page]\n" +
				"[label in this page|#section in this page]\n";
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
		
		input += "((another link|the link))\n" +
				"{ALINK(aname=another section)}another label{ALINK}\n";
		expected += "[the link|another link]\n" +
				"[another label|#another section]\n";
		actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);

	}
	
	public void testLoopingRegex() {
		String input = "abc_def_";
		String expected = "abc:def:";
		String replacement = "{group1}:";
		Pattern p = Pattern.compile("(\\w+?)_");
		Matcher finder = p.matcher(input);
		String actual = RegexUtil.loopRegex(finder, input, replacement);
		assertEquals(expected, actual);
		
		input = "((Somthing))\n((Else))\n";
		expected = "[Somthing]\n[Else]\n";
		replacement = "[{group1}]";
		p = Pattern.compile("\\(\\((.*?)\\)\\)");
		finder = p.matcher(input);
		actual = RegexUtil.loopRegex(finder, input, replacement);
		assertEquals(expected, actual);
		
		input = "(label|real)";
		expected = "[real+label]";
		replacement = "[{group2}+{group1}]";
		p = Pattern.compile("\\(([^|]*)\\|(.*)\\)");
		finder = p.matcher(input);
		actual = RegexUtil.loopRegex(finder, input, replacement);
		assertEquals(expected, actual);
		
	}
	
	public void testEscapedBrackets() {
		String input = "ALTER TABLE tblname ADD colname type \\[NOT NULL\\] \\[DEFAULT value\\];";
		String expected = input;
		String actual = tester.getConfluenceLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testNoLinkSyntaxLinks() {
		String input = "before http://www.google.com after";
		String expected = input;
		String actual = tester.getConfluenceLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
