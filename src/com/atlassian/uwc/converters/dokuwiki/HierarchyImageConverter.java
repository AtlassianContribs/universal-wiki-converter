package com.atlassian.uwc.converters.dokuwiki;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class HierarchyImageConverter extends HierarchyTarget {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		//images will be attached in the DokuwikiHierarchy
		String input = page.getOriginalText();
		String converted = convertImages(input, getCurrentPath(page));
		page.setConvertedText(converted);
		
	}

	Pattern image = Pattern.compile("(?s)\\{\\{(.*?)\\}\\}");
	protected String convertImages(String input) {
		return convertImages(input, null);
	}
	Pattern size = Pattern.compile("([^?]*)\\?(.*)");
	Pattern params = Pattern.compile("(\\d+)(x(\\d+))?");
	protected String convertImages(String input, String currentPath) {
		String currentSpacekey = getProperties().getProperty("spacekey", null);
		Vector<String> allspaces = getSpaces();
		HashMap<String,String> namespaces = getDokuDirectories();
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String target = imageFinder.group(1);
			String alias = null;
			if (target.contains("|")) {
				String[] parts = target.split("\\|");
				target = parts[0];
				if (parts.length > 1) {
					alias = parts[1];
					if ("".equals(alias)) alias = null;
				}
			}
			String image = target.replaceFirst("^.*:", "");
//			remove any opening colons (:namespace:page)
			boolean root = false;
			if (target.startsWith(":")) {
				target = target.replaceFirst(":", "");
				root = true;
			}
			//figure out if we've already got the space represented
			String targetPart1 = target.replaceFirst(":.*$", "");
			boolean containsSpace = false;
			if (allspaces.contains(targetPart1)) 
				containsSpace = true;
			String linkSpacekey = null;
			if (!containsSpace && namespaces.containsKey(targetPart1)) {
				linkSpacekey = namespaces.get(targetPart1); 
			}
			if (linkSpacekey == null) {
				if (containsSpace) linkSpacekey = targetPart1;
				else linkSpacekey = currentSpacekey;
			}
			//get the image's location
			Pattern pagename = Pattern.compile("^(.*:)?(.+):\\Q"+image + "\\E$");
			Matcher pageFinder = pagename.matcher(target);
			String hierarchy = target.replaceFirst(":[^:]*$", "");
			String page = target;
			image = image.replaceAll("%", "_"); //this change happens at some point
			if (pageFinder.find()) {
				page = pageFinder.group(2);
				target = HierarchyTitleConverter.fixTitle(page) + "^" + image;
			}
			else if (root) page = "Start";
			//fix collisions
			String origpage = page;
			if (page.equals(linkSpacekey) && hierarchy.startsWith(linkSpacekey)) 
				page = "Start";
			if (!page.contains(".")) {
				String completepage = fixCollisions(page, hierarchy, linkSpacekey);
				target = target.replaceFirst("(?i)\\Q" + origpage + "\\E", completepage);
				if (target.equals(image)) target = completepage + "^" + image;
			}
			//no page found yet, so link to parent
			if (!target.contains("^") && currentPath != null) {
				page = currentPath.replaceAll(".*\\/(?=.)", "");
				page = page.replaceFirst("\\/$", "");
				target = page + "^" + target;
			}
			//add spacekey to target if necessary
			target = linkSpacekey + ":" + target;
			//handle resize info
			Matcher sizeFinder = size.matcher(target);
			boolean hasSizeParam = false;
			if (sizeFinder.find()) {
				hasSizeParam = true;
				image = sizeFinder.group(1);
				String paramString = sizeFinder.group(2);
				Matcher paramFinder = params.matcher(paramString);
				if (paramFinder.find()) {
					String width = paramFinder.group(1);
					String height = paramFinder.group(3);
					target = image + "|width=" + width + "px" +  
					(height != null?",height="+height + "px":"");
				}
			}
			//build replacement string
			String replacement = "";
			if (hasSizeParam || isImage(image)) {
				replacement = "!" + target + "!";
			}
			else {
				replacement = (alias == null)?
					target:
					alias + "|" + target;
				replacement = "[" + replacement + "]";
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	private boolean isImage(String image) {
		String ext = image.replaceFirst("^.*\\.", "");
		boolean isimage = false;
		String mimes = getMimetypes();
		Pattern p = Pattern.compile("\n(.*) \\Q" + ext + "\\E[ \t]*\n");
		Matcher m = p.matcher(mimes);
		if (m.find()) {
			String line = m.group(1);
			if (line.startsWith("image")) 
					isimage = true;
		}
		return isimage;
	}
	String mimetypes;
	private String getMimetypes() {
		if (mimetypes != null) return mimetypes;
		mimetypes = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("conf/mime.types"));
			while ((line = reader.readLine()) != null) {
				mimetypes += line + "\n";
			}
			reader.close();
		} catch (Exception e) {
			mimetypes = null;
			log.error("Could not read mime.types file");
			e.printStackTrace();
		}
		return mimetypes;
	}	

}
