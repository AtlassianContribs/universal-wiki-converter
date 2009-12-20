package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * converts HTML/Jotspot tables to Confluence tables
 * @author Laura Kolker
 *
 */
public class TableConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	
	private String CELL_DELIM = "|";
	private static final String HEADERCELL = "th";
	
	public void convert(Page page) {
		log.debug("Table Converter - starting");
		String input = page.getOriginalText();
		
		input = cleanTags(input);
		String converted = convertTable(input);
		
		log.debug("converted = " + converted);
		page.setConvertedText(converted);
		log.debug("Table Converter - complete");

	}

	String rows = "<tr[^>]*>(.*?)</tr>";
	Pattern rowPattern = Pattern.compile(rows, Pattern.DOTALL);
	Pattern newlines = Pattern.compile("\n+");
	/**
	 * @param input HTML table
	 * @return Confluence table
	 */
	protected String convertTable(String input) {
		//do the conversion
		Matcher rowFinder = rowPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		int rownum = 1;
		while (rowFinder.find()) {
			String row = rowFinder.group(1);
			log.debug("row = " + row);
//			String replacement = (row.startsWith("\n")?"":"\n") + CELL_DELIM + convertRow(row);
			String replacement = "";
			replacement += convertRow(row);
			replacement = CELL_DELIM + replacement;
			replacement += row.startsWith("\n")?"":"\n";
			
//			String replacement = CELL_DELIM + convertRow(row);
//			Matcher nlFinder = newlines.matcher(replacement);
//			if (nlFinder.lookingAt()) {
//				replacement = nlFinder.replaceFirst("");
//				replacement = "\n" + replacement;
//			}
//			String replacement = CELL_DELIM + convertRow(row);
//			if (!row.startsWith("\n")) {
//				replacement = "\n" + replacement;
//			}
//			else {
//				Matcher nlFinder = newlines.matcher(row);
//				if (nlFinder.lookingAt()) {
//					replacement = nlFinder.replaceFirst("\n");
//				}
//			}
			if (rownum++ == 1) {
				replacement = convertHeader(replacement);
			}
			log.debug("rep = " + replacement);
			rowFinder.appendReplacement(sb, replacement);
		}
		rowFinder.appendTail(sb);
		input = sb.toString();
		input += input.endsWith("\n")?"":"\n";
		return input;
	}

	Pattern linebreaks = Pattern.compile("<br ?\\/>");
	/**
	 * gets rid of &lt;br /&gt;
	 * @param input 
	 * @return input without linebreaks
	 */
	private String removeLineBreaks(String input) {
		Matcher breakFinder = linebreaks.matcher(input);
		if (breakFinder.find()) {
			input = breakFinder.replaceAll("");
		}
		return input;
	}

	String cells = "<(t[hd])[^>]*>(.*?)</\\1>";
	Pattern cellPattern = Pattern.compile(cells, Pattern.DOTALL);
	/**
	 * converts HTML row into Confluence row (without initial line delim)
	 * @param input HTML row
	 * @return Confluence row
	 */
	protected String convertRow(String input) {
		//get rid of any jotspot inserted linebreaks 
		input = removeLineBreaks(input);
		//convert cell
		Matcher cellFinder = cellPattern.matcher(input);
		String newCell = "";
		while (cellFinder.find()) {
			String celltype = cellFinder.group(1);
			CELL_DELIM = HEADERCELL.equals(celltype)?"||":"|";
			String cell = cellFinder.group(2);
			cell = removeBadChar(cell); //jotspot exports some non-standard char as ws, sometimes.
			cell = cell.trim();
			if ("".equals(cell)) cell = " " + CELL_DELIM;
			else cell = " " + cell + " " + CELL_DELIM;
			newCell += cell;
		}
		return newCell;
	}
	
	Pattern header = Pattern.compile("\\|\\s*<b\\s*[^>]*>(.*?)<\\/b>\\s*");
	protected String convertHeader(String input) {
		String output = input;
		Matcher headerFinder = header.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		String delim = "||";
		while (headerFinder.find()) {
			found = true;
			String header = headerFinder.group(1);
			header = header.trim();
			String replacement = delim + " " + header + " |";
			headerFinder.appendReplacement(sb, replacement);
			delim = "|";
		}
		if (found) {
			headerFinder.appendTail(sb);
			output = sb.toString();
		}
		return output;
	}
	
	Pattern badChar = Pattern.compile("\\?\\|");
	/**
	 * removes jotspot injected "bad" characters: ?|
	 * @param input conversion file contents
	 * @return conversion file contents without ?|
	 */
	protected String removeBadChar(String input) {
		Matcher badCharFinder = badChar.matcher(input);
		if (badCharFinder.find()) {
			return badCharFinder.replaceAll("");
		}
		return input;
	}
	
	Pattern tableTag = Pattern.compile("<table[^>]*>(.*?)<\\/table>", Pattern.DOTALL);
	Pattern tbodyTag = Pattern.compile("<tbody[^>]*>(.*?)<\\/tbody>", Pattern.DOTALL);
	/**
	 * removes table and tbody tags
	 * @param input conversion file contents
	 * @return conversion file contents without table and tbody tags
	 */
	protected String cleanTags(String input) {
		String output = input;
		Matcher tableFinder = tableTag.matcher(input);
		if (tableFinder.find()) {
			String contents = tableFinder.group(1);
			output = tableFinder.replaceAll(contents);
		}
		Matcher tbodyFinder = tbodyTag.matcher(output);
		if (tbodyFinder.find()) {
			String contents = tbodyFinder.group(1);
			output = tbodyFinder.replaceAll(contents);
		}
		return output.trim();
	}
}
