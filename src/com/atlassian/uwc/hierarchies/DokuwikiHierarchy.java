package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sun.tools.tree.AddExpression;

import com.atlassian.uwc.converters.dokuwiki.HierarchyTitleConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class DokuwikiHierarchy extends FilepathHierarchy {

	int newpagescount = 0;
	
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		log.debug("Number of hierarchy pages: " + pages.size());
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
		//attach images - old attachment handling. Idea is to put the attachments in the 
		//new confluence page corresponding with the media directory for that namespace
		//but this doesn't work if the media dir doesn't correspond to an existing pages namespace
//		String attdirRaw = getProperties().getProperty("attachmentdirectory", "");
//		Vector<File> attdirs = getAttDirs(attdirRaw);
//		if (attdirs.isEmpty()) return node;
//		node = attachAllImages(node, attdirs, true);
		
		getProperties().setProperty("newpagescount", newpagescount+"");
		
		if (log.isDebugEnabled()) {
			printTree(node);
		}
		
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
					if (topnode.getPage() == null) {
						log.debug("NULL! (Skipping) topnode.getName() = " + topnode.getName());
						continue;
					}
					if (spacekey.equals(topnode.getPage().getSpacekey())) {
						Set<HierarchyNode> children = topnode.getChildren();
						log.debug("Moving topnode: " + topnode.getPage().getName());
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
		log.debug("setAncestorsBySpacekey: node.getName():'" + node.getName() + "' spacekey " + spacekey); 
//		printTree(node);
		HierarchyNode parent = node.getParent();
		if (parent.getName() == null) { //we're at the root level return
			log.debug("...parent name is null");
			return node;
		}
		if (parent.getPage() == null) {
			log.debug("...parent page is null. creating for... " + parent.getName());
			Page page = createPage(parent.getName());
			page.setSpacekey(spacekey);
			parent.setPage(page);
			parent = setAncestorsBySpacekey(root, parent, spacekey);
			if (parent.getParent() == null)
				root.addChild(parent);
		}
		else if (parent.getPage().getSpacekey() == null) {
			log.debug("...parent page spacekey is null. Setting to: " + spacekey);
			parent.getPage().setSpacekey(spacekey);
			parent = setAncestorsBySpacekey(root, parent, spacekey);
		}
		else if (!parent.getPage().getSpacekey().equals(spacekey)) {
			log.debug("...parent.getPage().getSpacekey: " + parent.getPage().getSpacekey() + "... and spacekey: " + spacekey);
			log.debug("Copying branch to new parent because of spacekey: " + node.getName());

			copyBranch(node, spacekey, parent);
			
		}
		return node;
	}


	public void copyBranch(HierarchyNode node, String spacekey,
			HierarchyNode parent) {
		HierarchyNode newparent = new HierarchyNode();
		newparent.setName(parent.getName());
		Page newparentpage = createPage(parent.getName());
		newparentpage.setSpacekey(spacekey);
		if (parent.getPage().getOriginalText() != null) 
			newparentpage.setOriginalText(parent.getPage().getOriginalText());
		if (parent.getPage().getConvertedText() != null) 
			newparentpage.setConvertedText(parent.getPage().getConvertedText());
		newparent.setPage(newparentpage);
		parent.removeChild(node);
		newparent.addChild(node);
		parent.getParent().addChild(newparent);
		if (parent.getParent().getName() == null) return;
		copyBranch(newparent, spacekey, parent.getParent());
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
					log.debug("Examining collisions candidate: '" + eqname + "' for this child: '" + childname + "'");
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
			String name = getOrigChildName(child);
			if (child.getPage() == null) child.setPage(createPage(name));
			for (File attdir : attdirs) {
				log.debug ("Examining attachment directory: " + attdir);
				log.debug(equalize(name)+" ... " + equalize(attdir.getName()));
				if (top && equalize(name).equals(equalize(attdir.getName()))) {
					doAttach(child, attdir);
					Vector<File> thisfile = new Vector<File>();
					thisfile.add(attdir);
					attachAllImages(child, thisfile, false);
				}
				File[] files = attdir.listFiles(getSvnFilter());
				log.debug("files["+files.length+"]");
				for (File file : files) {
					if (top && file.isFile() && name.equals("Start")) {
						log.debug("Attaching: " + file.getName() + " to " + name);
						child.getPage().addAttachment(file);
					}
					if (file.isFile()) 
						continue;
					String filename = equalize(file.getName());
					log.debug("filename: " + filename);
					String childname = equalize(name);
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

	Pattern leafpath = Pattern.compile("[^\\"+File.separator+"]+$");
	public String getOrigChildName(HierarchyNode child) {
		if (child.getPage() != null) {
			String path = child.getPage().getPath();
			Matcher leafFinder = leafpath.matcher(path);
			if (leafFinder.find()) return leafFinder.group();
		}
		return child.getName();
	}
	
	private void doAttach(HierarchyNode child, File file) {
		log.debug("doAttach: " + child.getName() + " ... " + file.getName());
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
         newpagescount++;
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
				String fixed = titleConverter.fixTitle(child.getPage().getName());
				child.setName(fixed);
				child.getPage().setName(fixed);
			}
			child = fixTitles(child);
		}
		return node;
	}
	
	
	Page currentpage;
	HierarchyNode currentParent;
	boolean combineHomepageNodes = false;
	private int count;
	@Override 
	protected void buildRelationships(Page page, HierarchyNode root) {
		currentpage = page;
		combineHomepageNodes = false;
		currentParent = null;

		super.buildRelationships(page, root);
		if (combineHomepageNodes) {
			combineHomepages(page);
		}
		log.debug("++count: "+(count++) +", completed building relationship for page: " + page.getName());
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
		if (noChildrenNode.getPage() == null) return;//indicates this isn't the right scenario to combine
		if (noChildrenNode.getChildren().size() > 0) {
			log.error("Combining Homepages - noChildrenNode has children!: " + noChildrenNode.getName());
			return;
		}
		log.debug("Combining: " + page.getName());
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
	

	private void printTree(HierarchyNode node) {
		log.debug("PRINTTREE: " + node.getName());
		printTree(node.getChildren(), "");
	}

	private void printTree(Set<HierarchyNode> children, String delim) {
		for (HierarchyNode child : children) {
			String newdelim = delim + " ";
			log.debug("PRINTTREE: " + newdelim + child.getName());
			printTree(child.getChildren(), newdelim);
		}
	}
}
