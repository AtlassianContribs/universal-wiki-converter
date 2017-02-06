package com.atlassian.uwc.splitters;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.atlassian.uwc.ui.Page;

/**
 * 
 * This interface allows files containing multiples pages, or multiple versions of pages to be
 * separated into Page objects directly by the engine, rather than requiring a two part process.
 * To use: (a) implement this interface, and (b) in your converter.xxx.properties file, set a property that looks like this:
 * wiki.1234.pagesplitter.property=com.atlassian.path.to.implemented.class
 * @author Laura Kolker
 */
public interface PageSplitter {

	/**
	 * This method should take a file, (probably read its contents), and generate a list of Page objects
	 * which it will return. The engine will expect that:
	 * 1) contents of the returned List are non-null Page objects
	 * 2) the Page object's file, path, originaltext and unchangedsource fields have been set
	 * @param file
	 * @return
	 */
	public List<Page> split (File file);
	
	/**
	 * miscellaneous properties will be passed in with this method
	 * @param properties
	 */
	public void setProperties(Properties properties);
	
}
