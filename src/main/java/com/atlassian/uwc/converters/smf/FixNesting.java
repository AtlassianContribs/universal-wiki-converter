package com.atlassian.uwc.converters.smf;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class FixNesting extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertNesting(input);
		page.setConvertedText(converted);
	}

	protected String convertNesting(String input) {
		input = addMissingLi(input);
		TreeMap<Integer, String> start = getIndexes("<((li)|(ul)|(ol))>", input);
		TreeMap<Integer, String> end = getIndexes("<\\/((li)|(ul)|(ol))>", input);
		input = fixNesting(start, end, input);
		
		return input;
	}

	Pattern listsNoLI = Pattern.compile("" +
			"(?s)(<[uo]l>)(.*?)(?=<[uo]l>)");
	protected String addMissingLi(String input) {
		Matcher listFinder = listsNoLI.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (listFinder.find()) {
			String firstList = listFinder.group(1);
			String contents = listFinder.group(2);
			if (contents.contains("<li>")) continue;
			found = true;
			String replacement = firstList + "<li>" + contents;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			listFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected TreeMap<Integer, String> getIndexes(String tag, String input) {
		Pattern tagStart = Pattern.compile(tag);
		Matcher tagFinder = tagStart.matcher(input);
		TreeMap<Integer, String> indexes = new TreeMap<Integer, String>();
		while (tagFinder.find()) {
			indexes.put(tagFinder.start(), tagFinder.group());
		}
		return indexes;
	}
	
	private String fixNesting(TreeMap<Integer, String> start, TreeMap<Integer, String> end, 
			String input) {
		if (same(start, end)) return input;
		int diff = (start.size() - end.size());
		if (diff != 0) {
			input = addClosing(start, end, input);
		}
		return input;
	}

	protected boolean same(TreeMap a, TreeMap b) {
		return a.size() == b.size();
	}
	
	Pattern outerlist = Pattern.compile("[ou]l");

	protected String addClosing(TreeMap<Integer, String> start, TreeMap<Integer, String> end, String input) {
		//foreach key in start, note value
		TreeMap<Integer, String> combined = new TreeMap<Integer, String>();
		combined.putAll(start);
		combined.putAll(end);
		Vector<Integer> indexes = new Vector<Integer>();
		if (!combined.isEmpty())
			indexes.addAll(combined.keySet());
		HashMap<String, Integer> tree = new HashMap<String, Integer>();
		for (int i = 0; i < indexes.size(); i++) {
			int index = indexes.get(i);
			String assocTag = combined.get(index);
			int count = 0;
			String key = assocTag.replaceFirst("^<\\/?", "");
			key = key.replaceFirst(">$", "");
			if (tree.containsKey(key)) count = tree.get(key);
			if (assocTag.startsWith("</")) count--;
			else count++;
			tree.put(key, count);
		}
		Set<String> tags = tree.keySet();
		TreeSet<String> sortedTags = new TreeSet<String>(new NestingOrderComparator());
		sortedTags.addAll(tags);
		Set<Integer> ends = end.keySet();
		Vector<Integer> endsVec = new Vector<Integer>();
		endsVec.addAll(ends);
		int adjustment = 0;
		TreeSet<String> nonZeroTags = new TreeSet<String>();
		for (String tag : tree.keySet()) if (tree.get(tag) != 0) nonZeroTags.add(tag);
		for (String tag : sortedTags) {
			int problem = tree.get(tag);
			if (problem > 0) {
				boolean justadd = isJustAdd(nonZeroTags);
				if (justadd) {
					String addition = "</" + tag + ">";
					input += addition;
					adjustment += addition.length();
				}
				else { 
					//find the last ul or ol
					for (int i = endsVec.size()-1; i >=0 ; i--) {
						Integer index = (Integer) endsVec.get(i);
						if (outerlist.matcher(end.get(index)).find()) {
							String pre = input.substring(0, index+adjustment);
							String post = input.substring(index+adjustment);
							String addition = "</" + tag + ">";
							adjustment += addition.length();
							input = pre + addition + post;
							break;
						}
					}
				}
			}
			else if (problem < 0) {
				for (int i = endsVec.size()-1; i >=0 ; i--) {
					Integer index = (Integer) endsVec.get(i);
					if (end.get(index).contains(tag)) {
						String pre = input.substring(0, index+adjustment);
						String post = input.substring(index+adjustment);
						String find = "<\\/" + tag + ">";
						String newpost = post.replaceFirst(find, "");
						adjustment -= post.length() - newpost.length();
						input = pre + newpost;
						break;
					}
				}
			}
			//ignore 0
			nonZeroTags.remove(tag);
		}
		return input;
	}
	
	protected boolean isJustAdd(TreeSet<String> tags) {
		boolean hasOuter = false;
		for (String tag : tags) {
			if (outerlist.matcher(tag).find()) {
				hasOuter = true;
				break;
			}
		}
		return hasOuter;
	}

	public class NestingOrderComparator implements Comparator {

		public int compare(Object a, Object b) {
			//li has to come before ol or ul
			String s1 = (String) a;
			String s2 = (String) b;
			if (s1.contains("li")) return -1;
			if (s2.contains("li")) return 1;
			return 1;
		}
		
	}

}
