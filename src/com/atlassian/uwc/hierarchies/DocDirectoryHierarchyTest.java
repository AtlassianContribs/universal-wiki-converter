package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.atlassian.uwc.ui.Page;

public class DocDirectoryHierarchyTest extends TestCase {

	DocDirectoryHierarchy tester = null;
	Logger log = Logger.getLogger(this.getClass());
	protected void setUp() throws Exception {
		tester = new DocDirectoryHierarchy();
		PropertyConfigurator.configure("log4j.properties");
		Properties props = new Properties();
		props.setProperty("doc-directory-attachments", "sampleData/docdirectory/attachments");
		tester.setProperties(props);
	}

	public void testBuildHierarchy() {
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/docdirectory/testtemplate.txt");
		assertTrue(sampledir.exists());
		
		//ignore svn directories
		Properties props = tester.getProperties();
		props.setProperty("doc-directory-exclude", "\\.svn");
		tester.setProperties(props);
		
		Page page = new Page(sampledir);
		page.setOriginalText("");
		page.setConvertedText(page.getOriginalText());
		page.setUnchangedSource(page.getOriginalText());
		page.setName(sampledir.getName().replaceFirst("\\.txt$", ""));
		pages.add(page);
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Collection<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Home"));
		assertTrue(nodes1.get(0).getPage().getAttachments().size() == 1);
		Object object = nodes1.get(0).getPage().getAttachments().toArray()[0];
		assertTrue(object instanceof File);
		File file = (File) object;
		assertTrue("test.txt".equals(file.getName()));
		
		Collection<HierarchyNode> children2 = nodes1.get(0).getChildren();
		assertEquals(3, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		for (HierarchyNode node : nodes2) {
			assertTrue(node.getName().equals("a") ||
					node.getName().equals("b") ||
					node.getName().equals("c")
					);
			if (node.getName().equals("a")) {
				assertTrue(node.getPage().getAttachments().size() == 2);
				assertTrue(node.getPage().getAttachments().toArray()[0] instanceof File);
				File filea = (File) node.getPage().getAttachments().toArray()[0];
				assertTrue("test1.txt".equals(filea.getName())||
						"test2.txt".equals(filea.getName())
						);
				Collection<HierarchyNode> children3 = node.getChildren();
				assertEquals(1, children3.size());
				Vector<HierarchyNode> nodes3 = new Vector<HierarchyNode>();
				nodes3.addAll(children3);
				assertTrue((nodes3.get(0)).getName().equals("d"));
				assertTrue(nodes3.get(0).getPage().getAttachments().size() == 1);
				assertTrue(nodes3.get(0).getPage().getAttachments().toArray()[0] instanceof File);
				File filed = (File) nodes3.get(0).getPage().getAttachments().toArray()[0];;
				assertTrue("test3.txt".equals(filed.getName()));
			}
			if (node.getName().equals("b")) {
				assertTrue(node.getPage().getAttachments().size() == 1);
				assertTrue(node.getPage().getAttachments().toArray()[0] instanceof File);
				File fileb = (File) node.getPage().getAttachments().toArray()[0];
				assertTrue("test4.txt".equals(fileb.getName()));
			}
			if (node.getName().equals("c")) {
				assertTrue(node.getPage().getAttachments().size() == 1);
				assertTrue(node.getPage().getAttachments().toArray()[0] instanceof File);
				File filec = (File) node.getPage().getAttachments().toArray()[0];
				assertTrue("test5.txt".equals(filec.getName()));
			}
		}
	}
	
	public void testBuildHierarchy_root() {
		Properties props = tester.getProperties();
		props.setProperty("doc-directory-root", "Test");
		tester.setProperties(props);
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/docdirectory/testtemplate.txt");
		assertTrue(sampledir.exists());
		
		Page page = new Page(sampledir);
		page.setOriginalText("");
		page.setConvertedText(page.getOriginalText());
		page.setUnchangedSource(page.getOriginalText());
		page.setName(sampledir.getName().replaceFirst("\\.txt$", ""));
		pages.add(page);
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Collection<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Test"));
	}
	
	public void testBuildHierarchy_template() {
		Properties props = tester.getProperties();
		props.setProperty("doc-directory-template", "/Users/laura/Code/Subversion/uwc-spac/devel/sampleData/docdirectory/testtemplate.txt");
		tester.setProperties(props);
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/docdirectory/testtemplate.txt");
		assertTrue(sampledir.exists());
		
		Page page = new Page(sampledir);
		page.setOriginalText("");
		page.setConvertedText(page.getOriginalText());
		page.setUnchangedSource(page.getOriginalText());
		page.setName(sampledir.getName().replaceFirst("\\.txt$", ""));
		pages.add(page);
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Collection<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		HierarchyNode node = nodes1.get(0);
		assertTrue((node).getName().equals("Home"));
		assertEquals("Testing\n{attachments}\n", node.getPage().getConvertedText());
	}
	
	public void testBuildHierarhcy_exclude() {
		Properties props = tester.getProperties();
		props.setProperty("doc-directory-exclude", "(c)|(\\.svn)"); //need to ignore svn dir as well
		tester.setProperties(props);
		Vector<Page> pages = new Vector<Page>();
		File sampledir = new File("sampleData/docdirectory/testtemplate.txt");
		assertTrue(sampledir.exists());
		
		Page page = new Page(sampledir);
		page.setOriginalText("");
		page.setConvertedText(page.getOriginalText());
		page.setUnchangedSource(page.getOriginalText());
		page.setName(sampledir.getName().replaceFirst("\\.txt$", ""));
		pages.add(page);
		
		HierarchyNode actual = tester.buildHierarchy(pages);
		assertNotNull(actual); //root node
		assertNull(actual.getName());
		assertNull(actual.getPage());
		assertNull(actual.getParent());
		assertNotNull(actual.getChildren());
		
		Collection<HierarchyNode> children1 = actual.getChildren();
		assertEquals(1, children1.size());
		Vector<HierarchyNode> nodes1 = new Vector<HierarchyNode>();
		nodes1.addAll(children1);
		assertTrue((nodes1.get(0)).getName().equals("Home"));
		assertTrue(nodes1.get(0).getPage().getAttachments().size() == 1);
		Object object = nodes1.get(0).getPage().getAttachments().toArray()[0];
		assertTrue(object instanceof File);
		File file = (File) object;
		assertTrue("test.txt".equals(file.getName()));
		
		Collection<HierarchyNode> children2 = nodes1.get(0).getChildren();
		assertEquals(2, children2.size());
		Vector<HierarchyNode> nodes2 = new Vector<HierarchyNode>();
		nodes2.addAll(children2);
		for (HierarchyNode node : nodes2) {
			assertTrue(node.getName(), node.getName().equals("a") ||
					node.getName().equals("b"));
		}
	}
	
}
