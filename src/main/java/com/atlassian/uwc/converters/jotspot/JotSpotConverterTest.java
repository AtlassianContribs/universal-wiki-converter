package com.atlassian.uwc.converters.jotspot;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;


public class JotSpotConverterTest extends TestCase {

	String openingTagAttributes = "[^>]*";
	String itemTag = "<li" + openingTagAttributes + ">";
	String listRegexStr = 
			"((?:(?:<\\/?[ou]l>\r?\n*)+" + 	// list start or end
			"(?:(?:"+ itemTag+ 				// item start
			".*?" + 						// item contents
			"<\\/li>" +						// item end
			"\r?\n*" +						// newlines allowed
			")+))+)" +						// repeat
			"((?:<\\/[ou]l>\r?\n?)+)"; 		// list final end
	String listPlusRegex = "((.*?)" +  listRegexStr + ")"; // pretext + list
	Pattern listChunker = Pattern.compile(listPlusRegex, Pattern.DOTALL);
//	JavaRegexes helper = new JavaRegexes();
	Logger log;
	ListPreprocessor processor = new ListPreprocessor();
	TableConverter tableConverter = new TableConverter();
	ListConverter listConverter = new ListConverter();
	LinkConverter linkConverter = new LinkConverter();
	
	protected void setUp() throws Exception {
		super.setUp();
		log = Logger.getLogger(this.getClass());
		BasicConfigurator.configure();
	}

	/* Start List Section */
	public void testChunkJotspotList() {	
		
		String pre =  "bulleted lists" +
		"<br />";
		String firstList = "<ul>" +
		"<li>item1</li>" +
		"<li>item2" +
		"<ul><li>item2a</li><li>item2b</li></ul></li>" +
		"<li>item3<ul><li>item3a" +
		"<ul><li>item3a1<ul><li>item3a1A</li></ul>" +
		"</li><li>item3a2</li><li>item3a3</li></ul></li></ul></li><li>item4" +
		"<br /></li></ul>";
		String mid = "numbered lists<br />";
		String secondList = "<ol><li>item1</li><li>item2<ol><li>item2a</li><li>item2b</li></ol>" +
		"</li><li>item3<ol><li>item3a<ol><li>item3a1<ol><li>item3a1A</li></ol>" +
		"</li><li>item3a2</li><li>item3a3</li></ol></li></ol></li><li>item4" +
		"<br /></li></ol>";		
		String input = pre + firstList + mid + secondList;
		input = processor.preProcessLists(input);
		Matcher m = listChunker.matcher(input);
		//does the regex work at all?
		assertTrue(m.find());
		//log what happened
		m.reset();
//		log.debug("HERE WE ARE:!!!!");
//		helper.pretextGroupPrinter(m, this.log);
		//test the found pretexts
		m.reset();
		Vector expectedPretexts = new Vector();
		expectedPretexts.add(pre + "\n");
		expectedPretexts.add(mid + "\n");
		testPretexts("Jotspot", m, expectedPretexts);
		//test the found lists
		m.reset();
		Vector expectedLists = new Vector();
		expectedLists.add(processor.preProcessLists(firstList));
		expectedLists.add(processor.preProcessLists(secondList));
		testLists("Jotspot", m, expectedLists);
	}
	

	public void testChunkList() {
		String pre = "PRE\n";
		String firstList = 
		"<ol>" +
		"<li>a</li>" +
		"<li>b</li>" +
		"<ol>" +
		"<li>1</li>" +
		"</ol>" +
		"<li>c</li>" +
		"</ol>\n";
		String mid = 
		"MID\n";
		String secondList =
		"<ul>" +
		"<li>blah</li>" +
		"</ul>\n";
		String mid2 = 
		"MID2\n";
		String thirdList = 
		"<ol>" +
		"<li>blah</li>" +
		"</ol>\n";
		String input = pre + firstList + mid + secondList + mid2 + thirdList;
		Matcher m = listChunker.matcher(input);
//		does the regex work at all?
		assertTrue(m.find());
		//log what happened
//		helper.pretextGroupPrinter(m, this.log);
//		m.reset();
		//test the found pretexts
		m.reset();
		Vector expectedPretexts = new Vector();
		expectedPretexts.add(pre);
		expectedPretexts.add(mid);
		expectedPretexts.add(mid2);
		testPretexts("Basic", m, expectedPretexts);
		//test the found lists
		m.reset();
		Vector expectedLists = new Vector();
		expectedLists.add(firstList);
		expectedLists.add(secondList);
		expectedLists.add(thirdList);
		testLists("Basic", m, expectedLists);

	}

	private void testPretexts(String message, Matcher matcher, Vector expected) {
		int index = 0;
		while (matcher.find()) {
			String actual = matcher.group(2);
			String exp = (String) expected.get(index++);
			assertEquals(message + " #" + index, exp, actual);
		}
		assertTrue(message + " index = " + index + " exp = " + expected.size(), 
				expected.size() == index);
	}
	
	private void testLists(String message, Matcher matcher, Vector expected) {
		int index = 0;
		while (matcher.find()) {
			String actual = matcher.group(3) + matcher.group(4);
			String exp = (String) expected.get(index++);
			assertEquals(message, exp, actual);
		}
		assertTrue(message + " index = " + index + " exp = " + expected.size(), 
				expected.size() == index);	
	}

	
	public void testAddNewlines() {
		String input = "<br /><ul><li>a</li></ul>";
		String expected = "<br />\n<ul>\n<li>a</li>\n</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}
	
	public void testSimpleList() {
		String input = "<ul>\n" +
		"<li>This is an item</li>\n" +
		"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(input, actual);
	}
	
	public void testSimpleListPreWS() {
		String input = "<ul>   \n" +
		"<li>This is an item</li>\n" +
		"</ul>\n";
		String expected = "<ul> \n" +
		"<li>This is an item</li>\n" +
		"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}
	public void testSimpleListPostWS() {
		String input = "<ul>\n   " +
		"<li>This is an item</li>\n" +
		"</ul>\n";
		String expected = "<ul> \n" +
		"<li>This is an item</li>\n" +
		"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}
	
	public void testManualList() {
		//pre processing
			String input = "<ul>\n" +
				"<li>This is how\n" +
				"</li><li>I would\n" +
				"</li><li>make a\n" +
				"</li><li>list\n" +
				"</li></ul>\n";
		String expected = "<ul>\n" +
				"<li>This is how</li>\n" +
				"<li>I would</li>\n" +
				"<li>make a</li>\n" +
				"<li>list</li>\n" +
				"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		//parsing
		input = actual;
		expected = "* This is how\n" +
				"* I would\n" +
				"* make a\n" +
				"* list\n\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testNotAList() {
		String input = "* This is NOT a list in Jotspot";
		String expected = "\\* This is NOT a list in Jotspot";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = "pre\n" +
				input;
		expected = "pre\n" + expected;
		actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = "pre\n" +
				"<br />* NOT a list";
		expected = "pre\n<br />" + "\\* NOT a list";
		actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}

	public void testMarkupList() {
		String input = "<ul><li> One\n" +
				"</li><li> Two\n" +
				"<ul><li> Two Point Five\n" +
				"\n" +
				"</li></ul></li></ul>\n" ;
		String expected = "<ul>\n" +
				"<li> One</li>\n" +
				"<li> Two</li>\n" +
				"<ul>\n" +
				"<li> Two Point Five</li>\n" +
				"</ul>\n" +
				"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);

		input = actual;
		expected = "*  One\n" +
		"*  Two\n" +
		"**  Two Point Five\n\n" ;
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testComboLists() {
		String input = "<ul><li> One\n" +
				"<ol><li> Confusing\n" +
				"</li><li> Nesting\n" +
				"</li></ol></li><li> Two\n" +
				"\n" +
				"</li></ul>\n";
		String expected = "<ul>\n" +
				"<li> One</li>\n" +
				"<ol>\n" +
				"<li> Confusing</li>\n" +
				"<li> Nesting</li>\n" + 
				"</ol>\n" +
				"<li> Two</li>\n" +
				"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected = "*  One\n" +
				"*#  Confusing\n" +
				"*#  Nesting\n" +
				"*  Two\n\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		// a different combo list variant
		input = "<ul><li> One\n" +
				"<ol><li> Confusing\n" +
				"</li></ol></li><li> Nesting\n" +
				"</li><li> Two\n" +
				"\n" +
				"</li></ul>\n" ;
		expected = "<ul>\n" +
				"<li> One</li>\n" +
				"<ol>\n"+
				"<li> Confusing</li>\n" +
				"</ol>\n" + 
				"<li> Nesting</li>\n" +
				"<li> Two</li>\n" +
				"</ul>\n" ;
		actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected = 
				"" +
				"*  One\n" +
				"*#  Confusing\n" +
				"*  Nesting\n" +
				"*  Two\n\n";
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
	}
	
	public void testAnotherListTest() {
		String input = "<ol><li> One\n" +
				"</li><li> Two\n" +
				"<ol><li> Two Point Five\n" +
				"</li></ol></li><li> Three\n" +
				"\n" +
				"</li></ol>\n";
		String expected = "<ol>\n" +
				"<li> One</li>\n" +
				"<li> Two</li>\n" +
				"<ol>\n" +
				"<li> Two Point Five</li>\n" +
				"</ol>\n" + 
				"<li> Three</li>\n" +
				"</ol>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected = "#  One\n" +
				"#  Two\n" +
				"##  Two Point Five\n" +
				"#  Three\n\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testTwoListsAtOnce() {
		String input = "<ul><li> One\n" +
				"</li><li> Two\n" +
				"<ul><li> Two Point Five\n" +
				"\n" +
				"</li></ul></li></ul><p>Ordered List:\n" +
				"\n" +
				"</p><ol><li> One\n" +
				"</li><li> Two\n" +
				"<ol><li> Two Point Five\n" +
				"</li></ol></li><li> Three\n" +
				"\n" +
				"</li></ol>\n" ;
		String expected = "<ul>\n" +
				"<li> One</li>\n" +
				"<li> Two</li>\n" +
				"<ul>\n" +
				"<li> Two Point Five</li>\n" +
				"</ul>\n" +
				"</ul>\n" +
				"<p>Ordered List:\n" +
				"</p>\n" +
				"<ol>\n" +
				"<li> One</li>\n" +
				"<li> Two</li>\n" +
				"<ol>\n" +
				"<li> Two Point Five</li>\n" +
				"</ol>\n" +
				"<li> Three</li>\n" +
				"</ol>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected = "*  One\n" +
				"*  Two\n" +
				"**  Two Point Five\n\n" +
				"<p>Ordered List:\n" +
				"</p>\n" +
				"#  One\n" +
				"#  Two\n" +
				"##  Two Point Five\n" +
				"#  Three\n\n" ;
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testNotListYesBold() {
		String input = "*_bold & italic_*";
		String expected = input;
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}
	
	public void testLostDepthRapidly() {
		String input = "<ul>\n" +
				"<li>A</li>\n" +
				"<ul>\n" +
				"<li>B</li>\n" +
				"<ul>\n" +
				"<li>C</li>\n" +
				"</ul>" +
				"</ul>" +
				"<li>D</li>\n" +
				"</ul>\n";
		String expected = "* A\n" +
				"** B\n" +
				"*** C\n" +
				"* D\n\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		String actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testListWithPara() {
		String fontcolor = "<font color=\"([^\"]+)\">(.*?)</font>";//{replace-with}{color:$1}$2{color}"
		Pattern fontPattern = Pattern.compile(fontcolor);
		String input = "<ul>\n" +
				"<li><font color=\"#006600\">The sun....our shinier brother celestial body (test edit history).</font></li>\n" +
				"<li><font color=\"#006600\">Gives off a warm warming glow...just like TV </font>\n" +
				"<ul>\n" +
				"<li><font color=\"#006600\">But not LCDs </font></li></ul></li>\n" +
				"<li><font color=\"#000099\">Is <font color=\"#ffcc00\">yellowish </font>in color</font></li>" +
				"<li><p><font color=\"#000099\">Nested <b style=\"background-color: rgb(204, 0, 0);\">" +
				"<font color=\"#ff9900\">font might be a challenge to</font></b> convert.</font></p></li></ul>\n";
		Matcher fontFinder = fontPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (fontFinder.find()) {
			String color = fontFinder.group(1);
			String text = fontFinder.group(2);
			String replacement = "{color:" + color + "}" + text + "{color}";
			fontFinder.appendReplacement(sb, replacement);
		}
		fontFinder.appendTail(sb);
		input = sb.toString();
		String expected = "<ul>\n" +
				"<li>{color:#006600}The sun....our shinier brother celestial body (test edit history).{color}</li>\n" +
				"<li>{color:#006600}Gives off a warm warming glow...just like TV {color}\n" +
				"<ul>\n" +
				"<li>{color:#006600}But not LCDs {color}</li></ul></li>\n" +
				"<li>{color:#000099}Is <font color=\"#ffcc00\">yellowish {color}in color</font></li>" +
				"<li><p>{color:#000099}Nested <b style=\"background-color: rgb(204, 0, 0);\">" +
				"<font color=\"#ff9900\">font might be a challenge to{color}</b> convert.</font></p></li></ul>\n";
		
		assertEquals(expected, input);
		
		
		expected = "<ul>\n" +
				"<li>{color:#006600}The sun....our shinier brother celestial body (test edit history).{color}</li>\n" +
				"<li>{color:#006600}Gives off a warm warming glow...just like TV {color}</li>\n" +
				"<ul>\n" +
				"<li>{color:#006600}But not LCDs {color}</li>\n" +
				"</ul>\n" +
				"<li>{color:#000099}Is yellowish {color}in color</li>\n" +
				"<li>{color:#000099}Nested " +
				"font might be a challenge to{color} convert.</li>\n" +
				"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected = 	
				"* {color:#006600}The sun....our shinier brother celestial body (test edit history).{color}\n" +
				"* {color:#006600}Gives off a warm warming glow...just like TV {color}\n" +
				"** {color:#006600}But not LCDs {color}\n" +
				"* {color:#000099}Is yellowish {color}in color\n" +
				"* {color:#000099}Nested font might be a challenge to{color} convert.\n\n" ;
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testWindowsNewlines() {
		String input = 
				"<ul><li> <a href=\"wiki:Inventory of products\">Inventory of products</a>\r\n" +
				"</li><li> <a href=\"wiki:T9 Marketing Message\">T9 Marketing Message</a>\r\n" +
				"</li><li> <a href=\"wiki:Text Input and Output\">Text Input and Output</a>\r\n" +
				"</li><li> <a href=\"wiki:T9 Navigator and Ultra\">T9 Navigator and Ultra</a>\r\n" +
				"</li><li> <a href=\"wiki:XT9 Architecture\">XT9 Architecture</a>\r\n" +
				"</li></ul>\r\n" ;
		String expected = "<ul>\r\n" +
				"<li> <a href=\"wiki:Inventory of products\">Inventory of products</a></li>\r\n" +
				"<li> <a href=\"wiki:T9 Marketing Message\">T9 Marketing Message</a></li>\r\n" +
				"<li> <a href=\"wiki:Text Input and Output\">Text Input and Output</a></li>\r\n" +
				"<li> <a href=\"wiki:T9 Navigator and Ultra\">T9 Navigator and Ultra</a></li>\r\n" +
				"<li> <a href=\"wiki:XT9 Architecture\">XT9 Architecture</a></li>\r\n" +
				"</ul>\r\n" ;
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		expected =
			"*  [Inventory of products]\n" +
			"*  [T9 Marketing Message]\n" +
			"*  [Text Input and Output]\n" +
			"*  [T9 Navigator and Ultra]\n" +
			"*  [XT9 Architecture]\n\r\n" ;
		Page page = new Page(null);
		page.setOriginalText(input);
		linkConverter.convert(page);
		page.setOriginalText(page.getConvertedText());
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testListsNoPretextBetween() {
		String input = "<ul><li> <a href=\"wiki:AOL Developer Programs\">AOL Developer Programs</a>\n" +
				"\n" +
				"</li></ul><ul><li> <a href=\"wiki:AOL Announcements\">AOL Announcements</a>\n" +
				"</li><li> <a href=\"wiki:AOL Internal Sites\">AOL Internal Sites</a>\n" +
				"</li><li> <a href=\"wiki:AOL 2.Open\">AOL 2.Open</a>\n" +
				"\n" +
				"</li></ul>\n" ;
		String expected = "<ul>\n" +"<li> <a href=\"wiki:AOL Developer Programs\">AOL Developer Programs</a></li>\n" +
				"</ul> \n" + //add a space here so that the converter can chunk the lists
				"<ul>\n" +"<li> <a href=\"wiki:AOL Announcements\">AOL Announcements</a></li>\n" +
				"<li> <a href=\"wiki:AOL Internal Sites\">AOL Internal Sites</a></li>\n" +
				"<li> <a href=\"wiki:AOL 2.Open\">AOL 2.Open</a></li>\n" +
				"</ul>\n" ;
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
		
		input = actual;
		input = expected;
		expected = "*  [AOL Developer Programs]" +
		"\n\n \n" +
		"*  [AOL Announcements]\n" +
		"*  [AOL Internal Sites]\n" +
		"*  [AOL 2.Open]\n\n" ;
		Page page = new Page(null);
		page.setOriginalText(input);
		linkConverter.convert(page);
		page.setOriginalText(page.getConvertedText());
		listConverter.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}

	/* End List Section */
	/* Start Table Section */

	public void testTableEndProblem() {
		String input = 
				"numbered lists" +
				"<br /><ol><li>item1</li><li>item2<ol><li>item2a</li><li>item2b</li></ol></li><li>item3<ol>" +
				"<li>item3a<ol><li>item3a1<ol><li>item3a1A</li></ol></li><li>item3a2</li><li>item3a3</li>" +
				"</ol></li></ol></li><li>item4<br /></li></ol>" +
				"hyper links both internal and external<br />" +
				"<a href=\"wiki:///WikiHome/test page 1\">test page 1</a><br />" +
				"<a href=\"http://www.google.com/\">http://www.google.com</a><br /><br />tables\n" +
				"<br />";
		String expected =  
				"numbered lists" +
				"<br />\n" +
				"<ol>\n" +
				"<li>item1</li>\n" +
				"<li>item2</li>\n" +
				"<ol>\n" +
				"<li>item2a</li>\n" +
				"<li>item2b</li>\n" +
				"</ol>\n" +
				"<li>item3</li>\n" +
				"<ol>\n" +
				"<li>item3a</li>\n" +
				"<ol>\n" +
				"<li>item3a1</li>\n" +
				"<ol>\n" +
				"<li>item3a1A</li>\n" +
				"</ol>\n" +
				"<li>item3a2</li>\n" +
				"<li>item3a3</li>\n" +
				"</ol>\n" +
				"</ol>\n" +
				"<li>item4</li>\n" +
				"</ol>\n" +
				"hyper links both internal and external<br />" +
				"<a href=\"wiki:///WikiHome/test page 1\">test page 1</a><br />" +
				"<a href=\"http://www.google.com/\">http://www.google.com</a><br /><br />tables\n" +
				"<br />";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);
	}

	public void testTableBasic() {
		String input = "<tr><td width=\"100\">Header1</td><td width=\"100\">Header2 " +
				"</td><td width=\"100\">Header3 " +
				"</td></tr><tr><td width=\"100\">r1c1 " +
				"</td><td width=\"100\">r1c2 " +
				"</td><td width=\"100\">r1c3 " +
				"</td></tr><tr><td style=\"vertical-align: top;\">r2c1" +
				"</td><td style=\"vertical-align: top;\">r2c2" +
				"</td><td style=\"vertical-align: top;\">r2c3" +
				"</td></tr>";
		String expected = "| Header1 | Header2 | Header3 |\n" +
				"| r1c1 | r1c2 | r1c3 |\n" +
				"| r2c1 | r2c2 | r2c3 |\n";
		String actual = tableConverter.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testTableWithBreaks() {
		String input = "<tr><td width=\"100\"> Header1</td>" +
				"<td width=\"100\">Header2 <br /></td><td width=\"100\">" +
				"Header3 <br /></td></tr><tr><td width=\"100\">r1c1 <br />" +
				"</td><td width=\"100\">r1c2 <br /></td><td width=\"100\">" +
				"r1c3 <br /></td></tr><tr><td style=\"vertical-align: top;\">" +
				"r2c1<br /></td><td style=\"vertical-align: top;\">r2c2<br />" +
				"</td><td style=\"vertical-align: top;\">r2c3<br /></td></tr>";
		String expected = "| Header1 | Header2 | Header3 |\n" +
		"| r1c1 | r1c2 | r1c3 |\n" +
		"| r2c1 | r2c2 | r2c3 |\n";
		String actual = tableConverter.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertRow() {
		String input = "<td>a</td><td>b</td>";
		String expected = " a | b |";
		String actual = tableConverter.convertRow(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertHeaders() {
		String input = 
			"<tr>\n" +
			"<td width=\"100\"><b>?| Task</b></td>\n" +
			"<td width=\"100\"><b>Priority <br /></b></td>\n" +
			"<td width=\"103\">?| <b>Estimate</b></td></tr>\n" +
			"<tr>\n" +
			"<td width=\"100\">Design</td>\n" +
			"<td width=\"100\">S</td>\n" +
			"<td width=\"103\">?| </td></tr>\n";
		String expected = "|| Task || Priority || Estimate ||\n" +
			"| Design | S | |\n";
		String actual = tableConverter.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testRemoveBadChar() {
		String input = "<td width=\"103\">?| </td>";
		String expected = "<td width=\"103\"> </td>";
		String actual = tableConverter.removeBadChar(input);
		assertEquals(expected, actual);
	}
	
	public void testSimpleConvertHeader() {
		String input = "| <b>A</b> |";
		String expected = "|| A ||";
		String actual = tableConverter.convertHeader(input);
		assertEquals(expected, actual);
		
		input = "| B |";
		expected = input;
		actual = tableConverter.convertHeader(input);
		assertEquals(expected, actual);
		
		input = "| <b>A</b> | <b>B</b> |";
		expected = "|| A || B ||";
		actual = tableConverter.convertHeader(input);
		assertEquals(expected, actual);
	}
	
	public void testCleanTags() {
		String input = "<table border=\"1\" cellPadding=\"1\" cellSpacing=\"0\" class=\"jot-tabular\" height=\"96\" width=\"767\"><tbody>Some stuff</tbody></table>";
		String expected = "Some stuff";
		String actual = tableConverter.cleanTags(input);
		assertEquals(expected, actual);
		
		input = "<table border=\"1\" cellPadding=\"1\" cellSpacing=\"0\" class=\"jot-tabular\" height=\"96\" width=\"767\">\n" +
				"<tbody>\n" +
				"Some stuff\n" +
				"</tbody></table>";
		actual = tableConverter.cleanTags(input);
		assertEquals(expected, actual);

	}
	
	public void testTableWithTH() {
		//test cleaning tags
		String input = "<table>\n" +
				"<tbody><tr>\n" +
				"<th>Header?</th><th>ooo! Did the ? stay in the previous header?</th></tr>\n" +
				"<tr><td>question mark</td><td>This is just a normal cell</td></tr>\n" +
				"</tbody></table>\n";
		String expected = "<tr>\n" +
				"<th>Header?</th><th>ooo! Did the ? stay in the previous header?</th></tr>\n" +
				"<tr><td>question mark</td><td>This is just a normal cell</td></tr>";
		String actual = tableConverter.cleanTags(input);
		assertEquals(expected, actual);
		
		//test headers 
		input = actual;
		expected = "|| Header? || ooo! Did the ? stay in the previous header? ||\n" +
				"| question mark | This is just a normal cell |\n";
		actual = tableConverter.convertTable(input);
		assertEquals(expected, actual);
	}
	
	public void testTH() {
		String input = "<th>Header</th>";
		String expected = " Header ||";
		String actual = tableConverter.convertRow(input);
		
		assertEquals(expected, actual);
	}
	
	public void testTableNewlineIssue() {
		String input = 
//				"Pre Text" +  // Jotspot seems to adds <br/> before table, so no worries here
				"<table><tbody><tr><th>One</th><th>Two</th></tr><tr>" +
				"<td>A</td><td>B</td></tr></tbody></table>Post Text";
		String expected = 
//				"Pre Text\n" +
				"|| One || Two ||\n" +
				"| A | B |\n" +
				"Post Text\n";
		String actual = tableConverter.convertTable(tableConverter.cleanTags(input));
		assertEquals(expected, actual);
	}
	
	/* End Table Section */
	/* Start Attachment Section */
	
	public void testEncodingChars() {
		AttachmentConverter attachHelper = new AttachmentConverter();
		String input = "goodchars_but1then,acomma";
		String expected = "goodchars_but1then%2Cacomma";
		String actual = attachHelper.encodeChars(input);
		assertEquals(expected, actual);
//		
//		input = ",_+as d123! (";
//		expected = "%2C_+as d123%21 %28";
//		actual = attachHelper.encodeChars(input);
//		assertEquals(expected, actual);
//		
		input = "270px-Sun,_Earth_size_comparison_labeled.jpg";
		expected = "270px-Sun%2C_Earth_size_comparison_labeled.jpg";
		actual = attachHelper.encodeChars(input);
		assertEquals(expected, actual);
		
		input = "/Users/laura/Desktop/jotspot/testdata/System/" +
				"TmpImageUpload/270px-Sun,_Earth_size_comparison_labeled.jpg/" +
				"_data/270px-Sun,_Earth_size_comparison_labeled.jpg";
		expected = "/Users/laura/Desktop/jotspot/testdata/System/" +
				"TmpImageUpload/270px-Sun%2C_Earth_size_comparison_labeled.jpg/" +
				"_data/270px-Sun%2C_Earth_size_comparison_labeled.jpg";
		actual = attachHelper.encodeChars(input);
		assertEquals(expected, actual);
		
	}
	
	/* End Attachment Section */
	/* Start Link Section */
	
	public void testSimpleAddEndItemTag() {
		String input = "<ul>\n" +
				"<li>a</li>\n" +
				"<li>b<ul>\n" +
				"<li>c</li></ul></li>\n" +
				"</ul>";
		String expected =  "<ul>\n" +
			"<li>a</li>\n" +
			"<li>b</li>\n" +
			"<ul>\n" +
			"<li>c</li>\n" +
			"</ul>\n" +
			"</ul>\n";
		String actual = processor.preProcessLists(input);
		assertEquals(expected, actual);		
		
		input = expected;
		expected = "* a\n" +
				"* b\n" +
				"** c\n\n";
		
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		actual = page.getConvertedText();
		
		assertEquals(expected, actual);
		
	}
	
	public void testAltTagProblem() {
		String input = "<ul>\n" +
				"<li class=\"toclevel-2\">" +
		"3.1 [Core|http://en.wikipedia.org/wiki/Sun#Core]" +
		"</li>\n" + 
		"</ul>\n";

		String expected = 
				"" +
				"* " +
				"3.1 [Core|http://en.wikipedia.org/wiki/Sun#Core]\n\n";
		
		Page page = new Page(null);
		page.setOriginalText(input);
		listConverter.convert(page);
		String actual = page.getConvertedText();
		
		assertEquals(expected, actual);
	}
	
	public void testTOCLink() {
		String input = "<a href=\"http://en.wikipedia.org/wiki/Sun#Overview\">" +
				"<span class=\"tocnumber\">1</span> <span class=\"toctext\">" +
				"Overview</span></a>";
		String expected = "[1 Overview|http://en.wikipedia.org/wiki/Sun#Overview]";
		String actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
		
		input = "<a href=\"http://en.wikipedia.org/wiki/Sun#Core\"><span class=\"tocnumber\">3.1</span> <span class=\"toctext\">Core</span></a>";
		expected = "[3.1 Core|http://en.wikipedia.org/wiki/Sun#Core]";
		actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testSimpleLink() {
		String input = "<a href=\"http://www.google.com\">Google</a>";
		String expected = "[Google|http://www.google.com]";
		String actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
		
		input = "<a href=\"http://www.google.com\">http://www.google.com</a>";
		expected = "[http://www.google.com]";
		actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
		
	}
	
	public void testClearSpans() {
		
		String input = "<span something=\"something\">Actual Text</span> <span>More Text</span>";
		String expected = "Actual Text More Text";
		String actual = linkConverter.clearSpans(input);
		assertEquals(expected, actual);
	}
	
	public void testLinkList() {
		String input = "<ul><li class=\"toclevel-1\"><a href=\"http://en.wikipedia.org/wiki/Sun#Overview\">" +
				"<span class=\"tocnumber\">1</span> <span class=\"toctext\">Overview</span></a></li>" +
				"<li class=\"toclevel-1\"><a href=\"http://en.wikipedia.org/wiki/Sun#Life_cycle\">" +
				"<span class=\"tocnumber\">2</span> <span class=\"toctext\">Life cycle</span></a></li>" +
				"<li class=\"toclevel-1\"><a href=\"http://en.wikipedia.org/wiki/Sun#Structure\">" +
				"<span class=\"tocnumber\">3</span> <span class=\"toctext\">Structure</span></a></li></ul>";
		String expected = "<ul><li class=\"toclevel-1\">[1 Overview|http://en.wikipedia.org/wiki/Sun#Overview]" +
				"</li>" +
				"<li class=\"toclevel-1\">[2 Life cycle|http://en.wikipedia.org/wiki/Sun#Life_cycle]" +
				"</li>" +
				"<li class=\"toclevel-1\">[3 Structure|http://en.wikipedia.org/wiki/Sun#Structure]" +
				"</li></ul>";
		String actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
	}
	
	public void testInternalLink() {
		String input = "<a href=\"wiki:///WikiHome/Project Foo Calendar\">Release calendar</a>";
		String expected = "[Release calendar|Project Foo Calendar]";
		String actual = linkConverter.convertInternalLinks(input);
		assertEquals(expected, actual);
		
		input =  "<a href=\"wiki:///WikiHome/Project Foo Calendar\">Project Foo Calendar</a>";
		expected = "[Project Foo Calendar]";
		actual = linkConverter.convertInternalLinks(input);
		assertEquals(expected, actual);
		
	}
	
	public void testLinkWithExtraAtt() {
		String input = "<a href=\"http://www.google.com\" onclick=\"#\">Weird Link</a>";
		String expected = "[Weird Link|http://www.google.com]";
		String actual = linkConverter.convertExternalLinks(input);
		assertEquals(expected, actual);
		
		input = "<a onclick=\"#\" href=\"wiki://System/Something\" style=\"color:red\">Weird Link</a>";
		expected = "[Weird Link|Something]";
		actual = linkConverter.convertInternalLinks(input);
		assertEquals(expected, actual);
	}
		
	/* End Link Section */
	/* Start Header Section */
	
	// pattern from converter.jotspot.properties
	Pattern headerPattern = Pattern.compile("<h(\\d)>(.*?)(?:(?:\r\n)|\n)*<\\/h\\1>"); 
	String headerReplace = "h$1. $2<br/>";
	public void testHeaders() {
		String input = "<h1>Some header\n</h1>";
		String expected = "h1. Some header<br/>";
		String actual = testRegex(input, headerPattern, headerReplace);
		assertEquals(expected, actual);
	}
	
	public void testHeadersWithWindowsNewlines() {
		String input = "<h1>T9 The Future\r\n" + "\r\n" + "</h1>";
		String expected = "h1. T9 The Future<br/>";
		String actual = testRegex(input, headerPattern, headerReplace);
		assertEquals(expected, actual);
	}
	/* End Header section */
	
	Pattern imagePattern = Pattern.compile("<img src=\"(?:[^\"]*\\/)?([^/\"]+)\"[^/]*/>");
	String imageReplace = "!$1!";
	public void testImage() {
		String input = "<img src=\"/System/TmpImageUpload/hobbespounce.gif\" />";
		String expected = "!hobbespounce.gif!";
		String actual = testRegex(input, imagePattern, imageReplace);
		assertEquals(expected, actual);
	}
	
	public void testImageWithAtt() {
		String input = "<img src=\"top_bk3.jpg\" width=\"100%\" />";
		String expected = "!top_bk3.jpg!";
		String actual = testRegex(input, imagePattern, imageReplace);
		assertEquals(expected, actual);
	}
	
	/* converter properties regex tester */
	
	private String testRegex(String input, Pattern regex, String replacement) {
		Matcher finder = regex.matcher(input);
		if (finder.find()) {
			return finder.replaceAll(replacement);
		}
		return input;
	}
}
