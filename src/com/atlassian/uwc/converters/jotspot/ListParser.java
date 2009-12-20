package com.atlassian.uwc.converters.jotspot;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX parser that parses HTML lists (could be nested lists)
 * @author Laura Kolker
 */
public class ListParser extends DefaultHandler {
	String output = "";
	public static final String ORDERED = "ol";
	public static final String UNORDERED = "ul";
	public static final String LIST = "li";
	public static final int LISTTAGLENGTH = "<ul>".length();
	int depth = 0;
	int index = 0;
	int maxlength = 0;
	String currentDelim = "";
	boolean isOrdered = false;
	boolean grabItem = false;
	boolean itemContinue = false;
	Logger log = Logger.getLogger(this.getClass());
	public ListParser() {
		super();
	}
	
	public void startElement(String uri, String name,
			      String qName, Attributes atts) {
		//keep track of the depth of the list
		if (ORDERED.equals(qName) || UNORDERED.equals(qName)) {
			depth++;
		}
		
		//note whether the list is ordered or unordered
		if (ORDERED.equals(qName)) isOrdered = true;
		else if (UNORDERED.equals(qName)) isOrdered = false;
		
		//note whether it's time to grab the inner text
		if (LIST.equals(qName)) grabItem = true;
//		else grabItem = false; 
	}
	
	public void characters (char ch[], int start, int length) {
		String chars = new String(ch, start, length);
		
		//grab the inner text
		if (grabItem) {
			String list = chars; 
			//get Confluence list delimiters (* or #)
			String delim = getCurrentDelimiter(getDelimType()); 
			String newitem = "";
			newitem += (!itemContinue)?"\n" + delim + " ":"";
			newitem += list;
			this.output += newitem;
			//important for run-on list items
			itemContinue = true;
		}
    }
	
	/**
	 * creates the delimiter needed for the given depth and list type
	 * @param delim Confluence list delimiter (* or #)
	 * @return depth appropriate Confluence list delimiter
	 * Could be 3 * or 2 #, etc.
	 */
	private String getCurrentDelimiter(String delim) {
		int currentLength = this.currentDelim.length();
		//depth increased, so lengthen current delimiter
		if (currentLength < depth) 
			this.currentDelim += delim;
		
		return this.currentDelim;
	}

	/**
	 * @return Confluence list delimiter, based on isOrdered field:
	 * * or #
	 */
	private String getDelimType() {
		return (isOrdered?"#":"*");
	}

	public void endElement(String uri, String name, String qName) {
		if (ORDERED.equals(qName) || UNORDERED.equals(qName)) {
			depth--;
			//depth decreased, so shorten current delimiter
			//do this here, so that the depth and the delimiter stay in synch
			this.currentDelim = this.currentDelim.substring(0, this.currentDelim.length()- 1);
		
			grabItem = false;
		}
		else if (LIST.equals(qName))
			grabItem = itemContinue = false;
		log.debug("----ending: " + qName);
	}
	
	/**
	 * @return the Confluence list syntax.
	 * Should be called after parsing to get the result.
	 */
	public String getOutput() {
		return this.output;
	}

	/**
	 * clears the Confluence output. Should be called before parsing if parser is reused.
	 */
	public void clearOutput() {
		this.output = "";
		this.currentDelim = "";
	}
	
}
