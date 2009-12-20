package com.atlassian.uwc.ui;

import com.atlassian.uwc.ui.listeners.ExportWikiListenerTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.atlassian.uwc.ui");
		//$JUnit-BEGIN$
		suite.addTestSuite(UWCUserSettingsTest.class);
		suite.addTestSuite(UWCForm3Test.class);
		suite.addTestSuite(PageTest.class);
		suite.addTestSuite(ConverterErrorsTest.class);
		suite.addTestSuite(UWCGuiModelTest.class);
		suite.addTestSuite(ConverterEngineTest.class);
		suite.addTestSuite(ExportWikiListenerTest.class); 
		//$JUnit-END$
		return suite;
	}

}
