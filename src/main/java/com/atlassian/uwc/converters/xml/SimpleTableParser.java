package com.atlassian.uwc.converters.xml;

import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SimpleTableParser extends DefaultXmlParser {
	
	/**
	 * type of table tag (table, tr, td, etc)
	 */
	public enum Type {
		/**
		 * corresponds with tables
		 */
		TABLE,
		/**
		 * corresponds with rows
		 */
		ROW,
		/**
		 * corresponds with cells
		 */
		CELL;
		/**
		 * @param qName
		 * @return determines what type is associated with the given qName.
		 */
		static Type getType(String qName) {
			if ("table".equals(qName)) return TABLE;
			if ("tr".equals(qName)) return ROW;
			if ("td".equals(qName)) return CELL;
			return null;
		}
	}
	
	/**
	 * current output that will eventually be appended
	 */
	private static String tableout = "";
	private static String currentRow = "";
	private static int numCellsPerRow = 0;
	private static int maxCellsPerRow = 0;
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		switch (type) {
		case ROW:
			currentRow += "|";
			break;
		case CELL:
			currentRow += " ";
		}
	}
	
	Pattern ws = Pattern.compile("\\s*[\n]");
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		switch (type) {
		case TABLE:
			tableout = fixNL(tableout);
			appendOutput(tableout);
			clean();
			break;
		case ROW:
			currentRow = addMoreCells(currentRow);
//			currentRow = currentRow.replaceAll("[\n]", "");
			currentRow = ws.matcher(currentRow).replaceAll("");
			tableout += currentRow + "\n";
			currentRow = "";
			if (numCellsPerRow > maxCellsPerRow) maxCellsPerRow = numCellsPerRow;
			numCellsPerRow = 0;
			break;
		case CELL:
			numCellsPerRow++;
			currentRow += " |";
			break;
		}
	}

	/**
	 * clean any necessary state so we can start fresh next time
	 */
	private void clean() {
		tableout = currentRow = "";
		maxCellsPerRow = numCellsPerRow = 0;
	}
	
	private String fixNL(String input) {
		return input;
	}
	private String addMoreCells(String input) {
		if (numCellsPerRow < maxCellsPerRow) {
			for (int i = numCellsPerRow; i < maxCellsPerRow; i++) {
				input += " |";
			}
		}
		return input;
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		currentRow += String.copyValueOf(ch, start, length);
	}
}
