package com.atlassian.uwc.converters.smf;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class EntityConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	EntityConverter tester = null;
	protected void setUp() throws Exception {
		tester = new EntityConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testConvert() {
		String input, expected, actual;
		input = "Testing 123 &quot; &gt; &lt; &amp; &#039;";
		expected = "Testing 123 \" > < & '";
		Page page = new Page(null);
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
