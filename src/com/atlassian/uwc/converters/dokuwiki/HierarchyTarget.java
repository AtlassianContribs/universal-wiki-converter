package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public abstract class HierarchyTarget extends BaseConverter {
	Pattern space = Pattern.compile("space-([^-]*)");
	protected Pattern metaFile = Pattern.compile("([\\\\/])([^\\\\/]+)(\\.meta)$");
	
	Logger log = Logger.getLogger(this.getClass());
	
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
		return fixCollisions(target, hierarchy, linkSpacekey, null);
	}
	protected String fixCollisions(String target, String hierarchy, String linkSpacekey, String targetMetaFilename) {
		Vector<String> collisionsCandidates = getCollisionsCandidates(linkSpacekey);
		target = HierarchyTitleConverter.casify(target);
		if (collisionsCandidates.contains(target)) {
			String parentsRaw = hierarchy.replaceFirst("\\Q" + target + "\\E.*$", "");
			String[] parents = parentsRaw.split(":");
			if (parents.length < 2) return target;
			boolean again = false;
			for (int i = parents.length-2;i>=0;i--) {
				String parent = parents[i];
				log.debug("HT: parent = '" + parent + "', targetMetaFilename:'" + targetMetaFilename + "'");
				if (parent.toLowerCase().equals(target.toLowerCase())) continue;
				if ("".equals(parent)) continue;
				if (targetMetaFilename != null) {
					Matcher metaFinder = metaFile.matcher(targetMetaFilename);
					if (metaFinder.find()) {
						String parentMetaFilename = metaFinder.replaceFirst(".meta");
						log.debug("HT: parentMetaFilename: '" +  parentMetaFilename + "'");
						String tmpparent = HierarchyTitleConverter.getMetaTitle(parentMetaFilename);
						log.debug("HT: tmpparent: '" +  tmpparent + "'");
						if (tmpparent != null && !"".equals(tmpparent)) parent = tmpparent;
					}
				}
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

	protected String getMetaFilename(String path, String filetype) {
		String metadir = getProperties().getProperty("meta-dir", null);
		if (metadir == null) {
			return null;
		}
		String ignorable = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", null);
		if (ignorable == null) {
			return null;
		}
		String relative = path.replaceFirst("\\Q" + ignorable + "\\E", "");
		relative = relative.replaceFirst("\\.txt$", filetype);
		if (relative.startsWith(File.separator) && metadir.endsWith(File.separator))
			relative = relative.substring(1);
		if (!relative.startsWith(File.separator) && !metadir.endsWith(File.separator))
			relative = File.separator + relative;
		return metadir + relative;
	}
	
}
