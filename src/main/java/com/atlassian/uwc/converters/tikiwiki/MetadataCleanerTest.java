package com.atlassian.uwc.converters.tikiwiki;

import junit.framework.TestCase;

public class MetadataCleanerTest extends TestCase {

	MetadataCleaner tester = null;
	protected void setUp() throws Exception {
		super.setUp();
		tester = new MetadataCleaner();
	}

	public void testCleanMetadataOneVersion() {
		String input = "Date: Tue, 10 Jan 2006 20:24:55+00:00\n" +
		"Mime-Version: 1.0 (Produced by Tiki)\n" +
		"Content-Type: application/x-tikiwiki;\n" +
		"  pagename=testPage;\n" +
		"  flags=\"\";\n" +
		"  author=ajj41781;\n" +
		"  version=1;\n" +
		"  lastmodified=1090418964;\n" +
		"  author_id=139.136.45.124;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n" +
		"Content-Transfer-Encoding: binary\n" +
		"\n" +
		"There will be a file here...\n" ;
		String expected = "There will be a file here...\n" ;
		String actual = tester.cleanMetadata(input);
		assertEquals(expected, actual);
	}
	
	public void testCleanMetadataSeveralVersions() {
		String input = "Date: Tue, 10 Jan 2006 20:24:54+00:00\n" +
		"Mime-Version: 1.0 (Produced by Tiki)\n" +
		"Content-Type: multipart/mixed;\n" +
		"  boundary=\"=_multipart_boundary_158\"\n" +
		"\n" +
		"--=_multipart_boundary_158\n" +
		"Content-Type: application/x-tikiwiki;\n" +
		"  pagename=MySQL;\n" +
		"  flags=\"\";\n" +
		"  author=harkes00;\n" +
		"  version=2;\n" +
		"  lastmodified=1110392101;\n" +
		"  author_id=139.136.8.194;\n" +
		"  summary=\"\";\n" +
		"  hits=15;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n" +
		"Content-Transfer-Encoding: binary\n" +
		"\n" +
		"[http://www.mysql.com|MySQL] is an open-source RDBMS that's very easy to set up and use.  We sometimes use MySQL in the development phase of projects when we need a database we can administer ourselves because of frequent schema changes.  ((Hypersonic)) is another nice RDBMS for this purpose.  Recently, getting an ((Oracle)) instance made and administering it ourselves has become easier, so we're relying less on MySQL.\n" +
		"\n" +
		"--=_multipart_boundary_158\n" +
		"Content-Type: application/x-tikiwiki;\n" +
		"  pagename=MySQL;\n" +
		"  flags=\"\";\n" +
		"  author=harkes00;\n" +
		"  version=2;\n" +
		"  lastmodified=1110392017;\n" +
		"  author_id=139.136.8.194;\n" +
		"  summary=\"\";\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n" +
		"Content-Transfer-Encoding: binary\n" +
		"\n" +
		"[http://www.mysql.com|MySQL] is an open-source DBMS that's very easy to set up and use.  We sometimes use MySQL in the development phase of projects when we need a database we can administer ourselves because of frequent schema changes.  Recently, getting an ((Oracle)) instance made and administering it ourselves has become easier, so we're relying less on MySQL.\n" ;
		String expected ="[http://www.mysql.com|MySQL] is an open-source RDBMS that's very easy to set up and use.  We sometimes use MySQL in the development phase of projects when we need a database we can administer ourselves because of frequent schema changes.  ((Hypersonic)) is another nice RDBMS for this purpose.  Recently, getting an ((Oracle)) instance made and administering it ourselves has become easier, so we're relying less on MySQL.\n\n" ;
		String actual = tester.cleanMetadata(input);
		assertEquals(expected, actual);
	}
	
	public void testNoMetadata() {
		String input = "abc";
		String expected = input;
		String actual = tester.cleanMetadata(input);
		assertEquals(expected, actual);
	}

	public void testWithCarriageReturns() {
		String input = "Date: Tue, 10 Jan 2006 20:24:55+00:00\r\n" +
		"Mime-Version: 1.0 (Produced by Tiki)\r\n" +
		"Content-Type: application/x-tikiwiki;\r\n" +
		"  pagename=testPage;\r\n" +
		"  flags=\"\";\r\n" +
		"  author=ajj41781;\r\n" +
		"  version=1;\r\n" +
		"  lastmodified=1090418964;\r\n" +
		"  author_id=139.136.45.124;\r\n" +
		"  summary=\"\";\r\n" +
		"  hits=22;\r\n" +
		"  description=\"\";\r\n" +
		"  charset=iso-8859-1\r\n" +
		"Content-Transfer-Encoding: binary\r\n" +
		"\r\n" +
		"There will be a file here...\r\n" ;
		String expected = "There will be a file here...\r\n" ;
		String actual = tester.cleanMetadata(input);
		assertEquals(expected, actual);
	}
}
