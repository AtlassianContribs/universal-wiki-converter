package com.atlassian.uwc.hierarchies;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import com.atlassian.uwc.ui.Page;

public class SmfHierarchy extends MetaHierarchy {

	private static final String PROPKEY_COMMENTS = "reply-comments";
	private static final String PROPKEY_HIERARCHYCOMPARATOR = "hierarchy-child-comparator";
	private static final String TITLE_DELIM = " - ";
	private static final String DEFAULT_DATEFORMAT = "yyyy-MM-dd hmma";
	private static final String DEFAULT_EMPTY_TIME = "1";
	private static final String DEFAULT_EMPTY_NAME = "No Name";
	private static final String DEFAULT_EMPTY_TITLE = "No Title";
	public static final String DEFAULT_ROOTPAGENAME = "Home";
	
	@Override
	public HierarchyNode buildHierarchy(Collection<Page> pages) {
		HierarchyNode root = super.buildHierarchy(pages);
		handleComments(root);
		return root;
	}

	@Override
	protected HierarchyNode buildRelationships(Page page, HierarchyNode root) {
		assignRootPage(root);
		log.debug("page: " + page.getName());
		try {
			Properties meta = getMeta(page);
			String ancestorString = meta.getProperty("ancestors", "");
			if ("null".equals(ancestorString)) ancestorString = null;
			
			HierarchyNode node = root.getChildren().iterator().next(); //first child = Home. root will be ignored by engine.
			if (ancestorString != null && !"".equals(ancestorString)) { //set up ancestors
				String[] ancestors = ancestorString.split(":");
				for (int i = 0; i < ancestors.length; i++) {
					String ancestor = ancestors[i];
					node = buildNode(node, ancestor); 
				}
			}
			node = buildNode(node, createNodeId(meta.getProperty("id"), meta.getProperty("type")), page);
			convertPagename(page, meta);
			node.setName(page.getName());
		} catch (FileNotFoundException e) {
			String error = "Meta file for page does not exist: " + getMetaPath(page);
			log.warn(error);
		} catch (IOException e) {
			String error = "Problem opening meta page: " + getMetaPath(page);
			log.warn(error);
			e.printStackTrace();
		}
		return root;
	}

	protected void init() { //called from MetaHierarchy.buildHierarchy
		super.init();
		clearCollisions();
	}
	
	/**
	 * contains already used titles, so we can detect collisions
	 */
	HashMap<String,String> collisions = null;
	/**
	 * updates the page title with the info from the meta properties.
	 * If the page title is not a valid Confluence title, it removes
	 * any disallowed characters. It also checks for namespace collisions
	 * and updates the page title with the username and timestamp if there
	 * is one. The timestamp is controlled with the converter property "title-date-format".
	 * @param page
	 * @param meta
	 * @return
	 */
	protected String convertPagename(Page page, Properties meta) {
		String origTitle = meta.getProperty("title", DEFAULT_EMPTY_TITLE);
		String title = removeIllegalTitleChars(origTitle);
		if ("".equals(title)) title = DEFAULT_EMPTY_TITLE;
		//namespace collisions
		if (getCollisions().containsKey(title)) {
			String origName = meta.getProperty("username", DEFAULT_EMPTY_NAME);
			String name = removeIllegalTitleChars(origName);
			String type = meta.getProperty("type", "top");
			if (type.equals("top") || type.equals("re")) {
				String time = meta.getProperty("time", DEFAULT_EMPTY_TIME);
				String formattedTime = removeIllegalTitleChars(formatTime(time));
				if (!"".equals(formattedTime)) formattedTime = TITLE_DELIM + formattedTime;
				title += TITLE_DELIM + name + formattedTime;
			}
			//unlikely, but possible
			int index = 2;
			String current = title;
			while (getCollisions().containsKey(title)) {
				title = current + TITLE_DELIM + "No." + index++;
			}
		}
		getCollisions().put(title, "");
		//set the page name
		page.setName(title);
		return page.getName();
	}

	/**
	 * @param time seconds since the epoch for the timestamp we are trying to format
	 * @return title friendly formatted time (using either the converter property title-date-format
	 * or the DEFAULT_DATEFORMAT. Date format should be formatted as described in SimpleDateFormat
	 * @see SimpleDateFormat
	 */
	protected String formatTime(String time) {
		//get format from converter properties
		String format = this.properties.getProperty("title-date-format", DEFAULT_DATEFORMAT);
		
		//format time
		long seconds;
		try {
			seconds = Long.parseLong(time);
		} catch (Exception e) {
			return ""; //can't parse time
		}
		long milli = seconds * 1000;
		Date date = new Date(milli);
		DateFormat dateFormat = null;
		try {
			dateFormat = new SimpleDateFormat(format);
		} catch (IllegalArgumentException e) {
			log.error("Custom date format is not a valid SimpleDateFormat: " + format
					+ ". Using default format instead: " + DEFAULT_DATEFORMAT);
			dateFormat = new SimpleDateFormat(DEFAULT_DATEFORMAT);
		}
		return (dateFormat.format(date));
	}
	

	protected static String removeIllegalTitleChars(String input) {
		//html entities
		input = StringEscapeUtils.unescapeHtml(input);
		//illegal characters
		input = input.replaceAll("^[$~]", ""); //starting at title with ~ or $ is illegal
		input = input.replaceAll("^\\.\\.", "");//starting a title with .. is illegal
		//conf illegal chars
		input = input.replaceAll("[:;{}\\[\\]<>()@/\\\\|^#]", ""); 
		input = input.trim();
		return input;
	}
	
	private HashMap<String,String> getCollisions() {
		if (this.collisions == null) this.collisions = new HashMap<String, String>();
		return this.collisions;
	}
	
	public void clearCollisions() {
		this.collisions = null;
	}
	
	protected HierarchyNode buildNode(HierarchyNode parent, String nodeid) {
		return buildNode(parent, nodeid, null);
	}
	protected HierarchyNode buildNode(HierarchyNode parent, String nodeid, Page page) {
		//does parent have this node already?
		if (!parent.getChildren().isEmpty()) {
			for (Iterator iter = parent.getChildIterator(); iter.hasNext();) {
				HierarchyNode child = (HierarchyNode) iter.next();
				if (same(nodeid, child)) {
					if (child.getPage() == null && page != null) child.setPage(page);
					return child;
				}
			}
		}
		//if not add it here
		HierarchyNode child = new HierarchyNode();
		if (page != null) child.setPage(page);
		child.setName(nodeid);
		parent.addChild(child);
		return child;
	}

	private boolean same(String nodeid, HierarchyNode child) {
		if (child == null) return false;
		if (child.getName().equals(nodeid)) return true;
		if (child.getPage() == null) return false;
		return child.getPage().getFile().getName().endsWith(nodeid+".txt");
	}

	protected String createNodeId(String id, String type) {
		return type+id;
	}

	private void assignRootPage(HierarchyNode root) {
		HierarchyNode home;
		if (root.getChildren().isEmpty()) {
			home = new HierarchyNode();
			home.setChildrenComparator(new SmfTimeComparator());
			home.setName(DEFAULT_ROOTPAGENAME);
			root.addChild(home);
		}
	}

	private void handleComments(HierarchyNode root) {
		if (this.getProperties().containsKey(PROPKEY_COMMENTS) &&
				Boolean.parseBoolean(this.getProperties().getProperty(PROPKEY_COMMENTS))) {
			Vector<HierarchyNode> topicNodes = getTopicNodes(root);
			for (HierarchyNode topic : topicNodes) {
				Set<HierarchyNode> children = topic.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					HierarchyNode reply = (HierarchyNode) iter.next();
					String comment = createComment(reply.getName());
					if (topic.getPage() == null) continue;// could happen if we're converting only one file
					topic.getPage().addComment(comment);
				}
			}
		}
	}
	
	Pattern topicPattern = Pattern.compile("_top\\d+\\.txt$");
	/**
	 * recursive method for getting all the topic nodes from within
	 * a given root. Note that because of child boards, we can't assume
	 * that topics are all on the 3rd level
	 * @param root
	 * @return
	 */
	protected Vector<HierarchyNode> getTopicNodes(HierarchyNode root) {
		Vector<HierarchyNode> topics = new Vector<HierarchyNode>();
		String filename;
		//use the filename to determine the page's level
		if (root.getPage() == null || 
				root.getPage().getFile() == null || 
				root.getPage().getFile().getName() == null) {
			filename = "";
		}
		else filename = root.getPage().getFile().getName();
		//if it's a topic, add it
		if (topicPattern.matcher(filename).find()) 
			topics.add(root);
		else { //check it's children for topic pages
			for (HierarchyNode node : root.getChildren()) {
				topics.addAll(getTopicNodes(node));
			}
		}
		return topics;
	}

	protected String createComment(String name) {
		return "h1. [" + name + "]\n{include:" + name + "}";
	}


	/**
	 * Pattern for identifying object id from filename
	 */
	Pattern id = Pattern.compile("(\\d+)(\\.txt)?$");
	/**
	 * Uses the id of the object to determine it's sort order.
	 * (Smaller ids mean the object was created earlier.)
	 */
	public class SmfTimeComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			HierarchyNode node0 = (HierarchyNode) arg0;
			HierarchyNode node1 = (HierarchyNode) arg1;

			String name0 = node0.getName();
			String name1 = node1.getName();
			if (!id.matcher(name0).find() && node0.getPage() != null) 
				name0 = node0.getPage().getFile().getName();
			if (!id.matcher(name1).find() && node1.getPage() != null) 
				name1 = node1.getPage().getFile().getName();
			
			Matcher idFinder0 = id.matcher(name0);
			Matcher idFinder1 = id.matcher(name1);
 			if (idFinder0.find() && idFinder1.find()) {
 				String idStr0 = idFinder0.group(1);
 				String idStr1 = idFinder1.group(1);
 				
 				int id0 = Integer.parseInt(idStr0);
 				int id1 = Integer.parseInt(idStr1);
 				return id0 - id1;
 			}
				
			return 0;
		}

	}


}
