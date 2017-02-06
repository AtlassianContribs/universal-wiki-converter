package com.atlassian.uwc.converters.dokuwiki;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ListConverter extends BaseConverter {
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertList(input);
		page.setConvertedText(converted);
	}

	Pattern dokulist = Pattern.compile("" +
			"(?<=\n|^)( +)([*-])[ ]?");
	Pattern listend = Pattern.compile("" +
			"(?m)^[^ ][^- *]*");
	protected String convertList(String input) {
		Matcher listFinder = dokulist.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		String lastdelim = "";
		Vector<Integer> depths = new Vector<Integer>();
		int lastdepth = 0;
		int laststart = 0;
		while (listFinder.find()) {
			found = true;
			String ws = listFinder.group(1);
			String delim = listFinder.group(2);
			int start = listFinder.start();
			if (delim.equals("-")) delim = "#"; //switch to confluence ordered delim
			
			//depth state
			if (start > 0) { //make sure we don't need to clean up the depths state
				String between = input.substring(laststart, start);
				Matcher notlistFinder = listend.matcher(between);
				if (notlistFinder.find()) {
					depths.clear();
					lastdepth = 0;
					lastdelim = "";
				}
			}
			int depth = ws.length(); //each dokuwiki depth is a set a of two spaces
			//note the depths that have happened so far, so we can properly remove delims later
			if (depths.isEmpty()) depths.add(depth);
			else if (depth > depths.lastElement()) depths.add(depth);
			
			//depth increased: add delim
			if (depth > lastdepth)   
				lastdelim += delim;
			//depth is the same: replace last char with delim
			else if (depth == lastdepth) 
				lastdelim = lastdelim.substring(0,lastdelim.length()-1) + delim;
			//depth is less: figure out how much less and replace last with delim
			else {
				try {
					int level = lastdelim.length()-1; //level = confluence; depth = dokuwiki
					for (int i = level; i > 0; i--) {
						int maxdepth = depths.get(i);
						int mindepth = depths.get(i-1);
						if (depth >= mindepth && depth < maxdepth)  {
							lastdelim = lastdelim.substring(0, i); //figure out correct length
							//replace last with delim
							if (lastdelim.length() > 1)  
								lastdelim = lastdelim.substring(0, lastdelim.length()-1) + delim;
							else
								lastdelim = delim;
						}
						//else if depth == maxdepth, lastdelim is unchanged
					}
				} catch (ArrayIndexOutOfBoundsException e) { 
					// the list syntax had errors in it, wasn't consistent, etc.
					// so we'll just use the lastdelimiter as a best guess and continue forward
					// which means we do nothing here
				}
			}
			String replacement = lastdelim + " ";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			listFinder.appendReplacement(sb, replacement);
			lastdepth = depth;
			laststart = start;
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
