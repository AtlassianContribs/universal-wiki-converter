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
import com.atlassian.uwc.ui.VersionPage;

public abstract class HierarchyTarget extends BaseConverter {
	Pattern space = Pattern.compile("space-([^-]*)");
	
	Logger log = Logger.getLogger(this.getClass());
	static HashMap<String,String> directories;
	static HashMap<String, String[]> relatedPaths;
	
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
	
	protected String[] getRelatedPaths(String path) {
		if (relatedPaths == null) getDokuDirectories();
		return relatedPaths.get(path);
	}
	protected HashMap<String,String> getDokuDirectories() {
		Properties props = getProperties();
		if (directories != null) return directories;
		directories = new HashMap<String,String>();
		relatedPaths = new HashMap<String, String[]>();
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next(); 
			Matcher spaceFinder = space.matcher(key);
			if (spaceFinder.find()) {
				String confspace = spaceFinder.group(1);
				String dokuDirsRaw = (String) props.get(key); //dokuwiki directories/namespaces
				String[] dokuDirs = dokuDirsRaw.split(",");
				for (String dir : dokuDirs) { 
					directories.put(dir,confspace);
					relatedPaths.put(dir, dokuDirs);
				}
			}
		}
		return directories;
	}
	
	//useful for unit testing, because we keep some static fields
	protected void clear() {
		directories = null;
		relatedPaths = null;
	}
	
	protected String fixCollisions(String target, String hierarchy, String linkSpacekey) {
		return fixCollisions(target, hierarchy, linkSpacekey, null);
	}
	protected Pattern metaFile = Pattern.compile("([\\\\/])([^\\\\/]+)(\\.meta)$");
	protected String fixCollisions(String target, String hierarchy, String linkSpacekey, String targetMetaFilename) {
		Vector<String> collisionsCandidates = getCollisionsCandidates(linkSpacekey);
		target = HierarchyTitleConverter.casify(target);
		if (isCollisionCandidate(target, collisionsCandidates)) {
			String parentsRaw = hierarchy.replaceFirst("\\Q" + target + "\\E.*$", "");
			String[] parents = parentsRaw.split(":");
			if (parents.length < 2) return target;
			boolean again = false;
			String tmpMetaFilename = targetMetaFilename;
			for (int i = parents.length-2;i>=0;i--) {
				String parent = parents[i];
//				log.debug("HT: parent = '" + parent + "', tmpMetaFilename:'" + tmpMetaFilename + "'");
				if (parent.toLowerCase().equals(target.toLowerCase())) continue;
				if ("".equals(parent)) continue;
				if (tmpMetaFilename != null) {
					Matcher metaFinder = metaFile.matcher(tmpMetaFilename);
					if (metaFinder.find()) {
						String parentMetaFilename = metaFinder.replaceFirst(".meta");
//						log.debug("HT: parentMetaFilename: '" +  parentMetaFilename + "'");
						String tmpparent = HierarchyTitleConverter.getMetaTitle(parentMetaFilename);
//						log.debug("HT: tmpparent: '" +  tmpparent + "'");
						if (tmpparent != null && !"".equals(tmpparent)) parent = tmpparent;
						tmpMetaFilename = parentMetaFilename; //in case we have to go again
					}
				}
				parent = HierarchyTitleConverter.fixTitle(parent);
				//how many parents do we need? if the parent is a collision, we need its parent
				if (isCollisionCandidate(parent, collisionsCandidates)) again = true;
				else again = false;
				//add the parent to the link
				target = parent + " " + target;
				if (!again) break;
			}
		}
		return target;
	}

	public boolean isCollisionCandidate(String target,
			Vector<String> collisionsCandidates) {
		for (String candidate : collisionsCandidates) {
			if (candidate.equalsIgnoreCase(target)) return true;
		}
		return false;
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
		if (page instanceof VersionPage) {
			ignorable = getProperties().getProperty("page-history-load-as-ancestors-dir", "");
		}
		String full = page.getPath();
		if (full == null) return null;
		return full.replaceAll("\\Q"+ignorable + "\\E", "");
	}

	public String getMetaFilename(String path, String filetype) {
		String ignorable = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", null);
		if (ignorable == null) {
			return null;
		}
		String metadir = getProperties().getProperty("meta-dir", null);
		if (metadir == null) {
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

	public String getRelativePath(Page page) {
		String path = page.getFile().getPath();
		if (page instanceof VersionPage)
			path = page.getParent().getFile().getPath();
		log.debug("HierarchyTarget: relative path = " + path);
		return path;
	}
		
}
