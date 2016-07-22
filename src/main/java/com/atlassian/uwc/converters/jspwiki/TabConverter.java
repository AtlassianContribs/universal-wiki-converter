package com.atlassian.uwc.converters.jspwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class TabConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertTabs(input);
		page.setConvertedText(converted);
	}

	Pattern tabbed = Pattern.compile("" +
			"(%%tabbedSection)" + //group 1 = set of tabs begins
			"|" +
			"(%%tab-(\\S+)(.*?)(?=\\/%))" + //group 2 = one tab. group3 = tabname. group 4 = tab content
			"|" +
			"(\\/%)", Pattern.DOTALL); //group 5 =  any closing delimiter
	protected String convertTabs(String input) {
		clearUnique();
		Matcher tabFinder = tabbed.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		boolean inDeck = false;
		boolean inCard = false;
		while (tabFinder.find()) {
			found = true;
			boolean endDelim = (tabFinder.group(5) != null) == true;
			if (endDelim && !inDeck && !inCard) continue;
			if (needsSetup(input)) {
				input = addSetup(input);
				tabFinder.reset(input);
				continue;
			}
			boolean startTabs = (tabFinder.group(1) != null) == true;
			boolean startCard = (tabFinder.group(2) != null) == true;
			String tabname = tabFinder.group(3);
			String tabcontents = tabFinder.group(4);
			String replacement = getTabPart(inDeck, inCard, startTabs, startCard, endDelim, tabname, tabcontents);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tabFinder.appendReplacement(sb, replacement);
			inDeck = setDeckState(inDeck, inCard, startTabs,startCard, endDelim);
			inCard = setCardState(inDeck, inCard, startTabs,startCard, endDelim);
		}
		if (found) {
			tabFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern setup = Pattern.compile("\\{composition-setup\\}");
	protected boolean needsSetup(String input) {
		Matcher setupFinder = setup.matcher(input);
		return !setupFinder.find();
	}
	
	Pattern firstTabbed = Pattern.compile(
			"(%%tabbedSection)"
			);
	protected String addSetup(String input) {
		Matcher firstFinder = firstTabbed.matcher(input);
		if (firstFinder.find()) {
			return firstFinder.replaceFirst("{composition-setup}\n%%tabbedSection");
		}
		return input;
	}
	
	protected String getTabPart(boolean inDeck, boolean inCard, boolean startTabs, boolean startCard, 
			boolean endDelim, String tabname, String tabcontents) {
		if (startTabs) {
			return "{deck:id=" +
					getUniqueId() +
					"}";
		}
		else if (startCard) {
			return "{card:label=" + tabname + "}" + tabcontents;
		}
		else if (endDelim && inCard) {
			return "{card}";
		}
		else if (endDelim && !inCard && inDeck) {
			return "{deck}";
		}
		log.error("Problem getting tab part. " +
				"inDeck = " + inDeck +
				" incard = " + inCard + 
				" startTabs = " + startTabs +
				" startCard = " + startCard +
				" endDelim = " + endDelim + 
				" tabname = " + tabname +
				" tabcontents = " + tabcontents); //shouldn't get here
		return "";
	}
	
	private int uniqueId = 0;
	private String getUniqueId() {
		return "" + ++uniqueId;
	}
	
	protected void clearUnique() {
		uniqueId = 0;
	}

	protected boolean setDeckState(boolean inDeck, boolean inCard, boolean startTabs, boolean startCard, boolean endDelim) {
		return startTabs || startCard || inCard;
	}
	protected boolean setCardState(boolean inDeck , boolean inCard, boolean startTabs, boolean startCard, boolean endDelim) {
		return (startCard || inCard) && !endDelim;
	}

}
