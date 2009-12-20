
package com.atlassian.uwc.converters.swiki;


import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Converts links from swiki syntax into confluence syntax
 * @author Kelly Meese
 * 
 * This class was based off the jotspot LinkConverter created by Laura Kolker.
 */
public class LinkConverter extends BaseConverter
{
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page)
	{
		log.info("Converting Links -- starting");
		String input = page.getOriginalText();
		log.debug("Link input = " + input);
		
		page.setConvertedText(convertLinks(input));
		log.info("Converting Links -- completed");
	}
	/**
	 * Converts swiki link syntax to confluence link syntax.
	 * 
	 * The converter will convert both internal and external links.
	 * External link will convert from *Alias>http://someurl.com* to [Alias|http://someurl.com]
	 * Internal link will convert from *Link* to [Link]
	 * @author Kelly Meese
	 * 
	 * @param pageInput
	 * @return String
	 */
	public String convertLinks(String pageInput)
	{
		Pattern pattern = null;
		Matcher matcher = null;
		StringBuffer sb = new StringBuffer();
		Boolean found = false;
		String pageOut = pageInput;
		
		// Find the external links e.g. *alias>http://somewhere.com/index.html/*
		// and internal links *someinternallink*
		//pattern = Pattern.compile("\\*(?:\\\\.|[^\\*\\\\])*\\*");
		pattern = Pattern.compile("\\*+\\+?([^\\*\\r\\n]*)\\+?\\*");
		matcher = pattern.matcher(pageInput);
		while(matcher.find())
		{
			found = true;
			String link = matcher.group();
			String converted = link.replaceFirst("\\*+\\+?\\s*", "[");
			if (converted.charAt(1) == '@' )
				converted=converted.replaceFirst("@", "#");
			converted = converted.replaceAll("\\+?\\*$", "]");
			converted=converted.replaceAll(">@", "|#");
			converted = converted.replaceAll(">", "|");
			matcher.appendReplacement(sb, converted);
		}
		matcher.appendTail(sb);

		if(found)
			pageOut = sb.toString();
		
		return pageOut;
	}
}

