package com.atlassian.uwc.converters.jive;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.ui.ConverterEngine;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.PropertyFileManager;
import com.atlassian.uwc.util.TokenMap;

public class CommentConverter extends BaseConverter {

	Pattern jivemeta = Pattern.compile("\\{jive-export-meta:([^}]+)\\}");
	Pattern typePattern = Pattern.compile("type=(\\w+)");
	Pattern objidPattern = Pattern.compile("^id=(\\d+)");
	public final long DOCTYPE = 102;
	public final long BLOGTYPE = 38;
	
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		Matcher jivemetaFinder = jivemeta.matcher(page.getOriginalText());
		if (jivemetaFinder.find()) {
			//figure out page id
			String params = jivemetaFinder.group(1);
			Matcher typeFinder = typePattern.matcher(params);
			String type = (typeFinder.find())?typeFinder.group(1):null;
			long typenum = ("DOC".equals(type))?DOCTYPE:BLOGTYPE;
			Matcher idFinder = objidPattern.matcher(params);
			String id = (idFinder.find())?idFinder.group(1):null;
			String pageid = typenum + "_" + id;
			
			//get comments using page id and attach to page
			Vector<String> commentpaths = getCommentsFromDir(page.getFile(), pageid);
			if (commentpaths.isEmpty()) return;
			Vector<String> comments = getCommentStrings(commentpaths);
			for (String comment : comments) {
				setupComment(page, comment);
			}
		}

	}
	
	private FilenameFilter commentfilter = new FilenameFilter() {
		
		public boolean accept(File dir, String name) {
			return name.startsWith("COMMENT-");
		}
	};
	
	protected Vector<String> getCommentsFromDir(File file, String pageid) {
		String parent = file.getParent();
		String[] comments = file.getParentFile().list(commentfilter);
		Vector<String> paths = new Vector<String>();
		for (String commentname : comments) {
			if (commentname.endsWith("-"+pageid + "-1.txt")) {
				paths.add(parent + file.separator + commentname);
			}
		}
		return paths;
	}
	protected Vector<String> getCommentStrings(Vector<String> commentpaths) {
		Vector<String> comments = new Vector<String>();
		for (String path : commentpaths) {
			File file = new File(path);
			try {
				comments.add(FileUtils.readTextFile(file));
			} catch (IOException e) {
				log.error("Problem reading comment file: " + path, e);
			}
		}
		return comments;
	}
	UserDateConverter converter = new UserDateConverter();
	protected void setupComment(Page page, String comment) {
		String creator = converter.getUser(comment); 
		String timestamp = converter.getDateAsString(comment);
		String text = convert(comment);
		page.addComment(text, creator, timestamp);
	}
	
	protected String convert(String string) {
		String path = "conf/converter.jive.properties";
		TreeMap<String,String> converters = null; 
        try {
			converters = PropertyFileManager.loadPropertiesFile(path);
		} catch (IOException e) {
			log.error("Couldn't load properties at "+ path);
			e.printStackTrace();
		}
		//no filters
		converters.remove("Jive.0002.nocomment.filter");
		converters.remove("Jive.0002.onlyblogsfromusercontainer.filter");
		converters.remove("Jive.0002.filterbytag.filter");
		//no metadata handling
		converters.remove("Jive.0200.title.class"); 
		converters.remove("Jive.0225.blog.class");
		converters.remove("Jive.0250.space.class");
		//no comments handling
		converters.remove("Jive.0275.comment.class");
		//no attachments for now
		converters.remove("Jive.0500.attachments.class");
		//no userdate handling
		converters.remove("Jive.0004.userdate.class");
		//Save Page Tokens so they are not lost when the comment converter detokenizes
		TokenMap.backupTokens();
		//create a "page" object to use so we can transform the comment's syntax
		Page page = new Page(null);
		page.setOriginalText(string);
		page.setConvertedText(string);
		ConverterEngine engine = new ConverterEngine();
		Vector<String> converterStrings = new Vector<String>();
		for (Object converterobj : converters.keySet()) {
			String converterKey = (String) converterobj;
			String value = (String) converters.get(converterKey);
			String converterString = converterKey + "=" + value;
			converterStrings.add(converterString);
		}
		boolean runningState = false; //some converter engine state is not necessary in this context
		ArrayList<Converter> createConverters = engine.createConverters(converterStrings, runningState);
		for (Converter converter : createConverters) {
			
			if (converter == null) continue; 
			log.debug("Converting comment with: " + converter.getKey());
			try {
				converter.setAttachmentDirectory(getAttachmentDirectory());
				converter.convert(page);
				page.setOriginalText(page.getConvertedText());
			} catch (Exception e) {
				log.error("Problem converting with " + converter.getKey() + ". Skipping.");
				e.printStackTrace();
				continue;
			}
		}
		//put the page tokens back in.
		TokenMap.revertTokens();
		return page.getConvertedText();
	}

}
