package com.atlassian.uwc.converters.jive.filters;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.jive.InternalLinkConverter;
import com.atlassian.uwc.converters.jive.SpaceConverter;
import com.atlassian.uwc.converters.jive.TagConverter;
import com.atlassian.uwc.filters.UWCFilter;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;

public class TagFilter implements FileFilter, UWCFilter {

	Logger log = Logger.getLogger(this.getClass());
	Properties properties;
	InternalLinkConverter linkhandler = new InternalLinkConverter();
	public boolean accept(File file) {
		if (file.isDirectory()) return true;
		//get the global filters
		String exregex = getProperties().getProperty("filterbytag-excluderegex", null);
		String inregex = getProperties().getProperty("filterbytag-includeregex", null);
		
		//get the spacekey
		Page page = new Page(file);
		try {
			page.setOriginalText(FileUtils.readTextFile(file));
		} catch (IOException e) {
			log.error("Could not read file: " + file.getName(), e);
		}
		SpaceConverter spaceConverter = new SpaceConverter();
		spaceConverter.setProperties(getProperties()); //so we can init space data
		spaceConverter.convert(page);
		String spacekey = page.getSpacekey();//NOTE: This will be case sensitive within the properties file and should match space

		String spaceExregex = getProperties().getProperty("filterbytag-excluderegex-"+spacekey, null);
		String spaceInregex = getProperties().getProperty("filterbytag-includeregex-"+spacekey, null);
		if (spaceExregex != null) {
			log.debug("Overriding global exclude filter with space exclude filter.");
			exregex = spaceExregex;
			inregex = null; //so a global include filter does not take precedence
		}
		if (spaceInregex != null) { //we set include here so that it will win if both exclude and include were set for this space
			log.debug("Overriding global include filter with space include filter.");
			inregex = spaceInregex; 
			exregex = null; //so a global exclude filter does not take precedence
		}
		
		if (inregex == null && exregex == null) {
			log.info("No filterbytag-[exclude|include]regex property defined. Skipping.");
			return true;
		}
		if (inregex != null && exregex != null) {
			log.info("filterbytag: can only set either exclude or include filter. Not both. Using include filter.");
		}

		//get the labels
		TagConverter converter = new TagConverter();
		converter.convert(page);
		Set<String> labels = page.getLabels();

		String regex = null;
		boolean include = true;
		if (inregex != null) { //use the include property if it's defined
			regex = inregex;
		}
		else { //otherwise use the exclude property
			regex = exregex;
			include = false;
		}

		//handle case where there are no labels
		if (labels.isEmpty()) {
			labels.add("");
		}

		//compile the regex and apply the filter
		Pattern p = Pattern.compile(regex);
		for (String label : labels) {
			Matcher m = p.matcher(label);
			if (m.find()) {
				if (!include) {
					log.debug("filterbytag - Found matching tag: " + label + ". Filtering page:" + file.getName());
					linkhandler.filterTitle(file.getName(), properties);
				}
				return include;
			}
		}
		
		if (include) {
			log.debug("filterbytag - did not find any matching tags. Filtering page:" + file.getName());
			linkhandler.filterTitle(file.getName(), properties);
		}
		return !include;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public Properties getProperties() {
		return this.properties;
	}

}
