package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class HierarchyTitleConverter extends DokuwikiUserDate {

	static Logger log = Logger.getLogger(HierarchyTitleConverter.class);
	public void convert(Page page) {
		if (page instanceof VersionPage) { //we'll address this in the engine
			return;
		}
		String name = page.getName();
		//check the metadata for a title
		name = getMetaTitle(page);
		// Strip trailing file name extension.
		name = fixTitle(name);
		log.debug("Changing title from '"+ page.getName() + "' to '" + name + "'");
		page.setName(name);
	}
	
	static Pattern title = Pattern.compile("s:5:\"title\";" + 
			"s:\\d+:\"(.*?)\";" + 
			"s:11:\"description\";");
	public String getMetaTitle(Page page) {
		if (page == null) return page.getName();
		if (page.getFile() == null) return page.getName();
		String metaFilename = getMetaFilename(page.getFile().getPath(), ".meta");
		String title = getMetaTitle(metaFilename);
		if (title == null) return page.getName();
		return title;
	}

	public static String getMetaTitle(String metaFilename) {
		if (metaFilename == null) return null;
		String contents = null;
		try {
			contents = FileUtils.readTextFile(new File(metaFilename));
		} catch (IOException e) {
			log.debug("Problem reading meta file: " + metaFilename);
			return null;
		}
		Matcher titleFinder = title.matcher(contents);
		if (titleFinder.find()) {
			return titleFinder.group(1);
		}
		return null;
	}

	public static String fixTitle(String name) {
		if (name.endsWith(".txt")) {
			name = name.substring(0, name.length()-4);
		}
		//replace slashes
		name = name.replaceAll("[\\\\\\/]", " ");
		// Replace underscores with spaces
		name = name.replaceAll("_", " ");

		// Casify the name
		name = casify(name);
		return name;
	}

	static Pattern word = Pattern.compile("(?<= |^)([a-z])");
	public static String casify(String name) {
		Matcher wordFinder = word.matcher(name);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (wordFinder.find()) {
			found = true;
			String first = wordFinder.group(1);
			String replacement = first.toUpperCase();
			wordFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			wordFinder.appendTail(sb);
			return sb.toString();
		}
		return name;
	}

}
