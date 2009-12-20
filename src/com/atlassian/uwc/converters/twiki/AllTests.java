package com.atlassian.uwc.converters.twiki;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All TWiki Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AutoNumberListConverterTest.class);
		suite.addTestSuite(JavaRegexAndTokenizerConverterTest.class);
		suite.addTestSuite(TWikiPrepareAttachmentFilesConverterTest.class);
		suite.addTestSuite(TWikiRegexConverterCleanerWrapperTest.class);
		suite.addTestSuite(TWikLinkiPostProcessorTest.class);
		//$JUnit-END$
		return suite;
	}

}
