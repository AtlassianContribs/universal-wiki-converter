package com.atlassian.uwc.converters.moinmoin;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class MoinMoinAttachmentConverterTest extends TestCase {

	MoinMoinAttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new MoinMoinAttachmentConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvert() {
		String input, expected, actual;
		tester.setAttachmentDirectory("/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/moinmoin/sample/attachmenttest/pages");
		input = "Inline image\n" + 
				"{{attachment:cow.jpg}}\n" + 
				"\n" + 
				"Link to attachment:\n" + 
				"\n" + 
				"[[attachment:cow.jpg]]\n" +
				"[[attachment:Somepage/image.png]]\n" + 
				"";
		expected = "Inline image\n" + 
				"!cow.jpg!\n" + 
				"\n" + 
				"Link to attachment:\n" + 
				"\n" + 
				"[^cow.jpg]\n" +
				"[Somepage^image.png]" + 
				"\n" + 
				"";
		Page page = new Page(null);
		page.setName("TestPage.txt");
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		assertNotNull(page.getAttachments());
		assertFalse(page.getAttachments().isEmpty());
	}

}
