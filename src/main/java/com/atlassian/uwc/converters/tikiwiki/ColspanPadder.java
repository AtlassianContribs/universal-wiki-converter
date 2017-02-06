package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * used to transform tables that originally had colspans and
 * now, in Confluence, have a mismatched number of tables,
 * so that each row has the same number of columns
 */
public class ColspanPadder extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());

	/*We want to reuse table converter code in this class.
	 *We don't do these ops _in_ the TableConverter 
	 *because we want this converter to be optional.
	 */ 
	TableConverter tableConverter = new TableConverter();

	public void convert(Page page) {
		log.debug("Padding Colspans - starting");

		String input = page.getOriginalText();
		String converted = padColspans(input);
		page.setConvertedText(converted);
		
		log.debug("Padding Colspans - complete");
	}

	String confluenceTable = 
		"(?<=" +		//zero-width look behind
			"^|\n" +	//beginning of string or newline
		")" +			//end zero-width
		"(" +			//start capture (group 1)
			"\\|" + "{1,2}" +	//one or two pipes
			"[^\n]+" +	//anything but a newline until
			"\n" +		//a newline
		")" +			//end capture (group 1)
		"+" +			//repeat group 1
		"(?!" +			//zero-width neg look ahead
			"\\|" +		//a pipe
		")";			//end zero-width
	Pattern tablePattern = Pattern.compile(confluenceTable);
	/**
	 * adds cells to tables with varying numbers of columns, so that they all line up
	 * @param input 
	 * @return
	 */
	protected String padColspans(String input) {
		Matcher tableFinder = tablePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			found = true;
			String table = tableFinder.group();
			String replacement = padColspansInOneTable(table);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * pads the rows in a table with extra cells, so that
	 * the number of columns in each row is the same
	 * @param table one table of Confluence syntax
	 * @return table with extra cells where necessary
	 * Example:<br/>
	 * if input =<br/> 
	 * | r1c1 |<br/>
	 * | r2c1 | r2c2 |<br/>
	 * then return=<br/>
	 * | r1c1 | |<br/>
	 * | r2c1 | r2c2 |<br/>
	 */
	protected String padColspansInOneTable(String table) {
		//Find the max number of columns.
		String[] rows = table.split("\n");
		int max = getMaxNumColumns(rows);
		
		//go through each and add the difference as necessary
		String padded = addColumnsToRows(rows, max);
		
		//handle "table" with no rows case
		if (rows.length == 0) padded = table;
		
		return padded;
	}

	/**
	 * adds cells to each row, so that the number of cols = max
	 * @param rows
	 * @param max
	 * @return padded table 
	 */
	protected String addColumnsToRows(String[] rows, int max) {
		String padded = "";
		for (int i = 0; i < rows.length; i++) {
			String row = rows[i];
			int inputNum = tableConverter.getNumberOfColumns(row);
			row = tableConverter.addColspans(max, row, inputNum);
			padded += row + "\n";
		}
		return padded;
	}

	/**
	 * figures out the max number of columns that any of the
	 * given rows has
	 * @param rows
	 * @return the biggest number of columns for this set of rows
	 */
	protected int getMaxNumColumns(String[] rows) {
		int max = 1; 
		for (int i = 0; i < rows.length; i++) {
			String row = rows[i];
			int numCols = tableConverter.getNumberOfColumns(row);
			if (numCols > max) max = numCols;
		}
		return max;
	}

}
