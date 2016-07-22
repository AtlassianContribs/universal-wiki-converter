package com.atlassian.uwc.converters.mediawiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class DecodeEntitiesTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	DecodeEntities tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new DecodeEntities();
	}

	public void testDecodeEntities() {
		String input = "noentitieshere.txt";
		String expected = input;
		String actual = tester.decodeEntities(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Hello%21World.txt";
		expected = "Hello!World.txt";
		actual = tester.decodeEntities(input);
		log.debug(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Hello%2FWorld.txt";
		expected = "Hello/World.txt";
		actual = tester.decodeEntities(input);
		log.debug(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "Hello%2CWorld.txt";
		expected = "Hello,World.txt";
		actual = tester.decodeEntities(input);
		log.debug(actual);
		assertNotNull(actual);
		assertEquals(expected, actual);

	}

}
