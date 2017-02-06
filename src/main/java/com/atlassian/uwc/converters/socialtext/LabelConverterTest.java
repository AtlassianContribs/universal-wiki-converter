package com.atlassian.uwc.converters.socialtext;

import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class LabelConverterTest extends TestCase {

	LabelConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new LabelConverter();
	}

	public void testConvert() {
		String input, expected;
		input = "Subject: SampleSocialtext-InputLabels\n" + 
				"From: laura.kolker@gmail.com\n" + 
				"Date: 2009-08-03 12:00:00 EDT\n" + 
				"Category: test\n" + 
				"This is a sample page for labels. The following labels should be attached to the page: test, abc09, abc, abcanddef\n" + 
				"";
		Page page = new Page(null);
		assertTrue(page.getLabels().isEmpty());
		page.setOriginalText(input);
		tester.convert(page);
		assertFalse(page.getLabels().isEmpty());
		assertEquals("test", page.getLabelsAsString());
		
		input = "Category: test\n" + 
				"Category: TESTING123\n";
		page.setOriginalText(input);
		tester.convert(page);
		assertEquals(2, page.getLabels().size());
		assertEquals("TESTING123, test", page.getLabelsAsString());
	}

	public void testGetLabels() {
		String input, expected;
		input = "Category: test\n";
		Vector<String> actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("test", actual.get(0));
		
		actual = null;
		input = "Category: test\n" +
				"Category: TESTING123\n";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("test", actual.get(0));
		assertEquals("TESTING123", actual.get(1));
		
		actual = null;
		input = "Category: \n";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(0, actual.size());

		//valid nonword chars
		actual = null;
		input = "Category: abc$%-_+={}\\|\"\'~\n";
		expected = "abc$%-_+={}\\|\"\'~";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
		
		//invalid nonword chars
		actual = null;
		input = "Category: abc!#&()*,.:;<>?@[]^def\n";
		expected = "abcdef";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));

		//using properties
		actual = null;
		Properties props = new Properties();
		props.put("label-trans-1", "&=and");
		props.put("label-trans-2", "#=_hash_");
		tester.setProperties(props);

		input = "Category: abc!#&()*,.:;<>?@[]^def\n";
		expected = "abc_hash_anddef";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
		
		//properties of chars that need escaping
		props.clear();
		props.put("label-trans-1", "^=_");
		props.put("label-trans-2", "[=_");
		props.put("label-trans-3", "]=_");
		input = "Category: abc!#&()*,.:;<>?@[]^def\n";
		expected = "abc___def";
		actual = tester.getLabels(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(expected, actual.get(0));
		

	}

	public void testGetTransformationOptions() {
		
		HashMap<String, String> actual = tester.getTransformationOptions();
		assertNotNull(actual);
		assertTrue(actual.isEmpty());
		
		Properties props = new Properties();
		props.put("label-trans-1", "&=and");
		props.put("label-trans-2", "#=_hash_");
		tester.setProperties(props);

		actual = tester.getTransformationOptions();
		assertNotNull(actual);
		assertEquals(2, actual.size());
		assertEquals("and", actual.get("&"));
		assertEquals("_hash_", actual.get("#"));
	}
}
