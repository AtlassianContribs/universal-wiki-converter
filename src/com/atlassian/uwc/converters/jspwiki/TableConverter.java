package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * converts Jspwiki table syntax to comparable Confluence syntax
 */
public class TableConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	
	public void convert(Page page) {
		log.info("Table Converting - start");
		
		String input = page.getOriginalText();
		
		String converted = convertTables(input);
		
		page.setConvertedText(converted);
		
		log.info("Table Converting - complete");
	}

	String table = "(?<=^|\n)" +//beginning of input or newline (zero width)
			"(" +				//start capturing (group1)
				"(?:" +			//start noncapturing container (A)
				"\\|" +			//pipe
				"[^\n]+" +		//not a newline, until...
				"(?:\n|$)" +	//newline or end
				")"  +			//close noncapturing A
				"+"	+			//repeat non capturing A group 
			")";				//close group 1
					   
	Pattern tablePattern = Pattern.compile(table);
	/**
	 * converts Jspwiki table syntax to Confluence syntax where
	 * jspwiki table syntax is found in the given input string
	 * @param input input containing table syntax. Could be multiple times, 
	 * and does not have to contain it at all.
	 * @return input with tables converted
	 */
	protected String convertTables(String input) {
		String converted = input;
		Matcher tableFinder = tablePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			found = true;
			String table = tableFinder.group(1); 
			String replacement = convertRows(table); 
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			converted = sb.toString();
			converted = escapeSpecialChars(converted);
		}
		return converted;
	}

	
	/**
	 * converts the rows in a given table to confluence syntax
	 * @param input a jspwiki table
	 * @return a confluence table
	 */
	protected String convertRows(String input) {
		//split on newlines, but don't capture them
		String[] lines = input.split("(?=\n)");
		
		String converted = "";
		int last = lines.length - 1;
		
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];

			//don't try to convert the last line if it's only a newline
			if (i == last && "\n".equals(line)) break; 
			
			line = escapePipes(line);  //escapePipes has to be called before table conversion 
			
			//the first line could be a header, otherwise, it's a cell 
			//FIXME is the above assertion true?
			switch (i) {
			case (0):
				String header = convertHeaders(line);
				//use header if convertHeaders did something
				if (!line.equals(header)) {
					converted += header;
					break;
				}
				//otherwise, convert as a cell
			default:
				converted += convertCells(line);
			}
		}
		return converted;
	}
	
	String header = "(" +				// group 1 starts 
					"\\|\\|" +			// double pipe
					".*?" +				// everything until... 
					"(?=\\|\\||n)" +	// zero-width double pipe or newline
					")";				// group 1 ends
	Pattern headerPattern = Pattern.compile(header);
	/**
	 * converts a jspwiki table row to Confluence header syntax, or 
	 * makes no changes if input is not a jspwiki header
	 * @param input table row
	 * @return confluence header or unchanged input
	 */
	protected String convertHeaders(String input) {
		String converted = input;
		Matcher headerFinder = headerPattern.matcher(converted);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (headerFinder.find()) {
			found = true;
			String header = headerFinder.group(1);
			headerFinder.appendReplacement(sb, header);
		}
		if (found) {
			headerFinder.appendTail(sb);
			converted = sb.toString();
			converted = converted.trim();
			converted += " ||\n";
		}
		return converted;
	}
	
	//cell regexes
	String content = "([^|\\\\]+)";
	String cell = 	"\\|" +				// starting pipe
					"\\s*" +			// optional whitespace
					content + 			// cell content (group 1)
					"(?=" +				// zero-width start (A)
						"\\||\n" +		// pipe or newline
					")";				// close A
	Pattern cellPattern = Pattern.compile(cell);
	/**
	 * converts a row of jspwiki cells to a row of confluence cells.
	 * Note: removes jspwiki double backslashes from within cells
	 * @param input example: | r2c1 | r2c2\n
	 * @return example: | r2c1 | r2c2 |\n
	 */
	protected String convertCells(String input) {
		String converted = removeBackslashes(input);
		Matcher cellFinder = cellPattern.matcher(converted);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (cellFinder.find()) {
			found = true;
			String cellContents = cellFinder.group(1);
			cellContents = cellContents.trim();
			String replacement = "| " + cellContents + " ";
			cellFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			cellFinder.appendTail(sb);
			converted = sb.toString();
			converted = converted.trim();
			converted += " |\n";
			return converted;
		}
		return input;
	}

	String backslash = "\\\\"; // this is java's fault
	String doubleBackslash = "\\s*" + backslash + backslash + "\\s*";
	/**
	 * removes sets of two backslashes from input 
	 * @param input
	 * @return input without sets of double backslashes<br/>
	 * Example: <br/>
	 * input = "a \\ b" <br/>
	 * return = "a b" <br/>
	 */
	protected String removeBackslashes(String input) {
		return input.replaceAll(doubleBackslash, " ");
	}

	String special = "[-*]"; //group containing any special chars (note: - has to be the first char in the group)
	String specialPattern = "(?<=\\| ?)"  //zerowidth pipe 
					+ "(" + special + ")"; //group of special characters
	/**
	 * adds backslashes before appropriate characters in tables
	 * (Confluence syntax for lists)
	 * NOTE: used to be called escapeStars
	 * @param input 
	 * @return input with backslashes to escape stars and dashes 
	 */
	protected String escapeSpecialChars(String input) {
		String replacement = "\\\\{group1}";
		return RegexUtil.loopRegex(input, specialPattern, replacement);
	}

	String pipe = "[|]"; //group containing other special chars
	String pipePattern = "~" + //tilda (Jspwiki escape char for pipes)
				"(" + pipe + ")"; //escaped chars (group1)
	/**
	 * escapes pipe characters in tables
	 * @param input
	 * @return input with appropriate pipe chars escaped
	 */
	protected String escapePipes(String input) {
		//See javadoc for handleWeirdReplacementBug, 
		//regarding how many backslashes to use in the replacement
		String replacement = "\\\\{group1}";
		return RegexUtil.loopRegex(input, pipePattern, replacement);
	}

}
