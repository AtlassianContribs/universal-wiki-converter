package com.atlassian.uwc.hierarchies;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.dokuwiki.HierarchyTitleConverter;
import com.atlassian.uwc.filters.NoSvnFilter;
import com.atlassian.uwc.ui.Page;

public class DokuwikiHierarchy extends FilepathHierarchy {

	int newpagescount = 0;
	
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
//		getRootNode().setChildrenComparator(dokuComparator());
		getRootNode().childrenAsList(true);
		log.debug("Number of hierarchy pages: " + pages.size());
		//run the filepath hierarchy first -
		HierarchyNode node = super.buildHierarchy(pages);
//		log.debug("filepath hierarchy tree:");
//		printTree(node);
		//move spacekeys
//		node = handleSpacekeyBranchWithProp(node);
		//handle spacekeys not with a property, but with page.getSpacekey setting
		node = handleSpacekeyBranchWithPage(node);
//		log.debug("branching by spacekey:");
//		printTree(node);
		//handle start pages
		node = handleHomePages(node, true);
//		log.debug("merging home pages:");
//		printTree(node);
		
		//fix collisions
		node = fixCollisions(node);
//		log.debug("fix collisions:");
//		printTree(node);
		//fix broken branches
		node = mergeBrokenNodes(node);
//		log.debug("merge broken nodes:");
//		printTree(node);
		//fix titles
		node = fixTitles(node);
//		log.debug("fix titles:");
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
			Collection<HierarchyNode> top = node.getChildren();
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
		Collection<HierarchyNode> top = root.getChildren();
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
		}
		Collection<HierarchyNode> nextSet = node.getChildren();
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
			HierarchyNode branch = hasSpaceBranchAlready(parent, spacekey);
			if (branch != null) {
				log.debug("Moving node ('"+node.getName()+"') to existing branch ('"+branch.getName()+"') because of spacekey: " + spacekey);
				moveBranch(node, parent, branch);
			}
			else {
				log.debug("Copying branch to new parent because of spacekey: " + node.getName());
				copyBranch(node, spacekey, parent);
			}
//			log.debug("Printing the whole tree after copy/move");
//			printTree(root);
		}
		return node;
	}


	private HierarchyNode hasSpaceBranchAlready(HierarchyNode parent, String spacekey) {
		HierarchyNode current = parent;
		int level = 0;
		while (current.getPage() != null) {
			current = current.getParent();
			for (HierarchyNode child : current.getChildren()) {
				String childspace = (child.getPage() == null)?"":child.getPage().getSpacekey();
				if (childspace.equalsIgnoreCase(spacekey)) {
					HierarchyNode depthNode = child;
					while (level-- > 0) {
						if (depthNode.getChildren().size()>0) {
							Vector<HierarchyNode> onenode = new Vector<HierarchyNode>(depthNode.getChildren());
							depthNode = onenode.firstElement();
						}
					}
					return depthNode;
				}
			}
			level++;
		}
		return null;
	}


	private void moveBranch(HierarchyNode node, HierarchyNode parent, HierarchyNode branch) {
		log.debug("node: " + node.getName() + " parent: " + parent.getName() + " branch: " + branch.getName());
		parent.removeChild(node);
		branch.addChild(node);
	}


	public void copyBranch(HierarchyNode node, String spacekey,
			HierarchyNode parent) {
		log.debug("node: " + node.getName() + " parent: " + parent.getName());
		HierarchyNode newparent = new HierarchyNode();
		newparent.setName(parent.getName());
		Page newparentpage = createPage(parent.getName());
		newparentpage.setSpacekey(spacekey);
		if (parent.getPage().getOriginalText() != null) 
			newparentpage.setOriginalText(parent.getPage().getOriginalText());
		if (parent.getPage().getConvertedText() != null) 
			newparentpage.setConvertedText(parent.getPage().getConvertedText());
		newparent.setPage(newparentpage);
		HierarchyNode gparent = parent.getParent();
		if (!parent.removeChild(node)) {
//			log.debug("tree for parent: " + parent.getName());
//			printTree(parent);
		}
		newparent.addChild(node);
		gparent.addChild(newparent);
		if (gparent.getName() == null) return;
		copyBranch(newparent, spacekey, gparent);
	}

	private void setTopNodeBranch(HierarchyNode root, Iterator topiter, HierarchyNode nexttopnode) {
		Collection<HierarchyNode> children = nexttopnode.getChildren();
		topiter.remove(); //Only allowed way to remove from an iterator. 
		nexttopnode.setParent(null); //since we have to use iter.remove instead of node.removeChild(topnode)
		for (HierarchyNode child : children) {
			root.addChild(child);
		}
	}

	private HierarchyNode handleHomePages(HierarchyNode node, boolean top) {
		Collection<HierarchyNode> children = node.getChildren();
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
		
		Collection<HierarchyNode> children = node.getChildren();
		Vector<HierarchyNode> childrenV = new Vector<HierarchyNode>(children);
		for (int i = 0; i < childrenV.size(); i++) {
			HierarchyNode child = (HierarchyNode) childrenV.get(i);
			
			Vector<String> collisions = getCollisionsForThisNode(child);
			for (int j = 0; j < collisions.size(); j++) {
				String name = collisions.get(j);
				String eqname = equalize(name);
				String childname = equalize(child.getName());
				if (childname.equals(eqname)) {
					log.debug("Examining collisions candidate: '" + eqname + "' for this child: '" + childname + "'");
					String parent = getParentTitle(child);
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
	
	private static int numericCollisionCounter = 2;
	private HierarchyNode mergeBrokenNodes(HierarchyNode node) {
		
		Collection<HierarchyNode> children = node.getChildren();
		Vector<HierarchyNode> childrenV = new Vector<HierarchyNode>(children);
		for (int i = 0; i < childrenV.size(); i++) {
			HierarchyNode child = (HierarchyNode) childrenV.get(i);
			log.debug("Merging child: '"  + child.getName()+"'");
			HierarchyNode parent = child.getParent();
			if (child.getPage() != null && child.getPage().getFile() != null) { 
				Collection<HierarchyNode> siblingsSet = parent.getChildren();
				Vector<HierarchyNode> siblings = new Vector<HierarchyNode>();
				for (HierarchyNode s : siblingsSet) {
					if (!s.equals(child)) {
						siblings.add(s);
					}
				}
//				log.debug("sibling size: " + siblings.size());
				for (int j = 0; j < siblings.size(); j++) {
					HierarchyNode sibling = siblings.get(j);
//					log.debug("all siblings: " + sibling.getName());
					if (sibling.getPage() == null || sibling.getPage().getFile() == null) {
						if (compareNodes(child, sibling)) {
							mergeTwoNodes(child, sibling, parent);
						}
					}
					else if (child.getName().equalsIgnoreCase(sibling.getName())) {
						String newname = sibling.getName() + " " + numericCollisionCounter++;
						log.debug("Found conflicting node. Changing name to: '"+ newname + "'");
						sibling.setName(newname);
						sibling.getPage().setName(newname);
					}
				}
			}
//			log.debug("***v");
			child = mergeBrokenNodes(child);
		}

		return node;
	}


	protected void mergeTwoNodes(HierarchyNode first, HierarchyNode second,
			HierarchyNode parent) {
		log.debug("Merging second node (sibling): '" + second.getName()+"'");
		for (HierarchyNode gchild : second.getChildren()) {
//			log.debug("adding child: " + gchild.getName());
			first.addChild(gchild);
		}
		if (parent != null) {
			if (!parent.removeChild(second)) {
//				log.debug("parent tree");
//				printTree(parent);
			}
		}
//		log.debug("child tree");
//		printTreeOneLevel(first);
	}


	/**
	 * Uses the filepath of the first node to compare names with the second node.
	 * Note, these nodes should already be at the same depth and have the same parent before
	 * calling this method.
	 * @param first should have a page with a file object
	 * @param second must have node name, but doesn't need page 
	 * @return if they are the same node
	 */
	protected boolean compareNodes(HierarchyNode first, HierarchyNode second) {
		String filename = first.getPage().getFile().getName();
		filename = filename.replaceFirst("\\.txt$", "");
		boolean samenode = equalize(second.getName()).equals(equalize(filename));
		if (samenode) {
			log.debug("first node name: " + first.getName() + " and full file path: " + first.getPage().getFile().getAbsolutePath());
			log.debug("filename of first node: " + filename + " and equalized: " + equalize(filename));
			log.debug("second node name: " + second.getName() + " and equalized: " + equalize(second.getName()));
			log.debug("first spacekey " + first.getPage().getSpacekey() + " ... second spacekey " + ((second.getPage()!=null)?second.getPage().getSpacekey():"null"));
		}
		return samenode;
	}

	protected String getParentTitle(HierarchyNode child) {
		String basename = child.getParent().getName(); 
		//unchanged source is set in the engine. 
		//if it's null, we created the parent in the hierarchy, so we need to review the title
		if (child.getParent().getPage() != null && child.getParent().getPage().getUnchangedSource() != null) 
			return child.getParent().getPage().getName();
		log.debug("Attempting to identify meta name for parent title.");
		HierarchyNode gparent = child.getParent().getParent();
		if (gparent == null) return basename;
		Collection<HierarchyNode> siblingsSet = gparent.getChildren();
		Vector<HierarchyNode> siblings = new Vector<HierarchyNode>(siblingsSet);
		for (int i = 0; i < siblings.size(); i++) {
			HierarchyNode sibling = (HierarchyNode) siblings.get(i);
			if (sibling.getPage() != null && sibling.getPage().getFile() != null) {
//				log.debug("Examining sibling: "+ sibling.getName());
				String filename = sibling.getPage().getFile().getName();
				filename = filename.replaceFirst("\\.txt$", "");
				if (equalize(basename).equals(equalize(filename))) {
					log.debug("returning sibling name: " + sibling.getName());
					return sibling.getName();
				}
			}
		}
		return basename;
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
//		log.debug("Looking for collisions candidates for spacekey: " + spacekey);
		for (Iterator iter = props.keySet().iterator();iter.hasNext();) {
			String key = (String) iter.next();
			if (key.toLowerCase().startsWith("collision-titles-"+spacekey.toLowerCase())) {
				String namesraw = props.getProperty(key, "");
//				log.debug("Found collisions data: " + namesraw);
				if ("".equals(namesraw)) continue;
				String[] names = namesraw.split(",");
				for (String name : names) {
					name = name.trim();
					candidates.add(name);
				}
			}
		}
//		log.debug("candidates size? " + candidates.size());
		return candidates;
	}

	private HierarchyNode attachAllImages(HierarchyNode node, Vector<File> attdirs, boolean top) {
		Collection<HierarchyNode> children = node.getChildren();
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
		log.debug("Creating page in hierarchy for: " + name);
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
		Collection<HierarchyNode> children = node.getChildren();
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
		log.debug("Building Relationship for page: " + page.getName() + " -> " + page.getFile().getName());
		currentpage = page;
		combineHomepageNodes = false;
		currentParent = null;

		super.buildRelationships(page, root);
		if (combineHomepageNodes) {
			combineHomepages();
		}
		log.debug("++count: "+(count++) +", completed building relationship for page: " + page.getName() + " -> " + page.getFile().getName());
	}

	public void combineHomepages() {
		Collection<HierarchyNode> children = currentParent.getChildren();
		Vector<HierarchyNode> all = new Vector<HierarchyNode>(children);
		HierarchyNode first = null, second = null;
		for (int i = 0; i < all.size()-1 && all.size() > 1; i++) {
			first = all.get(i);
			second = all.get(i+1); 
			if (first == null || second == null) continue;
			log.debug ("first = " + first.getName() + " second = " + second.getName());
			if ((first.getPage() == null || first.getPage().getFile() == null)) {
				if (second.getPage() != null && second.getPage().getFile() != null) {
					HierarchyNode tmp = first;
					first = second;
					second = tmp;
					log.debug ("first = " + first.getName() + " second = " + second.getName());
				}
				else continue;
			}
			if (compareNodes(first, second)) {
				
				if (first.getPage() == null) {
					combineHomepages(first, second, second.getPage());
				}
				else { 
					combineHomepages(second, first, first.getPage());
				}
			}
		}
	}
	
	private void combineHomepages(HierarchyNode nullPageNode, HierarchyNode noChildrenNode,
			Page page) {
		if (noChildrenNode.getPage() == null) return;//indicates this isn't the right scenario to combine
		if (noChildrenNode.getChildren().size() > 0) {
			mergeTwoNodes(noChildrenNode, nullPageNode, noChildrenNode.getParent());
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
				log.debug("-> (dokuwikihierarchy check) false");
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

	private void printTree(Collection<HierarchyNode> children, String delim) {
		for (HierarchyNode child : children) {
			String newdelim = delim + " .";
			String childdata = child.getName();
//			if (child.getPage() != null && child.getPage().getFile() != null)
//				childdata += " -> " + child.getPage().getFile().getAbsolutePath();
			log.debug("PRINTTREE: " + newdelim + childdata);
			printTree(child.getChildren(), newdelim);
		}
	}
	
	private void printTreeOneLevel(HierarchyNode node) {
		Collection<HierarchyNode> children = node.getChildren();
		for (HierarchyNode child : children) {
			log.debug("PRINTTREE: " + child.getName());
		}
	}
}
