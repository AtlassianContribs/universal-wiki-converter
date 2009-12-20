package com.atlassian.uwc.converters.socialtext;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Transforms the Search widget into either the search macro or the 
 * contentbylabel macro, depending on if it is referencing tags/labels or not.
 * The contentbylabel variant can have parameters added to it. 
 * The title param will create a title from the labels used by the macro. 
 * Any other parameters referenced will be added to all contentbylabel macros. 
 * For example, if the spaces and sort property is uncommented, the 
 * contentbylabel results will look like: 
 * {contentbylabel:labels=foo|spaces=@self|sort=creation}
 * <pre>
 * Socialtext.1080.search-title.property=true
 * Socialtext.1080.search-spaces.property=@self
 * Socialtext.1080.search-sort.property=creation
 * Socialtext.1080.search-reverse.property=true
 * </pre>
 */
public class SearchConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	Pattern search = Pattern.compile("" +
			"\\{search:\\s*tag:([^\\}]+)\\}");
	Pattern taglist = Pattern.compile("" +
			"\\{tag_list:\\s*([^\\}]+)\\}");
	public void convert(Page page) {
		log.debug("Converting Search - start");
		String input = page.getOriginalText();
		String converted = convertSearch(input);
		page.setConvertedText(converted);
		log.info("Converting Search - complete");
	}
	protected String convertSearch(String input) { //used by test classes
		HashMap<String,String> options = getOptions();
		String converted = convertToContentByLabel(input, options, search.matcher(input));
		if (convertTaglist())
			converted = convertToContentByLabel(converted, options, taglist.matcher(converted));
		return converted;
	}
	
	private boolean convertTaglist() {
		Properties props = this.getProperties();
		return props.containsKey("taglist-to-contentbylabel") &&
		Boolean.parseBoolean(props.getProperty("taglist-to-contentbylabel"));
	}
	
	
	protected String convertToContentByLabel(String input, HashMap<String, String> options, Matcher finder) {
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (finder.find()) {
			found = true;
			String searchParams = finder.group(1);
			String labels = searchParams.trim();
			
			if (hasMultipleTags(searchParams)) {
				labels = buildMultipleLabelsString(searchParams);
			}
			else labels = handleBadChars(labels); 
			String optionalParams = buildParams(labels, options);
			String replacement = "{contentbylabel:labels=" + labels + optionalParams + "}";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			finder.appendReplacement(sb, replacement);
		}
		if (found) {
			finder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern optionPattern = Pattern.compile("search-(.*)");
	/**
	 * gets a list of search converter properties from the properties file
	 */
	protected HashMap<String, String> getOptions() {
		HashMap<String,String> options = new HashMap<String, String>();
		Properties props = getProperties();
		for (Enumeration<?> iter = props.propertyNames(); iter.hasMoreElements();) {
			String propkey = (String) iter.nextElement();
			if (!propkey.startsWith("search-")) continue;
			Matcher optionsFinder = optionPattern.matcher(propkey);
			if (optionsFinder.matches()) {
				String key = optionsFinder.group(1);
				String value = props.getProperty(propkey);
				options.put(key, value);
			}
			else log.error("search property key doesn't meet requirements: " + propkey);
		}
		return options;
	}
	
	Pattern hasMultiple = Pattern.compile("" +
			"\\b((and)|(or))\\b", Pattern.CASE_INSENSITIVE);

	/**
	 * @param searchParams
	 * @return true if the searchParams contains the keywords "and" or "or" 
	 * followed by "tag:"
	 */
	protected boolean hasMultipleTags(String searchParams) {
		Matcher hasMultFinder = hasMultiple.matcher(searchParams);
		return hasMultFinder.find();
	}
	
	Pattern and = Pattern.compile("and", Pattern.CASE_INSENSITIVE);
	Pattern or = Pattern.compile("or", Pattern.CASE_INSENSITIVE);
	Pattern tag = Pattern.compile("tag:", Pattern.CASE_INSENSITIVE);
	/**
	 * creates the contentbylabel labels parameter string for the given searchParams,
	 * handling any illegal characters in the same way that the LabelConverter did. 
	 * @param searchParams might look something like: "tag: foo! AND tag:lorem ipsum"
	 * @return contentbylabel labels string. For the searchParams listed above:
	 * +foo,+loremipsum 
	 */
	protected String buildMultipleLabelsString(String searchParams) {
		String[] words = searchParams.split(" ");
		String labels = words[0];
		String delim = "";
		for (int i = 1; i < words.length; i++) {
			String word = words[i];
			word = word.replaceFirst("tag_list:", "");
			if (and.matcher(word).matches()) {
				if ("".equals(delim)) labels = "+" + labels;
				delim = "+";
				labels += ",";
			}
			else if (or.matcher(word).matches()) {
				delim = "";
				labels += ",";
			}
			else if (tag.matcher(word).matches()) continue;
			else if (tag.matcher(word).lookingAt()) {
				words[i--] = word.replaceFirst("tag:", "");
				continue;
			}
			else {
				labels += delim + handleBadChars(word);
			}
			
		}
		labels = labels.replaceAll("(?<=[^=,])[+]", "");
		return labels;
	}
	/**
	 * @param labels
	 * @param options
	 * @return creates confluence parameter string for the given labels and options
	 */
	protected String buildParams(String labels, HashMap<String, String> options) {
		String params = "";
		
		if (options.containsKey("title")) {
			String[] parts = labels.split(",");
			String title = "";
			if (parts.length == 1) title = capitalize(labels);
			else {
				String delim = " or ";
	 			for (int i = 0; i < parts.length; i++) {
					String label = parts[i];
					if (label.startsWith("+")) {
						delim = " and ";
						label = label.substring(1);
					}
					else delim = " or ";
					if (i > 0) title += delim;
					title += capitalize(label);
				}
			}
			params += "|title=" + title;
		}
		Set<String> keys = options.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (key.equals("title")) continue;
			params += "|" + key + "=" + options.get(key);
		}
		return params;
	}
	/**
	 * @param input
	 * @return capitalizes the first char in the input string
	 */
	private String capitalize(String input) {
		return input.substring(0,1).toUpperCase() + input.substring(1);
	}

	/**
	 * uses the LabelConverter to handle illegal characters in labels
	 * @param input labels string
	 * @return labels string with illegal chars handled
	 */
	protected String handleBadChars(String input) {
		LabelConverter converter = new LabelConverter();
		converter.setProperties(this.getProperties());
		String newinput = converter.transformCategory(getOptions(converter), input);
		return newinput;
	}
	
	HashMap<String, String> options = null;
	/**
	 * @param converter
	 * @return gets labels illegal handling config options from properties
	 */
	private HashMap<String,String> getOptions(LabelConverter converter) {
		if (this.options == null) 
			this.options = converter.getTransformationOptions();
		return this.options;
	}
	
	/**
	 * removes the contents of the options object.
	 * Useful for junit tests.
	 */
	protected void clearOptions() {
		this.options = null;
	}
}
