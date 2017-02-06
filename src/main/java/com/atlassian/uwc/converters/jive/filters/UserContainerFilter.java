package com.atlassian.uwc.converters.jive.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.jive.InternalLinkConverter;
import com.atlassian.uwc.filters.UWCFilter;

public class UserContainerFilter implements FileFilter, UWCFilter {

	Logger log = Logger.getLogger(this.getClass());
	Pattern usercontainer = Pattern.compile("[\\/\\\\]2020-\\d+$");
	Properties properties;
	InternalLinkConverter linkhandler = new InternalLinkConverter();
	public boolean accept(File file) {
		if (file.isDirectory()) return true;
		String parent = file.getParent();
		String name = file.getName();
		Matcher ucFinder = usercontainer.matcher(parent);
		if (ucFinder.find() && name.startsWith("DOC-")) {
			log.debug("filtering user container documents: " + parent + "/" + name);
			linkhandler.filterTitle(file.getName(), properties);
			return false;
		}
		return true;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public Properties getProperties() {
		return this.properties;
	}

}
