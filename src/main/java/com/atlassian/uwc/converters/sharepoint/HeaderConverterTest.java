package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;

public class HeaderConverterTest extends TestCase {

	HeaderConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new HeaderConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertHeaders1() { //simple
		String input = "<html><font size=\"7\">Testing</font></html>";
		String expected = "<html>h1. Testing</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders2() { //simple but spanning two lines
		String input = "<html><font size=\"5\">{color:#8b0000}\n" + 
				"Colored text{color}</font>\n</html>";
		String expected = "<html>h3. {color:#8b0000}Colored text{color}\n</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders3() { //spans too many lines contextually, so strip out
		String input = "<html><font size=\"7\">{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
				"\n" + 
				"It\'s my\n" + 
				"*party *\n" + 
				"\n" + 
				"and I\'ll\n" + 
				"_cry _\n" + 
				"\n" + 
				"if I want to.{color}{panel}\n" + 
				"</font>\n</html>";
		String expected = "<html>" +
				"{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
				"\n" + 
				"It\'s my\n" + 
				"*party *\n" + 
				"\n" + 
				"and I\'ll\n" +  
				"_cry _\n" + 
				"\n" + 
				"if I want to.{color}{panel}\n\n" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders4() { //spans too many changes, so strip out
		String input = "<html>" +
				"<font size=\"7\">{color:#8b0000}\n" + 
				"Cry <font size=\"4\">\n" + 
				"if I want to</font>\n" + 
				"{color}</font>\n" +
				"</html>";
		String expected = "<html>" +
				"{color:#8b0000}\n" + 
				"Cry \n" + 
				"if I want to\n" + 
				"{color}\n" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders5() { //dealing with extra attributes
		String input = "<html>" +
				"<font size=\"4\" face=\"Times New Roman\">\n" + 
				"123!</font>\n</html>";
		String expected = "<html>h4. 123!\n</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders6() { //unnecessary headers
		String input = "<html>" +
				"<font size=\"7\">{color:#8b0000}\n" + 
				"<font size=\"4\" face=\"Times New Roman\">\n" + 
				"123!\n" + 
				"{color}</font>\n</font>\n" +
				"</html>";
		String expected = "<html>\n" +
				"h4. {color:#8b0000}123!{color}\n\n" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testConvertHeaders8() { //headers and panels don't mix
		String input = "<html>" +
				"<font size=\"7\">{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
				"abcdef" + 
				"{color}{panel}</font>" +
				"</html>";
		String expected = "<html>" +
				"{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
				"abcdef" + 
				"{color}{panel}" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders9() { //normal header syntax (could happen) - <h1> etc
		String input = "<html>" +
				"<h2>lalala</h2>" +
				"</html>";
		String expected = "<html>" +
				"h2. lalala" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders10() {
		String input = "<html>\n" +
				"Some text\n" + 
				"<font size=\"5\"><font size=\"2\">\n" + 
				"  {anchor:Lorem Ipsum 2} (anchor here)\n" + 
				" \n" + 
				" </font></font><font size=\"5\"><font size=\"2\"><font size=\"7\">Lorem Ipsum 2</font>\n" + 
				" </font></font>\n" + 
				"</html>";
		String expected = "<html>\n" +
				"Some text\n\n" +
				"  {anchor:Lorem Ipsum 2} " + 
				"(anchor here)\n" + 
				" \n" + 
				"h1. Lorem Ipsum 2\n" + 
				" \n" + 
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders11() {
		String input = "<html>" +
				"<font size=\"5\"><font size=\"2\">\n" + 
				"{anchor:Lorem Ipsum 2}(anchor here)\n" + 
				"\n" + 
				"</font></font><font size=\"5\"><font size=\"2\"><font size=\"7\">Lorem Ipsum 2</font>\n" + 
				"</font></font>\n" +
				"</html>"; 
		String expected = "<html>" +
				"h6. {anchor:Lorem Ipsum 2}(anchor here)\n" + 
				"\n" +
				"h1. Lorem Ipsum 2\n" + 
				"\n" + 
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders12() {
		String input, expected, actual;
		input = "<html>\n" +
				"<h3 class=\"ExternalClassCCAFF3FAA0E340529C5A6A3E0B18184B\">" +
				"<font face=\"Arial\">xxxxxx</font></h3>\n" +
				"</html>";
		expected = "<html>\n" +
				"h3. xxxxxx\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders13() {
		String input = "<html><font size=\"7\">Te<span>s</span>ting</font></html>";
		String expected = "<html>h1. Te<span>s</span>ting</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders14() {
		String input, expected, actual;
		input = "<html>\n" +
				"<ul type=\"disc\"><li class=\"MsoNormal\">{color:black}<font face=\"Arial\" size=\"2\">" +
				"xxxxxx</font>{color}</li><li class=\"MsoNormal\">{color:black}<font face=\"Arial\" s" +
				"ize=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\">{color:black}<font face=\"Ari" +
				"al\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\">{color:black}<font face" +
				"=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\">{color:black}<fon" +
				"t face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\">{color:blac" +
				"k}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\">{colo" +
				"r:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNormal\"" +
				">{color:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=\"MsoNo" +
				"rmal\">{color:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li><li class=" +
				"\"MsoNormal\">{color:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}</li></ul>" +
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" + 
				"* h6. {color:black}xxxxxx{color}\n" +
				"\n" + 
				"</html>";
		actual = tester.convertHeaders(input);
		ListConverter lister = new ListConverter();
		actual = lister.convertLists(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders15() {
		String input, expected, actual;
		input = "<html>\n" +
					"_{color:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}_\n" + 
				"</html>";
		expected = "<html>\n" +
				"h6. _{color:black}xxxxxx{color}_\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders16() {
		String input, expected, actual;
		input = "<html>*<font size=\"4\">Unorderedlist</font>*\n" + 
				"*<font size=\"4\">Orderedlist</font>*\n" +
				"" +
				"</html>";
		expected = "<html>h4. *Unorderedlist*\n" + 
				"h4. *Orderedlist*\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>*<font size=\"4\">Unorderedlist</font>*\n" + 
				"<ol><li>abc</li></ol>*<font size=\"4\">Both list</font>*\n" + 
				"</html>";
		expected = "<html>h4. *Unorderedlist*\n" + 
				"<ol><li>abc</li></ol>\n" +
				"h4. *Both list*\n" +
				"</html>";
				actual = tester.convertHeaders(input);
				assertNotNull(actual);
				assertEquals(expected, actual);
	}
	
	public void testConvertHeaders17() {
		String input, expected, actual;
		input = "<html>\n" + 
				"<font face=\"Arial\" size=\"6\">*strong header*</font>" +
				"*<font face=\"Arial\" size=\"6\">strong header</font>*" +
				"<font face=\"Arial\" size=\"6\">_em header_</font>" +
				"_<font face=\"Arial\" size=\"6\">em header</font>_" +
				"<font face=\"Arial\" size=\"6\">+u header+</font>" +
				"+<font face=\"Arial\" size=\"6\">u header</font>+" +
				"<font face=\"Arial\" size=\"6\">*b header*</font>" +
				"*<font face=\"Arial\" size=\"6\">b header</font>*" +
				"<font face=\"Arial\" size=\"6\">_i header_</font>" +
				"_<font face=\"Arial\" size=\"6\">i header</font>_\n" + 
				"</html>";
		expected = "<html>\n" + 
				"h2. *strong header*\n" +
				"h2. *strong header*\n" +
				"h2. _em header_\n" +
				"h2. _em header_\n" +
				"h2. +u header+\n" +
				"h2. +u header+\n" +
				"h2. *b header*\n" +
				"h2. *b header*\n" +
				"h2. _i header_\n" +
				"h2. _i header_\n" + 
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders18() {
		String input, expected, actual;
		input = "<html>\n" +
				"<font size=\"4\"/>*<font size=\"4\">Other Syntax</font>*\n" +
				"</html>";
		expected = "<html>\n" +
				"h4. *Other Syntax*\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders19() {
		String input, expected, actual;
		input = "<html>\n" +
				"<font size=\"1\">" +
				"+<font size=\"2\"><font face=\"Times New Roman\">xxxxxx</font></font>+" +
				"</font>\n" +
				"</html>";
		expected = "<html>\n" +
				"h6. +xxxxxx+\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders20() {
		String input, expected, actual;
		input = "<html>xxxxxx\n" + 
				"xxxxxx[xxxxxx|/xxxxxx]<h3>xxxxxx</h3>\n" + 
				"</html>";
		expected = "<html>" +
				"xxxxxx\n" + 
				"xxxxxx[xxxxxx|/xxxxxx]\n" + 
				"h3. xxxxxx\n" + 
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertHeaders21() {
		String input, expected, actual;
		input = "<html>\n" +
				"<font size=\"1\"><font face=\"Times New Roman\"><span><font size=\"2\">(1)</font> <span/></span><font size=\"2\">xxxxxx</font></font></font>\n" + 
				"\n" + 
				"</html>";
		expected = "<html>\n" + 
				"<span>h6. (1) <span/></span>xxxxxx\n" + 
				"\n" + 
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders22() {
		String input, expected, actual;
		input = "<html>\n" +
				"*<span><font face=\"Trebuchet MS\" size=\"2\">Bonver Contacts</font></span>*\n" +
				"</html>";
		expected = "<html>\n" +
				"h6. *<span>Bonver Contacts</span>*\n" +
				"</html>";
		actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNeedsNLBefore() {
		String input, expected, actual;
		input = "<html>xxxxxx\n" + 
			"xxxxxx[xxxxxx|/xxxxxx]<h3>xxxxxx</h3>\n" + 
			"</html>";
		Element root = tester.getRootElement(input, false);
		Element el = root.element("h3");
		assertTrue(tester.needsNLBefore(el));
		assertFalse(tester.needsNLAfter(el));
		
		input = "<html>xxxxxx\n" + 
			"xxxxxx[xxxxxx|/xxxxxx]\n" +
			"<h3>xxxxxx</h3>\n" + 
			"</html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("h3");
		assertFalse(tester.needsNLBefore(el));
		assertFalse(tester.needsNLAfter(el));
		
		input = "<html>xxxxxx\n" + 
			"xxxxxx[xxxxxx|/xxxxxx]\n" +
			"\n" +
			"<h3>xxxxxx</h3>Stuff" + 
			"</html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("h3");
		assertFalse(tester.needsNLBefore(el));
		assertTrue(tester.needsNLAfter(el));
			
		input = "<html><h3>xxxxxx</h3>" + 
			"</html>";
		root = el = null;
		root = tester.getRootElement(input, false);
		el = root.element("h3");
		assertFalse(tester.needsNLBefore(el));
		assertFalse(tester.needsNLAfter(el));
	}

	public void testSwitchStyleAndFontTags() {
		String input, expected, actual;
		input = "*<font face=\"Arial\" size=\"6\">strong header</font>*";
		expected = "<font face=\"Arial\" size=\"6\">*strong header*</font>";
		actual = tester.switchStyleAndFontPositions(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<font face=\"Arial\" size=\"6\">*strong header*</font>";
		expected = input;
		actual = tester.switchStyleAndFontPositions(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "_<font face=\"Arial\" size=\"6\">em header</font>_";
		expected = "<font face=\"Arial\" size=\"6\">_em header_</font>";
		actual = tester.switchStyleAndFontPositions(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "+<font face=\"Arial\" size=\"6\">u header</font>+";
		expected = "<font face=\"Arial\" size=\"6\">+u header+</font>";
		actual = tester.switchStyleAndFontPositions(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html>\n" + 
				"<font face=\"Arial\" size=\"6\">*strong header*</font>" +
				"*<font face=\"Arial\" size=\"6\">strong header</font>*" +
				"<font face=\"Arial\" size=\"6\">_em header_</font>" +
				"_<font face=\"Arial\" size=\"6\">em header</font>_" +
				"<font face=\"Arial\" size=\"6\">+u header+</font>" +
				"+<font face=\"Arial\" size=\"6\">u header</font>+" +
				"<font face=\"Arial\" size=\"6\">*b header*</font>" +
				"*<font face=\"Arial\" size=\"6\">b header</font>*" +
				"<font face=\"Arial\" size=\"6\">_i header_</font>" +
				"_<font face=\"Arial\" size=\"6\">i header</font>_\n" + 
		"</html>";
		expected = "<html>\n" + 
				"<font face=\"Arial\" size=\"6\">*strong header*</font>" +
				"<font face=\"Arial\" size=\"6\">*strong header*</font>" +
				"<font face=\"Arial\" size=\"6\">_em header_</font>" +
				"<font face=\"Arial\" size=\"6\">_em header_</font>" +
				"<font face=\"Arial\" size=\"6\">+u header+</font>" +
				"<font face=\"Arial\" size=\"6\">+u header+</font>" +
				"<font face=\"Arial\" size=\"6\">*b header*</font>" +
				"<font face=\"Arial\" size=\"6\">*b header*</font>" +
				"<font face=\"Arial\" size=\"6\">_i header_</font>" +
				"<font face=\"Arial\" size=\"6\">_i header_</font>\n" +
				"</html>";
		actual = tester.switchStyleAndFontPositions(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testTransformSize() {
		String input = "1";
		String expected = "6";
		String actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "2";
		expected = "6";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "3";
		expected = "5";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "4";
		expected = "4";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "5";
		expected = "3";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "6";
		expected = "2";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "7";
		expected = "1";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "8";
		expected = "1";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "10";
		expected = "1";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "100";
		expected = "1";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "0";
		expected = "6";
		actual = tester.transformSize(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "-1";
		actual = tester.transformSize(input);
		assertNull(actual);
		
		input = "a";
		actual = tester.transformSize(input);
		assertNull(actual);
		
		input = "";
		actual = tester.transformSize(input);
		assertNull(actual);
		
		input = null;
		actual = tester.transformSize(input);
		assertNull(actual);
		
	}
	
	public void testShouldTransform() {
		//one line
		String input = "<font size=\"7\">Testing</font>";
		Element el = tester.getRootElement(input, false);
		assertTrue(tester.shouldTransform(el));
		
		//two lines but should be oneline
		el = null;
		input = "<font size=\"5\">{color:#8b0000}\n" + 
			"Colored text{color}</font>\n";
		el = tester.getRootElement(input, false);
		assertTrue(tester.shouldTransform(el));
		
		//many lines
		input = "<font size=\"7\">{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
		"\n" + 
		"It\'s my\n" + 
		"*party *\n" + 
		"\n" + 
		"and I\'ll\n" + 
		"_cry _\n" + 
		"\n" + 
		"if I want to.{color}{panel}\n" + 
		"</font>";
		el = null;
		el = tester.getRootElement(input, false);
		assertFalse(tester.shouldTransform(el));
		
		//different headers within the same line, so strip out
		input = "<font size=\"7\" color=\"#8b0000\">\n" + 
		"Cry <font size=\"4\">\n" + 
		"if I want to</font>\n" + 
		"</font>\n";
		el = null;
		el = tester.getRootElement(input, false);
		assertFalse(tester.shouldTransform(el));
		Element el4 = el.element("font");
		assertNotNull(el4);
		assertFalse(el == el4);
		assertFalse(tester.shouldTransform(el4));

		//unnecessary header
		input = "<font size=\"7\">{color:#8b0000}\n" + 
			"<font size=\"4\" face=\"Times New Roman\">\n" + 
			"123!\n" + 
			"{color}</font>\n</font>";
		el = null;
		el = tester.getRootElement(input, false);
		assertFalse(tester.shouldTransform(el));
		
		//panels. So, strip
		input = "<font size=\"7\">{panel:bgColor=#ffc0cb}{color:#8b0000}\n" + 
			"abcdef" + 
			"{color}{panel}</font>";
		el = null;
		el = tester.getRootElement(input, false);
		assertFalse(tester.shouldTransform(el));
		
		//extra attributes
		input = "<font face=\"Arial\" size=\"2\">xxxxxx \"xxxxxx\" xxxxxx</font>";
		el = null;
		el = tester.getRootElement(input, false);
		assertTrue(tester.shouldTransform(el));
		
	}


	public void testFixColorTags() {
		String input = "{color:red}\n" +
				"h1. something{color}";
		String expected = "\nh1. {color:red}something{color}";
		String actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "prefix {color:red}\n" +
				"h1. something{color}";
		expected = "prefix \n" +
				"h1. {color:red}something{color}";
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "h1. something";
		expected = input;
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "abcdef";
		expected = input;
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{color:red}\n" +
				"h1. something\n" +
				"{color}";
		expected = "\nh1. {color:red}something{color}";
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "{color:red}\n" +
				"h1. something\n" +
				"\n" +
				"{color}";
		expected = "\nh1. {color:red}something{color}";
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "{color:red}h4. Something{color}\n";
		expected = "h4. {color:red}Something{color}\n";
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//nesting!
		input = "h3. {color}{color:#000000}Let\'s create a [child page|mytestwiki:child page].{color}{color:#8b0000}";
		expected = "{color}\n" +
			"h3. {color:#000000}Let\'s create a [child page|mytestwiki:child page].{color}\n" +
			"{color:#8b0000}";
		actual = tester.fixColorTags(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testNoWS() {
		String input = "<html>*<font size=\"4\">abcdef</font>*</html>";
		String expected = "<html>" +
				"h4. *abcdef*" +
				"</html>";
		String actual = tester.convertHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testNoWsBeforeHeader() {
		String input = "<html>\n" +
				"  h1. Testing 123\n" +
				"</html>";
		String expected = "<html>\n" +
		"h1. Testing 123\n" +
		"</html>";
		String actual = tester.noWsBeforeHeaderSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = expected;
		actual = tester.noWsBeforeHeaderSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
}
