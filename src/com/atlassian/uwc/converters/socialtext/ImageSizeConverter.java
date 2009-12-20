package com.atlassian.uwc.converters.socialtext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Transforms socialtext image size parameters to comparable confluence
 * inline image size parameters.
 */
public class ImageSizeConverter extends BaseConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertImageSize(input);
		page.setConvertedText(converted);
	}

	Pattern image = Pattern.compile("" +
			"[!]" +
			"([^!]+)" +
			"[!]");
	Pattern size = Pattern.compile("^(.*) size=(\\w+)(.*)$");
	protected String convertImageSize(String input) {
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String contents = imageFinder.group(1);
			Matcher sizeFinder = size.matcher(contents);
			if (!sizeFinder.find()) continue;
			String pre = sizeFinder.group(1);
			String sizeValue = sizeFinder.group(2);
			String post = sizeFinder.group(3);
			String newValue = convertValue(sizeValue);
			String replacement = "!" + pre + "|" + newValue + post + "!";
			if (pre.endsWith("bmp") && newValue.equals("thumbnail"))
				replacement = "!" + pre + post + "!";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern digits = Pattern.compile("^\\d+");
	/**
	 * 
	 * @param input scaled, large, medium, small, or a number
	 * @return confluence image param corresponding with the given socialtext image param,
	 * or "" empty string if an unexpected input is used
	 */
	protected String convertValue(String input) {
		if ("scaled".equals(input)) return "thumbnail";
		else if ("large".equals(input)) return "width=50%";
		else if ("medium".equals(input)) return "width=20%";
		else if ("small".equals(input)) return "width=5%";
		Matcher digitFinder = digits.matcher(input);
		if (digitFinder.find()) {
			String pixels = digitFinder.group();
			return "width=" + pixels + "px";
		}
		return "";
	}

}
