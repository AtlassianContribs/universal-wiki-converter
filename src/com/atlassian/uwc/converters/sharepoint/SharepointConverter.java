package com.atlassian.uwc.converters.sharepoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;
import org.w3c.tidy.Tidy;

import com.atlassian.uwc.converters.BaseConverter;

/**
 * Converter that contains all the SharepointConverter parsing code
 */
public abstract class SharepointConverter extends BaseConverter {

	private static final String DEFAULT_DOCTYPE = "strict";
	private static final String DEFAULT_USERAGENT = "Universal Wiki Converter";
	Logger log = Logger.getLogger(this.getClass());
	boolean running = true;
	/**
	 * parses the given html/xml string with dom4j and
	 * returns the root element
	 * @param input
	 * @return dom4j root element of the given html string
	 * or null if the given input could not be parsed.
	 */
	protected Element getRootElement(String input) {
		return getRootElement(input, true);
	}
	/**
	 * parses the given html/xml string with dom4j and
	 * returns the root element. Adds html surround tags
	 * if addHtmlTags is true.
	 * @param input given html/xml string
	 * @param addHtmlTags Adds html surround tags
	 * if addHtmlTags is true.
	 * @return Element representing given input, and containing additional
	 * surrounding html tags if addHtmlTags is true
	 */
	protected Element getRootElement(String input, boolean addHtmlTags) {
		if (addHtmlTags) 
			input = cleanWithJTidy(input); 
		Document document = null;
		System.setProperty( "http.agent", getUserAgent());
		try {
			document = DocumentHelper.parseText(input);
		} catch (DocumentException e) {
			log.error("Problem parsing with dom4j. File may have invalid html.");
			log.error(e.getMessage());
			return null;
		}
		Element rootElement = document.getRootElement();
		return rootElement;
	}
	private String getUserAgent() {
		Properties props = this.getProperties();
		if (!props.containsKey("user-agent"))
			return DEFAULT_USERAGENT;
		return props.getProperty("user-agent", DEFAULT_USERAGENT);
	}
	
	/**
	 * runs the given html input through JTidy, cleaning up bad html, and emitting xhtml
	 * @param input html
	 * @return
	 */
	protected String cleanWithJTidy(String input) {
		log.info("Cleaning Sharepoint HTML with JTidy: Starting. (This may take a while...)");
		Tidy tidy = new Tidy();
		tidy.setTidyMark(false);
		tidy.setDropEmptyParas(true);
		tidy.setXmlOut(true);
		tidy.setDropFontTags(false);
		tidy.setDocType(getDoctype());
		log.debug("xml space = " + tidy.getXmlSpace());
		InputStream in = null;
		String encoding = "utf-8";
		try {
			in = new ByteArrayInputStream(input.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			log.error("Could not use encoding: " + encoding);
			e.printStackTrace();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tidy.parseDOM(in, out);
		log.info("Cleaning Sharepoint HTML with JTidy: Completed.");
		return out.toString();
	}
	private String getDoctype() {
		Properties props = getProperties();
		if (!props.containsKey("doctype"))
			return DEFAULT_DOCTYPE;
		return props.getProperty("doctype", DEFAULT_DOCTYPE);
	}
	
	/**
	 * transforms elements in the given element tree 
	 * with the given search tag into text elements with the given
	 * replace delimiters
	 * <br/>
	 * Example:<br/>
	 * If the root element represents html that looks like: &lt;html&gt;&lt;p&gt;something &lt;b&gt;else&lt;/b&gt;&lt;/p&gt;&lt;/html&gt;<br/>
	 * and the search param is b,<br/>
	 * and the replace param is *,<br/>
	 * Then the return element will represent html that looks like:
	 * &lt;html&gt;&lt;p&gt;something *else*&lt;/p&gt;&lt;/html&gt;
	 * @param root given element tree to transform
	 * @param search tags with this name will be transformed from elements to text
	 * @param replace transformed tags will be given this as a surrounding delimiter
	 * @return transformed element tree (will be the same object)
	 */
	protected Element simpleTransform(Element root, String search, String replace) {
		return transform(root, search, replace, true, true);
	}
	
	/**
	 * transforms the root element by search and replacing tags with
	 * the given parameters
	 * @param root root to be transformed
	 * @param search tag to be replaced Ex: "b" would be a bold tag
	 * @param replace string used for replacement
	 * @param replaceBothSides if true, the replacement will be added to both
	 * sides of the existing content. if false, the replacement will simple replace the
	 * content once as a text object
	 * @param all if true, the method will search for all instances of the search tag. If false,
	 * it will only look for the next instance of the search tag
	 * @return
	 */
	protected Element transform(
			Element root, String search, String replace, 
			boolean replaceBothSides, boolean all) {
		running = true;
		if (root.getName().equals(search)) {
			//create a content list that represents the transformation
			if (replaceBothSides) surroundWithReplace(replace, root);
			else transformContentSimpleReplace(replace, root.content());
			//set new content
			Element parent = setNewParentContent(root, root.content());
			if (!all) running = false;
			return parent;
		}
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transform(nodeEl, search, replace, replaceBothSides, all);
			}
			if (!running) break;
		}
		return root;
	}
	
	/**
	 * transforms single replacement situations.
	 * @param root element to be searched and replaced
	 * @param search tag name to search for. Example: html, p, br
	 * @param replace List content used to replace tag when found
	 * @param all if true, replace all instances of the search tag. 
	 * If false, replace only the first
	 * @return
	 */
	protected Element transform(
			Element root, String search, List replace, 
			boolean all) {
		running = true;
		if (root.getName().equals(search)) {
			//set new content
			Element parent = setNewParentContent(root, replace);
			if (!all) running = false;
			return parent;
		}
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transform(nodeEl, search, replace, all);
			}
			if (!running) break;
		}
		return root;
	}
	
	/**
	 * removes root from its parent and replaces it with transformation list 
	 * @param root
	 * @param transformation
	 * @return
	 */
	protected Element setNewParentContent(Element root, List transformation) {
		Element parent = root.getParent();
		if (parent == null) {
			log.debug("parent is null");
			return root;
		}
		List parentContent = parent.content();
		int index = parentContent.indexOf(root);
		parentContent.remove(index);
		for (Object node : transformation) {
			parentContent.add(index++, node);
		}
		return parent;
	}
	
	/**
	 * adds the replace String to the beginning and end of the given el's contentList
	 * as part of or new Text objects
	 * @param replace
	 * @param el
	 */
	protected void surroundWithReplace(String replace, Element el) {
		List contentList = el.content();
		if (contentList == null || contentList.isEmpty()) {
			if (el.getName().equals("p")) //maintain empty paras = \n, but otherwise delete
				transformClosedContent(replace, contentList);
			return;
		}
		Object first = contentList.get(0);
		Object last = contentList.get(contentList.size()-1);
		if (first instanceof Text) {
			//add replace to beginning of trans.get(0)
			Text firstText = (Text) first;
			String firstContent = firstText.asXML();
			if (firstContent.startsWith("\n") || firstContent.startsWith("\r")) {
				firstContent = firstContent.replaceFirst("[\r\n]+", "");
			}
			firstContent = replace + firstContent;
			firstText.setText(firstContent);
		}
		else {
			//create new first Text object, and insert it
			Text firstText = new DefaultText(replace);
			contentList.add(0, firstText);
		}
		if (last instanceof Text) {
			//add replace to end of last element
			Text lastText = (Text) last;
			String lastContent = lastText.asXML();
			if (lastContent.endsWith("\n") || lastContent.endsWith("\r")) {
				lastContent = lastContent.replaceFirst("[\r\n]+$", "");
			}
			lastContent = lastContent + replace;
			lastText.setText(lastContent);
		}
		else {
			//create new last Text object
			Text lastText = new DefaultText(replace);
			contentList.add(lastText);
		}
	}
	
	
	/**
	 * transforms the given element tree into a string
	 * @param tree
	 * @return html/xml string representing the tree
	 */
	protected String toString(Element tree) {
		return tree.asXML();
	}
	
	/**
	 * @param content
	 * @return string version of the content in readable form
	 */
	protected String toString(List content) {
		String s = "";
		for (Object object : content) {
			if (object instanceof Node)
				s += ((Node) object).asXML();
		}
		return s;
	}

	/**
	 * replaces the given root content with the replace value 
	 * and returns a list of content representing that transformation
	 * @param replace
	 * @param contentList
	 * @return
	 */
	protected List transformClosedContent(String replace, List contentList) {
		if (contentList == null || contentList.isEmpty()) {
			transformContentSimpleReplace(replace, contentList);
		}
		return contentList; 
	}
	
	/**
	 * clears the contentList, and adds a new text object containing the replace string
	 * @param replace
	 * @param contentList
	 */
	protected void transformContentSimpleReplace(String replace, List contentList) {
		contentList.clear();
		Text onlyText = new DefaultText(replace);
		contentList.add(onlyText);
	}
	
	String wsOnly = "((\\s+)|(<br.?>)+)";
	Pattern wsPattern = Pattern.compile(wsOnly);
	/**
	 * removes confluence syntax that only surrounds whitespace
	 * @param input 
	 * @param syntax Confluence syntax delimiter. Should be of the sort that surrounds the content. 
	 * Example, for bold: *
	 * @return input with no references to formatted whitespace
	 */
	protected String removeWhitespaceOnlyConversions(String input, String syntax) {
		String syntaxPairs = "\\Q" + syntax + "\\E" +
				"(.*?)" +
				"\\Q" + syntax + "\\E";
		Pattern syntaxPattern = Pattern.compile(syntaxPairs);
		Matcher syntaxFinder = syntaxPattern.matcher(input);

		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (syntaxFinder.find()) {
			String contents = syntaxFinder.group(1);
			Matcher wsFinder = wsPattern.matcher(contents);
			if (wsFinder.matches()) {
				found = true;
				syntaxFinder.appendReplacement(sb, contents);
			}
		}
		if (found) {
			syntaxFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	String unclosedLink = "<a([^>]+href=\"#\"[^>]*[^\\/])>";
	Pattern unclosedLinkPattern = Pattern.compile(unclosedLink);
	/**
	 * adds closing forward slash to anchor links that don't have them.
	 * Example: input = &lt;a href="#" name="something"&gt;
	 * output = &lt;a href="#" name="something"/&gt;
	 * @param input
	 * @return
	 */
	protected String cleanUnclosedAnchorLinks(String input) {
		Matcher unclosedLinkFinder = unclosedLinkPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (unclosedLinkFinder.find()) {
			String attributes = unclosedLinkFinder.group(1);
			if (attributes.contains(" name=\"")) {
				found = true;
				String replacement = "<a" + attributes + "/>";
				unclosedLinkFinder.appendReplacement(sb, replacement);
			}
		}
		if (found) {
			unclosedLinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	/**
	 * removes root from parent and replaces it with contentlist
	 * @param root
	 * @param contentList
	 * @return
	 */
	protected Element updateParentContentList(Element root, List contentList) {
		Element parent = root.getParent();
		if (parent == null) {
			parent = new DefaultElement("tmp");
			parent.setContent(contentList);
		}
		else {
			int index = parent.indexOf(root);
			parent.remove(root);
			parent.content().addAll(index, contentList);
		}
		return parent;
	}
	/**
	 * combines all the text nodes in the root's content list.
	 * Example: root's content contains two text nodes: one contains the word "abc"
	 * and the other containing the word "def". The content of the root element
	 * would be replaced with one text node containing the word "abcdef".
	 * @param root
	 */
	protected void combineTextNodes(Element root) {
		List content = root.content();
		combineTextNodes(content);
	}
	/**
	 * combines all the text nodes in the given content list
	 * such that back to back text nodes are combined into one text node.
	 * @param content
	 */
	protected void combineTextNodes(List content) {
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			Node sibling = null;
			if (i > 0) {
				sibling = (Node) content.get(i - 1);
				if (node instanceof Text && sibling instanceof Text) {
					String nodetext = node.getText();
					String sibtext = sibling.getText();
					nodetext = sibtext + nodetext;
					node.setText(nodetext);
					node.getParent().remove(sibling);
					i--;
				}
			}
		}
	}
	/**
	 * replaces root with Text node containing replacement text. 
	 * sets root equal to parent node and returns parent
	 * @param root
	 * @param replacement
	 * @return
	 */
	protected Element replaceRootInParent(Element root, String replacement) {
		Text replaceNode = new DefaultText(replacement);
		Element parent = root.getParent();
		int index = 0;
		if (parent != null) {
			index = parent.content().indexOf(root);
			parent.remove(root);
		}
		if (parent == null) parent = new DefaultElement("tmp");
		parent.content().add(index, replaceNode);
		root = parent;
		return root;
	}

	Pattern dollars = Pattern.compile("\\$");
	/**
	 * escapes dollar signs in input (important for regex search and 
	 * replacements that use the Matcher.appendX methods)
	 * @param input
	 * @return
	 */
	protected String handleDollarSigns(String input) {
		Matcher dollarFinder = dollars.matcher(input);
		if (dollarFinder.find()) { //dollar signs could spell disaster!
			input = dollarFinder.replaceAll("\\\\\\$"); // \\\\ = bs, \\$ = dollar
		}
		return input;
	}
	
	Pattern backslashes = Pattern.compile("\\\\");
	/**
	 * escapes backslashes in input (important for regex search and
	 * replacements that use the Matcher.appendX methods)
	 * @param input
	 * @return
	 */
	protected String handleBackslashes(String input) {
		Matcher bsFinder = backslashes.matcher(input);
		if (bsFinder.find()) {
			input = bsFinder.replaceAll("\\\\\\\\");
		}
		return input;
	}
	
}
