package com.atlassian.uwc.hierarchies;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllHierarchyTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllHierarchyTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(DocDirectoryHierarchyTest.class);
		suite.addTestSuite(MetaHierarchyTest.class);
		suite.addTestSuite(FilepathHierarchyTest.class);
		suite.addTestSuite(TWikiHierarchyTest.class);
		suite.addTestSuite(DokuwikiHierarchyTest.class);
		suite.addTestSuite(Many2OneHierarchyTest.class);
		suite.addTestSuite(ContentHierarchyTest.class);
		suite.addTestSuite(SmfHierarchyTest.class);
		//$JUnit-END$
		return suite;
	}

}
