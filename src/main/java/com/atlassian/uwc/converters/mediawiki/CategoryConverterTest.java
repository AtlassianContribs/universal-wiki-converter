package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class CategoryConverterTest extends TestCase {

	CategoryConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new CategoryConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String input, expected, actual;
		input = "[[Category:abc]]\n" +
				"[[category: def]]\n" +
				"[[Category: testing123]]\n" +
				"[[Category: withCAPS]]\n" +
				"[[category: An Example Category]]\n" +
				"[[Category: An_Example_Category]]\n" +
				"[[Category:thislabel:hascolons]]\n" + 
				"[[Category:thislabel(hasparens)]]\n";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		expected = "withCAPS, thislabelhasparens, def, AnExampleCategory, testing123, abc, thislabelhascolons";
		actual = page.getLabelsAsString();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
