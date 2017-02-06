package com.atlassian.uwc.converters.mindtouch;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class TableParser extends DefaultXmlParser {

	private static final String TOKEN = "UWCSTARTTABLE";
	private static final String ROWTOKEN = "UWCSTARTROW";

	/**
	 * type of table tag (table, tr, td, etc)
	 */
	public enum Type {
		/**
		 * corresponds with tables
		 */
		TABLE,
		/**
		 * corresponds with table headers
		 */
		HEAD,
		/**
		 * corresponds with rows
		 */
		ROW,
		/**
		 * corresponds with cells
		 */
		CELL,
		/**
		 * corresponds with caption tag
		 */
		CAPTION;
		/**
		 * @param qName
		 * @return determines what type is associated with the given qName.
		 */
		static Type getType(String qName) {
			if ("table".equals(qName)) return TABLE;
			if ("tr".equals(qName)) return ROW;
			if ("td".equals(qName)) return CELL;
			if ("th".equals(qName)) return HEAD;
			if ("caption".equals(qName)) return CAPTION;
			return null;
		}
	}
	
	/**
	 * current output that will eventually be appended
	 */
	private static int numCellsPerRow = 0;
	private static int maxCellsPerRow = 0;
	
	/**
	 * delimiter fields
	 */
	public final static String CELLDELIM = "|";
	public final static String HEADDELIM = "||";
	private static final String DELIM_TOKEN = "DELIM_TOKEN";
	private static String delim = DELIM_TOKEN;
	
	/**
	 * caption fields
	 */
	private static String caption = "";
	private static Type type;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case TABLE: 
			appendOutput(TOKEN); //we'll use this to find the contents of other inner tags
			break;
		case ROW:
			appendOutput(ROWTOKEN);
			appendOutput(delim);
			break;
		case HEAD: 
			delim = HEADDELIM;
			replaceOutput(getOutput().replaceAll(DELIM_TOKEN, delim));
			break;
		case CELL:
			delim = CELLDELIM;
			replaceOutput(getOutput().replaceAll(DELIM_TOKEN, delim));
			appendOutput(" ");
			break;
		}
		this.type = type;
	}
	
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case TABLE:
			replaceOutput(getOutput().replaceFirst(TOKEN + "\\s*", ""));
			replaceOutput(getOutput().replaceFirst("(?<=[|]\n)\\s+\n$", ""));
			appendOutput(createCaption());
			clean();
			break;
		case ROW:
			String output = getOutput();
			replaceOutput(output.replaceFirst(" *" + ROWTOKEN + "(?s).*$", ""));
			String currentRow = output.replaceFirst("^(?s).*"+ROWTOKEN, "");
			currentRow = addMoreCells(currentRow);
			currentRow = currentRow.trim();
			currentRow = currentRow.replaceAll("[\n]", "");
			currentRow = currentRow.replaceAll(" {2,}", " ");
			appendOutput(currentRow + "\n");
			if (numCellsPerRow > maxCellsPerRow) maxCellsPerRow = numCellsPerRow;
			numCellsPerRow = 0;
			delim = DELIM_TOKEN;
			break;
		case HEAD:
		case CELL:
			numCellsPerRow++;
			appendOutput(" " + delim);
			break;
		}
	}

	private void replaceOutput(String newOutput) {
		clearOutput();
		appendOutput(newOutput);
	}

	/**
	 * clean any necessary state so we can start fresh next time
	 */
	private void clean() {
		caption = "";
	}
	
	private String addMoreCells(String input) {
		if (numCellsPerRow < maxCellsPerRow) {
			for (int i = numCellsPerRow; i < maxCellsPerRow; i++) {
				input += " " + delim;
			}
		}
		return input;
	}
	
	private String createCaption() {
		if (this.caption == null ||"".equals(this.caption)) return "";
		return "^" + this.caption.trim() + "^";
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		String content = String.copyValueOf(ch, start, length);
		switch (this.type) {
		case CAPTION:
			this.caption += content;
			break;
		default:
			appendOutput(content);
			break;
		}
	}
}
