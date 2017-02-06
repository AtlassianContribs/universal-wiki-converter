package com.atlassian.uwc.converters.tikiwiki;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;


public class MetadataTitleTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	MetadataTitle tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new MetadataTitle();
	}


	public void testGetTitleFromMetadata() {
		String input = "Date: Tue, 10 Jan 2006 20:24:55+00:00\n" +
		"Mime-Version: 1.0 (Produced by Tiki)\n" +
		"Content-Type: multipart/mixed;\n" +
		"  boundary=\"=_multipart_boundary_2\"\n" +
		"\n" +
		"--=_multipart_boundary_2\n" +
		"Content-Type: application/x-tikiwiki;\n" +
		"  pagename=MetadataTitlesTestPage;\n" +
		"  flags=\"\";\n" +
		"  author=xxxxxxxx;\n" +
		"  version=2;\n" +
		"  lastmodified=1168026094;\n" +
		"  author_id=xxx.xxx.xxx.xxx;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n" +
		"Content-Transfer-Encoding: binary\n" +
		"\n" +
		"Blah - Page Contents\n" +
		"\n" +
		"--=_multipart_boundary_2\n" +
		"\n" +
		"Other blah\n" +
		"\n" +
		"--=_multipart_boundary_2--\n";
		String expected = "MetadataTitlesTestPage";
		String actual = tester.getNameFromMetadata(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetTitleFromMetadata_NoTitle() {
		String input = "not useful";
		String actual = tester.getNameFromMetadata(input);
		assertNull(actual);
		
	}
	
	public void testGetTitleFromMetadata_WithEntities() {
		String input = "Date: Tue, 10 Jan 2006 20:24:55+00:00\n" +
		"Mime-Version: 1.0 (Produced by Tiki)\n" +
		"Content-Type: multipart/mixed;\n" +
		"  boundary=\"=_multipart_boundary_2\"\n" +
		"\n" +
		"--=_multipart_boundary_2\n" +
		"Content-Type: application/x-tikiwiki;\n" +
		"  pagename=Metadata%20Titles%20Test%20Page;\n" +
		"  flags=\"\";\n" +
		"  author=xxxxxxxx;\n" +
		"  version=2;\n" +
		"  lastmodified=1168026094;\n" +
		"  author_id=xxx.xxx.xxx.xxx;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n" +
		"Content-Transfer-Encoding: binary\n" +
		"\n" +
		"Blah - Page Contents\n" +
		"\n" +
		"--=_multipart_boundary_2\n" +
		"\n" +
		"Other blah\n" +
		"\n" +
		"--=_multipart_boundary_2--\n";
		String expected = "Metadata Titles Test Page";
		String actual = tester.getNameFromMetadata(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	public void testGetPagename() {
		String input = "Content-Type: application/x-tikiwiki;\n" +
		"  pagename=ABC\n" +
		"  flags=\"\";\n" +
		"  author=xxxxxxxx;\n" +
		"  version=2;\n" +
		"  lastmodified=1168026094;\n" +
		"  author_id=xxx.xxx.xxx.xxx;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n";
		String expected = "ABC"; 
		String actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Content-Type: application/x-tikiwiki;\n" +
		"  pagename=ABC;\n" +
		"  flags=\"\";\n" +
		"  author=xxxxxxxx;\n" +
		"  version=2;\n" +
		"  lastmodified=1168026094;\n" +
		"  author_id=xxx.xxx.xxx.xxx;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n";
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Content-Type: application/x-tikiwiki;\n" +
		"  pagename=A%20BC;\n" +
		"  flags=\"\";\n" +
		"  author=xxxxxxxx;\n" +
		"  version=2;\n" +
		"  lastmodified=1168026094;\n" +
		"  author_id=xxx.xxx.xxx.xxx;\n" +
		"  summary=\"\";\n" +
		"  hits=22;\n" +
		"  description=\"\";\n" +
		"  charset=iso-8859-1\n";
		expected = "A%20BC";
		actual = tester.getPagename(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "no metadata";
		actual = tester.getPagename(input);
		assertNull(actual);
		
		input = null;
		try {
			actual = tester.getPagename(input);
		} catch (NullPointerException e) {
			fail("threw NPE");
		}
		assertNull(actual);
	}
	
	public void testDecodeEntities() {
		String noentity = "abc";
		String expected = "abc";
		String actual = tester.decodeEntities(noentity);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		String entity = "a%20BC";
		expected = "a BC";
		actual = tester.decodeEntities(entity);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testSettingPagenameWhenNoneIsFound() {
		String text = "blah";
		String name = "A";
		String expected = name;
		Page page = new Page(new File(""));
		page.setName(name);
		page.setOriginalText(text);
		tester.convert(page);
		String actual = page.getName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

}
