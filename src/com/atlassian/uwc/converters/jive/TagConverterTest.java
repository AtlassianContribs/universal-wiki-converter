package com.atlassian.uwc.converters.jive;

import java.util.TreeSet;

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

	public void testGetTags() {
		String actual, input, expected;
		input = "{tags: foo, bar}\n" + 
				"";
		expected = "foo, bar";
		Page page = new Page(null);
		page.setOriginalText(input);
		assertNull(page.getLabelsAsString());
		tester.convert(page);
		actual = page.getLabelsAsString();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
