package com.atlassian.uwc.converters.jspwiki;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class StyleConverterTest extends TestCase {

	StyleConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new StyleConverter();
		tester.init();
	}

	public void testConvertStyles() {
		String input = "This is for testing CSS style conversions\n" +
				"\n" +
				"%%sub This should be subscript%% not sub\n" +
				"\n" +
				"normal\n" +
				"\n" +
				"%%sup This should be superscript %% not super\n" +
				"\n" +
				"normal\n";
		String expected = "This is for testing CSS style conversions\n" +
				"\n" +
				"~This should be subscript~ not sub\n" +
				"\n" +
				"normal\n" +
				"\n" +
				"^This should be superscript^ not super\n" +
				"\n" +
				"normal\n";	
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testSub() {
		String input = "%%sub This should be subscript%% not sub";
		String expected = "~This should be subscript~ not sub";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testSup() {
		String input = "%%sup This should be superscript %% not super";
		String expected = "^This should be superscript^ not super";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testStrike() {
		String input = "%%strike\n" +
				"This should be strikethroughed\n" +
				"%%";
		String expected = "-This should be strikethroughed-";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testComment() {
		String input = "%%commentbox\n" +
				"floating right margin comment box\n" +
				"%%";
		String expected = "{panel}\n" +
				"floating right margin comment box\n" +		
				"{panel}";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testInfo() {
		String input = "%%information\n" +
				"This is an info box\n" +
				"%%\n";
		String expected = "{info}\n" +
			"This is an info box\n" +
			"{info}\n";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);


	}

	public void testWarning() {
		String input = "%%warning\n" +
				"This is a warning box\n" +
				"%%";
		String expected = "{note}\n" +
				"This is a warning box\n" +
				"{note}";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
 
	}
	public void testError() {
		String input = "%%error\n" +
				"Error box\n" +
				"%%";
		String expected = "{warning}\n" +
				"Error box\n" +
				"{warning}";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testInline() {
		String input = "%%warning what about inline? %%";
		String expected = "{note}\n" +
				"what about inline?\n" +
				"{note}";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testCss() {
		String input = "%%( font-name:Times New Roman; color:blue; background-color:#999999; border:2px dashed #999900;)\n" +
				"This panel could be weird\n" +
				"%%";
		String expected = "{panel:bgColor=#999999|borderWidth=2px|borderStyle=dashed|borderColor=#999900}\n" +
				"{color:blue}" +
				"This panel could be weird" +
				"{color}\n" +
				"{panel}" ;	
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testNotSupporting() {
		String input ="%%small\n" +
				"Something we're not supporting\n" +
				"%%";
		String expected = "{panel}\n" +
				"Something we're not supporting\n" +
				"----\n" +
				"Jspwiki style: small\n" +
				"{panel}";
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "%%sortable\n" +
				"Something else we're not supporting\n" +
				"%%\n";
		expected = "{panel}\n" +
				"Something else we're not supporting\n" +
				"----\n" +
				"Jspwiki style: sortable\n" + 
				"{panel}\n";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testGetStyle() {
		String input = "sub";
		StyleConverter.JspwikiStyle expected = StyleConverter.JspwikiStyle.SUBSCRIPT;
		StyleConverter.JspwikiStyle actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "sup";
		expected = StyleConverter.JspwikiStyle.SUPERSCRIPT;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "strike";
		expected = StyleConverter.JspwikiStyle.STRIKETHROUGH;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "commentbox";
		expected = StyleConverter.JspwikiStyle.COMMENTBOX;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "information";
		expected = StyleConverter.JspwikiStyle.INFOBOX;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "warning";
		expected = StyleConverter.JspwikiStyle.WARNINGBOX;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "error";
		expected = StyleConverter.JspwikiStyle.ERRORBOX;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "( font-name:Times New Roman; background-color:blue; )";
		expected = StyleConverter.JspwikiStyle.INLINESTYLE;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "small";
		expected = StyleConverter.JspwikiStyle.UNKNOWN;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);
		
		input = "sortable";
		expected = StyleConverter.JspwikiStyle.UNKNOWN;
		actual = tester.getStyle(input);
		assertNotNull(actual);
		assertTrue(actual.toString(), expected == actual);

	}
	
	
	public void testGetType() {
		for (StyleConverter.JspwikiStyle style : StyleConverter.JspwikiStyle.values()) {
			StyleConverter.ConversionType actual = tester.getType(style);
			StyleConverter.ConversionType expected = null;
			switch(style) {
			case SUBSCRIPT:
			case SUPERSCRIPT:
			case STRIKETHROUGH:
				expected = StyleConverter.ConversionType.INLINE;
				break;
			case COMMENTBOX:
			case INFOBOX:
			case WARNINGBOX:
			case ERRORBOX:
				expected = StyleConverter.ConversionType.MULTILINE;
				break;
			case INLINESTYLE:
			case UNDERLINE:
			case ITALIC:
			case COLOR:
			case UNKNOWN:
				expected = StyleConverter.ConversionType.SPECIAL;
			}
			assertNotNull("actual is null", actual);
			assertNotNull("expected is null", expected);
			assertEquals(expected, actual);
		}
	}
	
	
	public void testGetInline() {
		//valid inline request
		String text = "inner text";
		StyleConverter.JspwikiStyle type = StyleConverter.JspwikiStyle.STRIKETHROUGH;
		String expected = "-" + text + "-";
		String actual = tester.getInline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		type = StyleConverter.JspwikiStyle.SUBSCRIPT;
		expected = "~" + text + "~";
		actual = tester.getInline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		type = StyleConverter.JspwikiStyle.SUPERSCRIPT;
		expected = "^" + text  + "^";
		actual = tester.getInline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
			
		//invalid inline request
		type = StyleConverter.JspwikiStyle.COMMENTBOX;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			String expMessage = "Type " + "COMMENTBOX" + 
			" is not a valid inline type. Use " + "getMultiline()" + 
			" instead.";
			assertEquals(expMessage, message);
		}
		
		type = StyleConverter.JspwikiStyle.INFOBOX;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.WARNINGBOX;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.ERRORBOX;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.INLINESTYLE;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.UNKNOWN;
		try {
			actual = tester.getInline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
	}
	
	public void testGetMultiline() {
		//valid multiline request
		String text = "inner text\nmore text";
		StyleConverter.JspwikiStyle type = StyleConverter.JspwikiStyle.COMMENTBOX;
		String expected = "{panel}\n" + text + "\n{panel}";
		String actual = tester.getMultiline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		type = StyleConverter.JspwikiStyle.INFOBOX;
		expected = "{info}\n" + text + "\n{info}";
		actual = tester.getMultiline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);

		type = StyleConverter.JspwikiStyle.WARNINGBOX;
		expected = "{note}\n" + text + "\n{note}";
		actual = tester.getMultiline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);

		type = StyleConverter.JspwikiStyle.ERRORBOX;
		expected = "{warning}\n" + text + "\n{warning}";
		actual = tester.getMultiline(type, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//invalid multiline request
		type = StyleConverter.JspwikiStyle.SUBSCRIPT;
		try {
			actual = tester.getMultiline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			String expMessage = "Type " + "SUBSCRIPT" + 
			" is not a valid inline type. Use " + "getInline()" + 
			" instead.";
			assertEquals(expMessage, message);
		}
		
		type = StyleConverter.JspwikiStyle.SUPERSCRIPT;
		try {
			actual = tester.getMultiline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.STRIKETHROUGH;
		try {
			actual = tester.getMultiline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.INLINESTYLE;
		try {
			actual = tester.getMultiline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

		type = StyleConverter.JspwikiStyle.UNKNOWN;
		try {
			actual = tester.getMultiline(type, text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

	}
	
	public void testGetSpecial() {
		//valid special request
		String text = "special text";
		String raw = "( color: red; background-color:#555555)";
		StyleConverter.JspwikiStyle type = StyleConverter.JspwikiStyle.INLINESTYLE;
		String expected = "{panel:bgColor=#555555}\n{color:red}" + text + "{color}\n{panel}"; 
		String actual = tester.getSpecial(type, raw, text);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		type = StyleConverter.JspwikiStyle.UNKNOWN;
		raw = "small";
		expected = "{panel}\n" + text + "\n----\nJspwiki style: " + raw + "\n{panel}"; 
		actual = tester.getSpecial(type, raw, text);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//invalid special request
		type = StyleConverter.JspwikiStyle.SUBSCRIPT;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {
			String message = e.getMessage();
			String expMessage = "Type " + "SUBSCRIPT" + 
			" is not a valid inline type. Use " + "getInline()" + 
			" instead.";
			assertEquals(expMessage, message);
		}
		
		type = StyleConverter.JspwikiStyle.SUPERSCRIPT;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.STRIKETHROUGH;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}
		
		type = StyleConverter.JspwikiStyle.COMMENTBOX;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

		type = StyleConverter.JspwikiStyle.INFOBOX;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

		type = StyleConverter.JspwikiStyle.WARNINGBOX;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

		type = StyleConverter.JspwikiStyle.ERRORBOX;
		try {
			actual = tester.getSpecial(type, "", text);
			fail("Should have failed for type: "  + type.toString() + ". actual = " + actual);
		} catch (IllegalArgumentException e) {}

	}
	
	public void testGetMethodName() {
		String input = "INLINE";
		String expected = "getInline()";
		String actual = tester.getMethodName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPanelArgs() {
		//each that will be converted
		String input = "( background-color:#555555)";
		String expected = "bgColor=#555555";
		String actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( border-style: groove )";
		expected = "borderStyle=groove";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( border-width: 3px)";
		expected = "borderWidth=3px";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( border-color: red)";
		expected = "borderColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( border-color: #ff0000)";
		expected = "borderColor=#ff0000";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( border: 2px dashed red;)";
		expected = "borderWidth=2px|borderStyle=dashed|borderColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with or without a semicolon on final one
		input = "( background-color:red;)";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( background-color:red)";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with or without ws
		input = "( background-color:red )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( background-color:red)";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(background-color:red )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(background-color:red)";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( background-color:red;)";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( background-color:red; )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(background-color: red )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(background-color :red )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(background-color: red )";
		expected = "bgColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//one that won't
		input = "(font-weight: bold)";
		expected = "";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//and color
		input = "(color:red)";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(color:#ff0000)";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//several together
		input = "( " +
			"background-color:#999999; " +
			"border:2px dashed #999900;)";
		expected = "bgColor=#999999|borderWidth=2px|borderStyle=dashed|borderColor=#999900";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//border uses different ordering
		input = "( " +
			"background-color:#999999; " +
			"border:dashed 2px #999900;)";
		expected = "bgColor=#999999|borderWidth=2px|borderStyle=dashed|borderColor=#999900";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//border uses different ordering and only words
		input = "( " +
			"background-color:#999999; " +
			"border:dashed thin red)";
		expected = "bgColor=#999999|borderWidth=thin|borderStyle=dashed|borderColor=red";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( " +
			"background-color:#999999; " +
			"border-style:dashed;" +
			"border-width:4px )";
		expected = "bgColor=#999999|borderStyle=dashed|borderWidth=4px";
		actual = tester.convertPanelArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//several with color and non-convertable one
		input = "( font-name:Times New Roman; " +
				"color:blue; " +
				"background-color:#999999; " +
				"border:2px dashed #999900;)";
		expected = "bgColor=#999999|borderWidth=2px|borderStyle=dashed|borderColor=#999900";

	}
	
	public void testHasColor() {
		String input = "( " +
			"background-color:#999999; " +
			"border-style:dashed;" +
			"border-width:4px )";
		boolean expected = false;
		boolean actual = tester.hasColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	//several with color and non-convertable one
		input = "( font-name:Times New Roman; " +
			"color:blue; " +
			"background-color:#999999; " +
			"border:2px dashed #999900;)";
		expected = true;
		actual = tester.hasColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConvertColorArgs() {
		String input = "( color:red )";
		String expected = "red";
		String actual = tester.convertColorArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "(color:#333322;)";
		expected = "#333322";
		actual = tester.convertColorArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( font-name:Times New Roman; " +
			"color:blue; " +
			"background-color:#999999; " +
			"border:2px dashed #999900;)";
		expected = "blue";
		actual = tester.convertColorArgs(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "( " +
			"background-color:#999999; " +
			"border-style:dashed;" +
			"border-width:4px )";
		actual = tester.convertColorArgs(input);
		assertNull(actual);
		
	}
	
	public void testIsWidth() {
		String input = "thin";
		assertTrue(tester.isWidth(input));
		input = "thick";
		assertTrue(tester.isWidth(input));
		input = "medium";
		assertTrue(tester.isWidth(input));
		input = "none";
		assertTrue(tester.isWidth(input));
		input = "1px";
		assertTrue(tester.isWidth(input));
		input = "20px";
		assertTrue(tester.isWidth(input));
		input = "1em";
		assertTrue(tester.isWidth(input));
		input = "1cm";
		assertTrue(tester.isWidth(input));
		input = "1in";
		assertTrue(tester.isWidth(input));
		input = "1pt";
		assertTrue(tester.isWidth(input));
		
		//not a width
		input = "red";
		assertFalse(tester.isWidth(input));
		input = "rgb(0,0,0)";
		assertFalse(tester.isWidth(input));
		input = "#fff";
		assertFalse(tester.isWidth(input));
		input = "#00ff00";
		assertFalse(tester.isWidth(input));
		input = "dashed";
		assertFalse(tester.isWidth(input));
	}
	
	public void testIsStyle() {
		String input = "none";
		assertTrue(tester.isStyle(input));
		input = "hidden";
		assertTrue(tester.isStyle(input));
		input = "dotted";
		assertTrue(tester.isStyle(input));
		input = "dashed";
		assertTrue(tester.isStyle(input));
		input = "solid";
		assertTrue(tester.isStyle(input));
		input = "double";
		assertTrue(tester.isStyle(input));
		input = "groove";
		assertTrue(tester.isStyle(input));
		input = "ridge";
		assertTrue(tester.isStyle(input));
		input = "inset";
		assertTrue(tester.isStyle(input));
		input = "outset";
		assertTrue(tester.isStyle(input));
		
		//not a style
		input = "medium";
		assertFalse(tester.isStyle(input));
		input = "red";
		assertFalse(tester.isStyle(input));
		input = "rgb(0,0,0)";
		assertFalse(tester.isStyle(input));
		input = "#fff";
		assertFalse(tester.isStyle(input));
		input = "#00ff00";
		assertFalse(tester.isStyle(input));
	}
	
	public void testAltUnderline() {
		String input, expected, actual;
		input = "%%(text-decoration: underline) underlined text%%";
		expected = "+underlined text+";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAltItalics() {
		String input, expected, actual;
		input = "%%(font-style: italic)italics text 2%%\n" + 
				"";
		expected = "_italics text 2_\n";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAltColor() {
		String input, expected, actual;
		input = "%%(color: red;)\n" + 
				"foobar%%";
		expected = "{color:red}foobar{color}";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testComboNewAltSyntaxes() {
		String input = "%%( font-name:Times New Roman; color:blue; " +
				"background-color:#999999; border:2px dashed #999900;" +
				"text-decoration:underline)" +
		"This panel could be weird" +
		"%%";
		String expected = "{panel:bgColor=#999999|borderWidth=2px|borderStyle=dashed|borderColor=#999900}\n" +
		"{color:blue}" +
		"+This panel could be weird+" +
		"{color}\n" +
		"{panel}" ;	
		String actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "%%(text-decoration: underline; color: red) gooba%%";
		expected = "{color:red}+gooba+{color}";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testColorPanelHeaderCombo() { //uwc-350
		String input, expected, actual;
		input = "%%( color: black ; background-color : silver )   h1. *THE INFORMATION ON THIS SITE IS RESTRICTED*%%";
		expected = "{panel:bgColor=silver}\n" + 
				"h1. {color:black}*THE INFORMATION ON THIS SITE IS RESTRICTED*{color}\n" + 
				"{panel}";
		actual = tester.convertStyles(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleHeaders() {
		String input, expected, actual;
		input = "{panel:bgColor=silver}\n" + 
				"{color: black}h1. *THE INFORMATION ON THIS SITE IS RESTRICTED*{color}\n" + 
				"{panel}\n" +
				"{panel:bgColor=silver}\n" + 
				"{color: red}Testing\n" +
				"h1. *THE INFORMATION ON THIS SITE IS $10*" +
				"{color}\n" + 
				"{panel}\n" +
				"";
		expected = "{panel:bgColor=silver}\n" + 
				"h1. {color: black}*THE INFORMATION ON THIS SITE IS RESTRICTED*{color}\n" + 
				"{panel}\n" +
				"{panel:bgColor=silver}\n" + 
				"{color: red}Testing\n" +
				"{color}\n" +
				"h1. {color: red}*THE INFORMATION ON THIS SITE IS $10*" +
				"{color}\n" + 
				"{panel}\n" +
				"";
		actual = tester.handleHeaders(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
