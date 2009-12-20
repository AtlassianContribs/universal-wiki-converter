package com.atlassian.uwc.converters.socialtext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Transforms socialtext workspaces to confluence spacekeys using
 * config options in the converter properties file. 
 * 
 * There are a number of socialtext syntaxes that allow the user to specify
 * the workspace (links, images, recent changes macro, include macro).
 * Since socialtext workspaces are not necessarily going to be the same as your
 * corresponding confluence spacekey, you can specify a workspace to spacekey
 * map in the properties file for this class to use.  
 */
public class SpaceConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Spacenames - start");
		String input = page.getOriginalText();
		String converted = convertSpaces(input);
		page.setConvertedText(converted);
		log.info("Converting Spacenames - complete");
	}

	/**
	 * transforms all uses of workspaces to spacekeys in the page text
	 * @param input page text
	 * @return page text with confluence spacekeys instead of 
	 * socialtext workspaces
	 */
	protected String convertSpaces(String input) {
		HashMap spaces = getSpaceProperties();
		if (spaces == null || spaces.isEmpty()) return input;
		String converted = convertSpaceInLinks(input, spaces);
		converted = convertSpaceInImages(converted, spaces);
		converted = convertSpaceInRecentMacro(converted, spaces);
		converted = convertSpaceInIncludeMacro(converted, spaces);
		return converted;
	}

	Pattern linksWithSpace = Pattern.compile("" +
			"\\[" +
			"([^:\\]]+)");
	/**
	 * transforms workspaces in link syntax to confluence spacekeys
	 * @param input page text
	 * @param spaces mapping of workspaces to spacekeys
	 * @return
	 */
	protected String convertSpaceInLinks(String input, HashMap spaces) {
		Matcher spaceFinder = linksWithSpace.matcher(input);
		return convertSpaces(input, spaces, spaceFinder, "[");
	}

	/**
	 * transforms workspaces in a given syntax (represented by the regex Matcher) to
	 * confluence spacekeys
	 * @param input page text
	 * @param spaces workspace to spacekey mapping
	 * @param finder represents the regex syntax to identify the workspace. The associated pattern
	 * must have at least one group which captures the socialtext workspace. It must not capture 
	 * anything after the workspace.
	 * @param The replacement text for what comes before group 1 in the regex.
	 * @return transformed text
	 */
	private String convertSpaces(String input, HashMap spaces, Matcher finder, String delim) {
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (finder.find()) {
			found = true;
			String rawSpace = finder.group(1);
			log.debug("rawSpace = " + rawSpace);
			String space = rawSpace;
			String alias = "";
			if (rawSpace.contains("|")) {
				String[] parts = rawSpace.split("\\|");
				alias = parts[0] + "|";
				space = parts[1];
			}
			if (!spaces.containsKey(space)) continue;
			String newspace = (String) spaces.get(space);
			log.debug("newspace = " + newspace);
			String replacement = delim + alias + newspace;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			finder.appendReplacement(sb, replacement);
		}
		if (found) {
			finder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern imagesWithSpace = Pattern.compile("" +
			"[!]" +
			"([^:!\\]]+)");
	/**
	 * transforms workspaces in image syntax to confluence spacekeys
	 * @param input page text
	 * @param spaces mapping of workspaces to spacekeys
	 * @return
	 */
	protected String convertSpaceInImages(String input, HashMap spaces) {
		Matcher spaceFinder = imagesWithSpace.matcher(input);
		return convertSpaces(input, spaces, spaceFinder, "!");
	}

	Pattern recentWithSpace = Pattern.compile("" +
			"\\{recent_changes:\\s*([^}]+)");
	/**
	 * transforms workspaces in recent changes macro syntax to confluence spacekeys
	 * @param input page text
	 * @param spaces mapping of workspaces to spacekeys
	 * @return
	 */
	protected String convertSpaceInRecentMacro(String input, HashMap spaces) {
		Matcher recentFinder = recentWithSpace.matcher(input);
		return convertSpaces(input, spaces, recentFinder, "{recent_changes: ");
	}

	Pattern includeWithSpace = Pattern.compile("" +
			"\\{include:\\s*([^}\\[\\s]+)");
	/**
	 * transforms workspaces in include macro syntax to confluence spacekeys
	 * @param input page text
	 * @param spaces mapping of workspaces to spacekeys
	 * @return
	 */
	protected String convertSpaceInIncludeMacro(String input, HashMap spaces) {
		Matcher includeFinder = includeWithSpace.matcher(input);
		return convertSpaces(input, spaces, includeFinder, "{include: ");
	}

	/**
	 * gets the workspace to spacekey misc properties and creates a map of them
	 * @return map of workspace to spacekey maps. keys are socialtext workspaces.
	 * values are confluence spacekeys.
	 */
	protected HashMap getSpaceProperties() {
		Properties props = getProperties();
		HashMap spaces = new HashMap<String, String>();
		for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (key.startsWith("space-")) {
				String newkey = key.replaceFirst("^space-", "");
				String value = props.getProperty(key);
				spaces.put(newkey, value);
			}
		}
		return spaces;
	}
	
}
