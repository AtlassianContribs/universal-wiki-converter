package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class SpaceConverter extends HierarchyTarget {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String path = page.getFile().getPath();
		if (page.getParent() != null && page.getParent().getSpacekey() != null) {
			page.setSpacekey(page.getParent().getSpacekey());
			return;
		}
		String ancestors = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", "");
		if (path.startsWith(ancestors)) path = path.replaceFirst("\\Q"+ancestors+"\\E", "");
		log.debug("Path after removing ancestors = " + path);
		HashMap<String, String> dirs = getDokuDirectories();//FIXME do we want to fix HierarchyTarget so it doesn't do this more than once
		String tmppath = path;
		while (!"".equals(tmppath)) {
			if (dirs.containsKey(tmppath)) {
				log.debug("tmppath = " + tmppath);
				String spacekey = dirs.get(tmppath);
				if (matchesSpaceNameRule(tmppath)) {
					String spaceName = getSpaceName(spacekey);
					log.debug("matched. spacename = " +spaceName);
					page.setSpace(spacekey, spaceName, "");
				}
				else page.setSpacekey(spacekey);
				break;
			}
			if (!tmppath.contains(File.separator)) 
				break; //break here if we can't find a spacekey for this dir
			//remove deepest portion of the path
			tmppath = removeDeepest(tmppath);
		}
		log.debug("spacekey set to: " + page.getSpacekey());
	}
	
	static Pattern filetype = Pattern.compile("[.]\\w+$");
	public static String removeDeepest(String tmppath) {
		//if thire's a filetype, remove that
		if (filetype.matcher(tmppath).find())
			return tmppath.replaceFirst("[.]\\w+$", ""); 
		//otherwise remove up to the last directory
		return tmppath.replaceFirst("\\"+File.separator+"[^\\"+File.separator+"]*$", "");
	}
	private boolean matchesSpaceNameRule(String path) {
		String regex = getProperties().getProperty("spacename-rule-regex", null);
		if (regex == null) return false;
		String[] relatedPaths = getRelatedPaths(path);
		Pattern p = Pattern.compile(regex);
		for (String relpath : relatedPaths) {
			log.debug("relpath = " + relpath);
			Matcher m = p.matcher(relpath);
			if (m.find()) return true;
		}
		return false;
	}
	private String getSpaceName(String spacekey) {
		String prefix = getProperties().getProperty("spacename-rule-prefix", null);
		boolean casify = Boolean.parseBoolean(getProperties().getProperty("spacename-rule-uppercase", "false"));
		if (prefix == null) {
			if (casify) return HierarchyTitleConverter.casify(spacekey);
			return spacekey;
		}
		if (casify) return HierarchyTitleConverter.casify(prefix + spacekey);
		return prefix + spacekey;
	}

}
