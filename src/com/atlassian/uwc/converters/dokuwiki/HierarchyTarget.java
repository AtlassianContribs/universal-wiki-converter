package com.atlassian.uwc.converters.dokuwiki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public abstract class HierarchyTarget extends BaseConverter {
	Pattern space = Pattern.compile("space-([^-]*)");
	protected Vector<String> getSpaces() {
		Properties props = getProperties();
		Vector<String> spaces = new Vector<String>();
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next(); 
			Matcher spaceFinder = space.matcher(key);
			if (spaceFinder.find()) {
				spaces.add(spaceFinder.group(1)); //confluence space
			}
		}
		return spaces;
	}
	
	protected HashMap<String,String> getDokuDirectories() {
		Properties props = getProperties();
		HashMap<String,String> directories = new HashMap<String,String>();
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next(); 
			Matcher spaceFinder = space.matcher(key);
			if (spaceFinder.find()) {
				String confspace = spaceFinder.group(1);
				String dokuDirsRaw = (String) props.get(key); //dokuwiki directories/namespaces
				String[] dokuDirs = dokuDirsRaw.split(",");
				for (String dir : dokuDirs) 
					directories.put(dir,confspace);
			}
		}
		return directories;
	}
	protected String fixCollisions(String target, String hierarchy, String linkSpacekey) {
		Vector<String> collisionsCandidates = getCollisionsCandidates(linkSpacekey);
		target = HierarchyTitleConverter.casify(target);
		if (collisionsCandidates.contains(target)) {
			String parentsRaw = hierarchy.replaceFirst("\\Q" + target + "\\E.*$", "");
			String[] parents = parentsRaw.split(":");
			boolean again = false;
			for (int i = parents.length-1;i>=0;i--) {
				String parent = parents[i];
				if (parent.toLowerCase().equals(target.toLowerCase())) continue;
				if ("".equals(parent)) continue;
				parent = HierarchyTitleConverter.fixTitle(parent);
				//how many parents do we need? if the parent is a collision, we need its parent
				if (collisionsCandidates.contains(parent)) again = true;
				else again = false;
				//add the parent to the link
				target = parent + " " + target;
				if (!again) break;
			}
		}
		return target;
	}
	protected Vector<String> getCollisionsCandidates(String spacekey) {
		Properties props = getProperties();
		Vector<String> candidates = new Vector<String>();
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next();
			if (key.toLowerCase().startsWith("collision-titles-"+spacekey.toLowerCase())) {
				String namesraw = props.getProperty(key, "");
				if ("".equals(namesraw)) continue;
				String[] names = namesraw.split(",");
				for (String name : names) {
					name = name.trim();
					candidates.add(name);
				}
			}
		}
		return candidates;
	}

	protected String getCurrentPath(Page page) {
		String ignorable = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", "");
		String full = page.getPath();
		if (full == null) return null;
		return full.replaceAll("\\Q"+ignorable + "\\E", "");
	}

}
