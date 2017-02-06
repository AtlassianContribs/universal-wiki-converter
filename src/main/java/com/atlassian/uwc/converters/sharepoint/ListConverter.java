package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint list syntax to Confluence list syntax. 
 * Works closely with ColorConverter and HeaderConverter to handle 
 * edge cases.
 */
public class ListConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting List Syntax");
		String input = page.getOriginalText();
		String converted = convertLists(input);
		page.setConvertedText(converted);
	}

	/**
	 * converts sharepoint list syntax to Confluence list syntax
	 * @param input
	 * @return
	 */
	protected String convertLists(String input) {
		Element root = getRootElement(input, false);
		Element changed = transformLists(root, null, "");
		String xml = toString(changed);
		xml = CleanConverter.cleanJTidyExtras(xml);
		xml = removeUnnecessaryNewlines(xml);
		xml = removeExtraHeaders(xml);
		return xml;
	}


	boolean first = true;
	boolean last = false;
	/**
	 * transforms list elements contained by the 
	 * given root element to confluence list syntax text objects.
	 * Note: Recursive method
	 * @param root root element potentially containing a list item
	 * @param type ORDERED or UNORDERED if this is a sublevel list 
	 * representing the parent list, 
	 * or null if this is the first level of list items.
	 * Example: If we have a list that should eventually look like:
	 * <br/>* a
	 * <br/>*# b
	 * <br/>
	 * When we are processing the "a" node, we would pass null.
	 * When we are processing the "b" node, we would pass UNORDERED
	 * @param current current list string which will be appended to with the next
	 * level of list, or empty ("") if this is the first level of list.
	 * Example: If we have a list that should eventually look like:
	 * <br/>* a
	 * <br/>*# b
	 * <br/>
	 * When we are processing the "a" node, we would pass "".
	 * When we are processing the "b" node, we would pass "*"
	 * @return
	 */
	protected Element transformLists(Element root, ListType type, String current) {
		String name = root.getName();
		ListType thistype = null;
		if (Pattern.matches("[ou]l", name)) {
			thistype = ListType.getListType(name);
			String listitems = transformListItems(root.content(), thistype, current);
			listitems = requireNewlines(listitems); //we'll end up with extra ws sometimes, but the extra newlines are ignored by the renderer so I think it's ok.
			Element parent = root.getParent();
			if (parent == null) parent = new DefaultElement("tmp");
			int index = parent.indexOf(root);
			parent.remove(root);
			Text newtext = new DefaultText(listitems);
			parent.content().add(index, newtext);
			root = parent;
		}
		if ("li".equals(name)) {
			current = getListDelim(type, current);
		}
		//look for children
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				if (thistype != null) type = thistype;
				nodeEl = transformLists(nodeEl, type, current);
			}
		}
		return root;
	}


	/**
	 * transforms the contents of a OL or UL tag to Confluence list syntax.
	 * @param listContents contents of a OL or UL html tag
	 * @param type ListType reflecting this list's list type. Example: ORDERED
	 * @param current string reflecting parent list's list delimiter or "" if
	 * this list has no parent list. Example: *#*
	 * @return
	 */
	protected String transformListItems(List listContents, ListType type, String current) {
		String transformed = "";
		for (int i = 0; i < listContents.size(); i++) {
			Node node = (Node) listContents.get(i);
			if (node instanceof Element) {
				if ("li".equals(node.getName())) {
					String text = transformListItem((Element) node, type, current);
					transformed += text;
				}
				else {
					String delim = getListDelim(type, current);
					String text = transformListToString((Element) node, type, delim);
					transformed += text;
				}
			}
			else if (node instanceof Text) {
				String text = node.asXML();
				text = text.trim();
				transformed += text;
			}
		}
		return transformed;
	}
	

	/**
	 * transforms list containing element that is not an li tag. 
	 * Example situation:<br/>
	 * &lt;ol&gt;<br/>
	 * &lt;li&gt;a&lt;/li&gt;<br/>
	 * <b>&lt;ol&gt;&lt;li&gt;b&lt;/li&gt;&lt;/ol&gt;<br/></b>
	 * &lt;/ol&gt;<br/>
	 * @param element element contained by an OL or UL that isn't LI. 
	 * Probably another OL or UL.
	 * @param type this list's type. Example: UNORDERED
	 * @param current this list's list delimiter. Example: *# 
	 * @return
	 */
	protected String transformListToString(Element element, ListType type, String current) {
		String transformed = "";
		String name = element.getName();
		if (Pattern.matches("[ou]l", name)) {
			ListType thistype = ListType.getListType(name);
			List content = element.content();
			transformed += transformListItems(content, thistype, current);
		}
		return transformed;
	}

	/**
	 * transforms a list item (LI tag).
	 * @param root LI element
	 * @param type this list's list type. Example: ORDERED
	 * @param current this list's parent's list delimiter, or "" if this
	 * list has no parent. Example: #*
	 * @return
	 */
	protected String transformListItem(Element root, ListType type, String current) {
		String delim = getListDelim(type, current);
		List contentList = root.content();
		String replacement = "";
		String textOnly = "";
		for (int i = 0; i < contentList.size(); i++) {
			Node node = (Node) contentList.get(i);
			if (node instanceof Text) {
				Text nodeText = (Text) node;
				String text = nodeText.getText();
				text = text.replaceAll("^\n+", "");
				text = text.replaceAll("\n+$", "");
				textOnly += text;
			}
			else if (node instanceof Element) {
				Element nodeEl = (Element) node;
				if (nodeEl.getName().equals("span")) {
					setNewParentContent(nodeEl, nodeEl.content());
					i--;
					continue;
				}
				if (!"".equals(textOnly)) {
					replacement += addListItemNL(delim, textOnly);
					textOnly = "";
				}
				nodeEl = transformLists(nodeEl, type, delim);
				Text content = (Text) nodeEl.content().get(0);
				String text = content.asXML();
				replacement += text;
			}
		}
		if (!"".equals(textOnly)) {
			replacement += addListItemNL(delim, textOnly);
			textOnly = "";
		}
		return replacement;
	}

	/**
	 * adds newlines to list item text as appropriate
	 * @param delim
	 * @param input
	 * @return
	 */
	private String addListItemNL(String delim, String input) {
		if (input.startsWith(delim)) return "\n" + input;
		return "\n" + delim + " " + input;
	}
	
	/**
	 * creates the list item delimiter for the given list type, 
	 * with the given current list delimiter (representing
	 * this list's parent's list delimiter)
	 * @param type this list's type: ORDERED or UNORDERED
	 * @param current this list's parent's list delimiter. Example: #*
	 * @return Example: If type is ORDERED and current is *, then the 
	 * return value would be *#
	 */
	protected String getListDelim(ListType type, String current) {
		String delim = current;
		switch(type) {
		case ORDERED:
			delim += "#";
			break;
		case UNORDERED:
			delim += "*";
			break;
		}
		return delim;
	}


	/**
	 * inserts newlines as necessary
	 * @param input
	 * @return
	 */
	protected String requireNewlines(String input) {
		input = requireBeginNewline(input);
		input = requireFinalNewlines(input);
		return input;
	}

	Pattern finalNewlines = Pattern.compile("(\n{1,2})$");
	/**
	 * inserts list ending newlines as necessary
	 * @param input
	 * @return
	 */
	private String requireFinalNewlines(String input) {
		Matcher finalNLFinder = finalNewlines.matcher(input);
		if (finalNLFinder.find()) {
			String end = finalNLFinder.group(1);
			if (end.equals("\n")) input += "\n";
		}
		else input += "\n\n";
		return input;
	}

	/**
	 * inserts list beginning newlines as necessary
	 * @param input
	 * @return
	 */
	private String requireBeginNewline(String input) {
		if (!input.startsWith("\n")) input = "\n" + input;
		return input;
	}

	String listNewlines = 
			"(?<=^|\n)" +		//zero-width newline or beginning of string
			"([*#]+ [^\n]*)" +	//list item
			"(\n+)" + 			//one or more newlines
			"(?=[*#]+ )";
	Pattern listNewlinesPattern = Pattern.compile(listNewlines);
	/**
	 * removes extra newlines between list items.
	 * Example: If the input is<br/>
	 * * a<br/>
	 * * b<br/>
	 * <br/>
	 * * c<br/>
	 * Then the return value will be<br/>
	 * * a<br/>
	 * * b<br/>
	 * * c<br/>
	 * 
	 * @param input
	 * @return 
	 */
	protected String removeUnnecessaryNewlines(String input) {
		Matcher listNewlinesFinder = listNewlinesPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (listNewlinesFinder.find()) {
			found = true;
			String listitem = listNewlinesFinder.group(1);
			listitem = handleBackslashes(listitem);
			listitem = handleDollarSigns(listitem);
			String replacement = listitem + "\n";
			listNewlinesFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			listNewlinesFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern extraHeaders = Pattern.compile("" +
			"(?<=^|\n)" +
			"([#*]+\\s*)" +		//group 1 
			"(h\\d\\.\\s?)" +	//group 2 
			"([^\n]+)" +		//group 3 
			"h\\d\\.\\s?" +
			"([^\n]+)"			//group 4 
			);
	/**
	 * removes extra header syntax from within a list item.
	 * Example: input = * h1. header h2. still in same item!<br/>
	 * output = * h1. header still in same item!
	 * @param input
	 * @return
	 */
	protected String removeExtraHeaders(String input) {
		Matcher extraHeadersFinder = extraHeaders.matcher(input);
		String replacement = "{group1}" +
				"{group2}" +
				"{group3}" +
				" " +
				"{group4}";
		return RegexUtil.loopRegex(extraHeadersFinder, input, replacement);
	}

	/**
	 * types of lists: ordered and unordered
	 */
	public enum ListType {
		/**
		 * ordered list type
		 */
		ORDERED,
		/**
		 * unordered list type 
		 */
		UNORDERED;
		/**
		 * uses html tag name to determine type of list
		 * @param s "ol" or "ul", case sensitive
		 * @return ORDERED or UNORDERED
		 */
		public static ListType getListType(String s) {
			if ("ol".equals(s)) return ORDERED;
			if ("ul".equals(s)) return UNORDERED;
			return null;
		}
	}
}
