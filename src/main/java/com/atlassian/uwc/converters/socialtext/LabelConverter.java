package com.atlassian.uwc.converters.socialtext;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * transforms categories to labels.
 * Transformation can be controlled by properties like:
 * <pre>Socialtext.0150.label-trans-1.property=&=and</pre>
 * which means that labels with &amp; will have the word 'and' used 
 * as a replacement 
 */
public class LabelConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Labels - start");
		String input = page.getOriginalText();
		Vector<String> labels = getLabels(input);
		for (String label : labels) {
			page.addLabel(label);
		}
		log.info("Converting Labels - complete");
	}
	Pattern category = Pattern.compile("" +
			"(?<=^|\n)" +
			"Category: *" +
			"(.*)", Pattern.CASE_INSENSITIVE);
	
	/**
	 * finds the categories from the page text
	 * @param input page text
	 * @return list of categories
	 */
	protected Vector<String> getLabels(String input) {
		Vector<String> labels = new Vector<String>();
		Matcher categoryFinder = category.matcher(input);
		HashMap<String,String> configOptions = getTransformationOptions();
		while (categoryFinder.find()) {
			String category = categoryFinder.group(1);
			category = transformCategory(configOptions, category);
			//add the label
			if (!category.equals(""))
				labels.add(category);
		}
		return labels;
	}
	
	/**
	 * transforms socialtext categories to allowed confluence categories
	 * by first using the configOptions mapping to replace configOption keys with
	 * its values, and then removing any remaining invalud chars. Invalid label chars are:
	 * (space) ! # & ( ) * , . : ; < > ? @ [ ] ^
	 * @param configOptions map of key->value socialtext to confluence character config option
	 * @param category the category to be transformed 
	 * @return the transformed category
	 */
	public String transformCategory(HashMap<String, String> configOptions, String category) {
		//handle configuration options
		for (String key : configOptions.keySet()) {
			String val = configOptions.get(key);
			category = category.replaceAll("["+key+"]", val);
		}
		//remove any remaining invalid label characters
		category = category.replaceAll("[ !#&()*,.:;<>?@\\[\\]\\^]", "");
		return category;
	}

	Pattern equalsSeparator = Pattern.compile("^(.)=(.*)");
	/**
	 * map of transformation configuration options from the properties file that
	 * look like:
	 * Socialtext.0150.label-trans-1.property=&=and
	 * @return map of config options
	 */
	public HashMap<String, String> getTransformationOptions() {
		HashMap<String,String> options = new HashMap<String, String>();
		Properties props = getProperties();
		Vector<String> fromChars = new Vector<String>();
		Vector<String> toChars = new Vector<String>();
		for (Enumeration<?> iter = props.propertyNames(); iter.hasMoreElements();) {
			String propkey = (String) iter.nextElement();
			if (!propkey.startsWith("label-trans")) continue;
			String value = (String) props.get(propkey);
			Matcher delimFinder = equalsSeparator.matcher(value);
			if (delimFinder.matches()) {
				String from = delimFinder.group(1);
				String to = delimFinder.group(2);
				if (Pattern.matches("[\\[\\]\\^]", from))
						from = "\\" + from;
				options.put(from, to);
			}
			else log.error("label-trans value doesn't meet requirements: " + value);
		}
		
		return options;
	}

}
