package com.atlassian.uwc.converters.jive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.ui.Comment;
import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.TokenMap;

public class CommentConverterTest extends TestCase {

	CommentConverter tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new CommentConverter();
		PropertyConfigurator.configure("log4j.properties");
	}
	
	public void testCommentConverter() {
		String path = "sampleData/jive/junit_resources/comment-sample/DOC-1234-test-3.txt";
		Page page = new Page(new File(path));
		String input = read(path);
		page.setOriginalText(input);
		page.setConvertedText(input);
		
		assertFalse(page.hasComments());
		tester.convert(page);
		assertTrue(page.hasComments());
		Vector<String> actual = page.getComments();
		assertEquals(1, actual.size());
		assertTrue(actual.get(0).contains("tada"));
	}

	public void testGetCommentsFromDir() {
		String path = "sampleData/jive/junit_resources/comment-sample/DOC-1234-test-3.txt";
		File file = new File(path);
		Vector<String> commentsFromDir = tester.getCommentsFromDir(file, "102_1234");
		assertNotNull(commentsFromDir);
		assertEquals(1, commentsFromDir.size());
		assertEquals("sampleData/jive/junit_resources/comment-sample/COMMENT-1001-102_1234-1.txt", commentsFromDir.get(0));
	}
	public void testGetCommentStrings() {
		Vector<String> input = new Vector<String>();
		input.add("sampleData/jive/junit_resources/comment-sample/COMMENT-1001-102_1234-1.txt");
		Vector<String> commentStrings = tester.getCommentStrings(input);
		assertNotNull(commentStrings);
		assertEquals(1, commentStrings.size());
		assertEquals("{jive-export-meta:id=1001|version=1|type=COMMENT|commentparent=0|referrertype=102|referrerid=1234}\n" + 
				"{user:foo}\n" + 
				"{timestamp:1277935786314}\n" + 
				"<body><p><b>tada</b></p></body>\n", commentStrings.get(0)); 
	}
	public void testSetupComment() {
		Page page = new Page(null);
		String input = "{jive-export-meta:id=1001|version=1|type=COMMENT|commentparent=0|referrertype=102|referrerid=1234}\n" + 
		"{user:foo}\n" + 
		"{timestamp:1277935786314}\n" + 
		"<body><p><b>tada</b></p></body>\n";
		assertFalse(page.hasComments());
		tester.setupComment(page, input);
		assertTrue(page.hasComments());
		
		String actual = page.getComments().get(0);
		String expected = "*tada*\n" + 
				"\n" + 
				"\n" + 
				" ";
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		Vector<Comment> allCommentData = page.getAllCommentData();
		Comment actComment = allCommentData.get(0);
		assertNotNull(actComment);
		assertTrue(actComment.hasCreator());
		assertTrue(actComment.hasTimestamp());
		assertEquals("foo", actComment.creator);
		assertEquals("2010:06:30:18:09:46:314", actComment.timestamp);
		
	}
	
	public void testCommentConverter_Tokenization() {
		Stack<String> keys = new Stack<String>();
		
		String path = "sampleData/jive/junit_resources/comment-sample/DOC-5678-test-4.txt";
		Page page = new Page(new File(path));
		String input = read(path);
		page.setOriginalText(input);
		page.setConvertedText(input);
		ConverterEngine engine = new ConverterEngine();
		List<String> converterStrings = new Vector<String>();
		converterStrings.add("Jive.1234.pre.java-regex-tokenizer=<pre>(.*?)<\\/pre>{replace-with}{code}$1{code}");
		ArrayList<Converter> pageConverters = engine.createConverters(converterStrings, false);
		for (Converter converter : pageConverters) {
			if (converter == null) continue; 
			log.debug("Converting comment with: " + converter.getKey());
			try {
				converter.convert(page);
				page.setOriginalText(page.getConvertedText());
			} catch (Exception e) {
				log.error("Problem converting with " + converter.getKey() + ". Skipping.");
				e.printStackTrace();
				continue;
			}
		}
		assertFalse(TokenMap.getKeys().isEmpty());
		
		String expected = "Testing {code}123{code}";
		assertFalse(page.hasComments());
		tester.convert(page);
		assertTrue(page.hasComments());
		Vector<String> actual = page.getComments();
		assertEquals(1, actual.size());
		assertTrue(actual.get(0).contains(expected));
		
		Stack<String> keys2 = TokenMap.getKeys();
		assertNotNull(keys2);
		assertFalse(keys2.isEmpty());
		assertTrue(keys2.size() == 1);
	}
	
	protected String read(String path) {
		String filestring = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			while ((line = reader.readLine()) != null) {
				filestring += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filestring;
	}

}
