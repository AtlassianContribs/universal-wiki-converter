package com.atlassian.uwc.converters;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.IllegalChar.Type;
import com.atlassian.uwc.ui.Page;

public class IllegalPageNameConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	IllegalNameConverter tester = null;
	Page page = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new IllegalPageNameConverter();
		tester.getProperties().setProperty("illegalnames-urldecode", "true");
		page = new Page(new File("")); 
	}

	public void testAccessToIllegalNameConverterMethod() {
		String input = "testing";
		String expected = input;
		String actual = tester.convertIllegalName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_None() {
		String input = "NoIllegalChars";
		String expected = input;
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_One() {
		String input = "OneIllegalChar[End";
		String expected = "OneIllegalChar(End";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "OneIllegalChar:End";
		expected = "OneIllegalChar.End";
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_GoodUrlEncoding() {
		String input = "OneGoodUrlEncodedChar%5fEnd";
		String expected = "OneGoodUrlEncodedChar_End";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "OneGoodUrlEncodedChar%43End";
		expected = "OneGoodUrlEncodedCharCEnd";
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvert_IllegalUrlEncoding() {
		String input = "OneBadUrlEncoding%7bEnd";
		String expected = "OneBadUrlEncoding(End"; //translates encoding, then gets legal equivalent 
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testMultipleIllegalChars() {
		String input = "MultBadChars#and:End";
		String expected = "MultBadCharsNo.and.End";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testBothTypesOfIllegalChars() {
		String input = "BothBadChars%7band{End";
		String expected = "BothBadChars(and(End";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testBadStart() {
		String input = "$MONEY";
		String expected = "_MONEY";
		String actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testColon() {
		String input, expected, actual;
		input = "ABC:DEF";
		expected = "ABC.DEF";
		actual = this.getActual(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testCreateIllegalChar_Backslash() {
		TreeMap<String,String> properties = tester.getIllegalCharProperties();
		String key = "illegalchar.backslash.replacement";
		IllegalChar.Type type = IllegalChar.Type.ANYWHERE;
		
		IllegalChar actual = tester.createIllegalChar(properties, key, type);
		IllegalChar expected = new IllegalChar("\\", "-", IllegalChar.Type.ANYWHERE);
		
		log.debug("expected = " + expected);
		log.debug("actual = " + actual);
		assertTrue(expected.equals(actual));
	}
	
	public void testGetIllegalChars() {
		
		List<IllegalChar> actual = tester.getIllegalCharObjects();
		assertNotNull(actual);
		
		int expectedSize = 17; 
		assertEquals(expectedSize, actual.size());
		
		int index = 0;
		for (Iterator iter = actual.iterator(); iter.hasNext();) {
			IllegalChar illegal = (IllegalChar) iter.next();
			String actVal = illegal.getValue();
			String actReplace = illegal.getReplacement();
			IllegalChar.Type actType = illegal.getType();
			log.debug("Illegal actual:\n" + illegal);
			verifyIllegalObject(actVal, actReplace, actType, index);
			index++;
		}
	}
	
	String[] expectedIllegalValues = {
			":",
			";",
			"<",
			">",
			"@",
			"/",
			"\\",
			"|",
			"#",
			"[",
			"]",
			"{",
			"}",
			"^",
			"$",
			"..",
			"~",
	};
	String[] expectedIllegalReplacement = {
			".",
			".",
			"_",
			"_",
			"at",
			"-",
			"-",
			"-",  //not counting pipe now
			"No.",
			"(",
			")",
			"(",
			")",
			"-",
			"_",
			"_",
			"_"
	};
	
	IllegalChar.Type[] expectedIllegalType = {
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,  //pipe
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.ANYWHERE,
		IllegalChar.Type.START_ONLY,
		IllegalChar.Type.START_ONLY,
		IllegalChar.Type.START_ONLY,
	};
	
	private void verifyIllegalObject(String actVal, String actReplace, Type actType, int index) {
		String expVal = expectedIllegalValues[index];
		String expReplace = expectedIllegalReplacement[index];
		IllegalChar.Type expType = expectedIllegalType[index];
		
		assertEquals(expVal, actVal);
		assertEquals(expReplace, actReplace);
		assertEquals(expType, actType);
	}

	public void testSearchAndReplaceIllegalChars() {
		String input = "mouse";
		IllegalChar illegal = new IllegalChar("u", "o", IllegalChar.Type.ANYWHERE);
		List<IllegalChar> illegalChars = new ArrayList<IllegalChar>();
		illegalChars.add(illegal);

		String expected = "moose";
		String actual = tester.searchAndReplaceIllegalChars(input, illegalChars);
		assertNotNull(actual);
		assertEquals(expected, actual);

		input = "CBC";
		IllegalChar illegal2 = new IllegalChar("C", "A", IllegalChar.Type.START_ONLY);
		illegalChars.add(illegal2);
		
		expected = "ABC";
		actual = tester.searchAndReplaceIllegalChars(input, illegalChars);
		assertNotNull(actual);
		assertEquals(expected, actual);


	}
	
	public void testGetProperties() {
		TreeMap<String, String> expected = createTestProperties();
		TreeMap<String, String> actual = tester.getIllegalCharProperties();

		assertNotNull(actual);
		assertEquals(expected.size(), actual.keySet().size());
		
		Iterator expIt = expected.keySet().iterator();
		while (expIt.hasNext()) {
			String expKey = (String) expIt.next();
			log.debug("key = " + expKey);
			assertTrue(actual.containsKey(expKey));
			String actVal = actual.get(expKey);
			String expVal = expected.get(expKey);
			assertEquals("value: ", expVal, actVal);
		}
	}
	
	private TreeMap<String, String> createTestProperties() {
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("illegalchar.colon.replacement", ".");
		map.put("illegalchar.semicolon.replacement", ".");
		map.put("illegalchar.lessthan.replacement", "_");
		map.put("illegalchar.greaterthan.replacement", "_");
		map.put("illegalchar.at.replacement", "at");
		map.put("illegalchar.forwardslash.replacement", "-");
		map.put("illegalchar.backslash.replacement", "-");
		map.put("illegalchar.pipe.replacement", "-");
		map.put("illegalchar.hash.replacement", "No.");
		map.put("illegalchar.leftbracket.replacement", "(");
		map.put("illegalchar.rightbracket.replacement", ")");
		map.put("illegalchar.leftcurlybrace.replacement", "(");
		map.put("illegalchar.rightcurlybrace.replacement", ")");
		map.put("illegalchar.carat.replacement", "-");
		map.put("illegalstart.dollar.replacement", "_");
		map.put("illegalstart.twodots.replacement", "_");
		map.put("illegalstart.tilda.replacement", "_");
		return map;
	}

	public void testGetAnywhere() {
		String[] required = { "colon", "hash" };
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("illegalchar.colon.replacement", "A");
		Vector<IllegalChar> actual = tester.getIllegalAnywhere(properties, required);
		assertNotNull(actual);
		
		int expectedSize = 2;
		assertEquals(expectedSize , actual.size());
		
		IllegalChar exp1 = new IllegalChar(":", "A", IllegalChar.Type.ANYWHERE);
		IllegalChar exp2 = new IllegalChar("#", "_", IllegalChar.Type.ANYWHERE);
		
		IllegalChar act1 = actual.get(0);
		IllegalChar act2 = actual.get(1);
		
		assertTrue(exp1.equals(act1));
		assertTrue(exp2.equals(act2));
		
	}
	
	public void testGetStarting() {
		String[] required = { "dollar", "tilde" };
		TreeMap<String,String> properties = new TreeMap<String,String>();
		properties.put("illegalstart.dollar.replacement", "A");
		Vector<IllegalChar> actual = tester.getIllegalStarting(properties, required);
		assertNotNull(actual);
		
		int expectedSize = 2;
		assertEquals(expectedSize , actual.size());
		
		IllegalChar exp1 = new IllegalChar("$", "A", IllegalChar.Type.START_ONLY);
		IllegalChar exp2 = new IllegalChar("~", "_", IllegalChar.Type.START_ONLY);
		
		IllegalChar act1 = actual.get(0);
		IllegalChar act2 = actual.get(1);
		
		assertTrue(exp1.equals(act1));
		assertTrue(exp2.equals(act2));
		 
	}
	
	
	public void testCreateIllegalChar() {
		TreeMap<String, String> properties = new TreeMap<String, String> ();
		properties.put("illegalchar.colon.replacement", "b");
		String key = "illegalchar.colon.replacement";
		IllegalChar.Type type = IllegalChar.Type.START_ONLY;
		
		IllegalChar expected = new IllegalChar(":", "b", IllegalChar.Type.START_ONLY);
		IllegalChar actual = tester.createIllegalChar(properties, key, type);
		
		assertTrue(expected.equals(actual));
		
	}
	public void testGetIllegalCharValue() {
		String key = "illegalchar.hash.replacement";
		String expected = "#";
		String actual = tester.getIllegalCharValue(key);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDefaultReplacement() {
		String expected = "_";
		String actual = tester.getDefaultReplacement();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testIsLegalReplacement() {
		//simple test
		String input = "a";
		boolean expected = true;
		boolean actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		
		//anywhere
		input = ":";
		expected = false;
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = ":";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = ";";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "<";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = ">";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "@";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "/";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "\\"; 
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "|";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "#";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "[";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "]";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "{";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "}";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		input = "^";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.ANYWHERE);
		assertEquals(expected, actual);
		//start only
		input = "$";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.START_ONLY);
		assertEquals(expected, actual);
		input = "..";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.START_ONLY);
		assertEquals(expected, actual);
		input = "~";
		actual = tester.isLegalReplacement(input, IllegalChar.Type.START_ONLY);
		assertEquals(expected, actual);
	}
	
	public void testIllegal() {
		//no illegal chars
		String input = "abc";
		boolean expected = false;
		boolean actual = tester.illegal(input);
		assertEquals(expected, actual);
		
		input = "a-bc";
		actual = tester.illegal(input);
		assertEquals(expected, actual);

		//illegal chars
		input = "a:bc";
		expected = true;
		actual = tester.illegal(input);
		assertEquals(expected, actual);
		
		input = "a_b[cd";
		actual = tester.illegal(input);
		assertEquals(expected, actual);
		
		input = "$abc";
		actual = tester.illegal(input);
		assertEquals(expected, actual);
	}
	
	public void testAddingToState() {
		String input = "abc";
		String expected = "abc";
		String actual = tester.convertIllegalName(input);
		HashSet<String> state = tester.getIllegalPagenames();
		assertNull(state);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "$abc:";
		expected = "_abc.";
		actual = tester.convertIllegalName(input);
		state = tester.getIllegalPagenames();
		assertNotNull(state);
		assertEquals(1, state.size());
		
		assertTrue(state.contains(input));
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "a%20b";
		expected = "a b";
		actual = tester.convertIllegalName(input);
		state = tester.getIllegalPagenames();
		assertNotNull(state);
		assertEquals(2, state.size());
		
		assertTrue(state.contains(input));
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testDecodeUrl() {
		String input, expected, actual;
		//utf-4
		input = "Detta är en sida med åäö och ÅÄÖ";
		expected = input;
		try {
			String encoded = URLEncoder.encode(input, "utf-8");
			actual = tester.searchAndReplaceIllegalChars(encoded, tester.getIllegalCharObjects());
			assertNotNull(actual);
			assertEquals(expected, actual);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		tester.getProperties().remove("illegalnames-urldecode");
		actual = tester.searchAndReplaceIllegalChars(input, tester.getIllegalCharObjects());
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//random utf-8 korean character - see UWC-310
		input = "\uce73"; 
		expected = input;
		actual = tester.searchAndReplaceIllegalChars(input, tester.getIllegalCharObjects());
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	/* Helper Methods */
	public String getActual(String input) {
		String actual;
		page.setName(input);
		tester.convert(page);
		actual = page.getName();
		return actual;
	}
}
