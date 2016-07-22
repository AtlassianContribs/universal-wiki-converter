package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ImageConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvertImages() { //simple
		String input, expected, actual;
		input = "{image:img.png}\n" + 
				"";
		expected = "!img.png!\n";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages2() { //width/height shorthand
		String input, expected, actual;
		input = "{image:img.png|30}\n" + 
				"{image:img.png| |20}\n" + 
				"{image:img.png|30|20}\n" + 
				"";
		expected = "!img.png|height=30!\n" + 
				"!img.png|width=20!\n" + 
				"!img.png|height=30,width=20!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages3() { //alt
		String input, expected, actual;
		input = "{image:cow.jpg|alt=A Cow. Moo}\n" + 
				"";
		expected = "!cow.jpg|alt=A Cow. Moo!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages4() { //width/height explicit
		String input, expected, actual;
		input = "width/height: {image:gamesigns.jpg|height=128|width=128}\n" +
				"";
		expected = "width/height: !gamesigns.jpg|height=128,width=128!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages5() { //align
		String input, expected, actual;
		input = "align: {image:cow.jpg|align=right}\n" + 
				"halign: {image:cow.jpg|halign=floatleft}\n" + 
				"";
		expected = "align: !cow.jpg|align=right!\n" + 
				"halign: !cow.jpg|halign=floatleft!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages6() { //document
		String input, expected, actual;
		input = "document: {image:img.png|document=XWiki.XWikiSyntax}\n" + 
				"";
		expected = "document: !XWiki:XWikiSyntax^img.png!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages7() { //link
		String input, expected, actual;
		input = "link: {image:img.jpg|link=}\n" + 
				"";
		expected = "link: !img.jpg!\n" + 
				"";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertImages8() { //fromIncludingDoc
		String input, expected, actual;
		input = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
				"<parent>Main.Abc</parent>\n" + 
				"<content>This sample is for converting image syntax.\n" + 
				"\n" + 
				"fromIncludingDoc: {image:img.png|fromIncludingDoc=}\n" +
				"\n" + 
				"</content></xwikidoc>";
		expected = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
				"<parent>Main.Abc</parent>\n" + 
				"<content>" +
				"This sample is for converting image syntax.\n" + 
				"\n" + 
				"fromIncludingDoc: !Main:Abc^img.png!\n" + 
				"\n" + 
				"</content></xwikidoc>";
		actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateConfluenceParam1() {
		String input, expected, actual;
		input = "100";
		int index = 1;
		expected = "height=100";
		actual = tester.createConfluenceParam(index, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateConfluenceParam2() {
		String input, expected, actual;
		input = "100";
		int index = 2;
		expected = "width=100";
		actual = tester.createConfluenceParam(index, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateConfluenceParam3() {
		String input, expected, actual;
		input = "height=100";
		expected = "height=100";
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateConfluenceParam4() {
		String input, expected, actual;
		input = "width=100";
		expected = "width=100";
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateConfluenceParam5() {
		String input, expected, actual;
		input = "alt=Something useful or witty";
		expected = input;
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateConfluenceParam6() {
		String input, expected, actual;
		input = "align=top";
		expected = input;
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testCreateConfluenceParam7() {
		String input, expected, actual;
		input = "halign=floatleft";
		expected = input;
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateConfluenceParam9() {
		String input, expected, actual;
		input = "link=";
		expected = "";
		actual = tester.createConfluenceParam(1, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testFindParentPage() {
		String input, expected, actual;
		input = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
			"<parent>Main.Abc</parent>\n" + 
			"<content>This sample is for converting image syntax.\n" + 
			"\n" + 
			"fromIncludingDoc: {image:img.png|fromIncludingDoc=}\n" + 
			"</content></xwikidoc>";
		expected = "Main.Abc";
		actual = tester.findParentPage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
			"<parent>Virt:Main.Abc</parent>\n" + 
			"<content>This sample is for converting image syntax.\n" + 
			"\n" + 
			"fromIncludingDoc: {image:img.png|fromIncludingDoc=}\n" + 
			"</content></xwikidoc>";
		expected = "Virt:Main.Abc";
		actual = tester.findParentPage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "<content>This sample is for converting image syntax.\n" + 
			"\n" + 
			"fromIncludingDoc: {image:img.png|fromIncludingDoc=}\n" + 
			"</content></xwikidoc>";
		actual = tester.findParentPage(input);
		assertNull(actual);
	}
	
	public void testAddPageOrSpace1() { //document
		String param, expected, actual;
		param = "document=Xwiki.Something";
		String parent = null;
		String image = "img.png";
		expected = "Xwiki:Something^img.png";
		actual = tester.addPageOrSpace(image, parent, param);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddPageOrSpace2() { //fromIncludingDoc
		String param, expected, actual;
		param = "fromIncludingDoc=";
		String parent = "Something.Abc";
		String image = "img.png";
		expected = "Something:Abc^img.png";
		actual = tester.addPageOrSpace(image, parent, param);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddPageOrSpace3() { //nothing
		String param, expected, actual;
		param = "link=";
		String parent = null;
		String image = "abc.jpg";
		expected = "abc.jpg";
		actual = tester.addPageOrSpace(image, parent, param);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testAddPageOrSpace4() { //handle virtual wiki
		String param, expected, actual;
		param = "fromIncludingDoc=";
		String parent = "Virtual:Something.Abc";
		String image = "img.png";
		expected = "VirtualSomething:Abc^img.png";
		actual = tester.addPageOrSpace(image, parent, param);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testImagesNextToThings() {
		String input, expected, actual;
		Page page = new Page(null);
		input = "Text{image:cow.jpg}Something else\n" + 
				"";
		expected = "Text !cow.jpg! Something else\n" + 
				"";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Before{image:cow.jpg}\n" +
				"\n" +
				"{image:abc.png}After\n" + 
				"";
		expected = "Before !cow.jpg!\n" +
				"\n" +
				"!abc.png! After\n" + 
				"";
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
