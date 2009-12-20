package com.atlassian.uwc.converters.sharepoint;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All Tests for Sharepoint Converter Project");
		//$JUnit-BEGIN$
		suite.addTestSuite(BoldConverterTest.class);
		suite.addTestSuite(BreakConverterTest.class);
		suite.addTestSuite(CleanConverterTest.class);
		suite.addTestSuite(ColorConverterTest.class);
		suite.addTestSuite(EmphasisConverterTest.class);
		suite.addTestSuite(HeaderConverterTest.class);
		suite.addTestSuite(InlineConverterTest.class);
		suite.addTestSuite(LinkConverterTest.class);
		suite.addTestSuite(ListConverterTest.class);
		suite.addTestSuite(ParagraphConverterTest.class);
		suite.addTestSuite(QuoteWSConverterTest.class);
		suite.addTestSuite(SeperateSyntaxesTest.class);
		suite.addTestSuite(SharepointConverterTest.class);
		suite.addTestSuite(SimpleImageConverterTest.class);
		suite.addTestSuite(TableConverterTest.class);
		suite.addTestSuite(UnderlineConverterTest.class);
		//$JUnit-END$
		return suite;
	}

}
