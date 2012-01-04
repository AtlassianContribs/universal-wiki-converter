package com.atlassian.uwc.converters.trac;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalPageNameConverter;
import com.atlassian.uwc.ui.Page;

public class UserDateConverter extends BaseConverter {

	private static final String PROPKEY_FILE = "userdata-filepath";
	private static final String PROPKEY_ARCHIVECREATOR_FILE = "archivecreator-filepath";
	private static final String PROPKEY_ARCHIVECREATOR_COMMENT = "archivecreator-comment";
	private static final String ARCHIVECREATOR_VAR = "%username%";

	Logger log = Logger.getLogger(this.getClass());
	
	private static HashMap<String,UserDate> data; 
	private static HashMap<String,UserDate> archivedata; 
	
	@Override
	public void convert(Page page) {
		log.debug("Processing User and Timestamp.");
		initUserDateData();
		//page author and timestamp. 
		UserDate data = getData(page);
		if (data == null) {
			log.warn("No User and Timestamp data for this pagename: " + page.getName());
			return;
		}
		page.setAuthor(data.author);
		page.setTimestamp(data.timestamp);
		
		//if the user has assigned a different file for archiving authorname, we add a comment
		//useful if they are uploading only the latest version, but want to archive the original creator
		if (archivedata != null && getProperties().containsKey(PROPKEY_ARCHIVECREATOR_FILE)) {
			UserDate archivedataitem = getData(page, archivedata);
			if (getProperties().containsKey(PROPKEY_ARCHIVECREATOR_COMMENT)) {
				String template = getProperties().getProperty(PROPKEY_ARCHIVECREATOR_COMMENT, null);
				if (template != null) {
					String comment = template.replaceAll(ARCHIVECREATOR_VAR, archivedataitem.author);
					page.addComment(comment);
				}
			}
			else {
				log.warn("No property associated with property: " + PROPKEY_ARCHIVECREATOR_COMMENT);
			}
		}
	}
	private void initUserDateData() {
		if (data == null) {
			String filepath = getDataFilepath();
			if (filepath == null) return;
			data = initDataFromFile(filepath);
		}
		if (archivedata == null && getProperties().containsKey(PROPKEY_ARCHIVECREATOR_FILE)) { 
			String filepath = getDataFilepath(PROPKEY_ARCHIVECREATOR_FILE);
			if (filepath == null) return;
			archivedata = initDataFromFile(filepath);
		}
	}
	
	public HashMap<String, UserDate> initDataFromFile(String filepath) {
		HashMap<String, UserDate> thisdata = new HashMap<String, UserDateConverter.UserDate>(); 
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filepath));
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\|");
				if (parts.length != 3) { 
					log.warn("Problem parsing user and timestamp data: " + line);
					continue;
				}
				String page = parts[0].trim();
				String user = parts[1].trim();
				String timestamp = parts[2].trim();
				UserDate userdate = new UserDate(user, timestamp);
				thisdata.put(page, userdate);
			}
			reader.close();
		} catch (Exception e) {
			log.error("Problem reading file: " + filepath, e);
		}
		return thisdata;
	}
	
	public String getDataFilepath() {
		return getDataFilepath(PROPKEY_FILE);
	}
	public String getDataFilepath(String propkey) {
		String filepath = getProperties().getProperty(propkey, null);
		if (filepath == null) {
			log.error("No user date filepath supplied for property key: " + propkey);
			return null;
		}
		File file = new File(filepath);
		if (!file.exists() || !file.isFile()) {
			log.error("Not a valid file: " + filepath);
			return null;
		}
		return filepath;
	}
	
	private UserDate getData(Page page) {
		return getData(page, data);
	}
	private UserDate getData(Page page, HashMap<String, UserDate> thisdata) {
		String pagename = page.getName();
		if (pagename.contains("%")) { //uridecode 
			IllegalPageNameConverter decoder = new IllegalPageNameConverter();
			pagename = decoder.decodeUrl(pagename);
		}
		return thisdata.get(pagename);
	}
	public class UserDate {
		public String author;
		public Date timestamp;
		
		public UserDate(String user, String timestamp) {
			this.author = user;
			if (timestamp.length()<11) { //too short! (in seconds)
				this.timestamp = new Date(new Long(timestamp)*1000);
			}
			else if (timestamp.length() == 16) { //too long! sometimes dbs output this
				this.timestamp = new Date(new Long(timestamp)/1000);
			}
			else if (timestamp.length() == 13) //just right! (in milliseconds)
				this.timestamp = new Date(new Long(timestamp));
			else { //do our best with the unexpected format
				while (timestamp.length() < 13) { // pad to 13 digits
					timestamp += "0";
				}
				timestamp = timestamp.substring(0, 13); //or remove the extra digits
				this.timestamp = new Date(new Long(timestamp));
			}
		}
	}
}
