package com.atlassian.uwc.converters.dokuwiki;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class ExternalInternalLinksConverterTest extends TestCase {

	ExternalInternalLinksConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new ExternalInternalLinksConverter();
		PropertyConfigurator.configure("log4j.properties");
		tester.getProperties().setProperty("external-internal-links-identifier",
				"https?:\\/\\/(?:(?:foo-bar)|(?:dokuwiki))\\/");
	}

	public void testConvertExternalInternalLinks() {
		String input, expected, actual;
		input = "[[http://foo-bar/doku.php?do=recent&id=some:ns:index|ALIAS]]\n" + 
				"[[http://dokuwiki/doku.php?id=some:ns:index]]\n" +
				"[[http://dontchangethisone/doku.php?do=recent&id=some:ns:index]]\n" +
				"";
		expected = "[[some:ns:index|ALIAS]]\n" +
				"[[some:ns:index]]\n" +
				"[[http://dontchangethisone/doku.php?do=recent&id=some:ns:index]]\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testConvertRemoveMoreArgs() {
		String input, expected, actual;
		input = "[[http://foo-bar/doku.php?id=some:ns:tada&s[]=media&s[]=service|{{:some:image:image.png}}]]\n" +
				"[[http://foo-bar/doku.php?s[]=media&id=some:ns:tada]]\n";
		expected = "[[some:ns:tada|{{:some:image:image.png}}]]\n" +
				"[[some:ns:tada]]\n";
		actual = tester.convertExternalInternalLinks(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
}
