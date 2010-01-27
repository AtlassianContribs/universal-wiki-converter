package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.ui.Page;

public class NamespaceCleaner extends BaseConverter {

	private static final String IMAGE_NAMESPACE = "Image";
	Logger log = Logger.getLogger(this.getClass());
	Pattern imagePatternNoCase = Pattern.compile(IMAGE_NAMESPACE, Pattern.CASE_INSENSITIVE);
	
	public void convert(Page page) {
		log.debug("Removing Namespace references - starting");

		String input = page.getOriginalText();
		String converted = cleanNamespace(input);
		page.setConvertedText(converted);
		
		log.debug("Removing Namespace References - complete");
	}

	protected String cleanNamespace(String input) {
		String converted = input;
		
		String regex = "\\[\\[" +	//opening [[ link delimiter
				"(" +				//everything between link delimiters (group 1)
				"([^:\\]]*)" +		//namespace, not colons not closing link delimiter (group 2)
				"(:+[^:\\]]*)" +	//colon followed by non-colon, non-bracket chars (group 3)
				"+)" +				//end of group 1
				"\\]\\]";			//closing ]] link delimiters

		Pattern linkWithNamespace = Pattern.compile(regex);
		Matcher namespaceFinder = linkWithNamespace.matcher(input);

		StringBuffer sb = new StringBuffer();
		while (namespaceFinder.find()) {
		
			String link = namespaceFinder.group();
			
			String namespace = namespaceFinder.group(2);
			if (isImage(namespace)||isExternal(namespaceFinder.group(1) + namespace))
				continue;
			
			Pattern colons = Pattern.compile(":\\s*");
			Matcher colonFinder = colons.matcher(link);
			if (colonFinder.find()) {
				int numUS = colonFinder.group().length() + 1; //uwc-270
				String us = "";
				while (numUS-- > 0) { us += "_"; }
				link = colonFinder.replaceAll(us);
			}

			namespaceFinder.appendReplacement(sb, link);
		}
		namespaceFinder.appendTail(sb);
		converted = sb.toString();
		return converted;
	}

	private boolean isExternal(String link) {
		IllegalLinkNameConverter linkConverter = new IllegalLinkNameConverter();
		return linkConverter.isExternalLink(link);
	}

	private boolean isImage(String namespace) {
		Matcher imageFinder = imagePatternNoCase.matcher(namespace);
		return imageFinder.find();
	}

}
