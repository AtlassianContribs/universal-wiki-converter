package com.atlassian.uwc.converters.dokuwiki;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class PrepRowSpansConverter extends BaseConverter {


	public static final String TOKENKEY = "UWCTOKENROWSPANS:";
	private static final String DELIM = "::";
	@Override
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = prep(input);
		page.setConvertedText(converted);
	}
	
	Pattern rowspan = Pattern.compile("(?<=^|\n)[|^](.*?):::(.*?)((\n(?=\\s*[^|^]))|$)", Pattern.DOTALL);
	protected String prep(String input) {
		Matcher rowspanFinder = rowspan.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (rowspanFinder.find()) {
			found = true;
			String table = rowspanFinder.group();
			//remove everything before the last table
			String pre = "";
			if (needToTrim(table)) {
				String parts[] = trimTable(table);
				if (parts.length > 1) {
					pre = parts[0];
					table = parts[1];
				}
			}
			//how many columns
			int numcols = getNumCols(table);
			//Look for a cell without the signal. Does the one following have it?
			for (int i = 0; i < numcols; i++) {
				//for each :::, if the one above is not :::, put the token?
				table = handleColumn(i, table);
			}
			table = RegexUtil.handleEscapesInReplacement(pre + table);
			rowspanFinder.appendReplacement(sb, table);
		}
		if (found) {
			rowspanFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern moreThanOneTable = Pattern.compile("[|^]\n([^|^]|\n).*?[|^]", Pattern.DOTALL);
	protected boolean needToTrim(String input) {
		Matcher finder = moreThanOneTable.matcher(input);
		return finder.find();
	}

	Pattern lastTable = Pattern.compile("^(.*?(?:(?:\n|[^|^])\n))([|^].*)", Pattern.DOTALL);
	protected String[] trimTable(String input) {
		Matcher lastFinder = lastTable.matcher(input);
		if (lastFinder.find()) {
			return new String[] {lastFinder.group(1),lastFinder.group(2)};
		}
		return new String[] {input};
	}

	String cellstring = "[|^][^|^]*";
	Pattern cell = Pattern.compile(cellstring);
	protected String handleColumn(int i, String input) {
		String pre = "(?<=^|\n)";
		while (i-- > 0) { pre += cellstring;} 
		Pattern p = Pattern.compile(pre+"("+cellstring+")");
		Matcher colFinder = p.matcher(input);
		int lastrow = -1;
		int length = 0;
		TreeMap<Integer, String> additions = new TreeMap<Integer, String>();
		while (colFinder.find()) {
			String cellcontents = colFinder.group(1);
			if (!cellcontents.contains(":::")) {
				if (length > 0) {
					additions.put(lastrow, DELIM + TOKENKEY + (length+1) + DELIM);
					length = 0;
				}
				lastrow = colFinder.end();
			}
			else {
				length++;
			}
		}
		if (length > 0) {
			additions.put(lastrow, DELIM + TOKENKEY + (length+1) + DELIM);
		}
		Set<Integer> keySet = additions.keySet();
		Vector<Integer> keys = new Vector<Integer>(keySet);
		if (keys.isEmpty()) return input;
		String output = "";
		int first = 0, last = 0;
		for (int index = 0; index < keys.size(); index ++ ) {
			last = keys.get(index);
			String part = input.substring(first, last);
			output += part + additions.get(last); 
			first = last;
		}
		output += input.substring(last);
		return output;
	}

	Pattern line = Pattern.compile("^[^\n]+", Pattern.MULTILINE);
	protected int getNumCols(String input) {
		Matcher lineFinder = line.matcher(input);
		while (lineFinder.find()) {
			String firstline = lineFinder.group();
			if (firstline.contains(PrepColSpansConverter.TOKENKEY)) continue;
			String[] cols = firstline.split("[|^]");
			return cols.length - 1;
		}
		return 0;
	}

}
