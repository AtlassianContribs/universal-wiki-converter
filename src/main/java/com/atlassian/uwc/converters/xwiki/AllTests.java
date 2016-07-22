package com.atlassian.uwc.converters.xwiki;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All Tests for Xwiki Converter");
		//$JUnit-BEGIN$
		suite.addTestSuite(AttachmentConverterTest.class);
		suite.addTestSuite(BackSlashConverterTest.class);
		suite.addTestSuite(BoxConverterTest.class);
		suite.addTestSuite(ImageConverterTest.class);
		suite.addTestSuite(LinkConverterTest.class);
		suite.addTestSuite(ListConverterTest.class);
		suite.addTestSuite(QuoteConverterTest.class);
		suite.addTestSuite(TableConverterTest.class);
		suite.addTestSuite(VelocityCleanerTest.class);
		suite.addTestSuite(XmlCleanerTest.class);
		//$JUnit-END$
		return suite;
	}

}
