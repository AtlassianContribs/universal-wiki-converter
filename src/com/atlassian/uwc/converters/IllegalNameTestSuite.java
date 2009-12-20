package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.ConverterEngineTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All the unit test files that are relevant for the Illegal Name Handling
 * Code. See issue: <a href="http://developer.atlassian.com/jira/browse/UWC-106">uwc-106</a>
 */
public class IllegalNameTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("All Tests for Illegal Name Handling");
		//$JUnit-BEGIN$
		suite.addTestSuite(IllegalLinkNameConverterTest.class);
		suite.addTestSuite(IllegalPageNameConverterTest.class);
		suite.addTestSuite(ConverterEngineTest.class);
		suite.addTestSuite(IllegalCharTest.class);
		//$JUnit-END$
		return suite;
	}

}
