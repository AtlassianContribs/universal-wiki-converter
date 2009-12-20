
package com.atlassian.uwc.converters;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.TokenMap;

public class IllegalLinkNameConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	IllegalLinkNameConverter tester = null;
	Page page = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new IllegalLinkNameConverter();
		page = new Page(new File("")); 
	}
	
	
	public void testAccessToIllegalNameConverterMethod() {
		String input = "testing";
		String expected = input;
		String actual = tester.convertIllegalName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_None() {
		String input = "Before [NoIllegalChars] After";
		String expected = input;
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_One() {
		String input = "Before [OneIllegalChar[End] After";
		String expected = "Before [OneIllegalChar(End] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvert_PreserveSpaceColon() {
		String input, expected, actual;
		input = "Before [Space:OneIllegalChar:End] After";
		expected = "Before [Space:OneIllegalChar.End] After"; 
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testBadStart() {
		String input = "Before [$MONEY] After";
		String expected = "Before [_MONEY] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_GoodUrlEncoding() {
		Properties props = new Properties();
		props.setProperty("illegalnames-urldecode", "true");
		tester.setProperties(props);
		String input = "Before [OneGoodUrlEncodedChar%5fEnd] After";
		String expected = "Before [OneGoodUrlEncodedChar_End] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Before [OneGoodUrlEncodedChar%43End] After";
		expected = "Before [OneGoodUrlEncodedCharCEnd] After";
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_IllegalUrlEncoding() {
		Properties props = new Properties();
		props.setProperty("illegalnames-urldecode", "true");
		tester.setProperties(props);
		String input = "Before [OneBadUrlEncoding%7bEnd] After";
		String expected = "Before [OneBadUrlEncoding(End] After"; //translates encoding, then gets legal equivalent 
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultipleIllegalChars() {
		String input = "Before [MultBadChars;and}End] After";
		String expected = "Before [MultBadChars.and)End] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultipleIllegalChars_PreserveSpace() {
		String input = "Before [MultBad:Chars;and}End] After";
		String expected = "Before [MultBad:Chars.and)End] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testBothTypesOfIllegalChars() {
		Properties props = new Properties();
		props.setProperty("illegalnames-urldecode", "true");
		tester.setProperties(props);
		String input = "Before [BothBadChars%7band{End] After";
		String expected = "Before [BothBadChars(and(End] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testRightBracketProblem() {
		String input = "Before [Hello]Bah] After";
		String expected = "Before [Hello)Bah] After";
		HashSet<String> pagenames = new HashSet<String>();
		tester.setIllegalPagenames(pagenames);
		pagenames.add("Hello]Bah");
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testDontConvertEscapedBrackets() {
		String input = "{{ ALTER TABLE tblname ADD colname type \\[NOT NULL\\] \\[DEFAULT value\\]; }}";
		String expected = input;
		String actual = tester.legalizeLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Testing \\[Escaped [brackets]";
		expected = input;
		actual = tester.legalizeLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testPipeProblem() {
		String input = "Before [alias|Something|Something] After";
		String expected = "Before [alias|Something-Something] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testColonProblem() {
		//how can we tell if a colon means a space or a bad pagename?
		String input = "Before [Something:Something] After";
		String expected = "Before [Something:Something] After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testManyLinks() {
		String input = "Before\n" +
				"[Bad[]\n" +
				"Middle\n" +
				"[Bad{]\n" +
				"After";
		String expected = "Before\n" +
			"[Bad(]\n" +
			"Middle\n" +
			"[Bad(]\n" +
			"After";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAlias() {
		String input = "[alias|Bad[]";
		String expected = "[alias|Bad(]";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
//	public void testEncodeBrackets() {
//		String input, expected, actual;
//		String input = "[]]end";
//		String expected = "[%5d]end";
//		String actual = tester.encodeBrackets(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
		
//		input = "[]]";
//		expected = "[%5d]";
//		actual = tester.encodeBrackets(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
		
//		input = "blah \\[ [correct]";
//		expected = input;
//		actual = tester.encodeBrackets(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
//		
//		
//		input = "nesting [ a [ b ] c ]";
//		expected = "nesting [ a [ b %5d c ]";
//		actual = tester.encodeBrackets(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
//
//		input = "too [many ]right]";
//		expected = "too [many %5d]right]";
//		actual = tester.encodeBrackets(input);
//		assertNotNull(actual);
//		assertEquals(expected, actual);
//		
//	}

	
	public void testIdentifyAlias() {
		String input = "abc";
		String expected = "";
		String actual = tester.identifyAlias(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "alias|abc";
		expected = "alias";
		actual = tester.identifyAlias(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIdentifyAnchor() {
		String input = "abc";
		String expected = "";
		String actual = tester.identifyInPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "alias|#abc";
		expected = "#";
		actual = tester.identifyInPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "#abc";
		expected = "#";
		actual = tester.identifyInPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "not#the#beginning";
		expected = "";
		actual = tester.identifyInPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHashProblem() {
		//aliases with a hash shouldn't cause a hash to be added to the link
		String input, expected, actual;
		input = "[#alias|http://www.google.com]";
		expected = input;
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testIdentifyLink() {
		String input = "abc";
		String expected = input;
		String actual = tester.identifyLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc|def";
		expected = "def";
		actual = tester.identifyLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abc#def";
		expected = input;
		actual = tester.identifyLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIdentifyOtherLink() {
		String input = "abc";
		String expected = "";
		String actual = tester.identifyOtherPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "#anchor";
		expected = "";
		actual = tester.identifyOtherPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Page#anchor";
		expected = "anchor";
		actual = tester.identifyOtherPageAnchor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsFile() {
		String input = "abc";
		assertFalse(tester.isAttachment(input));
		
		input = "^abc";
		assertTrue(tester.isAttachment(input));
	}
	
	public void testIsExternalLink() {
		String input = "http://something";
		boolean expected = true;
		boolean actual = tester.isExternalLink(input);
		assertEquals(expected, actual);
		
		input = "something";
		expected = false;
		actual = tester.isExternalLink(input);
		assertEquals(expected, actual);
		
		input = "file://myserver/hiddenshare/folder";
		expected = true;
		actual = tester.isExternalLink(input);
		assertEquals(expected, actual);
		
		input = "ftp://something.com";
		expected = true;
		actual = tester.isExternalLink(input);
		assertEquals(expected, actual);
	}
	

	public void testCodeBlockProblem() {
		String input = "{code} blah [[blah]] blah{code}";
		String expected = input;
		HashSet<String> pagenames = new HashSet<String>();
		pagenames.add("unimportant");
		tester.setIllegalPagenames(pagenames);
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{code:xml} blah [[blah]] blah {code}";
		expected = input;
		pagenames = new HashSet<String>();
		pagenames.add("unimportant");
		tester.setIllegalPagenames(pagenames);
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testLegalizeWithState() {
		//no object
		String input = "Beatles song: [revolution #9] ";
		String expected = input;
		String actual = null;
		try {
			actual = tester.legalizeWithState(input, null);
			fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {}
		
		//object, but empty
		actual = tester.legalizeWithState(input, new HashSet<String>());
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//object with something
		HashSet<String> state = new HashSet<String>();
		String goodfilename = "Back in the USSR";
		state.add(goodfilename);
		actual = tester.legalizeWithState(input, state);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//object with the particular filename
		String badfilename = "revolution #9";
		expected = "Beatles song: [revolution No.9] ";
		state.add(badfilename);
		actual = tester.legalizeWithState(input, state);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//object with regex chars
		String regexFilename = "Too [ManyBrackets";
		String regexContent= "Testing [Too [ManyBrackets]";
		expected = "Testing [Too (ManyBrackets]";
		state.add(regexFilename);
		actual = tester.legalizeWithState(regexContent, state);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testLegalizeLinks() {
		String input = "[abc @ def]";
		String expected = "[abc at def]";
		String actual = tester.legalizeLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//test one page in the illegalNames object
		HashSet<String> names = new HashSet<String>();
		names.add("abc @ def");
		tester.setIllegalPagenames(names);
		actual = tester.legalizeLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//test uniqueness
		tester.addIllegalPagename("abc @ def");
		tester.setIllegalPagenames(names);
		actual = tester.legalizeLinks(input);
		HashSet<String> unique = tester.getIllegalPagenames();
		assertNotNull(unique);
		assertEquals(1, unique.size());
		
		//test non-state run regardless of state?
		input = "[semi ; colon]";
		expected = "[semi . colon]";
		actual = tester.legalizeLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Testing Escaped Brackets#anchor]";
		expected = input;
		actual = tester.legalizeLinksWithoutState(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testEscaped() {
		char ch = '\\';
		int index = 0;
		String input = "abc";
		assertFalse(tester.escaped(input, index, ch));
		
		input = "abc [bracket]";
		index = 5;
		assertFalse(tester.escaped(input, index, ch));
		
		input = "[bracket]";
		index = 1;
		assertFalse(tester.escaped(input, index, ch));
		
		input = "\\[escaped]";
		index = 2;
		assertTrue(tester.escaped(input, index, ch));
		
		input = "askdj \\[blah [link]asdjkj";
		index = 8;
		assertTrue(tester.escaped(input, index, ch));
		
		index = 14;
		assertFalse(tester.escaped(input, index, ch));
	}
	
	public void testDontMessupEmails() {
		String input, expected, actual;
		input = "[mailto:somebody@somewhere.com]";
		expected = input;
		actual = tester.legalizeLinksWithoutState(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontMessupAttachmentLinks() {
		String input, expected, actual;
		input = "[Page^attachment.png]";
		expected = "[Page^attachment.png]";
		actual = tester.legalizeLinksWithoutState(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	/* Helper Methods */
	public String getActual(String input) {
		String actual;
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		return actual;
	}


	public void testTokenizeCodeBlocks() {
		String input = "{code} blah {code}";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.tokenizeCodeBlocks(page);

		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertFalse(input.equals(actual));
		assertTrue(actual.startsWith(TokenMap.TOKEN_START));
		assertTrue(actual.endsWith(TokenMap.TOKEN_END));
		
		String tokenValue = TokenMap.detokenizeText(actual);
		assertNotNull(tokenValue);
		assertEquals(input, tokenValue);
		
		//noformat
		input = "{noformat} blah {noformat}";
		page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.tokenizeCodeBlocks(page);

		actual = page.getConvertedText();
		assertNotNull(actual);
		assertFalse(input.equals(actual));
		assertTrue(actual.startsWith(TokenMap.TOKEN_START));
		assertTrue(actual.endsWith(TokenMap.TOKEN_END));
		
		tokenValue = TokenMap.detokenizeText(actual);
		assertNotNull(tokenValue);
		assertEquals(input, tokenValue);
	}
	
	public void testDetokenizeCodeBlocks() {
		String input = "{code} blah {code}";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.tokenizeCodeBlocks(page);
		page.setOriginalText(page.getConvertedText());
		tester.detokenizeCodeBlocks(page);
		
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(input, actual);
		
		//noformat
		input = "{noformat} blah {noformat}";
		page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		tester.tokenizeCodeBlocks(page);
		assertFalse(input.equals(page.getConvertedText()));
		page.setOriginalText(page.getConvertedText());
		tester.detokenizeCodeBlocks(page);
		
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(input, actual);

	}
	

	public void testHasSpace() {
		String pagename;
		pagename = "Space:Pagename";
		assertTrue(tester.hasSpace(pagename));
		pagename = "Space:Pagename:illegal";
		assertTrue(tester.hasSpace(pagename));
		pagename = "Pagename";
		assertFalse(tester.hasSpace(pagename));
	}
	
	public void testRemoveSpace() {
		String input, expected, actual;
		input = "Space:Pagename";
		String space = "Space:";
		expected = "Pagename";
		actual = tester.removeSpace(input, space);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Space:Pagename:illegal";
		expected = "Pagename:illegal";
		actual = tester.removeSpace(input, space);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testIdentifySpace() {
		String input, expected, actual;
		input = "Space:Pagename";
		expected = "Space:";
		actual = tester.identifySpace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Space:Pagename:illegal";
		actual = tester.identifySpace(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testAllowAtSymbolForSpecialLinkingSyntax() {
		String input, expected, actual;
		Properties props = new Properties();
		props.setProperty(IllegalLinkNameConverter.ALLOW_AT_IN_LINKS_KEY, "true");
		tester.setProperties(props);

		input = "[query@google]";
		expected = input;
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[query@google;illegalchar]";
		expected = "[query@google.illegalchar]";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testAllowAtSymbolForSpecialLinkingSyntax2() {
		String input, expected, actual;
		Properties props = new Properties();
		props.setProperty(IllegalLinkNameConverter.ALLOW_AT_IN_LINKS_KEY, "false");
		tester.setProperties(props);
		input = "[query@google]";
		expected = "[queryatgoogle]";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAllowTildeSymbolForSpecialLinkingSyntax() {
		String input, expected, actual;
		Properties props = new Properties();
		props.setProperty(IllegalLinkNameConverter.ALLOW_TILDE_IN_LINKS_KEY, "true");
		tester.setProperties(props);

		input = "[~user]";
		expected = input;
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "[Alias|~username;illegalchar.foo.bar]";
		expected = "[Alias|~username.illegalchar.foo.bar]";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testAllowTildeSymbolForSpecialLinkingSyntax2() {
		String input, expected, actual;
		Properties props = new Properties();
		props.setProperty(IllegalLinkNameConverter.ALLOW_TILDE_IN_LINKS_KEY, "false");
		tester.setProperties(props);
		input = "[~user]";
		expected = "[_user]";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testSpacesWithDollarSigns() {
		String input, expected, actual;
		/* This is actually templating code. Xwiki's fault. 
		 * Not sure what I'm _supposed_ to do with it. 
		 * I'll settle for not causing illegal handling to barf. */
		input = "[$bentrydocname|${bentrydoc:fullName}] <em>- 1 new comment</em>\n" + 
				""; 
		expected = "[$bentrydocname|${bentrydoc:fullName)] <em>- 1 new comment</em>\n" + 
				"";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testQuestionMarkProblem() {
		String input, expected, actual;
		input = "[foo|http://foo.com/foo.dll?bar=gah]";
		expected = "[foo|http://foo.com/foo.dll?bar=gah]";
		actual = getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
