package com.atlassian.uwc.converters.tikiwiki;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Converts tikiwiki tables to Confluence table syntax
 * 
 * @author Laura Kolker
 *
 */
public class TableConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());

	public void convert(Page page) {
		log.debug("Converting Table - starting");

		String input = page.getOriginalText();
		String converted = convertTable(input);
		
		page.setConvertedText(converted);
		log.debug("Converting Table - complete");
	}

	/**
	 * @param input tikiwiki input
	 * @return Confluence syntax replacement for the given input
	 */
	protected String convertTable(String input) {
		String output = convertRows(input);
		output = convertHeaders(output);
		return output;
	}

	String pipe = "\\|";
	String doublePipe = pipe + pipe;
	String rowDelim = doublePipe + "|\n";
	String table = 
		"(?:" +			//dont capture
			"^|\n" +	//beginning of string or newline
		")" +	 		//end don't capture
		doublePipe + 	//double pipe
		".*?" + 		//anything until
		doublePipe + 	//double pipe
		"(?=" +			//zero width non capture group (zero-width in important, 'cause we'll need that newline)
			"\n|$" +	//newline or end of string
		")"; 			//end non capture group
	public final Pattern tablePattern = Pattern.compile(table, Pattern.DOTALL);
	String row = "(?:" +rowDelim + ")" + "(.*?)" + "(?="+rowDelim+")";//FIXME this doesn't quite work -- one of the rowDelims Has to be | or we get false positives
	Pattern rowPattern = Pattern.compile(row);
	/**
	 * converts the basic row syntax
	 * @param input tikiwiki syntax
	 * <br/>Example:<br/>
	 * || __Some__| tikiwiki||rows|here||
	 * @return confluence syntax
	 * <br/>Example:<br/>
	 * | __Some__| tikiwiki|<br/>
	 * |rows|here|
	 */
	protected String convertRows(String input) {
		String output = input;
		
		//tables that are right next to each other need an extra newline
		int newlineIndex = -1;
		Vector<Boolean> needsDelimNewline = new Vector<Boolean>();
		
		//we slurp up the table in case of ambiguous pipe delimiters
		Matcher tableFinder = tablePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			found = true;
			String table = tableFinder.group();
			Matcher rowFinder = rowPattern.matcher(table);
			StringBuffer rowSb = new StringBuffer();
			int numColumns = 0;
			while (rowFinder.find()) {
				String content = rowFinder.group(1);
				if ("".equals(content)) continue;
				
				//handle empty first cells (otherwise are lost)
				if (content.startsWith("|")) content = " " + content;

				String replacement = "|" + content + "|\n";
				replacement = convertCells(replacement);
				if (numColumns < 1) 
					numColumns = getNumberOfColumns(replacement);
				else
					replacement = enforceColumnNumbering(numColumns, replacement);
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				rowFinder.appendReplacement(rowSb, replacement);
			}
			numColumns = 0;
			rowFinder.appendTail(rowSb);
			String replacement = rowSb.toString();
			
			//deal with newline issues for tables right next to each other
			if (newlineIndex > -1 && newlineIndex == tableFinder.start()) 
				needsDelimNewline.add(new Boolean(true));
			else 
				needsDelimNewline.add(new Boolean(false));
			newlineIndex = tableFinder.end();
			
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			output = sb.toString();
			output = removeOpeningExtraPipes(output);
			output = removeFinalDelims(output, needsDelimNewline);
		}
		return output;
	}

	/**
	 * removes the last cell delimiters from all the tables. 
	 * Otherwise we end up with an extra "\n||\n" at then end of the table 
	 * @param input string with tables
	 * @param needsDelimNewline list of true/false objects, one for each table.
	 * If the object is true, the corresponding table needs an extra newline to seperate
	 * it from the next table. (Important for tables that are sitting right next to each other)
	 * @return 
	 */
	protected String removeFinalDelims(String input, Vector<Boolean> needsDelimNewline) {
		//removing delimiters might have to happen more than once
		String preRemove = "";
		String postRemove = input;
		while (!preRemove.equals(postRemove)) {
			preRemove = postRemove;
			postRemove = removeFinalDelim(preRemove, needsDelimNewline);
		}
		if (postRemove != null) input = postRemove;
		return input;
	}

	/**
	 * same as removeFinalDelim(String input, Vector<Boolean>), but
	 * the boolean vector that's passed is null. 
	 * @param input
	 * @return
	 */
	protected String removeFinalDelim(String input) {
		return removeFinalDelim(input, null);
	}
	String finalDelim = 
		"(" +				//start capture (group 1)
			"(?:" +			//start non-capture group
				"\\|" +		//a pipe
				"[^\n]*" +	//0 or more newline until
				"\n" +		//newline
			")" +			//end non-capture group
			"+" +			//1 or more of the previous non-capture group
		")" +				//end capture (group1)
		"(" +				//start capture (group 2)
			"(" +			//start capture (group 3)
				"\n*" +		//0 or more newlines
			")" +			//end capture (group 3)
			"\\|\\|" +		//two pipes
		")" +				//end capture (group 2)
		"(" +				//start capture (group 4)
			"\n|$" +		//a newline or end of string
		")";				//end capture (group 4)
	Pattern finalDelimPattern = Pattern.compile(finalDelim);
	/**
	 * This is used to clean up the conversion for rows.
	 * This must be called to avoid having an extra || delimiter 
	 * after the converted table. Also, important for handling
	 * tables that are right next to each other.
	 * @param input 
	 * <br/>Example:<br/>
	 * | Table | here |<br/>
	 * ||
	 * @param needsNL has the same number of objects as the number of tables in 
	 * the given input. If a needNL object is true, then the corresponding table
	 * needs an extra newline to seperate it from another table. If this object
	 * if null, it's the same as if a needsNL object was passed with all elements set to false. 
	 * @return
	 * <br/>Example:<br/>
	 * | Table | here |
	 */
	protected String removeFinalDelim(String input, Vector<Boolean> needsNL) {
		Matcher finalFinder = finalDelimPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		int tableIndex = 0;
		while (finalFinder.find()) {
			found = true;
			String before = finalFinder.group(1);
			
			//deal with table-seperating newlines
			if (needsNL != null && tableIndex < needsNL.size())
				before = addNewline(before, needsNL.get(tableIndex));
			tableIndex++;
			
			String replacement = before;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			finalFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			finalFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * if needsNL is true, adds a newline to the beginning of input
	 * @param input
	 * @param needsNL
	 * @return
	 */
	protected String addNewline(String input, Boolean needsNL) {
		return (needsNL.booleanValue()?"\n":"") + input;
	}

	String header = "\\|\\s*__([^|]*)(?:__\\s*)(?:(?=\\|[^\n])|(\\|\n))";
	Pattern headerPattern = Pattern.compile(header);
	/**
	 * converts tikiwiki header syntax (bolded) to confluence header syntax
	 * @param input
	 * <br/>Example:<br/>
	 * | __Header__ | __Header__ | 
	 * | __Header__ | not a header |
	 * 
	 * @return
	 * <br/>Example:<br/>
	 * || Header || Header ||
	 * || Header | not a header |
	 */
	protected String convertHeaders(String input) {
		Matcher headerFinder = headerPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		String output = input;
		boolean found = false;
		while (headerFinder.find()) {
			found = true;
			String content = headerFinder.group(1);
			String endDelim = headerFinder.group(2);
			if (endDelim == null) endDelim = " ";
			else endDelim = " |" + endDelim;
			String replacement = "|| " + content + endDelim;
			headerFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			headerFinder.appendTail(sb);
			output = sb.toString();
		}
		return output;
	}

	String noWSBegin = "\\|+([^\\s|]+)";
	String noWSEnd = "([^\\s|]+)\\|+";
	/**
	 * makes sure cells have an orderly amount of whitespace
	 * @param input
	 * <br/>Example:<br/>
	 * |cells|with| disorderly|whitespace |
	 * 
	 * @return
	 * <br/>Example:<br/>
	 * | cells | with | disorderly | whitespace |
	 */
	protected String convertCells(String input) {
		String replacementBegin = "| {group1}";
		String replacementEnd = "{group1} |";
		String output = RegexUtil.loopRegex(input, noWSBegin, replacementBegin);
		output = RegexUtil.loopRegex(output, noWSEnd, replacementEnd);
		output = escapeSpecialCharacters(output);
		output = expandEmptyCells(output);
		return output;
	}

	/**
	 * escapes confluence special characters that would
	 * be rendered incorrectly.
	 * 
	 * Example: if cell begins with a dash (-), the Confluence
	 * renderer will think it's a list item unless we escape the dash.
	 * 
	 * @param input
	 * @return
	 */
	protected String escapeSpecialCharacters(String input) {
		// so far there's only dashes
		String escaped = escapeListContextDashes(input);
		return escaped;
	}

	String listContextDashes = 
			"(?<=" +		//zero width lookbehind
				"\\|" + 	//starting pipe
			")" +			//end zero width capture
			"\\s*" +		//optional whitespace
			"-";			//dash
	Pattern listDashesPattern = Pattern.compile(listContextDashes);
	/**
	 * @param input
	 * @return replaces any dashes in the given input
	 * that were not meant to be in list context
	 * with escaped dashes.
	 */
	protected String escapeListContextDashes(String input) {
		Matcher listDashesFinder = listDashesPattern.matcher(input);
		if (listDashesFinder.find()) {
			return listDashesFinder.replaceAll(" \\\\-");
		}
		return input;
	}


	String triplePipe = "(?<=^|\n)" + doublePipe + pipe;
	/**
	 * Translates incorrect triple pipes (|||) to a single pipe.
	 * 
	 * (Sometimes at the end of a conversion we end up with 
	 * a triple pipe at the beginning of a line.)
	 * 
	 * @param input
	 * @return input with instances of line beginning triple pipes, 
	 * translated to single pipes (|)
	 */
	protected String removeOpeningExtraPipes(String input) {
		return input.replaceAll(triplePipe, "|");
	}

	
	Pattern pipePattern = Pattern.compile(pipe + "{1,2}"); //one or 2 columns
	/**
	 * @param input a row
	 * @return the number of columns referenced in the given input
	 */
	protected int getNumberOfColumns(String input) {
		Matcher pipeFinder = pipePattern.matcher(input);
		int num = 0;
		while (pipeFinder.find()) {
			num++;
		}
		return num - 1;
	}

	/**
	 * makes the given input have numColumns number of cols 
	 * @param numColumns
	 * @param input a table row
	 * @return
	 */
	protected String enforceColumnNumbering(int numColumns, String input) {
		int num = getNumberOfColumns(input);
		if (num < numColumns) {
			input = addColspans(numColumns, input, num);
		}
		else if (num > numColumns) {
			//how many more?
			int difference = num - numColumns;
			input = reduceColspans(input, difference);
		}
		return input;
	}

	/**
	 * adds the necessary num of columns to input to make it have
	 * requiredNum number of cols
	 * @param requiredNum the required number of columns
	 * @param input the row
	 * @param inputNum the row's current number of columns
	 * @return the row with the required # of cols
	 */
	protected String addColspans(int requiredNum, String input, int inputNum) {
		String withoutNewline = input.replaceFirst("\n+$", "");
		boolean addNL = (!input.equals(withoutNewline));
		for(int i = inputNum; i < requiredNum; i++)
			withoutNewline += " |";
		input = withoutNewline + (addNL?"\n":"");
		return input;
	}

	/**
	 * removes unnecessary (unsupported) colspans. (Cleans up ugly extra | |)
	 * @param input A row 
	 * @param difference a positive number, indicating how many extra cells
	 * there were than the header of the table
	 * @return If the ending cells in the row are empty, and the row has colspans, then
	 * try to remove the unnecessary empty cells, so that the row looks cleaner.
	 * Doesn't work for every case. 
	 * @throws IllegalArgumentException if difference is 0 or negative
	 */
	protected String reduceColspans(String input, int difference) {
		if (difference < 1) {
			String message = "difference must be greater than 0. Difference: " + difference;
			log.error(message);
			throw new IllegalArgumentException(message);
		}
		//FIXME this doesn't work for every case. 
		//if the difference is greater than the number of empty cells, then 
		//even if there are candidate empty cells for pruning,
		//they won't get removed
//		log.debug("input =\t'" + input + "'");
		boolean andNewline = input.endsWith("\n");
		String emptyColumns = 
				"^" +					//start at the beginning of the string
				"(" +					//start capture (group 1)
					".*" +				//everything until
				")" +					//end capture (group 1)
				"(?:" +					//start non-capture group
					"(" +				//start capture (group 2)
						"\\|" +			//a pipe
					")" +				//end capture (group 2)
					" " +				//a space
				")" +					//end non-capture group
				"{"+difference+"}" +	//repeat the previous group exactly 'difference' number of times
				"\\|" +					//a pipe
				"$";					//the end of the string
		Pattern emptyColsPattern = Pattern.compile(emptyColumns);
		Matcher emptyColsMatcher = emptyColsPattern.matcher(input);
		if (emptyColsMatcher.find()) {
			String preEmptyCols = emptyColsMatcher.group(1);
			String lastPipe = emptyColsMatcher.group(2);
			String newRow = preEmptyCols + lastPipe;
			newRow += andNewline?"\n":"";
//			log.debug("newRow =\t'" + newRow + "'");
			return newRow;
		}
		return input;
	}

	/**
	 * expands empty cells so that they are correct Confluence Syntax.
	 * <br/>Example:
	 * <br/>input: | A ||
	 * <br/>output: | A | | 
	 * <br/>Example:
	 * <br/>input: ||A||
	 * <br/>output: ||A||
	 * @param input 
	 * @return
	 */
	protected String expandEmptyCells(String input) {
		if (!input.startsWith("||")) { //don't change double pipes in header rows 
			return input.replaceAll(doublePipe, "| |"); 
		}
		return input;
	}


}
