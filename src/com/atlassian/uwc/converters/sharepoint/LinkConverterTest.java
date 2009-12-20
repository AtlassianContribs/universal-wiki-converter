package com.atlassian.uwc.converters.sharepoint;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;

import com.atlassian.uwc.converters.sharepoint.LinkConverter.Link;
import com.atlassian.uwc.ui.Page;

public class LinkConverterTest extends TestCase {

	LinkConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new LinkConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertExternalLinks() {
		String input = "<html>" +
				"before <a href=\"http://www.google.com\">Google</a> after" +
				"</html>";
		String expected = "<html>before [Google|http://www.google.com] after</html>";
		String actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertInternalLinks() {
		//internal link 
		String input = "<html>" +
				"<a href=\"/test%20wiki/Test%20Page%2042.aspx\">Test Page 42</a>" +
				"</html>";
		String expected =  "<html>" + //FIXME
				"[Test Page 42|testwiki2:Test Page 42]" +
						"</html>";
		String actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetNextReplacement() {
		String input = "<html>" +
				"<p>Some text " +
				"<a href=\"http://abc.com\">A link</a>" +
				"Some more text" +
				" Some <b>bold</b> text" +
				"A <b>" +
				"<a href=\"http://www.google.com\">" +
				"bold link" +
				"</a>" +
				"</b>" +
				"</p>" +
				"</html>";
		Element root = tester.getRootElement(input, false);
		String expected = "[A link|http://abc.com]";
		List actualList = tester.getNextReplacement(root);
		String actual = concatList(actualList);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
		"<p>Some text " +
		"[A link|http://abc.com]" +
		"Some more text" +
		" Some <b>bold</b> text" +
		"A <b>" +
		"<a href=\"http://www.google.com\">" +
		"bold link" +
		"</a>" +
		"</b>" +
		"</p>" +
		"</html>";
		root = null;
		root = tester.getRootElement(input, false);
		expected = 	"[bold link|http://www.google.com]";
		actualList = tester.getNextReplacement(root);
		actual = concatList(actualList);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "<html>" +
		"<p>Some text " +
		"[A link|http://abc.com]" +
		"Some more text" +
		" Some <b>bold</b> text" +
		"A <b>" +
		"[bold link|http://www.google.com]" +
		"</b>" +
		"</p>" +
		"</html>";
		root = tester.getRootElement(input, false);
		actualList = tester.getNextReplacement(root);
		assertNull(actualList);

	}
	
	public static String concatList(List actualList) {
		String output = "";
		for (int i = 0; i < actualList.size(); i++) {
			Node node = (Node) actualList.get(i);
			output += node.asXML();
		}
		return output;
	}

	public void testCreateInternalLink() {
		//internal
		String input = "/test%20wiki/How%20To%20Use%20This%20Wiki%20Library.aspx";
		String expected = "[test|testwiki2:How To Use This Wiki Library]";
		String alias = "test";
		String actual = tester.createLink(input, alias, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//external
		input = "http://testing";
		expected = "[test|http://testing]";
		actual = tester.createLink(input, alias, null);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//attachments
		String attdir = "http://testing";
		tester.setAttachmentDirectory(attdir);

		input = "/Shared%20Documents/certmemo.pdf";
		expected = "[test|" + attdir + input + "]";
		actual = tester.createLink(input, alias, "");
		assertNotNull(actual);
		assertEquals(expected, actual);

		//anchor
		input = "#";
		String name = "AnchorName";
		expected = "{anchor:" + name + "}";
		actual = tester.createLink(input, alias, name);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//toanchor
		input = "#Lorem Ipsum 2";
		expected = "[test|" + input + "]";
		actual = tester.createLink(input, alias, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetLinkType() {
		//attachment
		String href = "/Shared%20Documents/certmemo.pdf";
		Link expected = LinkConverter.Link.ATTACHMENT;
		Link actual = tester.getLinkType(href, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//anchor
		href = "#";
		String name = "AnchorName";
		expected = LinkConverter.Link.ANCHOR;
		actual = tester.getLinkType(href, name);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//toanchor
		href = "#Lorem Ipsum 2";
		expected = LinkConverter.Link.TO_ANCHOR;
		actual = tester.getLinkType(href, null);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//external
		href = "http://www.google.com";
		expected = LinkConverter.Link.EXTERNAL;
		actual = tester.getLinkType(href, "");
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//wikipage
		href = "/my%20test%20wiki/Test%20Page%2072.aspx";
		expected = LinkConverter.Link.WIKIPAGE;
		actual = tester.getLinkType(href, null);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetAttachmentLink() {
		String attdir = "http://testing";
		tester.setAttachmentDirectory(attdir);

		String input = "/Shared%20Documents/certmemo.pdf";
		String expected = attdir + input;
		String actual = tester.getAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		attdir = "http://testing/";
		tester.setAttachmentDirectory(attdir);
		actual = tester.getAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "certmemo.pdf";
		expected = attdir + input;
		actual = tester.getAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		attdir = "http://testing";
		tester.setAttachmentDirectory(attdir);
		actual = tester.getAttachmentLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	public void testGetAnchorLink() {
		String aname = "AnchorName";
		String expected = "{anchor:" + aname + "}";
		Text actualText = tester.getAnchorLink(aname);
		assertNotNull(actualText);
		String actual = actualText.getText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetWikiPageLink() {
		String input = "/test%20wiki/Test%20Page%2072.aspx";
		String expected = "testwiki2:Test Page 72";
		String actual = tester.getWikipageLink(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLinks2() {
		String input = "<html>For Link Testing. We\'re going to link to this from <a href=\"/test%20wiki/How%20To%20Use%20This%20Wiki%20Library.aspx\">another page</a>.\n" + 
				"Here\'s an <a href=\"/Shared%20Documents/certmemo.pdf\">attachment</a>\n" + 
				"And apparently, there\'s an internal link syntax too: [[Home]]</html>";
		tester.setAttachmentDirectory("");
		String expected = "<html>For Link Testing. We\'re going to link to this from " +
				"[another page|testwiki2:How To Use This Wiki Library]" +
				".\n" + 
				"Here\'s an " +
				"[attachment|/Shared%20Documents/certmemo.pdf]" +
				"\n" + 
				"And apparently, there\'s an internal link syntax too: [[Home]]</html>";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html><font size=\"2\">\n" + 
				"<a href=\"http://www.google.com\" title=\"Search Engine Extraordinaire\">\n" + 
				"Google</a>\n" + 
				"</font></html>";
		expected = "<html><font size=\"2\">\n" + 
				"[Google|http://www.google.com]\n" + 
				"</font></html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testRemoveJTidyNewlines() {
		String input = "<html>" +
				"<a href=\"http://www.google.com\" shape=\"rect\">Lorem Ipsum 2 in\n" + 
				"page</a>" +
				"</html>";
		String expected = "<html>[Lorem Ipsum 2 in page|http://www.google.com]</html>";
		String actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLinks3() {
		String input, expected, actual;
		input = "<html>" +
				"<font size=\"5\"><font size=\"2\">\n" + 
				"<a id=\"Lorem Ipsum 2\" href=\"#\" name=\"Lorem Ipsum 2\" shape=\"rect\">(anchor here)\n" + 
				"\n" + 
				"<font size=\"5\"><font size=\"2\"><font size=\"7\">Lorem Ipsum 2</font>\n" + 
				"</font></font></font></font>\n" + 
				"</html>";
		
		expected = "<html>" +
				"<font size=\"5\"><font size=\"2\">\n" + 
				"<a id=\"Lorem Ipsum 2\" href=\"#\" name=\"Lorem Ipsum 2\" shape=\"rect\"/>(anchor here)\n" + 
				"\n" + 
				"<font size=\"5\"><font size=\"2\"><font size=\"7\">Lorem Ipsum 2</font>\n" + 
				"</font></font></font></font>\n" + 
				"</html>";
		actual = tester.cleanUnclosedAnchorLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "<html>" +
				"<font size=\"5\"><font size=\"2\">\n" +
				"{anchor:Lorem Ipsum 2} (anchor here)\n" + 
				"\n" + 
				"<font size=\"5\"><font size=\"2\"><font size=\"7\">Lorem Ipsum 2</font>\n" + 
				"</font></font></font></font>\n" + 
				"</html>";
		actual = tester.convertLinks(expected);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testConverLinks4() {
		String input, expected, actual;
		input = "<html><a id=\"OLE_LINK1\" name=\"OLE_LINK1\" shape=\"rect\"/></html>";
		expected = "<html>{anchor:OLE_LINK1}</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testConvertLinks5() { //font effects in link alias have to be moved outside the link
		String input = "<html>\n" +
				"<font face=\"Arial\">*_xxxxxx_*xxxxxx</font>" +
				"<a href=\"xxxxxx\" shape=\"rect\">" +
					"<font face=\"Arial\">xxxxxx</font>" +
				"</a>\n" +
				"</html>";
		String expected = "<html>\n" +
				"<font face=\"Arial\">*_xxxxxx_*xxxxxx</font>" +
				"<font face=\"Arial\">" +
					"[xxxxxx|/xxxxxx]" +
				"</font>\n" +
				"</html>";
		String actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertLinks6() { //using an image as a link alias
		String input = "<html>\n" +
				"<a href=\"http://abc.com\" shape=\"rect\">" +
					"<img src=\"image.jpg\" align=\"center\"/>" +
				"</a>\n" +
				"</html>";
		String expected = "<html>\n" +
				"[<img src=\"image.jpg\" align=\"center\"/>|" +
				"http://abc.com]\n" +
				"</html>";
		String actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//and just to double check
		SimpleImageConverter imager = new SimpleImageConverter();
		imager.setAttachmentDirectory("http://images.com/");
		input = actual;
		expected = "<html>\n" +
				"[!http://images.com/image.jpg|align=\"center\"!|" +
				"http://abc.com]\n" +
				"</html>";
		actual = imager.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertLinks7() {
		String input, expected, actual;
		input = "<html><a class=\"ms-wikilink\" href=\"/test%20wiki/Page%20With%20Bad%20;Char.aspx\" shape=\"rect\">Page With Bad ;Char</a></html>";
		expected = "<html>[Page With Bad Char|testwiki2:Page With Bad Char]</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testExtractElement() {
		String input = "<html><a href=\"abc\"><font att=\"val\">abcdef</font></a></html>";
		String expected = "<html><font att=\"val\"><a href=\"abc\">abcdef</a></font></html>";
		
		Element root = tester.getRootElement(input, false);
		Element el = root.element("a").element("font");
		tester.extractElement(el);
		String actual = root.asXML();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testMailToLinks() {
		String input, expected, actual;
		
		input = "<html>\n" +
		"<a href=\"mailto:a@b.com\">a@b.com</a>\n" +
		"</html>";
		expected = "<html>\n" +
				"[a@b.com|mailto:a@b.com]\n" +
				"</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"<a href=\"mailto:a@b.com\" shape=\"rect\"><span style=\"FONT-WEIGHT: normal\">" +
				"<a href=\"mailto:a@b.com\" shape=\"rect\"><font face=\"Trebuchet MS\" size=\"2\">" +
				"a@b.com</font></a></span></a>\n" +
				"</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>\n" +
				"<span>" +
					"<font face=\"trebuchet ms\" size=\"2\">" +
						"lars oscarsson at" +
					"</font>" +
					"<a href=\"mailto:lars.oscarsson@bonver.com\" shape=\"rect\">" +
						"<span style=\"font-weight: normal\">" +
							"<a href=\"mailto:lars.oscarsson@bonver.com\" shape=\"rect\">" +
								"<font face=\"trebuchet ms\" size=\"2\">" +
									"lars.oscarsson@bonver.com" +
								"</font>" +
							"</a>" +
						"</span>" +
					"</a>" +
				"</span>\n" +
				"</html>";
		expected = "<html>\n" +
				"<span>" +
					"<font face=\"trebuchet ms\" size=\"2\">" +
						"lars oscarsson at" +
					"</font>" +
					" [lars.oscarsson@bonver.com|mailto:lars.oscarsson@bonver.com]" +
				"</span>\n" +
				"</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<html>" +
				"<a href=\"mailto:Colin_Perren@paramount.com\" shape=\"rect\">" +
				"<font face=\"Trebuchet MS\" size=\"2\">Colin_Perren@paramount.com</font></a>" +
				"</html>";
		expected = "<html>" +
				"<font face=\"Trebuchet MS\" size=\"2\">" +
				"[Colin_Perren@paramount.com|mailto:Colin_Perren@paramount.com]" +
				"</font>" + 
				"</html>";
		actual = tester.convertLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Abc Def at<span>[abc.com|mailto:abc@def.com]</span>";
		expected = "Abc Def at<span> [abc.com|mailto:abc@def.com]</span>";
		actual = tester.addWSAsNecessary(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
