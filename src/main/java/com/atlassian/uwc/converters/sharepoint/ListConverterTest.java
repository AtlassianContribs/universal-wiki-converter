package com.atlassian.uwc.converters.sharepoint;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.dom4j.Text;

public class ListConverterTest extends TestCase {

	ListConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ListConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertLists1() {
		String input = 
				"<html>" +
				"<ul>" +
				"<li>" +
				"abc\n" +
				"</li>\n" +
				"<li>\n" +
				"def" +
				"</li>" +
				"</ul>" +
				"</html>";
		String expected = "<html>\n" +
				"* abc\n" +
				"* def\n" +
				"\n" +
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists2() {
		String input = 
				"<html>" +
				"<ol>" +
				"<li>" +
				"abc\n" +
				"</li>\n" +
				"<li>\n" +
				"def" +
				"</li>" +
				"</ol>" +
				"</html>";
		String expected = "<html>\n" +
				"# abc\n" +
				"# def\n" +
				"\n" +
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	public void testConvertLists3() {
		String input = 
				"<html>" +
				"<ol>" +
				"<li>" +
				"abc\n" +
				"</li>\n" +
				"<ol>" +
				"<li>\n" +
				"def" +
				"</li>" +
				"</ol>" +
				"</ol>" +
				"</html>";
		String expected = "<html>\n" +
				"# abc\n" +
				"## def\n" +
				"\n" +
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists4() {
		String input = 
				"<html>" +
				"<ol>" +
				"<li>" +
				"abc\n" +
				"</li>\n" +
				"<li>\n" +
				"def" +
				"</li>" +
				"</ol>" +
				"After" +
				"</html>";
		String expected = "<html>\n" +
				"# abc\n" +
				"# def\n" +
				"\n" + 
				"After" +
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists5() {
		String input = 
				"<html>\n" +
				"<ul>\n" + 
					"<li>\n" + 
					"abc</li>\n" + 
					"<li>\n" + 
					"def</li>\n" + 
					"<ul>\n" + 
						"<li>\n" + 
						"ghi</li>\n" + 
						"<li>\n" + 
						"hij</li>\n" + 
					"</ul>\n" + 
					"<li>\n" + 
					"klm</li>\n" + 
					"<ul>\n" + 
						"<li>\n" + 
						"nop</li>\n" + 
						"<ul>\n" + 
							"<li>\n" + 
							"qrs</li>\n" + 
						"</ul>\n" + 
						"<li>\n" + 
						"tuv</li>\n" + 
					"</ul>\n" + 
				"</ul>" +
				"</html>";
		String expected = "<html>\n" +
				"\n" +
				"* abc\n" + 
				"* def\n" + 
				"** ghi\n" + 
				"** hij\n" + 
				"* klm\n" + 
				"** nop\n" + 
				"*** qrs\n" + 
				"** tuv\n" +
				"\n" +
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLists6() {
		String input = 
				"<html>\n" +
				"<ul>\n" + 
					"<li>\n" + 
					"abc</li>\n" + 
					"<li>\n" + 
					"def</li>\n" + 
					"<ol>\n" + 
						"<li>\n" + 
						"ghi</li>\n" + 
					"</ol>\n" + 
					"<ol>\n" + 
						"<li>\n" + 
						"hij</li>\n" + 
					"</ol>\n" + 
				"</ul>\n" + 
				"<ol>\n" + 
					"<li>\n" + 
					"klm</li>\n" + 
					"<ol>\n" + 
						"<li>\n" + 
						"nop</li>\n" + 
					"</ol>\n" + 
					"<ol>\n" + 
						"<ol>\n" + 
							"<li>\n" + 
							"qrs</li>\n" + 
						"</ol>\n" + 
					"</ol>\n" + 
				"</ol>\n" + 
				"<ul>\n" + 
					"<ul>\n" + 
						"<li>\n" + 
						"tuv</li>\n" + 
					"</ul>\n" + 
				"</ul>" +
				"</html>";
		String expected = "<html>\n" +
				"\n" +
				"* abc\n" + 
				"* def\n" + 
				"*# ghi\n" + 
				"*# hij\n" + 
				"# klm\n" + 
				"## nop\n" + 
				"### qrs\n" + 
				"** tuv\n" +
				"\n" +
				"</html>"; 
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists7() {
		String input = 
				"<html>\n" +
				"<ul>\n" + 
				"<li>\n" + 
				"abc</li>\n" + 
				"<li>\n" + 
				"def</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"*ghi*\n" + 
				"</li>\n" + 
				"<li>\n" + 
				"_hij_\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"<li>\n" + 
				"klm</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"{color:#ff0000}nop{color}\n" + 
				"</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"+qrs+\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"<li>\n" + 
				"tuv</li>\n" + 
				"</ul>\n" + 
				"</ul>" +
				"</html>";
		String expected = "<html>\n" +
				"\n" +
				"* abc\n" + 
				"* def\n" + 
				"** *ghi*\n" + 
				"** _hij_\n" + 
				"* klm\n" + 
				"** {color:#ff0000}nop{color}\n" + 
				"*** +qrs+\n" + 
				"** tuv\n" +
				"\n" + 
				"</html>";
		String actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists8() {
		String input, actual, expected;
		input = "<html>*Unorderedlist*\n" + 
			"<ul><li>abc</li><li>def</li><li><ul><li>ghi</li><li>hij</li></ul></li><li>klm</li><li><ul><li>nop</li><li><ul><li>qrs</li></ul></li><li>tuv</li></ul></li></ul></html>";
		expected = "<html>*Unorderedlist*\n" +
			"\n" + 
			"* abc\n" +
			"* def\n" +
			"** ghi\n" +
			"** hij\n" +
			"* klm\n" +
			"** nop\n" +
			"*** qrs\n" +
			"** tuv\n" +
			"\n" +
			"</html>";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//correctly handled 122 list layout
		input = "<html>" +
		"<ol><li>klm</li>" +
			"<li><ol><li>nop</li></ol></li>" +
			"<li><ol><li>qrs</li></ol></li>" +
		"</ol>" +
		"</html>";
		input = cleanWithCleanConverter(input);
		actual = tester.convertLists(input);
		expected = "<html>\n" +
			"# klm\n" +
			"## nop\n" +
			"## qrs\n" +
			"\n" +
			"</html>"; 

		//FIXME test case changing
		input = "<html>\n" +
				"*Both list*\n" + 
				"<ul><li>abc</li><li>def</li><li/><li><ol><li>ghi</li></ol><ol><li>hij</li>" +
				"</ol></li></ul><ol><li>klm</li><li/><li><ol><li>nop</li></ol><ol><li/><li>" +
				"<ol><li>qrs</li></ol></li></ol></li></ol><ol><li/><li><ul><li>tuv</li></ul>" +
				"</li></ol>\n" + 
				"</html>";
		expected = "<html>\n" +
				"*Both list*\n" + 
				"\n" +
				"* abc\n" +
				"* def\n" +
				"*# ghi\n" +
				"*# hij\n" +
				"# klm\n" +
				"## nop\n" +
				"### qrs\n" +
				"#* tuv\n" +
				"\n" +
				"\n" + 
				"</html>";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
		input = "<html>*Unorderedlist*\n" + 
				"<ul><li>abc</li><li>def</li><li/><li><ul><li>ghi</li><li>hij</li></ul></li><li>klm</li><li/><li><ul><li>nop</li><li/><li><ul><li>qrs</li></ul></li><li>tuv</li></ul></li></ul>\n" + 
				"*Orderedlist*\n" + 
				"<ol><li>abc</li><li>def</li><li/><li><ol><li>ghi</li><li>hij</li></ol></li><li>klm</li><li/><li><ol><li>nop</li><li/><li><ol><li>qrs</li></ol></li><li>tuv</li></ol></li></ol>*Both list*\n" + 
				"<ul><li>abc</li><li>def</li><li/><li><ol><li>ghi</li></ol><ol><li>hij</li></ol></li></ul><ol><li>klm</li><li/><li><ol><li>nop</li></ol><ol><li/><li><ol><li>qrs</li></ol></li></ol></li></ol><ul><li/><li><ul><li>tuv</li></ul></li></ul>\n" + 
				"*Other Syntax*\n" + 
				"<ul><li>abc</li><li>def</li><li/><li><ul><li>*ghi*</li><li>_hij_</li></ul></li><li>klm</li><li/><li><ul><li>{color:#ff0000}nop{color}</li><li/><li><ul><li>+qrs+</li></ul></li><li>tuv</li></ul></li></ul>*With Font Size*\n" + 
				"<ul><li>h4. abc</li><li>h4. def</li><li/><li><ul><li>h4. ghi</li><li>h4. hij</li></ul></li><li>h4. klm</li><li/><li><ul><li>h4. nop</li><li/><li><ul><li>h4. qrs</li></ul></li><li>h4. tuv</li></ul></li></ul></html>";
		expected = "<html>*Unorderedlist*\n" + 
				"\n" +
				"* abc\n" +
				"* def\n" +
				"** ghi\n" +
				"** hij\n" +
				"* klm\n" +
				"** nop\n" +
				"*** qrs\n" +
				"** tuv\n" +
				"\n" +
				"\n" +
				"*Orderedlist*\n" +
				"\n" + 
				"# abc\n" +
				"# def\n" +
				"## ghi\n" +
				"## hij\n" +
				"# klm\n" +
				"## nop\n" +
				"### qrs\n" +
				"## tuv\n" +
				"\n" +
				"*Both list*\n" + 
				"\n" +
				"* abc\n" +
				"* def\n" +
				"*# ghi\n" +
				"*# hij\n" +
				"# klm\n" +
				"## nop\n" +
				"### qrs\n" +
				"** tuv\n" +
				"\n" +
				"\n" + 
				"*Other Syntax*\n" + 
				"\n" +
				"* abc\n" +
				"* def\n" +
				"** *ghi*\n" +
				"** _hij_\n" +
				"* klm\n" +
				"** {color:#ff0000}nop{color}\n" +
				"*** +qrs+\n" +
				"** tuv\n" +
				"\n" +
				"*With Font Size*\n" + 
				"\n" +
				"* h4. abc\n" +
				"* h4. def\n" +
				"** h4. ghi\n" +
				"** h4. hij\n" +
				"* h4. klm\n" +
				"** h4. nop\n" +
				"*** h4. qrs\n" +
				"** h4. tuv\n" +
				"\n" +
				"</html>";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists9() {
		String input, expected, actual;
		input = "<html>\n" +
				"<ul><li>h6. {color:black}xxxxxx<span>~U??</span>xxxxxx{color}</li></ul>\n" +
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"* h6. {color:black}xxxxxx~U??xxxxxx{color}\n" +
				"\n\n" +
				"</html>";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists10() {
		String input, expected, actual;
		input = "<html>\n" + //no dollars
				"<ul><li>1</li><li>2</li></ul>\n" + 
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"* 1\n" + 
				"* 2\n" +
				"\n" +
				"\n" +
				"</html>";
		actual = tester.removeUnnecessaryNewlines(expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" + //has dollars
				"<ul><li>$1</li><li>$2</li></ul>\n" + 
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"* $1\n" + 
				"* $2\n" +
				"\n" +
				"\n" +
				"</html>";
		actual = tester.removeUnnecessaryNewlines(expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists11() {
		String input, expected, actual;
		input = "<html>\n" +
				"<ul><li>\\1</li><li>\\2</li></ul>\n" +
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"* \\1\n" + 
				"* \\2\n" +
				"\n\n" + 
				"</html>"; 
		actual = tester.removeUnnecessaryNewlines(expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLists12() {
		String input, expected, actual;
		input = "<html>\n" +
				"<ol><li>h6. <span><span>Ensure that the target server/application is up and running and you are able to connect successfully.</span></span></li><li>h6. <span><span>For Inbound process failure, place the appropriate interface file(s) in the source</span></span>\n" + 
				"h6. <span><span>directory \"/infodata/as2/bonver/in\" in EAIP2. The maestro job will run automatically and process the file(s).</span></span></li><li>h6. <span><span>For Outbound process failure due to Source, ensure that there are data files in the appropriate source location &amp; there</span></span>\n" + 
				" <span><span>was a trigger file generated. Ask the Source Team to drop off the data files and the appropriate trigger file in /EAI/STP/data/OUT directory in EAIP2 for the process to run automatically. A new event would be created as part of this process.</span></span></li><li>h6. <span><span>For Outbound process failure due to AS2, troubleshoot and resolve the AS2 issue and re-queue the file(s) in the appropriate AS2 out directory /infodata/as2/bonver/out in GFS with .out extension to filename. Ensure that the persistent workorder is running in memory.</span></span></li></ol>\n" + 
				"</html>";
		expected = "<html>\n" +
				"\n" +
				"# h6. Ensure that the target server/application is up and running and you are able to connect successfully.\n" + 
				"# h6. For Inbound process failure, place the appropriate interface file(s) in the source directory \"/infodata/as2/bonver/in\" in EAIP2. The maestro job will run automatically and process the file(s).\n" + 
				"# h6. For Outbound process failure due to Source, ensure that there are data files in the appropriate source location &amp; there was a trigger file generated. Ask the Source Team to drop off the data files and the appropriate trigger file in /EAI/STP/data/OUT directory in EAIP2 for the process to run automatically. A new event would be created as part of this process.\n" + 
				"# h6. For Outbound process failure due to AS2, troubleshoot and resolve the AS2 issue and re-queue the file(s) in the appropriate AS2 out directory /infodata/as2/bonver/out in GFS with .out extension to filename. Ensure that the persistent workorder is running in memory.\n" + 
				"\n" + 
				"\n" +
				"</html>";
		actual = tester.convertLists(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetListDelim() {
		String current = "##";
		ListConverter.ListType type = ListConverter.ListType.getListType("ul");
		String expected = "##*";
		String actual = tester.getListDelim(type, current);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		current = "*";
		expected = "**";
		actual = tester.getListDelim(type, current);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		type = ListConverter.ListType.getListType("ol");
		expected = "*#";
		actual = tester.getListDelim(type, current);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}


	public void testTransformListToString() {
		String input = "<ul>\n" + 
				"<li>\n" + 
				"abc</li>\n" + 
				"<li>\n" + 
				"def</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"ghi</li>\n" + 
				"<li>\n" + 
				"hij</li>\n" + 
				"</ul>\n" + 
				"<li>\n" + 
				"klm</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"nop</li>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"qrs</li>\n" + 
				"</ul>\n" + 
				"<li>\n" + 
				"tuv</li>\n" + 
				"</ul>\n" + 
				"</ul>\n";
		String expected = "\n** ghi\n" +
				"** hij";
		Element root = tester.getRootElement(input, false);
		Element el = root.element("ul");
		String current = "*";
		ListConverter.ListType type = ListConverter.ListType.UNORDERED;
		String actual = tester.transformListToString(el, type, current);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "\n** nop\n" +
				"*** qrs\n" +
				"** tuv";
		el = null;
		el = (Element) root.elements("ul").get(1);
		actual = tester.transformListToString(el, type, current);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testRemoveUnnecessaryNewlines() {
		String input, expected, actual;
		input = "<html>\n" +
				"* abc\n" +
				"* def \n" +
				"\n" +
				"\n" +
				"** ghi\n" +
				"\n" +
				"</html>";
		expected = "<html>\n" +
				"* abc\n" +
				"* def \n" +
				"** ghi\n" +
				"\n" +
				"</html>";
		actual = tester.removeUnnecessaryNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = expected;
		actual = tester.removeUnnecessaryNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"* abc\n" +
				"* def \n" +
				"\n" +
				"\n" +
				"\n" +
				"* ghi\n" +
				"\n" +
				"*Bold*\n" +
				"</html>";
		expected = "<html>\n" +
				"* abc\n" +
				"* def \n" +
				"* ghi\n" +
				"\n" +
				"*Bold*\n" +
				"</html>";
		actual = tester.removeUnnecessaryNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"* abc\n" +
				"*# def \n" +
				"\n" +
				"\n" +
				"\n" +
				"# ghi\n" +
				"\n" +
				"h1. Header\n" +
				"</html>";
		expected = "<html>\n" +
				"* abc\n" +
				"*# def \n" +
				"# ghi\n" +
				"\n" +
				"h1. Header\n" +
				"</html>";
		actual = tester.removeUnnecessaryNewlines(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	private String cleanWithCleanConverter(String input) {
		CleanConverter cleaner = new CleanConverter();
		return cleaner.clean(input);
	}


}
