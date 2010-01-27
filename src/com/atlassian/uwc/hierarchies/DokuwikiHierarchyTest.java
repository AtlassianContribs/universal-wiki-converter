package com.atlassian.uwc.hierarchies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class DokuwikiHierarchyTest extends TestCase {

	DokuwikiHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new DokuwikiHierarchy();
		PropertyConfigurator.configure("log4j.properties");
		
	}

	public void testBuildHierarchy() {
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
		
		//test basic moving and basic start page handling 
		
		File sampledir = new File("sampleData/hierarchy/dokuwiki");
		Collection<Page> pages = new Vector<Page>();
		assertTrue(sampledir.exists());
		File[] files = sampledir.listFiles(new NoSvnFilter());
		pages = createPages(pages, files);

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root node
		assertNull(root.getName());
		assertNull(root.getPage());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		
		Set<HierarchyNode> top = root.getChildren();
		assertEquals(5, top.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(top);
		String[] exp = {"Drink", "Start", "Baklava", "Fruit", "Pie" };
		testNodeResults(nodes0, exp);
		
		//test start page moves to parent
		HierarchyNode drink = getNode("Drink", nodes0);
		assertNotNull(drink);
		assertNotNull(drink.getPage());
		String content = "This should be content for the Drink page\n"; //was originally Drink/start.txt
		assertEquals(content, drink.getPage().getConvertedText()); //start.txt got moved to Drink node
		
		//test fixing collisions
		assertEquals(2, drink.getChildren().size());
		Vector<HierarchyNode> drinknodes = new Vector<HierarchyNode>();
		drinknodes.addAll(drink.getChildren());
		String[] exp2 = {"Juice", "Water"};
		testNodeResults(drinknodes, exp2);
		
		HierarchyNode juice = getNode("Juice", drinknodes);
		Vector<HierarchyNode> juicenodes = new Vector<HierarchyNode>();
		juicenodes.addAll(juice.getChildren());
		assertEquals(1, juicenodes.size());
		assertEquals("Juice Apple", juicenodes.get(0).getName());
	}
	

	private HierarchyNode getNode(String name, Vector<HierarchyNode> nodes) {
		for (Iterator iter = nodes.iterator(); iter.hasNext();) {
			HierarchyNode node = (HierarchyNode) iter.next();
			if (node.getName().toLowerCase().equals(name.toLowerCase())) return node;
		}
		return null;
	}

	private void testNodeResults(Vector<HierarchyNode> nodes, String[] exp) {
		for (int i = 0; i < exp.length; i++) {
			String expected = exp[i];
			boolean found = false;
			for (Iterator iter = nodes.iterator(); iter.hasNext();) {
				HierarchyNode node = (HierarchyNode) iter.next();
				String actual = node.getName();
				if (expected.equals(actual)) {
					found = true; 
					break;
				}
			}
			assertTrue(found);
		}
	}
	
	public void testBuildHierarchy_midlevelstartpages() {
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
		
		File sampledir = new File("sampleData/hierarchy/dokuwiki");
		Collection<Page> pages = new Vector<Page>();
		assertTrue(sampledir.exists());
		File[] files = sampledir.listFiles(new NoSvnFilter());
		pages = createPages(pages, files);

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root node
		assertNull(root.getName());
		assertNull(root.getPage());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		
		Set<HierarchyNode> top = root.getChildren();
		assertEquals(5, top.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(top);
		String[] exp = {"Drink", "Start", "Baklava", "Fruit", "Pie" };
		testNodeResults(nodes0, exp);
		
		//test mid level start directories
		HierarchyNode pie = getNode("Pie", nodes0);
		assertNotNull(pie);
		assertNull(pie.getPage());
		assertEquals(3, pie.getChildren().size());
		Vector<HierarchyNode> pienodes = new Vector<HierarchyNode>();
		pienodes.addAll(pie.getChildren());
		String[] exp2 = {"Start", "Pie Apple"};
		testNodeResults(pienodes, exp2);
		
		HierarchyNode start = getNode("Start", pienodes);
		Vector<HierarchyNode> startnodes = new Vector<HierarchyNode>();
		startnodes.addAll(start.getChildren());
		assertEquals(1, startnodes.size());
		assertEquals("Start Apple", startnodes.get(0).getName());
	}
	
	
	public void testBuildHierarchy_collision_levelprop() {
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple,Fruit");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
		
		File sampledir = new File("sampleData/hierarchy/dokuwiki");
		Collection<Page> pages = new Vector<Page>();
		assertTrue(sampledir.exists());
		File[] files = sampledir.listFiles(new NoSvnFilter());
		pages = createPages(pages, files);

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root node
		assertNull(root.getName());
		assertNull(root.getPage());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		
		Set<HierarchyNode> top = root.getChildren();
		assertEquals(5, top.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(top);
		String[] exp = {"Drink", "Start", "Baklava", "Fruit", "Pie" };
		testNodeResults(nodes0, exp);
		
		//needs more than one level of parent to avoid collision
		HierarchyNode fruit1 = getNode("Fruit", nodes0);
		assertNotNull(fruit1);
		assertNotNull(fruit1.getPage());
		assertEquals(1, fruit1.getChildren().size());
		Vector<HierarchyNode> fruitnodes1 = new Vector<HierarchyNode>();
		fruitnodes1.addAll(fruit1.getChildren());
		String[] exp2 = {"Fruit Apple"};
		testNodeResults(fruitnodes1, exp2);

		HierarchyNode pie = getNode("Pie", nodes0);
		assertNotNull(pie);
		assertNull(pie.getPage());
		assertEquals(3, pie.getChildren().size());
		Vector<HierarchyNode> pienodes = new Vector<HierarchyNode>();
		pienodes.addAll(pie.getChildren());
		String[] exppie = {"Pie Apple", "Pie Fruit", "Start"};
		testNodeResults(pienodes, exppie);
		
		HierarchyNode piefruit = getNode("Pie Fruit", pienodes);
		assertNotNull(piefruit);
		assertNotNull(piefruit.getPage());
		assertEquals(1, piefruit.getChildren().size());
		Vector<HierarchyNode> fruitnodes2 = new Vector<HierarchyNode>();
		fruitnodes2.addAll(piefruit.getChildren());
		String[] expfruit = {"Pie Fruit Apple"};
		testNodeResults(fruitnodes2, expfruit);

	}
	
	public void testBuildHierarchy_attachimages() {
		
		Properties props = new Properties();
		props.setProperty("spacekey", "image");
		props.setProperty("space-image", "images,wiki,test,playground");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/doku-pages");
		props.put("attachmentdirectory", "sampleData/hierarchy/doku-media");
		tester.setProperties(props);
		
		File sampledir = new File("sampleData/hierarchy/doku-pages");
		Collection<Page> pages = new Vector<Page>();
		assertTrue(sampledir.exists());
		File[] files = sampledir.listFiles(new NoSvnFilter());
		pages = createPages(pages, files);

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root node
		assertNull(root.getName());
		assertNull(root.getPage());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		
		Set<HierarchyNode> top = root.getChildren();
		assertEquals(6, top.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(top);
		String[] exp = {"Images", "Playground", "Test", "Wiki", "Start", "Test Images" };
		testNodeResults(nodes0, exp);
		
		//needs more than one level of parent to avoid collision
		HierarchyNode start = getNode("Start", nodes0);
		assertNotNull(start);
		assertNotNull(start.getPage());
		assertNotNull(start.getPage().getAttachments());
		Vector<File> startAtt = new Vector<File>();
		startAtt.addAll(start.getPage().getAttachments());
		assertEquals(1, startAtt.size());
		assertEquals("cow.jpg", startAtt.get(0).getName());
		
		HierarchyNode images = getNode("Images", nodes0);
		assertNotNull(images);
		assertNotNull(images.getPage());
		assertNotNull(images.getPage().getAttachments());
		Vector<File> imageAtt = new Vector<File>();
		imageAtt.addAll(images.getPage().getAttachments());
		assertEquals(2, imageAtt.size());
		assertTrue("cow.jpg".equals(imageAtt.get(0).getName()) || 
				"hobbespounce.gif".equals(imageAtt.get(0).getName()));
		assertTrue("cow.jpg".equals(imageAtt.get(1).getName()) || 
				"hobbespounce.gif".equals(imageAtt.get(1).getName()));
		
		Vector<HierarchyNode> imagesNodes = new Vector<HierarchyNode>();
		imagesNodes.addAll(images.getChildren());
		HierarchyNode cows = getNode("Cows", imagesNodes);
		assertNotNull(cows);
		assertNotNull(cows.getPage());
		assertNotNull(cows.getPage().getAttachments());
		Vector<File> cowAtt = new Vector<File>();
		cowAtt.addAll(cows.getPage().getAttachments());
		assertEquals(1, cowAtt.size());
		assertEquals("cow.jpg", cowAtt.get(0).getName());
		
		Vector<HierarchyNode> cowNodes = new Vector<HierarchyNode>();
		cowNodes.addAll(cows.getChildren());
		HierarchyNode jpgs = getNode("Jpgs", cowNodes);
		assertNotNull(jpgs);
		assertNotNull(jpgs.getPage());
		assertNotNull(jpgs.getPage().getAttachments());
		Vector<File> jpgsAtt = new Vector<File>();
		jpgsAtt.addAll(jpgs.getPage().getAttachments());
		assertEquals(1, jpgsAtt.size());
		assertEquals("cow.jpg", jpgsAtt.get(0).getName());
		
	}
	
	public void testBuildHierarchy_fixBranchNames() {
		Properties props = new Properties();
		props.setProperty("spacekey", "food");
		props.setProperty("collision-titles-food", "Apple");
		props.put("filepath-hierarchy-ext", "");
		props.put("filepath-hierarchy-ignorable-ancestors", "sampleData/hierarchy/dokuwiki");
		tester.setProperties(props);
		
		//test basic moving and basic start page handling 
		
		File sampledir = new File("sampleData/hierarchy/dokuwiki");
		Collection<Page> pages = new Vector<Page>();
		assertTrue(sampledir.exists());
		File[] files = sampledir.listFiles(new NoSvnFilter());
		pages = createPages(pages, files);
		//lowercase all the pagenames, artificially
		for (Page page : pages) {
			String name = page.getName().toLowerCase();
			name = name.replaceAll(" ", "_");
			page.setName(name);
		}

		HierarchyNode root = tester.buildHierarchy(pages);
		assertNotNull(root); //root node
		assertNull(root.getName());
		assertNull(root.getPage());
		assertNull(root.getParent());
		assertNotNull(root.getChildren());
		
		Set<HierarchyNode> top = root.getChildren();
		assertEquals(5, top.size());
		Vector<HierarchyNode> nodes0 = new Vector<HierarchyNode>();
		nodes0.addAll(top);
		String[] exp = {"Drink", "Start", "Baklava", "Fruit", "Pie" };
		testNodeResults(nodes0, exp);
		
		//test start page moves to parent
		HierarchyNode drink = getNode("Drink", nodes0);
		assertNotNull(drink);
		assertNotNull(drink.getPage());
		String content = "This should be content for the Drink page\n"; //was originally Drink/start.txt
		assertEquals(content, drink.getPage().getConvertedText()); //start.txt got moved to Drink node
		
		//test fixing collisions
		assertEquals(2, drink.getChildren().size());
		Vector<HierarchyNode> drinknodes = new Vector<HierarchyNode>();
		drinknodes.addAll(drink.getChildren());
		String[] exp2 = {"Juice", "Water"};
		testNodeResults(drinknodes, exp2);
		
		HierarchyNode juice = getNode("Juice", drinknodes);
		Vector<HierarchyNode> juicenodes = new Vector<HierarchyNode>();
		juicenodes.addAll(juice.getChildren());
		assertEquals(1, juicenodes.size());
		assertEquals("Juice Apple", juicenodes.get(0).getName());
	}
	
	private Collection<Page> createPages(Collection<Page> pages, File[] files) {
		for (File file : files) {
			if (file.getName().endsWith(".swp")) continue;
			if (file.isDirectory()) pages = createPages(pages, file.listFiles(new NoSvnFilter()));
			else {
				Page page = new Page(file);
				page.setName(fixname(file.getName()));
				page.setOriginalText(readFile(file));
				page.setConvertedText(page.getOriginalText());
				page.setUnchangedSource(page.getOriginalText());
				page.setPath(fixpath(file));
				pages.add(page);
			}
		}
		return pages;
	}

	// yanked from DokuWikiLinkConverter.formatPageName	
	private String fixname(String name) {
		if (name.endsWith(".txt")) {
			name = name.substring(0, name.length()-4);
		}
		// Replace underscores with spaces
		name = name.replaceAll("_", " ");

		// Casify the name
		name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
		return name;
	}
	private String fixpath(File file) {
		String path = file.getPath();
		int fileNameStart = path.lastIndexOf(File.separator);
		if (fileNameStart >= 0) {
		    path = path.substring(0, fileNameStart);
		} else {
		    path = "";
		}
		return path;
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
