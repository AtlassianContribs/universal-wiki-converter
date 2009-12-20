package com.atlassian.uwc.converters.socialtext;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All Socialtext Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AttachmentConverterTest.class);
		suite.addTestSuite(ImageSizeConverterTest.class);
		suite.addTestSuite(ImageWhitespaceConverterTest.class);
		suite.addTestSuite(IncludeIllegalConverterTest.class);
		suite.addTestSuite(InlineExternalImagesConverterTest.class);
		suite.addTestSuite(LabelConverterTest.class);
		suite.addTestSuite(PagenameConverterTest.class);
		suite.addTestSuite(SearchConverterTest.class);
		suite.addTestSuite(SpaceConverterTest.class);
		suite.addTestSuite(TableConverterTest.class);
		//$JUnit-END$
		return suite;
	}

}
