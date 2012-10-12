package com.atlassian.uwc.converters.dokuwiki;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class TableRowColSpanConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String tmpconverted = convertColspans(input);
		if (!(page instanceof VersionPage) && !input.equals(tmpconverted)) 
			log.debug("Colspans detected: '" + page.getName() + "' in space: " + page.getSpacekey());
		String converted = convertRowspans(tmpconverted);
		if (!(page instanceof VersionPage) && !tmpconverted.equals(converted)) 
			log.debug("Rowspans detected: '" + page.getName() + "' in space: " + page.getSpacekey());
		page.setConvertedText(converted);
	}

	Pattern td = Pattern.compile("<t([dh])>(.*?)</t[dh]>", Pattern.DOTALL);
	Pattern uwctokencolspan = Pattern.compile("::" + PrepColSpansConverter.TOKENKEY + "(\\d+)::");
	protected String convertColspans(String input) {
		Matcher tdFinder = td.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tdFinder.find()) {
			found = true;
			String type = tdFinder.group(1);
			String row = tdFinder.group(2);
			Matcher uwctokenFinder = uwctokencolspan.matcher(row);
			boolean found2 = false;
			String len = "";
			StringBuffer sb2 = new StringBuffer();
			while (uwctokenFinder.find()) {
				found2 = true;
				len = uwctokenFinder.group(1);
				String rep2= RegexUtil.handleEscapesInReplacement("");
				uwctokenFinder.appendReplacement(sb2, rep2);
			}
			if (found2) {
				uwctokenFinder.appendTail(sb2);
				row = sb2.toString();
			}
			else continue;
			String replacement = "<t"+type+" colspan='"+len+"'>"+row+"</t"+type+">";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tdFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tdFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern table = Pattern.compile("<table>(.*?)</table>", Pattern.DOTALL);
	Pattern rowspan = Pattern.compile(":::");
	Pattern tablerow = Pattern.compile("<tr>(.*?)</tr>", Pattern.DOTALL);
	Pattern tdWithColspan = Pattern.compile("<t([dh])(?: colspan='(\\d+)')?>(.*?)</t[dh]>", Pattern.DOTALL);
	protected String convertRowspans(String input) {
		Matcher tableFinder = table.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			Vector<Integer> rowindeces = new Vector<Integer>();
			Vector<Integer> colindeces = new Vector<Integer>();
			Vector<Integer> rowvals = new Vector<Integer>();
			Vector<Integer> colclear = new Vector<Integer>();
			found = true;
			String tableContents = tableFinder.group(1);
			Matcher rowspanFinder = rowspan.matcher(tableContents);
			if (!rowspanFinder.find()) continue;

			int rowspanVal = 1; //the value of the rowspan attribute
			int lastrow = 0;
			int rowspancount = 0;

			Matcher rowFinder = tablerow.matcher(tableContents);
			StringBuffer rowsb = new StringBuffer();
			boolean rowfound = false;
			boolean noteindex = true;

			boolean clearrow = false;
			while (rowFinder.find()) {
				int lastcol = -1;
				rowfound = true;
				boolean newrow = true;

				String rowcontents = rowFinder.group(1);
				Matcher tdFinder = tdWithColspan.matcher(rowcontents);
				StringBuffer tdsb = new StringBuffer();
				boolean tdfound = false;
				boolean rowspanfoundLast = true;
				boolean rowspanfoundCurrent = true;
				int rowspancountThisRow = 0;
				int currentColOffset = 0;
				while (tdFinder.find()) {
					tdfound = true;
					lastcol++;
					String cell = tdFinder.group(3);
					String colspanOffset = tdFinder.group(2);

					rowspanFinder = rowspan.matcher(cell);
					rowspanfoundLast = rowspanfoundCurrent;
					rowspanfoundCurrent = rowspanFinder.find();
					if (!rowspanfoundCurrent) { //no rowspan
						colclear.add(lastcol+currentColOffset);
					}
					else { //found a rowspan!
						tdFinder.appendReplacement(tdsb, ""); //remove the ::: cells
						if (newrow && clearrow && rowspanVal > 1) {
							rowspancount++;
							fillRowSpanVals(rowindeces, rowvals, rowspanVal);
							rowspanVal = currentColOffset+1;
							colindeces.add(lastcol); //note the index of the current cell
							rowindeces.add(lastrow-1);//note the index of the previous row
							noteindex = false;
						}
						rowspanVal++;

						if (noteindex || !newrow) {
							rowspancount++;
							rowspancountThisRow++;
							colindeces.add(lastcol); //note the index of the current cell
							rowindeces.add(lastrow-1);//note the index of the previous row
							noteindex = false;
							if (!newrow && rowspancountThisRow > 0) rowspanVal--;
							colclear.removeAllElements();
						}
						else if (!rowspanfoundLast) {
							noteindex = true; 
							rowvals.add(rowspanVal);
							rowspanVal=currentColOffset+1;
						}

						newrow = false;
					}
					if (colspanOffset != null) 
						currentColOffset += (Integer.parseInt(colspanOffset));
				}
				if (tdfound) {
					tdFinder.appendTail(tdsb);
					rowcontents = "<tr>"+tdsb.toString()+"</tr>";
				}
				else
					rowcontents = "<tr>" + rowcontents + "</tr>";


				String replacement = rowcontents;
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				rowFinder.appendReplacement(rowsb, replacement);
				lastrow++;
				boolean tmpclear = true;
				for (int i = 0; i < lastcol-1; i++) {
					if (!colclear.contains(i+currentColOffset)) tmpclear = false; 
				}
				clearrow = tmpclear;
				colclear.removeAllElements();
			}
			if (rowfound) {
				fillRowSpanVals(rowindeces, rowvals, rowspanVal);
				rowspanVal=1;
				rowFinder.appendTail(rowsb);
				tableContents = rowsb.toString();
			}

			rowFinder = tablerow.matcher(tableContents);
			rowsb = new StringBuffer();
			rowfound = false;
			int currentrow = 0;
			int index = 0;
			while (rowFinder.find()) {
				rowfound = true;
				String rowcontents = rowFinder.group(1);

				if (index >= rowspancount) break;
				int rowindex = rowindeces.get(index);
				int colindex = colindeces.get(index);
				int rowval = rowvals.get(index);
				if (currentrow == rowindex) {
					Matcher tdFinder = td.matcher(rowcontents);
					StringBuffer tdsb = new StringBuffer();
					boolean tdfound = false;

					int currentcell = 0;
					while (tdFinder.find()) {
						tdfound = true;
						String type = tdFinder.group(1);
						if (currentcell == colindex) {
							String newcell = "<t"+type+" rowspan='"+rowval+"'>"+tdFinder.group(2)+"</t"+type+">";
							tdFinder.appendReplacement(tdsb, newcell); //replace the rowspan cell
							index++;
							if (index < rowspancount) { //get these now in case we have more rowspans in the same row
								colindex = colindeces.get(index); 
								rowval = rowvals.get(index);
							}
						}
						currentcell++;
					}
					if (tdfound) {
						tdFinder.appendTail(tdsb);
						rowcontents = tdsb.toString();
					}
				}

				String replacement = "<tr>"+rowcontents+"</tr>";
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				rowFinder.appendReplacement(rowsb, replacement);
				currentrow++;
			}
			if (rowfound) {
				rowFinder.appendTail(rowsb);
				tableContents = rowsb.toString();
			}

			String replacement = "<table>" + tableContents + "</table>";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	public void fillRowSpanVals(Vector<Integer> indeces, Vector<Integer> vals, int val) {
		vals.add(val);
		int last = indeces.get(vals.size()-1);
		while (indeces.size() > vals.size()) {
			if (indeces.get(vals.size()-1) == last)
				vals.add(val);
			else break;
		}
	}

}
