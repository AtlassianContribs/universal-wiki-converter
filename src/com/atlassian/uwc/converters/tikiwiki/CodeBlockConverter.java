package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class CodeBlockConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Code Block - starting");
		String input = page.getOriginalText();
		String converted = convertCodeBlock(input);
		page.setConvertedText(converted);
		log.debug("Converting Code Block - complete");
	}
	String codeblock = "\\{CODE\\(\\)\\}(.*?)\\{CODE\\}"; 
	Pattern codeblockPattern = Pattern.compile(codeblock, Pattern.DOTALL);
	protected String convertCodeBlock(String input) {
		Matcher codeFinder = codeblockPattern.matcher(input);
		String replacement = "{code}{group1}{code}"; 
		String converted =  RegexUtil.loopRegex(codeFinder, input, replacement);
		converted = handleWhitespace(converted);
		return converted;
	}
	
	String codeNewlines = 
		"(\\{code\\})" +	//{code} string
		"\\s*" +			//newlines, and any other whitespace
		"(.*?)" +			//the contents of the codeblock
		"\\s*" +			//newlines, and any other whitespace
		"(\\{code\\})";		//{code} string
	Pattern codeNLPattern = Pattern.compile(codeNewlines, Pattern.DOTALL);
	/**
	 * replaces all whitespace seperating the {code} blocks from the content
	 * with just one newline. Example:
	 * <br/>
	 * if input = <br/>
	 * {code} codeblock {code}
	 * <br/><br/>
	 * then return = <br/>
	 * {code}<br/>
	 * codeblock<br/>
	 * {code}
	 * @param input
	 * @return
	 */
	protected String handleWhitespace(String input) {
		Matcher codeNLFinder = codeNLPattern.matcher(input);
		String replacement = "{group1}\n{group2}\n{group3}";
		return RegexUtil.loopRegex(codeNLFinder, input, replacement);
	}

}
