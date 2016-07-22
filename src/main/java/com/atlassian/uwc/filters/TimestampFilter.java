package com.atlassian.uwc.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimestampFilter implements FileFilter {

	Pattern timestamp = Pattern.compile("^(\\d+)\\.txt$");
	public boolean accept(File file) {
		String filename = file.getName();
		if ("index.txt".equals(filename)) return false; //index.txt is never the right answer
		if (file.isFile() && !filename.endsWith("txt")) return false; //only allow txt files
		Matcher timestampFinder = timestamp.matcher(filename);
		if (timestampFinder.find()) { //if it's a timestamp
			long mostRecent = getMostRecent(file);
			long thisTime = Long.parseLong(timestampFinder.group(1));
			return (thisTime == mostRecent);
		}
		return true; //everything else (parent directories, regression tests, etc.)
	}
	
	protected long getMostRecent(File file) {
		File[] siblings = file.getParentFile().listFiles();
		long most = -1;
		for (int i = 0; i < siblings.length; i++) {
			File sib = siblings[i];
			Matcher timestampFinder = timestamp.matcher(sib.getName());
			if (timestampFinder.find()) {
				long thisTime = Long.parseLong(timestampFinder.group(1));
				if (thisTime > most) most = thisTime;
			}
		}
		return most;
	}

}
