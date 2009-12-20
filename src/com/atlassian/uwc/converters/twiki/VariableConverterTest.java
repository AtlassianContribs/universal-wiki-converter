package com.atlassian.uwc.converters.twiki;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class VariableConverterTest extends TestCase {

	private static final String EXP_URL = "http://localhost:8090";
	private static final String EXP_PUBURL = "http://localhost:8090/plugins/servlet/confluence/default/Global";
	private static final String EXP_WEB = "foobar";
	private static final String EXP_ATTACHURLPATH = "foobar:~UWCTOKENCURRENTPAGE~"; //have to do pagename later 
	private static final String EXP_ATTACHURL = "http://localhost:8090/display/foobar/~UWCTOKENCURRENTPAGE~";
	VariableConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	Properties props = new Properties();
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new VariableConverter();
		props.setProperty("vars-url", EXP_URL);
		props.setProperty("vars-attachurl", "~UWCTOKENURL~/display/~UWCTOKENCURRENTSPACE~/~UWCTOKENCURRENTPAGE~");
		props.setProperty("vars-attachurlpath", "~UWCTOKENCURRENTSPACE~:~UWCTOKENCURRENTPAGE~");
		props.setProperty("vars-puburl", "~UWCTOKENURL~/plugins/servlet/confluence/default/Global");
		props.setProperty("vars-puburlpath", "~UWCTOKENURL~/plugins/servlet/confluence/default/Global");
		props.setProperty("vars-web", "~UWCTOKENCURRENTSPACE~");
		props.setProperty("spacekey", EXP_WEB); //will be provided by converter engine, not converter properties
		tester.setProperties(props);
	}
	
	public void testConvertVariables_AttachUrl() {
		String input, expected, actual;
		input = "%ATTACHURL%";
		Page page = new Page(null);
		page.setName("testing123");
		page.setOriginalText(input);
		tester.convert(page);
		expected = EXP_ATTACHURL; 
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertVariables_AttachUrlPath() {
		String input, expected, actual;
		input = "%ATTACHURLPATH%";
		Page page = new Page(null);
		page.setName("testing123");
		page.setOriginalText(input);
		tester.convert(page);
		expected = EXP_ATTACHURLPATH;
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertVariables_Web() {
		String input, expected, actual;
		input = "%WEB%";
		expected = EXP_WEB;
		actual = tester.convertVariable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertVariables_PubUrl() {
		String input, expected, actual;
		input = "%PUBURL%";
		expected = EXP_PUBURL;
		actual = tester.convertVariable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertVariables_PubUrlPath() {
		String input, expected, actual;
		input = "%PUBURLPATH%";
		expected = EXP_PUBURL;
		actual = tester.convertVariable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertVariables_URL() {
		String input, expected, actual;
		input = "~UWCTOKENURL~";
		expected = EXP_URL;
		actual = tester.convertVariable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}


	public void testConvertVariables_CurrentSpace() {
		String input, expected, actual;
		input = "~UWCTOKENCURRENTSPACE~";
		String spacekey = EXP_WEB;
		expected = spacekey;
		Page page = new Page(null);
		page.setName("testing");
		page.setOriginalText(input);
		tester.convert(page);
		actual = page.getConvertedText();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testConvertVariables_InlineImageExample() {
		String input, expected, actual;
		input = "[[%PUBURL%/%WEB%/SampleTwiki-InputAttachments2/cow.jpg]]\n" + 
				"\n" + 
				"attachurl: %ATTACHURL%\n" + 
				"attachurl path: %ATTACHURLPATH%\n" + 
				"puburl: %PUBURL%\n" + 
				"web: %WEB%\n" + 
				"\n" + 
				"*Inline1:*\n" + 
				"(with hardcoded url and params)\n" + 
				"http://192.168.2.104/twiki/bin/viewfile/Sandbox/SampleTwiki-InputAttachments2?rev=1;filename=cow.jpg\n" + 
				"\n" + 
				"*Inline2:*\n" + 
				"(with PUBURL, hand writted web)\n" + 
				"%PUBURL%/Sandbox/SampleTwiki-InputAttachments2/cow.jpg\n" + 
				"\n" + 
				"*Inline3:*\n" + 
				"(with img tag, puburl)\n" + 
				"<img src=\"%PUBURL%/Sandbox/SampleTwiki-InputAttachments2/cow.jpg\" width=\"88\" height=\"31\" border=\"0\" alt=\"logo\" />\n" + 
				"";
		expected = "[[" + EXP_PUBURL +
				"/" + EXP_WEB +
				"/SampleTwiki-InputAttachments2/cow.jpg]]\n" + 
				"\n" + 
				"attachurl: " + EXP_ATTACHURL +"\n" + 
				"attachurl path: " + EXP_ATTACHURLPATH + "\n" + 
				"puburl: " + EXP_PUBURL + "\n" + 
				"web: " + EXP_WEB +
				"\n" + 
				"\n" + 
				"*Inline1:*\n" + 
				"(with hardcoded url and params)\n" + 
				"http://192.168.2.104/twiki/bin/viewfile/Sandbox/SampleTwiki-InputAttachments2?rev=1;filename=cow.jpg\n" + 
				"\n" + 
				"*Inline2:*\n" + 
				"(with PUBURL, hand writted web)\n" + 
				"" + EXP_PUBURL +
				"/Sandbox/SampleTwiki-InputAttachments2/cow.jpg\n" + 
				"\n" + 
				"*Inline3:*\n" + 
				"(with img tag, puburl)\n" + 
				"<img src=\"" + EXP_PUBURL +
				"/Sandbox/SampleTwiki-InputAttachments2/cow.jpg\" width=\"88\" height=\"31\" border=\"0\" alt=\"logo\" />\n" + 
				"";
		actual = tester.convertVariable(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
}
