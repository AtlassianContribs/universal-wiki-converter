package com.atlassian.uwc.filters;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SocialtextFilterTest extends TestCase {

	SocialtextFilter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new SocialtextFilter();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testAccept() {
		File file = new File("sampleData/socialtext/SampleSocialtext-InputLinks.txt");
		assertTrue(file.exists());
		assertTrue(tester.accept(file));
		
		file = new File("sampleData/socialtext/SampleSocialtext-InputFilter/20090806120000.txt");
		assertTrue(tester.accept(file));
		
		file = new File("sampleData/socialtext/SampleSocialtext-InputFilter/20090805170912.txt");
		assertFalse(tester.accept(file));
		
		file = new File("sampleData/socialtext/SampleSocialtext-InputDeleted/20090814120000.txt");
		assertFalse(tester.accept(file));
		
		file = new File("sampleData/");
		assertTrue(tester.accept(file));
	}

}
