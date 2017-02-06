package com.atlassian.uwc.converters.tikiwiki;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.ConverterErrors;
import com.atlassian.uwc.ui.Page;

public class AttachmentConverterTest extends TestCase {

	AttachmentConverter tester;
	HashMap<String,String> idsAndNames;
	private static final String DB_PROP_FILENAME = "settings.tikiwiki.properties";
	String location = "conf-local/" + DB_PROP_FILENAME; //conf-local should be a sibling dir to conf
	String attachDir = "/Users/laura/Code/Workspace/tikiwiki-1.9.5";
	
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("log4j.properties");
		tester = new AttachmentConverter();
		idsAndNames = new HashMap<String,String>();
		idsAndNames.put("1", "ABC");
		idsAndNames.put("12", "DEF");
		tester.setPropertyLocation(location);
		tester.FILE_SEP = "/";

	}

	public void testReplaceImageIds() {
		String input = "{img src=\"show_image.php?id=1\"}";
		String expected = "{img src=\"show_image.php?name=ABC\"}";
		String actual = tester.replaceImageIds(
				input, idsAndNames, AttachmentConverter.GalleryType.IMAGE);
		assertEquals(expected, actual);
	}

	public void testWithAmp() {
		String input = "{img src=\"show_image.php?id=12&width=200\"}";
		String expected = "{img src=\"show_image.php?name=DEF&width=200\"}";
		String actual = tester.replaceImageIds(
				input, idsAndNames, AttachmentConverter.GalleryType.IMAGE);
		assertEquals(expected, actual);
	}
	
	public void testWithQuotes() {
		String input = "{img src=\"show_image.php?id=12&width=200\" }";
		String expected = "{img src=\"show_image.php?name=DEF&width=200\" }";
		String actual = tester.replaceImageIds(
				input, idsAndNames, AttachmentConverter.GalleryType.IMAGE);
		assertEquals(expected, actual);		
	}
	
	public void testIsGalleryConversion() {
		TreeMap<String,String> map = new TreeMap<String,String>();
		tester.properties = map;
		
		map.put(tester.PROPERTIES_CONVERTGALLERY, "true");
		boolean actual = tester.isGalleryConversion();
		assertTrue(actual);
		
		map.put(tester.PROPERTIES_CONVERTGALLERY, "false");
		actual = tester.isGalleryConversion();
		assertFalse(actual);
		
	}
	
	public void testCombineVectors() {
		Vector<String> a = new Vector<String>();
		a = vectorUtilAdd("a,b", a);
		Vector<String> b = new Vector<String>();
		b = vectorUtilAdd("b,c,x", b);
		Vector<String> expected = new Vector<String>();
		expected = vectorUtilAdd("a,b,c,x", expected);
		Vector<String> actual = tester.combineVectors(a, b);
		vectorUtilCompare("combining vectors 1", expected, actual);
		
		a.removeAllElements();
		a = vectorUtilAdd("a,b,c", a);
		b.removeAllElements();
		b = vectorUtilAdd("d,e,f", b);
		expected.removeAllElements();
		expected = vectorUtilAdd("a,b,c,d,e,f", expected);
		actual.removeAllElements();
		actual = tester.combineVectors(a, b);
		vectorUtilCompare("combining vectors 2", expected, actual);
		
		b.removeAllElements();
		b = vectorUtilAdd("a,b,c",b);
		expected.removeAllElements();
		expected = vectorUtilAdd("a,b,c",expected);
		actual.removeAllElements();
		actual = tester.combineVectors(a, b);
		vectorUtilCompare("combining vectors 2", expected, actual);
		
	}
	
	private void vectorUtilCompare(String msg, Vector<String> expected, Vector<String> actual) {
		assertNotNull(msg + " is null", actual);
		assertTrue(msg + " size not same", expected.size() == actual.size());
		
		for(int i = 0; i < actual.size(); i++) {
			String exp = expected.get(i);
			String act = actual.get(i);
			assertEquals(msg + " index " + i + " comparison", exp, act);
		}
		
	}

	private Vector<String> vectorUtilAdd(String commaDelimed, Vector<String> v) {
		String[] items = commaDelimed.split(",");
		for (String i : items) {
			v.add(i);
		}
		return v;
	}
	
	public void testStandardizeImageSyntax() {
		String input = 
			"{img src=&quot;img/wiki_up/hobbespounce.gif&quot; width=&quot;200&quot;}\n" +
			"{img src=show_image.php?name=Wiki.png }\n" +
			"{img src=show_image.php?id=1 }\n" +
			"\n" +
			"Importance of quotes\n" +
			"{img src=img/wiki_up/hobbespounce.gif}\n" +
			"{img src=\"show_image.php?name=Wiki.png\" }\n" +
			"\n" +
			"Importance of space\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\"}\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\" }\n" +
			"{img src=show_image.php?name=Wiki.png}\n" +
			"{img src=http://localhost:8081/tikiwiki-1.9.5/images/book.gif}" +
			"";
		String expected = 
			"{img src=\"img/wiki_up/hobbespounce.gif\" width=\"200\"}\n" +
			"{img src=\"show_image.php?name=Wiki.png\" }\n" +
			"{img src=\"show_image.php?id=1\" }\n" +
			"\n" +
			"Importance of quotes\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\"}\n" +
			"{img src=\"show_image.php?name=Wiki.png\" }\n" +
			"\n" +
			"Importance of space\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\"}\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\" }\n" +
			"{img src=\"show_image.php?name=Wiki.png\"}\n" +
			"{img src=\"http://localhost:8081/tikiwiki-1.9.5/images/book.gif\"}" +
			"";

		String actual = tester.standardizeImageSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testReplaceImageIdsForFileGallery() {
		String input = "{img src=\"tiki-download_file.php?id=1\"}";
		String expected = "{img src=\"tiki-download_file.php?name=ABC\"}";
		String actual = tester.replaceImageIds(
				input, idsAndNames, AttachmentConverter.GalleryType.FILE);
		assertEquals(expected, actual);
	}
	
	public void testStandardizeImageSyntaxForFileGallery() {
		String input = "{img src=tiki-download_file.php?fileId=1 }\n";
		String expected = "{img src=\"tiki-download_file.php?fileId=1\" }\n";
		String actual = tester.standardizeImageSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		input = "{img src=http://localhost:8081/tikiwiki-1.9.5/tiki-download_file.php?fileId=1}\n";
		expected = "{img src=\"tiki-download_file.php?fileId=1\"}\n";
		actual = tester.standardizeImageSyntax(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGettingGalleryInfoFromDB() {
		String sql = "select imageId, name from tiki_images;";
		assertTrue(tester.readDBProperties(location));
		//test image gallery
		String imageId = "1";
		String expected = "Wiki.png";
		HashMap<String, String> map = tester.getIdsAndNames(sql, "imageId", "name");
		String actual = map.get(imageId);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		sql = "select fileId, fileName from tiki_files;";
		assertTrue(tester.readDBProperties(location));
		//test file gallery 
		String fileId = "1";
		expected = "cow.jpg";
		map = tester.getIdsAndNames(sql, "fileId", "fileName");
		actual = map.get(fileId);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testAddAttachmentsToPage_ImageGal() {
		
		String input = "{img src=show_image.php?id=1 }";
		String expected = "{img src=\"show_image.php?name=Wiki.png\" }" ;
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.addAttachmentsToPage(page, this.attachDir);
		//check syntax
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//check attachments
		Set<File> attachments = page.getAttachments();
		int expSize = 1;
		int actSize = attachments.size();
		assertEquals(expSize, actSize);
		
		File attachment = null;
		for (File file : attachments) {
			attachment = file;
		}
		String expFilename = "Wiki.png";
		String actFilename = attachment.getName(); 
		assertNotNull(actFilename);
		assertEquals(expFilename, actFilename);
	}
	
	public void testAddAttachmentsToPage_FileGal_DB() {
		
		String input = "{img src=tiki-download_file.php?fileId=1}\n";
		String expected = "{img src=\"tiki-download_file.php?name=cow.jpg\"}\n" ;
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.addAttachmentsToPage(page, this.attachDir);
		//check syntax
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//check attachments
		Set<File> attachments = page.getAttachments();
		int expSize = 1;
		int actSize = attachments.size();
		assertEquals(expSize, actSize);
		
		File attachment = null;
		for (File file : attachments) {
			attachment = file;
		}
		String expFilename = "cow.jpg";
		String actFilename = attachment.getName();
		assertNotNull(actFilename);
		assertEquals(expFilename, actFilename);

	}
	
	public void testAddAttachmentsToPage_FileGal_Filesystem() {
		
		String input = "{img src=tiki-download_file.php?fileId=2}\n";
		String expected = "{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}\n" ;
		
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.addAttachmentsToPage(page, this.attachDir);
		//check syntax
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//check attachments
		Set<File> attachments = page.getAttachments();
		int expSize = 1;
		int actSize = attachments.size();
		assertEquals(expSize, actSize);
		
		File attachment = null;
		for (File file : attachments) {
			attachment = file;
		}
		String expFilename = "warmdrink_sm.jpg";
		String actFilename = attachment.getName();
		assertNotNull(actFilename);
		assertEquals(expFilename, actFilename);

	}
	public void testReplaceIdsWithNames() {
		assertTrue(tester.readDBProperties(location));
		String input = 	"All id syntaxes\n" +
				"{img src=\"show_image.php?id=1\"}\n" +
				"{img src=\"tiki-download_file.php?fileId=1\"}\n" +
				"{img src=\"tiki-download_file.php?fileId=2\"}\n";
		String expected = 	"All id syntaxes\n" +
			"{img src=\"show_image.php?name=Wiki.png\"}\n" +
			"{img src=\"tiki-download_file.php?name=cow.jpg\"}\n" +
			"{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}\n";
		String actual = tester.replaceIdsWithNames(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetAllGalleryPaths() {
		assertTrue(tester.readDBProperties(location));
		String file1 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/hobbespounce.gif";
		String syntax2= "{img src=\"show_image.php?name=Wiki.png\"}\n";
		String file2 = "/Users/laura/Desktop/tikiwiki-output/Wiki.png";
		String syntax3 = "{img src=\"tiki-download_file.php?name=cow.jpg\"}\n";
		String file3 = "/Users/laura/Desktop/tikiwiki-output/cow.jpg";
		String syntax4 = "{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}\n";
		String file4 = "/Users/laura/Desktop/tikiwiki-output/warmdrink_sm.jpg";
		String input = 	"All id syntaxes\n" +
			syntax2 +
			syntax3 +
			syntax4;
		Vector<String> paths = new Vector<String>();
		paths.add(file1);
		
		Vector<String> expected = new Vector<String>();
		expected.add(file1);
		expected.add(file2);
		expected.add(file3);
		expected.add(file4);
//		String attachDir = "/Users/laura/Code/Workspace/tikiwiki-1.9.5";
		Vector<String> actual = tester.getAllGalleryPaths(input, this.attachDir, paths);
		
		assertNotNull(actual);
		int expectedSize = 4;
		assertEquals(expectedSize, actual.size());
		
		for (int i = 0; i < actual.size(); i++) {
			String exp = expected.get(i);
			String act = actual.get(i);
			assertNotNull(act);
			assertEquals("index: " + i, exp, act);
		}
	}
	
	public void testGetGalleryPaths() {
		//stubs
		String syntax3 = "{img src=\"tiki-download_file.php?name=cow.jpg\"}\n";
		String syntax4 = "{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}\n";
		String file4 = "/Users/laura/Desktop/tikiwiki-output/warmdrink_sm.jpg";
		
		String input = syntax3;
		String table = "tiki_files";
		String column = "fileName";
		String baseDir  = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/file_galleries/Attachments/";

		//prepare the db
		tester.readDBProperties(this.location);
		
		//syntax 3 will return no paths from getGalleryPaths
		Vector<String> actualVector = tester.getGalleryPaths(input, table, column, baseDir);
		assertNotNull(actualVector);
		assertTrue(actualVector.isEmpty());
		
		//syntax4 will return 1 path
		input = syntax4;
		actualVector = tester.getGalleryPaths(input, table, column, baseDir);
		assertNotNull(actualVector);
		int expectedSize = 1;
		assertEquals(expectedSize, actualVector.size());
		
		String expected = file4;
		String actual = actualVector.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	
	public void testDownloadDbFilesToTmp() {
		//prepare the db
		tester.readDBProperties(this.location);
		
		//create stubs
		String syntax3 = "{img src=\"tiki-download_file.php?name=cow.jpg\"}\n";
		String file3 = "/Users/laura/Desktop/tikiwiki-output/cow.jpg";
		String syntax4 = "{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}\n";

		String input = syntax4;
		String table = "tiki_files";
		String column = "fileName";
		String tmpDir = tester.getTmpDir();

		//syntax 4 will return no paths
		Vector<String> actualVector = tester.downloadDbFilesToTmp(input, table, column, tmpDir);
		assertNotNull(actualVector);
		assertTrue(actualVector.isEmpty());

		//syntax3 will return one path
		input = syntax3;
		actualVector = tester.downloadDbFilesToTmp(input, table, column, tmpDir);
		assertNotNull(actualVector);
		int expectedSize = 1;
		assertEquals(expectedSize, actualVector.size());
		
		actualVector = tester.downloadDbFilesToTmp(input, table, column, tmpDir);	
		String expected = file3;
		String actual = actualVector.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//test that the tmp file exists
		File file = new File(file3);
		assertTrue(file.exists());

	}
	public void testGetTmpDir() {
		tester.readDBProperties(this.location);
		String expected = "/Users/laura/Desktop/tikiwiki-output";
		String actual = tester.getTmpDir();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testEntireURLs() {
		String input = "{img src=http://localhost:8081/tikiwiki-1.9.5/tiki-download_file.php?fileId=2}";
		String expected = "{img src=\"tiki-download_file.php?name=warmdrink_sm.jpg\"}";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		tester.addAttachmentsToPage(page, attachDir);
		String actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//check that the page got attached
		Set<File> attFiles = page.getAttachments();
		int expectedSize = 1;
		assertEquals(expectedSize, attFiles.size());
		
		File attachment = null;
		for (File file : attFiles) {
			attachment = file;
		}
		assertTrue(attachment.exists());
		assertTrue(attachment.isFile());
		expected = "warmdrink_sm.jpg";
		actual = attachment.getName();
		assertEquals(expected, actual);
	}
	
	public void testStandardizeUrl() {
		String input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/tiki-download_file.php?fileId=2\"}";
		String expected = "{img src=\"tiki-download_file.php?fileId=2\"}";
		String actual = tester.standardizeUrl(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/img/wiki_up/" +
				"hobbespounce.gif\"}\n";
		expected = "{img src=\"img/wiki_up/hobbespounce.gif\"}\n";
		actual = tester.standardizeUrl(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//ignore
		input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/images/book.gif\"}";
		expected = input;
		actual = tester.standardizeUrl(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testImgQuotesAsEntities() {
		String input = "{img src=&quot;img/wiki_up/hobbespounce.gif&quot; }";
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		
		tester.addAttachmentsToPage(page, attachDir);
		
		//test attachment set's size
		Set<File> attachments = page.getAttachments();
		int expectedSize = 1;
		assertEquals(expectedSize, attachments.size());
		
		//test attachment name;
		File attachment = null;
		for (File file : attachments) {
			attachment = file;
		}
		String expectedName = "hobbespounce.gif";
		String actualName = attachment.getName();
		assertNotNull(actualName);
		assertEquals(expectedName, actualName);
	}
	
	public void testImgLargeContext() {
		String input = "{CODE}\n" +
			"*_Screenshot(s)_:\n" +
			"{img src=\"img/wiki_up/hobbespounce.gif\" }\n" +
			"^\n" ;
		Page page = new Page(new File(""));
		page.setOriginalText(input);
		
		tester.addAttachmentsToPage(page, attachDir);
		
		//test attachment set's size
		Set<File> attachments = page.getAttachments();
		int expectedSize = 1;
		assertEquals(expectedSize, attachments.size());
		
		//test attachment name;
		File attachment = null;
		for (File file : attachments) {
			attachment = file;
		}
		String expectedName = "hobbespounce.gif";
		String actualName = attachment.getName();
		assertNotNull(actualName);
		assertEquals(expectedName, actualName);
		
	}
	
	public void testAttachmentDirectoryIsWinDrive() {
		//XXX Set this to be a file in Windows location: like
		String winlocation = "img\\wiki_up\\image.gif";
		String input = "{img src=\""+winlocation+"\" }";
		//XXX Set this to be a local Windows drive
		String attachDir = "R:";
		tester.FILE_SEP = "\\";
		//On a Windows box, expected would be R:\\img\wiki_up\hobbespounce.gif
		String expected = attachDir + "\\\\" +winlocation; 
		
		Vector<String> paths = tester.getUploadPaths(input, attachDir);

		//test size of paths
		assertNotNull(paths);
		int expectedSize = 1;
		assertEquals(expectedSize, paths.size());

		//test name of path
		String actual = paths.get(0);
	}
	
	public void testAttachingImageWithWhitespace() {
		String propLocation = "/Users/laura/Code/Clients/Webroot/Data/uwc/conf/" + DB_PROP_FILENAME;
		tester.setPropertyLocation(propLocation);
		String input = "{img src=http://216.17.247.75/tikiwiki/tiki-download_file.php?fileId=1902}";
		String expected = "{img src=\"tiki-download_file.php?name=BraveSentry FakeAlert Desktop.JPG\"}";
		Page page = new Page(null);
		page.setName("Testing Attachments");
		page.setOriginalText(input);
		String attachDir = "/Volumes/Spike/Work/Clients/Webroot/Data/attachments";
		tester.addAttachmentsToPage(page, attachDir);
		assertNotNull(page);
		assertEquals(expected, page.getConvertedText());
		
		//now test that it attached the file
		Set<File> attachments = page.getAttachments();
		assertNotNull(attachments);
		assertEquals(1, attachments.size());
		File file = null;
		for (File attachment : attachments) {
			file = attachment;
		}
		assertNotNull(file);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		log.debug("file = " + file.getPath());
		String expectedName = "BraveSentry FakeAlert Desktop.JPG"; 
		String expectedPath = "/Volumes/Spike/Work/Clients/Webroot/Data/output-attachments/BraveSentry FakeAlert Desktop.JPG"; 
		assertEquals(expectedName, file.getName());
		assertEquals(expectedPath, file.getPath());
	}
	
	public void testGetImageGalleryDir() {
//		prepare the db
		tester.readDBProperties(this.location);
		
		String expected = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/images/Attachments/";
		String actual = tester.getImageGalleryDirectory(this.attachDir );
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetFileGalleryDir() {
//		prepare the db
		tester.readDBProperties(this.location);
		
		String expected = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/file_galleries/Attachments/";
		String actual = tester.getFileGalleryDirectory(this.attachDir);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetAbsolutePath() {
		String input = "/blah";
		String expected = input;
		String actual = tester.getAbsolutePath(input, this.attachDir);
		assertNotNull(actual);
		assertEquals(expected,actual);
		
		input = "blah.txt";
		expected = input;
		actual = tester.getAbsolutePath(input, this.attachDir);
		assertNotNull(actual);
		assertEquals(expected,actual);
		
		input = "./blah";
		expected = this.attachDir + "/" + "blah";
		actual = tester.getAbsolutePath(input, this.attachDir);
		assertNotNull(actual);
		assertEquals(expected,actual);
	}
	
	public void testAttach() {
		String goodpath1 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/hobbespounce.gif";
		String badpath1 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/nofile.gif";
		String badpath2 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/img/wiki_up/";
		String badpath3 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/tiki-download_file.php?fileId=1";
		String badpath4 = "/Users/laura/Code/Workspace/tikiwiki-1.9.5/show_image.php?name=bahhumbug.gif";
		
		Page page = new Page(new File(""));
		page.setName("testing");
		
		Vector<String> paths = new Vector<String>();

		//test that only one of the paths attaches a file
		paths.add(goodpath1);
		paths.add(badpath1);
		paths.add(badpath2);
		paths.add(badpath3);
		paths.add(badpath4);
		tester.attach(paths, page);
		Set<File> attachments = page.getAttachments();
		assertNotNull(attachments);
		assertTrue(attachments.size() == 1);
		
		cleanTester(paths);
		
		//test that the first bad path (doesn't exist) creates errors
		paths.add(badpath1);
		tester.attach(paths, page);
		ConverterErrors errors = tester.getErrors();
		assertNotNull(errors);
		assertTrue(errors.hasErrors());
		
		cleanTester(paths);
		
		//test that the second bad path (a dir) creates errors
		paths.add(badpath2);
		tester.attach(paths, page);
		assertNotNull(tester.getErrors());
		assertTrue(tester.getErrors().hasErrors());
		
		cleanTester(paths);
		
		//test that the 3rd and 4th bad paths (not file system paths) suppresses errors
		paths.add(badpath3);
		tester.attach(paths, page);
		assertNotNull(tester.getErrors());
		assertFalse(tester.getErrors().hasErrors());
		
		cleanTester(paths);
		
		paths.add(badpath4);
		tester.attach(paths, page);
		assertNotNull(tester.getErrors());
		assertFalse(tester.getErrors().hasErrors());
		
		cleanTester(paths);
	}

	public void testConvert() {
		String input, expected, actual;
		Page page = null;
		tester.setAttachmentDirectory(this.attachDir);
		//good
		input = "{img src=\"img/wiki_up/testimage.png\" width=\"200\"}";
		expected = input;
		page = null;
		page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertNotNull(page.getAttachments());
		assertFalse(page.getAttachments().isEmpty());

		assertNotNull(tester.getErrors());
		assertNotNull(tester.getErrors().getErrors());
		assertTrue(tester.getErrors().getErrors().isEmpty());

		//not converter - don't attach this - leave it alone, and don't display errors
		input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/images/book.gif\"}";
		expected = input;
		page = null;
		page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertNotNull(page.getAttachments());
		assertTrue(page.getAttachments().isEmpty());
		
		assertNotNull(tester.getErrors());
		assertNotNull(tester.getErrors().getErrors());
		assertTrue(tester.getErrors().getErrors().isEmpty());


		//bad
		input = "{img src=\"img/wiki_up/nofile.gif\"}";
		expected = input;
		page = null;
		page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertNotNull(page.getAttachments());
		assertTrue(page.getAttachments().isEmpty());
		
		assertNotNull(tester.getErrors());
		assertNotNull(tester.getErrors().getErrors());
		assertFalse(tester.getErrors().getErrors().isEmpty());

	}
	
	public void testGetUploadPaths() {
		String input, expected, actual;
		Vector paths;
		//good
		paths = null;
		input = "{img src=\"img/wiki_up/testimage.png\" width=\"200\"}";
		paths = tester.getUploadPaths(input, attachDir);
		assertNotNull(paths);
		assertEquals(1, paths.size());
		expected = attachDir + "/img/wiki_up/testimage.png";
		actual = (String) paths.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "{img src=\"tiki-download_file.php?fileId=2\"}";
		paths = tester.getUploadPaths(input, attachDir);
		assertNotNull(paths);
		assertEquals(1, paths.size());
		expected = attachDir + "/tiki-download_file.php?fileId=2";
		actual = (String) paths.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//not converter - don't attach this - leave it alone, and don't display errors
		paths = null;
		input = "{img src=\"http://localhost:8081/tikiwiki-1.9.5/images/book.gif\"}";
		paths = tester.getUploadPaths(input, attachDir);
		assertNotNull(paths);
		assertEquals(1, paths.size());
		expected = "http://localhost:8081/tikiwiki-1.9.5/images/book.gif";
		actual = (String) paths.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//bad
		paths = null;
		input = "{img src=\"img/wiki_up/nofile.gif\"}";
		paths = tester.getUploadPaths(input, attachDir);
		assertNotNull(paths);
		assertEquals(1, paths.size());
		expected = attachDir + "/img/wiki_up/nofile.gif";
		actual = (String) paths.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//windows
		tester.FILE_SEP = "\\";
		paths = null;
		input = "{img src=\"img/wiki_up/testimage.png\" width=\"200\"}";
		String winattach = attachDir.replaceAll("\\/", "\\\\");
		tester.setAttachmentDirectory(winattach);
		paths = tester.getUploadPaths(input, winattach);
		assertNotNull(paths);
		assertEquals(1, paths.size());
		expected = winattach + "\\img\\wiki_up\\testimage.png";
		actual = (String) paths.get(0);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	private void cleanTester(Vector<String> paths) {
		paths.clear();
		tester.getErrors().clear();
		assertFalse(tester.getErrors().hasErrors());
	}
}
