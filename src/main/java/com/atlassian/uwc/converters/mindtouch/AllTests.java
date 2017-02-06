package com.atlassian.uwc.converters.mindtouch;

import com.atlassian.uwc.exporters.MindtouchExporterTest;
import com.atlassian.uwc.hierarchies.FilepathHierarchyTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Mindtouch Converter Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(CommentParserTest.class);
		suite.addTestSuite(TableParserTest.class);
		suite.addTestSuite(TagParserTest.class);
		suite.addTestSuite(TitleParserTest.class);
		suite.addTestSuite(StyleConverterTest.class);
		suite.addTestSuite(AttachmentParserTest.class);
		suite.addTestSuite(LinkParserTest.class);
		suite.addTestSuite(ImageParserTest.class);
		//slightly more general, but having mindtouch specific edge cases
		suite.addTestSuite(FilepathHierarchyTest.class);
		//external to the mindtouch directory
		suite.addTestSuite(MindtouchExporterTest.class);
		//$JUnit-END$
		return suite;
	}

}
