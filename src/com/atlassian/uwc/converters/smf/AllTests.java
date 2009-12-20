package com.atlassian.uwc.converters.smf;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.atlassian.uwc.exporters.SMFExporterTest;
import com.atlassian.uwc.hierarchies.SmfHierarchyTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"All Smf Tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(AttachmentConverterTest.class);
		suite.addTestSuite(EntityConverterTest.class);
		suite.addTestSuite(ImageConverterTest.class);
		suite.addTestSuite(MetaPageContentTest.class);
		suite.addTestSuite(SMFExporterTest.class);
		suite.addTestSuite(SmfHierarchyTest.class);
		suite.addTestSuite(TransposeWSConverterTest.class);
		//$JUnit-END$
		return suite;
	}

}
