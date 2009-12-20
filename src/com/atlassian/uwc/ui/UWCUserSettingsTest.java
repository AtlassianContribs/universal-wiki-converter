package com.atlassian.uwc.ui;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;

public class UWCUserSettingsTest extends TestCase {

	private static final String LOGIN_VAL2 = "bah.humbug";
	private static final String LOGIN_VAL = "tada";
	private static final String PROPS_FILE_TEST = "usersettings.test.properties";
	private static final String TEST_SETTINGS_DIR = "sampleData/UWCSettings/";
	UWCUserSettings tester = null;
	Logger log = Logger.getLogger(this.getClass());
	private String testSettings;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("Log4j.properties");
		tester = new UWCUserSettings(null); //don't auto-load from the default settings location
		testSettings = TEST_SETTINGS_DIR + PROPS_FILE_TEST;
	}

	public void testGetSettingsFromFile() {
		String preLogin = tester.getLogin();
		String defaultLogin = "login";
		assertEquals(defaultLogin, preLogin);
		
		tester.getSettingsFromFile(testSettings);
		
		String expected = LOGIN_VAL;
		String actual = tester.getLogin();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSaveSettingsToFile() {
		
		String input = LOGIN_VAL2;
		tester.setLogin(input);
		
		tester.saveSettingsToFile(this.testSettings);

		String actual = null;
		try {
			actual = FileUtils.readTextFile(new File(this.testSettings));
		} catch (IOException e) {
			fail("Reading file shouldn't cause exception");
			e.printStackTrace();
		}
		String expected = LOGIN_VAL2;
		assertNotNull(actual);
		String[] breakup = actual.split("login=");
		assertTrue(breakup.length == 2);
		assertTrue(breakup[1].startsWith(expected));
		
		//reset test settings
		resetTestSettings("login", LOGIN_VAL);
	}


	private void resetTestSettings(String key, String val) {
		String text = key + "=" + val;
		FileUtils.writeFile(text, this.testSettings);
		
	}

	public void testGetSettings() {
		Properties props = tester.getSettings();
		//test defaults
		String actual = (String) props.get(UWCUserSettings.PROPKEY_LOGIN);
		String expected = "login";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}

	public void testSetSettings() {
		String currentWiki = tester.getWikitype();
		String wikitype = "mediawiki";
		assertFalse(wikitype.equals(currentWiki));
		
		Properties props = new Properties();
		props.setProperty(UWCUserSettings.PROPKEY_WIKITYPE, wikitype);
		tester.setSettings(props);
		
		String actual = tester.getWikitype();
		String expected = wikitype;
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testBadLoad() {
		String filename = "settings.none-here.properties";
		String path = TEST_SETTINGS_DIR + filename;

		assertTrue(tester.feedback == Feedback.NONE);
		
		tester.getSettingsFromFile(path);
		
		assertTrue(tester.feedback == Feedback.BAD_SETTINGS_FILE);
	}
	
	public void testBadSave() {
		//XXX If this test is failing make sure that the
		//sample file has permissions chmod 444
		String filename = "settings.bad-permissions.properties";
		String path = TEST_SETTINGS_DIR + filename;

		assertTrue(tester.feedback == Feedback.NONE);
		
		tester.saveSettingsToFile(path);
		
		assertTrue(tester.feedback == Feedback.BAD_SETTINGS_FILE);
	}
	
	public void testStripProtocol() {
		String input, expected, actual = "";
		input = "localhost";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "localhost:8082";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something.com";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "www.something.com";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something.com:8080";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something.com/confluence";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something.com:8080/confluence";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "something.com/confluence/";
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//strip out normal http protocol
		String base = "localhost:8082";
		String protocol = "http://";
		input = protocol + base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com";
		input = protocol + base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "www.something.com";
		input = protocol+base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com";
		input = protocol+ base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com:8080";
		input = protocol + base; 
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com/confluence";
		input = protocol + base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com:8080/confluence";
		input = protocol + base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com/confluence/";
		input = protocol + base;
		expected = base;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//don't strip out unsupported security protocol https
		base = "localhost:8082";
		protocol = "https://";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "www.something.com";
		input = protocol+base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com";
		input = protocol+ base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com:8080";
		input = protocol + base; 
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com/confluence";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com:8080/confluence";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		base = "something.com/confluence/";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);

		//don't strip out broken http protocol
		protocol = "http:";
		base = "localhost:8082";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		protocol = "http:/";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		protocol = "htp://";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		protocol = "http:";
		input = protocol + base;
		expected = input;
		actual = UWCUserSettings.stripProtocol(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testIsValid() {
		String value = "";
		UWCUserSettings.Setting setting = UWCUserSettings.Setting.URL;

		value = "localhost";
		assertTrue(UWCUserSettings.isValid(setting, value));
		
		value = "http://localhost";
		assertTrue(UWCUserSettings.isValid(setting, value));
		
		value = "something.com";
		assertTrue(UWCUserSettings.isValid(setting, value));
		
		value = "www.something.com";
		assertTrue(UWCUserSettings.isValid(setting, value));
		
		value = "https://localhost";
		assertTrue(UWCUserSettings.isValid(setting, value));
		
		//broken protocol
		value = "http:localhost";
		assertFalse(UWCUserSettings.isValid(setting, value));

		value = "htp://localhost";
		assertFalse(UWCUserSettings.isValid(setting, value));

		value = "http:/localhost";
		assertFalse(UWCUserSettings.isValid(setting, value));

		value = "http:localhost:8082";
		assertFalse(UWCUserSettings.isValid(setting, value));
		
		//login
		value = "tada";
		setting = UWCUserSettings.Setting.LOGIN;
		assertTrue(UWCUserSettings.isValid(setting, value));
		
	}
	
	public void testToString() {
		UWCUserSettings.Setting input = UWCUserSettings.Setting.LOGIN;
		String expected = "LOGIN";
		String actual = input.toString();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = UWCUserSettings.Setting.URL;
		expected = "ADDRESS";
		actual = input.toString();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
}
