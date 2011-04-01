package com.atlassian.uwc.converters.jive;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class BlogConverterTest extends TestCase {

	BlogConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = null;
	protected void setUp() throws Exception {
		tester = new BlogConverter();
		PropertyConfigurator.configure("log4j.properties");
		props = new Properties();
		props.setProperty("spacemap-200-600", "testconf");
		props.setProperty("spacemap-201-14", "testconf2");
		props.setProperty("spacemap-202-14", "testconf3");
		props.setProperty("spacedata", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/jive/junit_resources/exported_jive_containerdata.txt");
		tester.setProperties(props);
	}
	
	public void testIsBlog() {
		String input, expected, actual;
		String type = "DOC";
		input = "{jive-export-meta:id=1001|version=2|type=" + type + "|containertype=2020|containerid=300|usercontainername=admin}\n" + 
				"<p>Page Content</p>";
		Page page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);
		
		assertFalse(page.isBlog());
		
		type = "BLOG";
		input = "{jive-export-meta:id=1001|version=2|type=" + type + "|containertype=2020|containerid=300|usercontainername=admin}\n" + 
				"<p>Page Content</p>";
		page = new Page(null);
		page.setName("Test Page");
		page.setConvertedText(input);
		page.setOriginalText(input);
		tester.convert(page);

		assertTrue(page.isBlog());

		
	}

}
