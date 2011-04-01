package com.atlassian.uwc.converters.jive;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class PreConverterTest extends TestCase {

	PreConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new PreConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvertPre_simple() {
		String input, expected, actual;
		input = "<pre>Testing {color:red} </pre>";
		expected = "{code}Testing {color:red} {code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPre_inPre() {
		String input, expected, actual;
		input = "<pre __default_attr=\"html\" __jive_macro_name=\"code\"><![CDATA[<pre>-foo&lt;bar&gt;</pre>]]></pre>" + 
				"";
		expected = "{{-foo&lt;bar&gt;}}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//another example - this one has to be handled with code blocks though
		input = "<pre __default_attr=\"java\" __jive_macro_name=\"code\" class=\"jive_text_macro jive_macro_code\">" +
				"<span style=\"text-decoration: line-through;\">test.foo()</span> // abc" +
				"<pre ___default_attr=\"java\" jivemacro=\"code\"><br/>test.bar()\n" + 
				"</pre>\n" + 
				"";
		expected = "{code}test.foo() // abc\n" +
				"test.bar()\n" +
				"{code}\n";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<pre >abc <pre>123</pre> 456</pre>";
		expected = "{code}abc 123 456{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<pre>abc 123<pre> 456</pre>";
		expected = "{code}abc 123 456{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConvertPre_inCData() {
		String input, expected, actual;
		input = "<![CDATA[<pre>\n" + 
				"Foo ::= BAR {\n" + 
				"  foo  bar,\n" + 
				"}\n" + 
				"</pre>]]>";
		expected = "{code}\n" +
				"Foo ::= BAR {\n" + 
				"  foo  bar,\n" + 
				"}\n" +		
				"{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPre_CDatainpre() {
		String input, expected, actual;
		input = "<pre __jive_macro_name=\"code\"><![CDATA[lalala \"Testing\" -tada-]]></pre>";
		expected = "{code}lalala \"Testing\" -tada-{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPre_hasHtml() {
		String input, expected, actual;
		input = "<pre __default_attr=\"java\" __jive_macro_name=\"code\" " +
				"class=\"jive_text_macro jive_macro_code\"><p>simple extraneous html problem</p></pre>";
		expected = "{code}simple extraneous html problem\n" +
				"{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertPre_hasEntities() {
		String input, expected, actual;
		input = "<pre __default_attr=\"xml\" __jive_macro_name=\"code\" " +
				"class=\"jive_text_macro jive_macro_code\"><div id=\"_mcePaste\">&lt;foo:bar&gt;</div></pre>";
		expected = "{code}<foo:bar>\n" +
				"{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPre_blockVsSpan() {
		String input, expected, actual;
		input = "<pre __jive_macro_name=\"quote\" class=\"jive_text_macro jive_macro_quote\">Testing \"<strong>foo</strong>\" bar </pre>";
		expected = "{code}Testing \"foo\" bar {code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPre_cdata2() {
		String input, expected, actual;
		input = "<![CDATA[<pre>\n" + 
				"public class main {\n" + 
				"   int a = 0" +
				"}\n" + 
				"</pre>\n" + 
				"\n" + 
				"testing:\n" + 
				"\n" + 
				"<pre>\n" + 
				"public class mainagain extends {\n" + 
				"  byte[] testing(byte[] data);\n" + 
				"}\n" + 
				"</pre>]]>";
		expected = "{code}\n" + 
				"public class main {\n" + 
				"   int a = 0}\n" + 
				"{code}\n" + 
				"\n" + 
				"testing:\n" + 
				"\n" + 
				"{code}\n" + 
				"public class mainagain extends {\n" + 
				"  byte[] testing(byte[] data);\n" + 
				"}\n" + 
				"{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPreWithTitle() {
		String input, expected, actual;
		input = "<pre __jive_macro_name=\"quote\" title=\"Foo Bar\"><p></p><p>Testing $$${$123}</p></pre>" +
				"<pre __jive_macro_name=\"quote\" title=\"Test\"><p></p><p>shell$ cd testdir</p><p>ls</p><p></p></pre><h1><span>Header</span></h1>";
		expected = "{code:title=Foo Bar}\n" +
				"Testing $$${$123}\n" +
				"{code}" +
				"{code:title=Test}\n" +
				"shell$ cd testdir\n" +
				"ls\n" +
				"\n" +
				"{code}" +
				"<h1><span>Header</span></h1>";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testBQInPre() {
		String input, expected, actual;
		input = "<pre __default_attr=\"html\" __jive_macro_name=\"code\"><![CDATA[<blockquote>Testing 123<br /><br />" +
				"</blockquote>]]></pre>";
		expected = "{code}" +
				"Testing 123\n" +
				"\n" +
				"\n" +
				"{code}";
		actual = tester.convertPre(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
