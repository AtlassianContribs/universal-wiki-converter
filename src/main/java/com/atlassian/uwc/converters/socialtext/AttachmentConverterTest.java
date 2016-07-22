package com.atlassian.uwc.converters.socialtext;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	private static final String ATT_DIR = "sampleData/socialtext/attachments";
	AttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new AttachmentConverter();
		tester.setAttachmentDirectory(ATT_DIR);
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String testpage = "sampleData/socialtext/junit-tests/foobar.txt";
		Page page = new Page(new File(testpage));
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		assertFalse(page.getAttachments().isEmpty());
		Set<File> actual = page.getAttachments();
		assertEquals(2, actual.size());
		Vector<File> attachments = new Vector<File>();
		attachments.addAll(actual);
		File file1 = attachments.get(0);
		File file2 = attachments.get(1);
		File foo, bar;
		if (file1.getName().startsWith("foo")) { //sets don't maintain order
			foo = file1;
			bar = file2;
		}
		else {
			foo = file2;
			bar = file1;
		}
		
		String pagedirstring = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/socialtext/attachments/junit-tests";
		assertEquals("foo.jpg", foo.getName());
		assertEquals("bar.xls", bar.getName());
		assertEquals(pagedirstring + "/20090103010101-0-12345/foo.jpg", foo.getPath());
		assertEquals(pagedirstring + "/20090102010101-0-12345/bar.xls", bar.getPath());
		
	}

	public void testHasAttachments() {
		String testpage = "sampleData/socialtext/junit-tests/foobar.txt";
		Page page = new Page(new File(testpage));
		String parent = page.getFile().getParentFile().getName();
		assertTrue(tester.hasAttachments(ATT_DIR, parent));
		assertFalse(tester.hasAttachments("sampleData/", parent));
		assertFalse(tester.hasAttachments(ATT_DIR, "SampleSocialtext-InputFilter"));
	}

	public void testGetPageDir() {
		String testpage = "sampleData/socialtext/junit-tests/foobar.txt";
		Page page = new Page(new File(testpage));
		String parent = page.getFile().getParentFile().getName();
		File actual = tester.getPageDir(ATT_DIR, parent);
		assertNotNull(actual);
		assertEquals(ATT_DIR + "/junit-tests", actual.getPath());
	}

	public void testGetSortedTimestampDirectories() {
		File pagedir = new File(ATT_DIR + "/junit-tests");
		assertTrue(pagedir.exists());
		assertTrue(pagedir.isDirectory());
		
		TreeSet<String> actual = tester.getSortedTimestampDirectories(pagedir);
		assertNotNull(actual);
		assertEquals(4, actual.size());
		
		Vector<String> actVector = new Vector<String>();
		actVector.addAll(actual);
		
		String timestamp1 = actVector.get(0);
		String timestamp2 = actVector.get(1);
		String timestamp3 = actVector.get(2);
		String timestamp4 = actVector.get(3);
		assertEquals("20090104010101-0-12345", timestamp1);
		assertEquals("20090103010101-0-12345", timestamp2);
		assertEquals("20090102010101-0-12345", timestamp3);
		assertEquals("20090101010101-0-12345", timestamp4);
		
	}

	public void testGetAttachments() {
		String pagedirstring = ATT_DIR + "/junit-tests";
		File pagedir = new File(pagedirstring);
		String timestamp = "20090103010101-0-12345";
		pagedirstring = "/Users/laura/Code/Subversion/uwc-current/devel/" + pagedirstring;
		String expName = "foo.jpg";
		String expected = pagedirstring + "/" + timestamp + "/" + expName;
		File[] actual = tester.getAttachments(pagedir, timestamp);
		assertNotNull(actual);
		assertEquals(1, actual.length);
		assertEquals(expName, actual[0].getName());
		assertEquals(expected, actual[0].getPath());
		
		actual = null;
		timestamp = "20090102010101-0-12345";
		expName = "bar.xls";
		expected = pagedirstring + "/" + timestamp + "/" + expName;
		actual = tester.getAttachments(pagedir, timestamp);
		assertNotNull(actual);
		assertEquals(1, actual.length);
		assertEquals(expName, actual[0].getName());
		assertEquals(expected, actual[0].getPath());
		
		actual = null;
		timestamp = "20090101010101-0-12345";
		expName = "foo.jpg";
		expected = pagedirstring + "/" + timestamp + "/" + expName;
		actual = tester.getAttachments(pagedir, timestamp);
		assertNotNull(actual);
		assertEquals(1, actual.length);
		assertEquals(expName, actual[0].getName());
		assertEquals(expected, actual[0].getPath());
		
	}

	public void testIsDeleted() {
		String pagedirstring = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/socialtext/attachments/junit-tests";
		//not deleted
		File file = new File(pagedirstring + "/" + "20090101010101-0-12345/foo.jpg");
		assertFalse(tester.isDeleted(file));
		
		//deleted
		file = new File(pagedirstring + "/" + "20090104010101-0-12345/deleted.txt");
		assertTrue(tester.isDeleted(file));
	}
}
