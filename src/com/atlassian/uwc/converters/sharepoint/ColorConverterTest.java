package com.atlassian.uwc.converters.sharepoint;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.converters.sharepoint.ColorConverter.Color;

public class ColorConverterTest extends TestCase {

	ColorConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ColorConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertColor() {
		String input, expected, actual;
		//color in span
		input = "<html><span color=\"#ffff00\">Testing</span></html>";
		expected = "<html>{color:#ffff00}Testing{color}</html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//simple color in font
		input = "<font size=\"5\" color=\"#ffff00\">Testing</font>";
		expected = "<font size=\"5\">" +
				"{color:#ffff00}Testing{color}" +
				"</font>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//bgcolor in div
		input = "<html><div align=\"left\" dir=\"ltr\" style=\"background-color:rgb(255, 192, 203)\">\n" + 
				"Testing</div></html>";
		expected = "<html>{panel:bgColor=#ffc0cb}\n" + 
				"Testing{panel}</html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//bgcolor in span
		input = "<html><span style=\"background-color:rgb(255, 240, 200)\">\n" + 
				"Testing span</span>\n</html>";
		expected = "<html>{panel:bgColor=#fff0c8}\n" + 
				"Testing span{panel}\n</html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//color and background at same time
		input = "<html>" +
				"<font size=\"7\" color=\"#8b0000\">" +
				"\n" + 
				"<span style=\"background-color:rgb(50, 0, 255)\">" +
				"\n" + 
				"Testing both</span>" +
				"\n" + 
				"</font>" +
				"</html>";
		expected = "<html>" +
				"<font size=\"7\">" +
				"{panel:bgColor=#3200ff}" +
				"{color:#8b0000}" +
				"\n" +
				"\n" + 
				"Testing both" +
				"{color}" +
				"{panel}\n" +
				"</font></html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//same ones back to back
		input = "<html><span style=\"background-color:rgb(255, 192, 203)\">" +
				"It\'s my " +
				"</span>" +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"*party *" +
				"</span>" +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"and I\'ll </span>" +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"_cry _" +
				"</span>" +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"if I want to.\n" + 
				"</span></html>";
		expected = "<html>{panel:bgColor=#ffc0cb}" +
				"It\'s my *party *and I\'ll _cry _if I want to.\n" +
				"{panel}</html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//nesting
		input = "<html><span style=\"background-color:rgb(255, 192, 203)\">" +
				"Before " +
				"<span style=\"background-color:rgb(0, 0, 255)\">" +
				" middle " +
				"</span>" +
				"after.\n" + 
				"</span>" +
				"</html>";
		expected = "<html>{panel:bgColor=#ffc0cb}" +
				"Before {panel}" +
				"{panel:bgColor=#0000ff}" +
				" middle " +
				"{panel}" +
				"{panel:bgColor=#ffc0cb}" +
				"after.\n" + 
				"{panel}</html>";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testGetColor() {
		String input = "<font size=\"7\" color=\"#8b0000\">\nTesting</font>";
		Element root = tester.getRootElement(input, false);
		Attribute att = root.attribute("color");
		Color actualC = tester.getColor(att);
		assertNotNull(actualC);
		String expected = "#8b0000";
		String actual = actualC.getValue();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHasStyle() {
		String input = "<span style=\"color:red;background-color:white;border:0px solid grey\">Testing</span>";
		Element root = tester.getRootElement(input, false);
		Attribute att = root.attribute("style");
		assertTrue(tester.hasStyleColor(att));
		assertTrue(tester.hasStyleBackground(att));
		
		input = "<span style=\"background-color:white;border:0px solid grey\">Testing</span>";
		root = tester.getRootElement(input, false);
		att = root.attribute("style");
		assertFalse(tester.hasStyleColor(att));
		assertTrue(tester.hasStyleBackground(att));
		
		input = "<span style=\"color:red;border:0px solid grey\">Testing</span>";
		root = tester.getRootElement(input, false);
		att = root.attribute("style");
		assertTrue(tester.hasStyleColor(att));
		assertFalse(tester.hasStyleBackground(att));
		
		input = "<span style=\"border:0px solid grey\">Testing</span>";
		root = tester.getRootElement(input, false);
		att = root.attribute("style");
		assertFalse(tester.hasStyleColor(att));
		assertFalse(tester.hasStyleBackground(att));
		
		
	}
	
	public void testGetStyleColor() {
		String input = "<span style=\"" +
				"color:red;" +
				"background-color:#00ffff;" +
				"border:0px solid grey\">Testing</span>";
		Element root = tester.getRootElement(input, false);
		Attribute att = root.attribute("style");
		ColorConverter.Color expected = (new ColorConverter()). new Color("red");
		ColorConverter.Color actual = tester.getStyleColor(att);
		assertNotNull(actual);
		assertTrue(expected.equals(actual));
		
		ColorConverter.Background expectedBG = (new ColorConverter()). new Background("#00ffff");
		ColorConverter.Background actualBG = tester.getStyleBackground(att);
		assertNotNull(actualBG);
		assertTrue(expectedBG.equals(actualBG));
		
		input = "<span style=\"background-color:rgb(0,255,0);" +
				"border:0px solid grey\">Testing</span>";
		root = tester.getRootElement(input, false);
		att = root.attribute("style");
		expectedBG = (new ColorConverter()). new Background("rgb(0,255,0)");
		actualBG = tester.getStyleBackground(att);
		assertNotNull(actualBG);
		assertTrue(expectedBG.equals(actualBG));
		
	}
	
	public void testShouldSaveElement() {
		String input = "font";
		assertTrue(tester.shouldSaveElement(input));
		input = "a";
		assertFalse(tester.shouldSaveElement(input));
		input = "span";
		assertFalse(tester.shouldSaveElement(input));
		input = "div";
		assertFalse(tester.shouldSaveElement(input));
	}
	
	public void testTransformContentAddChildTextNodes() {
		String input = "<font size=\"5\" color=\"white\">Testing</font>";
		Element root = tester.getRootElement(input, false);
		ColorConverter.Color color = new ColorConverter().new Color("white");
		ColorConverter.Background bg = null;
		Element el = root;
		tester.transformContentAddChildTextNodes(color, bg, el);
		String expected = 
				"<font size=\"5\" color=\"white\">" +
				"{color:white}" +
				"Testing" +
				"{color}" +
				"</font>";
		String actual = tester.toString(root);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<b><font size=\"5\" color=\"white\">Testing <span>123</span></font></b>";
		root = tester.getRootElement(input, false);
		color = new ColorConverter().new Color("white");
		bg = null;
		el = root.element("font");
		tester.transformContentAddChildTextNodes(color, bg, el);
		expected = "<b>" +
				"<font size=\"5\" color=\"white\">" +
				"{color:white}" +
				"Testing <span>123</span>" +
				"{color}" +
				"</font></b>" +
				"";
		actual = tester.toString(root);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testTransformContentReplaceWithText() {
		String input = "<html><span color=\"white\">Testing</span></html>";
		Element root = tester.getRootElement(input, false);
		ColorConverter.Color color = new ColorConverter().new Color("white");
		ColorConverter.Background bg = null;
		Element el = root.element("span");
		tester.transformContentReplaceWithText(color, bg, el);
		String expected = "<html>" +
				"{color:white}" +
				"Testing" +
				"{color}" +
				"</html>";
		String actual = tester.toString(root);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<b><span style=\"background-color:black\">Testing <span>123</span></span></b>";
		root = tester.getRootElement(input, false);
		color = null;
		bg = new ColorConverter().new Background("black");
		el = root.element("span");
		tester.transformContentReplaceWithText(color, bg, el);
		expected = "<b>" +
				"{panel:bgColor=black}" +
				"Testing <span>123</span>" +
				"{panel}" +
				"</b>" +
				"";
		actual = tester.toString(root);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsRgb() {
		String input;
		
		input = "red";
		assertFalse(ColorConverter.isRGB(input));
		
		input = "#ff0000";
		assertFalse(ColorConverter.isRGB(input));
		
		input = "rgb(255, 0, 255)";
		assertTrue(ColorConverter.isRGB(input));
	}

	public void testRgb2Hex() {
		String input, expected, actual;
		input = "rgb(255, 0, 255)";
		expected = "#ff00ff";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "rgb(255,255, 255)";
		expected = "#ffffff";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
				
		input = "rgb(0,0,0)";
		expected = "#000000";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "rgb(50,0,255)";
		expected = "#3200ff";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "rgb(1,2,3)";
		expected = "#010203";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "rgb(012,123,234)";
		expected = "#0c7bea";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "rgb(255, 192, 203)";
		expected = "#ffc0cb";
		actual = ColorConverter.rgb2Hex(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testSwitchColorAndBackground() {
		String input = "<html><span>Testing</span></html>";
		Element root = tester.getRootElement(input, false);
		Element span = root.element("span");
		span.content().add(0,new DefaultText("{color:white}"));
		span.content().add(1,new DefaultText("{panel:bgColor=black}"));
		span.content().add(new DefaultText("{panel}"));
		span.content().add(new DefaultText("{color}"));
		
		tester.switchColorAndBackground(span);
		
		assertNotNull(span);
		assertTrue(span.content().size() == 5);
		Text act0 = (Text) span.content().get(0);
		Text act1 = (Text) span.content().get(1);
		Text act2 = (Text) span.content().get(2);
		Text act3 = (Text) span.content().get(3);
		Text act4 = (Text) span.content().get(4);
		assertNotNull(act0);
		assertNotNull(act1);
		assertNotNull(act2);
		assertNotNull(act3);
		assertNotNull(act4);
		assertEquals("{panel:bgColor=black}", act0.getText());
		assertEquals("{color:white}", act1.getText());
		assertEquals("Testing", act2.getText());
		assertEquals("{color}", act3.getText());
		assertEquals("{panel}", act4.getText());
	}
	
	public void testGetPanelColor() {
		String input = "{panel:bgColor=#ff00ee}Testing";
		String expected = "#ff00ee";
		String actual = tester.getPanelColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{panel}";
		actual = tester.getPanelColor(input);
		assertNull(actual);
		
		input = "something else";
		actual = tester.getPanelColor(input);
		assertNull(actual);
		
		input = "something else {panel:bgColor=red}";
		expected = "red";
		actual = tester.getPanelColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testSample4() {
		String input = "<html>\n" + 
				"\n" + 
				"<font size=\"7\" color=\"#8b0000\">\n" + 
				"<span style=\"background-color:rgb(255, 192, 203)\">\n" + 
				"It\'s my </span>\n" + 
				"*party *\n" + 
				"<span style=\"background-color:rgb(255, 192, 203)\">\n" + 
				"and I\'ll </span>\n" + 
				"_cry _\n" + 
				"<span style=\"background-color:rgb(255, 192, 203)\">\n" + 
				"if I want to.</span>\n" + 
				"</font>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<font size=\"5\" color=\"#8b0000\">\n" + 
				"Cry if I want to</font>\n" + 
				"</li>\n" + 
				"<li>\n" + 
				"<font size=\"7\" color=\"#8b0000\">\n" + 
				"Cry <font size=\"4\">\n" + 
				"if I want to</font>\n" + 
				"</font>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"<blockquote>\n" + 
				"<font size=\"7\" color=\"#8b0000\">\n" + 
				"<font size=\"4\" face=\"Times New Roman\">\n" + 
				"Formatting oddities of SharePoint have not changed!</font>\n" + 
				"\n" + 
				"</font>\n" + 
				"</blockquote>\n" + 
				"<font size=\"7\" color=\"#8b0000\">\n" + 
				"But even so the editor is not bad.\n" + 
				"\n" + 
				"<font size=\"5\" color=\"#000000\">\n" + 
				"Let\'s create a [child page|mytestwiki:child page]\n" + 
				".</font>\n" + 
				"\n" + 
				"</font>\n" + 
				"\n" + 
				"</html>";
		String expected = "<html>\n" + 
				"\n" + 
				"<font size=\"7\">" + 
				"{panel:bgColor=#ffc0cb}" +
				"{color:#8b0000}" + 
				"\n\n" +
				"It\'s my \n" + 
				"*party *\n" + 
				"\n" + 
				"and I\'ll \n" + 
				"_cry _\n" + 
				"\n" + 
				"if I want to." +
				"{color}" +
				"{panel}\n" + 
				"</font>\n" + 
				"\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<font size=\"5\">{color:#8b0000}\n" + 
				"Cry if I want to{color}</font>\n" + 
				"</li>\n" + 
				"<li>\n" + 
				"<font size=\"7\">{color:#8b0000}\n" + 
				"Cry <font size=\"4\">\n" + 
				"if I want to</font>\n" + 
				"{color}</font>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"<blockquote>\n" + 
				"<font size=\"7\">{color:#8b0000}\n" + 
				"<font size=\"4\" face=\"Times New Roman\">\n" + 
				"Formatting oddities of SharePoint have not changed!</font>\n" + 
				"\n" + 
				"{color}</font>\n" + 
				"</blockquote>\n" + 
				"<font size=\"7\">{color:#8b0000}\n" + 
				"But even so the editor is not bad.\n" + 
				"\n" + 
				"<font size=\"5\">{color:#000000}\n" + 
				"Let\'s create a [child page|mytestwiki:child page]\n" + 
				".{color}</font>\n" + 
				"\n" + 
				"{color}</font>\n" + 
				"\n" + 
				"</html>";
		String actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertColor2() {
		String input, expected, actual;
		input = "<html><font size=\"7\" color=\"#8b0000\"><span style=" +
				"\"background-color:rgb(255, 192, 203)\">" +
				"It\'s my</span> *party* " +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"and I\'ll</span> _cry_ " +
				"<span style=\"background-color:rgb(255, 192, 203)\">" +
				"if I want to.</span>\n" + 
				"</font><ul><li><font size=\"5\" color=\"#8b0000\">" +
				"Cry if I want to</font></li><li><font size=\"7\" color=\"#8b0000\">" +
				"Cry <font size=\"4\">if I want to</font></font></li><li/></ul>" +
				"<blockquote><font size=\"7\" color=\"#8b0000\"><font size=\"4\" " +
				"face=\"Times New Roman\">Formatting oddities of SharePoint have not changed!</font>\n" + 
				"\n" + 
				"</font></blockquote><font size=\"7\" color=\"#8b0000\">But even so the editor is not bad.\n" + 
				"\n" + 
				"<font size=\"5\" color=\"#000000\">Let\'s create a [child page|mytestwiki:child page].</font>\n" + 
				"\n" + 
				"</font></html>\n" + 
				"";
		expected = "<html>" +
				"<font size=\"7\">" +
				"{panel:bgColor=#ffc0cb}{color:#8b0000}" +
				"It\'s my *party* and I\'ll _cry_ if I want to." +
				"{color}{panel}\n" + 
				"</font>" +
				"<ul>" +
					"<li><font size=\"5\">" +
						"{color:#8b0000}Cry if I want to{color}" +
					"</font></li>" +
					"<li><font size=\"7\">" +
						"{color:#8b0000}Cry <font size=\"4\">if I want to</font>{color}" +
					"</font></li>" +
					"<li/>" +
				"</ul>" +
				"<blockquote>" +
					"<font size=\"7\">" +
						"{color:#8b0000}" +
						"<font size=\"4\" face=\"Times New Roman\">" +
							"Formatting oddities of SharePoint have not changed!" +
						"</font>\n" + 
						"\n" + 
						"{color}" +
					"</font>" +
				"</blockquote>" +
				"<font size=\"7\">" +
				"{color:#8b0000}But even so the editor is not bad.\n" + 
				"\n" + 
				"<font size=\"5\">" +
				"{color:#000000}Let\'s create a [child page|mytestwiki:child page]." +
				"{color}" +
				"</font>\n" + 
				"\n" + 
				"{color}" +
				"</font></html>" + 
				"";
		actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertColorHeaderList() {
		String input, expected, actual;
		QuoteWSConverter quoter = new QuoteWSConverter();
		HeaderConverter header = new HeaderConverter();
		ListConverter lister = new ListConverter();
		
		input = "<html>" +
			"<font size=\"7\" color=\"#8b0000\">But even so the editor is not bad.\n" + 
			"\n" + 
			"<font size=\"5\" color=\"#000000\">Let\'s create a [child page|mytestwiki:child page].</font>\n" + 
			"\n" + 
			"</font></html>\n" + 
			"";
		expected = "<html>" +"{color:#8b0000}" + 
			"But even so the editor is not bad.\n" +
			"\n" + 
			"{color}\n" + 
			"h3. {color:#000000}Let\'s create a [child page|mytestwiki:child page].{color}\n" + 
			"{color:#8b0000}\n" +
			"\n" + 
			"{color}</html>";
		actual = tester.convertColor(input);
		actual = quoter.convertQuoteWS(actual);
		actual = header.convertHeaders(actual);
		actual = quoter.convertQuoteWS(actual);

		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><font size=\"7\" color=\"#8b0000\"><span style=\"background-color:rgb(255, 192, 203)\">It\'s my</span> *party* <span style=\"background-color:rgb(255, 192, 203)\">and I\'ll</span> _cry_ <span style=\"background-color:rgb(255, 192, 203)\">if I want to.</span>\n" + 
				"</font><ul><li><font size=\"5\" color=\"#8b0000\">Cry if I want to</font></li><li><font size=\"7\" color=\"#8b0000\">Cry <font size=\"4\">if I want to</font></font></li><li/></ul>{quote}<font size=\"7\" color=\"#8b0000\"><font size=\"4\" face=\"Times New Roman\">Formatting oddities of SharePoint have not changed!</font>\n" + 
				"\n" + 
				"</font>{quote}<font size=\"7\" color=\"#8b0000\">But even so the editor is not bad.\n" + 
				"\n" + 
				"<font size=\"5\" color=\"#000000\">Let\'s create a [child page|mytestwiki:child page].</font>\n" + 
				"\n" + 
				"</font></html>\n" + 
				"";
		expected = "<html>{panel:bgColor=#ffc0cb}{color:#8b0000}It\'s my *party* and I\'ll _cry_ if I want to.{color}{panel}\n" + 
				"\n" + 
				"* h3. {color:#8b0000}Cry if I want to{color}\n" + 
				"* {color:#8b0000}Cry if I want to{color}\n" + 
				"\n" +
				"\n" + 
				"{quote}\n" +
				"h4. {color:#8b0000}Formatting oddities of SharePoint have not changed!" +
				"\n" +
				"\n" +
				"{color}\n" + 
				"{quote}\n" + 
				"{color:#8b0000}" + 
				"But even so the editor is not bad.\n" +
				"\n" +
				"{color}\n" + 
				"h3. {color:#000000}Let\'s create a [child page|mytestwiki:child page].{color}\n" + 
				"{color:#8b0000}\n" +
				"\n" +
				"{color}</html>";
		
		actual = tester.convertColor(input);
		actual = quoter.convertQuoteWS(actual);
		actual = header.convertHeaders(actual);
		actual = lister.convertLists(actual);
		actual = quoter.convertQuoteWS(actual);
		
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"<span style=\"color:black\"><font face=\"Arial\" size=\"2\">xxxxxx \"xxxxxx\" xxxxxx</font></span>\n" + 
				"\n" + 
				"<span style=\"color:black\"><font face=\"Arial\" size=\"2\">xxxxxx</font></span>\n" + 
				"\n" + 
				"</html>";
		expected = "<html>\n" +
				"h6. {color:black}xxxxxx \"xxxxxx\" xxxxxx{color}\n" + 
				"\n" + 
				"h6. {color:black}xxxxxx{color}\n" + 
				"\n" + 
				"</html>";
		actual = tester.convertColor(input);
		actual = quoter.convertQuoteWS(actual);
		actual = header.convertHeaders(actual);
		actual = quoter.convertQuoteWS(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDisallowNestingColor() {
		String input = "<html>Before\n" +
				"{color:red}Red\n" +
				"\n" +
				"Mid\n" +
				"{color:blue}Blue{color}\n" +
				"\n" +
				"After\n" +
				"{color}</html>";
		String expected = "<html>Before\n" +
				"{color:red}Red\n" +
				"\n" +
				"Mid\n" +
				"{color}{color:blue}Blue{color}{color:red}\n" +
				"\n" +
				"After\n" +
				"{color}</html>";
		Element root = tester.getRootElement(input, false);
		tester.disallowNestingColor(root);
		String actual = root.asXML();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontRemoveTableTags() {
		String input = "<html>\n" +
				"<table><tbody>" +
				"<tr>" +
				"<td rowspan=\"1\" colspan=\"1\">*r1c1*</td>" +
				"<td rowspan=\"1\" colspan=\"1\"><font color=\"#00ff00\">r1c2</font></td>" +
				"</tr>" +
				"<tr>" +
				"<td colspan=\"1\" style=\"background-color:rgb(255, 215, 0)\" rowspan=\"1\">Œær2c1</td>" +
				"<td colspan=\"1\" rowspan=\"1\"><font size=\"6\">r2c2</font>Œæ</td>" +
				"</tr>" +
				"</tbody></table>\n" + 
				"</html>";
		String expected = "<html>\n" +
				"<table><tbody>" +
				"<tr>" +
				"<td rowspan=\"1\" colspan=\"1\">*r1c1*</td>" +
				"<td rowspan=\"1\" colspan=\"1\"><font>{color:#00ff00}r1c2{color}</font></td>" +
				"</tr>" +
				"<tr>" +
				"<td colspan=\"1\" rowspan=\"1\">" +
				"{panel:bgColor=#ffd700}Œær2c1{panel}</td>" +
				"<td colspan=\"1\" rowspan=\"1\"><font size=\"6\">r2c2</font>Œæ</td>" +
				"</tr>" +
				"</tbody></table>\n" + 
				"</html>";
		String actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testComplexListItems() {
		String input = "<html>\n" +
				"<ul type=\"disc\">" +
					"<li class=\"MsoNormal\" style=\"margin:0in 0in 0pt;color:black;tab-stops:list .5in\">" +
						"<font face=\"Arial\" size=\"2\">xxxxxx</font>" +
					"</li>" +
				"</ul>\n" + 
				"</html>";
		String expected = "<html>\n" +
				"<ul type=\"disc\">" +
					"<li class=\"MsoNormal\">" +
						"{color:black}<font face=\"Arial\" size=\"2\">xxxxxx</font>{color}" +
					"</li>" +
				"</ul>\n" + 
				"</html>";
		String actual = tester.convertColor(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
