package com.atlassian.uwc.converters.socialtext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class CombineMultipleWorkspacesConverter extends BaseConverter {
	private static final String DEFAULT_DELIM = "-";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String input = page.getOriginalText();
		String append = getNewTitle(page);
		String converted = convertThisSpaceLinks(input, append);
		converted = convertOtherSpaceLinks(converted);
		converted = convertImageLinks(converted, append);
		converted = convertMacros(converted, append);
		page.setConvertedText(converted);
		if (!isExempt(page))
			page.setName(page.getName() + append);
	}

	protected boolean isExempt(Page page) {
		return isExempt(page.getName());
	}
	protected boolean isExempt(String pagetitle) {
		//included?
		String includeRegex = getProperties().getProperty("many2one-include", null);
		boolean isIncludePagetitle = true; //if no include prop, then everything is included
		if (includeRegex != null) {
			Pattern p = Pattern.compile(includeRegex);
			Matcher m = p.matcher(pagetitle);
			isIncludePagetitle = m.find();
		}
		//exempt?
		String exemptRegex = getProperties().getProperty("many2one-exemption", null);
		boolean isExemptPagetitle = false; //excluded pages are always excluded
		if (exemptRegex != null) {
			Pattern p = Pattern.compile(exemptRegex);
			Matcher m = p.matcher(pagetitle);
			isExemptPagetitle = m.find();
		}

		//it's exempt if: not explicitly included, explictly exempted, or it's external 
		return !isIncludePagetitle || isExemptPagetitle || isExternal(pagetitle);
	}

	private boolean isExternal(String input) {
		String protocol = getProperties().getProperty("customprotocol", 
				"^(https?:\\/\\/)|(ftp:\\/\\/)|(mailto:)");
		Pattern p = Pattern.compile(protocol);
		Matcher m = p.matcher(input);
		return m.find();
	}

	Pattern workspacename = Pattern.compile("" +
			"[/\\\\]" +
			"([^/\\\\]*)" +
			"[/\\\\][^/\\\\]*$");
	
	protected String getNewTitle(Page page) {
		String path = page.getPath();
		Matcher workspaceFinder = workspacename.matcher(path);
		if (workspaceFinder.find()) {
			String delim = getDelim();
			return delim + workspaceFinder.group(1);
		}
		return "";
	}

	Pattern link = Pattern.compile("(?<=\\[)([^:\\]]*)(?=\\])");
	protected String convertThisSpaceLinks(String input, String titleappend) {
		Matcher linkFinder = link.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String contents = linkFinder.group(1);
			String anchor = "";
			String file = "";
			String alias = "";
			if (contents.contains("#")) {
				String[] parts = contents.split("#");
				contents = parts[0];
				anchor = "#" + parts[1];
			}
			String page = contents;
			if (contents.contains("|") && !contents.endsWith("|")) {
				String[] parts = contents.split("\\|");
				page = parts[1];
			}
			if (contents.contains("^")) {
				String[] parts = contents.split("\\^");
				page = parts[0];
				file = "^" + parts[1];
				file = file.replaceAll("\\[", "%5B");
				file = file.replaceAll("\\]", "%5D");
			}
			if (contents.contains("|")) {
				String[] parts = contents.split("\\|");
				alias = parts[0] + "|";
				if (parts.length > 1)
					contents = parts[1];
			}
			if ("".equals(page)) continue;
			if (isExempt(page)) continue;
			String title = contents.replaceAll("^[^|]*\\|", "");
			if (!title.equals("")) contents = alias + page + titleappend;
			String replacement = contents + anchor + file;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern spacelinks = Pattern.compile("(?<=\\[)([^:\\]]*):([^\\]]*)(?=\\])");
	protected String convertOtherSpaceLinks(String input) {
		Matcher spacelinksFinder = spacelinks.matcher(input);
		String delim = getDelim();
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (spacelinksFinder.find()) {
			found = true;
			String spaceandalias = spacelinksFinder.group(1);
			String page = spacelinksFinder.group(2);
			String space = spaceandalias;
			String alias = ""; //default is no alias
			String anchor = ""; //default is no anchor
			String file = ""; //default is no file
			if (spaceandalias.contains("|")) {
				String[] parts = spaceandalias.split("\\|");
				alias = parts[0] + "|";
				space = parts[1];
			}
			if (page.contains("#")) {
				String[] parts = page.split("#");
				page = parts[0];
				anchor = "#" + parts[1];
			}
			if (page.contains("^")) {
				String[] parts = page.split("\\^");
				page = parts[0];
				file = "^" + parts[1];
				file = file.replaceAll("\\[", "%5B");
				file = file.replaceAll("\\]", "%5D");
			}
			if (isExternal(space + ":" + page)) { //oops - it was an external link. Fix.
				page = space + ":" + page;
				space = delim = "";  
			}
			if (isExempt(page)) space = delim = ""; //ignore space if we're exempting
			String replacement = alias + page + delim + space + anchor + file;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			spacelinksFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			spacelinksFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern image = Pattern.compile("(?<=[!])([^!\\[\\]]*)(?=[!])");
	private String convertImageLinks(String input, String append) {
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String content = imageFinder.group(1);
			if (content.matches("\\s*")) continue;
			String space = "";
			String page = "";
			String localappend = append;
			String delim = getDelim();
			if (content.contains(":")) {
				String[] parts = content.split(":");
				space = parts[0];
				content = parts[1];
			}
			String file = content; //default
			if (!content.startsWith("^") && content.contains("^")) {
				String[] parts = content.split("\\^");
				page = parts[0];
				file = "^" + parts[1];
			}
			if (page.equals("")) continue;
			if (isExempt(page)) space = delim = "";
			if (!"".equals(space)) localappend = delim + space;
			String replacement = page + localappend + file; 
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern include = Pattern.compile("(?<=\\{include:)(\\s*)([^}]*)(?=\\})");
	Pattern recent = Pattern.compile("(?<=\\{recent_changes:)([^}]*)(?=\\})");
	Pattern linkall = Pattern.compile("(?<=\\[)([^\\]]*)(?=\\])");
	protected String convertMacros(String input, String append) {
		Matcher includeFinder = include.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (includeFinder.find()) {
			found = true;
			String ws = includeFinder.group(1);
			String contents = includeFinder.group(2);
			String delim = getDelim();
			String space = "";
			String pagename = "";
			String localappend = append;
			Matcher linkFinder = linkall.matcher(contents);
			if (linkFinder.find() ) {
				pagename = linkFinder.group(1);
			}
			if (!contents.startsWith("[")) { //no space
				space = contents.replaceAll("\\[.*", "").trim();
				localappend = delim + space;
			}
			
			String replacement = ws + "[" + pagename + localappend + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			includeFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			includeFinder.appendTail(sb);
			input = sb.toString();
		}
		
		Matcher recentFinder = recent.matcher(input);
		sb = new StringBuffer();
		found = false;
		while (recentFinder.find()) {
			found = true;
			String contents = recentFinder.group(1);
			if (contents.matches("\\s*")) continue;
			String replacement = " ";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			recentFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			recentFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


	private String getDelim() {
		String delim = " " + getProperties().getProperty("many2one-delimiter", DEFAULT_DELIM) + " ";
		return delim.replaceAll("[ ]{2,}", " "); //don't allow multiple whitespaces
	}

}
