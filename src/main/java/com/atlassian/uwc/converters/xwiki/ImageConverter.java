package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Xwiki Image Syntax to Confluence Image Syntax.
 */
public class ImageConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Image Syntax -- starting");
		String input = page.getOriginalText();
		String converted = convertImages(input);
		converted = adjustSurroundingWS(converted);
		page.setConvertedText(converted);
		log.info("Converting Image Syntax -- complete");
	}
	
	Pattern image = Pattern.compile("" +
			"\\{image:" +
			"([^}]+)" +
			"\\}"
			);
	protected String convertImages(String input) {
		String parent = findParentPage(input);
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String contents = imageFinder.group(1);
			String replacement = "";
			if (hasParameters(contents)) {
				String[] imageParts = contents.split("\\|");
				String image = imageParts[0];
				String confParams = "";
				//create a confluence parameter string
				for (int i = 1; i < imageParts.length; i++) {
					String param = imageParts[i];
					image = addPageOrSpace(image, parent, param);	
					param = createConfluenceParam(i, param);
					if ("".equals(param)) continue; //some xwiki parameters are ignored
					if (!"".equals(confParams))  	//not first - so add confluence delim
						confParams += ",";
					confParams += param;			//add confluence parameter
					//some images come from other pages
				}
				if (!"".equals(confParams)) //if there are confluence parameters, add them
					contents = image + "|" + confParams;
				else						//otherwise just use the image
					contents = image;
			}
			contents = RegexUtil.handleEscapesInReplacement(contents);
			replacement = "!" + contents + "!";
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern parent = Pattern.compile("<parent>(.*?)<\\/parent>");
	protected String findParentPage(String input) {
		Matcher parentFinder = parent.matcher(input);
		String parent = null;
		if (parentFinder.find()) {
			parent = parentFinder.group(1);
		}
		return parent;
	}
	
	protected String addPageOrSpace(String image, String parent, String param) {
		if (param.startsWith("document=")) {
			String[] parts = param.split("=");
			String page = parts[1];
			page = LinkConverter.fixSpaceSyntax(page);
			return page + "^" + image;
		}
		else if (param.startsWith("fromIncludingDoc=")) {
			parent = LinkConverter.fixSpaceSyntax(parent);
			return parent + "^" + image;
		}
		return image;
	}

	protected String createConfluenceParam(int i, String param) {
		param = param.trim();
		if (!param.contains("=")) { //no key = width or height
			switch(i) {
			case 1: //first one
				return (!"".equals(param))?"height=" + param:"";
			default:
				return (!"".equals(param))?"width=" + param:"";
			}
		}
		//has a key
		String[] parts = param.split("=");
		String key = parts[0];
		if (key.equals("link")) return "";
		if (key.equals("document")) return "";
		if (key.equals("fromIncludingDoc")) return "";
		return param;
	}

	private boolean hasParameters(String contents) {
		return contents.contains("|");
	}

	String before = "(\\S)(![^\n!]+!)";
	String after = "(![^\n!]+!)(\\S)";
	protected String adjustSurroundingWS(String input) {
		input = RegexUtil.loopRegex(input, before, "{group1} {group2}");
		input = RegexUtil.loopRegex(input, after, "{group1} {group2}");
		return input;
	}


}
