package com.atlassian.uwc.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.*;

/**
 * Filters out .DS_Store and *.xml files.
 */
public class XWikiFilter implements FileFilter {

	public boolean accept(File file) {
		String name = file.getName();
		if (".DS_Store".equals(name)) return false;
		if (Pattern.matches(".*\\.xml$", name)) return false;
		return true;
	}

}
