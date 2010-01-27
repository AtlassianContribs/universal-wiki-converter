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

public class DokuwikiUserDate extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String changeFilepath = createChangeFilename(page.getFile().getPath());
		if (changeFilepath == null) {
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
			log.error("Could not read changes file: " + changeFilepath +". Skipping.");
			e.printStackTrace();
			return;
		}
		//not preserving history at this time
		String lastline = getLastLine(changeContent);
		ChangeData data = getData(lastline);
		if (data == null) {
			log.warn("changes content was malformed in file: " + changeFilepath + ". Skipping.");
			return;
		}
		long timestring = Long.parseLong(data.timestamp);
		Date date = new Date(timestring*1000); //multiply x 1000 because the Date interface is in milliseconds
		page.setTimestamp(date);
		page.setAuthor(data.user);
	}

	private String createChangeFilename(String path) {
		String metadir = getProperties().getProperty("meta-dir", null);
		if (metadir == null) {
			log.warn("Could not handle user and date data. meta-dir property must be set. Skipping");
			return null;
		}
		String ignorable = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", null);
		if (ignorable == null) {
			log.warn("Could not handle user and date data. filepath-hierarchy-ignorable-ancestors must be set. Skipping");
			return null;
		}
		String relative = path.replaceFirst("\\Q" + ignorable + "\\E", "");
		relative = relative.replaceFirst("\\.txt$", ".changes");
		if (relative.startsWith(File.separator) && metadir.endsWith(File.separator))
			relative = relative.substring(1);
		if (!relative.startsWith(File.separator) && !metadir.endsWith(File.separator))
			relative = File.separator + relative;
		return metadir + relative;
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
