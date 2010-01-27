package com.atlassian.uwc.hierarchies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class ContentHierarchyTest extends TestCase {

	ContentHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new ContentHierarchy();
	}

	public void testBuildHierarchy() {
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(file.getName().replaceFirst("\\.txt$", ""));
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(2, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Trees") ||
				(nodes2.get(0)).getName().equals("Flowers"));
		assertTrue((nodes2.get(1)).getName().equals("Trees") ||
				(nodes2.get(1)).getName().equals("Flowers"));

		HierarchyNode treenode, flowernode;
		if (nodes2.get(0).getName().equals("Trees")) {
			treenode = nodes2.get(0); 
			flowernode = nodes2.get(1);
		}
		else {
			flowernode = nodes2.get(0);
			treenode = nodes2.get(1);
		}
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(2, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		assertTrue((trees.get(0)).getName().equals("Chestnut") ||
				(trees.get(0)).getName().equals("Pine"));
		assertTrue((trees.get(1)).getName().equals("Chestnut") ||
				(trees.get(1)).getName().equals("Pine"));
		
		Set<HierarchyNode> flowerchildren = flowernode.getChildren();
		assertEquals(3, flowerchildren.size());
		Vector<HierarchyNode> flowers = new Vector<HierarchyNode>();
		flowers.addAll(flowerchildren);
		assertTrue((flowers.get(0)).getName().equals("Rose") ||
				(flowers.get(0)).getName().equals("Daisy") ||
				(flowers.get(0)).getName().equals("Orchid"));
		assertTrue((flowers.get(1)).getName().equals("Rose") ||
				(flowers.get(1)).getName().equals("Daisy") ||
				(flowers.get(1)).getName().equals("Orchid"));
		assertTrue((flowers.get(2)).getName().equals("Rose") ||
				(flowers.get(2)).getName().equals("Daisy") ||
				(flowers.get(2)).getName().equals("Orchid"));
		
	}
	
	public void testBuildHierarchy_SetProps() {
		Properties props = new Properties();
		props.setProperty(ContentHierarchy.PROP_ROOT, "Home");
		props.setProperty(ContentHierarchy.PROP_PATTERN, "ancestors:([^\\n]*)");
		props.setProperty(ContentHierarchy.PROP_DELIM, "&");
		props.setProperty(ContentHierarchy.PROP_CURRENT, "false");
		tester.setProperties(props);

		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-wprops/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(file.getName().replaceFirst("\\.txt$", ""));
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> home = actual.getChildren();
		assertEquals(1, home.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(home);
		assertTrue((nodes0.get(0)).getName().equals("Home"));
		
		Set<HierarchyNode> children1 = nodes0.get(0).getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(2, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Trees") ||
				(nodes2.get(0)).getName().equals("Flowers"));
		assertTrue((nodes2.get(1)).getName().equals("Trees") ||
				(nodes2.get(1)).getName().equals("Flowers"));

		HierarchyNode treenode, flowernode;
		if (nodes2.get(0).getName().equals("Trees")) {
			treenode = nodes2.get(0); 
			flowernode = nodes2.get(1);
		}
		else {
			flowernode = nodes2.get(0);
			treenode = nodes2.get(1);
		}
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(2, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		assertTrue((trees.get(0)).getName().equals("Chestnut") ||
				(trees.get(0)).getName().equals("Pine"));
		assertTrue((trees.get(1)).getName().equals("Chestnut") ||
				(trees.get(1)).getName().equals("Pine"));
		
		Set<HierarchyNode> flowerchildren = flowernode.getChildren();
		assertEquals(3, flowerchildren.size());
		Vector<HierarchyNode> flowers = new Vector<HierarchyNode>();
		flowers.addAll(flowerchildren);
		assertTrue((flowers.get(0)).getName().equals("Rose") ||
				(flowers.get(0)).getName().equals("Daisy") ||
				(flowers.get(0)).getName().equals("Orchid"));
		assertTrue((flowers.get(1)).getName().equals("Rose") ||
				(flowers.get(1)).getName().equals("Daisy") ||
				(flowers.get(1)).getName().equals("Orchid"));
		assertTrue((flowers.get(2)).getName().equals("Rose") ||
				(flowers.get(2)).getName().equals("Daisy") ||
				(flowers.get(2)).getName().equals("Orchid"));
		

		
	}
	
	public void testBuildHierarchy_NamespaceCollisions() {
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-collisions/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(file.getName());
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(3, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Plants_Trees") ||
				(nodes2.get(0)).getName().equals("Plants_Flowers") ||
				(nodes2.get(0)).getName().equals("Plants_Nuts"));
		assertTrue((nodes2.get(1)).getName().equals("Plants_Trees") ||
				(nodes2.get(1)).getName().equals("Plants_Flowers") ||
				(nodes2.get(1)).getName().equals("Plants_Nuts"));

		HierarchyNode treenode = null, flowernode = null, nutnode = null;
		for (HierarchyNode node : nodes2) {
			if (node.getName().equals("Plants_Trees")) treenode = node;
			if (node.getName().equals("Plants_Flowers")) flowernode = node;
			if (node.getName().equals("Plants_Nuts")) nutnode = node;
		}
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(2, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		assertTrue((trees.get(0)).getName().equals("Plants_Trees_Chestnut") ||
				(trees.get(0)).getName().equals("Plants_Trees_Pine"));
		assertTrue((trees.get(1)).getName().equals("Plants_Trees_Chestnut") ||
				(trees.get(1)).getName().equals("Plants_Trees_Pine"));
		
		Set<HierarchyNode> flowerchildren = flowernode.getChildren();
		assertEquals(3, flowerchildren.size());
		Vector<HierarchyNode> flowers = new Vector<HierarchyNode>();
		flowers.addAll(flowerchildren);
		assertTrue((flowers.get(0)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(0)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(0)).getName().equals("Plants_Flowers_Orchid"));
		assertTrue((flowers.get(1)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(1)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(1)).getName().equals("Plants_Flowers_Orchid"));
		assertTrue((flowers.get(2)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(2)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(2)).getName().equals("Plants_Flowers_Orchid"));
		
		Set<HierarchyNode> nutchildren = nutnode.getChildren();
		assertEquals(2, nutchildren.size());
		Vector<HierarchyNode> nuts = new Vector<HierarchyNode>();
		nuts.addAll(nutchildren);
		assertTrue((nuts.get(0)).getName().equals("Plants_Nuts_Chestnut") ||
				(nuts.get(0)).getName().equals("Plants_Nuts_Peanut"));
		assertTrue((nuts.get(1)).getName().equals("Plants_Nuts_Chestnut") ||
				(nuts.get(1)).getName().equals("Plants_Nuts_Peanut"));
		
	}
	
	public void testBuildHierarchy_NamespaceCollisions_HasCurrentFalse() {
		Properties props = new Properties();
		props.setProperty(ContentHierarchy.PROP_CURRENT, "false");
		tester.setProperties(props);

		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-collisions-hascurrent/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(file.getName());
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(3, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Plants_Trees") ||
				(nodes2.get(0)).getName().equals("Plants_Flowers") ||
				(nodes2.get(0)).getName().equals("Plants_Nuts"));
		assertTrue((nodes2.get(1)).getName().equals("Plants_Trees") ||
				(nodes2.get(1)).getName().equals("Plants_Flowers") ||
				(nodes2.get(1)).getName().equals("Plants_Nuts"));

		HierarchyNode treenode = null, flowernode = null, nutnode = null;
		for (HierarchyNode node : nodes2) {
			if (node.getName().equals("Plants_Trees")) treenode = node;
			if (node.getName().equals("Plants_Flowers")) flowernode = node;
			if (node.getName().equals("Plants_Nuts")) nutnode = node;
		}
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(2, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		assertTrue((trees.get(0)).getName().equals("Plants_Trees_Chestnut") ||
				(trees.get(0)).getName().equals("Plants_Trees_Pine"));
		assertTrue((trees.get(1)).getName().equals("Plants_Trees_Chestnut") ||
				(trees.get(1)).getName().equals("Plants_Trees_Pine"));
		
		Set<HierarchyNode> flowerchildren = flowernode.getChildren();
		assertEquals(3, flowerchildren.size());
		Vector<HierarchyNode> flowers = new Vector<HierarchyNode>();
		flowers.addAll(flowerchildren);
		assertTrue((flowers.get(0)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(0)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(0)).getName().equals("Plants_Flowers_Orchid"));
		assertTrue((flowers.get(1)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(1)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(1)).getName().equals("Plants_Flowers_Orchid"));
		assertTrue((flowers.get(2)).getName().equals("Plants_Flowers_Rose") ||
				(flowers.get(2)).getName().equals("Plants_Flowers_Daisy") ||
				(flowers.get(2)).getName().equals("Plants_Flowers_Orchid"));
		
		Set<HierarchyNode> nutchildren = nutnode.getChildren();
		assertEquals(2, nutchildren.size());
		Vector<HierarchyNode> nuts = new Vector<HierarchyNode>();
		nuts.addAll(nutchildren);
		assertTrue((nuts.get(0)).getName().equals("Plants_Nuts_Chestnut") ||
				(nuts.get(0)).getName().equals("Plants_Nuts_Peanut"));
		assertTrue((nuts.get(1)).getName().equals("Plants_Nuts_Chestnut") ||
				(nuts.get(1)).getName().equals("Plants_Nuts_Peanut"));
		
	}
	
	public void testHierarchyWithNormalNames() {
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-normal/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(guessName(file.getName()));
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(2, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Plants Flowers") ||
				(nodes2.get(0)).getName().equals("Plants Nuts"));
		assertTrue((nodes2.get(1)).getName().equals("Plants Flowers") ||
				(nodes2.get(1)).getName().equals("Plants Nuts"));

		HierarchyNode flowernode = null, nutnode = null;
		for (HierarchyNode node : nodes2) {
			if (node.getName().equals("Plants Flowers")) flowernode = node;
			if (node.getName().equals("Plants Nuts")) nutnode = node;
		}
		
		Set<HierarchyNode> flowerchildren = flowernode.getChildren();
		assertEquals(2, flowerchildren.size());
		Vector<HierarchyNode> flowers = new Vector<HierarchyNode>();
		flowers.addAll(flowerchildren);
		assertTrue((flowers.get(0)).getName().equals("Plants Flowers Rose") ||
				(flowers.get(0)).getName().equals("Plants Flowers Orchid"));
		assertTrue((flowers.get(1)).getName().equals("Plants Flowers Rose") ||
				(flowers.get(1)).getName().equals("Plants Flowers Orchid"));
		
		Set<HierarchyNode> nutchildren = nutnode.getChildren();
		assertEquals(1, nutchildren.size());
		Vector<HierarchyNode> nuts = new Vector<HierarchyNode>();
		nuts.addAll(nutchildren);
		assertTrue((nuts.get(0)).getName().equals("Plants Nuts Chestnut"));
	}
	
	private String guessName(String input) {
		input = input.replaceAll("_", " ");
		input = input.replaceFirst(".txt$", "");
		return input;
	}
	
	public void testBuildHierarchy_History() {
		Properties props = new Properties();
		props.setProperty("switch.page-history-preservation", "true");
		props.setProperty("suffix.page-history-preservation", "-#.txt");
		tester.setProperties(props);
		
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-pagehistory/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(guessName(file.getName()).replaceFirst("-\\d$", ""));
			page.setVersion(Integer.parseInt(file.getName().replaceAll("\\D", "")));
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(1, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Trees"));

		HierarchyNode treenode = nodes2.get(0); 
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(5, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		HierarchyNode node1 = trees.get(0);
		HierarchyNode node2 = trees.get(1);
		HierarchyNode node3 = trees.get(2);
		HierarchyNode node4 = trees.get(3);
		HierarchyNode node5 = trees.get(4);
		
		assertNotNull(node1);
		assertEquals("Chestnut", node1.getName());
		assertEquals(1, node1.getPage().getVersion());
		assertNotNull(node2);
		assertEquals("Chestnut", node2.getName());
		assertEquals(2, node2.getPage().getVersion());
		assertNotNull(node3);
		assertEquals("Chestnut", node3.getName());
		assertEquals(3, node3.getPage().getVersion());
		assertNotNull(node4);
		assertEquals("Pine", node4.getName());
		assertEquals(1, node4.getPage().getVersion());
		assertNotNull(node5);
		assertEquals("Pine", node5.getName());
		assertEquals(2, node5.getPage().getVersion());
		
	}
	
	public void testBuildHierarchy_History_ToHome() {
		Properties props = new Properties();
		props.setProperty("switch.page-history-preservation", "true");
		props.setProperty("suffix.page-history-preservation", "-#.txt");
		props.setProperty(ContentHierarchy.PROP_ROOT, "Home");
		tester.setProperties(props);
		
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/hierarchy/content-pagehistory/");
		assertTrue(sampledir.exists() && sampledir.isDirectory());
		
		for (File file : sampledir.listFiles(new NoSvnFilter())) {
			if (file.isDirectory()) continue;
			Page page = new Page(file);
			page.setOriginalText(readFile(file));
			page.setConvertedText(page.getOriginalText());
			page.setUnchangedSource(page.getOriginalText());
			page.setName(guessName(file.getName()).replaceFirst("-\\d$", ""));
			page.setVersion(Integer.parseInt(file.getName().replaceAll("\\D", "")));
			pages.add(page);
		}
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> home = actual.getChildren();
		assertEquals(1, home.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(home);
		assertTrue((nodes0.get(0)).getName().equals("Home"));
		
		Set<HierarchyNode> children1 = nodes0.get(0).getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Plants"));

		Set<HierarchyNode> children2 = (nodes1.get(0)).getChildren();
		assertEquals(1, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		assertTrue((nodes2.get(0)).getName().equals("Trees"));

		HierarchyNode treenode = nodes2.get(0); 
		
		Set<HierarchyNode> treechildren = treenode.getChildren();
		assertEquals(5, treechildren.size());
		Vector<HierarchyNode> trees = new Vector<HierarchyNode>();
		trees.addAll(treechildren);
		HierarchyNode node1 = trees.get(0);
		HierarchyNode node2 = trees.get(1);
		HierarchyNode node3 = trees.get(2);
		HierarchyNode node4 = trees.get(3);
		HierarchyNode node5 = trees.get(4);
		
		assertNotNull(node1);
		assertEquals("Chestnut", node1.getName());
		assertEquals(1, node1.getPage().getVersion());
		assertNotNull(node2);
		assertEquals("Chestnut", node2.getName());
		assertEquals(2, node2.getPage().getVersion());
		assertNotNull(node3);
		assertEquals("Chestnut", node3.getName());
		assertEquals(3, node3.getPage().getVersion());
		assertNotNull(node4);
		assertEquals("Pine", node4.getName());
		assertEquals(1, node4.getPage().getVersion());
		assertNotNull(node5);
		assertEquals("Pine", node5.getName());
		assertEquals(2, node5.getPage().getVersion());
		
	}

	public void testGetRootName() {
		//default behavior
		String expected = "";
		String actual = tester.getRootName();
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with Home as root
		Properties props = new Properties();
		props.setProperty(ContentHierarchy.PROP_ROOT, "Home");
		tester.setProperties(props);
		expected = "Home";
		actual = tester.getRootName();
		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	public void testGetHierarchy() {
		//default behavior
		String input, expected, actual;
		input = "testing\n{orig-title:Foo Bar/Test}\n";
		expected = "Foo Bar/Test";
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setUnchangedSource(input);
		actual = tester.getHierarchy(page);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		//with different pattern
		Properties props = new Properties();
		props.setProperty(ContentHierarchy.PROP_PATTERN, "\\[\\[hierarchy\\?(.*?)\\]\\]");
		tester.setProperties(props);
		input = "testing\n[[hierarchy?a:b:c]]\n";
		expected = "a:b:c";
		Page page2 = new Page(null);
		page2.setOriginalText(input);
		page2.setUnchangedSource(input);
		actual = tester.getHierarchy(page2);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testGetAncestors() {
		//default behavior
		String input;
		Vector<String> actual;
		input = "Foo Bar/Test/abc";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertEquals("Foo Bar/Test/abc", actual.get(0));
		assertEquals("Foo Bar/Test", actual.get(1));
		assertEquals("Foo Bar", actual.get(2));
		//with different delimiter
		Properties props = new Properties();
		props.setProperty(ContentHierarchy.PROP_DELIM, ":");
		tester.setProperties(props);
		input = "a:b:c:d";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(4, actual.size());
		assertEquals("a:b:c:d", actual.get(0));
		assertEquals("a:b:c", actual.get(1));
		assertEquals("a:b", actual.get(2));
		assertEquals("a", actual.get(3));
		
		//include current: false
		tester.getProperties().clear();
		props.setProperty(ContentHierarchy.PROP_CURRENT, "false");
		tester.setProperties(props);
		input = "Foo Bar/Test/abc";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertEquals("Foo Bar/Test/abc", actual.get(0));
		assertEquals("Foo Bar/Test", actual.get(1));
		assertEquals("Foo Bar", actual.get(2));
		
		//page histories
		tester.getProperties().clear();
		props.setProperty("switch.page-history-preservation", "true");
		tester.setProperties(props);
		input = "Plants/1";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals(input, actual.get(0));
			
		input = "Foo Bar/Test/abc/2";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertEquals("Foo Bar/Test/abc/2", actual.get(0));
		assertEquals("Foo Bar/Test/1", actual.get(1)); 
		assertEquals("Foo Bar/1", actual.get(2));
		
	}
	
	public void testGetPageVersion() {
		String input, suffix, expected, actual;
		input = "foobar-1.txt";
		suffix = "-#.txt";
		expected = "1";
		actual = tester.getPageVersion(input, suffix);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	private String readFile(File file) {
		String filestring = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
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
