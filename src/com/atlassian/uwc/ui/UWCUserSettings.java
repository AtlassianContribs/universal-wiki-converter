package com.atlassian.uwc.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.listeners.FeedbackHandler;

/**
 * handles UWC settings - saving, loading, getting, etc.
 */
public class UWCUserSettings implements FeedbackHandler {

	private static final String DEFAULT_TAB_INDEX = "0";

	/**
	 * identifies each setting
	 */
	public enum Setting {
		SEND_TO_CONFLUENCE, 
		WIKITYPE,
		ATTACHMENTS,		//attachments directory		
		PATTERN,
		SPACE,
		PASSWORD,
		LOGIN,
		URL,				//address
		PAGE_CHOOSER_DIR,	//The directory the page chooser dialog was open to last
		ATTACHMENT_SIZE,
		UPLOAD_ORPHAN_ATTACHMENTS,
		CURRENT_TAB, 
		FEEDBACK_OPTION,
		TRUSTSTORE,
		TRUSTPASS,
		TRUSTALL,
		PAGES;				//saved pages, colon delimited page paths
		
		public String toString() {
			switch (this) {
			case URL:
				return "Address".toUpperCase();
			default:
				return super.toString();
			}
		}
	}

	// CONSTANTS
	private static final String DEFAULT_URL = "localhost:8080";
	private static final String DEFAULT_SPACE = "ds";
	private static final String DEFAULT_SENDTOCONF = "true";
	private static final String DEFAULT_VALUE = "";
	private static final String DEFAULT_LOGIN = "login";
	private static final String DEFAULT_ATTACHMENT_DIR = DEFAULT_VALUE;
	private static final String DEFAULT_FEEDBACK_OPTION = "true";
	public static final String DEFAULT_ATTACHMENT_SIZE = "-1";
	public static final String DEFAULT_UPLOAD_ORPHAN_ATTACHMENTS = "false";
	public static final String DEFAULT_TRUSTSTORE = "";
	public static final String DEFAULT_TRUSTPASS = "";
	public static final String DEFAULT_TRUSTALL = "";
	public static final String DEFAULT_PAGES = "";
	public static final String PROPKEY_SEND_TO_CONFLUENCE = "sendToConfluence";
	public static final String PROPKEY_WIKITYPE = "wikitype";
	public static final String PROPKEY_ATTACHMENTS = "attachments";
	public static final String PROPKEY_PATTERN = "pattern";
	public static final String PROPKEY_SPACE = "space";
	public static final String PROPKEY_PASSWORD = "password";
	public static final String PROPKEY_LOGIN = DEFAULT_LOGIN;
	public static final String PROPKEY_URL = "url";
	public static final String PROPKEY_PAGE_CHOOSER_DIR = "pageChooserDir";
	public static final String PROPKEY_ATTACHMENTSIZE = "attachment.size.max";
	public static final String PROPKEY_UPLOAD_ORPHAN_ATTACHMENTS = "uploadOrphanAttachments";
	public static final String PROPKEY_CURRENTTAB = "current.tab.index";
	public static final String PROPKEY_FEEDBACK_OPTION = "feedback.option";
	public static final String PROPKEY_TRUSTSTORE = "truststore";
	public static final String PROPKEY_TRUSTPASS = "trustpass";
	public static final String PROPKEY_TRUSTALL = "trustall";
	public static final String PROPKEY_PAGES = "pages";
	private static final String DEFAULT_USER_SETTINGS_LOCATION = "conf/confluenceSettings.properties";
	
	//Objects
	private Properties settings;
	private Logger log = Logger.getLogger(this.getClass());
	private String pageChooserDir; /* The directory the page chooser dialog was open to last */
	private String url;
	private String login;
	private String password;
	private String space;
	private String pattern;
	private String attachmentDirectory;
	private String wikitype;
	private String sendToConfluence;
	private String attachmentSize;
	private String uploadOrphanAttachments;
	private String currentTab;
	private String feedbackOption;
	public Feedback feedback = Feedback.NONE;
	private String truststore;
	private String trustpass;
	private String trustall;
	private String pages; /* double colon delimited list of page paths */
	
	/**
	 * creates the object, and loads settings from the default location
	 */
	public UWCUserSettings() {
		this.settings = getSettingsFromFile();
	}
	
	/**
	 * creates the object, and loads settings from the given location
	 * @param location path to a settings object. If null, settings will not be loaded
	 * from a file.
	 */
	public UWCUserSettings(String location) {
		if (location == null)
			return;
		this.settings = getSettingsFromFile(location);
	}

	/**
	 * save the settings to the file at the default location
	 * @throws IOException if default location doesn't represent a valid useable settings file
	 */
	public void saveSettingsToFile() {
		saveSettingsToFile(DEFAULT_USER_SETTINGS_LOCATION, this.settings);
	}
	
	/**
	 * save the current settings to the file at the given location
	 * @param settingsFilePath
	 * @throws IOException if settingsFilePath doesn't represent a valid useable settings file
	 */
	public void saveSettingsToFile(String settingsFilePath) {
		saveSettingsToFile(settingsFilePath, this.settings);
	}
	
	/**
	 * save the settings to the given location
	 * @param settingsFilePath the given location where the settings will be saved
	 * @param settings all the settings that are to be saved
	 * @throws IOException if settingsFilePath is not a valid path to a usable settings file
	 */
	public void saveSettingsToFile(String settingsFilePath, Properties settings) {
		FileOutputStream fos = null;
        try {
        	if (!new File(settingsFilePath).exists()) 
        		log.warn("Settings file does not exist. Creating " + settingsFilePath);
            fos = new FileOutputStream(settingsFilePath);
            getSettings().store(fos, null);
            //did we get this far?
            this.feedback = Feedback.OK;
        } catch (IOException e) {
        	log.error("Saving user settings: Problem when saving settings to file:\n" + 
        			settingsFilePath +
        			"\n" +
        			"Note: File permissions may need to be changed.");
            this.feedback = Feedback.BAD_SETTINGS_FILE;
        }
        try {
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e) {
        	log.error("Saving user settings: Problem when closing user settings file: " + settingsFilePath);
        	this.feedback = Feedback.BAD_SETTINGS_FILE;
        }
	}

	/**
	 * loads the settings from the file at the given location
	 * @param location
	 * @return the settings as a set of properties. 
	 * See the public CONSTANTS in this file for keys to 
	 * the Properties object.
	 */
	public Properties getSettingsFromFile(String location) {
		// load properties file
		FileInputStream file;
		try {
			file = new FileInputStream(location);
			getSettings().load(file);
			file.close();
			setSettings(this.settings);
			//Did we get this far?
			this.feedback = Feedback.OK;
			return this.settings;
		} catch (FileNotFoundException e) {
			log.warn("No File Found at: " + location);
			this.feedback = Feedback.BAD_SETTINGS_FILE;
		} catch (IOException e) {
			log.error("Problem loading settings from location: " + location);
			this.feedback = Feedback.BAD_SETTINGS_FILE;
		}
		return getSettings();
	}

	/**
	 * @return all the user settings
	 */
	public Properties getSettings() {
		if (this.settings == null) {
			this.settings = new Properties();
		}
		this.settings.put(PROPKEY_PAGE_CHOOSER_DIR, getPageChooserDir());
		this.settings.put(PROPKEY_URL, getUrl());  
		this.settings.put(PROPKEY_LOGIN, getLogin());
		this.settings.put(PROPKEY_PASSWORD, getPassword());
		this.settings.put(PROPKEY_SPACE, getSpace());
		this.settings.put(PROPKEY_PATTERN, getPattern());
		this.settings.put(PROPKEY_ATTACHMENTS, getAttachmentDirectory());
		this.settings.put(PROPKEY_WIKITYPE, getWikitype());
		this.settings.put(PROPKEY_SEND_TO_CONFLUENCE, getSendToConfluence());
		this.settings.put(PROPKEY_ATTACHMENTSIZE, getAttachmentSize());
		this.settings.put(PROPKEY_UPLOAD_ORPHAN_ATTACHMENTS, getUploadOrphanAttachments());
		this.settings.put(PROPKEY_CURRENTTAB, getCurrentTab());
		this.settings.put(PROPKEY_FEEDBACK_OPTION, getFeedbackOption());
		this.settings.put(PROPKEY_TRUSTSTORE, getTruststore());
		this.settings.put(PROPKEY_TRUSTPASS, getTrustpass());
		this.settings.put(PROPKEY_TRUSTALL, getTrustall());
		this.settings.put(PROPKEY_PAGES, getPages());
		return this.settings;
	}

	/**
	 * uses the given settings to set each object representing each setting
	 * @param settings
	 */
	public void setSettings(Properties settings) {
		if (settings == null) {
			log.error("Passed settings object is null.");
			this.feedback = Feedback.BAD_SETTING;
			return;
		}
		setPageChooserDir((String) settings.get(PROPKEY_PAGE_CHOOSER_DIR));
		setUrl((String) settings.get(PROPKEY_URL));
		setLogin((String) settings.get(PROPKEY_LOGIN));
		setPassword((String) settings.get(PROPKEY_PASSWORD));
		setSpace((String) settings.get(PROPKEY_SPACE));
		setPattern((String) settings.get(PROPKEY_PATTERN));
		setAttachmentDirectory((String) settings.get(PROPKEY_ATTACHMENTS));
		setWikitype((String) settings.get(PROPKEY_WIKITYPE));
		setSendToConfluence((String) settings.get(PROPKEY_SEND_TO_CONFLUENCE));
		setAttachmentSize((String) settings.get(PROPKEY_ATTACHMENTSIZE));
		setUploadOrphanAttachments((String) settings.get(PROPKEY_UPLOAD_ORPHAN_ATTACHMENTS));
		setCurrentTab((String) settings.get(PROPKEY_CURRENTTAB));
		setFeedbackOption((String) settings.get(PROPKEY_FEEDBACK_OPTION));
		setTruststore((String) settings.get(PROPKEY_TRUSTSTORE));
		setTrustpass((String) settings.get(PROPKEY_TRUSTPASS));
		setTrustall((String) settings.get(PROPKEY_TRUSTALL));
		setPages((String) settings.get(PROPKEY_PAGES));
	}

	/**
	 * @return gets the settings from the default settings file,
	 * or if that file doesn't exist, returns the default settings. 
	 * 
	 */
	public Properties getSettingsFromFile() {
		return getSettingsFromFile(DEFAULT_USER_SETTINGS_LOCATION);
	}
	
	/* Getters and Setters */
	
	/**
	 * @return user chosen attachment directory or default
	 */
	public String getAttachmentDirectory() {
		if (this.attachmentDirectory == null) 
			this.attachmentDirectory = DEFAULT_ATTACHMENT_DIR;
		return this.attachmentDirectory;
	}

	/**
	 * setter
	 * @param attachmentDirectory
	 */
	public void setAttachmentDirectory(String attachmentDirectory) {
		this.attachmentDirectory = attachmentDirectory;
	}

	/**
	 * @return user chosen login or default
	 */
	public String getLogin() {
		if (this.login == null)
			this.login = DEFAULT_LOGIN;
		return login;
	}

	/**
	 * setter
	 * @param login
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @return last used dialog directory or default
	 */
	public String getPageChooserDir() {
		if (this.pageChooserDir == null)
			this.pageChooserDir = DEFAULT_VALUE;
		return pageChooserDir;
	}

	/**
	 * setter
	 * @param pageChooserDir
	 */
	public void setPageChooserDir(String pageChooserDir) {
		this.pageChooserDir = pageChooserDir;
	}

	/**
	 * @return user chosen pass or default
	 */
	public String getPassword() {
		if (this.password == null)
			this.password = DEFAULT_VALUE;
		return password;
	}

	/**
	 * setter
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return user chosen pattern, or default
	 */
	public String getPattern() {
		if (this.pattern == null)
			this.pattern = DEFAULT_VALUE;
		return pattern;
	}

	/**
	 * setter
	 * @param pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * @return user chosen setting or default
	 */
	public String getSendToConfluence() {
		if (this.sendToConfluence == null)
			this.sendToConfluence = DEFAULT_SENDTOCONF;
		return sendToConfluence;
	}

	/**
	 * setter
	 * @param sendToConfluence
	 */
	public void setSendToConfluence(String sendToConfluence) {
		this.sendToConfluence = sendToConfluence;
	}

	/**
	 * @return user chosen space or default
	 */
	public String getSpace() {
		if (this.space == null)
			this.space = DEFAULT_SPACE;
		return space;
	}

	/**
	 * setter
	 * @param space
	 */
	public void setSpace(String space) {
		this.space = space;
	}

	/**
	 * @return url to Confluence or default url. will strip out protocol, if necessary
	 */
	public String getUrl() { 
		if (this.url == null)
			this.url = DEFAULT_URL;
		return stripProtocol(url);
	}

	/**
	 * setter
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return user chosen wikitype or default
	 */
	public String getWikitype() {
		if (this.wikitype == null)
			this.wikitype = DEFAULT_VALUE;
		return wikitype;
	}

	/**
	 * setter
	 * @param wikitype
	 */
	public void setWikitype(String wikitype) {
		this.wikitype = wikitype;
	}
	
	/**
	 * @return user chosen attachment max size or default
	 */
	public String getAttachmentSize() {
		if (this.attachmentSize == null)
			this.attachmentSize = DEFAULT_ATTACHMENT_SIZE;
		return this.attachmentSize;
	}
	
	/**
	 * setter
	 * @param attachmentSize
	 */
	public void setAttachmentSize(String attachmentSize) {
		this.attachmentSize = attachmentSize;
	}
	
	/**
	 * @return the flag to upload orphan attachments or default
	 */
	public String getUploadOrphanAttachments() {
		if (this.uploadOrphanAttachments == null)
			this.uploadOrphanAttachments = DEFAULT_UPLOAD_ORPHAN_ATTACHMENTS;
		return this.uploadOrphanAttachments;
	}
	
	/**
	 * setter
	 * @param the flag to upload orphan attachments
	 */
	public void setUploadOrphanAttachments(String uploadOrphanAttachments) {
		this.uploadOrphanAttachments = uploadOrphanAttachments;
	}
	
	/**
	 * @return user chosen current tab or default
	 */
	public String getCurrentTab() {
		if (this.currentTab == null)
			this.currentTab = DEFAULT_TAB_INDEX;
		return this.currentTab;
	}
	
	/**
	 * setter
	 * @param tabIndex
	 */
	public void setCurrentTab(String tabIndex) {
		this.currentTab = tabIndex;
	}
	
	/**
	 * @return user chosen feedback setting or default
	 */
	public String getFeedbackOption() {
		if (this.feedbackOption == null)
			this.feedbackOption = DEFAULT_FEEDBACK_OPTION;
		return this.feedbackOption;
	}
	
	/**
	 * setter
	 * @param feedbackOption
	 */
	public void setFeedbackOption(String feedbackOption) {
		this.feedbackOption = feedbackOption;
	}
	
	/**
	 * @return location to truststore that matches SSL CA for url.
	 * Only necessary if working with SSL encrypted confluences.
	 */
	public String getTruststore() {
		if (this.truststore == null)
			this.truststore = DEFAULT_TRUSTSTORE;
		return this.truststore;
	}
	
	/**
	 * setter
	 * @param truststore
	 */
	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}
	
	/**
	 * @return password associated with truststore that matches SSL CA for url.
	 * Only necessary if working with SSL encrypted confluences.
	 */
	public String getTrustpass() {
		if (this.trustpass == null)
			this.trustpass = DEFAULT_TRUSTPASS;
		return this.trustpass;
	}
	
	/**
	 * setter
	 * @param trustpass
	 */
	public void setTrustpass(String trustpass) {
		this.trustpass = trustpass;
	}

	/**
	 * @return "true", "false", null, or "".
	 * Note: null and empty will be translated to true.
	 * If this setting is true, the Confluence Remote
	 * Java Wrapper will trust all certificates. This
	 * could lead to man in the middle vulnerabilities.
	 */
	public String getTrustall() {
		if (this.trustall == null)
			this.trustall = DEFAULT_TRUSTALL;
		return this.trustall;
	}
	
	/**
	 * setter
	 * @param trustall
	 */
	public void setTrustall(String trustall) {
		this.trustall = trustall;
	}

	
	/**
	 */
	public String getPages() {
		if (this.pages == null)
			this.pages = DEFAULT_PAGES;
		return this.pages;
	}
	
	/**
	 * setter
	 * @param pages
	 */
	public void setPages(String pages) {
		this.pages = pages;
	}

	/**
	 * Sets the given user setting with the given value
	 * @param setting
	 * @param value
	 */
	public void setOneSetting(Setting setting, String value) {
		switch(setting) {
		case ATTACHMENTS:
			setAttachmentDirectory(value);
			break;
		case LOGIN:
			setLogin(value);
			break;
		case PAGE_CHOOSER_DIR:
			setPageChooserDir(value);
			break;
		case PASSWORD:
			setPassword(value);
			break;
		case PATTERN:
			setPattern(value);
			break;
		case SEND_TO_CONFLUENCE:
			setSendToConfluence(value);
			break;
		case SPACE:
			setSpace(value);
			break;
		case URL:
			setUrl(value);
			break;
		case WIKITYPE:
			setWikitype(value);
			break;
		case ATTACHMENT_SIZE:
			setAttachmentSize(value);
			break;
		case UPLOAD_ORPHAN_ATTACHMENTS:
			setUploadOrphanAttachments(value);
			break;
		case CURRENT_TAB:
			setCurrentTab(value);
			break;
		case FEEDBACK_OPTION:
			setFeedbackOption(value);
			break;
		case PAGES:
			setPages(value);
			break;
		}
	}
	
	/**
	 * @param setting
	 * @return the value for the given setting
	 */
	public String getSetting(Setting setting) {
		switch(setting) {
		case ATTACHMENTS:
			return getAttachmentDirectory();
		case LOGIN:
			return getLogin();
		case PAGE_CHOOSER_DIR:
			return getPageChooserDir();
		case PASSWORD:
			return getPassword();
		case PATTERN:
			return getPattern();
		case SEND_TO_CONFLUENCE:
			return getSendToConfluence();
		case SPACE:
			return getSpace();
		case URL:
			return getUrl();
		case WIKITYPE:
			return getWikitype();
		case ATTACHMENT_SIZE:
			return getAttachmentSize();
		case UPLOAD_ORPHAN_ATTACHMENTS:
			return getUploadOrphanAttachments();
		case CURRENT_TAB:
			return getCurrentTab();
		case FEEDBACK_OPTION:
			return getFeedbackOption();
		case PAGES:
			return getPages();
		}
		return "";
	}
	
	static String protocol = "http:\\/\\/(.*)";
	static Pattern protocolPattern = Pattern.compile(protocol);
	/**
	 * removes http:// protocol from given url
	 * @param url
	 * @return url without protocal. Example, if url is 'http://localhost', then the return value would be 'localhost'
	 */
	public static String stripProtocol(String url) { 
		Matcher protocolFinder = protocolPattern.matcher(url);
		if (protocolFinder.find()) {
			return protocolFinder.group(1);
		}
		return url;
	}
	static String brokenProtocol = "([^:]+):\\/\\/(.*)";
	static Pattern brokenProtocolPattern = Pattern.compile(brokenProtocol);
	static String brokenProtocol2 = "https?(.*)";
	static Pattern brokenProtocol2Pattern = Pattern.compile(brokenProtocol2);
	/**
	 * @param setting
	 * @param value
	 * @return true if the given value for the given setting is valid
	 */
	public static boolean isValid(Setting setting, String value) {
		switch (setting) {
		case URL:
			Matcher brokenProtocolFinder = brokenProtocolPattern.matcher(value);
			boolean validProtocol = true;
			if (brokenProtocolFinder.lookingAt()) {
				String type = brokenProtocolFinder.group(1);
				validProtocol = "http".equals(type) || "https".equals(type);
			}
			Matcher brokenProtocol2Finder = brokenProtocol2Pattern.matcher(value);
			boolean validDelimiter = true;
			if (brokenProtocol2Finder.find()) {
				String delim = brokenProtocol2Finder.group(1);
				validDelimiter = delim.startsWith("://");
			}
			return validProtocol && validDelimiter;
		}
		return true;
	}
}
