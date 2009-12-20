package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class PostListItemConverter extends BaseConverter{

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converter Post List Items - start");
		String input = page.getOriginalText();
		String converted = convertPostListItems(input);
		page.setConvertedText(converted);
		log.info("Converter Post List Items - complete");
	}
	String listChars = "*#";
	String postListItems = 
		"(?<=" +		//zero-width look behind
			"^|\n" +	//beginning of string or newline
		")" +			//end zero-width group
		"(" +			//start capturing (group 1)
			"["+		
				listChars+	//a list char
			"]" +		
			"[^\n]+" +	//anything but a newline until
			"\n" +		//a newline
		")" +			//end capture (group 1)
		"(" +			//start capture (group 2)
			"[^"+
				listChars	//not a list char
			+"]" +	
		")";			//end capture
	Pattern postPattern = Pattern.compile(postListItems);
	/**
	 * converts a special case of post list context. 
	 * @param input Example: <br/>
	 * * item a<br/>
	 * * item b<br/>
	 * not a list item
	 * @return input but with an extra newline between the list and the "not a list item" line, 
	 * so that the indentation is correct
	 */
	protected String convertPostListItems(String input) {
		Matcher postFinder = postPattern.matcher(input);
		boolean found = false;
		StringBuffer sb = new StringBuffer();
		while (postFinder.find()) {
			found = true;
			String listParts = postFinder.group(1); //ends with a newline
			String postItem = postFinder.group(2);
			if ("\n".equals(postItem)) continue; 	//don't need to bother if postItem's just a newline
			String replacement = listParts + "\n" + postItem; //add the extra newline here
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			postFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			postFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

}
