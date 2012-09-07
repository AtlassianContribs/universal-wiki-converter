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
		node = handleSpacekeyBranchWithProp(node);
		//handle start pages
		node = handleHomePages(node, true);
		//handle spacekeys not with a property, but with page.getSpacekey setting
		node = handleSpacekeyBranchWithPage(node);
		
		//fix collisions
		node = fixCollisions(node);
		//fix titles
		node = fixTitles(node);
		//attach images
		String attdirRaw = getProperties().getProperty("attachmentdirectory", "");
		Vector<File> attdirs = getAttDirs(attdirRaw);
		if (attdirs.isEmpty()) return node;
		node = attachAllImages(node, attdirs, true);
		return node;
	}

	private HierarchyNode handleSpacekeyBranchWithProp(HierarchyNode node) {
		String spacekey = getProperties().getProperty("spacekey");
		if (spacekey != null && !"".equals(spacekey)) { 
			Set<HierarchyNode> top = node.getChildren();
			for (Iterator iter = top.iterator(); iter.hasNext();) {
				HierarchyNode topnode = (HierarchyNode) iter.next();
				if (topnode.getName().toLowerCase().equals(spacekey.toLowerCase())) {
					//topnode children should be top level
					setTopNodeBranch(node, iter, topnode);
					break;
				}
			}
		}
		return node;
	}

	private HierarchyNode handleSpacekeyBranchWithPage(HierarchyNode root) {
		Set<HierarchyNode> top = root.getChildren();
		Vector<HierarchyNode> ordered = new Vector<HierarchyNode>(top);
		for (HierarchyNode topnode : ordered) {
			root = handleSpacekeyBranchTop(root, topnode);
		}
		return root;
	}

	private HierarchyNode handleSpacekeyBranchTop(HierarchyNode root, HierarchyNode node) {
		if (node.getPage() == null) {
			//FIXME Do we want to do anything here? or just go straight to children 
		}
		else if (node.getPage().getSpacekey() != null) {
			String spacekey = node.getPage().getSpacekey();
			node = setAncestorsBySpacekey(root, node, spacekey);
			if (node.getParent() == null) {
				Set<HierarchyNode> top = root.getChildren();
				Vector<HierarchyNode> ordered = new Vector<HierarchyNode>(top);
				for (HierarchyNode topnode : ordered) {
					if (spacekey.equals(topnode.getPage().getSpacekey())) {
						Set<HierarchyNode> children = topnode.getChildren();
						topnode.setParent(null); //since we have to use iter.remove instead of node.removeChild(topnode)
						for (HierarchyNode child : children) {
							root.addChild(child);
						}
						break;
					}
				}
			}
		}
		Set<HierarchyNode> nextSet = node.getChildren();
		Vector<HierarchyNode> ordered = new Vector<HierarchyNode>(nextSet);
		for (HierarchyNode next : ordered) {
			root = handleSpacekeyBranchTop(root, next);
		}
		return root;
	}

	private HierarchyNode setAncestorsBySpacekey(HierarchyNode root, HierarchyNode node,
			String spacekey) {
		HierarchyNode parent = node.getParent();
		if (parent.getName() == null) { //we're at the root level return
			return node;
		}
		if (parent.getPage() == null) {
			Page page = createPage(parent.getName());
			page.setSpacekey(spacekey);
			parent.setPage(page);
			parent = setAncestorsBySpacekey(root, parent, spacekey);
			if (parent.getParent() == null)
				root.addChild(parent);
		}
		else if (parent.getPage().getSpacekey() == null) {
			parent.getPage().setSpacekey(spacekey);
			parent = setAncestorsBySpacekey(root, parent, spacekey);
		}
		else if (!parent.getPage().getSpacekey().equals(spacekey)) {
			parent.removeChild(node);
		}
		return node;
	}

	private void setTopNodeBranch(HierarchyNode root, Iterator topiter, HierarchyNode nexttopnode) {
		Set<HierarchyNode> children = nexttopnode.getChildren();
		topiter.remove(); //Only allowed way to remove from an iterator. 
		nexttopnode.setParent(null); //since we have to use iter.remove instead of node.removeChild(topnode)
		for (HierarchyNode child : children) {
			root.addChild(child);
		}
	}

	private HierarchyNode handleHomePages(HierarchyNode node, boolean top) {
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			String name = child.getName();
			String dokuwikiFilename = getDokuwikiHomepageFilename();
			if (name.toLowerCase().equals(dokuwikiFilename) && !top) {
				Page page = child.getPage();
				if (page == null) continue; //mid level start directories
				iter.remove(); //only allowed way to remove from an iterator
				child.setParent(null); //since we have to use iter.remove instead of node.removeChild(child)
				if (node.getPage() == null) node.setPage(page);
				else log.warn("parent already had page object");
				log.debug("Moving start page to parent. Changing start page name: " + node.getName());
				node.getPage().setName(node.getName());
			}
			child = handleHomePages(child, false);
		}
		return node;
	}

	public String getDokuwikiHomepageFilename() {
		return getProperties().getProperty("hierarchy-homepage-dokuwiki-filename", "");
	}

	private HierarchyNode fixCollisions(HierarchyNode node) {
		
		Set<HierarchyNode> children = node.getChildren();
		for (Iterator iter = children.iterator(); iter.hasNext();) { 
			HierarchyNode child = (HierarchyNode) iter.next();
			
			Vector<String> collisions = getCollisionsForThisNode(child);
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
			child = fixCollisions(child);
		}
		return node;
	}

	public Vector<String> getCollisionsForThisNode(HierarchyNode node) {
		log.debug("node.getPage: " + node.getPage());
		if (node.getPage() != null) log.debug("node.getPage.getSpacekey: " + node.getPage().getSpacekey());
		String spacekey = (node.getPage() != null && node.getPage().getSpacekey() != null)?
				node.getPage().getSpacekey() :
				getProperties().getProperty("spacekey", "");
		return getCollisionsCandidates(spacekey);
	}

	protected Vector<String> getCollisionsCandidates(String spacekey) {
		Properties props = getProperties();
		Vector<String> candidates = new Vector<String>();
		log.debug("Looking for collisions candidates for spacekey: " + spacekey);
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next();
			if (key.toLowerCase().startsWith("collision-titles-"+spacekey.toLowerCase())) {
				String namesraw = props.getProperty(key, "");
				log.debug("Found collisions data: " + namesraw);
				if ("".equals(namesraw)) continue;
				String[] names = namesraw.split(",");
				for (String name : names) {
					name = name.trim();
					candidates.add(name);
				}
			}
		}
		log.debug("candidates size? " + candidates.size());
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
	
	
	Page currentpage;
	HierarchyNode currentParent;
	boolean combineHomepageNodes = false;
	@Override 
	protected void buildRelationships(Page page, HierarchyNode root) {
		currentpage = page;
		combineHomepageNodes = false;
		currentParent = null;
		//DELETE
		if (page.getFile().getPath().endsWith("forumdescription.txt")) {
			int food = 0;
		}
		super.buildRelationships(page, root);
		if (combineHomepageNodes) {
			combineHomepages(page);
		}
	}

	public void combineHomepages(Page page) {
		Set<HierarchyNode> children = currentParent.getChildren();
		Vector<HierarchyNode> all = new Vector<HierarchyNode>(children);
		HierarchyNode first = null, second = null;
		for (HierarchyNode node : all) {
			if (node.getName().equalsIgnoreCase(page.getName())) {
				if (first == null) first = node;
				else second = node;
			}
		}
		if (first.getPage() == null) {
			combineHomepages(first, second, page);
		}
		else { 
			combineHomepages(second, first, page);
		}
	}
	
	private void combineHomepages(HierarchyNode nullPageNode, HierarchyNode noChildrenNode,
			Page page) {
		//this one represents the one with all the hierarchy data
		nullPageNode.setPage(page); 
		//this one represents the one that (used) to have page data. We don't need it anymore
		noChildrenNode.getParent().removeChild(noChildrenNode); 
	}

	@Override
	protected boolean hasExistingRelationship(HierarchyNode parent, String childname) {
		boolean hasRel = super.hasExistingRelationship(parent, childname);
		if (hasRel && "".equals(getProperties().getProperty("hierarchy-homepage-dokuwiki-filename"))) {
			if (parent.getPage() == null && currentpage.getName().equalsIgnoreCase(childname)) {
				combineHomepageNodes = true;
				currentParent = parent;
				return false;
			}
			return hasRel;
		}
		return hasRel;
			
	}
}
