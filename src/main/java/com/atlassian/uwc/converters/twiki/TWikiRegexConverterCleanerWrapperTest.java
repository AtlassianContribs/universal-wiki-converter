package com.atlassian.uwc.converters.twiki;

import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.twiki.cleaners.Base64EncodeBetweenCodeTags;
import com.atlassian.uwc.converters.twiki.cleaners.VerbatimOrCodeTagTokenizer;
import com.atlassian.uwc.ui.Page;

public class TWikiRegexConverterCleanerWrapperTest extends TestCase {

	TWikiRegexConverterCleanerWrapper tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TWikiRegexConverterCleanerWrapper();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert_StripDoctype() throws Exception { 
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.StripDoctypeTag";
		
		String input, expected, actual;
		input = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" + 
				"<html>\n" + 
				"<body>\n" +
				"testing" +
				"</body>\n" +
				"</html>" + 
				"\n" + 
				"";
		expected = "\n" +
				"<html>\n" + 
				"<body>\n" +
				"testing" +
				"</body>\n" +
				"</html>" + 
				"\n" + 
				"";
		
		Page page = new Page(null);
		page.setOriginalText(input);
		
		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConvert_VerbatimOrCodeTag() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.VerbatimOrCodeTagTokenizer";

		String input, expected, actual;
		input = "before\n" +
				"<code>abc</code>\n" +
				"<pre>testing</pre>\n" +
				"in between\n" +
				"<verbatim>foobar</verbatim>\n" +
				"after";
		expected = "before\n" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN + "abc" + VerbatimOrCodeTagTokenizer.CODE_TOKEN +
						"\n" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN + "testing" + VerbatimOrCodeTagTokenizer.CODE_TOKEN + 
						"\n" +
				"in between\n" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN + "foobar" + VerbatimOrCodeTagTokenizer.CODE_TOKEN + 
						"\n" +
				"after";
		
		Page page = new Page(null);
		page.setOriginalText(input);
		
		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

	public void testConvert_Base64Encode() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.Base64EncodeBetweenCodeTags";

		String input, expected, actual;
		input = "before\n" +
			VerbatimOrCodeTagTokenizer.CODE_TOKEN + "abc" + VerbatimOrCodeTagTokenizer.CODE_TOKEN +
			"\n";
		expected = "before\n" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING + 
				Base64EncodeBetweenCodeTags.CODE_BLOCK_TOKEN +
				"01" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING +
				"\n";
		
		Page page = new Page(null);
		page.setOriginalText(input);
		
		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

		HashMap cache = Base64EncodeBetweenCodeTags.codeBlockCache;
		assertNotNull(cache);
		String savedtext = (String) cache.get(Base64EncodeBetweenCodeTags.CODE_BLOCK_TOKEN+"01");
		assertNotNull(savedtext);
		assertEquals("abc", savedtext);
		
		//testing keytoken generation
		input = VerbatimOrCodeTagTokenizer.CODE_TOKEN + "foobar" + VerbatimOrCodeTagTokenizer.CODE_TOKEN;
		page.setOriginalText(input);
		tester.convert(page);
		assertTrue(cache.containsKey(Base64EncodeBetweenCodeTags.CODE_BLOCK_TOKEN + "11"));
		
		//and Decode
		converter = "com.atlassian.uwc.converters.twiki.cleaners.Base64DecodeBetweenCodeTags";

		input = "before\n" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING + 
				Base64EncodeBetweenCodeTags.CODE_BLOCK_TOKEN +
				"01" +
				VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING +
				"\n";
		expected = "before\n" +
				"{code}" + "abc" + "{code}" +
				"\n";
		page = new Page(null);
		page.setOriginalText(input);
		
		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		cache.clear();
	}
	
	public void testConvert_Header() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.Header";

		String input, expected, actual;
		input = "---+ Heading 1\n" + 
				"---++ Heading 2\n" + 
				"---++++++ Heading 6";
		expected = "h1.  Heading 1\n" +
				"h2.  Heading 2\n" +
				"h6.  Heading 6";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_AttImage() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.AttachedImage";

		String input, expected, actual;
		input = "<img src=\"%ATTACHURLPATH%/cow.jpg\" alt=\"cow.jpg\" width=\'450\' height=\'319\' />";
		expected = "!cow.jpg!";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_EscapeNonLinks() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.EscapeNonLinks1";

		String input, expected, actual;
		input = "a[testing]b";
		expected = "a\\[testing\\]b";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	

	public void testConvert_EscBoldDash() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.EscapeBoldDash";

		String input, expected, actual;
		input = "Testing *-* 123";
		expected = "Testing *\\-* 123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_EscStrike() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.EscapeStrikethrough";

		String input, expected, actual;
		input = " -testing- \n" +
				"=-foo-=\n" +
				"{-bar-}\n" +
				"}-lorem-{\n" +
				"--abc--\n" +//deal with this one someplace else
				"---\n" +    //so we can leave this one alone
				"---------"; 
		expected = " \\-testing\\- \n" +
				"=\\-foo\\-=\n" +
				"{\\-bar\\-}\n" +
				"}\\-lorem\\-{\n" +
				"--abc--\n" + //deal with this one someplace else
				"---\n" +
				"---------";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_Anchor() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.Anchor";

		String input, expected, actual;
		input = "Testing\n" +
				"#Anchor\n" +
				"123\n";
		expected = "Testing\n" +
				"{anchor:Anchor}\n" +
				"123\n";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_BoldFixed() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BoldFixedFont";

		String input, expected, actual;
		input = "Testing ==Bold and Monospace== 123";
		expected = "Testing {{*Bold and Monospace*}} 123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_BoldItal() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BoldItalics";

		String input, expected, actual;
		input = "Testing __Bold and Italics__ 123";
		expected = "Testing *_Bold and Italics_* 123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_FixedFont() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.FixedFont";

		String input, expected, actual;
		input = "Testing =mono= \n" +
				"=123=, =456=.\n" +
				"=abc=; =def=: =ghi=!\n" +
				"=lorem=? =ipsum=)";
		expected = "Testing {{mono}}  \n" +
				"{{123}} , {{456}} .\n" +
				"{{abc}} ; {{def}} : {{ghi}} !\n" +
				"{{lorem}} ? {{ipsum}} )";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_Separator() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.Separator";

		String input, expected, actual;
		input = "Testing\n" +
				"----------\n" +
				"123\n" +
				"---\n" +
				"foo\n" +
				"-------\n" +
				"--------Whatnow?"; //Fix this edgecase elsewhere
		expected = "Testing\n" +
				"----\n" +
				"123\n" +
				"----\n" +
				"foo\n" +
				"----\n" +
				"----Whatnow?";//Fix this edgecase elsewhere

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_TableColSpacer() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.TableColumnSpacer";

		String input, expected, actual;
		input = "|| Header||123||";
		expected = "| |  Header| | 123| | ";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_TableHeader() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.TableHeader";

		String input, expected, actual;
		input = "|*ABC*|*123*|";
		expected = "||ABC||123||";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//UWC-368
		input = "|* Works *|* C *|* R *|\n" + 
				"| *Nope* | *C* | *R* |\n" + 
				"";
		expected = "|| Works || C || R ||\n" + 
				"||Nope||C||R||\n";

		page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_RemoveMacros() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.RemoveTWikiMacros";

		String input, expected, actual;
		input = "%META:testing123%";
		expected = "";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_HtmlBold() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlBold";

		String input, expected, actual;
		input = "<b>Testing</b>";
		expected = "*Testing*";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_HtmlItal() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlItalics";

		String input, expected, actual;
		input = "<i>Testing 123</i>";
		expected = "_Testing 123_";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvert_HtmlHeader() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlHeader";

		String input, expected, actual;
		input = "<h1>Testing 123</h1>";
		expected = "h1. Testing 123\n";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvert_HtmlCode() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlCode";

		String input, expected, actual;
		input = "<code>Testing 123</code>";
		expected = "{{Testing 123}}";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_HtmlEmph() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlEmphasis";

		String input, expected, actual;
		input = "<em>Testing</em>";
		expected = "_Testing_";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_HtmlBreak() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlBreak";

		String input, expected, actual;
		input = "Testing 1" +
				"<br/>" +
				"Testing 2" +
				"<p>" +
				"Testing 3" +
				"</p>" +
				"Testing 4";
		expected = "Testing 1\n" +
				"Testing 2\n" +
				"Testing 3\n" +
				"Testing 4";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_HtmlQuote() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlBlockQuote";

		String input, expected, actual;
		input = "<blockquote>Testing</blockquote>";
		expected = "{quote}Testing{quote}";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_HtmlHref() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlHref";

		String input, expected, actual;
		input = "<a href=\"http://www.google.com\">Alias</a>";
		expected = "[Alias|http://www.google.com]";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	public void testConvert_ScrubOutNOP() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ScrubOutNOP";

		String input, expected, actual;
		input = "<nop>";
		expected = "";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_CamelCaseEsc() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.CamelCaseEscape";

		String input, expected, actual;
		input = "CamelCase\n" +
				"!CamelCase\n" +
				"!ThisIsNotALinkAbcDefAbcDef\n" +
				"!SomeFile.xml\n" +
				"!SomeFile123";
		expected = "CamelCase\n" +
				"{nl}CamelCase{nl}\n" +
				"{nl}ThisIsNotALinkAbcDefAbcDef{nl}\n" +
				"{nl}SomeFile.xml{nl}\n" +
				"{nl}SomeFile123{nl}";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_EmailLinks() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.EmailLinks";

		String input, expected, actual;
		input = "abc@something.com";
		expected = "[mailto:abc@something.com]";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "mailto:abc@something.com";
		expected = "[mailto:abc@something.com]";

		page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

		
	}	
	public void testConvert_HtmlConfTag() throws Exception {
	String converter = "com.atlassian.uwc.converters.twiki.cleaners.HtmlTagSurroundWithConfTag";
	
	String input, expected, actual;
	input = "<b>Testing123</b>";
	expected = "{html}<b>{html}Testing123{html}</b>{html}";
	
	Page page = new Page(null);
	page.setOriginalText(input);
	
	tester.getTWikiRegexConverterCleanerWrapper(converter);
	tester.setValue(converter);
	tester.convert(page);
	
	actual = page.getConvertedText();
	assertNotNull(actual);
	assertEquals(expected, actual);

}	
	
	public void testConvert_ToC() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.TableOfContents";

		String input, expected, actual;
		input = "Testing %TOC% 123";
		expected = "Testing {toc} 123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_ToCParams() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.TableOfContentsWithParams";

		String input, expected, actual;
		input = "%TOC\\{depth=\"4\"\\}%";
		expected = "{toc:maxLevel=4}";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_Red() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorRed";

		String input, expected, actual;
		input = "Testing color %RED% abc";
		expected = "Testing color {color:RED} abc";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Green() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorGreen";

		String input, expected, actual;
		input = "Testing color %GREEN% abc";
		expected = "Testing color {color:GREEN} abc";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	

	public void testConvert_Blue() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorBlue";

		String input, expected, actual;
		input = "lorem %BLUE% ipsum ";
		expected = "lorem {color:BLUE} ipsum ";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_Yellow() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorYellow";

		String input, expected, actual;
		input = "lorem %YELLOW% ipsum ";
		expected = "lorem {color:YELLOW} ipsum ";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvert_Orange() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorOrange";

		String input, expected, actual;
		input = "lorem %ORANGE% ipsum ";
		expected = "lorem {color:ORANGE} ipsum ";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testConvert_EndColor() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.ColorEndtag";

		String input, expected, actual;
		input = "lorem %ENDCOLOR% ipsum ";
		expected = "lorem {color} ipsum ";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	public void testConvert_BR() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BRTag";

		String input, expected, actual;
		input = "Testing %BR% 123";
		expected = "Testing \\\\ 123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_Bullet4() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BulletListLevel4";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t\t\t* abc\n" +
				"\t\t\t\t* def\n" +
				"            * spaces";
		expected = "Testing 123\n" +
				"**** abc\n" +
				"**** def\n" +
				"**** spaces";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_Bullet3() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BulletListLevel3";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t\t* abc\n" +
				"\t\t\t* def\n" +
				"         * spaces\n";
		expected = "Testing 123\n" +
				"*** abc\n" +
				"*** def\n" +
				"*** spaces\n";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Bullet2() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BulletListLevel2";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t* abc\n" +
				"\t\t* def\n" +
				"      * spaces\n";
		expected = "Testing 123\n" +
				"** abc\n" +
				"** def\n" +
				"** spaces\n";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Bullet1() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.BulletListLevel1";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t* abc\n" +
				"\t* def\n" +
				"   * spaces\n";
		expected = "Testing 123\n" +
				"* abc\n" +
				"* def\n" +
				"* spaces\n";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
	public void testConvert_Num4() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.NumberListLevel4";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t\t\t1. abc\n" +
				"\t\t\t\t1 def\n" +
				"            1. spaces";
		expected = "Testing 123\n" +
				"#### abc\n" +
				"#### def\n" +
				"#### spaces";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Num3() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.NumberListLevel3";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t\t1. abc\n" +
				"\t\t\t1 def\n" +
				"         1. spaces";
		expected = "Testing 123\n" +
				"### abc\n" +
				"### def\n" +
				"### spaces";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Num2() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.NumberListLevel2";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t\t1. abc\n" +
				"\t\t1 def\n" +
				"      1. spaces";
		expected = "Testing 123\n" +
				"## abc\n" +
				"## def\n" +
				"## spaces";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_Num1() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.NumberListLevel1";

		String input, expected, actual;
		input = "Testing 123\n" +
				"\t1. abc\n" +
				"\t1 def\n" +
				"   1. spaces";
		expected = "Testing 123\n" +
				"# abc\n" +
				"# def\n" +
				"# spaces";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	public void testConvert_RemoveStartInclude() throws Exception {
		String converter = "com.atlassian.uwc.converters.twiki.cleaners.RemoveSTARTINCLUDE";

		String input, expected, actual;
		input = "Testing %STARTINCLUDE% 123";
		expected = "Testing  123";

		Page page = new Page(null);
		page.setOriginalText(input);

		tester.getTWikiRegexConverterCleanerWrapper(converter);
		tester.setValue(converter);
		tester.convert(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);

	}	
	
//	public void testConvert_() throws Exception {
//		String converter = "";
//		
//		String input, expected, actual;
//		input = "";
//		expected = "";
//		
//		Page page = new Page(null);
//		page.setOriginalText(input);
//		
//		tester.getTWikiRegexConverterCleanerWrapper(converter);
//		tester.setValue(converter);
//		tester.convert(page);
//		
//		actual = page.getConvertedText();
//		assertNotNull(actual);
//		assertEquals(expected, actual);
//
//	}	

}
