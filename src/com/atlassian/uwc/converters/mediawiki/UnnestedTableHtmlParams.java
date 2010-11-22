package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class UnnestedTableHtmlParams extends BaseConverter {

	private Pattern tableparam = Pattern.compile("<((?:table)|(?:tr)|(?:td)) (\\w[^>]+)>");

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertTableParams(input);
		page.setConvertedText(converted);
	}

	protected String convertTableParams(String input) {
		Matcher tableparamFinder = tableparam .matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tableparamFinder.find()) {
			found = true;
			String tag = tableparamFinder.group(1);
			String params = tableparamFinder.group(2);
			String convertedParams = convertParams(params);
			String replacement = "{"+tag+convertedParams+"}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			tableparamFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tableparamFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern background = Pattern.compile("background:([^;\"]+)");
	protected String convertParams(String input) {
		String[] params = input.split(" ");
		String converted = "";
		for (String param : params) {
			if (!param.contains("=")) continue; 
			if (!"".equals(converted)) converted += "|";//add delim
			String[] parts = param.split("=");
			String key = parts[0];
			String val = parts[1];
			val = val.replaceAll("\"", "");
			if (key == null) continue;
			key = key.toLowerCase();
			if (key.equals("width") || key.equals("height")) 
				converted += key + "=" + val + "px";
			else if (key.equals("style")) {
				Matcher bgFinder = background.matcher(val);
				if (bgFinder.find()) {
					val = bgFinder.group(1);
					converted += "bgcolor=" + getColor(val);
				}
			}
			else converted += key + "=" + val;
		}
		if (!"".equals(converted)) converted = ":" + converted;
		return converted;
	}

	protected String getColor(String input) {
		if (input == null) return "";
		if (input.startsWith("#")) return input;
		if ("red".equals(input)) return "#ff0000";
		if ("green".equals(input)) return "#00ff00";
		if ("yellow".equals(input)) return "#ffff00";
		if ("grey".equals(input)) return "#999999";
		if ("blue".equals(input)) return "#0000ff";
		if ("lime".equals(input)) return "#aadd00";
		if ("white".equals(input)) return "#ffffff";
		if ("dodgerblue".equals(input)) return "#1e90ff";
		else return "#ffffff";
	}

}
