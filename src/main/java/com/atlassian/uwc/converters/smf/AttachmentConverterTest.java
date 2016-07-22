package com.atlassian.uwc.converters.smf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	private static final String OUT = "sampledata/smf/junit_resources/attachments/";
	AttachmentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new AttachmentConverter();
		tester.setAttachmentDirectory(OUT);
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		Page page = new Page(new File("sampleData/smf/junit_resources/attachments2/SampleSmf-InputAttach_re16.txt"));
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		Set<File> attachments = page.getAttachments();
		assertFalse(attachments.isEmpty());
		assertEquals(2, attachments.size());
		
		String name1 = "1_cow.jpg";
		String name2 = "2_cow.jpg_thumb.jpg";
		String parent = "sampleData/smf/junit_resources/attachments2/attachments/";

		for (File file : attachments) {
			assertNotNull(file);
			assertTrue(file.exists());
			assertTrue(file.isFile());
			assertTrue(file.getName().equals(name1)||file.getName().equals(name2));
			assertTrue(file.getAbsolutePath().endsWith(parent+name1)||
					file.getAbsolutePath().endsWith(parent+name2));
		}
		FileUtils.deleteDir(new File(parent));
	}
	
	public void testConvert_Category() {
		Page page = new Page(new File("sampleData/smf/junit_resources/attachments2/testatt_cat6.txt"));
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		assertTrue(page.getAttachments().isEmpty());
	}

	public void testGetAttachmentPaths() {
		String input = "abc.jpg,test.gif";
		Vector<String> actual = tester.getAttachmentPaths(input);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		String exp1 = OUT + "abc.jpg";
		String exp2 = OUT + "test.gif";
		String act1 = actual.get(0);
		assertNotNull(act1);
		assertEquals(exp1, act1);
		String act2 = actual.get(1);
		assertNotNull(act2);
		assertEquals(exp2, act2);
	}

	public void testAttach() {
		Vector<String> input = new Vector<String>();
		String name1 = "foo.jpg";
		String name2 = "bar.jpg";
		input.add(OUT + name1);
		input.add(OUT + name2);
		Page page = new Page(null);
		assertTrue(page.getAttachments().isEmpty());
		
		tester.attach(page, input);
		Set<File> attachments = page.getAttachments();
		assertFalse(attachments.isEmpty());
		assertEquals(2, attachments.size());
		for (File file : attachments) {
			assertNotNull(file);
			assertTrue(file.exists());
			assertTrue(file.isFile());
			assertTrue(file.getName().equals(name1)||file.getName().equals(name2));
		}
	}

	public void testCreateTmpPaths() {
		String tmp = "/Users/laura/tmp/";
		tester.setAttachmentDirectory(tmp);
		String input = "abc.jpg,test.gif";
		Vector<String> actual = tester.createTmpPaths(input, tmp);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		String exp1 = tmp + "attachments/abc.jpg";
		String exp2 = tmp + "attachments/test.gif";
		String act1 = actual.get(0);
		assertNotNull(act1);
		assertEquals(exp1, act1);
		String act2 = actual.get(1);
		assertNotNull(act2);
		assertEquals(exp2, act2);

	}
	
	public void testCopyToTmp() throws IOException {
		Vector<String> from = new Vector<String>();
		Vector<String> to = new Vector<String>();
		String in1 = "sampleData/smf/junit_resources/attachments/bar.jpg";
		String in2 = "sampleData/smf/junit_resources/attachments/1_1369c5b06a8394704b9bd20d8e6e9191eda82494";
		from.add(in1);
		from.add(in2);
		String out1 = "/Users/laura/tmp/attachments/abc.jpg";
		String out2 = "/Users/laura/tmp/attachments/def.jpg";
		to.add(out1);
		to.add(out2);
		File file1 = new File(out1);
		File file2 = new File(out2);
		assertFalse(file1.exists());
		assertFalse(file2.exists());
		
		tester.copyToTmp(from, to);
		
		assertTrue(file1.exists());
		assertTrue(file2.exists());

		String command = "diff " + in1 + " " + out1;
		Process child = Runtime.getRuntime().exec(command);
		InputStream in = child.getInputStream();
        int c;
        String s = "";
        while ((c = in.read()) != -1) {
            s += ((char)c);
        }
        in.close();
        assertEquals("", s);
        command = "diff " + in2 + " " + out2;
		Process child2 = Runtime.getRuntime().exec(command);
		InputStream stream2 = child2.getInputStream();
        int c2;
        String s2 = "";
        while ((c2 = stream2.read()) != -1) {
            s2 += ((char)c2);
        }
        stream2.close();
        FileUtils.deleteDir(new File("/Users/laura/tmp/attachments/"));
	}
	
	public void testGetAttachmentsPaths_ProbBracketsColons() {
		Properties props = new Properties();
		props.setProperty("attachment-chars-remove", "[]:");
		tester.setProperties(props);
		String input = "ProblemFileHasBrackets[Colons:Arg]InIt369c5b06a8394704b9bd20d8e6e9191eda82494";
		Vector<String> actual = tester.getAttachmentPaths(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		String exp1 = OUT + "ProblemFileHasBracketsColonsArgInIt369c5b06a8394704b9bd20d8e6e9191eda82494";
		String act1 = actual.get(0);
		assertNotNull(act1);
		assertEquals(exp1, act1);
		
		//what about problem regex chars
		props.setProperty("attachment-chars-remove", "[:\\\\^-");
		tester.setProperties(props);
		input = "a-b^c[:\\\\def";
		actual = tester.getAttachmentPaths(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		exp1 = OUT + "abcdef";
		act1 = actual.get(0);
		assertNotNull(act1);
		assertEquals(exp1, act1);
		
	}
	public void testGetAttachmentsPaths_ProbWhitespace() {
		Properties props = new Properties();
		props.setProperty("attachment-chars-to-underscore", " .");
		tester.setProperties(props);
		String input = "ProblemFileHasWhite SpaceInIt369c5b06a8394704b9bd20d8e6e9191eda82494";
		Vector<String> actual = tester.getAttachmentPaths(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		String exp1 = OUT + "ProblemFileHasWhite_SpaceInIt369c5b06a8394704b9bd20d8e6e9191eda82494";
		String act1 = actual.get(0);
		assertNotNull(act1);
		assertEquals(exp1, act1);
	}

	public void testCustomDelim() {
		Page page = new Page(new File("sampleData/smf/junit_resources/delim/SampleSmf-InputAttach_re16.txt"));
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		Set<File> attachments = page.getAttachments();
		assertFalse(attachments.isEmpty());
		assertEquals(2, attachments.size());
		
		String name1 = "1_cow.jpg";
		String name2 = "2_cow.jpg_thumb.jpg";
		String parent = "sampleData/smf/junit_resources/delim/attachments/";

		for (File file : attachments) {
			assertNotNull(file);
			assertTrue(file.exists());
			assertTrue(file.isFile());
			assertTrue(file.getName().equals(name1)||file.getName().equals(name2));
			assertTrue(file.getAbsolutePath().endsWith(parent+name1)||
					file.getAbsolutePath().endsWith(parent+name2));
		}
		FileUtils.deleteDir(new File(parent));
	}
	
	public void testProblemAttachment() {
		Page page = new Page(new File("sampleData/smf/junit_resources/attachments2/SampleSmf-InputAttachProb_re17.txt"));
		assertTrue(page.getAttachments().isEmpty());
		tester.convert(page);
		Set<File> attachments = page.getAttachments();
		assertFalse(attachments.isEmpty());
		assertEquals(1, attachments.size());
		
		String name1 = "100_Foo + Bar.jpg";
		String parent = "sampleData/smf/junit_resources/attachments2/attachments/";

		for (File file : attachments) {
			assertNotNull(file);
			assertTrue(file.exists());
			assertTrue(file.isFile());
			assertTrue(file.getName().equals(name1));
			assertTrue(file.getAbsolutePath().endsWith(parent+name1));
		}
		FileUtils.deleteDir(new File(parent));
	}
	
}
