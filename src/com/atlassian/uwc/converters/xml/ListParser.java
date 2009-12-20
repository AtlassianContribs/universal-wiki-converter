package com.atlassian.uwc.converters.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * Used to transform html style lists to confluence style lists.
 * Should be used with ol, ul, and li tags.
 * @see <a href="http://confluence.atlassian.com/display/CONFEXT/UWC+Xml+Framework">UWC Xml Framework Documentation</a>
 */
public class ListParser extends DefaultXmlParser {
	
	private static final String TOKEN = "UWCSTARTLIST";
	/**
	 * Confluence delimiter for unordered lists
	 */
	private static final String UNORDERED_DELIM = "*";
	/**
	 * Confluence delimiter for ordered lists
	 */
	private static final String ORDERED_DELIM = "#";

	/**
	 * type of list tag (ol, ul, li, etc)
	 */
	public enum Type {
		/**
		 * corresponds with ordered lists
		 */
		ORDERED,
		/**
		 * corresponds with unordered lists
		 */
		UNORDERED,
		/**
		 * corresponds with list items
		 */
		ITEM;
		/**
		 * @param qName
		 * @return determines what type is associated with the given qName.
		 */
		static Type getType(String qName) {
			if ("ol".equals(qName)) return ORDERED;
			if ("ul".equals(qName)) return UNORDERED;
			if ("li".equals(qName)) return ITEM;
			return null;
		}
	}
	
	/**
	 * current ongoing list delimiter - For example: #**
	 */
	private static String delim = "";
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		switch (type) {
		case ORDERED:
			delim += ORDERED_DELIM;
			appendOutput(delim + TOKEN);
			break;
		case UNORDERED:
			delim += UNORDERED_DELIM;
			appendOutput(delim + TOKEN);
			break;
		case ITEM:
			appendOutput("\n" + delim + " ");
			break;
		}
	}

	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		switch (type) {
		case ORDERED:
		case UNORDERED:
			Pattern token = Pattern.compile("^(?s)(.*?)\\Q" + delim + "\\E" +
					TOKEN +
					"(.*)$");
			delim = delim.substring(0, delim.length()-1);
			String listout = getOutput();
			Matcher listFinder = token.matcher(listout);
			boolean found = false;
			//We have to fix the newlines and whitespace for lists so that they maintain
			//proper list context. So, we're going to grab all the current output
			//find out list token, fix the list whitespace, and re-output the current output
			//with the fixed list.
			while (listFinder.find()) { //find the token for this element
				found = true;
				String pre = listFinder.group(1); //save existing pre-list content
				listout = listFinder.group(2);
				listout = fixNL(listout); //check for whitespace problems
				this.clearOutput(); //delete all saved content
				appendOutput(pre + listout); //rewrite saved content with fixed list 
				break;
			}
			if (!found) log.error("Problem ending list.");
		}
	}
	
	/**
	 * transforms a Confluence list, so that we have the right number of newlines
	 * @param input
	 * @return
	 */
	private String fixNL(String input) {
		//remove opening newline
		input = input.replaceAll("([\n][ \t]*){2,}", "\n"); //remove extra newlines
		input = input.replaceAll("^[ \t]+", ""); 	//get rid of non-newline ws at beginning
		input = input.replaceFirst("^\n+", "");		//get rid of opening newlines
		input = input.replaceAll("(?<=[*#] )\n", "");
		input = input.replaceAll("(?<=\n|^)[*#] +\n", ""); //get rid of empty items
		if (!getOutput().endsWith("\n") && !input.startsWith("\n")) input = "\n" + input;
		return input;
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		String content = String.copyValueOf(ch, start, length);
		if (content != null) appendOutput(content);
	}
}
