package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Xwiki Info, Warning, and Error boxes to Confluence Info, Note, and Warning
 * boxes, respectively.
 */
public class BoxConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Info, Warning and Error boxes -- starting");
		String input = page.getOriginalText();
		String converted = convertBoxes(input);
		page.setConvertedText(converted);
		log.info("Converting Info, Warning and Error boxes -- complete");
	}
	
	Pattern box = Pattern.compile(
			"#([^)]+)" +		//#info 
			"\\(\"" +			//("
			"([^\"]+)" +		//... (group2)
			"\"\\)"				//")
			);
	protected String convertBoxes(String input) {
		Matcher boxFinder = box.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (boxFinder.find()) {
			found = true;
			String type = boxFinder.group(1); 		//info, warning, error
			String contents = boxFinder.group(2);
			String confType = getConfType(type); 	//info, note, warning
			String replacement = "{" + confType + "}\n" +
					contents + "\n" +
					"{" + confType + "}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			boxFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			boxFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	private String getConfType(String type) {
		if (type.equals("warning")) return "note";
		if (type.equals("error")) return "warning";
		return type; //info is the same
	}

}
