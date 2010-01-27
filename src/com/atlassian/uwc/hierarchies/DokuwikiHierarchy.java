package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.dokuwiki.HierarchyTitleConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class DokuwikiHierarchy extends FilepathHierarchy {

	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		//run the filepath hierarchy first -
		HierarchyNode node = super.buildHierarchy(pages);
		//move spacekeys
		node = handleSpacekeyBranch(node);
		//handle start pages
		node = handleStartPages(node, true);
		String spacekey = getProperties().getProperty("spacekey", "");
		Vector<String> candidates = getCollisionsCandidates(spacekey);
		//fix collisions
		node = fixCollisions(node, candidates);
		//fix titles
		node = fixTitles(node);
		//attach images
		String attdirRaw = getProperties().getProperty("attachmentdirectory", "");
		Vector<File> attdirs = getAttDirs(attdirRaw);
		if (attdirs.isEmpty()) return node;
		node = attachAllImages(node, attdirs, true);
		return node;
	}

	private HierarchyNode handleSpacekeyBranch(HierarchyNode node) {
		String spacekey = getProperties().getProperty("spacekey");
		if (spacekey != null && !"".equals(spacekey)) { 
			Set<HierarchyNode> top = node.getChildren();
			for (Iterator iter = top.iterator(); iter.hasNext();) {
				HierarchyNode topnode = (HierarchyNode) iter.next();
				if (topnode.getName().toLowerCase().equals(spacekey.toLowerCase())) {
					//topnode children should be top level
					Set<HierarchyNode> children = topnode.getChildren();
					iter.remove(); //Only allowed way to remove from an iterator. 
					topnode.setParent(null); //since we have to use iter.remove instead of node.removeChild(topnode)
					for (HierarchyNode child : children) {
						node.addChild(child);
					}
					break;
				}
			}
		}
		return node;
	}

	private HierarchyNode handleStartPages(HierarchyNode node, boolean top) {
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			String name = child.getName();
			if (name.toLowerCase().equals("start") && !top) {
				Page page = child.getPage();
				if (page == null) continue; //mid level start directories
				iter.remove(); //only allowed way to remove from an iterator
				child.setParent(null); //since we have to use iter.remove instead of node.removeChild(child)
				if (node.getPage() == null) node.setPage(page);
				else log.warn("parent already had page object");
				log.debug("Moving start page to parent. Changing start page name: " + node.getName());
				node.getPage().setName(node.getName());
			}
			child = handleStartPages(child, false);
		}
		return node;
	}

	private HierarchyNode fixCollisions(HierarchyNode node, Vector<String> collisions) {
		if (collisions.isEmpty()) return node;
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			for (String name : collisions) {
				String eqname = equalize(name);
				String childname = equalize(child.getName());
				if (childname.equals(eqname)) {
					log.debug("Fixing collisions? " + eqname + " vs. " + childname);
					String parent = child.getParent().getName();
					log.debug("parent = " + parent);
					if (parent == null) continue;
					String newname = parent + " " + child.getName();
					log.debug("newname = " +newname);
					child.setName(newname);
					if (child.getPage() != null) child.getPage().setName(newname);
				}
			}
			child = fixCollisions(child, collisions);
		}
		return node;
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

	private HierarchyNode attachAllImages(HierarchyNode node, Vector<File> attdirs, boolean top) {
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			if (child.getPage() == null) child.setPage(createPage(child.getName()));
			for (File attdir : attdirs) {
				if (top && equalize(child.getName()).equals(equalize(attdir.getName()))) {
					doAttach(child, attdir);
					Vector<File> thisfile = new Vector<File>();
					thisfile.add(attdir);
					attachAllImages(child, thisfile, false);
				}
				File[] files = attdir.listFiles(getSvnFilter());
				for (File file : files) {
					if (top && file.isFile() && child.getName().equals("Start")) {
						log.debug("Attaching: " + file.getName() + " to " + child.getName());
						child.getPage().addAttachment(file);
					}
					if (file.isFile()) continue;
					String filename = equalize(file.getName());
					String childname = equalize(child.getName());
					Vector<File> thisfile = new Vector<File>();
					thisfile.add(file);
					if (filename.equals(childname)) {
						doAttach(child, file);
						attachAllImages(child, thisfile, false);
					}
					else if (childname.equals(attdir.getName()))
						attachAllImages(child, thisfile, false);
				}
			}
		}
		return node;
	}

	private void doAttach(HierarchyNode child, File file) {
		File[] attachments = file.listFiles(getSvnFilter());
		for (File att : attachments) {
			if (att.isFile()) {
				log.debug("Attaching: " + att.getName() + " to " + child.getName());
				child.getPage().addAttachment(att);
			}
		}
	}

	private String equalize(String name) {
		name = name.toLowerCase();
		name = name.replaceAll("_", " ");
		return name;
	}

	private Page createPage(String name) {
		 Page page = new Page(null);
         page.setName(name);
         page.setOriginalText("");
         page.setConvertedText("");
         page.setPath(name); 
         return page;
	}

	NoSvnFilter svnfilter = new NoSvnFilter();
	private NoSvnFilter getSvnFilter() {
		if (this.svnfilter == null)
			this.svnfilter = new NoSvnFilter();
		return this.svnfilter;
	}

	private Vector<File> getAttDirs(String attdirRaw) {
		String[] dirs = attdirRaw.split("::");
		Vector<File> atts = new Vector<File>();
		for (String dir : dirs) {
			dir = dir.trim();
			if ("".equals(dir)) continue;
			File file = new File(dir);
			if (!file.exists() || !file.isDirectory())
				log.error("Attachment Directory does not exist or is not a directory: " + dir);
			else
				atts.add(file);
		}
		return atts;
	}

	HierarchyTitleConverter titleConverter = new HierarchyTitleConverter();
	//nodes without pages probably will need this
	private HierarchyNode fixTitles(HierarchyNode node) { 
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			if (child.getPage() == null)
				child.setName(titleConverter.fixTitle(child.getName()));
			else {
				titleConverter.convert(child.getPage());
				child.setName(child.getPage().getName());
			}
			child = fixTitles(child);
		}
		return node;
	}
}
