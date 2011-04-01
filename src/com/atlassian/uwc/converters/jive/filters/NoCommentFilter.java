package com.atlassian.uwc.converters.jive.filters;

import java.io.File;
import java.io.FileFilter;

import org.apache.log4j.Logger;

public class NoCommentFilter implements FileFilter {

	Logger log = Logger.getLogger(this.getClass());
	public boolean accept(File file) {
		boolean accepting = !file.getName().startsWith("COMMENT-");
		if (!accepting) log.debug("filtering: " + file.getName());
		return accepting;
		
	}

}
