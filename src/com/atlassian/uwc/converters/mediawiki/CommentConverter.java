package com.atlassian.uwc.converters.mediawiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.PropertyFileManager;

/**
 * Transforms mediawiki discussion pages into user/page comments for associated page
 */
public class CommentConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());

	public enum TYPE {
		START,
		END;
		public static TYPE getType (String string) {
			if (string.contains("-start-")) return START;
			if (string.contains("-end-")) return END;
			throw new IllegalArgumentException("Must contain either '-start-' or '-end-'.");
		}
	}
	
	private HashMap<String, TYPE> delimtypes = new HashMap<String, TYPE>(); 
	
	public void convert(Page page) {
		log.debug("Comment Converter - start");
		String pageParent = page.getFile().getParent();
		//get location of Discussion pages. log directory used
		String discussionDir = getDiscussionLocation(pageParent);
		if (discussionDir == null) return;
		//get discussion page for this page
		String discussionContent = getDiscussionContent(discussionDir, page.getName());
		if (discussionContent == null) return;
		//get delimiter properties and compile 
		Vector<Pattern> delimPatterns = getDelimiters(); 
		//create comments using discussion page and assign commments to this page
		findAndAddComments(page, discussionContent, delimPatterns);
		log.debug("Comment Converter - complete");
	}
	/**
	 * @return location of discussion input files. Uses property assigned in
	 * converter.xxx.properties, using misc property framework. property should be called
	 * discussion-location
	 */
	protected String getDiscussionLocation() {
		Properties props = getProperties();
		if (!props.containsKey("discussion-location")) {
			log.error("Comment Converter requires property 'discussion-location'. Skipping.");
			return null;
		}
		String location = props.getProperty("discussion-location");
		log.debug("Comment Converter discussion-location='"+ location + "'");
		return location;
	}
	
	/**
	 * @param parent
	 * @return location if given parent should take the place of current directory in the
	 * relative path's resolution
	 */
	protected String getDiscussionLocation(String parent) {
		String rel = getDiscussionLocation();
		if (rel.startsWith("." + File.separator)) return parent + File.separator + rel.substring(2);
		while (rel.startsWith(".." + File.separator)) {
			rel = rel.substring(3);
			parent = parent.replaceFirst("\\" + File.separator + "[^\\" + File.separator + "]+$", "");
		}
		return parent + File.separator + rel;
	}

	/**
	 * @param discussionDir
	 * @param name
	 * @return content of discussion page associated with page of given name, located
	 * in given discussionDir
	 */
	protected String getDiscussionContent(String discussionDir, String name) {
		String discname = getDiscussionName(name);
		if (!discussionDir.endsWith(File.separator)) discussionDir += File.separator;
		String filepath = discussionDir + discname;
		try {
			return readFile(filepath);
		} catch (IOException e) {
			log.error("Could not read discussion file at: '" + filepath + "'. Skipping.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param name
	 * @return discussion file's name, given page's name
	 */
	protected String getDiscussionName(String name) {
		name = name.replaceFirst("\\.txt$", "");
		name += "_Discussion.txt";
		return name;
	}
	/**
	 * @param path
	 * @return contents of file at given path
	 * @throws IOException if there are problems reading file at given path
	 */
	private String readFile(String path) throws IOException {
		String filestring = "";
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));
		while ((line = reader.readLine()) != null) {
			filestring += line + "\n";
		}
		reader.close();
		return filestring;
	}
	
	/**
	 * @return Pattern's compiled from the delimiters assigned in 
	 * converter.xxx.properties file, with keys that look like:
	 * discussion-delim-[start/end]-#
	 */
	protected Vector<Pattern> getDelimiters() {
		Properties props = getProperties();
		Set keys = props.keySet();
		Vector<Pattern> delims = new Vector<Pattern>();
		for (Object keyobj : keys) {
			String key = (String) keyobj;
			if (key.startsWith("discussion-delim-")) {
				String val = props.getProperty(key);
				log.debug("compiling pattern: " + val);
				delims.add(Pattern.compile(val));
				delimtypes.put(val, TYPE.getType(key));
			}
		}
		return delims;
	}
	/**
	 * finds comments in given discussionContent by parsing with given delimPatterns.
	 * and add found comments to given page.
	 * NOTE: Each pattern from delimPatterns must be assigned a TYPE in the delimTypes map. 
	 * For example, call getDelimiters first.
	 * @param page
	 * @param discussionContent
	 * @param delimPatterns
	 * @throws NullPointerException if delimTypes does not contain type for any pattern from delimPatterns
	 */
	protected void findAndAddComments(Page page, String discussionContent, Vector<Pattern> delimPatterns) {
		//find all the indexes, and associate with type
		TreeMap<Integer, TYPE> indexes = new TreeMap<Integer, TYPE>();  
		for (Pattern pattern : delimPatterns) {
			Matcher m = pattern.matcher(discussionContent);
			TYPE type = delimtypes.get(pattern.pattern());
			while (m.find()) {
				switch (type) {
				case START:
					indexes.put(m.start(), type);
					break;
				case END:
					indexes.put(m.end(), type);
					break;
				}
			}
		}
		//deliminate all the comments using the indexes.
		int start = 0;
		int end = 0;
		for (int index : indexes.keySet()) {
			end = index;
			String comment = discussionContent.substring(start, end);
			//skip empty comments
			if ("".equals(comment.trim())) continue;
			//attach comments
			page.addComment(convert(comment));
			start = end;
		}
		String comment = discussionContent.substring(end);
		if (!"".equals(comment.trim())) page.addComment(convert(comment));
	}
	
	/**
	 * @param string
	 * @return converts given string from Mediawiki to Confluence 
	 * NOTE: can't just run converter on these as we'd hit a loop with the CommentConverter 
	 */
	protected String convert(String string) {
		String path = "conf/converter.mediawiki.properties";
		TreeMap<String,String> converters = null; 
        try {
			converters = PropertyFileManager.loadPropertiesFile(path);
		} catch (IOException e) {
			log.error("Couldn't load properties at "+ path);
			e.printStackTrace();
		}
//		no name handling
		converters.remove("Mediawiki.1000-remove-extension.class"); 
		converters.remove("Mediawiki.1020-underscore2space.class");
		//no comments handling
		converters.remove("Mediawiki.1100.discussionpages2comments.class");
		Page page = new Page(null);
		page.setOriginalText(string);
		page.setConvertedText(string);
		ConverterEngine engine = new ConverterEngine();
		for (Object converterobj : converters.keySet()) {
			String converterKey = (String) converterobj;
			String value = (String) converters.get(converterKey);
			String converterString = converterKey + "=" + value;
			Converter converter = engine.getConverterFromString(converterString);
			if (converter == null) continue; //XXX long-term we may want to allow for misc props framework
			log.debug("Converting comment with: " + converterString);
			try {
				converter.convert(page);
				page.setOriginalText(page.getConvertedText());
			} catch (Exception e) {
				log.error("Problem converting with " + converterKey + ". Skipping.");
				e.printStackTrace();
				continue;
			}
		}
		return page.getConvertedText();
	}
	protected HashMap<String, TYPE> getDelimtypes() {
		return delimtypes;
	}

}
