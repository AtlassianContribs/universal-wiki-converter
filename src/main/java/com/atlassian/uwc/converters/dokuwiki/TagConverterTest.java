package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TagConverterTest extends TestCase {

	TagConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TagConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testConvertTags() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "{{tag>tag6 Food tag5}}\n";
		page.setOriginalText(input);
		tester.convert(page);
		assertNotNull(page.getLabels());
		assertEquals(3, page.getLabels().size());
		expected = "tag5, food, tag6";
		actual = page.getLabelsAsString();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		expected = "\n";
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTags_quotes() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "{{tag>\"foo bar\" baz \".less{}\"}}\n" + 
				"";
		page.setOriginalText(input);
		tester.convert(page);
		assertNotNull(page.getLabels());
		assertEquals(3, page.getLabels().size());
		expected = "baz, less{}, foo_bar";
		actual = page.getLabelsAsString();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertTags_none() {
		Page page = new Page(null);
		String input, expected, actual;
		input = "{{tag>}}\n" + 
				"";
		page.setOriginalText(input);
		tester.convert(page);
		assertNotNull(page.getLabels());
		assertEquals(0, page.getLabels().size());
		actual = page.getLabelsAsString();
		assertNull(actual);
	}

}
