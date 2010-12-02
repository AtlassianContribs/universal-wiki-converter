package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class EnforceTableMinimumConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertTableMinimums(input);
		page.setConvertedText(converted);
	}

	Pattern table = Pattern.compile("(?s)\\{\\|(.*?)\\|\\}");
	Pattern header = Pattern.compile("(?s)^(.*?\\|-\\s*\n)(!.*?)(?=\\|-)");
	Pattern cells = Pattern.compile("(?s)\\|-\\s*\n(\\|.*?)(?=(?:\\|-)|$)");
	
	protected String convertTableMinimums(String input) {
		Matcher tableFinder = table.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableFinder.find()) {
			found = true;
			String table = tableFinder.group(1);
			Matcher headerFinder = header.matcher(table);
			String header = null;
			String pre = "";
			if (headerFinder.find()) {
				pre = headerFinder.group(1);
				header = headerFinder.group(2);
			}
			else continue;
			String replacement = "{|" + pre + header ;
			int min = getMin(header);
			log.debug("min = " + min);
			Matcher cellFinder = cells.matcher(table);
			//seperate by lines
			boolean foundcell = false;
			while (cellFinder.find()) {
				foundcell = true;
				String row = cellFinder.group(1);
				//determine if we need more columns
				int numCells = getNumCells(row);
				if (numCells < min) {
					//add more columns, as necessary
					row = addCells(row, numCells, min);
					replacement += "|-\n" + row;
				}
				else replacement += "|-\n" + row;
			}
			if (!foundcell) { //edge case - ignore
				continue;
			}
			replacement = replacement + "\n|}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected String addCells(String line, int numCells, int min) {
		for (int i = numCells; i < min; i++) {
			line += "| \n";
		}
		return line;
	}

	protected int getMin(String input) {
		String delim = "!";
		return getNum(input, delim);
	}

	private int getNum(String input, String delim) {
		Pattern p = Pattern.compile(delim);
		int num = 0;
		Matcher numFinder = p.matcher(input);
		while (numFinder.find()) {
			num++;
		}
		return num;
	}

	protected int getNumCells(String input) {
		return getNum(input, "\\|");
	}


}
