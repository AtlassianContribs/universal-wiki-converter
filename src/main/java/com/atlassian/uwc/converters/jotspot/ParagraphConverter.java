package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class ParagraphConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Paragraphs - starting");
		
		String input = page.getOriginalText();
		
		String converted = convertPara(input);
		
		page.setConvertedText(converted);
		log.info("Converting Paragraphs - complete");
	}
	
	Pattern paragraph = Pattern.compile("<p>(.*?)</p>", Pattern.DOTALL);
	private String convertPara(String input) {
		Matcher paraFinder = paragraph.matcher(input);
		if (paraFinder.find()) {
			String contents = paraFinder.group(1);
			return paraFinder.replaceAll("$1\n");
		}
		return input;
	}

}
