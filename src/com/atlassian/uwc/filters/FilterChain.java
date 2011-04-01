package com.atlassian.uwc.filters;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

public class FilterChain {

	private Set<String> values;
	private Properties properties;
	Logger log = Logger.getLogger(this.getClass());
	public FilterChain(Set<String> values) {
		this.values = values;
	}

	public FilterChain(Set<String> values, Properties properties) {
		this(values);
		this.properties = properties;
	}

	public FileFilter getFilter() {
		if (this.values == null) return null;
		if (this.values.isEmpty()) return null;
		Vector<FileFilter> filters = new Vector<FileFilter>();
		for (String val : this.values) {
			if (isFileFilterClass(val)) filters.add(createFilter(val));
			else filters.add(createEndsWithFilter(val));
		}
		Chain chain = new Chain(filters);
		return chain;
	}

	private boolean isFileFilterClass(String val) {
		try {
			Class c = Class.forName(val);
			FileFilter filter = (FileFilter) c.newInstance();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private FileFilter createFilter(String val) {
		Class c;
		try {
			log.debug("Pages filtered from class: " + val);
			c = Class.forName(val);
			FileFilter instance = (FileFilter) c.newInstance();
			if (instance instanceof UWCFilter) {
				((UWCFilter) instance).setProperties(this.properties);
			}
			return instance;
		} catch (Exception e) {
			log.error("Problem creating FileFilter: " + val);
			e.printStackTrace();
			return null;
		}
	}

	private FileFilter createEndsWithFilter(String val) {
		log.debug("Pages filtered with pattern: " + val);
		return new Endswith(val);
	}
	
	private class Endswith implements FileFilter {

		private String val;

		public Endswith(String val) {
			this.val = val;
		}

		public boolean accept(File file) {
			return val == null ||
			"".equals(val) ||
			file.isDirectory() ||
			file.getName().endsWith(val);
		}
		
	}

	/**
	 * 
	 *
	 */
	private class Chain implements FileFilter {

		private Vector<FileFilter> filters;

		public Chain(Vector<FileFilter> filters) {
			this.filters = filters;
			if (this.filters == null) filters = new Vector<FileFilter>();
		}

		//What we are trying to accomplish is that
		//- any non-endswith filter if it excludes a file will force the file to be excluded
		//- if there are any endswith filters, then only one of those filters needs to succeed for 
		//  the file to be included
		public boolean accept(File file) {
			Boolean accepted = null;
			Boolean endswithAccepted = null;
			for (FileFilter filter : filters) {
				boolean acceptable = filter.accept(file); //the current filter's result on this file
				if (accepted == null) accepted = acceptable;
				if (filter instanceof Endswith) {
					if (endswithAccepted == null) endswithAccepted = acceptable;
					else endswithAccepted |= acceptable; //endswith: only one needs to succeed
				}
				else {
					accepted &= acceptable; //non-endswith: only one needs to fail
					if (!accepted) return accepted; //if non-endswith failed at any point, then return false 
				}
			}
			if (endswithAccepted == null) endswithAccepted = true;//if no endswith filters, then autosuceeed
			return endswithAccepted && accepted; 
		}
	}
}
