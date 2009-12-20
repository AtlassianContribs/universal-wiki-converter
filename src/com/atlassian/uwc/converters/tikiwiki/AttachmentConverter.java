package com.atlassian.uwc.converters.tikiwiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.FileUtils;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.ui.UWCUserSettings.Setting;
import com.atlassian.uwc.util.PropertyFileManager;


/**
 * Prepares tikiwiki attachments for uploading to Confluence.
 * NOTE: This class was heavily influenced by TwikiPrepareAttachmentFilesConverter
 * Database methods were from MediawikiExporter
 * @author Laura Kolker
 */
public class AttachmentConverter extends BaseConverter {
	private static final String PREF_COL = "value";
	protected static final String FILE_GAL_PREF_SQL = "select value from tiki_preferences where name=\"fgal_use_dir\";";
	protected static final String IMAGE_GAL_PREF_SQL = "select value from tiki_preferences where name=\"gal_use_dir\";";
	protected static String FILE_SEP = System.getProperty("file.separator");
	Logger log = Logger.getLogger(this.getClass());
	private Connection con; //DB connection

	/* properties fields */
	private String propertyLocation;
	protected TreeMap<String,String> properties;
	private String output;
	private String dbName;
	private String login;
	private String password;
	private String dbUrl;
	private String jdbcDriver;

	/* CONSTANTS */
	private static final String DEFAULT_PROPERTIES_LOCATION = "settings.tikiwiki.properties";
	private static final String DEFAULT_PROPERTIES_DIR = "conf/";
	protected static final String PROPERTIES_CONVERTGALLERY="Tikiwiki.gallery-conversion-on.switch";
	private static final String PROPERTIES_OUTPUTDIR = "Tikiwiki.output-dir.setting";
	private static final String PROPERTIES_DBNAME = "Tikiwiki.dbname.dbSetting";
	private static final String PROPERTIES_LOGIN = "Tikiwiki.login.dbSetting";
	private static final String PROPERTIES_PASSWORD = "Tikiwiki.password.dbSetting";
	private static final String PROPERTIES_DBURL = "Tikiwiki.connection-url.dbSetting";
	private static final String PROPERTIES_DRIVER = "Tikiwiki.driver.dbSetting";

	public enum GalleryType {
		IMAGE,
		FILE
	};
	
	public void convert(Page page) {
		log.debug("Converting Tikiwiki Attachments -- starting");
        // scan the page and create a list of attachments
        addAttachmentsToPage(page, this.getAttachmentDirectory());
		log.debug("Converting Tikiwiki Attachments -- complete");

	}

    /**
     * looks for attachments and attaches them
     * @param page object to attach page
     * s to
     */
    protected void addAttachmentsToPage(Page page, String attachDir) {
    	//simplify image syntax to one code path
    	String standardized = standardizeImageSyntax(page.getOriginalText());

    	//two types of attachments, images uploaded to pages & gallery files

    	//images uploaded to pages
    	Vector<String> uploadPaths = getUploadPaths(standardized, attachDir);
    	//gallery files
    	Vector<String> paths = null;
    	this.propertyLocation = getPropertyLocation();
    	if(readDBProperties(this.propertyLocation) && isGalleryConversion()) {
    		//fix the syntax
	    	standardized = replaceIdsWithNames(standardized);
	    	//get the paths to the files
	    	paths = getAllGalleryPaths(
	    			standardized, attachDir, uploadPaths);
    	}
    	if (paths == null) paths = uploadPaths; //no gallery paths
    	attach(paths, page);

    	//save changes to image syntax
    	page.setConvertedText(standardized);
    	closeDB();
    }

    /**
     * translates any references to images with id numbers,
     * to images with filenames
     * @param input wiki syntax
     * @return input with ids translated to filenames
     */
    protected String replaceIdsWithNames(String input) {
    	//fix image gallery ids
    	String sql = "select imageId,name from tiki_images;";
    	String colId = "imageId";
    	String colName = "name";
		HashMap<String,String> imageNames = getIdsAndNames(sql, colId, colName);
		
		//fix file gallery ids
		sql = "select fileId, fileName from tiki_files;";//XXX refactor!
		colId = "fileId";
		colName = "fileName";
		HashMap<String,String> fileNames = getIdsAndNames(sql, colId, colName);
		
		//replace the input ids with filenames
		input = replaceImageIds(input, imageNames, GalleryType.IMAGE);
		input = replaceImageIds(input, fileNames, GalleryType.FILE);
		return input;
	}

	/**
	 * gets a vector of all unique filepaths for gallery images or files.
	 * @param input wiki syntax that refers to gallery attachments
	 * @param attachDir directory where these files would live (attachment directory in the UI)
	 * @param existingPaths vector of paths that will be combined with the
	 * paths that will be found
	 * @return vector containing the given existing paths, and any paths to gallery images and files.
	 * 
	 */
	protected Vector<String> getAllGalleryPaths(String input, String attachDir, Vector<String> existingPaths) {
		//get image gallery paths
		Vector<String> imageGalleryPaths = getImageGalleryPaths(input, attachDir);
		//get file gallery paths
		Vector<String> fileGalleryPaths = getFileGalleryPaths(input, attachDir);
		
		//combine and uniquify
		Vector<String> paths = combineVectors(existingPaths, imageGalleryPaths);
		paths = combineVectors(paths, fileGalleryPaths);
		return paths;
	}

	/**
	 * makes all image syntax follow the sames rules 
	 * regarding quotes and using http urls
	 * @param input input with tikiwiki img syntax
	 * @return all img syntax, standardized to the same rules 
	 */
	protected String standardizeImageSyntax(String input) {
		String standardized = standardizeQuotes(input);
		standardized = standardizeUrl(standardized);
		return standardized;
	}

	String noQuotes = "(\\{img src=)([^\"} ]+)([ }])";
	/**
	 * makes all image syntax use the same quotes rules.
	 * Example: If the input contains {img src=something}
	 * then the return value would be {img src="something"}
	 * @param input input with tikiwiki img syntax
	 * @return input with all img syntax using the same rules for quotes
	 */
	private String standardizeQuotes(String input) {
		String standardized = decodeEntities(input);
		String replacement = "{group1}\"{group2}\"{group3}";
		standardized = RegexUtil.loopRegex(standardized, noQuotes, replacement);
		return standardized;
	}
	
	String noProtocol = "(\\{img\\s+src=\")http.*?((tiki-download_file)|(img/wiki_up))";
	/**
	 * removes all references to URLs in image syntax. 
	 * (We assume that we have access to the file system (through
	 * the attachment directory setting in the UI), and to the 
	 * database (settings.tikiwiki.properties), and therefore,
	 * references to the URL of this tikiwiki are unnecessary.
	 * @param input tikiwiki input with img syntax
	 * @return all img syntax has been standardized regarding urls
	 */
	protected String standardizeUrl(String input) {
		String replacement = "{group1}{group2}";
		return RegexUtil.loopRegex(input, noProtocol, replacement);
	}

	String script = ".*\\.php\\?.*$";
	Pattern scriptPattern = Pattern.compile(script);
	/**
	 * attaches given String Vector of paths to page
	 * @param paths
	 * @param page
	 */
	protected void attach(Vector<String> paths, Page page) {
		//foreach path in paths
		for (String path : paths) {
			//get the complete path to the file

			log.debug("complete path = " + path);

			//confirm existance of file
			File file = new File(path);
			if (!file.exists() || file.isDirectory()) {
				if (path.contains("http://")) continue; 
	        	Matcher scriptMatcher = scriptPattern.matcher(path);
	        	//if it's not a reference to a php script (which will need the DB to translate)
	        	if (!scriptMatcher.find()) { // give the user an error message
	        		String message = "Attachment '" + path + "' " +
	        		"does not exist or is a directory. Skipping.";
	        		log.warn(Feedback.BAD_FILE + ": " + message);
	        		addError(Feedback.BAD_FILE, message, true);
	        	}
	        	continue;
	        }

			//attach the file
			log.info("adding attachment: " + file.getName());
			log.debug("attachment path: " + file.getPath());
			page.addAttachment(file);
		}
	}

	
	String imgSrc = "\\{img src=\"([^\"]+)\"";
	Pattern imgPattern = Pattern.compile(imgSrc);
	String notUploadPath = "show_image\\.php";
	Pattern notUploadPattern = Pattern.compile(notUploadPath);
	/**
	 * @param input page text
	 * @param attachDir directory to tikiwiki
	 * @return list of absolute paths to images
	 */
	protected Vector<String> getUploadPaths(String input, String attachDir) {
		log.debug("Getting Upload Paths");
		Vector<String> paths = new Vector<String>();

		
		String root = attachDir; //XXX this needs to be the directory to tikiwiki
		Matcher imgFinder = imgPattern.matcher(input);

		String localsep = FILE_SEP;

		//look for {img src= and get the filepath from there
		while (imgFinder.find()) {
			String path = imgFinder.group(1);
			path = path.replaceAll("[\\/\\\\]", "\\" + localsep);
			Matcher notUploadFinder = notUploadPattern.matcher(path);
			if (notUploadFinder.find()) continue;

			if (!path.startsWith(localsep) && !root.endsWith(localsep)) {
				root += localsep;
			}
			if (root.matches("[A-Za-z]:" +"\\"+localsep)) {
				root += localsep;
			}

			if (!path.startsWith("http://"))
				path = root + path;

			log.debug("upload path = " + path);
			paths.add(path);
		}

		return paths;
	}

	String entity = "&((?:quot)|(?:amp)|(?:lt)|(?:gt));"; //find ", &, <, > html entities
	Pattern entityPattern = Pattern.compile(entity);

	/**
	 * @param input
	 * @return input string with decoded entities
	 * <br/>&amp;quot; becomes "
	 * <br/>&amp;amp; become &
	 * <br/>&amp;lt; become <
	 * <br/>&amp;gt; become >
	 */
	private String decodeEntities(String input) {
		Matcher entityFinder = entityPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (entityFinder.find()) {
			found = true;
			String thisEntity = entityFinder.group(1);
			String replacement = "";
			if ("quot".equals(thisEntity)) replacement = "\"";
			else if("amp".equals(thisEntity)) replacement = "&";
			else if("lt".equals(thisEntity)) replacement = "<";
			else if("gt".equals(thisEntity)) replacement = ">";
			entityFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			entityFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * gets all the image gallery paths for a given input and attachment directory
	 * @param input
	 * @param attachDir
	 * @return vector of paths to relevant image gallery files
	 */
	private Vector<String> getImageGalleryPaths(String input, String attachDir) {
		String baseDir = getImageGalleryDirectory(attachDir);
		Vector<String> systemPaths = getGalleryPaths(input, "tiki_images", "name", baseDir);
		return systemPaths;
	}

	/**
	 * gets all the file gallery paths for a given input and attachment directory
	 * @param input
	 * @param attachDir
	 * @return vector of paths to relevant file gallery files
	 */
	private Vector<String> getFileGalleryPaths(String input, String attachDir){
		String baseDir = getFileGalleryDirectory(attachDir);
		Vector<String> systemPaths = getGalleryPaths(input, "tiki_files", "fileName", baseDir);
		Vector<String> dbPaths = downloadDbFilesToTmp(input, "tiki_files", "fileName", getTmpDir());
		return combineVectors(systemPaths, dbPaths);
	}

	/**
	 * find the tikiwiki setting regarding where the image gallery files are
	 * saved on the file system
	 * @param attachDir the attachment directory setting that was passed in by the GUI.
	 * useful if the database only has a relative path. Unimportant if the database has
	 * an absolute path.
	 * @return path to the directoy on the file system that contains image gallery files
	 */
	protected String getImageGalleryDirectory(String attachDir) {
		return getDirectoryFromDbPrefs(IMAGE_GAL_PREF_SQL, attachDir);
	}

	/**
	 * find the tikiwiki setting regarding where the file gallery files are
	 * saved on the file system
	 * @param attachDir the attachment directory setting that was passed in by the GUI.
	 * useful if the database only has a relative path. Unimportant if the database has
	 * an absolute path.
	 * @return path to the directoy on the file system that contains file gallery files
	 */
	protected String getFileGalleryDirectory(String attachDir) {
		return getDirectoryFromDbPrefs(FILE_GAL_PREF_SQL, attachDir);
	}

	/**
	 * finds the directory for a gallery from the database, 
	 * @param sql SQL syntax that would find the directory from
	 * the tikiwiki preferences table
	 * @param attachDir attachment directory passed in by the GUI
	 * @return path to a directory where files are stored
	 */
	protected String getDirectoryFromDbPrefs(String sql, String attachDir) {
		connectToDB(this.properties);
		ResultSet results = this.sql(sql);
		String dir = null;
		try {
			while (results.next()) {
				dir = results.getString(PREF_COL);
			}
		} catch (SQLException e) {
			log.error("An error occurred while getting the image gallery directory.");
			e.printStackTrace();
		}
		dir = getAbsolutePath(dir, attachDir);
		return dir;
	}


	/**
	 * gets the absolute path for a given input, and with a given parent directory.
	 * @param input a directory that might be a relative path
	 * @param attachDir a parent dir, for use if the input is a relative path
	 * @return an absolute path to a directory
	 */
	protected String getAbsolutePath(String input, String attachDir) {
		if (input.startsWith("./")) {
			input = input.substring(1); //remove .
			return attachDir + input;
		}
		return input;
	}

	String imgParam = 
		"(" +								//start capturing (group1)
			"\\{" +							//open curly brace
			"img" +							//the string 'img'
			"\\s+" +						//at least one space until
			"src=" +						//the string 'src='
			"\"" +							//a double quote
			"(" +							//start capturing (group 2)
				"(?:show_image)" +			//non capturing group and string 'show_image'
				"|" +						//or 
				"(?:tiki-download_file)" +	//non captring group and string 'tiki-download_file.php'
			")" +							//end capturing (group 2)
			"\\.php\\?" +					//the string '.php?'
		")" +								//end capturing (group1)
		"(?:" +								//non capturing group
			"(?i)" +						//case insensitivity going forward
			"name=" +						//the string 'name='
		")" +								//close non-capturing group
		"(" +								//start capturing (group 3)
			"[^\"&}]+" +					//slurp greedily anything not a double quote, space, or &
		")"; 								//end capturing (group3)
	Pattern imgParamPattern = Pattern.compile(imgParam);

	/**
	 * gets the paths to gallery attachments (files or images), referenced by the given input
	 * @param input tikiwiki syntax that refers to attachments that would be found in a gallery
	 * @param table the table name containing the paths for the particular gallery
	 * @param column the column name for that table that would contain the file names
	 * @param baseFileDir parent directory where the files would live on the file system
	 * @return vector of absolute paths on the file system that the input refers to
	 */
	protected Vector<String> getGalleryPaths(
			String input, String table, String column, String baseFileDir){

		log.debug("Getting Gallery Paths");
		Vector<String> paths = new Vector<String>();
		String decoded = decodeEntities(input);
		Matcher imgNameFinder = imgParamPattern.matcher(decoded);
		connectToDB(this.properties);
		String pathCol = "path";
		while (imgNameFinder.find()) {
			String name = imgNameFinder.group(3);
			name = name.trim();
			String filenameSql = "select "+pathCol+" from "+table+" where "+column+" like \"" + name + "\";";
			ResultSet filenameData = sql(filenameSql);
			try {
				String path = null;
				while (filenameData.next()) {
					path = filenameData.getString(pathCol); 
					break; // I only want one filename. If we got multiple rows, too bad.
				}
				if (path == null) continue;
				if ("".equals(path)) continue;
				log.debug("baseFileDir = " + baseFileDir);
				log.debug("path = " + path);
				if (baseFileDir == null) baseFileDir = "";
				String fullpath = baseFileDir + path;
				log.debug("fullpath = " + fullpath);
				String newpath = copyFile(fullpath, this.output, name);
				if (newpath != null)
					paths.add(newpath);
			} catch (SQLException e) {
				log.error("Error while examining filename data.");
				e.printStackTrace();
			}
		}
//		closeDB(); //we're closing the connection at the end 
    	return paths;
	}

	/**
	 * determines which attachments are saved in the database,
	 * downloads them to a tmp directory,
	 * and adds the path to the file (via the tmp directory) to the
	 * return vector
	 * @param input syntax with references to attachable files
	 * @param table db table where images might be saved 
	 * @param column db column in the table where images might be saved
	 * @param tmpDir directory where images should be saved to
	 * @return Vector of paths to saved tmp images
	 */
	protected Vector<String> downloadDbFilesToTmp(
			String input, String table, String column, String tmpDir) {
		//XXX Somewhere in here we need to copy the globbed data files that might exist
		//to a tmp directory, and put that path in the Vector
		log.debug("Downloading DB Files");
		log.debug("table = " + table);
		log.debug("col = " + column);
		log.debug("tmpDir = " + tmpDir);
		
		Vector<String> paths = new Vector<String>();
		String decoded = decodeEntities(input);
		Matcher imgNameFinder = imgParamPattern.matcher(decoded);
		connectToDB(this.properties);
		String dataCol = "data";
		while (imgNameFinder.find()) {
			String name = imgNameFinder.group(3);
			String filenameSql = "select "+dataCol+" from "+table+" where "+column+" like \"" + name + "\";";
			ResultSet filenameData = sql(filenameSql);
			try {
				byte[] data = null;
				while (filenameData.next()) {
					data = filenameData.getBytes(dataCol); 
					break; // I only want one filename. If we got multiple rows, too bad.
				}
				if (data == null) continue;
				if (data.length == 0) continue;
				String newpath = tmpDir + FILE_SEP + name;
				boolean succeeded = FileUtils.writeFile(data, newpath);
				if (newpath != null && succeeded)
					paths.add(newpath);
				else 
					log.error("There was a problem writing to the file.");
			} catch (SQLException e) {
				log.error("Error while examining filename data.");
				e.printStackTrace();
			}
		}
//		closeDB();
    	return paths;
	}


	/**
	 * copies file at fromFilePath to directory toDir with the name toFilename
	 * @param fromFilePath
	 * @param toDir
	 * @param toFilename
	 * @return path to new file
	 */
	private String copyFile(String fromFilePath, String toDir, String toFilename) {
		
		//check that fromFilePath exists and is a file
		String root = this.getAttachmentDirectory(); //testing nullness so unit tests work
		if (!fromFilePath.startsWith(FILE_SEP) && !root.endsWith(FILE_SEP)) { //FIXME what about windows seperator (\\)?
			root += FILE_SEP;
		}
		File fromFile = new File(fromFilePath);
		if (!fromFile.exists() || fromFile.isDirectory()) {
			log.error("Attachment does not exist: " + fromFilePath);
			return null; //can't copy nonexistant file
		}

		//check that toDir exists and is a dir
		File toDirFile = new File(toDir);
		if (!toDirFile.exists()) {
			log.debug("Creating output directory: " + toDir);
			toDirFile.mkdir(); 	//try making the file
		}
		if (!toDirFile.exists() && !toDirFile.isDirectory()) {
			log.error("Cannot copy to this directory: " + toDir);
			return null; //otherwise give up
		}

		//create toFilePath
		if (!toDir.endsWith(FILE_SEP))
			toDir = toDir + FILE_SEP;
		String toFilePath = toDir + toFilename;
		File toFile = new File(toFilePath);
		if (toFile.exists()) {
//			 FIXME  more than one file by the same name, currently overwriting
			log.debug("File already exists: " + toFilePath);
		}

		//copy file
		boolean success = false;
		try {
			copy(fromFile, toFile);
			success = true;
		} catch (IOException e) {
			success = false;
			log.error("Could not copy: " + fromFilePath + " to " + toFilePath);
			e.printStackTrace();
		}
		log.debug("Copied file? " + success);
		return success?toFilePath:null; //return null if copy was unsuccessful
	}


	/**
	 * copies files.
	 * From the Java Developer's Almanac:
	 * http://www.exampledepot.com/egs/java.nio/File2File.html
	 * @param fromFile
	 * @param toFile
	 * @throws IOException
	 */
	private void copy(File fromFile, File toFile) throws IOException{
		FileChannel srcChannel = new FileInputStream(fromFile).getChannel();
        FileChannel dstChannel = new FileOutputStream(toFile).getChannel();

        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

        // Close the channels
        srcChannel.close();
        dstChannel.close();
	}

	/**
	 * @return true if tikiwiki setting indicates gallery conversions should be attempted
	 */
	protected boolean isGalleryConversion() {
		String isGC = this.properties.get(PROPERTIES_CONVERTGALLERY);
		return ("true".equals(isGC))?true:false;
	}

	/**
	 * gets a map with id->name relationships that the sql refers to
	 * @param sql sql that would return results that use colId and colName
	 * as the columns for objects that have an id->name relationship
	 * @param colId column used by the sql that refers to ids
	 * @param colName column used by the sql that refers to names
	 * @return map. keys = ids, values = names.
	 */
	protected HashMap<String,String> getIdsAndNames(String sql, String colId, String colName) {
		HashMap<String,String> map = new HashMap<String,String>();
		connectToDB(properties);
		ResultSet imageData = sql(sql);
		try {
			while (imageData.next()) {
				String id = imageData.getString(colId);
				String name = imageData.getString(colName);
				map.put(id, name);
			}
		} catch (SQLException e) {
			log.error("Problem while examining image data.");
			e.printStackTrace();
		}
//		closeDB(); //we're closing this at then end
		return map;
	}
	String imgIdParam = 
		"(" +								//start capturing (group1)
			"\\{" +							//open curly brace
			"img" +							//the string 'img'
			"\\s+?" +						//at least one space until
			"src=" +						//the string 'src='
			"\"" +							//a double quote
			"(" +							//start capturing (group 2)
				"(?:show_image)" +			//non capturing group and string 'show_image'
				"|" +						//or 
				"(?:tiki-download_file)" +	//non captring group and string 'tiki-download_file.php'
			")" +							//end capturing (group 2)
			"\\.php\\?" +					//the string '.php?'
		")" +								//end capturing (group1)
		"(?:" +								//non capturing group
			"(?:" +							//non capturing group
				"file" +					//the string 'file'
			")" +							//close non capturing group
			"?" +							//make previous group optional
			"(?i)" +						//case insensitivity going forward
			"id=" +							//the string 'id='
		")" +								//close non-capturing group
		"(" +								//start capturing (group 2)
			"[^\"& ]+" +					//slurp greedily anything not a double quote, space, or &
		")"; 								//end capturing (group2)
	Pattern imgIdPattern = Pattern.compile(imgIdParam);
	/**
	 * @param input tikiwiki syntax
	 * @param idsAndNames map of id=>filename key value pairs
	 * @param type replacing image gallery string or file gallery strings
	 * @return input with id=num in img syntax, replaced with name=filename
	 */
	protected String replaceImageIds(
			String input, HashMap<String, String> idsAndNames, GalleryType type) {
		Matcher imgIdFinder = imgIdPattern.matcher(input);
		log.debug("Replacing ids with names");
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imgIdFinder.find()) {
			found = true;
			String pre = imgIdFinder.group(1);

			// have to check which file map we're using
			// (can't combine because they might have overlapping keys)
			String typeString = imgIdFinder.group(2);
			if (type == GalleryType.IMAGE && !typeString.startsWith("show_image"))
				continue;
			else if (type == GalleryType.FILE && !typeString.startsWith("tiki-download_file"))
				continue;
				
			//figure out the name that goes with this id
			String id = imgIdFinder.group(3);
			log.debug("..id = " + id);
			String name = idsAndNames.get(id);
			log.debug("..name= " + name);

			//create the replacement string
			String replacement = pre + "name=" + name; 
			log.debug("..replacement = " + replacement);
			
			imgIdFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imgIdFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * combines elements from a and b into one vector. Common elements are combined as one in the new vector.
	 * See AttachmentConverterTest.testCombineVectors
	 * @param a
	 * @param b
	 * @return one Vector of all unique elements from a and b
	 */
	protected Vector<String> combineVectors(Vector<String> a, Vector<String> b) {
		Set<String> c = new TreeSet<String>();
		c.addAll(a);
		c.addAll(b);
		Vector<String> d = new Vector<String>();
		d.addAll(c);
		return d;
	}

	/**
	 * gets the database properties defined in a file at the given propLocation
	 * @param propLocation
	 * @return true if no problems were encountered. 
	 * false, if problems were encountered. If so, see log for error messages.
	 */
	protected boolean readDBProperties(String propLocation) {
		TreeMap<String, String> props = null;
		try {
			props = PropertyFileManager.loadPropertiesFile(propLocation);
		} catch (IOException e) {
			String message = "Could not load file at '" + propLocation + "'.\n" +
					"Gallery attachments cannot be converted.\n" +
					"Note: File permissions may be too restrictive.";
			this.addError(Feedback.BAD_SETTINGS_FILE, message, true);
			log.error(message);
			return false;
		}
		if (props == null) {
			String message = "Properties file at '"+propLocation+"' could not be found.\n" +
					"Gallery attachments cannot be converted.";
			this.addError(Feedback.BAD_SETTINGS_FILE, message, true);
			log.error(message);
			return false;
		}
		if (props.isEmpty()) {
			String message = "Properties file at '" + propLocation + "' contains no settings!\n" +
					"Please customize this file if \n" +
					"you wish gallery attachments to be converted.";
			this.addError(Feedback.BAD_SETTINGS_FILE, message, true);
			log.warn(message);
			return false;
		}
		log.debug("Loaded file: " + propLocation);
		this.properties=props;
		return true;
	}
	/* Database Methods
	 * From com.atlassian.uwc.exporters.MediawikiExporter
	 */
	/**
	 * connects to the database described by the given properties, unless the connection already exists.
	 * @param props Map of properties. See example file export.mediawiki.properties
	 */
	private void connectToDB(Map props) {
		//if we already have one, don't worry about it
		if (con != null) return;
		
		dbName = (String) props.get(PROPERTIES_DBNAME);
		dbUrl = (String) props.get(PROPERTIES_DBURL);
		login = (String) props.get(PROPERTIES_LOGIN);
		password = (String) props.get(PROPERTIES_PASSWORD);
		output = (String) props.get(PROPERTIES_OUTPUTDIR);
		jdbcDriver = (String) props.get(PROPERTIES_DRIVER);
		try {
			//load driver
			Class.forName(jdbcDriver);
			//connect to db
			String url = dbUrl + "/" + dbName;
			con = DriverManager.getConnection(url, login, password);
		} catch (ClassNotFoundException e) {
			String note = "Could not load JDBC driver: " + jdbcDriver;
			log.error(note);
			this.addError(Feedback.DB_DRIVER_FAILURE, note, true);
			e.printStackTrace();
		} catch (SQLException e) {
			String note = "Could not connect to database: " + dbName;
			log.error(note);
			this.addError(Feedback.DB_FAILURE, note, true);
			e.printStackTrace();
		}
	}
	/**
	 * closes the currently opened database, if a connection still exists.
	 */
	private void closeDB() {
		if (con != null) {
			try {
				con.close();
				con = null;
			} catch (SQLException e) {
				log.error("Error while closing JDBC connection");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs an sql SELECT query.
	 * @param sql String, sql SELECT query
	 * @return ResultSet of select query results
	 */
	private ResultSet sql(String sql) {
		return sql(sql, false);
	}

	/**
	 * runs an sql query.
	 * @param sql the sql string to be run
	 * @param isUpdate true if is an update, delete, or such type query. false, if just select
	 * @return ResultSet object with results from select query, or null. (Notice, return will
	 * always be null if isUpdate is true)
	 */
	private ResultSet sql(String sql, boolean isUpdate) {
		Statement sqlStatement = null;
		String message = "";
		ResultSet result = null;
		try {
			message = "Creating statement: "  + sql;
			sqlStatement = con.createStatement();
			log.debug(message);
			message = "Executing statement: " + sql;
			if (isUpdate) {
				sqlStatement.executeUpdate(sql);
			}
			else {
				result = sqlStatement.executeQuery(sql);
			}
			log.debug(message);
			SQLWarning warn = sqlStatement.getWarnings();
			while (warn != null) {
				log.warn(warn.getErrorCode() + "\n" +
						warn.getMessage() + "\n" +
						warn.getSQLState());
				warn = warn.getNextWarning();
			}
		} catch (SQLException e) {
			log.error("Error while: " + message);
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @return property location. If null, will return the default.
	 */
	public String getPropertyLocation() {
		if (this.propertyLocation == null)
			this.propertyLocation = DEFAULT_PROPERTIES_DIR + DEFAULT_PROPERTIES_LOCATION;
		return this.propertyLocation;
	}
	
	/**
	 * sets the property location
	 * @param location
	 */
	public void setPropertyLocation (String location) {
		this.propertyLocation = location;
	}
	
	/**
	 * the tmp directory is where attachments will be copied to if their
	 * original incarnation does not have the filename we want to upload the attachment
	 * with
	 * @return the tmp directory
	 */
	public String getTmpDir() {
		return this.properties.get(PROPERTIES_OUTPUTDIR);
	}
}
