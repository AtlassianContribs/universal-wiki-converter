package com.atlassian.uwc.converters.mediawiki;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.mediawiki.CommentConverter.TYPE;
import com.atlassian.uwc.ui.Page;

public class CommentConverterTest extends TestCase {

	Logger log = Logger.getLogger(this.getClass());
	CommentConverter tester = null;
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new CommentConverter();
		Properties props = loadProps("sampleData/mediawiki/test.discussion.properties");
		tester.setProperties(props);
	}
	public void testGetDiscussionLocation() {
		String expected = "./sampleData/mediawiki/Discussions/";
		String actual = tester.getDiscussionLocation();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetDiscussionContent() {
		String pagename = "Clacks.txt"; //XXX - We got some of our test samples for this implementation from Wikipedia
		String actual = tester.getDiscussionContent(tester.getDiscussionLocation(), pagename);
		String line, expected = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader("./sampleData/mediawiki/Discussions/Clacks_Discussion.txt"));
			while ((line = reader.readLine()) != null) {
				expected += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	public void testGetDelimiters() {
		Vector<Pattern> delimiters = tester.getDelimiters();
		assertNotNull(delimiters);
		assertEquals(3, delimiters.size());
		Pattern delim1 = delimiters.get(0);
		Pattern delim2 = delimiters.get(1);
		Pattern delim3 = delimiters.get(2);
		assertNotNull(delim1);
		assertNotNull(delim2);
		assertNotNull(delim3);
		
		assertEquals(delim3.pattern(), "\\[\\[User.*?UTC\\)[^\n]*");
		assertEquals(delim2.pattern(), "\n[=]");
		assertEquals(delim1.pattern(), "\n[----]");
		
	}
	
	public void testFindAndAddComments() {
		String discussionContent = "testing '''123'''\n" +
				"== header ==\n" +
				"something else\n";
		Vector<Pattern> delimPatterns = new Vector<Pattern>();
		delimPatterns.add(Pattern.compile("\n[=]"));
		Page page = new Page(null);
		HashMap<String, TYPE> delimtypes = tester.getDelimtypes();
		delimtypes.put("\n[=]", TYPE.START);
		tester.findAndAddComments(page, discussionContent, delimPatterns);
		Vector<String> actual = page.getComments();
		assertNotNull(actual);
		assertEquals(2, actual.size());
		String comment1 = actual.get(0);
		String comment2 = actual .get(1);
		assertNotNull(comment1);
		assertNotNull(comment2);
		assertEquals("testing *123*", comment1);
		assertEquals("\nh1.  header \nsomething else\n", comment2);
		
//		real test
		discussionContent = tester.getDiscussionContent(tester.getDiscussionLocation(), "Clacks.txt");
		delimPatterns = tester.getDelimiters();
		Page page2 = new Page(null);
		tester.findAndAddComments(page2, discussionContent, delimPatterns);
		actual = page2.getComments();
		assertNotNull(actual);
		assertEquals(12, actual.size());
	}
	
	public void testGetDiscussionName() {
		String input, expected, actual;
		input = "abc.txt";
		expected = "abc_Discussion.txt";
		actual = tester.getDiscussionName(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetDiscussionLocation_Parent() {
		String input, expected, actual;
		tester.getProperties().setProperty("discussion-location", "../Discussion/");
		input = "/Testing/123/456/Pages";
		expected = "/Testing/123/456/Discussion/";
		actual = tester.getDiscussionLocation(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("discussion-location", "./Discussion/");
		expected = "/Testing/123/456/Pages/Discussion/";
		actual = tester.getDiscussionLocation(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester.getProperties().setProperty("discussion-location", "../../../Discussion/");
		expected = "/Testing/Discussion/";
		actual = tester.getDiscussionLocation(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
	}
	
	private Properties loadProps(String path) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(path));
        } catch (IOException e) {
        	String message = "Make sure that the file '" + path + "' " +
			        			"exists and contains the following " +
			        			"settings: discussion-delim-1, discussion-delim-2, discussion-delim-3, discussion-location.";
			log.error(message);
        	e.printStackTrace();
        	fail(message);
        }
        return props;
	}
	
}
