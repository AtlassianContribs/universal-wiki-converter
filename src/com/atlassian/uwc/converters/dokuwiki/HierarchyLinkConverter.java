package com.atlassian.uwc.converters.dokuwiki;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class HierarchyLinkConverter extends HierarchyTarget {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertLink(input, getCurrentPath(page));
		page.setConvertedText(converted);

	}

	Pattern link = Pattern.compile("(?<=\\[)\\[([^\\]]*)\\](?=\\])");
	Pattern onecolon = Pattern.compile("^([^:]*:)([^:]*)$");
	protected String convertLink(String input) {
		return convertLink(input, null);
	}
	protected String convertLink(String input, String currentPath) {
		Matcher linkFinder = link.matcher(input);
		String currentSpacekey = getProperties().getProperty("spacekey", null);
		Vector<String> allspaces = getSpaces();
		HashMap<String,String> namespaces = getDokuDirectories();
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String target = linkFinder.group(1);
			String alias = null;
			if (target.startsWith("\\\\")) continue; //UNC link
			if (target.contains("|")) {
				String[] parts = target.split("\\|");
				target = parts[0];
				alias = parts[1];
			}
			//remove any opening colons (:namespace:page)
			target = target.trim();
			if (!isExternal(target)) {
				if (target.startsWith(".")) {
					target = target.replaceAll("^[.]*", "");
					if (currentPath != null && !currentPath.equals(currentSpacekey)) { //need to add hierarchy in
						String pre = currentPath.replaceAll("\\/", ":");
						target = pre + ":" + target;
					}
				}
				if (target.startsWith(":")) target = target.replaceFirst(":", "");
				//figure out if we've already got the space represented
				String targetPart1 = target.replaceFirst(":.*$", "");
				boolean containsSpace = false;
				if (allspaces.contains(targetPart1)) 
					containsSpace = true;
				//get rid of unnecessary links to start 
				//(start page content will be moved to parent in DokuwikiHierarchy
				//unless the start page is a top level page in the space)
				if (!(containsSpace && (onecolon.matcher(target)).matches())) 
					target = target.replaceFirst(":start$", "");
				if (containsSpace) //remove the space from the target for now
					target = target.replaceFirst("\\Q"+targetPart1+"\\E:", "");
				String hierarchy = target; //save for later
				//get confluence page name and fix the case to match HierarchyTitleConverter
				target = target.replaceFirst("^.*:", ""); //remove everything to the last colon
				target = HierarchyTitleConverter.casify(target); //foo_bar becomes Foo Bar
				//fix collisions
				String linkSpacekey = currentSpacekey;
				if (!containsSpace && namespaces.containsKey(targetPart1)) {
					linkSpacekey = namespaces.get(targetPart1); 
				}
				if (containsSpace) linkSpacekey = targetPart1;
				target = fixCollisions(target, hierarchy, linkSpacekey);
				//underscores to spaces
				target = target.replaceAll("_", " ");
				//add spacekey to target if necessary
				if (!target.contains(":") || containsSpace) 
					target = linkSpacekey + ":" + target;
			}
			//build complete link
			String replacement = (alias == null)?
					target:
					alias.trim() + "|" + target;
			//replace
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern protocol = Pattern.compile("(https?:)|(ftp:)");
	private boolean isExternal(String target) {
		Matcher protocolFinder = protocol.matcher(target);
		return protocolFinder.lookingAt();
	}
}
