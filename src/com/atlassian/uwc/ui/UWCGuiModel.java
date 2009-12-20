package com.atlassian.uwc.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;
import com.atlassian.uwc.ui.listeners.FeedbackHandler.Feedback;
import com.atlassian.uwc.util.PropertyFileManager;


/**
 * This object contains the underlying data for the UWC. It's essentially a gateway to allow
 * different classes to communicate with each other. For example, it:
 * - provides access to the file system for
 * -- getting saved and saving user settings
 * -- converter properties files
 * -- info about existance of properties files
 * - maintains user chosen wiki pages information
 * - provides access to the converter engine, for converting, and also getting error codes and feedback
 *   from the ConverterEngine
 * Note: heavily influenced by UWCForm2 and ChooseWikiForm code from the
 * UWC GUI v.2
 */
public class UWCGuiModel {
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * list of wiki converters, gathered from converter.xxx.properties files
	 */
	private Vector<String> converterListModel = null;
	/**
	 * list of wiki exporters, gathered from exporter.xxx.properties files
	 */
	private Vector<String> exportListModel = null;
	/**
	 * list of user chosen pages
	 */
	private Vector<String> pageNames = null;
	/**
	 * engine that runs the conversions
	 */
	private ConverterEngine engine = null;
	/**
	 * user settings
	 */
	private UWCUserSettings userSettings = null;
	/**
	 * which setting the user is currently changing, and has not been saved yet
	 */
	private UWCUserSettings.Setting unsaved = null;
	
	public UWCGuiModel() {
		userSettings = new UWCUserSettings();
		loadSavedPages(userSettings);
	}
	
	public UWCGuiModel(UWCUserSettings settings) {
		this.userSettings = settings;
		loadSavedPages(userSettings);
	}
	
	/**
     * gets the list of available wiki types that can be converted
	 * @param parentDir directory in which the wiki properties files can be found
	 * Example: "conf"
	 * @return list of wiki types
     */
    public Vector<String> getWikiTypesList(String parentDir) {
    	FilenameFilter filter = new UWCConverterPropFileFilter();
    	return getFromFileList(parentDir, filter, this.converterListModel);
    }
    
    /**
     * gets the list of available exportable wiki types
     * @param parentDir directory in which export properties files can be found
     * Example: "conf"
     * @return list of export wiki types
     */
    public Vector<String> getExportTypes(String parentDir) {
    	FilenameFilter filter = new UWCConverterExportFileFilter();
    	return getFromFileList(parentDir, filter, this.exportListModel);
    }
    
    /**
     * creates or gets a list of wiki types that 
     * exist in the given parentDir,
     * conform to the given filter,
     * and are maintained in the given model
     * @param parentDir directory in which the files representing the desired data exist
     * @param filter file filter which restricts which files contain the desired data
     * @param model object that maintains the desired data. If it is not null, the return value
     * will be this object
     * @return model or newly created list of wiki types
     */
    public Vector<String> getFromFileList(String parentDir, FilenameFilter filter, Vector<String> model) {
    	//return existing object, if it exists
    	if (model != null)
    		return model;

    	File confDir = new File(parentDir);
        
    	//check for errors
    	if (!confDir.exists()) {
        	log.error("confDir " + confDir.getAbsolutePath() + " does not exist");
        	return new Vector<String>(); //return empty list
        }
        if (!confDir.isDirectory()) {
        	log.error("confDir " + confDir.getAbsolutePath() + " is not a directory!");
        	return new Vector<String>();
        }
        
        //create the list
        File[] files = confDir.listFiles(filter);
        model = new Vector<String>();
        for (File file : files) {
            String fileName = file.getName();
            StringTokenizer st = new StringTokenizer(fileName, ".");
            st.nextToken();
            fileName = st.nextToken();
            model.add(fileName);
        }
        return model;
    }

    /**
     * @param files adds the given files to the pages state
     * @return current list of pages after additions
     */
    public Vector<String> addWikiPages(File[] files) {
        pageNames = getPageNames();
        for (File file : files) {
        	pageNames.add(file.getPath());
        }
        return this.pageNames;
    }

	/**
	 * @return the directory that the user chose pages from most recently
	 */
	public String getPageChooserDir() {
		return this.userSettings.getPageChooserDir();
	}

    /**
     * removes the given pages from the internal list of files
     * @param files 
     * @return resulting list of pagenames after removals
     */
    public Vector<String> removeWikiPages(Object[] files) {
    	
        for (Object file : files) 	{
            String item = (String) file;
            pageNames.remove(item);
        }
    	return pageNames;
    }
    
    /**
     * run the conversion
     * @param propsPath path to the converter properties file that will
     * be used to run the conversion
     * @throws IOException, {@link IllegalArgumentException} if there are problems reading the given converter
     * properties files
     */
    public void convert(String propsPath) throws IOException, IllegalArgumentException {
		ConverterEngine engine = getConverterEngine();
		List<File> pages = getPageFiles();
		List<String> converters = getConverters(propsPath);

		engine.convert(pages, converters, this.userSettings);
	}
    
    /**
     * cancels the currently being run conversion
     */
    public void cancelConvert() {
    	ConverterEngine engine = getConverterEngine();
    	engine.cancel();
    }
    
    /**
     * gets a list of converter strings from the file at the given propsPath
     * @param propsPath converter.xxx.properties file representing this wiki
     * @return list of converter strings
     * @throws IOException if can't load the properties from the given file at propsPath
     */
    protected List<String> getConverters(String propsPath) throws IOException {
		File props = new File(propsPath);
		if (!props.exists()) {
			String message = "No property file at that location: " + propsPath;
			log.error(message);
			throw new IllegalArgumentException(message);
		}
		
		TreeMap<String,String> converters = null; 
        converters = PropertyFileManager.loadPropertiesFile(propsPath);
        
        if (converters == null)
        	throw new IllegalArgumentException(); //unlikely, as the error handling above should be sufficient
		
        return getConvertersAsStrings(converters);
	}

	/**
	 * gets a list of syntax converters from the given converters map
	 * @param converters
	 * @return list of syntax converters
	 */
	protected List<String> getConvertersAsStrings(TreeMap<String, String> converters) {
		Vector<String> converterStrings = new Vector<String>(converters.keySet().size());
		for (String converter : converters.keySet()) {
			String value = converters.get(converter);
			String converterString = converter + "=" + value;
			converterStrings.add(converterString);
		}
		return converterStrings;
	}

	/**
     * @return creates a list of File objects from the saved pagenames state
     */
    protected List<File> getPageFiles() {
    	Vector<File> files = new Vector<File>();
    	this.pageNames = getPageNames();
    	for (String path : this.pageNames) {
			File file = new File(path);
			files.add(file);
		}
		return files;
	}

	/**
	 * @return gets the converter engine used to run conversions
	 */
	private ConverterEngine getConverterEngine() {
    	if (this.engine == null) {
    		this.engine = new ConverterEngine();
    	}
		return this.engine;
	}

	/**
	 * @return the page names representing the pages the user has chosen to convert
	 */
	public Vector<String> getPageNames() {
		if (this.pageNames == null)
			this.pageNames = new Vector<String>();
		return this.pageNames;
	}

	
	Pattern paths = Pattern.compile("" +
			"(.*?)" +		//everything until 
			"(?>::|$)"); 	//double colon or end of string
	/**
	 * loads the saved pages setting data into the model's pagenames object
	 * @param settings
	 */
	private void loadSavedPages(UWCUserSettings settings) {
		String pagestring = settings.getSetting(Setting.PAGES);
		Matcher pathFinder = paths.matcher(pagestring);
		Vector<String> pagenames = getPageNames();
		while (pathFinder.find()) {
			String path = pathFinder.group(1);
			if ("".equals(path)) continue;
			pagenames.add(path);
		}
	}
	
	/**
	 * file filter that accepts converter properties files
	 */
	public class UWCConverterPropFileFilter implements FilenameFilter {

        private static final String PROPFILE_PREFIX = "converter";
		private static final String PROPFILE_SUFFIX = ".properties";

		public boolean accept(File dir, String name) {
            if (name.equalsIgnoreCase(PROPFILE_PREFIX + PROPFILE_SUFFIX)) return false;
            if (name.startsWith(PROPFILE_PREFIX) &&
                    name.endsWith(PROPFILE_SUFFIX)) {
                return true;
            } 
            return false;
        }
    }
    
    /**
     * file filter that accepts exporter properties files
     */
    public class UWCConverterExportFileFilter implements FilenameFilter {

        private static final String FILE_PREFIX = "exporter";
		private static final String FILE_SUFFIX = ".properties";

		public boolean accept(File dir, String name) {
            if (name.equalsIgnoreCase(FILE_PREFIX + FILE_SUFFIX)) return false;
            if (name.startsWith(FILE_PREFIX) &&
                    name.endsWith(FILE_SUFFIX)) {
                return true;
            } 
            return false;
        }		
    }
    
	/**
	 * @return directory where the converted files will be saved before upload
	 */
	public static String getOutputDir() {
		return "output" + File.separator + "output"; 
	}

	/**
	 * sets the given setting with the given value. And
	 * saves all the current settings to the settings file
	 * @param setting
	 * @param value
	 * @return feedback on the process of saving the file
	 */
	public Feedback saveSetting(Setting setting, String value) {
		this.userSettings.setOneSetting(setting, value);
		this.userSettings.saveSettingsToFile();
		return this.userSettings.feedback;
	}

	/**
	 * saves all settings to the file system
	 */
	public void saveAllSettings() {
		log.debug("Saving All Settings");
		this.userSettings.saveSettingsToFile();
	}
	
	/**
	 * @param setting
	 * @return gets the value for the given setting
	 */
	public String getSetting(Setting setting) {
		return this.userSettings.getSetting(setting);
	}

	/**
	 * registers the given feedback window by:
	 * * getting the state from the engine
	 * * assigning that state to the feedback window
	 * @param feedbackWindow
	 */
	public void registerFeedbackWindow(FeedbackWindow feedbackWindow) {
		ConverterEngine engine = getConverterEngine();

		State state = engine.getState(this.userSettings);
		
		feedbackWindow.setState(state);
	}

	/**
	 * @return gets the feedback from the converter engine
	 */
	public Feedback getConverterFeedback() {
		ConverterEngine engine = getConverterEngine();
		return engine.getConverterFeedback();
	}

	/**
	 * @return gets and clears error messages
	 */
	public ConverterErrors getErrors() {
		ConverterEngine engine = getConverterEngine();
		return engine.getErrors();
	}

	/**
	 * @return true if the conversion generated errors
	 */
	public boolean getHadConverterErrors() {
		ConverterEngine engine = getConverterEngine();
		return engine.hadConverterErrors();
	}

	/**
	 * @return the setting that is currently being updated by the user
	 */
	public Setting getUnsaved() {
		return this.unsaved;
	}
	
	/**
	 * setter
	 * @param setting
	 */
	public void setUnsaved(Setting setting) {
		this.unsaved = setting;
	}
}
