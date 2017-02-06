package com.atlassian.uwc.exporters;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * unzips the Jotspot export zip file, and deletes unnecessary files.
 * Input zip and Output directory are set in exporter.jotspot.properties.
 * Optionally, a comma delimited list of protected files and directories 
 * can be set in the exporter.jotspot.properties
 * @author Laura Kolker
 */
public class JotspotExporter implements Exporter {
	//CONSTANTS
	//properties constants
	public static final String DEFAULT_PROPERTIES_LOCATION = "exporter.jotspot.properties";
	public static final String EXPORTER_PROPERTIES_INFILE = "exported.zipfile.location";
	public static final String EXPORTER_PROPERTIES_OUTPUTDIR = "exported.output.dir";
	public static final String EXPORTER_PROPERTIES_PROTECTED = "exported.protected.directories"; //optional comma delim list
	public static final String[] DEFAULT_EXCLUDES = {
		"Calendar",
		"Calendar.xml",
		"FileCabinet",
		"FileCabinet.xml",
		"GroupManagement.xml",
		"Lib",
		"Lib.xml",
		"META-INF",
		"MasterIndex.xml",
		"PhotoGallery",
		"PhotoGallery.xml",
		"Spreadsheet",
		"Spreadsheet.xml",
		"ToDoList",
		"ToDoList.xml",
		"WikiMarkupTips.xml",
		"_Admin",
		"_Admin.xml",
		"_deleted",
		"System/Actions",
		"System/Actions.xml",
	    "System/Async",
	    "System/Async.xml",
	    "System/CSS",
	    "System/CSS.xml",
	    "System/ColorSchemes",
	    "System/ColorSchemes.xml",
	    "System/Actions",
	    "System/Actions.xml",
	    "System/Async",
	    "System/Async.xml",
	    "System/CSS",
	    "System/CSS.xml",
	    "System/ColorSchemes",
	    "System/ColorSchemes.xml",
	    "System/Defaults.xml",
	    "System/Errors",
	    "System/Errors.xml",
	    "System/Forms",
	    "System/Forms.xml",
	    "System/Includes",
	    "System/Includes.xml",
	    "System/JotPlan",
	    "System/JotPlan.xml",
	    "System/Locale",
	    "System/Locale.xml",
	    "System/Packages",
	    "System/Packages.xml",
	    "System/Pages",
	    "System/Pages.xml",
	    "System/Plugins",
	    "System/Plugins.xml",
	    "System/SVG",
	    "System/SVG.xml",
	    "System/SystemShared",
	    "System/SystemShared.xml",
	    "System/Themes",
	    "System/Themes.xml",
	    "System/ToDoList",
	    "System/ToDoList.xml",
	    "System/WebspaceConfig.xml",
	    "System/WebspaceEmailConfig",
	    "System/WebspaceEmailConfig.xml"

	};
	//status constants
	private static final String STATUS_OK = "OK";
	private static final String STATUS_ERROR = "ERROR";
	private static final String STATUS_CANCELLED = "CANCELLED";
	//log4j
	private Logger log = Logger.getLogger(this.getClass());
	//status
	private String status = STATUS_OK;
	//cancel
	private boolean running = false;
	
	/**
	 * entry method if we use this class as an App.
	 * properties must be in exporter.jotspot.properties
	 * @param args
	 */
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		JotspotExporter exp = new JotspotExporter();
		Map propsMap = exp.getProperties(DEFAULT_PROPERTIES_LOCATION);

		exp.export(propsMap);
	}
	
	/**
	 * default properties grabber. used by main when this class is used as an app.
	 * @param filename path to properties file
	 * @return map of properties from properties file
	 */
	private Map getProperties(String filename) {
		Properties props = new Properties();
		Map<String, String> propsMap = new HashMap<String, String>();
		filename = "conf/"+filename;
		try {
			props.load(new FileInputStream(filename));
			propsMap.put(EXPORTER_PROPERTIES_INFILE, props.getProperty(EXPORTER_PROPERTIES_INFILE));
			propsMap.put(EXPORTER_PROPERTIES_OUTPUTDIR, props.getProperty(EXPORTER_PROPERTIES_OUTPUTDIR));
			propsMap.put(EXPORTER_PROPERTIES_PROTECTED, props.getProperty(EXPORTER_PROPERTIES_PROTECTED));
		} catch (FileNotFoundException e) {
			log.error("Cannot find properties file");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Cannot load properties file");
			e.printStackTrace();
		}
		return propsMap;
	}

	/**
	 * exports the Mediawiki database described in the given properties
	 * to text files that will be written to the output directory
	 * @param properties Map of properties. Must contain keys: databaseName, 
	 * dbUrl, jdbc.driver.class, login, password, output. See example file
	 * exporter.mediawiki.properties
	 */
	public void export(Map properties) {
		this.running = true;
		//set up log4j
		PropertyConfigurator.configure("log4j.properties");
		//setup database connection
		log.info("Exporting Jotspot...");
		
		if (missingRequiredProperties(properties)) {
			this.status = STATUS_ERROR + ": required properties are not set. " +
					"Please configure " + DEFAULT_PROPERTIES_LOCATION;
			log.error(this.status);
			return;
		}
		//prepare infile
		File infile = getInfile(properties);
		if (infile == null) return;
	
		//prepare output directory
		File outdir = getOutdirectory(properties);
		if (outdir == null) return;
		
		//prepare include/exclude map
		TreeMap<String, Boolean> excludeInclude = getIncludeExcludeMap(properties);
		
		//unzip infile to output directory
		expand(infile, outdir, excludeInclude);
		if (!STATUS_OK.equals(this.status)) {
			log.error("Export Status: " + this.status);
			return;
		}
		
		log.info("Export Complete.");
		this.running = false;
	}


	/**
	 * @param properties String Map containing all export properties
	 * @return true if any required property is null or empty.
	 */
	private boolean missingRequiredProperties(Map<String, String> properties) {
		String in = properties.get(EXPORTER_PROPERTIES_INFILE);
		String out = properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		return (in == null || out == null | "".equals(in) || "".equals(out));
	}

	/**
	 * @param properties String Map with key-value pair, where a key = EXPORTERS_PROPERTIES_INFILE
	 * @return File object associated with EXPORTER_PROPERTIES_INFILE, or null if no such non-directory
	 * object exists
	 */
	private File getInfile(Map<String, String> properties) {
		if (!this.running) {
			this.status = STATUS_CANCELLED; 
			return null;
		}
		String in = properties.get(EXPORTER_PROPERTIES_INFILE);
		File file = new File(in);
		if (!file.exists() || file.isDirectory()) {
			this.status = STATUS_ERROR + ": Zip file does not exist or is a directory: " + in; 
			log.error(this.status);
			return null;
		}
		log.info("Zip file: " + in);
		return file;
	}

	/**
	 * @param properties String Map with key-value pair, where a key = EXPORTERS_PROPERTIES_OUTPUTDIR
	 * @return File object representing empty directory associated with EXPORTER_PROPERTIES_OUTPUTDIR.
	 * If dir does not exist, it will be created.
	 * If dir already exists and is a directory, the directory will be deleted and recreated.
	 * If dir already exists but is not a directory, this method will return null.
	 */
	private File getOutdirectory(Map<String, String> properties) {
		if (!this.running) {
			this.status = STATUS_CANCELLED; 
			return null;
		}
		String out = properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		File file = new File(out);
		if (!file.exists()) {
			log.info("Creating output directory: " + out);
			file.mkdir();
		}
		else if (file.isDirectory()) {
			deleteDir(file);
			file.mkdir();
			log.info("Cleaning and creating output directory:" + out);
		}
		else {
			this.status = STATUS_ERROR + ": requested output directory (" +
					out +
					") is currently a file. Please choose a directory. ";
			log.error(this.status);
			return null;
		}
		return file;
	}

	/**
	 * @param properties String Map with key-value pair, where a key = EXPORTERS_PROPERTIES_PROTECTED
	 * @return Map String->Boolean key value pairs
	 * keys: file paths
	 * value: True if should be included (is protected), False if it should be excluded
	 */
	private TreeMap<String, Boolean> getIncludeExcludeMap(Map<String, String> properties) {
		if (!this.running) {
			this.status = STATUS_CANCELLED; 
			return null;
		}
		TreeMap<String, Boolean> ieMap = new TreeMap<String, Boolean>();
		//add default excludes
		for (String excludable : DEFAULT_EXCLUDES) {
			if (!this.running) {
				this.status = STATUS_CANCELLED; 
				return null;
			}
			ieMap.put(excludable, false);
		}
		//add properties set includes (Might trump default excludes.)
		String protectedPaths = properties.get(EXPORTER_PROPERTIES_PROTECTED);
		if (protectedPaths != null && !"".equals(protectedPaths) ) {
			String[] paths = protectedPaths.split(", ");
			for (String includable : paths) {
				if (!this.running) {
					this.status = STATUS_CANCELLED; 
					return null;
				}
				ieMap.put(includable, true);
			}
		}
		return ieMap;
	}
	
	/**
	 * unzip, leaving out unwanted Jotspot directories, but keeping
	 * protected directories
	 * @param infile from here
	 * @param outdir to there
	 * @param excludeInclude map of String->Boolean objects.
	 * keys are filenames/directories
	 * values are True if we include it, False if we exclude it
	 */
	private void expand(File infile, File outdir,
			TreeMap<String, Boolean> excludeInclude) {
		if (!this.running) {
			this.status = STATUS_CANCELLED; 
			return;
		}
		ZipInputStream zip = null;
		int BUFFER = 512;
		try {
			FileInputStream inStream = new FileInputStream(infile);
			BufferedOutputStream zipout = null;
			zip = new ZipInputStream(new BufferedInputStream(inStream));
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				if (!this.running) {
					this.status = STATUS_CANCELLED; 
					break; //break, not return, so we can close the file stream
				}
				if (exclude(entry.getName(), excludeInclude))
					continue;
				int count;
				byte data[] = new byte[BUFFER];
				
				createAnyNecessaryDirectories(outdir, entry.getName());
				
				String entryPath = outdir + "/" + entry.getName();
				FileOutputStream outStream = new FileOutputStream(entryPath);
				zipout = new BufferedOutputStream(outStream, BUFFER);
				while ((count = zip.read(data, 0, BUFFER)) != -1) {
					zipout.write(data, 0, count);
				}
				zipout.flush();
				zipout.close();
			}
			zip.close();
		} catch (FileNotFoundException e) {
			this.status = STATUS_ERROR + ": Could not expand file: " + infile.getName();
			log.error(this.status);
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * @param path file path
	 * @param excludeInclude keys should be paths (could be directories), values should be 
	 * booleans: true for include, false for exclude
	 * @return true if the given path should be excluded. false, if it should be included.
	 */
	private boolean exclude(String path, TreeMap<String, Boolean> excludeInclude) {
		// look for keys that are equivalent first
		if (excludeInclude.containsKey(path)) {
			return !excludeInclude.get(path);
		}
		
		// sort the [ex/in]clude keyset by length
		// important for handling deep paths 
		Set<String> keySet = (Set<String>) excludeInclude.keySet();
		Vector<String> keys = new Vector<String>();
		keys.addAll(keySet);
		LongestItems longestFirst = new LongestItems();
		Collections.sort(keys, longestFirst);
		
		// check for directory include/excludes
		for (String key : keys) {
			if (path.startsWith(key)) {
				return !excludeInclude.get(key); 
			}
		}
		// include by default
		return false;
	}

	/**
	 * creates any necessary directories from name in outdir
	 * @param outdir existing directory
	 * @param name filepath that shoudl go in outdir eventually
	 */
	private void createAnyNecessaryDirectories(File outdir, String name) {
		String completePath = outdir.getAbsolutePath() + "/" + name;
		Stack<File> candidates = new Stack<File>();
		//look at each parent dir (from bottom up) for existence
		while (!"".equals(completePath)) {
			completePath = getLowestDir(completePath);
			File testFile = new File(completePath);
			if (!testFile.exists()) {
				candidates.push(testFile);
			}
			else {
				break;
			}
		}
		//non existing parent directories are created
		while (!candidates.isEmpty()) {
			File tmpFile = candidates.pop();
			tmpFile.mkdir();
		}
	}

	Pattern lowestPath = Pattern.compile("(.*)\\/[^\\/]*");
	/**
	 * @param path
	 * @return the deepest parent directory.
	 * Example: 
	 * path = "Some/Directory/file.txt
	 * return = "Some/Directory"
	 */
	private String getLowestDir(String path) {
		Matcher pathFinder = lowestPath.matcher(path);
		if (pathFinder.lookingAt()) {
			String parent = pathFinder.group(1);
			return parent;
		}
		return path;
	}

	/**
	 * @return returns export status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * deletes the given file. This method is used recursively.
	 * @param file can be a directory or a file. Directory does not have to be empty.
	 */
	private void deleteDir(File file) {
		//if file doesn't exist (shouldn't happen), just exit
		if (!file.exists()) return;
		String name = "";
		try {
			name = file.getCanonicalPath();
		} catch (IOException e) {
			log.error("Problem while deleting directory. No filename!");
			e.printStackTrace();
		}
		//delete the file
		if (file.delete()) {
			log.debug("Deleting " + name);
			return;
		}
		else { // or delete the directory
			File[] files = file.listFiles();
			for (File f : files) {
				deleteDir(f);
			}
			file.delete();
			log.debug("Deleting dir: " + name);
		}
		
	}

	public void cancel() {
		String message = "Jotspot Exporter - Sending Cancel Signal";
    	log.debug(message);
    	this.running = false;
	}
	
	/**
	 * @author Laura Kolker
	 * Comparator for sorting by string length. Lengthier strings first.
	 */
	public class LongestItems implements Comparator {

		public int compare(Object a, Object b) {
			
			String aStr = (String) a;
			int aLen = aStr.length();
			
			String bStr = (String) b;
			int bLen = bStr.length();
			
			return (bLen - aLen); 
			
		}
		
	}
}