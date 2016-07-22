package com.atlassian.uwc.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

public class UWCGuiModelTest extends TestCase {

	private static final String UWC_BASE_DIR = "/Users/laura/Code/Subversion/universal-wiki-converter/devel";
	private static final String TEST_INPUT_DIR = UWC_BASE_DIR + "/sampleData/mediawiki";
	private static final String TEST_PROPS_DIR = UWC_BASE_DIR + "/sampleData/engine";
	UWCGuiModel tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new UWCGuiModel();
	}

	public void testGetConvertersAsStrings() {
		TreeMap<String, String> input = new TreeMap<String,String>();
		input.put("a", "b");
		
		List<String> actual = tester.getConvertersAsStrings(input);
		
		assertNotNull(actual);
		assertEquals(1, actual.size());
		for (String item : actual) {
			assertEquals("a=b", item);
		}
	}
	
	public void testGetPageFiles() {
 		Vector<String> names = tester.getPageNames();
 		String pagename1 = "SampleMediawiki-Input4.txt";
 		String pagename2 = "SampleMediawiki-Input5.txt";
		names.add(TEST_INPUT_DIR + File.separator + pagename1);
		names.add(TEST_INPUT_DIR + File.separator + pagename2);
		
		List<File> files = tester.getPageFiles();
		assertNotNull(files);
		assertEquals(2, files.size());
		assertEquals(pagename1, files.get(0).getName());
		assertEquals(pagename2, files.get(1).getName());
	}
	
	public void testGetConverters() {
		//no file at location
		String propsName = "converter.testing-nofilehere.properties";
		String propsPath = TEST_PROPS_DIR + File.separator + propsName;
		List<String> actual = null; 
		try {
			actual = tester.getConverters(propsPath);
			fail("An exception should have been thrown.");
		} catch (IOException e) {
			fail("We shouldn't get to the IOException.");
		} catch (IllegalArgumentException e) {
			//This is the expected behavior when there is no prop file at the passed location
			String expectedMessage = "No property file at that location: " + propsPath;
			assertEquals(expectedMessage, e.getMessage());
		}
		
		//file has no read permissions
		propsName = "converter.testing-noread-permission.properties";
		propsPath = TEST_PROPS_DIR + File.separator + propsName;
		try {
			actual = tester.getConverters(propsPath);
			fail("An exception should have been thrown.");
		} catch (IOException e) {
			//This is the expected behavior when the file can't be loaded for some reason
		} catch (Exception e) {
			fail("We should get an IOException.");
		}
		
		//file is there and can be read
		propsName = "converter.testing-load.properties";
		propsPath = TEST_PROPS_DIR + File.separator + propsName;
		try {
			actual = tester.getConverters(propsPath);
		} catch (Exception e) {
			fail("We shouldn't get an exception for a valid properties file.");
		}
		assertNotNull(actual);
		
		//the loaded file is a copy of the tikiwiki converter properties
		//To get the number of converters in a file run this from the commandline. 
		//(The last number is the number of converters for that file)
		//$shell$ egrep "^[^#]" converter.testing-load.properties | perl -ne '$i++; print "$i\n";'
		int expectedSize = 35; 
		assertEquals(expectedSize, actual.size());
		String actualFirstProp = actual.get(0);
		String expectedFirstProp = "Tikiwiki.0001-switch.page-history-preservation=true";
		assertEquals(expectedFirstProp, actualFirstProp);
	}
	

	public void testConvert() {
		String propsName = "converter.testing-convert.properties";
		String propsPath = TEST_PROPS_DIR + File.separator + propsName;

		//set some pages so that we can test that the convert occurred
		String pagename = "SampleEngine-Input1.txt";
		String inputpath = TEST_PROPS_DIR + File.separator + pagename;
 		Vector<String> names = tester.getPageNames();
		names.add(inputpath);
		
		//figure out where the output's going to be saved, and clean the pre-convert state
		String output = tester.getOutputDir();
		File outputDir = new File(output);
		File inputFile = new File(inputpath);
		File outputPath = new File(outputDir + "/" + pagename);
		if (outputDir.exists() && outputPath.exists()) {
				outputPath.delete();
				assertTrue(!outputPath.exists());
		}
		
		//set up some converter engine necessary stuff
		tester.registerFeedbackWindow(new FeedbackWindow()); //We do this so the necessary State object gets generated
		
		try {
			tester.convert(propsPath);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
		
		assertTrue(outputDir.exists());
		
		File outputFile = new File(UWC_BASE_DIR + "/output/output/" + pagename);
		assertTrue(outputFile.exists());
		
		String expected = "*BOLD*";
		String actual = null;
		try {
			actual = FileUtils.readTextFile(outputFile);
		} catch (IOException e) {
			fail("Should not cause an IOException");
		}
		assertNotNull(actual);
		actual = actual.trim();
		assertEquals(expected, actual);
	}
	
	public void testSaveSettings() {
//		FIXME - need to test new UWCUserSettings object, and how it interacts with UWCGuiModel
//		fail(); 
	}
	
	public void testGetSettings() {
//		fail();
	}
}
