package com.atlassian.uwc.converters.xml;

import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ContentFormattingTableParser extends DefaultXmlParser {
	
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
		CELL,
		HEADER;
		/**
		 * @param qName
		 * @return determines what type is associated with the given qName.
		 */
		static Type getType(String qName) {
			if ("table".equals(qName)) return TABLE;
			if ("tr".equals(qName)) return ROW;
			if ("td".equals(qName)) return CELL;
			if ("th".equals(qName)) return HEADER;
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
	
	Pattern borderatt = Pattern.compile("[:|]border=");
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		currentRow = currentRow.replaceAll("\n{2,}$", "\n");
		
		String params = getParams(attributes);
		
		switch (type) {
		case TABLE:
			if ("".equals(params)) params = ":border=1";
			else if (!borderatt.matcher(params).find()) params += "|border=1";
			currentRow += "{table"+params+"}\n";
			break;
		case ROW:
			currentRow += "{tr"+params+"}\n";
			break;
		case CELL:
			currentRow += "{td"+params+"}";
			break;
		case HEADER:
			currentRow += "{th"+params+"}";
			break;
		}
	}
	
	private String getParams(Attributes attributes) {
		String params = "";
		for (int i = 0; i < attributes.getLength(); i++) {
			String key = attributes.getLocalName(i);
			String val = attributes.getValue(i);
			if (!"".equals(params)) params += "|";
			params += key + "=" + val;
		}
		if (!"".equals(params)) params = ":" + params;
		return params;
	}

	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		currentRow = currentRow.replaceAll("\n{2,}$", "\n");
		switch (type) {
		case TABLE:
			tableout += "{table}";
			tableout = fixNL(tableout);
			appendOutput(tableout);
			clean();
			break;
		case ROW:
			currentRow += "{tr}\n";
			tableout += currentRow + "\n";
			currentRow = "";
			if (numCellsPerRow > maxCellsPerRow) maxCellsPerRow = numCellsPerRow;
			numCellsPerRow = 0;
			break;
		case CELL:
			numCellsPerRow++;
			currentRow += "{td}\n";
			break;
		case HEADER:
			numCellsPerRow++;
			currentRow += "{th}\n";
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
		input = input.replaceAll("(?<=\\{t[rdh]\\})\\s{2,}(?=\\{t[rdh])", "\n");
		input = input.replaceAll("(?<=\\})\\s{2,}(?=\\{tr\\})", "\n");
		input = input.replaceAll("(?<=\\{tr\\})\\s{2,}(?=\\{table\\})", "\n");
		return input;
	}

	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		currentRow += String.copyValueOf(ch, start, length);
	}
}
