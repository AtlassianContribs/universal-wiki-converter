package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class Many2OneHierarchyTest extends TestCase {

	private static final String DATADIR = "sampleData/socialtext/junit_resources/many2onetest/";
	Many2OneHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	private Page page;
	protected void setUp() throws Exception {
		tester = new Many2OneHierarchy();
		PropertyConfigurator.configure("log4j.properties");
		File file = new File(DATADIR + "many2onepage/20100414012345.txt");
		page = new Page(file);
        page.setPath(getPath(page));
        page.setName("SampleSocialtext-InputMany2OneLinks - many2onetest"); //this will have been set in the pagename converter
	}

	private String getPath(Page input) {
		String pagePath = input.getFile().getPath();
        String path = pagePath.substring(0, pagePath.lastIndexOf(File.separator));
		return path;
	}

	public void testBuildHierarchy() {
		Collection<Page> pages = new Vector<Page>();
		pages.add(page);
		HierarchyNode node = tester.buildHierarchy(pages);
		assertNotNull(node);
		assertNull(node.getName());
		assertEquals(1, node.getChildren().size());
		
		for (Iterator<HierarchyNode> iter = node.getChildIterator(); iter.hasNext();) {
			HierarchyNode parent = (HierarchyNode) iter.next();
			assertNotNull(parent);
			assertEquals("many2onetest", parent.getName());
			assertEquals(1, parent.getChildren().size());
			for (Iterator iterator = parent.getChildIterator(); iterator.hasNext();) {
				HierarchyNode child = (HierarchyNode) iterator.next();
				assertNotNull(child);
				assertEquals("SampleSocialtext-InputMany2OneLinks - many2onetest", child.getName());
				assertEquals(page, child.getPage());
			}
		}
	}
	
	public void testExemptionProps() {
		Properties props = tester.getProperties();
		props.setProperty("many2one-exemption", "(^F.*)"); //pages starting with the letter F
		props.setProperty("hierarchy-exemption-parent", "TestParent");
		File exFile = new File(DATADIR + "exemptiontest/20100414123456.txt");
		Page exemptionPage = new Page(exFile);
		exemptionPage.setPath(getPath(exemptionPage));
		exemptionPage.setName("Foo"); //would have been set by a converter
        
        Vector<Page> pages = new Vector<Page>();
        pages.add(page);
        pages.add(exemptionPage);
        
        HierarchyNode node = tester.buildHierarchy(pages);
		assertNotNull(node);
		assertNull(node.getName());
		assertEquals(2, node.getChildren().size());
		
		for (Iterator<HierarchyNode> iter = node.getChildIterator(); iter.hasNext();) {
			HierarchyNode parent = (HierarchyNode) iter.next();
			assertNotNull(parent);
			String actParentName = parent.getName();
			assertTrue(actParentName.equals("many2onetest") || actParentName.equals("TestParent"));
			assertEquals(1, parent.getChildren().size());
			if (actParentName.equals("TestParent")) {
				for (Iterator iterator = parent.getChildIterator(); iterator.hasNext();) {
					HierarchyNode child = (HierarchyNode) iterator.next();
					assertNotNull(child);
					assertEquals("Foo", child.getName());
					assertEquals(exemptionPage, child.getPage());
				}
			}
		}
	}
}
