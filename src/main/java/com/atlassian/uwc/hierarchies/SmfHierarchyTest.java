package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class SmfHierarchyTest extends TestCase {

	SmfHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	private static final String TESTDIR = "sampleData/smf/junit_resources/";
	protected void setUp() throws Exception {
		tester = new SmfHierarchy();
		PropertyConfigurator.configure("log4j.properties");
		tester.setProperties(new Properties());
		tester.clearCollisions();
	}
	
	public void testBuildRelationships_BoardFirst() {
		String input, expected, actual;
		Page page = getSamplePage(TESTDIR +
				"board_brd2.txt", true);
		HierarchyNode root = new HierarchyNode();
		
		HierarchyNode node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		HierarchyNode home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		HierarchyNode cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		HierarchyNode brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("Test Board", brd.getName());
		assertNotNull(brd.getPage());
		
	}
	
	public void testBuildRelationships_Category() {
		String input, expected, actual;
		Page page = getSamplePage(TESTDIR +
				"ancestornull_cat2.txt", true);
		HierarchyNode root = new HierarchyNode();
		
		HierarchyNode node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		HierarchyNode home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		HierarchyNode cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("Testing Null Ancestor String", cat.getName());
		assertTrue(cat.getChildren().isEmpty());
		
	}


	public void testBuildRelationships_Simple() {
		String input, expected, actual;
		Page page = getSamplePage(TESTDIR +
				"SampleSmf-InputHierarchy.txt", true);
		HierarchyNode root = new HierarchyNode();
		
		HierarchyNode node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		HierarchyNode home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		HierarchyNode cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		HierarchyNode brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd2", brd.getName());
		assertNull(brd.getPage());
		HierarchyNode top = getFirstNode(brd);
		assertNotNull(top);
		assertEquals("Syntax Test Page", top.getName());
		assertNotNull(top.getPage());
		
		String catfile = "category_cat1.txt";
		Page page2 = getSamplePage(TESTDIR +
				catfile, true);
		tester.buildRelationships(page2, root);
		assertNotNull(node);
		assertNull(node.getName());
		home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("Test Category", cat.getName());
		assertNotNull(cat.getPage());
		brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd2", brd.getName());
		assertNull(brd.getPage());
		top = getFirstNode(brd);
		assertNotNull(top);
		assertEquals("Syntax Test Page", top.getName());
		assertNotNull(top.getPage());
		
		String brdfile = "board_brd2.txt";
		Page page3 = getSamplePage(TESTDIR +
				brdfile, true);
		tester.buildRelationships(page3, root);
		assertNotNull(node);
		assertNull(node.getName());
		home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("Test Category", cat.getName());
		assertNotNull(cat.getPage());
		brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("Test Board", brd.getName());
		assertNotNull(brd.getPage());
		top = getFirstNode(brd);
		assertNotNull(top);
		assertEquals("Syntax Test Page", top.getName());
		assertNotNull(top.getPage());
		
	}

	public void testCreateNodeId() {
		String id, type, expected, actual;
		id = "10";
		type = "cat";
		expected = "cat10";
		actual = tester.createNodeId(id, type);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testBuildNode() {
		HierarchyNode parent = new HierarchyNode();
		assertTrue(parent.getChildren().isEmpty());
		
		HierarchyNode node = tester.buildNode(parent, "top3");
		assertEquals("top3", node.getName());
		assertFalse(parent.getChildren().isEmpty());
		assertEquals(1, parent.getChildren().size());
		
		HierarchyNode node2 = tester.buildNode(parent, "top3");
		assertEquals(node, node2);
		assertFalse(parent.getChildren().isEmpty());
		assertEquals(1, parent.getChildren().size());
		
		node.setName("somethingelse");
		File file = new File(TESTDIR +
				"something-top3.txt");
		Page page = new Page(file);
		node.setPage(page);
		
		HierarchyNode node3 = tester.buildNode(parent, "top3");
		assertEquals(node, node3);
		assertFalse(parent.getChildren().isEmpty());
		assertEquals(1, parent.getChildren().size());
	}

	public void testBuildRelationships_IllegalChars() {
		String input, expected, actual;
		tester.clearCollisions();
		Page page = getSamplePage(TESTDIR +
				"Badcharactersintitle_top4.txt", true);
		HierarchyNode root = new HierarchyNode();
		
		HierarchyNode node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		HierarchyNode home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		HierarchyNode cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		HierarchyNode brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd1", brd.getName());
		assertNull(brd.getPage());
		HierarchyNode top = getFirstNode(brd);
		assertNotNull(top);
		String exp = "Bad characters in title";
		assertEquals(exp, top.getName());
		assertNotNull(top.getPage());
		assertEquals(exp, top.getPage().getName());
		
		//starts with tilde ~
		page = getSamplePage(TESTDIR +
				"Badcharactersintitle_top9.txt", true);
		
		node = null;
		tester.clearCollisions();
		node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd1", brd.getName());
		assertNull(brd.getPage());
		top = getFirstNode(brd);
		assertNotNull(top);
		exp = "Bad characters in title";
		assertEquals(exp, top.getName());
		assertNotNull(top.getPage());
		assertEquals(exp, top.getPage().getName());
//		starts with dollar $
		page = getSamplePage(TESTDIR +
				"Badcharactersintitle_top10.txt", true);
		
		node = null;
		tester.clearCollisions();
		node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd1", brd.getName());
		assertNull(brd.getPage());
		top = getFirstNode(brd);
		assertNotNull(top);
		exp = "Bad characters in title";
		assertEquals(exp, top.getName());
		assertNotNull(top.getPage());
		assertEquals(exp, top.getPage().getName());
		
//		starts with double dot (..)
		page = getSamplePage(TESTDIR +
				"Badcharactersintitle_top11.txt", true);
		
		node = null;
		tester.clearCollisions();
		node = tester.buildRelationships(page, root);
		
		assertNotNull(node);
		assertNull(node.getName());
		home = getFirstNode(node);
		assertNotNull(home);
		assertEquals(tester.DEFAULT_ROOTPAGENAME, home.getName());
		cat = getFirstNode(home);
		assertNotNull(cat);
		assertEquals("cat1", cat.getName());
		assertNull(cat.getPage());
		brd = getFirstNode(cat);
		assertNotNull(brd);
		assertEquals("brd1", brd.getName());
		assertNull(brd.getPage());
		top = getFirstNode(brd);
		assertNotNull(top);
		assertEquals(exp, top.getName());
		assertNotNull(top.getPage());
		assertEquals(exp, top.getPage().getName());
	}
	
	public void testConvertPagename() {
		String input, expected, actual;
		Page page = new Page(null);
		page.setName("filename.txt");
		Properties meta = new Properties();
		
		input = "Bad characters in title :@/\\|^#;[]{}<>; foobar";
		expected = "Bad characters in title  foobar";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "~tilde";
		expected = "tilde";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "..dots";
		expected = "dots";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "$dollar";
		expected = "dollar";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
	
		input = "";
		expected = "No Title";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
	
		tester.clearCollisions();//not testing namespace collisions yet
		input = "..";
		expected = "No Title";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		//entities
		input = "&lt;html entity&gt;";
		expected = "html entity";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());

		input = "&quot; html quotes";
		expected = "\" html quotes";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "&amp; html quotes";
		expected = "& html quotes";
		meta.put("title", input);
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());

		//namespace collisions
		tester.clearCollisions();
		input = "namespace collision";
		expected = "namespace collision";
		meta.put("title", input);
		meta.put("username", "admin");
		meta.put("time", "1245696200");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());

		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 244PM";
		meta.put("title", input);
		meta.put("username", "admin");
		meta.put("time", "1245696264");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());

		//dumb chars in username (SMF allows semi-colons in usernames! o-O)
		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 245PM";
		meta.put("title", input);
		meta.put("username", "admin;");
		meta.put("time", "1245696300");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		//what if the name and timestamp isn't enough? (unlikely, but still)
		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 245PM - No.2";
		meta.put("title", input);
		meta.put("username", "admin:");
		meta.put("time", "1245696300");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 245PM - No.3";
		meta.put("title", input);
		meta.put("username", "admin:");
		meta.put("time", "1245696300");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		//custom date format
		input = "namespace collision";
		expected = "namespace collision - admin - 062209 1445";
		meta.put("title", input);
		meta.put("username", "admin:");
		meta.put("time", "1245696300");
		tester.getProperties().setProperty("title-date-format", "MMddyy HHmm");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		//bad custom date format
		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 1445";
		meta.put("title", input);
		meta.put("username", "admin:");
		meta.put("time", "1245696300");
		//format has disallowed confluence title character (:)
		tester.getProperties().setProperty("title-date-format", "yyyy-MM-dd HH:mm");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());

		input = "namespace collision";
		expected = "namespace collision - admin - 2009-06-22 245PM - No.4";
		meta.put("title", input);
		meta.put("username", "admin:");
		meta.put("time", "1245696300");
		//format has disallowed confluence title character (:)
		tester.getProperties().setProperty("title-date-format", "abc%%defg");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		//what if the poster or time is null
		input = "namespace collision";
		expected = "namespace collision - null - 2009-06-22 245PM";
		meta.put("title", input);
		meta.put("username", "null");
		meta.put("time", "1245696300");
		tester.getProperties().remove("title-date-format");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		input = "namespace collision";
		expected = "namespace collision - admin";
		meta.put("title", input);
		meta.put("username", "admin");
		meta.put("time", "null");
		tester.getProperties().remove("title-date-format");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
	}
	
	public void testCollisions_CategoriesBoards() {
		String input, expected, actual;
		Page page = new Page(null);
		page.setName("filename.txt");
		Properties meta = new Properties();
		
		input = "Testing";
		expected = "Testing";
		meta.put("title", input);
		meta.put("type", "brd");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		expected = "Testing - No.2";
		meta.put("title", input);
		meta.put("type", "cat");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		expected = "Testing - No.3";
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		tester.clearCollisions();
		input = "Testing";
		expected = "Testing";
		meta.put("title", input);
		meta.put("type", "cat");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		expected = "Testing - No.2";
		meta.put("title", input);
		meta.put("type", "brd");
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
		
		expected = "Testing - No.3";
		actual = tester.convertPagename(page, meta);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected, page.getName());
	}

	public void testFormatTime() {
		String input, expected, actual;
		input = "1234567890";
		expected = "2009-02-13 631PM";
		actual = tester.formatTime(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testCompareChildren() {
		Vector<Page> pages = new Vector<Page>();
		String parent = "sampleData/smf/junit_resources/sortorder/";
		Page page = new Page(new File(parent + "GeneralDiscussion_brd1.txt"));
		Page page1 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re11.txt"));
		Page page2 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re7.txt"));
		Page page3 = new Page(new File(parent + "ReWelcometoSMF_re10.txt"));
		Page page4 = new Page(new File(parent + "ReWelcometoSMF_re2.txt"));
		Page page5 = new Page(new File(parent + "ReWelcometoSMF_re20.txt"));
		Page page6 = new Page(new File(parent + "ReWelcometoSMF_re21.txt"));
		Page page7 = new Page(new File(parent + "WelcometoSMF_top1.txt"));
		Page page8 = new Page(new File(parent + "GeneralCategory_cat1.txt"));
		pages.add(page);
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page4);
		pages.add(page5);
		pages.add(page6);
		pages.add(page7);
		pages.add(page8);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		assertEquals(1, root.getChildren().size());
		
		HierarchyNode home = root.getChildIterator().next();
		assertNotNull(home);
		assertEquals(1, home.getChildren().size());
		assertEquals("Home", home.getName());
		
		HierarchyNode cat = home.getChildIterator().next();
		assertNotNull(cat);
		assertEquals(1, cat.getChildren().size());
		assertEquals("General Category", cat.getName());
		
		HierarchyNode board = cat.getChildIterator().next();
		assertNotNull(board);
		assertEquals(1, board.getChildren().size());
		assertEquals("General Discussion", board.getName());
		
		HierarchyNode topic = board.getChildIterator().next();
		assertNotNull(topic);
		assertEquals(6, topic.getChildren().size());
		assertEquals("Welcome to SMF!", topic.getName());

		int i = 0;
		for (Iterator iter = topic.getChildIterator(); iter.hasNext();) {
			HierarchyNode child = (HierarchyNode) iter.next();
			assertNotNull(child);
			switch(i) {
			case 0:
				assertEquals("ReWelcometoSMF_re2.txt", child.getPage().getFile().getName());
				break;
			case 1:
				assertEquals("ReWelcometoSMFChangingreplysubject_re7.txt", child.getPage().getFile().getName());
				break;
			case 2:
				assertEquals("ReWelcometoSMF_re10.txt", child.getPage().getFile().getName());
				break;
			case 3:
				assertEquals("ReWelcometoSMFChangingreplysubject_re11.txt", child.getPage().getFile().getName());
				break;
			case 4:
				assertEquals("ReWelcometoSMF_re20.txt", child.getPage().getFile().getName());
				break;
			case 5: 
				assertEquals("ReWelcometoSMF_re21.txt", child.getPage().getFile().getName());
				break;
			}
			i++;
		}
		
	}

	public void testReplyComments() {
		Properties props = tester.getProperties();
		props.setProperty("reply-comments", "true");
		
		//set up a hierarchy to look at
		Vector<Page> pages = new Vector<Page>();
		String parent = "sampleData/smf/junit_resources/sortorder/";
		Page page = new Page(new File(parent + "GeneralDiscussion_brd1.txt"));
		Page page1 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re11.txt"));
		Page page2 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re7.txt"));
		Page page3 = new Page(new File(parent + "ReWelcometoSMF_re10.txt"));
		Page page4 = new Page(new File(parent + "ReWelcometoSMF_re2.txt"));
		Page page5 = new Page(new File(parent + "ReWelcometoSMF_re20.txt"));
		Page page6 = new Page(new File(parent + "ReWelcometoSMF_re21.txt"));
		Page page7 = new Page(new File(parent + "WelcometoSMF_top1.txt"));
		Page page8 = new Page(new File(parent + "GeneralCategory_cat1.txt"));
		pages.add(page);
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page4);
		pages.add(page5);
		pages.add(page6);
		pages.add(page7);
		pages.add(page8);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		
		//does the topic page have comments with include macros pointing at the right pages in the right order?
		HierarchyNode home = root.getChildIterator().next();
		HierarchyNode cat = home.getChildIterator().next();
		HierarchyNode board = cat.getChildIterator().next();
		HierarchyNode topic = board.getChildIterator().next();
		assertNotNull(topic);
		assertEquals(6, topic.getChildren().size());
		assertEquals("Welcome to SMF!", topic.getName());

		Vector<String> comments = topic.getPage().getComments();
		assertNotNull(comments);
		assertEquals(6, comments.size());
		
		int i = 0;
		for (Iterator iter = topic.getChildIterator(); iter.hasNext();) {
			HierarchyNode child = (HierarchyNode) iter.next();
			String title = child.getPage().getName();
			String actualComment = comments.get(i);
			String expectedComment = "h1. [" + title + "]\n{include:" + title +"}";
			assertNotNull(child);
			assertEquals(expectedComment, actualComment);
			i++;
		}
	}
	
	public void testReplyComments_NoReplies() {
		Properties props = tester.getProperties();
		props.setProperty("reply-comments", "true");
		
		//set up a hierarchy to look at
		Vector<Page> pages = new Vector<Page>();
		String parent = "sampleData/smf/junit_resources/sortorder/";
		Page page = new Page(new File(parent + "GeneralDiscussion_brd1.txt"));
		Page page7 = new Page(new File(parent + "WelcometoSMF_top1.txt"));
		Page page8 = new Page(new File(parent + "GeneralCategory_cat1.txt"));
		pages.add(page);
		pages.add(page7);
		pages.add(page8);

		try { //shouldn't have NPE if there aren't any replies
			HierarchyNode root = tester.buildHierarchy(pages);
		} catch (NullPointerException e) { fail();}
	}

	public void testGetTopicNodes() {
		Vector<Page> pages = new Vector<Page>();
		String parent = "sampleData/smf/junit_resources/sortorder/";
		Page page = new Page(new File(parent + "GeneralDiscussion_brd1.txt"));
		Page page1 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re11.txt"));
		Page page2 = new Page(new File(parent + "ReWelcometoSMFChangingreplysubject_re7.txt"));
		Page page3 = new Page(new File(parent + "ReWelcometoSMF_re10.txt"));
		Page page4 = new Page(new File(parent + "ReWelcometoSMF_re2.txt"));
		Page page5 = new Page(new File(parent + "ReWelcometoSMF_re20.txt"));
		Page page6 = new Page(new File(parent + "ReWelcometoSMF_re21.txt"));
		Page page7 = new Page(new File(parent + "WelcometoSMF_top1.txt"));
		Page page8 = new Page(new File(parent + "GeneralCategory_cat1.txt"));
		pages.add(page);
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page4);
		pages.add(page5);
		pages.add(page6);
		pages.add(page7);
		pages.add(page8);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		Vector<HierarchyNode> topicNodes = tester.getTopicNodes(root);
		assertNotNull(topicNodes);
		assertEquals(1, topicNodes.size());
		assertEquals("Welcome to SMF!", topicNodes.get(0).getName());

	}
	
	public void testGetTopicNodes_ChildBoards() {
		Vector<Page> pages = new Vector<Page>();
		String parent = "sampleData/smf/junit_resources/sortorder/";
		Page page = new Page(new File(parent + "GeneralDiscussion_brd1.txt"));
		Page page1 = new Page(new File(parent + "ReChildBoard_re34.txt"));
		Page page2 = new Page(new File(parent + "ChildTopic_top33.txt"));
		Page page3 = new Page(new File(parent + "ChildBoard_brd3.txt"));
		Page page8 = new Page(new File(parent + "GeneralCategory_cat1.txt"));
		pages.add(page);
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		pages.add(page8);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		Vector<HierarchyNode> topicNodes = tester.getTopicNodes(root);
		assertNotNull(topicNodes);
		assertEquals(1, topicNodes.size());
		assertEquals("Child Board topic", topicNodes.get(0).getName());

	}

	public void testCreateComment() {
		String input, expected, actual;
		input = "Page Name";
		expected = "h1. [Page Name]\n{include:Page Name}";
		actual = tester.createComment(input);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	
	/* Convenience methods */
	private HierarchyNode getFirstNode(HierarchyNode node) {
		Vector<HierarchyNode> v = new Vector<HierarchyNode>();
		v.addAll(node.getChildren());
		HierarchyNode first = v.get(0);
		return first;
	}

	private Page getSamplePage(String path, boolean setNameFromFile) {
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file, path);
		if (setNameFromFile) page.setName(file.getName());
		assertNotNull(page.getFile().getName());
		return page;
	}
	
	

}
