package com.atlassian.uwc.hierarchies;

import java.util.Iterator;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class TWikiHierarchyTest extends TestCase {

	TWikiHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new TWikiHierarchy();
		PropertyConfigurator.configure("log4j.properties");
	}

	public void testBuildHierarchy_Basic() {
		Vector<Page> pages = new Vector<Page>();
		pages.add(createTestPage("abc", "%META:TOPICPARENT{name=\"def\"}%\n"));
		pages.add(createTestPage("def", "Testing 123 - Parent"));
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root
		for (Iterator iter = root.getChildIterator(); iter.hasNext();) {
			HierarchyNode child = (HierarchyNode) iter.next();
			assertNotNull(child);
			assertEquals("def", child.getName());
			for (Iterator iterator = child.getChildIterator(); iterator.hasNext();) {
				HierarchyNode gchild = (HierarchyNode) iterator.next();
				assertNotNull(gchild);
				assertEquals("abc", gchild.getName());
			}
		}
	}
	
	public void testBuildHierarchy_Duplicates() {
		Vector<Page> pages = new Vector<Page>();
		pages.add(createTestPage("abc", "%META:TOPICPARENT{name=\"def\"}%\n"));
		pages.add(createTestPage("abc", "Testing 2 - duplicate"));
		pages.add(createTestPage("def", "Testing 123 - Parent"));
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root
		int index = 0;
		for (Iterator iter = root.getChildIterator(); iter.hasNext();) {
			HierarchyNode child = (HierarchyNode) iter.next();
			assertNotNull(child);
			String name = child.getName();
			if (name.equals("def")) {
				for (Iterator iterator = child.getChildIterator(); iterator.hasNext();) {
					HierarchyNode gchild = (HierarchyNode) iterator.next();
					assertEquals("abc", gchild.getName());
				}
			}
			else assertEquals("abc", child.getName());
		}
	}
	
	//XXX This test represents existing behavior, as opposed to
	//desired behavior. I'm not currently sure what the desired behavior _should_ be,
	//but probably an NPE is wrong
	public void testBuildHierarchy_Duplicates2() {
		Vector<Page> pages = new Vector<Page>();
		pages.add(createTestPage("a", "%META:TOPICPARENT{name=\"b\"}%\n"));
		pages.add(createTestPage("b", "no parent"));
		pages.add(createTestPage("A", "no parent"));
		pages.add(createTestPage("A", "%META:TOPICPARENT{name=\"b\"}%"));
		try {
			HierarchyNode root = tester.buildHierarchy(pages);
		} catch (NullPointerException e) {
			e.printStackTrace();
			fail();
		}
	}

	private Page createTestPage(String title, String content) {
		Page page = new Page(null);
		page.setName(title);
		page.setOriginalText(content);
		page.setConvertedText(content);
		page.setUnchangedSource(content);
		return page;
	}

}
