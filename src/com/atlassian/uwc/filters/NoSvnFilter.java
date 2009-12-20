package com.atlassian.uwc.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Useful for testing hierarchy, since sampleData directory contains .svn files 
 * which interfere with tests.
 */
public class NoSvnFilter implements FileFilter {

	public boolean accept(File file) {
		if (Pattern.matches( "[.]svn", file.getName())) return false;
		return true;
	}

}
