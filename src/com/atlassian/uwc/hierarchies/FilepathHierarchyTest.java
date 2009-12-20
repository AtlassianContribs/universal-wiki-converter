package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.converters.jotspot.TitleConverter;
import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class FilepathHierarchyTest extends TestCase {

	FilepathHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	final String PARENT_FILEPATH = "parent.txt";
	final String CHILD_FILEPATH = "parent/child.txt";
	final String CHILD_FILEPATH2 = "parent/child2.txt";
	final String PARENT_FILENAME = "parent.txt";
	final String CHILD_FILENAME = "child.txt";
	final String CHILD_FILENAME2 = "child2.txt";
	protected void setUp() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		tester = new FilepathHierarchy();
	}
	
	protected void tearDown() {
		tester = null;
	}
	
	public void testBuildHierarchyErrors() {
		HierarchyNode node = tester.buildHierarchy(null);
		assertNull(node);
		
		Vector<Page> pages = new Vector<Page>();
		HierarchyNode node2 = tester.buildHierarchy(pages);
		assertNull(node2);
	}

	public void testBuildHierarchy() {
		//create stubs
		Page waterPage = new Page(new File(""));
		Page baklavaPage = new Page(new File(""));
		Page applePage = new Page(new File(""));
		Page fruitPage = new Page(new File(""));
		Page drinkPage = new Page(new File(""));
		Page foodPage = new Page(new File(""));
		
		setupStubPage(waterPage, "Water.txt", "Drink");
		setupStubPage(baklavaPage, "Baklava.txt", "Food");
		setupStubPage(applePage, "Apple.txt", "Food/Fruit");
		setupStubPage(fruitPage, "Fruit.txt", "Food");
		setupStubPage(drinkPage, "Drink.txt", "");
		setupStubPage(foodPage, "Food.txt", "");
		
		//try a different order
		List<Page> inputPages = new LinkedList<Page>();
		inputPages.add(waterPage);
		inputPages.add(baklavaPage);
		inputPages.add(applePage);
		inputPages.add(fruitPage);
		inputPages.add(drinkPage);
		inputPages.add(foodPage);
		
		HierarchyNode actual = tester.buildHierarchy(inputPages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Set<HierarchyNode> level1ChildSet = actual.getChildren();
		assertEquals(2, level1ChildSet.size());
		Vector<HierarchyNode> level1Children = new Vector<HierarchyNode>();
		level1Children.addAll(level1ChildSet);
		HierarchyNode level1Child1 = level1Children.get(0);
		HierarchyNode level1Child2 = level1Children.get(1);
		boolean level1Child1_Food = level1Child1.getName().startsWith("Food");
		
		HierarchyNode foodNode = level1Child1_Food?level1Child1:level1Child2;
		HierarchyNode drinkNode = level1Child1_Food?level1Child2:level1Child1;
		
		testFoodNode(foodNode, foodPage, fruitPage, applePage, baklavaPage);
		testDrinkNode(drinkNode, drinkPage, waterPage);
	}
	public void testBuildHierarchy2() {
		//create stubs
		Page waterPage = new Page(new File(""));
		Page baklavaPage = new Page(new File(""));
		Page applePage = new Page(new File(""));
		Page fruitPage = new Page(new File(""));
		Page drinkPage = new Page(new File(""));
		Page foodPage = new Page(new File(""));
		
		setupStubPage(waterPage, "Water.txt", "Drink");
		setupStubPage(baklavaPage, "Baklava.txt", "Food");
		setupStubPage(applePage, "Apple.txt", "Food/Fruit");
		setupStubPage(fruitPage, "Fruit.txt", "Food");
		setupStubPage(drinkPage, "Drink.txt", "");
		setupStubPage(foodPage, "Food.txt", "");
		

		//this order
		List<Page> inputPages = new LinkedList<Page>();
		inputPages.add(fruitPage);
		inputPages.add(baklavaPage);
		inputPages.add(applePage);
		inputPages.add(foodPage);
		inputPages.add(drinkPage);
		inputPages.add(waterPage);
		
		HierarchyNode actual = tester.buildHierarchy(inputPages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Set<HierarchyNode> level1ChildSet = actual.getChildren();
		assertEquals(2, level1ChildSet.size());
		Vector<HierarchyNode> level1Children = new Vector<HierarchyNode>();
		level1Children.addAll(level1ChildSet);
		HierarchyNode level1Child1 = level1Children.get(0);
		HierarchyNode level1Child2 = level1Children.get(1);
		boolean level1Child1_Food = level1Child1.getName().startsWith("Food");
		
		HierarchyNode foodNode = level1Child1_Food?level1Child1:level1Child2;
		HierarchyNode drinkNode = level1Child1_Food?level1Child2:level1Child1;
		
		testFoodNode(foodNode, foodPage, fruitPage, applePage, baklavaPage);
		testDrinkNode(drinkNode, drinkPage, waterPage);

	}
	public void testBuildHierarchy3() {
		//create stubs
		Page waterPage = new Page(new File(""));
		Page baklavaPage = new Page(new File(""));
		Page applePage = new Page(new File(""));
		Page fruitPage = new Page(new File(""));
		Page drinkPage = new Page(new File(""));
		Page foodPage = new Page(new File(""));
		
		setupStubPage(waterPage, "Water.txt", "Drink");
		setupStubPage(baklavaPage, "Baklava.txt", "Food");
		setupStubPage(applePage, "Apple.txt", "Food/Fruit");
		setupStubPage(fruitPage, "Fruit.txt", "Food");
		setupStubPage(drinkPage, "Drink.txt", "");
		setupStubPage(foodPage, "Food.txt", "");
		
	
		//another order
		List<Page >inputPages = new LinkedList<Page>();
		inputPages.add(drinkPage);
		inputPages.add(waterPage);
		inputPages.add(baklavaPage);
		inputPages.add(applePage);
		inputPages.add(foodPage);
		inputPages.add(fruitPage);
		
		HierarchyNode actual = tester.buildHierarchy(inputPages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Set<HierarchyNode> level1ChildSet = actual.getChildren();
		assertEquals(2, level1ChildSet.size());
		Vector<HierarchyNode> level1Children = new Vector<HierarchyNode>();
		level1Children.addAll(level1ChildSet);
		HierarchyNode level1Child1 = level1Children.get(0);
		HierarchyNode level1Child2 = level1Children.get(1);
		boolean level1Child1_Food = level1Child1.getName().startsWith("Food");
		
		HierarchyNode foodNode = level1Child1_Food?level1Child1:level1Child2;
		HierarchyNode drinkNode = level1Child1_Food?level1Child2:level1Child1;
		
		testFoodNode(foodNode, foodPage, fruitPage, applePage, baklavaPage);
		testDrinkNode(drinkNode, drinkPage, waterPage);
	
	}
	
	
	private void buildPages(File file, Vector<Page> inputPages) {
		if (file.isFile()) {
			Page page = new Page(file);
			page.setName(file.getName());
			page.setPath(getPath(file.getPath()));
			inputPages.add(page);
		}
		else if (file.isDirectory()) {
			File[] files = file.listFiles(new NoSvnFilter());
			for (File f: files) {
				buildPages(f, inputPages);
			}
		}
	}
	private String getPath(String pagePath) {
		int fileNameStart = pagePath.lastIndexOf(File.separator);
		if (fileNameStart >= 0) {
		    pagePath = pagePath.substring(0, fileNameStart);
		} else {
		    pagePath = "";
		}
		return pagePath;
	}

	private void setupStubPage(Page page, String file, String parentDir) {
		page.setName(file);
		page.setPath(parentDir);
	}

	public void testNewNode() {
		HierarchyNode root = new HierarchyNode();
		Page inputPage = new Page(new File(PARENT_FILEPATH));
		inputPage.setName(PARENT_FILENAME);
		inputPage.setPath(PARENT_FILEPATH);
		HierarchyNode inputNode = root;
		HierarchyNode parentNode = new HierarchyNode(inputPage, inputNode);
		String expected = PARENT_FILENAME;
		String actual = parentNode.getName();
		assertEquals(expected, actual);
		HierarchyNode parentExpected = root;
		HierarchyNode parentActual = parentNode.getParent();
		assertNotNull(parentActual);
		assertEquals(parentActual, parentExpected);
		
		inputPage = new Page(new File(CHILD_FILEPATH));
		inputPage.setName(CHILD_FILENAME);
		inputPage.setPath(CHILD_FILEPATH);
		inputNode = parentNode;
		HierarchyNode childNode = new HierarchyNode(inputPage, inputNode);
		expected = CHILD_FILENAME;
		actual = childNode.getName();
		assertEquals(expected, actual);
		parentExpected = parentNode;
		parentActual = childNode.getParent();
		assertNotNull(parentActual);
		assertEquals(parentActual, parentExpected);
	}
	
	
	public void testGetRootNode() {
		HierarchyNode root = tester.getRootNode();
		assertNotNull(root);
		assertNull(root.getName());
		
		//test that the root is always the same;
		HierarchyNode root2 = tester.getRootNode();
		assertNotNull(root);
		assertNull(root.getName());
		assertEquals(root, root2);
	}
	
	public void testGetAncestors() {
		String input = "Fruit";
		Vector<String> actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(1, actual.size());
		assertEquals("Fruit", actual.remove(0));
		
		
		input = "Food/Fruit/Apple";
		actual = tester.getAncestors(input);
		assertNotNull(actual);
		assertEquals(3, actual.size());
		assertEquals("Food", actual.remove(0));
		assertEquals("Fruit", actual.remove(0));
		assertEquals("Apple", actual.remove(0));
	}
	
	public void testHasExistingRelationship() {
		HierarchyNode parent = new HierarchyNode();
		parent.setName("parent");
		HierarchyNode child = new HierarchyNode(null, parent);
		child.setName("child");
		
		boolean expected = true;
		boolean actual = tester.hasExistingRelationship(parent, "child");
		assertEquals(expected, actual);
		
		expected = false;
		actual = tester.hasExistingRelationship(parent, "notchild");
		assertEquals(expected, actual);
	}
	
	public void testCreateChildNode_String() {
		HierarchyNode parent = new HierarchyNode();
		parent.setName("parent");
		String childname = "child";
		
		HierarchyNode actual = tester.createChildNode(parent, childname);
		assertNotNull(actual);
		assertEquals("child", actual.getName());
		assertEquals(parent, actual.getParent());
		assertTrue(actual.getChildren().isEmpty());
		assertNull(actual.getPage());
	}
	
	public void testCreateChildNode_Page() {
		HierarchyNode parent = new HierarchyNode();
		parent.setName("parent");
		Page child = new Page(new File(""));
		String childname = "child";
		child.setName(childname);
		child.setPath("parent/child");
		
		HierarchyNode actual = tester.createChildNode(parent, child);
		assertNotNull(actual);
		assertEquals("child", actual.getName());
		assertEquals(parent, actual.getParent());
		assertEquals(child, actual.getPage());
		assertTrue(actual.getChildren().isEmpty());

	}
	
	
	public void testGetChildNode() {
		HierarchyNode parent = new HierarchyNode();
		parent.setName("parent");
		Page child = new Page(new File(""));
		String childname = "child";
		child.setName(childname);
		child.setPath("parent/child");
		HierarchyNode expected = tester.createChildNode(parent, child);
		
		HierarchyNode actual = tester.getChildNode(parent, childname);
		assertNotNull(actual);
		assertEquals(expected, actual);
		assertEquals(expected.getParent(), actual.getParent());
	}

	public void testGetFileExtension() {
		String path = "path.txt";
		String expected = ".txt";
		String actual = tester.getFileExtension(path);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		path = "path.wiki";
		expected = ".txt";
		actual = tester.getFileExtension(path);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester = new FilepathHierarchy();
		expected = ".wiki";
		actual = tester.getFileExtension(path);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		tester = new FilepathHierarchy();
		path = "path";
		expected = "";
		actual = tester.getFileExtension(path);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		path = null;
		actual = tester.getFileExtension(path);
		assertNull(actual);
		
		
	}
	
	public void testFoodNode(HierarchyNode foodNode, Page foodPage, Page fruitPage, Page applePage, Page baklavaPage) {
		testNode(foodNode, "Food.txt", foodPage, tester.getRootNode());
		//children
		assertNotNull(foodNode.getChildren());
		assertEquals(2, foodNode.getChildren().size());
		HierarchyNode fruitNode = null;
		HierarchyNode baklavaNode = null;
		for (HierarchyNode child : foodNode.getChildren()) {
			if (child.getName().startsWith("Fruit")) fruitNode = child;
			if (child.getName().startsWith("Baklava")) baklavaNode = child;
		}
		testFruitNode(fruitNode, fruitPage, applePage, foodNode);
		testBaklavaNode(baklavaNode, baklavaPage, foodNode);
	}
	
	

	private void testFruitNode(HierarchyNode fruitNode, Page fruitPage, Page applePage, HierarchyNode parent) {
		testNode(fruitNode, "Fruit.txt", fruitPage, parent);
		//children
		assertNotNull(fruitNode.getChildren());
		assertEquals(1, fruitNode.getChildren().size());
		HierarchyNode appleNode = null;
		for (HierarchyNode child : fruitNode.getChildren()) {
			appleNode = child;
		}
		testAppleNode(appleNode, applePage, fruitNode);

	}

	private void testAppleNode(HierarchyNode appleNode, Page applePage, HierarchyNode parent) {
		testNode(appleNode, "Apple.txt", applePage, parent);
		if (appleNode.getChildren() != null) assertTrue(appleNode.getChildren().isEmpty());
		else assertNull(appleNode.getChildren());
	}

	private void testBaklavaNode(HierarchyNode baklavaNode, Page baklavaPage, HierarchyNode parent) {
		testNode(baklavaNode, "Baklava.txt", baklavaPage, parent);
		if (baklavaNode.getChildren() != null) assertTrue(baklavaNode.getChildren().isEmpty());
		else assertNull(baklavaNode.getChildren());
	}

	public void testDrinkNode(HierarchyNode drinkNode, Page drinkPage, Page waterPage) {
		testNode(drinkNode, "Drink.txt", drinkPage, tester.getRootNode());
		//children
		assertNotNull(drinkNode.getChildren());
		assertEquals(1, drinkNode.getChildren().size());
		HierarchyNode waterNode = null;
		for (HierarchyNode child : drinkNode.getChildren()) {
			waterNode = child;
		}
		testWaterNode(waterNode, waterPage, drinkNode);
	}
	
	public void testGetFileExtensionWithDots() {
		String root_file1 = "a.b.c.uwc";
		
		String expected = ".uwc";
		String actual = tester.getFileExtension(root_file1);
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	public void testDuplicates_diffDir() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("test/A/");
		page1.setOriginalText("testing duplicates - Foo");
		
		Page page2 = new Page(null);
		page2.setName("foo");
		page2.setPath("test/B/");
		page2.setOriginalText("testing duplicates - foo");
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		
		HierarchyNode node = tester.buildHierarchy(pages);
		assertNotNull(node);
	}
	
	public void testDuplicates_sameDir() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("test/A/");
		page1.setOriginalText("testing duplicates - Foo");
		
		Page page2 = new Page(null);
		page2.setName("foo");
		page2.setPath("test/A/");
		page2.setOriginalText("testing duplicates - foo");
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		
		HierarchyNode node = tester.buildHierarchy(pages);
		assertNotNull(node);
	}
	
	public void testDotsInPagenamesWithNoExt() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("test/A/");
		page1.setOriginalText("testing dots - Foo");
		
		Page page2 = new Page(null);
		page2.setName("9.0_MA_OA_Message_Processing");
		page2.setPath("test/A/");
		page2.setOriginalText("testing dots - 9.0...");
		
		Page page3 = new Page(null);
		page3.setName("Bar");
		page3.setPath("test/B/");
		page3.setOriginalText("testing dots - Bar");
		
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		
		//set an explicit extension
		Properties properties = tester.getProperties();
		if (properties == null) properties = new Properties();
		properties.put("filepath-hierarchy-ext", "");
		tester.setProperties(properties);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		assertEquals(1, root.getChildren().size());
		assertEquals(7, root.countDescendants()); //test, A, Foo, 9.0.., Bar, B, and null root
		//nodes
		for (HierarchyNode node : root.getChildren()) {
			assertEquals("test", node.getName());
			assertEquals(2, node.getChildren().size());
			//children nodes
			for (HierarchyNode child : node.getChildren()) {
				assertTrue("child name should be A or B, but is: " + child.getName(), 
						child.getName().equals("A") || child.getName().equals("B"));
				if (child.getName().equals("A")) {
					assertEquals(2, child.getChildren().size());
					for (HierarchyNode gchild : child.getChildren()) {
						assertTrue("gchild name should be Foo or 9.0_MA_OA_Message_Processing",
								gchild.getName().equals("Foo") || gchild.getName().equals("9.0_MA_OA_Message_Processing"));
					}
				}
				else { //B
					assertEquals(1, child.getChildren().size());
					for (HierarchyNode gchild : child.getChildren()) {
						assertTrue("gchild name should be Bar",
								gchild.getName().equals("Bar"));
					}
				}
			}
		}
	}
	
	public void testHiddenPages() {
		Page page1 = new Page(null);
		page1.setName("Foo");
		page1.setPath("test/A/");
		page1.setOriginalText("testing hidden - Foo");
		
		Page page2 = new Page(null);
		page2.setName(".hidden");
		page2.setPath("test/A/");
		page2.setOriginalText("testing hidden - .hidden");
		
		Page page3 = new Page(null);
		page3.setName("Bar");
		page3.setPath("test/B/");
		page3.setOriginalText("testing hidden - Bar");
		Vector<Page> pages = new Vector<Page>();
		pages.add(page1);
		pages.add(page2);
		pages.add(page3);
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
	}
	
	public void testChangedPagename() { //UWC-297, could occur with page title framework usage
		File dir = new File("sampleData/hierarchy/basic");
		assertTrue(dir.isDirectory() && dir.exists());
		tester.clearRootNode();
		Properties properties = tester.getProperties();
		properties.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/basic");
		
		Vector<Page> pages = new Vector<Page>();
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.getName().startsWith(".svn")) continue;
			if (file.isDirectory()) {
				File[] children = file.listFiles();
				for (File child : children) {
					if (child.getName().startsWith(".svn")) continue;
					if (child.isFile()) {
						Page page = new Page(child);
						page.setName(child.getName().replaceFirst("\\.txt$", ""));
						page.setPath(child.getPath().replaceFirst("[^\\/]*$", ""));
						pages.add(page);
					}
				}
			}
			else {
				Page page = new Page(file);
				page.setName(file.getName().replaceFirst("\\.txt$", ""));
				page.setPath(file.getPath().replaceFirst("[^\\/]*$", ""));
				pages.add(page);
			}
		}

		//make some page title changes
		for (Page page : pages) {
			if (page.getName().equals("Drink")) {
				page.setName("Liquid");
			}
			if (page.getName().equals("Food")) {
				page.setName("Comestibles");
			}
		}
		
		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		assertEquals(6, root.countDescendants());
		Set<HierarchyNode> level1 = root.getChildren();
		assertNotNull(level1);
		assertEquals(2, level1.size());
		for (HierarchyNode lvl1Node : level1) {
			assertNotNull(lvl1Node);
			if ("Food".equals(lvl1Node.getName())) fail("Still have Food node");
			else if ("Food.txt".equals(lvl1Node.getName())) fail("Still have Drink node");
			else if ("Drink".equals(lvl1Node.getName())) fail("Still have Drink node");
			else if ("Drink.txt".equals(lvl1Node.getName())) fail("Still have Drink node");
			else if ("Comestibles".equals(lvl1Node.getName())) {
				assertEquals(3, lvl1Node.countDescendants());
				Set<HierarchyNode> level2 = lvl1Node.getChildren();
				assertNotNull(level2);
				assertEquals(2, level2.size());
				for (HierarchyNode lvl2Node : level2) {
					if ("Baklava".equals(lvl2Node.getName())) continue;
					if ("Fruit".equals(lvl2Node.getName())) continue;
					else fail("Unexpected node under Comestibles: " + lvl2Node.getName());
				}
			}
			else if ("Liquid".equals(lvl1Node.getName())) {
				assertEquals(2, lvl1Node.countDescendants());
				Set<HierarchyNode> level2 = lvl1Node.getChildren();
				assertNotNull(level2);
				assertEquals(1, level2.size());
				for (HierarchyNode lvl2Node : level2) {
					if ("Water".equals(lvl2Node.getName())) continue;
					else fail("Unexpected node under Liquid: " + lvl2Node.getName());
				}
			}
		}
	}
	

	public void testDirSuffix() { //allow dir with subpages to have a different suffix from related page
		FilepathHierarchy tester = new MindtouchHierarchy();
		File dir = new File("sampleData/mindtouch/junit_resources/links");
		assertTrue(dir.isDirectory() && dir.exists());
		tester.clearRootNode();
		Properties properties = tester.getProperties();
		properties.put("filepath-hierarchy-ignorable-ancestors", "sampleData/mindtouch/junit_resources/links");
		
		Vector<Page> pages = getDirSuffixPages(dir);

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root);
		assertEquals(6, root.countDescendants());
		Set<HierarchyNode> level1 = root.getChildren();
		assertNotNull(level1);
		assertEquals(1, level1.size());
		for (HierarchyNode lvl1Node : level1) {
			assertNotNull(lvl1Node);
			if (lvl1Node.getName().endsWith("_subpages")) fail("Still have subpages dir node.");
			assertEquals(5, lvl1Node.countDescendants());
			Set<HierarchyNode> level2 = lvl1Node.getChildren();
			assertNotNull(level2);
			assertEquals(1, level2.size());
			for (HierarchyNode lvl2Node : level2) {
					if (lvl2Node.getName().endsWith("_subpages")) fail("Still have subpages dir node.");
					Set<HierarchyNode> level3 = lvl2Node.getChildren();
					assertNotNull(level3);
					assertEquals(3, level3.size());
			}
		}
	}

	private Vector<Page> getDirSuffixPages(File dir) {
		Vector<Page> pages = new Vector<Page>();
		if (dir.isFile()) {
			Page page = new Page(dir);
			String name = dir.getName().replaceFirst("\\.xml$", "");
			name = name.replaceFirst("^\\d+_", "");
			page.setName(name);
			page.setPath(dir.getPath().replaceFirst("[^\\/]*$", ""));
			pages.add(page);
		}
		File[] files = dir.listFiles(new NoSvnFilter());
		if (files == null) return pages;
		for (File file : files) {
			pages.addAll(getDirSuffixPages(file));
		}
		return pages;
	}
	

	
	
	public void testJustRelativeRoot() {
		File root = new File("sampleData/hierarchy/basic");
		assertTrue(root.exists());
		Vector<Page> inputPages = new Vector<Page>();
		buildPages(root, inputPages);

		HierarchyNode actual = tester.buildHierarchy(inputPages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());

		Set<HierarchyNode> level1ChildSet = actual.getChildren();
		assertEquals(1, level1ChildSet.size());
		assertEquals("sampleData.txt", level1ChildSet.iterator().next().getName());
		
		actual = null;
		tester.clearRootNode();
		Properties properties = tester.getProperties();
		properties.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy");
		actual = tester.buildHierarchy(inputPages);
		assertNotNull(actual); //root node
		level1ChildSet = actual.getChildren();
		assertEquals(1, level1ChildSet.size());
		assertEquals("basic.txt", level1ChildSet.iterator().next().getName());
	}
	
	public void testRemovePrefix() {
		String input, expected, actual;
		input = "sampleData/hierarchy/basic/Water.txt";
		String prefix = "sampleData/hierarchy";
		expected = "basic/Water.txt";
		actual = tester.removePrefix(input, prefix, File.separator);
		assertNotNull(actual);
		assertEquals(expected, actual);
		
		input = "Z:\\laura On My Mac\\Code\\Subversion\\uwc-current\\devel\\sampleData\\hierarchy\\basic\\Water.txt";
		prefix = "Z:\\laura On My Mac\\Code\\Subversion\\uwc-current\\devel\\sampleData\\hierarchy";
		expected = "basic\\Water.txt";
		actual = tester.removePrefix(input, prefix, "\\"); //Windows test
		assertNotNull(actual);
		assertEquals(expected, actual);
	}
	
	private void testWaterNode(HierarchyNode waterNode, Page waterPage, HierarchyNode parent) {
		testNode(waterNode, "Water.txt", waterPage, parent);
		if (waterNode.getChildren() != null) assertTrue(waterNode.getChildren().isEmpty());
		else assertNull(waterNode.getChildren());
	}

	private void testNode(HierarchyNode node, String name, Page page, HierarchyNode parent) {
		assertNotNull(node);
		log.debug("testing node: " + name);
		assertEquals("name test", name, node.getName());
		assertEquals("page test", page, node.getPage());
		assertEquals("parent test", parent, node.getParent());
	}
	
}
