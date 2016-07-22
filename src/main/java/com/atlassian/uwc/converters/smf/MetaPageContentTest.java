package com.atlassian.uwc.converters.smf;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MetaPageContentTest extends TestCase {

	MetaPageContent tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new MetaPageContent();
		PropertyConfigurator.configure("log4j.properties");
		tester.setProperties(new Properties());
	}

	public void testConvertMeta2PageContent() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "abc");
		meta.setProperty("useremail", "abc@def.org");
		meta.setProperty("time", "1234567890");
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addname", "true");
		props.setProperty("addtime", "true");
		props.setProperty("nameformat", "profile");
		props.setProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		
		expected = "*Original Poster:* [~abc]\n" +
				"*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertMeta2PageContent_NullMeta() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "null");
		meta.setProperty("useremail", "null");
		meta.setProperty("time", "null");
		meta.setProperty("type", "null");
		Properties props = tester.getProperties();
		props.setProperty("addname", "true");
		props.setProperty("addtime", "true");
		props.setProperty("nameformat", "profile");
		props.setProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		
		expected = "Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}	
	
	public void testConvertMeta2PageContent_addnamefalse() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "abc");
		meta.setProperty("useremail", "abc@def.org");
		meta.setProperty("time", "1234567890");
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addname", "false");
		props.setProperty("addtime", "true");
		props.setProperty("nameformat", "profile");
		props.setProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		
		expected = "*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertMeta2PageContent_addtimefalse() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "abc");
		meta.setProperty("useremail", "abc@def.org");
		meta.setProperty("time", "1234567890");
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addname", "true");
		props.setProperty("addtime", "false");
		props.setProperty("nameformat", "profile");
		props.setProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		
		expected = "*Original Poster:* [~abc]\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertMeta2PageContent_NameFormat() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "abc");
		meta.setProperty("useremail", "abc@def.org");
		meta.setProperty("time", "1234567890");
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addname", "true");
		props.setProperty("addtime", "true");
		props.setProperty("nameformat", "text");
		props.setProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		
		expected = "*Original Poster:* abc\n" +
				"*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		props.setProperty("nameformat", "profile");
		expected = "*Original Poster:* [~abc]\n" +
				"*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		props.setProperty("nameformat", "email");
		expected = "*Original Poster:* [abc|mailto:abc@def.org]\n" +
				"*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		meta.setProperty("username", "Simple Machines"); //special user
		meta.setProperty("userid", "0");
		expected = "*Original Poster:* Simple Machines\n" +
				"*Original Timestamp:* 2009-02-13 18:31:30\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		props.setProperty("nameformat", "profile");
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertMeta2PageContent_timeformat() {
		String input, expected, actual;
		input = "Testing 123";
		Properties meta = new Properties();
		meta.setProperty("username", "abc");
		meta.setProperty("useremail", "abc@def.org");
		meta.setProperty("time", "1234567890");
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addname", "true");
		props.setProperty("addtime", "true");
		props.setProperty("nameformat", "profile");
		props.setProperty("timeformat", "hmma");
		
		expected = "*Original Poster:* [~abc]\n" +
				"*Original Timestamp:* 631PM\n" +
				"\n" +
				"Testing 123";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertMeta2PageContent_addchildrenmacro() {
		String input, expected, actual;
		input = "foobar";
		Properties meta = new Properties();
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addchildrenmacro", "true");
		
		expected = "foobar\n" +
				"*Replies*\n" +
				"{children:sort=creation}\n" +
				"";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		meta.setProperty("type", "re");
		expected = "foobar";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testConvertMeta2PageContent_addgallerymacro() {
		String input, expected, actual;
		input = "foobar";
		Properties meta = new Properties();
		meta.setProperty("type", "top");
		Properties props = tester.getProperties();
		props.setProperty("addgallerymacro", "true");
		
		expected = "foobar\n" +
				"{gallery:title=Attached Images}\n" +
				"";
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		meta.setProperty("type", "re");
		actual = tester.convertMeta2PageContent(input, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
