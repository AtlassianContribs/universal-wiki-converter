package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class SpaceConverter extends HierarchyTarget {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String path = page.getFile().getPath();
		String ancestors = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", "");
		if (path.startsWith(ancestors)) path = path.replaceFirst("\\Q"+ancestors+"\\E", "");
		log.debug("Path after removing ancestors = " + path);
		HashMap<String, String> dirs = getDokuDirectories();//FIXME do we want to fix HierarchyTarget so it doesn't do this more than once
		String tmppath = path;
		while (!"".equals(tmppath)) {
			if (dirs.containsKey(tmppath)) {
				String spacekey = dirs.get(tmppath);
				page.setSpacekey(spacekey);
				break;
			}
			if (!tmppath.contains(File.separator)) 
				break; //break here if we can't find a spacekey for this dir
			//remove deepest portion of the path
			tmppath = tmppath.replaceFirst("\\"+File.separator+"[^\\"+File.separator+"]*$", "");
		}
		
	}

}
