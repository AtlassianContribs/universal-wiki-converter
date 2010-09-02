package com.atlassian.uwc.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

public class PageTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testCompareTo() {
		//test simple ascii comparison
		Page pageA = new Page(new File("A"));
		Page pageB = new Page(new File("B"));
		Page pageC = new Page(new File("C"));
		pageA.setName("A");
		pageB.setName("B");
		pageC.setName("C");
		
		Set<Page> sorting = new TreeSet<Page>();
		sorting.add(pageB);
		sorting.add(pageC);
		sorting.add(pageA);
		
		ArrayList<Page> sorted = new ArrayList<Page>(); 
		sorted.addAll(sorting);
		
		assertEquals(pageA, sorted.get(0));
		assertEquals(pageB, sorted.get(1));
		assertEquals(pageC, sorted.get(2));
		
		//test version comparison
		Page pageA2 = new Page(new File("A"));
		pageA.setName("A");
		pageA2.setName("A2");
		pageA.setVersion(1);
		pageA2.setVersion(2);
		
		sorting = new TreeSet<Page>();
		sorting.add(pageA2);
		sorting.add(pageC);
		sorting.add(pageA);
		
		sorted = new ArrayList<Page>(); 
		sorted.addAll(sorting);
		
		assertEquals(pageA, sorted.get(0));
		assertEquals(pageA2, sorted.get(1));
		assertEquals(pageC, sorted.get(2));
		
	}
	
	public void testSetLatestVersionData() {
		//test simple ascii comparison
		Page pageA = new Page(new File("A"));
		Page pageA2 = new Page(new File("A"));
		Page pageC = new Page(new File("C"));
		pageA.setName("A");
		pageA2.setName("A");
		pageC.setName("C");

		pageA.setVersion(1);
		pageA2.setVersion(2);
		pageC.setVersion(1);
		
		HashMap actual = Page.getLatestVersions();
		assertNotNull(actual);
		assertNull(actual.get("foo"));
		assertNotNull(actual.get("A"));
		assertNotNull(actual.get("C"));
		assertTrue(2 == (Integer) actual.get("A"));
		assertTrue(1 == (Integer) actual.get("C"));
		
		assertTrue(2 == Page.getLatestVersion("A"));
		assertTrue(1 == Page.getLatestVersion("C"));
	}

}
