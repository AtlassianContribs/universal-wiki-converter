package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles removing tikiwiki metadata from the tikiwiki file
 */
public class MetadataCleaner extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Cleaning Metadata - start");
		String input = page.getOriginalText();
		String converted = cleanMetadata(input);
		page.setConvertedText(converted);
		
		log.debug("Cleaning Metadata - complete");
	}
	
	String startFileMeta = "\\s*Date.*?(?=Content-Type)";
	String mimeMeta = "Content-Type(.*?)Content-Transfer-Encoding:[^\n]*\n"; 
	String mime2Meta = "--=_multipart_boundary";
	String newline = "\r?\n";
	String allmeta = 
		startFileMeta + 
		mimeMeta + 						//group 1 = metadata (used by MetadataTitle)
		newline + 			
		"(.*?)" + 						//group 1 = contents after metadata and before metadata separator
		"(?:" +
			"(?:"+mime2Meta+")|" +
			"(\\s*$)" +					//group 3 = whitespace until the end of the string
		")";
	Pattern allmetaPattern = Pattern.compile(allmeta, Pattern.DOTALL);
	/**
	 * removes tikiwiki metadata from the given tikiwiki input
	 * @param input tikiwiki input
	 * @return input without the metadata
	 */
	protected String cleanMetadata(String input) {
		Matcher allmetaFinder = allmetaPattern.matcher(input);
		if (allmetaFinder.lookingAt()) {
			String whitespace = allmetaFinder.group(3);
			if (whitespace == null) whitespace = "";
			return allmetaFinder.group(2) + whitespace;
		}
		return input;
	}

}
