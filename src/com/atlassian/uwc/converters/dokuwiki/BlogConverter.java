package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class BlogConverter extends HierarchyTarget {

	@Override
	public void convert(Page page) {
		//identify if this page is a blog
		if (namespaceIsBlog(page.getFile().getPath())) {
			page.setIsBlog(true);
		}
		//handle references to blog macro
		String input = page.getOriginalText();
		String converted = convertBlogMacro(input);
		page.setConvertedText(converted);

	}

	protected boolean namespaceIsBlog(String path) {
		String nsString = getProperties().getProperty("blog-namespaces", null);
		return namespaceIsBlog(path, nsString);
	}

	public static boolean namespaceIsBlog(String path, String nsPropString) {
		if (nsPropString == null) return false;
		String[] namespaces = nsPropString.split("::");
		for (String namespace : namespaces) {
			if (sameNamespace(path, namespace)) return true;
		}
		return false;
	}
	

	protected static boolean sameNamespace(String path, String namespace) {
		while (!"".equals(path)) {
			if (path.endsWith(namespace)) {
				return true;
			}
			if (!path.contains(File.separator)) 
				break; //break here if we can't find a spacekey for this dir
			//remove deepest portion of the path
			path = SpaceConverter.removeDeepest(path);
		}
		return false;
	}

	Pattern blogmacro = Pattern.compile("[{][{]blog>(.*?)[}][}]");
	Pattern macroargs = Pattern.compile("^([^?]+)[?](\\d+)");
	protected String convertBlogMacro(String input) {
		Matcher blogFinder = blogmacro.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (blogFinder.find()) {
			found = true;
			String args = blogFinder.group(1);
			Matcher argsFinder = macroargs.matcher(args);
			if (argsFinder.find()) {
				String ns = argsFinder.group(1);
				String max = argsFinder.group(2);
				ns = ns.replaceAll(":", File.separator);
				String spacekey = getSpacekey(ns);
				if (spacekey == null) {
					log.error("Spacekey could not be found for namespace: " + ns);
				}
				String replacement = getConfBlogMacro(max, spacekey);
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				blogFinder.appendReplacement(sb, replacement);
			}
		}
		if (found) {
			blogFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	

	public String getSpacekey(String namespace) {
		HashMap<String, String> directories = getDokuDirectories();
		String path = namespace;
		while (!"".equals(path)) {
			if (directories.containsKey(path)) {
				return directories.get(path);
			}
			else if (directories.containsKey(path.toLowerCase())) {
				return directories.get(path.toLowerCase());
			}
			if (!path.contains(File.separator)) 
				break; //break here if we can't find a spacekey for this dir
			//remove deepest portion of the path
			path = SpaceConverter.removeDeepest(path);
		}
		return null;
	}

	public String getConfBlogMacro(String max, String space) {
		return "" + 
				"<p>" +
				"<ac:macro ac:name=\"blog-posts\">" +
				"<ac:parameter ac:name=\"spaces\">" +
				space +
				"</ac:parameter>" +
				"<ac:parameter ac:name=\"reverse\">true</ac:parameter>" +
				"<ac:parameter ac:name=\"sort\">creation</ac:parameter>" +
				"<ac:parameter ac:name=\"max\">" +max+ "</ac:parameter></ac:macro></p>";
	}
}
