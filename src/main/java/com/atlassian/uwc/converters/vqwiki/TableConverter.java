package com.atlassian.uwc.converters.vqwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * converts VQWiki table syntax to comparable Confluence syntax
 * Note: VQWiki syntax comprises: 
 * 
 * @author P.Hunter (Vtech)
 *
 */
public class TableConverter extends BaseConverter {

	// Conversion needs to handle:
	// ==========================
	//	 * ####
	//	 * header1 ## header2 ##    ===>	| header1 | header2 |
	//	 * data1   ## data2   ##			| data1 | data2 |
	//	 * ####								/n
	
	// .. and migrate all Wiki formatting inside each table cell ...

	//debugger objects
	public Logger log = Logger.getLogger(this.getClass());

	//CONSTANTS - tokens
	// protected static final String HEADER_DELIM = "||";  -- assume all tables have NO header rows
	protected static final String CELL_DELIM = "|";

	//CONSTANTS - regex patterns
	protected static final String DOTALL = "(?s)"; // flag to enable dotall mode ("." includes newline in search)
	private static Pattern tablePattern = Pattern.compile("(####[\r\n])(" + DOTALL + ".*?)(####[\r\n])");
	private static Pattern newlinePattern = Pattern.compile("^[\r\n](.*)");
	private static Pattern rowPattern = Pattern.compile("((.*)##)");
	private static Pattern cellPattern = Pattern.compile("(.*?)##");

	
	public void convert(Page vqwikiPage) {
		
		log.info("** Converting VQWiki Tables -- starting.");
		String pageAsTextString = vqwikiPage.getOriginalText();   // input (original) page text
		String convertedPage = "";  // write page to this output string, after successful conversion

		Matcher vqTableFinder = tablePattern.matcher( pageAsTextString );	
		StringBuffer sb  = new StringBuffer();

		// for each table encountered ...
		while (vqTableFinder.find()) {
			String tableInnards = vqTableFinder.group(2);
			//log.info("** tableInnards.b4:" + tableInnards );
			// clean table innards and convert to Confluence table
			String tableConfluence = convertTable( tableInnards );
			//log.info("** tableInnards.aft:" + tableConfluence );
			// replace VQWiki table text with converted Confluence table text
			vqTableFinder.appendReplacement(sb, tableConfluence);
		}

		// terminate converted text string and pass back to calling program
		vqTableFinder.appendTail(sb);
		convertedPage = sb.toString();

		vqwikiPage.setConvertedText(convertedPage);
		log.info("** Converting VQWiki Tables -- completed.");
	}

	/**
	 * Strips newline chars from beginning and end, then strips out row data
	 * and passes this on for cleaning of cell data. Finally, puts sell data inside
	 * Confluence table delimiters and returns Confluence-formatted table
	 * @param vqTableData - data from VQWiki inside "####" tags
	 * @return table data formatted in Confluence-style
	 */
	private String convertTable(String vqTableData) {
		String confluTableData = "";

		// Strip off "/n" at start
		Matcher newlineFinder = newlinePattern.matcher( vqTableData );
		vqTableData = newlineFinder.replaceAll("$1");
		//log.info("** convertTable.vqTableData:[" + vqTableData + "]" );

		Matcher lineFinder = rowPattern.matcher( vqTableData );
		StringBuffer sb  = new StringBuffer();
		while (lineFinder.find()) {
			String vqTableRow = lineFinder.group(1);
			//log.info("** convertTable.vqTableRow:[" + vqTableRow + "]" );
			vqTableRow = convertRowData(vqTableRow);
			lineFinder.appendReplacement(sb, vqTableRow);
		}
		lineFinder.appendTail(sb);
		
		//String tableContents = vqTableDataFinder.replaceAll("$1");

		//Matcher vqCellFinder = allCellsPattern.matcher(vqTableData);
		//String confluTableData = vqCellFinder.replaceAll("$1 |");
		
		confluTableData = sb.toString();
		return confluTableData;
	}
	
	private String convertRowData(String vqRowData) {
		String convertedRowData = "";

		Matcher cellFinder = cellPattern.matcher(vqRowData);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (cellFinder.find()) {
			found = true;
			String cellContents = cellFinder.group(1);
			cellContents = cellContents.trim();
			String replacement = CELL_DELIM + " " + cellContents + " ";
			cellFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			cellFinder.appendTail(sb);
			convertedRowData = sb.toString();
			convertedRowData = convertedRowData.trim();
			convertedRowData += " " + CELL_DELIM;
			//log.info("*2* convertedRowData:[" + convertedRowData + "]" );
			return convertedRowData;
		}
		return vqRowData;
	}
	
}
