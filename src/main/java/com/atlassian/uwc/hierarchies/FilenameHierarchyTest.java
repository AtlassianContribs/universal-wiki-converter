package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

//NOTE: a valid set of pages for testing will need a valid file associated with that page node
public class FilenameHierarchyTest extends TestCase {

	FilenameHierarchy tester = null;
	private final String SAMPLEDIR_SIMPLE = "sampleData/hierarchy/filename/simple";
	private final String SAMPLEDIR_ENCODED = "sampleData/hierarchy/filename/urlencoded";
	Logger log = Logger.getLogger(this.getClass());
	Vector<Page> pages = null;
	Properties props = null;
	
	protected void setUp() throws Exception {
		tester = new FilenameHierarchy();
		PropertyConfigurator.configure("log4j.properties");
		pages = new Vector<Page>();
		props = new Properties();
		props.setProperty(FilenameHierarchy.PROPKEY_DELIM, "%2F");
		tester.setProperties(props);
	}
	
	public void testBuildHierarchy_OnePage_OneNode() {
		addOnePageToList(SAMPLEDIR_SIMPLE + File.separator + "Root");
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Collection<HierarchyNode> nodes = actual.getChildren();
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		for (HierarchyNode root : nodes) {
			assertEquals("Root", root.getName());
			
			Page rootpage = root.getPage();
			assertNotNull(rootpage);
			assertNotNull(rootpage.getName());
			assertEquals("Root", rootpage.getName());
			
			assertTrue(root.getChildren().isEmpty());
		}
		
	}


	public void testBuildHierarchy_OnePage_TwoNodes() {
		addOnePageToList(SAMPLEDIR_SIMPLE + File.separator + "Root%2FLeaf");
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Collection<HierarchyNode> nodes = actual.getChildren();
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		for (HierarchyNode root : nodes) {
			assertEquals("Root", root.getName());
			assertNull(root.getPage()); //cause we only had the leaf page
			
			Vector<HierarchyNode> children = asVector(root.getChildren());
			assertEquals(1, children.size());
			HierarchyNode child = children.get(0);
			assertEquals("Leaf", child.getName());
			
			Page childpage = child.getPage();
			assertNotNull(childpage);
			assertEquals("Leaf", childpage.getName());
			
			assertTrue(child.getChildren().isEmpty());
		}
	}
	

	public void testBuildHierarchy_OnePage_ThreeNodes() {
		addOnePageToList(SAMPLEDIR_SIMPLE + File.separator + "Root%2FBranch%2FChild");
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Collection<HierarchyNode> nodes = actual.getChildren();
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		for (HierarchyNode root : nodes) {
			assertEquals("Root", root.getName());
			assertNull(root.getPage());
			
			Vector<HierarchyNode> children = asVector(root.getChildren());
			assertEquals(1, children.size());
			HierarchyNode child = children.get(0);
			assertEquals("Branch", child.getName());
			assertNull(child.getPage());
			
			Vector<HierarchyNode> gchildren = asVector(child.getChildren());
			assertEquals(1, gchildren.size());
			HierarchyNode leaf = gchildren.get(0);
			assertEquals("Child", leaf.getName());
			
			Page leafpage = leaf.getPage();
			assertNotNull(leafpage);
			assertEquals("Child", leafpage.getName());
			
			assertTrue(leaf.getChildren().isEmpty());
		}
	}
	
	public void testBuildHierarchy_TwoPages() {
		addOnePageToList(SAMPLEDIR_SIMPLE + File.separator + "Root%2FLeaf");
		addOnePageToList(SAMPLEDIR_SIMPLE + File.separator + "Root");
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Collection<HierarchyNode> nodes = actual.getChildren();
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		for (HierarchyNode root : nodes) {
			assertEquals("Root", root.getName());
			Page rootpage = root.getPage();
			assertNotNull(rootpage);
			assertEquals("Root", rootpage.getName());
			
			Vector<HierarchyNode> children = asVector(root.getChildren());
			assertEquals(1, children.size());
			HierarchyNode child = children.get(0);
			assertEquals("Leaf", child.getName());
			
			Page childpage = child.getPage();
			assertNotNull(childpage);
			assertEquals("Leaf", childpage.getName());
			
			assertTrue(child.getChildren().isEmpty());
		}
	}

	public void testBuildHierarchy_Simple() {
		File dir = new File(SAMPLEDIR_SIMPLE);
		File[] listFiles = dir.listFiles(new NoSvnFilter());
		assertEquals(4, listFiles.length);
		for (File file : listFiles) {
			pages.add(new Page(file));
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Vector<HierarchyNode> nodes = asVector(actual.getChildren());
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		HierarchyNode root = nodes.get(0);
		assertNotNull(root.getPage());
		assertEquals("Root", root.getName());
		assertEquals("Root", root.getPage().getName());
		
		Vector<HierarchyNode> nodes2 = asVector(root.getChildren());
		assertNotNull(nodes2);
		assertEquals(2, nodes2.size());
		int branchindex = (nodes2.get(0).getName().startsWith("B"))?0:1; //we do this 'cause Sets aren't predictable
		int leafindex = branchindex == 0?1:0;
		HierarchyNode branch = nodes2.get(branchindex);
		HierarchyNode leaf = nodes2.get(leafindex);
		assertNotNull(leaf.getPage());
		assertEquals("Leaf", leaf.getName());
		assertEquals("Leaf", leaf.getPage().getName());
		assertTrue(leaf.getChildren().isEmpty());
		assertNotNull(branch.getPage());
		assertEquals("Branch", branch.getName());
		assertEquals("Branch", branch.getPage().getName());
		
		Vector<HierarchyNode> nodes3 = asVector(branch.getChildren());
		assertNotNull(nodes3);
		assertEquals(1, nodes3.size());
		HierarchyNode child = nodes3.get(0);
		assertNotNull(child.getPage());
		assertEquals("Child", child.getName());
		assertEquals("Child", child.getPage().getName());
		assertTrue(child.getChildren().isEmpty());
	}

	public void testBuildHierarchy_UrlEncoded() {
		File dir = new File(SAMPLEDIR_ENCODED);
		File[] listFiles = dir.listFiles(new NoSvnFilter());
		assertEquals(5, listFiles.length);
		for (File file : listFiles) {
			pages.add(new Page(file));
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual);
		
		Vector<HierarchyNode> nodes = asVector(actual.getChildren());
		assertNotNull(nodes);
		assertEquals(1, nodes.size());
		HierarchyNode root = nodes.get(0);
		assertNotNull(root.getPage());
		assertEquals("Parent", root.getName());
		assertEquals("Parent", root.getPage().getName());
		
		Vector<HierarchyNode> nodes2 = asVector(root.getChildren());
		assertNotNull(nodes2);
		assertEquals(4, nodes2.size());

		for (HierarchyNode node: nodes2) {
			assertNotNull(node.getPage());
			assertTrue(node.getChildren().isEmpty());
			
			String exp = null;
			if (node.getName().endsWith("Foo")) {
				exp = "Child Foo";
			} 
			else if (node.getName().endsWith("Bar")) {
				exp = "Child,Bar";
			}
			else if (node.getName().endsWith("Baz")) {
				exp = "Child'Baz";
			}
			else if (node.getName().endsWith("Xyz")) {
				exp = "Child&Xyz";
			}
			else fail("Wrong Node name: " + exp);
			assertEquals(exp, node.getName());
			assertEquals(exp, node.getPage().getName());
		}
		
	}

	// helper methods

	private void addOnePageToList(String path) {
		File file = new File(path);
		assertTrue(file.exists());
		Page page = new Page(file);
		pages.add(page);
	}
	
	private Vector<HierarchyNode> asVector(Collection<HierarchyNode> children) {
		Vector<HierarchyNode> nodes = new Vector<HierarchyNode>();
		nodes.addAll(children);
		return nodes;
	}
	
}
