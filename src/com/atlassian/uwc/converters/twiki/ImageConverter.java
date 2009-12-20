package com.atlassian.uwc.converters.twiki;

import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ImageConverter extends PropertyConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertImage(input);
		page.setConvertedText(converted);
	}

	protected String convertImage(String input) {
		input = convertHtmlImage(input);
		input = convertPlainImage(input);
		input = convertPlainLink(input);
		return input;
	}

	Pattern htmlImage = Pattern.compile("<img([^>]*?)src=\"([^\"]+)\"([^>]+)>");
	Pattern lastSlash = Pattern.compile("^(.*)\\/([^/]+)$");
	private String convertHtmlImage(String input) {
		Matcher htmlImageFinder = htmlImage.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (htmlImageFinder.find()) {
			found = true;
			String src = htmlImageFinder.group(2);
			Matcher slashFinder = lastSlash.matcher(src);
			if (slashFinder.find()) {
				src = slashFinder.group(1) + "^" + slashFinder.group(2);
			}
			String attributes = htmlImageFinder.group(1).trim() + " " + htmlImageFinder.group(3).trim();
			attributes = convertImageAttributes(attributes);
			String replacement = "!" + src + attributes + "!";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			htmlImageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			htmlImageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern attributes = Pattern.compile("" +
			"(\\w+=)[\"']?([^ \"']+)[\"' ] ?");
	private String convertImageAttributes(String input) {
		input = input.replaceAll("[/]\\s*$", "");
		input = input.trim();
		Matcher attFinder = attributes.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		boolean first = true;
		while (attFinder.find()) {
			found = true;
			String key = attFinder.group(1);
			String value = attFinder.group(2);
			String replacement = (first?"|":", ") + key + "\"" + value + "\"";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			attFinder.appendReplacement(sb, replacement);
			first = false;
		}
		if (found) {
			attFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern image = Pattern.compile("(?<!\\[)!?(http://[^|!\n]+[|]?[^!\n]+)!?");
	Pattern confpath = Pattern.compile("^([^/]+)\\/(.*)$");
	private String convertPlainImage(String input) {
		HashMap<String,String> variables = getVars();
		HashMap<String,String> removes = getRemovals();
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String src = imageFinder.group(1);
			String justSrc = src.replaceFirst("[|].*$", "");
			if (!hasExtension(justSrc)) continue;
			src = src.replaceAll("\\Q" + variables.get("url") + "\\E", "");
			src = src.replaceFirst("\\Q" + variables.get("puburl").replaceAll("~UWCTOKENURL~", ""), "");
			for (Iterator iter = removes.keySet().iterator(); iter.hasNext();) {
				String remove = removes.get((String) iter.next());
				src = src.replaceAll("\\Q" + remove + "\\E", "");
			}
			src = src.replaceFirst("\\?rev=\\d;filename=", "^");
			src = src.replaceFirst("^[/]", "");
			src = src.replaceFirst("[/]", ":");
			String replacement = "!" + src + "!";
			Matcher confpathFinder = confpath.matcher(src);
			if (confpathFinder.find()) {
				String page = confpathFinder.group(1);
				String file = confpathFinder.group(2);
				replacement = "!" + page + "^" + file + "!";
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
	
	Pattern imagelink = Pattern.compile("" +
			"(?<=\\[\\[)" +
			"([^\\]]+)" +
			"");
	private String convertPlainLink(String input) {
		HashMap<String,String> variables = getVars();
		HashMap<String,String> removes = getRemovals();
		Matcher imagelinkFinder = imagelink.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imagelinkFinder.find()) {
			found = true;
			String src = imagelinkFinder.group(1);
			if (!hasExtension(src)) continue;
			src = src.replaceAll("\\Q" + variables.get("url") + "\\E", "");
			src = src.replaceFirst("\\Q" + variables.get("puburl").replaceAll("~UWCTOKENURL~", ""), "");
			for (Iterator iter = removes.keySet().iterator(); iter.hasNext();) {
				String remove = removes.get((String) iter.next());
				src = src.replaceAll("\\Q" + remove + "\\E", "");
			}
			src = src.replaceFirst("\\?rev=\\d;filename=", "^");
			src = removeExtraSlashes(src);
			src = src.replaceFirst("[/]", ":");
			String replacement = src;
			Matcher confpathFinder = confpath.matcher(src);
			if (confpathFinder.find()) {
				String page = confpathFinder.group(1);
				String file = confpathFinder.group(2);
				replacement = page + "^" + file;
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imagelinkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imagelinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	
	Pattern extraSlashes = Pattern.compile("^(.*\\/)([^/]+\\/[^/]+\\/[^/]+)$");
	private String removeExtraSlashes(String input) {
		Matcher extraFinder = extraSlashes.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (extraFinder.find()) {
			found = true;
			String replacement = extraFinder.group(2);
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			extraFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			extraFinder.appendTail(sb);
			return sb.toString();
		}
		//else
		input = input.replaceFirst("^[/]", "");
		return input;
	}

	Pattern extPattern = Pattern.compile("\\.([^.]+)$");
	private boolean hasExtension(String src) {
		HashMap<String,String> extensions = getExtensions();
		Matcher extFinder = extPattern.matcher(src);
		boolean extfound = false;
		if (extFinder.find()) {
			String extension = extFinder.group(1);
			for (Iterator iter = extensions.keySet().iterator(); iter.hasNext();) {
				String extString = extensions.get((String) iter.next());
				String[] extArray = extString.split(",");
				for (int i = 0; i < extArray.length; i++) {
					String e = extArray[i];
					if (e.equals(extension)) extfound = true; 
				}
				if (extfound) break;
			}
		}
		return extfound;
	}
	
	HashMap<String, String> vars;
	private HashMap<String, String> getVars() {
		if (vars == null)
			vars = getPropsWithPrefix("vars-");
		return vars;
	}

	HashMap<String, String> ext;
	private HashMap<String, String> getExtensions() {
		if (ext == null)
			ext = getPropsWithPrefix("extensions-");
		return ext;
	}

	HashMap<String, String> removes;
	private HashMap<String, String> getRemovals() {
		if (removes == null)
			removes = getPropsWithPrefix("remove-twiki-path-");
		return removes;
	}

}
