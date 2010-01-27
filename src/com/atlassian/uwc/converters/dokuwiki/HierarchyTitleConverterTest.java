package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class HierarchyTitleConverterTest extends TestCase {

	HierarchyTitleConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new HierarchyTitleConverter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testFixTitle() {
		String input, expected, actual;
		input = "test_a123";
		expected = "Test A123";
		actual = tester.fixTitle(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCasify() {
		String input, expected, actual;
		input = "test a123";
		expected = "Test A123";
		actual = tester.casify(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
