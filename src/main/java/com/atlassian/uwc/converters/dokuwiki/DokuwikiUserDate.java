package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.VersionPage;

public class DokuwikiUserDate extends HierarchyTarget {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String changeFilepath = createChangeFilename(page);
		if (changeFilepath == null) {
			log.warn("Could not handle user and date data. Check filepath-hierarchy-ignorable-ancestors amd meta-dir settings. Skipping");
			return;
		}
		File changeFile = new File(changeFilepath);
		if (!changeFile.exists() || !changeFile.isFile()) {
			log.error("Could not find changes file for " + page.getFile().getName() + " at " + 
					changeFilepath + ". Skipping.");
			return;
		}
		String changeContent;
		try {
			changeContent = FileUtils.readTextFile(changeFile);
		} catch (IOException e) {
			log.error("Could not read changes file: " + changeFilepath +". Skipping.", e);
			return;
		}
		//not preserving history at this time
		String line = "";
		if (page instanceof VersionPage) {
			line = getHistoryLine(changeContent, page.getTimestamp());
		}
		else 
			line = getLastLine(changeContent);
		ChangeData data = getData(line);
		if (data == null) {
			log.warn("changes content was malformed in file: " + changeFilepath + ". Skipping.");
			return;
		}
		if (!(page instanceof VersionPage)) { //VersionPage already has timestamp set
			long timestring = Long.parseLong(data.timestamp);
			Date date = new Date(timestring*1000); //multiply x 1000 because the Date interface is in milliseconds
			log.debug("User Date Converter - setting timestamp: " + data.timestamp);
			page.setTimestamp(date);
		}
		log.debug("User Date Converter - setting author: " + data.user);
		page.setAuthor(data.user);
	}

	protected String getHistoryLine(String changeContent, Date timestamp) {
		String epoch = (timestamp.getTime()/1000)+"";
		Pattern p = Pattern.compile("(?<=^|\n)"+epoch+"[^\n]+");
		Matcher m = p.matcher(changeContent);
		if (m.find()) return m.group();
		else log.debug("Could not get history line for timestamp: " + epoch);
		return getLastLine(changeContent);
	}

	protected String createChangeFilename(Page page) {
		String path = getRelativePath(page);
		return getMetaFilename(path, ".changes");
	}

	Pattern lastline = Pattern.compile("[^\n]*$");
	private String getLastLine(String input) {
		Matcher lastFinder = lastline.matcher(input);
		if (lastFinder.find()) 
			return lastFinder.group();
		return input;
	}

	Pattern tab = Pattern.compile("" +
			"([^\t]*)\t" +
			"([^\t]*)\t" +
			"([^\t]*)\t" +
			"([^\t]*)\t" +
			"([^\t]*)\t" +
			"([^\t]*)" +
			"");
	private ChangeData getData(String input) {
		Matcher tabFinder = tab.matcher(input);
		ChangeData data = new ChangeData();
		if (tabFinder.find()) {
			data.timestamp = tabFinder.group(1);
			data.ip = tabFinder.group(2);
			data.changetype = tabFinder.group(3);
			data.pagename = tabFinder.group(4);
			String userdata = tabFinder.group(5);
			if ("".equals(userdata)) 
				data.user = null;
			else 
				data.user = userdata;
			data.comment = tabFinder.group(6);
			return data;
		}
		return null;
	}

	public class ChangeData {
		public String timestamp;
		public String ip;
		public String changetype; //C or E, create/edit respectively
		public String pagename;
		public String user;
		public String comment;
	}
}
