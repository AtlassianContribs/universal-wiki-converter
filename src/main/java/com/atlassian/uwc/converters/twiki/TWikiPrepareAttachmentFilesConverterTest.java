package com.atlassian.uwc.converters.twiki;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TWikiPrepareAttachmentFilesConverterTest extends TestCase {

	TWikiPrepareAttachmentFilesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TWikiPrepareAttachmentFilesConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testAddAttachmentsToPage() {
		String title = "SampleTWiki-InputAttachments2.txt";
		File file = new File("sampleData/twiki/" + title);
		Page page = new Page(file);
		
		tester.setAttachmentDirectory("/Users/laura/Code/Subversion/uwc-current/devel/sampleData/twiki/pub");
		
		tester.addAttachmentsToPage(page);
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File actFile = (File) iter.next();
			assertNotNull(actFile);
			if (actFile.getName().equals(".svn")) continue;
			assertEquals("cow.jpg", actFile.getName());
		}
	}

}
