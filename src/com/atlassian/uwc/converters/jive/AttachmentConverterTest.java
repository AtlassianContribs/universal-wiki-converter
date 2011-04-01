package com.atlassian.uwc.converters.jive;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Attachment;
import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	AttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = new Properties();
	protected void setUp() throws Exception {
		tester = new AttachmentConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.setAttachmentDirectory("/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/att-sample");
		props.put("attachmentdata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_attachments.txt");
		tester.setProperties(props);
	}

	public void testAttach() {
		String input;
		input = "{jive-export-meta:id=9009|version=1|type=DOC|containertype=14|containerid=201}\n" + 
				"{user:username}\n" + 
				"{timestamp:1234567890000}\n" + 
				"{{title: SampleJive-InputAtt }}\n" + 
				"<body><p/></body>";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		assertFalse(page.getAttachments().isEmpty());
		Set<Attachment> actual = page.getAllAttachmentData();
		assertEquals(3, actual.size());
		for (Attachment attachment : actual) {
			String actName = attachment.getName();
			assertTrue(actName.equals("cow.jpg") || actName.equals("doublefacepalm.jpg") || actName.equals("test.txt"));
			File actFile = attachment.getFile();
			assertTrue(actFile.exists());
		}
	}
	
}
