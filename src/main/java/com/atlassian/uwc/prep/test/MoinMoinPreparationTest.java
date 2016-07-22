package com.atlassian.uwc.prep.test;

import java.io.File;
import java.io.FilenameFilter;

import junit.framework.TestCase;

import com.atlassian.uwc.prep.MoinMoinPreparation.PageDirFileFilter;

public class MoinMoinPreparationTest extends TestCase {
	public void testPageDirFileFilter() {
		FilenameFilter filter = new PageDirFileFilter();
		File file = new File("", "JeanBaptisteCatt(c3a9)");
		if (file.mkdir()) {
			assertTrue(filter.accept(new File(""),
			"JeanBaptisteCatt(c3a9)"));
		}
		file = new File("", "JeanBaptisteCatt(c3a92f)MoinEditorBackup");
		if (file.mkdir()) {
			assertFalse(filter.accept(new File(""),
			"JeanBaptisteCatt(c3a92f)MoinEditorBackup"));
		}
	}
}
