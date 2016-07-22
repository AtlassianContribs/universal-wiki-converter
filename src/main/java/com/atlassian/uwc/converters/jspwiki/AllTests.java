package com.atlassian.uwc.converters.jspwiki;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All Tests - Jspwiki Converter");
		//$JUnit-BEGIN$
		suite.addTestSuite(TableConverterTest.class);
		suite.addTestSuite(LinkSpaceConverterTest.class);
		suite.addTestSuite(ImageConverterTest.class);
		suite.addTestSuite(HeaderConverterTest.class);
		suite.addTestSuite(DefinitionListConverterTest.class);
		suite.addTestSuite(PagenameConverterTest.class);
		suite.addTestSuite(StyleConverterTest.class);
		
		suite.addTestSuite(AltPrepConverterTest.class);
		suite.addTestSuite(AttachmentSpaceConverterTest.class);
		suite.addTestSuite(ListComboWhitespaceConverterTest.class);
		suite.addTestSuite(MultiLineMonoConverterTest.class);
		suite.addTestSuite(FileLinkBackslashConverterTest.class);
		//$JUnit-END$
		return suite;
	}

}
