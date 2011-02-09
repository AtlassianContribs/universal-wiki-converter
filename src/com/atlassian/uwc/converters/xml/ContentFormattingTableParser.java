package com.atlassian.uwc.converters.xml;

import java.util.regex.Matcher;
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
	
	/**
	 * token to help us keep nested data
	 */
	private static final String TABLETOKEN = "UWCSTARTTABLE";
	private static final String ROWTOKEN = "UWCSTARTTABLEROW";
	private static final String CELLTOKEN = "UWCSTARTTABLECELL";
	
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
			appendOutput(TABLETOKEN);
			break;
		case ROW:
			currentRow += "{tr"+params+"}";
			appendOutput(ROWTOKEN);
			break;
		case CELL:
			currentRow += "{td"+params+"}";
			appendOutput(CELLTOKEN);
			break;
		case HEADER:
			currentRow += "{th"+params+"}";
			appendOutput(CELLTOKEN);
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

	Pattern tabletoken = Pattern.compile("^(?s)(.*?)" + 
			TABLETOKEN +
			"(.*)$");
	Pattern rowtoken = Pattern.compile("^(?s)(.*?)" + 
			ROWTOKEN +
			"(.*)$");
	Pattern celltoken = Pattern.compile("^(?s)(.*?)" + 
			CELLTOKEN +
			"(.*)$");
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		currentRow = currentRow.replaceAll("\n{2,}$", "\n");
		switch (type) {
		case TABLE:
			tableout += "{table}";
			tableout = fixNL(tableout);
			integrateRowOutput(tabletoken, tableout);
			clean();
			break;
		case ROW:
			currentRow += "{tr}";
			tableout += currentRow;
			integrateRowOutput(rowtoken, currentRow);
			clean();
			currentRow = "";
			if (numCellsPerRow > maxCellsPerRow) maxCellsPerRow = numCellsPerRow;
			numCellsPerRow = 0;
			break;
		case CELL:
			numCellsPerRow++;
			integrateWithOutput(celltoken, currentRow);
			clean();
			currentRow += "{td}";
			break;
		case HEADER:
			numCellsPerRow++;
			integrateWithOutput(celltoken, currentRow);
			clean();
			currentRow += "{th}";
		}
	}

	protected void integrateWithOutput(Pattern tokenPattern, String content) {
		String out = getOutput();
		Matcher tokenFinder = tokenPattern.matcher(out); //find the beginning of the table
		
		boolean found = false;
		while (tokenFinder.find()) { //find the token for this element
			found = true;
			String pre = tokenFinder.group(1); //save existing pre-list content
			out = tokenFinder.group(2);
			this.clearOutput(); //delete all saved content
			appendOutput(pre + content + out); //rewrite saved content with fixed list 
			break;
		}
	}

	protected void integrateRowOutput(Pattern tokenPattern, String content) {
		String out = getOutput();
		Matcher tokenFinder = tokenPattern.matcher(out); //find the beginning of the table
		
		boolean found = false;
		while (tokenFinder.find()) { //find the token for this element
			found = true;
			String pre = tokenFinder.group(1); //save existing pre-list content
			out = tokenFinder.group(2);
			out = out.replaceAll("\n{2,}", "\n");
			this.clearOutput(); //delete all saved content
			appendOutput(pre + out + content); //rewrite saved content with fixed list 
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
