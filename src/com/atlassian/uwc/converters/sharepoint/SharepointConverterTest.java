package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.ContentListFacade;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

public class SharepointConverterTest extends TestCase {

	SharepointConverter tester = null;
	protected void setUp() throws Exception {
		tester = new BoldConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testSimpleTransform() {
		String input = "<html><a att=\"val\">blah</a><b>hrm<p/></b>tada</html>";
		Element root = tester.getRootElement(input, false);
		String search = "a";
		String replace = "!!";
		String expected = "<html>!!blah!!<b>hrm<p/></b>tada</html>";
		Element actualEl = tester.simpleTransform(root, search, replace);
		assertNotNull(actualEl);
		String actual = tester.toString(actualEl);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		root = tester.getRootElement(input, false);
		search = "b";
		replace = "-";
		expected = "<html><a att=\"val\">blah</a>-hrm<p/>-tada</html>";
		actualEl = tester.simpleTransform(root, search, replace);
		assertNotNull(actualEl);
		actual = tester.toString(actualEl);
		assertNotNull(actual);
		assertEquals(expected, actual); 
		
		root = tester.getRootElement(input, false);
		search = "p";
		replace = "HERE";
		expected = "<html><a att=\"val\">blah</a><b>hrmHERE</b>tada</html>";
		actualEl = tester.simpleTransform(root, search, replace);
		assertNotNull(actualEl);
		actual = tester.toString(actualEl);
		assertNotNull(actual);
		assertEquals(expected, actual); 
		
	}

	public void testToString() {
		String input = "<html><b>bold</b><em>emph</em>tada</html>";
		Element in = tester.getRootElement(input, false);
		String expected = input;
		String actual = tester.toString(in);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testTransformContent() {
		String replace = "0";
		//first is Text, last is Text
		List content = new Vector();
		Text testText0 = new DefaultText("text");
		Text testText1 = new DefaultText("text");
		content.add(testText0);
		content.add(testText1);
		Element el = new DefaultElement("el");
		el.setContent(content);
		tester.surroundWithReplace(replace, el);
		assertNotNull(content);
		assertTrue(content.size() == 2);
		assertTrue(content.get(0) instanceof Text);
		assertTrue(content.get(1) instanceof Text);
		Text t0 = (Text) content.get(0);
		Text t1 = (Text) content.get(1);
		assertEquals("0text", t0.getText());
		assertEquals("text0", t1.getText());
		
//		first is Text, last is Element
		testText0 = new DefaultText("text");
		Element testEl1 = new DefaultElement("el");
		content.clear();
		content.add(testText0);
		content.add(testEl1);
		el = new DefaultElement("el");
		el.setContent(content);
		tester.surroundWithReplace(replace, el);
		content = el.content();
		assertNotNull(content);
		assertTrue(content.size() == 3);
		assertTrue(content.get(0) instanceof Text);
		assertTrue(content.get(2) instanceof Text);
		t0 = (Text) content.get(0);
		t1 = (Text) content.get(2);
		assertEquals("0text", t0.getText());
		assertEquals("0", t1.getText());
		
//		first is Element, last is Text
		Element testEl0 = new DefaultElement("el");
		testText1 = new DefaultText("text");
		content.clear();
		content.add(testEl0);
		content.add(testText1);
		el = new DefaultElement("el");
		el.setContent(content);
		tester.surroundWithReplace(replace, el);
		content = el.content();
		assertNotNull(content);
		assertTrue(content.size() == 3);
		assertTrue(content.get(0) instanceof Text);
		assertTrue(content.get(2) instanceof Text);
		t0 = (Text) content.get(0);
		t1 = (Text) content.get(2);
		assertEquals("0", t0.getText());
		assertEquals("text0", t1.getText());
		
//		first is Element, last is Element
		testEl0 = new DefaultElement("el");
		testEl1 = new DefaultElement("el");
		content.clear();
		content.add(testEl0);
		content.add(testEl1);
		el = new DefaultElement("el");
		el.setContent(content);
		tester.surroundWithReplace(replace, el);
		content = el.content();
		assertNotNull(content);
		assertTrue(content.size() == 4);
		assertTrue(content.get(0) instanceof Text);
		assertTrue(content.get(3) instanceof Text);
		t0 = (Text) content.get(0);
		t1 = (Text) content.get(3);
		assertEquals("0", t0.getText());
		assertEquals("0", t1.getText());
		
		
		//represents empty element
		content.clear();
		el = new DefaultElement("p");
		el.setContent(content);
		tester.surroundWithReplace(replace, el);
		content = el.content();
		assertNotNull(content);
		assertTrue(content.size() == 1);
		assertTrue(content.get(0) instanceof Text);
		t0 = (Text) content.get(0);
		assertEquals("0", t0.getText());
	}
	
	public void testSimpleTransform_HandleWS() {
		String input = "<html><a>\n" +
				"<b/>\n" +
				"</a>\n" +
				"</html>";
		Element root = tester.getRootElement(input, false);
		String search = "a";
		String replace = "!!";
		String expected = "<html>" +
				"!!<b/>!!\n" +
				"</html>";
		Element actualEl = tester.simpleTransform(root, search, replace);
		assertNotNull(actualEl);
		String actual = tester.toString(actualEl);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testTransformContentSimpleReplace() {
		String input = "<p>text1<br/>text2</p>";
		String replace = "HERE";
		String search = "br";
		Element root = tester.getRootElement(input);
		boolean transformAll = true;
		boolean bothSides = false;
		tester.transform(root, search, replace, bothSides, transformAll);
		String actual = tester.toString(root);
		actual = CleanConverter.cleanJTidyExtras(actual);
		String expected = "<html>\n" + 
				"\n" + 
				"\n" + 
				"<p>text1HERE\n" + 
				"text2</p>\n" + 
				"\n" + 
				"</html>";
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCombinationConversions1() {
		String input, expected, actual;
		input = "<html>\n" +
				"<STRONG><U><FONT size=2>Testing</FONT></U><FONT face=\"Trebuchet MS\" size=2>&nbsp;</FONT></STRONG>\n" +
				"</html>"; 
		expected = "<html>" +
				"h6. *+Testing+*" +
				"</html>";
		
		CleanConverter cleaner = new CleanConverter();
		InlineConverter inline = new InlineConverter();
		BoldConverter bold = new BoldConverter();
		UnderlineConverter under = new UnderlineConverter();
		ColorConverter color = new ColorConverter();
		HeaderConverter header = new HeaderConverter();
		
		actual = cleaner.clean(input);
		actual = inline.convertInline(actual);
		actual = bold.convertBold(actual);
		actual = under.convertUnderline(actual);
		actual = color.convertColor(actual);
		actual = header.convertHeaders(actual);
		
		assertNotNull(actual);
		assertEquals(expected, actual); 
	}
}
