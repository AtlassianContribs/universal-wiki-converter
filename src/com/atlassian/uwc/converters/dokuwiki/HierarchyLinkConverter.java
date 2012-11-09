package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class HierarchyLinkConverter extends HierarchyTarget {

	private String origPageTitle;
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Link Conversion --- Start");
		String input = page.getOriginalText();
		origPageTitle = page.getName();
		String current = getCurrentPath(page);
		String spacekey = getSpacekey(page);
		log.debug("current: '" + current + "' spacekey: '" + spacekey + "'");
		String relativePath = getRelativePath(page);
		String converted = convertLink(input, current, spacekey, relativePath);
		page.setConvertedText(converted);
		log.debug("Link Conversion --- Completed");
	}
	
	public String getSpacekey(Page page) {
		if (page != null && page.getSpacekey() != null) {
			return page.getSpacekey();
		}
		return getProperties().getProperty("spacekey", null);
	}

	Pattern link = Pattern.compile("(?<=\\[)\\[([^\\]]*)\\](?=\\])");
	Pattern onecolon = Pattern.compile("^([^:]*:)([^:]*)$");
	protected String convertLink(String input) {
		return convertLink(input, null, getSpacekey(null), "");
	}
	protected String convertLink(String input, String currentPath, String spacekey, String pagepath) {
		Matcher linkFinder = link.matcher(input);
		String currentSpacekey = spacekey;
		Vector<String> allspaces = getSpaces();
		HashMap<String,String> namespaces = getDokuDirectories();
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String target = linkFinder.group(1);
			log.debug("link target orig: " + target);
			String alias = null;
			if (target.startsWith("\\\\")) continue; //UNC link
			if (target.contains("|")) {
				String[] parts = target.split("\\|");
				if (parts.length > 1) {
					target = parts[0];
					alias = parts[1];
				}
			}
			//FIXME anchors? we're not ready to transform these, so just get rid of the anchor part
			if (target.contains("#")) target = target.replaceAll("#[^|]*", "");
			log.debug("target: '" + target + "' and alias: '" + alias + "'");
			//remove any opening colons (:namespace:page)
			target = target.trim();
			if (!isExternal(target)) {
				if (target.startsWith(".")) {
					target = target.replaceAll("^[.]*", "");
					if (currentPath != null && !currentPath.equals(currentSpacekey)) { //need to add hierarchy in
						String pre = currentPath.replaceAll("\\/", ":");
//						log.debug("pre = " +pre);
						if (pre.endsWith(".txt")) {
							pre = pre.replaceFirst("[.]txt$", "");
							String sibling = target.replaceFirst(":[^:]+$", "");
//							log.debug("sibling = " +sibling);
							if (pre.endsWith(sibling)) {
								target = target.replaceFirst("^:[^:]+:", "");
							}
						}
						target = pre + ":" + target;
					}
				}
				if (target.startsWith(":")) target = target.replaceFirst(":", "");
				//figure out if we've already got the space represented
				String targetPart1 = target.replaceFirst(":[^:]+$", "");
				boolean containsSpace = false;
//				log.debug("targetPart1 = " + targetPart1);
				String nsFromTarget = findNSFromTarget(namespaces, target);
				if (nsFromTarget != null) {
					targetPart1 = nsFromTarget;
				}
				if (allspaces.contains(targetPart1)) 
					containsSpace = true;
//				log.debug("--LinkConverter");
//				log.debug("targetPart1 =" + targetPart1);
				//get rid of unnecessary links to start 
				//(start page content will be moved to parent in DokuwikiHierarchy
				//unless the start page is a top level page in the space)
				boolean isOne = (onecolon.matcher(target)).matches();
				if (!(containsSpace && isOne)) 
					target = target.replaceFirst(":start$", "");
				String hierarchy = target; //save for later
				//is there a meta title to be used?
//				log.debug("pagepath = " + pagepath);
				String origMetaFilename = getMetaFilename(pagepath, ".meta");
				String metaFilename = origMetaFilename;
//				log.debug("isOne = " + isOne + ", target = " + target + ", metaFilename = " + metaFilename);
				metaFilename = getTargetMetaFilename(target, metaFilename, isOne);
//				log.debug("metaFilename = " + metaFilename);
				String metatitle = HierarchyTitleConverter.getMetaTitle(metaFilename);
				log.debug("metatitle = " + metatitle);
				//get confluence page name and fix the case to match HierarchyTitleConverter
				if (metatitle == null || "".equals(metatitle)) 
					target = target.replaceFirst("^.*:", ""); //remove everything to the last colon
				else //title was set with metadata
					target = metatitle;
				target = HierarchyTitleConverter.fixTitle(target); //foo_bar becomes Foo Bar
				//fix collisions
				String linkSpacekey = currentSpacekey;
				targetPart1 = targetPart1.replaceAll(":+", File.separator);
//				log.debug("containsSpace: " + containsSpace + ", " +
//						"ns: "+ namespaces.containsKey(targetPart1)
//						+", tp1: '" + targetPart1+"'");
				if (!containsSpace && namespaces.containsKey(targetPart1)) {
					linkSpacekey = namespaces.get(targetPart1); 
					log.debug("linkSpacekey = " + linkSpacekey);
				}
				if (containsSpace) linkSpacekey = targetPart1;
				target = fixCollisions(target, hierarchy, linkSpacekey, metaFilename);
				//underscores to spaces
				target = target.replaceAll("_", " ");
				//add spacekey to target if necessary
				if (!target.contains(":") || containsSpace) 
					target = linkSpacekey + ":" + target;
				blogLinkReport(namespaces, targetPart1, origMetaFilename, spacekey, target);
				log.debug("link target = " + target);
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

	protected String findNSFromTarget(HashMap<String, String> namespaces, String target) {
		target = target.replaceAll(":", "/");
		while (!"".equals(target)) {
			if (namespaces.containsKey(target)) { return target; }
			if (target.contains("/")) target = target.replaceAll("[/][^/]*$", "");
			else return null;
		}
		return null;
	}
	
	
	private void blogLinkReport(HashMap<String, String> namespaces,
			String ns, String pageMetaFilename,
			String spacekey, String linktarget) {
		if (namespaces.containsKey(ns)) {
			String nsString = getProperties().getProperty("blog-namespaces", null);
			if (BlogConverter.namespaceIsBlog(ns, nsString)) {
				String pagetitle = HierarchyTitleConverter.getMetaTitle(pageMetaFilename);
				if (pagetitle == null || "".equals(pagetitle)) 
					pagetitle = origPageTitle;
				pagetitle = HierarchyTitleConverter.fixTitle(pagetitle); 
				log.info("Blog Link Report - page: '" + pagetitle + "' " +
						"in space: '" + spacekey + "' " +
						"linking to: '" + linktarget + "'");
			}
		}
		
	}

	protected String getTargetMetaFilename(String target, String metaFilename, boolean isOne) {
		target=target.replaceAll(":+", File.separator);
		if (!target.startsWith(File.separator)) target = File.separator + target;
		if (!isOne) {
			String metadir = getProperties().getProperty("meta-dir", null);
			return metadir + target + ".meta";  
		}
		Matcher metaFinder = metaFile.matcher(metaFilename);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		if (metaFinder.find()) {
			found = true;
			String replacement = target + metaFinder.group(3);
			
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			metaFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			metaFinder.appendTail(sb);
			return sb.toString();
		}
		return null;
	}

	Pattern protocol = Pattern.compile("(https?:)|(ftp:)");
	private boolean isExternal(String target) {
		Matcher protocolFinder = protocol.matcher(target);
		return protocolFinder.lookingAt();
	}
}
