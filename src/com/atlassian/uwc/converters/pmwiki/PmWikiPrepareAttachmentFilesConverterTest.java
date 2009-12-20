package com.atlassian.uwc.converters.pmwiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class PmWikiPrepareAttachmentFilesConverterTest extends TestCase {

	PmWikiPrepareAttachmentFilesConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new PmWikiPrepareAttachmentFilesConverter();
		tester.setAttachmentDirectory("sampleData/pmwiki/junit_resources/uploads");
	}

	public void testConvert() {
		String path = "sampleData/pmwiki/junit_resources/Main/TestingAttachments";
		Page page = getPage(path);
		tester.convert(page);
		assertNotNull(page.getAttachments());
		assertTrue(page.getAttachments().size() > 0);
		Set<File> attachments = page.getAttachments();
		int found = 0;
		for (File file : attachments) {
			if (file.getName().equals("cow.jpg")) found++;
		}
		assertTrue(found > 0);
	}

	/* Convenience Methods */
	private Page getPage(String path) {
		File file = new File(path);
		Page page = new Page(file);
		page.setPath(file.getPath());
		page.setName(file.getName());
		page.setOriginalText(readfile(file));
		return page;
	}
	
	private String readfile(File file) {
		String filestring = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				filestring += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filestring;
	}

}
