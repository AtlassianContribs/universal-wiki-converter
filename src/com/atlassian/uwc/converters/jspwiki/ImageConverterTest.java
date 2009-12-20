package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class ImageConverterTest extends TestCase {

	ImageConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	String jspAttachDirectory = "/Users/laura/Code/JSPWiki/web/www-data/jspwiki"; 
	String pagename = "AttachmentTestingPage";
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ImageConverter();
		tester.FILE_SEP = File.separator;
	}

	public void testConvertImages1() {
		String input = "[input]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}

	public void testConvertImages2() {
		String input = "[http://www.normal.com]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages3() {
		String input = "[http://www.normal.com/normal]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages4() {
		String input = "[http://www.normal.com/normal/]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}

	public void testConvertImages5() {
		String input = "[normal|alias]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}

	public void testConvertImages6() {
		String input = "[underline|]";
		String expected = input;
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages7() {
		String input = "[http://localhost:8083/JSPWiki/images/xmlCoffeeCup.png]";
		String expected = "!http://localhost:8083/JSPWiki/images/xmlCoffeeCup.png!";
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertImages8() {
		String input = "[alias|http://localhost:8083/JSPWiki/images/xmlCoffeeCup.png]";
		String expected = "!http://localhost:8083/JSPWiki/images/xmlCoffeeCup.png!";
		String actual = tester.convertImages(input);
		assertEquals(expected, actual);
	}
	
	public void testConvertImagesNonAttachPeriods() {
		String input, expected, actual;
		input = "[alias?" +
				"|AbcDefVs.GhiJkl]";
		expected = input;
		actual = tester.convertAttachments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetImageTypes() {
		String[] expected = {
			"JPEG",
			"JPG",
			"TIFF",
			"RAW",
		    "PNG",
		    "GIF",
		    "BMP",
		    "WDP",
		    "XPM",
		    "MrSID",
		    "SVG"
		};
		String[] actual = tester.getImageTypes();
		
		assertNotNull(actual);
		assertEquals(expected.length, actual.length);

		for (int i = 0; i < actual.length; i++) {
			String act = actual[i];
			assertNotNull(act);
			act = act.toUpperCase();
			boolean found = false;
			for (int j = 0; j < expected.length; j++) {
				String exp = expected[j];
				if (exp == null) continue;
				exp = exp.toUpperCase();
				if (act.equals(exp)) {
					found = true;
					expected[j] = null;
					break;
				}
				found = false;
			}
			assertTrue(act + " not found", found);
		}
	}
	
	public void testCreateOrString() {
		String[] input = { "A", "B" };
		String expected = "(?:A)|(?:B)";
		String actual = tester.createOrString(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String[] input2 = { "PNG", "GIF" };
		String shouldMatch = "http://lotsofstuff.com/image.png";
		String shouldntMatch = "http://lotsofstuff.com";
		String orString = tester.createOrString(input2);
		log.debug(orString);
		String regex = "(.*?)(?i)(" + orString + ")";
		Pattern p = Pattern.compile(regex);
		Matcher mShould = p.matcher(shouldMatch);
		assertTrue(mShould.find());
		Matcher mShouldNot = p.matcher(shouldntMatch);
		assertFalse(mShouldNot.find());
	}

	public void testAttachedNotImage() {
		String input = "[test.txt]";
		String expected = "[^test.txt]";
		String actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		input = "[alias|test.txt]";
		expected = "[alias|^test.txt]";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
		
		page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testAttachedImage() {
		//for testing simple attached files, same page
		String input = "[ed.jpeg]";
		String expected = "!ed.jpeg!";
		String actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		expected = "!ed.jpeg!";
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		input = "[alias|ed.jpeg]";
		expected = "!ed.jpeg!";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
		

		page = new Page(new File(""));
		page.setOriginalText(input);
		expected = "!ed.jpeg!";
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}

	public void testAttachedFromOtherFile() {
		//for testing attachments from another file
		String input = "[AttachmentPage/ed.txt]";
		String expected = "[AttachmentPage^ed.txt]";
		String actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
	

		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		input = "[alias|AttachPage/ed.txt]";
		expected = "[alias|AttachPage^ed.txt]";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);
		
		page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		//now images
		input = "[AttachmentPage/ed.jpg]";
		expected = "!AttachmentPage^ed.jpg!";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);

		page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		input = "[alias|AttachPage/ed.gif]";
		expected = "!AttachPage^ed.gif!";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);

		page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
		
		//uwc-346
		input = "blah blah blah\n" + 
				"[a link to another page attachment|awikipage/a wiki attachment.xls]" ; 
		expected = "blah blah blah\n" + 
				"[a link to another page attachment|awikipage^a wiki attachment.xls]";
		actual = tester.convertAttachments(input);
		assertEquals(expected, actual);

		page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertEquals(expected, actual);
	}
	
	public void testIsImage() {
		boolean expected = true;
		String input = ".jpg";
		boolean actual = tester.isImage(input);
		assertEquals(expected, actual);
		
		input = ".gif";
		actual = tester.isImage(input);
		assertEquals(expected, actual);
		
		expected = false;
		input = ".txt";
		actual = tester.isImage(input);
		assertEquals(expected, actual);
		
		input = ".zip";
		actual = tester.isImage(input);
		assertEquals(expected, actual);
		
	}
	
	public void testDontConvertExternalLinks() {
		String input = "External Link\n" +
				"[http://www.google.com]\n" +
				"[display|http://www.google.com]\n";
		String expected = input;
		String actual = tester.convertImages(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		actual = tester.convertAttachments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontConvertEscapingBrackets() {
		String input = "[[...]";
		String expected = input;
		String actual = tester.convertAttachments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontConvertPlugins() {
		String input = "[{INSERT com.ecyrd.jspwiki.plugin.RecentChangesPlugin}]";
		String expected = input;
		String actual = tester.convertAttachments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDontConvertFtpLinks() {
		String input, expected, actual;
		input = "[ftp://www.hp.com]\n" + 
				"[hp ftp site|ftp://www.hp.com]";
		expected = input;
		actual = tester.convertImages(input);
		actual = tester.convertAttachments(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testNotLink() {
		String input = "[";
		assertTrue(tester.notLink(input));
		
		input = "hello";
		assertFalse(tester.notLink(input));
		
		input = "{INSERT com";
		assertTrue(tester.notLink(input));
	}
	
	public void testGetAttachableFiles() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String confluenceSyntax = "A file: !ed.jpeg!";
		String expected = jspAttachDirectory + "/" + pagename + "-att/ed.jpeg-dir/1.jpeg";
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		int expectedSize = 1;
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
		
		//uwc-210
		confluenceSyntax = "Testing !!!) finden sich in dem neben dem Scanner liegenden Handbuch." +
				"\n" +
				"h2. Anmeldung am PC" +
				"\n" +
				"Ger?t, sofern erforderlich,  einschalten und als " +
				"Administrator (PW: geheim) mit der Option ?nur Arbeitsplatz? anmelden." +
				"\n" +
				"h2. Kopiersoftware ?ffnen\n" + 
				"!hilfe_diverse_006_01.gif!\n" + 
				"Auf dem Desktop (... Bildschirmfl?che)";
		
		expected = jspAttachDirectory + "/" + pagename + "-att/hilfe_diverse_006_01.gif-dir/1.gif";
		actual = null;
		actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
	}
		
	public void testGetAttachableFilesMany() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String confluenceSyntax = "!Wiki.png!\n" +
			"Not a file " +
			"!hummingbird.jpg!\n\n";
		String expected = jspAttachDirectory + "/" + pagename + "-att/ed.jpeg-dir/1.jpeg";

		String file1 = jspAttachDirectory + "/" + pagename + "-att/Wiki.png-dir/1.png";
		String file2 = jspAttachDirectory + "/" + pagename + "-att/hummingbird.jpg-dir/1.jpg";
		Vector<String> expectedFiles = new Vector();
		expectedFiles.add(file1);
		expectedFiles.add(file2);
		
		Vector<String> actualFiles = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actualFiles);
		
		int expectedSize = 2;
		assertEquals("size comparison: ", expectedSize, actualFiles.size());
		
		for (int i = 0; i < actualFiles.size(); i++) {
			String actualFile = actualFiles.get(i);
			String expectedFile = expectedFiles.get(i);
			assertEquals(expectedFile, actualFile);
		}

	}
	
	public void testGetAttachableFilesNonImage() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String confluenceSyntax = "How about a non-image file: [^ed.txt]";
		
		String expected = jspAttachDirectory + "/" + pagename + "-att/ed.txt-dir/1.txt";
		
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		int expectedSize = 1;
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
		
	}
	public void testGetAttachableFilesAlias() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String confluenceSyntax = "with alias: [alias|^ed.txt]";
		String expected = jspAttachDirectory + "/" + pagename + "-att/ed.txt-dir/1.txt";
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		int expectedSize = 1;
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
		
	}
	
	public void testGetAttachableFilesMultReferencesToSameFile() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String confluenceSyntax = "here's a file: !Wiki.png!\n" +
				"and here's another: !Wiki.png!\n" +
				"Are we done yet?\n";
		
		String expected = jspAttachDirectory + "/" + pagename + "-att/Wiki.png-dir/1.png";
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		int expectedSize = 1;
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
		
	}
	
	//should these return files? or not? See UWC-335
	public void testGetAttachableFilesRefersToAnotherPage() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String pagename = "CurrentPage"; //override field so that this more accurately reflects method usage
		String input = "an attachment: !AttachmentTestingPage^ed.jpeg!";
		
		String expected = jspAttachDirectory + "/" + this.pagename + "-att/ed.jpeg-dir/1.jpeg";
		Vector<String> actual = tester.getAttachableFiles(input, jspAttachDirectory, pagename);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		
		
		input = "not an image: [AttachmentTestingPage^ed.txt]";
		expected = jspAttachDirectory + "/" + this.pagename + "-att/ed.txt-dir/1.txt";
		actual = tester.getAttachableFiles(input, jspAttachDirectory, pagename);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		
		input = "with an alias: [alias|AttachmentTestingPage^ed.txt]";
		expected = jspAttachDirectory + "/" + this.pagename + "-att/ed.txt-dir/1.txt";
		actual = tester.getAttachableFiles(input, jspAttachDirectory, pagename);
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}
	
	public void testGetAttachableFilesSeveralVersions() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		//test getting a non 1 version of the file
		String confluenceSyntax = "here's a file: !Chai_LatteBeauty sm.jpg!\n";
		String expected = jspAttachDirectory + "/" + pagename + "-att/Chai_LatteBeauty+sm.jpg-dir/2.jpg";
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, jspAttachDirectory, pagename);
		assertNotNull(actual);
		
		int expectedSize = 1;
		assertEquals("size comparison: ", expectedSize, actual.size());
		
		for (String pathToFile : actual) {
			assertEquals(expected, pathToFile);
		}
	}
	
	public void testAttach() {
		//get a clean tmp dir
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		Page page = new Page (new File(""));
		page.setName(pagename + ".txt");
		String filename1 = jspAttachDirectory + "/" + pagename + "-att/ed.jpeg-dir/1.jpeg";
		String filename2 = jspAttachDirectory + "/" + pagename + "-att/Wiki.png-dir/1.png";
		Vector<String> files = new Vector<String>();
		files.add(filename1);
		files.add(filename2);
		
		Set<File> attached = page.getAttachments();
		
		assertTrue(attached.isEmpty());
		
		tester.attach(page, files, "");
		
		attached = page.getAttachments();
		int expectedSize = 2;
		assertEquals(expectedSize, attached.size());
		
		TreeSet<String> testable = new TreeSet<String>();
		String tmpDir = tester.getTmpDir();
		for (Iterator iter = attached.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			testable.add(file.getAbsolutePath());
		}
		
		int c = 0;
		for (Iterator iter = testable.iterator(); iter.hasNext();) {
			String path = (String) iter.next();
			String expected = tmpDir+"/"+ pagename + ".txt/" + ((c==0)?"Wiki.png":"ed.jpeg");
			String actual = path;
			assertEquals(expected, actual);
			c++;
		}
	}
	public void testAttachFromADifferentPage() {
		//clean up tmp directory
		deleteTmp();

		String input = "not an image: [AttachmentTestingPage^ed.txt]";
		String pagename = "CurrentPage";
		Page page = new Page(new File(""));
		page.setName(pagename + ".txt");
		
		String filename1 = jspAttachDirectory + "/" + this.pagename + "-att/ed.txt-dir/1.txt";
		
		Vector<String> files = new Vector<String>();
		files.add(filename1);
		
		String expectedOutput = "not an image: [^ed.txt]";
		String actualOutput = tester.attach(page, files, input);
		assertNotNull(actualOutput);
		assertEquals(expectedOutput, actualOutput);
		
		Set<File> actualFiles = page.getAttachments();
		String tmpDir = tester.getTmpDir();
		for (Iterator iter = actualFiles.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			String expected = tmpDir + "/" + this.pagename + ".txt/"+ "ed.txt";
			String actual = file.getAbsolutePath();
			assertNotNull(actual);
			assertEquals(expected, actual);
			
			assertTrue(file.exists());
		}
	}
	
	public void testCreateBasePath() {
		String dir = jspAttachDirectory;
		String page = pagename;
		String expected = dir + "/" + page + "-att/";
		String actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		dir = jspAttachDirectory + "/";
		actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//WINDOWS
		String pathSep = "\\";
		tester.FILE_SEP = "\\";
		dir = jspAttachDirectory.replaceAll("\\/", "\\\\");
		expected = dir + pathSep  + page + "-att\\";
		actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		dir = jspAttachDirectory.replaceAll("\\/", "\\\\") + pathSep;
		actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//uwc-194 - provide way to not allow filetypes in directory creation
		tester.FILE_SEP = File.separator;
		dir = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki";
		page = "A0Scanner" + ".txt";
		expected = dir + "/" + "A0Scanner.-att/";
		actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		dir = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki";
		page = "A0Scanner-test2" + ".txt";
		expected = dir + "/" + "A0Scanner-test2-att/";
		actual = tester.createBasePath(dir, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testUniquify() {
		Vector<String> input = new Vector<String>();
		input.add("a");
		input.add("b");
		input.add("a");
		Vector<String>actual = tester.uniquify(input);
		assertNotNull(actual);
		int expectedSize = 2;
		assertEquals(expectedSize, actual.size());
	}
	
	public void testGetVersion() {
		String pathsep = "/";
		String path = jspAttachDirectory + pathsep + pagename + "-att/";
		String file1 = "Wiki.png";
		int expected = 1;
		int actual  = tester.getVersion(path, file1);
		assertEquals(expected, actual);
		
		String file2 = "Chai_LatteBeauty+sm.jpg";
		expected = 2;
		actual  = tester.getVersion(path, file2);
		assertEquals(expected, actual);
		
		//WINDOWS
		pathsep = "\\";
		tester.FILE_SEP = pathsep;
		path = jspAttachDirectory.replaceAll("\\/", "\\\\") + pathsep + pagename + "-att" +
				pathsep;
		file1 = "Wiki.png";
		expected = 1;
		actual  = tester.getVersion(path, file1);
		assertEquals(expected, actual);
		
	}
	
	public void testGetOtherPage() {
		String input = "other^ed.jepg";
		String expected = "other";
		String actual = tester.getOtherPage(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "ed.jpeg";
		actual = tester.getOtherPage(input);
		assertNull(actual);
	}
	
	
	public void testFromADifferentPage() {
		String input = jspAttachDirectory + "/" + pagename + "-att/ed.jpeg-dir/1.jpeg";
		Page page = new Page(new File(""));
		page.setName(pagename);
		boolean expected = false;
		boolean actual = tester.fromADifferentPage(input, page);
		assertEquals(expected, actual);
		
		page.setName("CurrentPage");
		expected = true;
		actual = tester.fromADifferentPage(input, page);
		assertEquals(expected, actual);
	}
	
	public void testChangeReferences() {
		String pagename = "CurrentPage";
		String input = "with an alias: [alias|AttachmentTestingPage^ed.txt]";
		String filepath = jspAttachDirectory + "/" + pagename + "-att/ed.txt-dir/1.txt";
		String expected = "with an alias: [alias|^ed.txt]";
		String actual = tester.changeReferences(input, filepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "an attachment: !AttachmentTestingPage^ed.txt!";
		expected = "an attachment: !ed.txt!";
		actual = tester.changeReferences(input, filepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "not an image: [AttachmentTestingPage^ed.txt]";
		expected = "not an image: [^ed.txt]";
		actual = tester.changeReferences(input, filepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//uwc-216
		pagename = "flattering"; //FIXME contains 'att' already, so old regex will fail
		filepath = jspAttachDirectory + "/" + pagename + "-att/ed.txt-dir/1.txt";
		input = "with an alias: [alias|AttachmentTestingPage^ed.txt]";
		expected = "with an alias: [alias|^ed.txt]";
		actual = tester.changeReferences(input, filepath);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCreateTempDir() {
		String tmpDir = tester.getTmpDir();
		
		tester.createTmpDir(tmpDir);
		File file = new File(tmpDir);
		assertTrue(file.exists());
	}
	
	public void testCreateCorrectPath() {
		String pathsep = "/";
		String input = jspAttachDirectory + pathsep + pagename + "-att/ed.jpeg-dir/1.jpeg";
		String expected = tester.getTmpDir() + pathsep + pagename + ".txt/ed.jpeg";
		Page page = new Page(null);
		page.setName(pagename + ".txt");
		String actual = tester.createCorrectPath(input, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//WINDOWS
		pathsep = "\\";
		tester.FILE_SEP = pathsep;
		input = jspAttachDirectory.replaceAll("\\/", "\\\\") + 
				pathsep + pagename + "-att" +
				pathsep +
				"ed.jpeg-dir" +
				pathsep +
				"1.jpeg";
		expected = tester.getTmpDir() + 
				pathsep + pagename + ".txt" + pathsep +
				"ed.jpeg";
		actual = tester.createCorrectPath(input, page);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetDifferentPath() {
		String input = tester.getTmpDir() + "/pagename/ed.jpeg";
		String expected = tester.getTmpDir() + "/pagename/ed2.jpeg";
		String actual = tester.createDifferentPath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = expected;
		expected = tester.getTmpDir() + "/pagename/ed3.jpeg";
		actual = tester.createDifferentPath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = tester.getTmpDir() + "/pagename/ed9.jpeg";
		expected = tester.getTmpDir() + "/pagename/ed10.jpeg";
		actual = tester.createDifferentPath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = tester.getTmpDir() + "/pagename/ed10.jpeg";
		expected = tester.getTmpDir() + "/pagename/ed11.jpeg";
		actual = tester.createDifferentPath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//WINDOWS
		String pathsep = "\\";
		tester.FILE_SEP = pathsep;
		String tmp = tester.getTmpDir();
		input = tmp + pathsep +
				"pagename" +
				pathsep +
				"ed.jpeg";
		expected = tmp + pathsep +
				"pagename" +
				pathsep +
				"ed2.jpeg";
		actual = tester.createDifferentPath(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetClosingNumber() {
		String input = "abc1";
		int expected = 1;
		int actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);
		
		input = "something00";
		expected = 0;
		actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);

		input = "something02";
		expected = 2;
		actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);

		input = "blah123";
		expected = 123;
		actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);
		
		input = "a0b21";
		expected = 21;
		actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);
	
		input = "nonumbers";
		expected = 1;
		actual = tester.getClosingNumber(input);
		assertEquals(expected, actual);
		
	}

	public void testRemoveClosingNumber() {
		String input = "abc1";
		String expected = "abc";
		String actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something00";
		expected = "something";
		actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "something02";
		actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);


		input = "blah123";
		expected = "blah";
		actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a0b21";
		expected = "a0b";
		actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "nonumbers";
		expected = input;
		actual = tester.removeClosingNumber(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}
	
	public void testCopyFile() {
		//create clean tmp dir
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		String input = jspAttachDirectory + "/" + pagename + "-att/ed.jpeg-dir/1.jpeg";
		String output = tester.getTmpDir() + "/" + "test.jpeg";

		File inFile = new File(input);
		File outFile = new File(output);
		
		assertTrue(inFile.exists());
		assertFalse(outFile.exists());
		
		try {
			tester.copyFile(inFile, outFile);
		} catch (FileNotFoundException e) {
			fail("FileNotFoundException when copying: " + input + " to " + output);
		} catch (IOException e) {
			fail("IOException when copying: " + input + " to " + output);
		}
		
		assertTrue(inFile.exists());
		assertTrue(outFile.exists());
		
	}
	
	public void testCreateNewFile() {
		//create clean tmp dir
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		//attempt to create a new file
		String filename = tester.getTmpDir() + "/" + "test.txt";
		File file = new File(filename);
		File newfile = null;
		try {
			newfile = tester.createNewFile(file);
		} catch (IOException e) {
			fail("IO Exception while creating: " + filename);
		}  
		
		//did it work?
		assertNotNull(newfile);
		assertEquals(file, newfile);
		assertEquals(filename, newfile.getAbsolutePath());
		assertTrue(newfile.exists());
		
		try {
			newfile = tester.createNewFile(newfile);
		} catch (IOException e) {
			fail("IOException while creating: " + newfile.getAbsolutePath());
		}
		
		assertNotNull(newfile);
		assertEquals(tester.getTmpDir() + "/" + "test2.txt", newfile.getAbsolutePath());
		assertTrue(newfile.exists());
		
		try {
			newfile = tester.createNewFile(newfile);
		} catch (IOException e) {
			fail("IOException while creating: " + newfile.getAbsolutePath());
		}
		
		assertNotNull(newfile);
		assertEquals(tester.getTmpDir() + "/" + "test3.txt", newfile.getAbsolutePath());
		assertTrue(newfile.exists());
	}
	
	public void testCreateNewFileAndParents() {
		//create clean tmp dir
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		//attempt to create a new file
		String filename = tester.getTmpDir() + "/" + pagename + "/" + "test.txt";
		File file = new File(filename);
		File newfile = null;
		try {
			newfile = tester.createNewFile(file);
		} catch (IOException e) {
			fail("IO Exception while creating: " + filename);
		}  
		
		//did it work?
		assertNotNull(newfile);
		assertEquals(file, newfile);
		assertEquals(filename, newfile.getAbsolutePath());
		assertTrue(newfile.exists());
		
	}

	private void deleteTmp() {
		String tmp = tester.getTmpDir();
		File tmpDir = new File(tmp);
		if (tmpDir.exists()) {
			FileUtils.deleteDir(tmpDir);
		}
	}
	

	
	
	public void testMissingDirectories() {
		//create a clean dir
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		//a parent directory in this file does not exist
		String dir = tester.getTmpDir() + "/" + pagename;
		String input = dir + "/" + "test.txt";
		boolean expected = true;
		boolean actual = tester.missingDirectories(input);
		assertEquals(expected, actual);

		File pagenameDir = new File(dir);
		assertFalse(pagenameDir.exists());
		
		//now we make the directory
		assertTrue(pagenameDir.mkdir());
		
		assertTrue(pagenameDir.exists());
		assertTrue(pagenameDir.isDirectory());
		
		//now all parent directories exist
		expected = false;
		actual = tester.missingDirectories(input);
		assertEquals(expected, actual);
		
		//let's try the same test with more than one parent directory missing
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		String gpDir = dir;
		String pDir = gpDir + "/" + "parent";
		input = pDir + "/" + "test.txt";
		expected = true;
		actual = tester.missingDirectories(input);
		assertEquals(expected, actual);

		File grandparentDir = new File(gpDir);
		File parentDir = new File(pDir);
		assertFalse(grandparentDir.exists());
		assertFalse(parentDir.exists());
		
		//now we make the directories
		assertTrue(grandparentDir.mkdir());
		assertTrue(parentDir.mkdir());
		
		assertTrue(grandparentDir.exists());
		assertTrue(parentDir.exists());
		assertTrue(grandparentDir.isDirectory());
		assertTrue(parentDir.isDirectory());
		
		//now all parent directories exist
		expected = false;
		actual = tester.missingDirectories(input);
		assertEquals(expected, actual);
		
		//WINDOWS
		tester.FILE_SEP = "\\";
		dir = tester.getTmpDir() + "\\" + pagename;
		input = dir + "\\" + "test.txt";
		expected = true;
		//can't actually test this except on a Windows box, 
		//so we're testing that the method didn't throw a RuntimeExceptions (due to Backslashes and regex)
		try {
			actual = tester.missingDirectories(input);
		} catch (RuntimeException e) { 
			fail();
		}
	}
	
	public void testCreateParents() {
		//get clean tmp directory
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		
		String pathsep = "/";
		//gp = grandparent; p = parent
		String grandparentDir = tester.getTmpDir() + pathsep + pagename;
		String parentDir = grandparentDir + pathsep + "parent";
		String input = parentDir + pathsep + "test.txt";
		
		File grandparentFile = new File(grandparentDir);
		File parentFile = new File(parentDir);
		File file = new File(input);
		

		assertFalse(grandparentFile.exists());
		assertFalse(parentFile.exists());
		
		
		//now we make the directories
		tester.createParents(input);
		
		assertTrue(grandparentFile.exists());
		assertTrue(parentFile.exists());
		assertTrue(grandparentFile.isDirectory());
		assertTrue(parentFile.isDirectory());
		
		//WINDOWS
//		get clean tmp directory
		deleteTmp();
		tester.createTmpDir(tester.getTmpDir());
		pathsep = "\\";
		tester.FILE_SEP = pathsep;
		//we can't actually test this except on a Windows file system,
		//so we're going to just check that it doesn't throw RuntimeExceptions
		try {
			tester.createParents(input);
		} catch (RuntimeException e) {
			fail();
		}

	
	}
	
	public void testNoAttachmentDirectory() {
		Vector<String> actual = tester.getAttachableFiles("", "", "");
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}
	
	public void testGetTmpDir() {
		String base = System.getProperty("user.dir") + File.separator + "tmp";
		String expected = base;
		String actual = tester.getTmpDir();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//nonsense
		tester.FILE_SEP = "a";
		expected = base.replaceAll(File.separator, tester.FILE_SEP);
		actual = tester.getTmpDir();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//Windows
		tester.FILE_SEP = "\\";
		expected = base.replaceAll(File.separator, "\\" + tester.FILE_SEP);
		actual = tester.getTmpDir();
		assertNotNull(actual);
		assertEquals(expected, actual);
	
		//unix/mac
		tester.FILE_SEP = "/";
		expected = base;
		actual = tester.getTmpDir();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvert() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		String name = "SampleJspwiki-Input22.txt";
		//get file contents
		String input = "[Wiki.png]\n" + 
		"[ed.gif]";
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//create page
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//convert
		tester.convert(page);
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(2, actual.size());
		List<String> sorted = new Vector<String>();
		File parent = null;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			sorted.add(file.getName());
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
		assertEquals("ed.gif", sorted.get(0));
		assertEquals("Wiki.png", sorted.get(1));
		
		parent.delete();//clean up unit test
	}
	
	public void testConvert_Mult() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		//create page
		String name = "SampleJspwiki-Input22.txt";
		String input = "[Wiki.png]\n"; 
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//clean tmp
		File tmp = new File("tmp/" + name);
		File[] files = tmp.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//convert
		tester.convert(page);
		//run 1: tests
		Vector<File> actual = new Vector<File>();
		actual.addAll(page.getAttachments());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Wiki.png", actual.get(0).getName());

		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals("Wiki.png", files[0].getName());
		
		page.getAttachments().clear();

		//do it again
		tester.convert(page);
		//run 2: tests
		actual = new Vector<File>();
		actual.addAll(page.getAttachments());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Wiki.png", actual.get(0).getName());

		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals("Wiki.png", files[0].getName());

		//what if we convert a different page, are the run 2 files still there?
		String name2 = "SampleJspwiki-InputImageAlt.txt";
		String input2 = "[hummingbird.jpg]\n"; 
		Page page2 = new Page(null);
		page2.setOriginalText(input2);
		page2.setName(name2);
		tester.convert(page2);
		
		//run 3: tests
		actual = new Vector<File>();
		actual.addAll(page.getAttachments());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Wiki.png", actual.get(0).getName());
		
		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals("Wiki.png", files[0].getName());

		//clean up files
		for (File file : files) {
			file.delete();
		}
		
		actual = new Vector<File>();
		actual.addAll(page2.getAttachments());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("hummingbird.jpg", actual.get(0).getName());

		tmp = new File("tmp/" + name2);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals("hummingbird.jpg", files[0].getName());
		
		//clean up files
		for (File file : files) {
			file.delete();
		}
	}
	
	public void testFixLinks() throws IOException {
		String input, expected, actual;
		input = "!Wiki.png!\n" +
				"[^Wiki.png]\n" +
				"!Wiki.png|alt=test!\n" +
				"[alias|^Wiki.png]";
		expected = "!Wiki2.png!\n" +
				"[^Wiki2.png]\n" +
				"!Wiki2.png|alt=test!\n" +
				"[alias|^Wiki2.png]";
		File tmpfile = new File("tmp/testing/Wiki2.png");
		String origpath = "jspwiki/testing-att/Wiki.png-dir/1.png";
		actual = tester.fixLinks(tmpfile, origpath, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "!foo2+bar.jpg! !foo3+bar.jpg!";
		tmpfile = new File("/Users/laura/Code/Subversion/uwc-current/devel/tmp/SampleJspwiki-Input22.txt/foo2 bar.jpg");
		origpath = "sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo2+bar.jpg-dir/1.jpg";
		expected = "!foo2 bar.jpg! !foo3+bar.jpg!";
		actual = tester.fixLinks(tmpfile, origpath, input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testHandleWhitespaceInAttachments() {
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");

		//create page
		String name = "SampleJspwiki-Input22.txt";
		String input = "[foo bar.jpg]\n"; 
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//clean tmp
		File tmp = new File("tmp/" + name);
		File[] files = tmp.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//convert
		tester.convert(page);
		//run 1: tests
		Vector<File> actual = new Vector<File>();
		actual.addAll(page.getAttachments());
		assertNotNull(actual);
		assertEquals(1, actual.size());
		//test attachment name
		assertEquals("foo bar.jpg", actual.get(0).getName());
		//test input
		assertEquals("!foo bar.jpg!\n", page.getConvertedText());
		//test files on system
		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(1, files.length);
		assertEquals("foo bar.jpg", files[0].getName());
		
		//with URL Encoding
		name = "SampleJspwiki-Input22.txt";
		input = "[foo2+bar.jpg]\n" +
				"[foo3 bar.jpg]\n"; 
		page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//clean tmp
		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		//convert
		tester.convert(page);
		//run 1: tests
		actual = new Vector<File>();
		actual.addAll(page.getAttachments());
		assertNotNull(actual);
		assertEquals(2, actual.size());
		//test attachment name
		assertEquals("foo3 bar.jpg", actual.get(0).getName());
		assertEquals("foo2 bar.jpg", actual.get(1).getName());
		//test input
		assertEquals("!foo2 bar.jpg!\n!foo3 bar.jpg!\n", page.getConvertedText());
		//test files on system
		tmp = new File("tmp/" + name);
		files = tmp.listFiles();
		assertNotNull(files);
		assertEquals(2, files.length);
		assertEquals("foo2 bar.jpg", files[0].getName());
		assertEquals("foo3 bar.jpg", files[1].getName());
	}
	
	public void testHandleWS() {
		String input, expected, actual;
		input = "foo+bar";
		expected = "foo bar";
		actual = tester.handleWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "foo%20bar";
		actual = tester.handleWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "foo%2Bbar";
		actual = tester.handleWS(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testCheckUrlEncoding() {
		String input, expected, actual;
		input = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo2+bar.jpg-dir/1.jpg";
		expected = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo2%2Bbar.jpg-dir/1.jpg";
		actual = tester.checkUrlEncoding(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo3 bar.jpg-dir/1.jpg";
		expected = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo3%20bar.jpg-dir/1.jpg";
		actual = tester.checkUrlEncoding(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo0+bar.jpg-dir/1.jpg";
		expected = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input22.txt-att/foo0+bar.jpg-dir/1.jpg";
		actual = tester.checkUrlEncoding(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "/Users/laura/Code/Subversion/uwc-current/devel/sampleData/jspwiki/SampleJspwiki-Input%20TestSpace.txt-att/foo0+bar.jpg-dir/1.jpg";
		expected = input;
		actual = tester.checkUrlEncoding(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_NoRef() { //uwc-335
		String name = "SampleJspwiki-InputAttNoRef.txt";
		//get file contents
		String input = "no refs to images or attachments";
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//create page
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//convert
		tester.convert(page);
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(1, actual.size());
		File parent = null;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			assertEquals("doublefacepalm.jpg", file.getName());
			assertEquals("/Users/laura/Code/Subversion/uwc-current/devel/tmp/" + name + "/doublefacepalm.jpg", file.getPath());
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		
		parent.delete();//clean up unit test
	}
	
	public void testGetAttachableFiles_NoRef() {
		String confluenceSyntax = "testing";
		String attachmentDirectory = "sampleData/jspwiki/";
		String pagename = "SampleJspwiki-InputAttNoRef.txt";
		Vector<String> actual = tester.getAttachableFiles(confluenceSyntax, attachmentDirectory, pagename);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("sampleData/jspwiki/SampleJspwiki-InputAttNoRef.txt-att/doublefacepalm.jpg-dir/1.jpg", actual.get(0));
	}
	
	public void testConvert_NoRefTurnedOff() { //uwc-335
		String name = "SampleJspwiki-InputAttNoRef.txt";
		//get file contents
		String input = "no refs to images or attachments";
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//create page
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		//set property
		Properties properties = tester.getProperties();
		properties.put("images-all", "false");
		tester.setProperties(properties);
		//convert
		tester.convert(page);
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}
	
	public void testConvert_NoRefAndPlus() {
		String name = "SampleJspwiki-Input22.txt";
		//get file contents
		String input = "[foo2+bar.jpg] [foo3+bar.jpg]";
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		
		//without all images
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");
		tester.convert(page);
		String expected = "!foo2 bar.jpg! !foo3 bar.jpg!";
		assertNotNull(page.getConvertedText());
		assertEquals(expected, page.getConvertedText());
		
		//with all images
		page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		properties.put("images-all", "true");
		tester.convert(page);
		expected = "!foo2 bar.jpg! !foo3 bar.jpg!";
		assertNotNull(page.getConvertedText());
		assertEquals(expected, page.getConvertedText());
		
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(8, actual.size());
		File parent = null;
		int both = 0;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			if (file.getName().equals("foo2 bar.jpg") || file.getName().equals("foo3 bar.jpg")) both++;
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		assertEquals(2, both);
		
		parent.delete();//clean up unit test

	}
	
	public void testConvert_NoRefOtherPage() {
		String name = "SampleJspwiki-Input21.txt";
		//get file contents
		String input = "an attachment: [SampleJspwiki-Input21/ed.jpeg]\n" + 
				"not an image: [SampleJspwiki-Input21/ed.zip]\n" + 
				"\n" + 
				"with an alias: [alias|SampleJspwiki-Input21/ed.png]\n" + 
				"with an alias: [alias|SampleJspwiki-Input21/ed.doc]\n" +
				//see uwc-346
				"with spaces: [a link to another page attachment|awikipage/a wiki attachment.xls].\n" +
				"[a link to another page attachment|awikipage/a+wiki+attachment.xls]\n" +
				"[otherpage/a+wiki+attachment.png]\n" +
				"[otherpage/a wiki attachment.png]\n" +
				"[SampleJspwiki-Input22/foo+bar.jpg]\n" +
				//see http://developer.atlassian.com/jira/browse/UWC-335?page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel&focusedCommentId=25532#action_25532
				"[a.link.to.an.attachment|awikipage/a+wiki+attachment.xls]\n"; 
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		//without all pages
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		properties.put("images-all", "false");
		tester.convert(page);
		String expected = "an attachment: !ed.jpeg!\n" + 
				"not an image: [^ed.zip]\n" + 
				"\n" + 
				"with an alias: !ed.png!\n" + 
				"with an alias: [alias|^ed.doc]\n" +
				"with spaces: [a link to another page attachment|awikipage^a wiki attachment.xls].\n" +
				"[a link to another page attachment|awikipage^a wiki attachment.xls]\n" +
				"!otherpage^a wiki attachment.png!\n" +
				"!otherpage^a wiki attachment.png!\n" +
				"!SampleJspwiki-Input22^foo bar.jpg!\n" +
				"[a.link.to.an.attachment|awikipage^a wiki attachment.xls]\n";
		assertNotNull(page.getConvertedText());
		assertEquals(expected, page.getConvertedText());
		
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(4, actual.size());
		File parent = null;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			assertTrue(file.getName().equals("ed.zip") || 
					file.getName().equals("ed.jpeg") ||
					file.getName().equals("ed.png") ||
					file.getName().equals("ed.doc")
					);
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		
		
		//with all pages
		page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);
		properties.put("images-all", "true");
		tester.convert(page);
		assertNotNull(page.getConvertedText());
		assertEquals(expected, page.getConvertedText());
		
		//does page have attachments
		actual = null;
		actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(4, actual.size());
		parent = null;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			assertTrue(file.getName().equals("ed.zip") || 
					file.getName().equals("ed.jpeg") ||
					file.getName().equals("ed.png") ||
					file.getName().equals("ed.doc")
					);
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		
		parent.delete();//clean up unit test


	}
	
	public void testConvertFile() {
		//see interference between UWC-348 and UWC-335
		String name = "SampleJspwiki-Input21.txt";
		//get file contents
		String input = "[Cross-Project Calendar|file://Filesrv11\\PUBLIC\\09_Projects\\9.3_Project_Management\\Cross_Product_Calendar\\All_Projects.xls]"; 
		//set attachment directory
		tester.setAttachmentDirectory("sampleData/jspwiki/");

		Page page = new Page(null);
		page.setOriginalText(input);
		page.setName(name);

		tester.convert(page);
		String expected = input;
		assertNotNull(page.getConvertedText());
		assertEquals(expected, page.getConvertedText());
		//does page have attachments
		Set<File> actual = page.getAttachments();
		assertNotNull(actual);
		assertEquals(4, actual.size());
		File parent = null;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
			assertTrue(file.getName(), file.exists());
			assertTrue(file.getName().equals("ed.zip") || 
					file.getName().equals("ed.jpeg") ||
					file.getName().equals("ed.png") ||
					file.getName().equals("ed.doc")
					);
			parent = file.getParentFile();
			file.delete(); //clean up unit test
		}
		
		parent.delete();//clean up unit test

	}
	
	public void testGetAll() {
		assertTrue(tester.getAll());
		
		Properties properties = tester.getProperties();
		tester.setProperties(properties);
		
		properties.put("images-all", "");
		assertTrue(tester.getAll());
		
		properties.put("images-all", "true");
		assertTrue(tester.getAll());
		
		properties.put("images-all", "false");
		assertFalse(tester.getAll());
	}
	
	public void testGetAttachmentName() {
		String input, expected, actual;
		input = "testing123.jpg-dir";
		expected = "testing123.jpg";
		actual = tester.getAttachmentName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetExtension() {
		String input, expected, actual;
		input = "testing. foo bar.doc-dir";
		expected = ".doc";
		actual = tester.getExtension(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	

	public void testConvertPagenameWithWS() {
		String input, expected, actual;
		tester.getProperties().put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		input = "* [a.link.to.a.page|SampleJspwiki-Input.WithDots]\n" + 
				"* [a.link.to.a.page|a.page]\n" +
				"* [a.link.to.an.attach|ed.doc]";
		expected = "* [a.link.to.a.page|SampleJspwiki-Input.WithDots]\n" + 
				"* [a.link.to.a.page|a.page]\n" +
				"* [a.link.to.an.attach|^ed.doc]";
		actual = tester.convertAttachments(input, "SampleJspwiki-Input21");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertPagenameWithWS2 () {
		String input, expected, actual;
		tester.getProperties().put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
			
		input = "* [Client Automation 7.5|ClientAutomation7.5]\n" + 
				"* [Client Automation 7.2|CA7.2]\n" + 
				"* [Client Automation Standard 7.21|CAS7.21]\n" + 
				"";
		expected = input;
		actual = tester.convertAttachments(input, "SampleJspwiki-Input21");
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertPagenameWithDots() {
		String input, expected, actual;
		tester.getProperties().put(JspwikiLinkConverter.JSPWIKI_PAGEDIR, "sampleData/jspwiki/");
		tester.setAttachmentDirectory("sampleData/jspwiki/");
		
		Page page = new Page(null);
		page.setName("SampleJspwiki-Input.WithDots.txt");
		page.setOriginalText("Testing");

		assertTrue(page.getAttachments().isEmpty());
		
		tester.convert(page);
		
		assertFalse(page.getAttachments().isEmpty());
		
	}
}

