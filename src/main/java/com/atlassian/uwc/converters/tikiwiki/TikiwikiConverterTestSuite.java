package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TikiwikiConverterTestSuite extends TestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(
		"Test for com.atlassian.uwc.converters.tikiwiki");
		//$JUnit-BEGIN$
		suite.addTestSuite(TableConverterTest.class);
		suite.addTestSuite(WinNewlinesConverterTest.class);
		suite.addTestSuite(LinkConverterTest.class);
		suite.addTestSuite(EscapeBracketsTest.class);
		suite.addTestSuite(PlaintextLinkConverterTest.class);
		suite.addTestSuite(AttachmentConverterTest.class);
		suite.addTestSuite(CodeBlockConverterTest.class);
		suite.addTestSuite(PanelConverterTest.class);
		suite.addTestSuite(LeadingSpacesConverterTest.class);
		suite.addTestSuite(MetadataCleanerTest.class);
		suite.addTestSuite(HeaderConverterTest.class);
		suite.addTestSuite(PostListItemConverterTest.class);
		suite.addTestSuite(NestedListNewlineRemoverTest.class);
		suite.addTestSuite(ColspanPadderTest.class);
		suite.addTestSuite(MetadataTitleTest.class);
		//$JUnit-END$
		return suite;
	}
}
