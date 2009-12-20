package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint Table syntax to Confluence table syntax
 */
public class TableConverter extends SharepointConverter {

	
	public void convert(Page page) {
		log.info("Converting Table Syntax");
		String input = page.getOriginalText();
		String converted = convertTables(input);
		page.setConvertedText(converted);
	}

	/**
	 * converts table syntax
	 * @param input
	 * @return
	 */
	protected String convertTables(String input) {
		wasHeader = false;
		Element root = getRootElement(input, false);
		Element changed = transformTables(root);
		String xml = changed.asXML();
		xml = removeWSBeforeNL(xml);
		return xml;
	}
	boolean wasHeader = false;

	/**
	 * transforms table syntax on the root element 
	 * and it's children.
	 * Note: recursive method
	 * @param root
	 * @return
	 */
	protected Element transformTables(Element root) {
		String name = root.getName();
		if (name.equals("table") || name.equals("tbody")) {
			List content = root.content();
			root = updateParentContentList(root, content);
		}
		if (name.equals("tr")) {
			String replacement = "\n|";
			if (wasHeader) replacement = "|" + replacement;
			Text newtext = new DefaultText(replacement);
			root = insertTextBefore(root, newtext);
			wasHeader = false;
		}
		if (name.equals("td")) {
			Text pretext = new DefaultText(" ");
			Text posttext = new DefaultText(" |");
			root = cleanUpText(root);
			root = insertTextBoth(root, pretext, posttext);
			wasHeader = false;
		}
		if (name.equals("th")) {
			Text pretext = new DefaultText("| ");
			Text posttext = new DefaultText(" |");
			root = cleanUpText(root);
			root = insertTextBoth(root, pretext, posttext);
			wasHeader = true;
		}
		//look for children
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transformTables(nodeEl);
			}
		}
		return root;
	}
	
	/**
	 * removes root element from it's parent element,
	 * replacing it with it's content list, and inserts the
	 * given newtext node before that list.
	 * @param root
	 * @param newtext
	 * @return
	 */
	private Element insertTextBefore(Element root, Text newtext) {
		Element parent = root.getParent();
		List content = root.content();
		if (parent == null) parent = new DefaultElement("tmp");
		int index = parent.content().indexOf(root);
		parent.content().remove(root);
		parent.content().add(index, newtext);
		parent.content().addAll(index+1, root.content());
		return parent;
	}

	/**
	 * removes the root node from it's parent node, and replaces
	 * it with it's content list, and inserts the given newtext
	 * node after the content
	 * @param root
	 * @param newtext
	 * @return
	 */
	private Element insertTextAfter(Element root, Text newtext) {
		Element parent = root.getParent();
		List content = root.content();
		if (parent == null) parent = new DefaultElement("tmp");
		int index = parent.content().indexOf(root);
		parent.content().remove(root);
		root.content().add(newtext);
		parent.content().addAll(index, root.content());
		return parent;
	}
	
	/**
	 * removes the given root from it's parent, replaces it with it's 
	 * content list, and inserts the given pretext node before the
	 * content and the given posttext node after the content
	 * @param root
	 * @param pretext
	 * @param posttext
	 * @return
	 */
	private Element insertTextBoth(Element root, Text pretext, Text posttext) {
		Element parent = root.getParent();
		List content = root.content();
		if (parent == null) parent = new DefaultElement("tmp");
		int index = parent.content().indexOf(root);
		parent.content().remove(root);
		root.content().add(posttext);
		parent.content().add(index, pretext);
		parent.content().addAll(index+1, root.content());
		return parent;
	}

	String postRowWS = "(?<=\\|)( *)(?=\n)";
	Pattern postRowWSPattern = Pattern.compile(postRowWS);
	/**
	 * removes spaces before newlines
	 * @param input
	 * @return
	 */
	protected String removeWSBeforeNL(String input) {
		Matcher postRowWSFinder = postRowWSPattern.matcher(input);
		if (postRowWSFinder.find()) {
			return postRowWSFinder.replaceAll("");
		}
		return input;
	}

	/**
	 * for each text node, removes starting and ending newlines, replaces internal 
	 * newlines with double backslashes (confluence syntax for newline inside of a
	 * table cell), and deletes some unexpected chars.
	 * Note: Sharepoint inserts some characters into the input that appear to have
	 * no use other than to clutter up the output. This is probably due to 
	 * an encoding/decoding error in their web service. We delete these chars, _but_
	 * it's possible that it might delete desired characters from non English 
	 * encodings
	 * @param root
	 * @return
	 */
	protected Element cleanUpText(Element root) {
		combineTextNodes(root);
		for (int i = 0; i < root.content().size(); i++) {
			Node node = (Node) root.content().get(i);
			if (node instanceof Text) {
				Text textnode = (Text) node;
				String text = textnode.getText();
				text = text.replaceAll("^\n+", ""); //remove initial NL
				text = text.replaceAll("\n+$", ""); //remove ending NL
				text = text.replaceAll("\n", "\\\\\\\\"); //replace NL with \\
				//XXX This may not work with non-US encodings.
				//Problem: SP creates special ws chars with it's editor's table features
				//which it exports as some sort of unicode(?) char that is difficult to detect
				//this worked, but might also clear out desired chars when used with encodings for 
				//other languages.
				//For examples:
				//See TableConverterTest.testConvertTables4 and testConvertTables5.
				//Also see SampleSharepoint-Input6.txt
//				text = text.replaceAll("[^\\p{Graph}\\s]", ""); 
				textnode.setText(text);
			}
		}
		return root;
	}
}
