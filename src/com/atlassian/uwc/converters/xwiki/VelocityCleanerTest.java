package com.atlassian.uwc.converters.xwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class VelocityCleanerTest extends TestCase {

	VelocityCleaner tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new VelocityCleaner();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testCleanVelocity() {
		String input, expected, actual;
		input = "#set($something = \"something\")";
		expected = tester.CLEANED_VELOCITY_INFO_BOX;
		actual = tester.cleanVelocity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCleanVelocity2() {
		String input, expected, actual;
		input = "{pre}\n" +
				"#set($something = \"something\")\n" +
				"{/pre}\n" +
				"{code}\n" +
				"#set($something = \"something\")\n" +
				"{code}\n";
		expected = input;
		actual = tester.cleanVelocity(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testTokenizePre() {
		String input, expected, actual;
		input = "before\n" +
				"{pre}\n" +
				"something\n" +
				"{/pre}\n" +
				"after";
		actual = tester.tokenizePre(input);
		assertNotNull(actual);
		assertTrue(actual.startsWith("before\n"));
		assertFalse(actual.startsWith("before\n{pre}"));
		assertFalse(actual.contains("{pre}\nsomething\n{/pre}"));
		assertTrue(actual.endsWith("after"));
		assertFalse(actual.endsWith("{/pre}\nafter"));
	}

}
