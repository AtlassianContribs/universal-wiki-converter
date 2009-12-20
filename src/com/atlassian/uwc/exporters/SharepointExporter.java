package com.atlassian.uwc.exporters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.axis.message.MessageElement;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import sun.tools.tree.AddExpression;

import com.atlassian.confluence.extra.sharepoint.wrapper.SharePointWebServicesWrapper;
import com.atlassian.uwc.ui.ConverterEngine;
import com.microsoft.sharepoint.webservices.lists.GetList;
import com.microsoft.sharepoint.webservices.lists.GetListCollection;
import com.microsoft.sharepoint.webservices.lists.GetListCollectionResponse;
import com.microsoft.sharepoint.webservices.lists.GetListCollectionResponseGetListCollectionResult;
import com.microsoft.sharepoint.webservices.lists.GetListItems;
import com.microsoft.sharepoint.webservices.lists.GetListItemsQuery;
import com.microsoft.sharepoint.webservices.lists.GetListItemsQueryOptions;
import com.microsoft.sharepoint.webservices.lists.GetListItemsResponse;
import com.microsoft.sharepoint.webservices.lists.GetListItemsResponseGetListItemsResult;
import com.microsoft.sharepoint.webservices.lists.GetListItemsViewFields;
import com.microsoft.sharepoint.webservices.lists.GetListResponse;
import com.microsoft.sharepoint.webservices.lists.ListsSoap12Stub;

/**
 * exports pages from a Sharepoint Server to indivdual files.
 * Requires a properties file. See sample properties file exporter.sharepoint.properties
 */
public class SharepointExporter implements Exporter {
	private static final String WIKI_ITEMCOUNT_KEY = "ItemCount";
	//CONSTANTS
	//properties constants
	protected static final String DEFAULT_PROPERTIES_LOCATION = "exporter.sharepoint.properties";
	protected static final String EXPORTER_PROPERTIES_SERVER = "serverURL";
	protected static final String EXPORTER_PROPERTIES_WIKIS = "wikis";
	protected static final String EXPORTER_PROPERTIES_LOGIN = "login";
	protected static final String EXPORTER_PROPERTIES_PASSWORD = "password";
	protected static final String EXPORTER_PROPERTIES_OUTPUTDIR = "output";
	//output directory
	protected static final String EXPORT_DIR = "exported_sharepoint_pages";
	//sharepoint constants
	private static final String WIKI_TEMPLATE = "119";
	//log4j
	private Logger log = Logger.getLogger(this.getClass());

	//cancel object
	private boolean running = false;
	
	//Sharepoint Soap Service object
	private ListsSoap12Stub service;
	
	//permutations map (we have to permute SP dirs to valid unique spacekeys)
	private HashMap<String, String> permuted = new HashMap<String, String>();
	
	/**
	 * entry method if we use this class as an App.
	 * @param args
	 */
	public static void main(String[] args) {
		SharepointExporter exp = new SharepointExporter();
		String confdir = "conf" + File.separator;
		Map propsMap = exp.getProperties(confdir + DEFAULT_PROPERTIES_LOCATION);
		exp.export(propsMap);
	}
	
	/**
	 * @param filename path to properties file
	 * @return map of properties from properties file
	 */
	protected Map getProperties(String filename) {
		Properties props = new Properties();
		Map<String, String> propsMap = new HashMap<String, String>();
		try {
			props.load(new FileInputStream(filename));
			propsMap.put(EXPORTER_PROPERTIES_SERVER, props.getProperty(EXPORTER_PROPERTIES_SERVER));
			propsMap.put(EXPORTER_PROPERTIES_WIKIS, props.getProperty(EXPORTER_PROPERTIES_WIKIS));
			propsMap.put(EXPORTER_PROPERTIES_LOGIN, props.getProperty(EXPORTER_PROPERTIES_LOGIN));
			propsMap.put(EXPORTER_PROPERTIES_PASSWORD, props.getProperty(EXPORTER_PROPERTIES_PASSWORD));
			propsMap.put(EXPORTER_PROPERTIES_OUTPUTDIR, props.getProperty(EXPORTER_PROPERTIES_OUTPUTDIR));
		} catch (FileNotFoundException e) {
			log.error("Cannot find properties file");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Cannot load properties file");
			e.printStackTrace();
		}
		return propsMap;
	}

	public void export(Map properties) {
		startRunning();
		//set up log4j
		PropertyConfigurator.configure("log4j.properties");
		//setup database connection
		log.info("Exporting Sharepoint...");
		//do the export
		exportSharepoint(properties);
		//log status
		if (this.running) log.info("Export Complete.");

		this.running = false;
	}

	/**
	 * starts the running flag. Useful for unit testing.
	 */
	protected void startRunning() {
		this.running = true;
	}

	/**
	 * exports data from a sharepoint server using the settings in the
	 * given properties, and saves exported data to the file system
	 * @param properties  Must contain the following keys:
	 * serverURL (EXPORTER_PROPERTIES_SERVER)
	 * wikis (EXPORTER_PROPERTIES_WIKIS)
	 * login (EXPORTER_PROPERTIES_LOGIN)
	 * password (EXPORTER_PROPERTIES_PASSWORD)
	 * output (EXPORTER_PROPERTIES_OUTPUTDIR)
	 * <br/>
	 * The wikis setting is optional (if unset, will get all wikis), but otherwise
	 * all settings are required. See conf/exporter.sharepoint.properties for more details on
	 * what must be contained in these settings
	 * 
	 */
	protected void exportSharepoint(Map properties) { 
		if (!this.running) return;
		//validate properties
		validateProperties(properties);
		
		//prepare output directory
		cleanOutputDir(properties);
		HashMap pages = null;
		
		//get the pages. wikis are keys. vector of pages is value
		pages = getSharepointPages(properties);

		//create wiki directories
		Vector<String> dir = getDirectories(pages);
		createDirectoriesLocally(dir, properties); 
		
		//create files and send to output 
		createFilesLocally(pages, properties);
	}
	
	/**
	 * checks that all necessary props are set
	 * @param properties
	 * @throws IllegalArgumentException if properties are insufficiently set
	 */
	protected void validateProperties(Map properties) {
		String server = (String) properties.get(EXPORTER_PROPERTIES_SERVER);
		String outputdir = (String) properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		String basemessage = "setting was empty. " +
				"Please configure sharepoint exporter settings at: conf" + 
				File.separator + DEFAULT_PROPERTIES_LOCATION;
		if ("".equals(server)) {
			String message = "Server " + basemessage;
			throw new IllegalArgumentException(message);
		}
		if ("".equals(outputdir)) {
			String message = "Output dir " + basemessage;
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * deletes and recreates the output directory
	 * @param properties 
	 */
	protected void cleanOutputDir(Map properties) {
		if (!this.running) return;

		log.debug("Cleaning Output Dir");
		String output = (String) properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		if (!output.endsWith(File.separator)) output += File.separator;
		output = output + EXPORT_DIR;
		File file = new File(output);
		
		if (!file.exists()) {
			log.info("Creating output directory: " + output);
			while (!file.exists()) {
				file.mkdir();
				if (!createParents(output)) 
					throw new IllegalArgumentException("Bad Output Directory setting.");
			}
		}
		else { 
			deleteDir(file);
			file.mkdir();
			log.info("Cleaning and creating output directory:" + output);
		}
	}

	/**
	 * creates any missing directories in the given input.
	 * Given the example input, if something_else and tada don't exist, it will 
	 * create both something_else and tada. 
	 * @param input a directory path, "something/something_else/tada"
	 * @return false, if errors occurred that required exiting early.
	 */
	protected boolean createParents(String input) {
		log.debug("Creating Parent Directories");
		String[] pathParts = input.split("[\\/\\\\]");
		String accumulation = "";
		boolean abs = Pattern.matches("^[\\/\\\\].*", input);
		for (int i = 0; i < pathParts.length; i++) {
			String directory = pathParts[i];
			//not directories
			if ("".equals(directory)) continue;

			//create a file for the current parent dir
			if (i == 0 && !abs) accumulation += directory;
			else accumulation += File.separator + directory;
			File parent = new File(accumulation);
			if (parent.exists()) continue;
			
			log.debug("Creating " + accumulation);
			boolean succeeded = parent.mkdir();
			
			//check to see if there were problems
			if (!succeeded) { 
				log.error("Couldn't create directory: " + accumulation);
				log.error("Problem creating parents. Exiting.");
				return false;
			}
		}
		return true;
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
			log.debug("deleting " + name);
			return;
		}
		else { // or delete the directory
			File[] files = file.listFiles();
			for (File f : files) {
				deleteDir(f);
			}
			file.delete();
			log.debug("deleting dir: " + name);
		}
		
	}
	
	/**
	 * @param properties properties providing connection details
	 * @return Vector of all sharepoint pages represented by the
	 * settings in conf/exporter.sharepoint.properties
	 */
	protected HashMap getSharepointPages(Map properties) {
		HashMap<String, Vector<SharepointPage>> allpages = new HashMap<String, Vector<SharepointPage>>(); 
		//get all the wikis
		Vector<String> wikis = getWikis(properties);
		//go through each wiki and get every page
		for (String wiki : wikis) { 
			Vector<SharepointPage> pages = getPages(wiki, properties);
			allpages.put(wiki, pages);
		}
		return allpages;
	}

	/**
	 * @param properties properties providing connection details
	 * @return list of paths to wiki names. (Include subsite paths as necessary.) 
	 */
	protected Vector<String> getWikis(Map properties) {
		//pre-authentication (some users will report mysteriously unable to authenticate without this)
		System.clearProperty("httpclient.authentication.preemptive");
        System.getProperties().setProperty("httpclient.authentication.preemptive", "true");
		
		//get service
		ListsSoap12Stub service = getListsSoapService(properties);
		GetListCollection collection = new GetListCollection();
		GetListCollectionResponse response = null;
		try {
			response = service.getListCollection(collection);
		} catch (RemoteException e) {
			//FIXME communicate to the UWC Feedback Handler?
			log.error("Problem connecting with Sharepoint Soap Service.");
			String error = SharePointWebServicesWrapper.testSharePointConnection(service);
			if (!error.equals(SharePointWebServicesWrapper.TEST_RESULT_SUCCESS))
				log.error("Error message: " + error);
			if (error.equals(SharePointWebServicesWrapper.TEST_RESULT_WRONG_USERNAME_OR_PASSWORD))
				log.warn("If you experience this error repeatedly, check to see if your Sharepoint is configured to accept BasicAuthentication.");
			e.printStackTrace();
			return null;
		}
		GetListCollectionResponseGetListCollectionResult result = response.getGetListCollectionResult();
		MessageElement[] xml = result.get_any();
		Vector<String> wikis = getWikiNames(xml);
		wikis = filterWithProps(wikis, properties);
		return wikis;
	}

	/**
	 * filters the given vector of wikinames with the settings from the
	 * given properties
	 * @param wikis
	 * @param properties
	 * @return filtered vector
	 */
	protected Vector<String> filterWithProps(Vector<String> wikis, Map properties) {
		Vector<String> filtered = new Vector<String>();
		String filterRaw = (String) properties.get(EXPORTER_PROPERTIES_WIKIS);
		if (filterRaw == null || "".equals(filterRaw))
			return wikis;
		String[] filter = filterRaw.split(",");
		for (int i = 0; i < filter.length; i++) {
			String allow = filter[i].trim();
			if (wikis.contains(allow))
				filtered.add(allow);
		}
		return filtered;
	}

	/**
	 * logs into and gets the Lists Soap Service associated with the server
	 * and authentication details found in the given properties 
	 * @param properties
	 * @return Lists Soap Service. 
	 * See http://msdn2.microsoft.com/en-us/library/aa152622.aspx
	 */
	protected ListsSoap12Stub getListsSoapService(Map properties) {
		String serverURL = (String) properties.get(EXPORTER_PROPERTIES_SERVER);
		String login = (String) properties.get(EXPORTER_PROPERTIES_LOGIN);
		String password = (String) properties.get(EXPORTER_PROPERTIES_PASSWORD);
		if (this.service == null)
			this.service = SharePointWebServicesWrapper.getSharePointListsSOAPBinding(serverURL, login, password);
		return this.service;
	}

	/**
	 * @param xml array of xml MessageElements derived from calls to the Soap service
	 * @return list of wikinames representing the wikis found in the given xml 
	 */
	protected Vector<String> getWikiNames(MessageElement[] xml) {
		Vector<String> wikis = new Vector<String>();
		for (MessageElement element : xml) {
			Iterator childElements = element.getChildElements();
			for (Iterator iter = childElements; iter.hasNext();) {
				MessageElement child = (MessageElement) iter.next();
				String title = child.getAttribute("Title");
				String template = child.getAttribute("ServerTemplate");
				//this enum does not appear to be available, but ideally we would use
				//SPListTemplateType see: http://msdn2.microsoft.com/en-us/library/microsoft.sharepoint.splisttemplatetype.aspx
				if (template.equals(WIKI_TEMPLATE))
					wikis.add(title);
			}
		}
		return wikis; 
	}
	
	/**
	 * gets the pages from the given wiki, using the connection settings from
	 * the given properties
	 * @param wiki listname for the desired wiki
	 * @param properties
	 * @return vector of SharepointPage objects representing the pages
	 * from the given wiki
	 * @throws IllegalArgumentException if could not connect to service for given wiki 
	 */
	protected Vector<SharepointPage> getPages(String wiki, Map properties) {
		ListsSoap12Stub service = getListsSoapService(properties);
		GetListItems parameters = getItemParameters(wiki, properties);
		GetListItemsResponse response = null;
		try {
			response = service.getListItems(parameters );
		} catch (RemoteException e) {
			String message = "Could not get list items for wiki: " + wiki;
			log.error(message);
			e.printStackTrace();
			throw new IllegalArgumentException(message);
		}
		GetListItemsResponseGetListItemsResult getListItemsResult = response.getGetListItemsResult();
		MessageElement[] xml = getListItemsResult.get_any();
		Vector<SharepointPage> pages = getPagesFromXml(xml);
		pages = filterBadChars(pages);
		return pages;
	}

	/**
	 * creates item parameters for use with getListItems method
	 * @param wiki name of wiki to be queried
	 * @param properties connection settings are contained in this map
	 * @return 
	 */
	protected GetListItems getItemParameters(String wiki, Map properties) {
		String viewName = null; //default view
		GetListItemsQuery query = null; 
		GetListItemsViewFields viewFields = null; //filters returned fields. XXX can we leverage this?
		String rowLimit = getItemCount(wiki, properties);
		GetListItemsQueryOptions queryOptions = getEmptyQueryOptions();
		String webId = null; 
		GetListItems params = new GetListItems(wiki, viewName , query , viewFields , rowLimit, queryOptions, webId);
		return params;
	}

	/**
	 * gets the number of items from the given wiki
	 * @param wiki
	 * @param properties
	 * @return
	 * @throws IllegalArgumentException if could not connect to service for given wiki
	 */
	protected String getItemCount(String wiki, Map properties) {
		ListsSoap12Stub service = getListsSoapService(properties);
		GetList parameters = new GetList(wiki);
		GetListResponse list = null;
		try {
			list = service.getList(parameters);
		} catch (RemoteException e) {
			String message = "Problem connecting to service with wikiname: " + wiki;
			log.error(message);
			e.printStackTrace();
			throw new IllegalArgumentException(message);
		} 
		//get the attribute ItemCount from the first xml element in the list
		return list.getGetListResult().get_any()[0].getAttribute(WIKI_ITEMCOUNT_KEY);
	}

	/**
	 * creates a Vector of SharepointPages based on the given xml
	 * @param xml
	 * @return 
	 */
	protected Vector<SharepointPage> getPagesFromXml(MessageElement[] xml) {
		Vector<SharepointPage> pages = new Vector<SharepointPage>();
		for (int i = 0; i < xml.length; i++) {
			MessageElement listitems = xml[i]; 
			Iterator childElements = listitems.getChildElements();
			for (Iterator iter = childElements; iter.hasNext();) { 
				MessageElement data = (MessageElement) iter.next(); //data
				Iterator rows = data.getChildElements();
				for (Iterator iterator = rows; iterator.hasNext();) {
					MessageElement page = (MessageElement) iterator.next();
					String title = page.getAttribute("ows_LinkFilename");
					String rawcontents = page.getAttribute("ows_MetaInfo");
					title = title.replaceAll("\\.aspx", "");
					SharepointPage sppage = new SharepointPage(title, rawcontents);
					pages.add(sppage);
				}
			}
		}
		return pages;
	}
	
	/**
	 * @return valid query options object with no data
	 */
	protected GetListItemsQueryOptions getEmptyQueryOptions() {
		MessageElement[] elements = new MessageElement[1];
		MessageElement element = new MessageElement();
		element.setName("QueryOptions");
		elements[0] = element;
		return new GetListItemsQueryOptions(elements );
	}

	/**
	 * @param pages key = strings representing wikinames values = vector of pages for that wiki
	 * @return vector of directories, one representing each wiki
	 */
	protected Vector<String> getDirectories(HashMap<String, Vector<SharepointPage>> pages) {
		Set<String> wikinamesSet = pages.keySet();
		Vector<String> wikinames = new Vector<String>();
		wikinames.addAll(wikinamesSet);
		Vector<String> dirs = new Vector<String>();
		for (int i = 0; i < wikinames.size(); i++) {
			String wikiname = (String) wikinames.get(i);
			String newWikiname = ConverterEngine.validateSpacekey(wikiname);
			if (!wikiname.equals(newWikiname)) {
				while (wikinames.contains(newWikiname)
						|| dirs.contains(newWikiname)) {
					newWikiname = permuteWikiname(newWikiname);
				}
				log.info("Sharepoint Directory name '" + wikiname +"' does not conform to " +
						"Confluence spacekey naming or uniqueness constraints. " +
						"It will be changed to '" + newWikiname + "'");
			}
			permuted.put(wikiname, newWikiname);
			dirs.add(newWikiname);
		}
		savePermutationsToTmpFile(permuted);
		return dirs;
	}


	Pattern finalDigit = Pattern.compile("(\\d+)$");
	/**
	 * permutes input by adding a digit to the end or incrementing
	 * the digit at the end
	 * <br/>
	 * Example: in = abc, out = abc2
	 * <br/>
	 * in = abc2, out = abc3
	 * @param input
	 * @return permuted input
	 */
	protected String permuteWikiname(String input) {
		Matcher digitFinder = finalDigit.matcher(input);
		if (digitFinder.find()) {
			String digits = digitFinder.group(1);
			int num = Integer.parseInt(digits);
			if (num < 1) num = 1; //first permutation should be #2
			num++;
			return digitFinder.replaceAll(num+"");
		}
		else {
			return input + "2";
		}
	}
	
	/**
	 * saves map of sharepoint wiki lib to confluence space permutations
	 * to a tmp file at conf/tmp.permutations.sharepoint.properties
	 * @param permutations
	 */
	private void savePermutationsToTmpFile(HashMap<String, String> permutations) {
		String text = "";
		Set keys = permutations.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String value = permutations.get(key);
			text += "Key:" + key + "\n" +
					"Value:" + value + "\n";  
		}
		String path = "conf" + File.separator + "tmp.permutations.sharepoint.properties"; 
		writeFile(path, text);
	}




	/**
	 * creates the wiki directories in the output directory 
	 * @param dir list of wikinames to be created as directories
	 * @param properties containing connection settings and output directory info
	 */
	protected void createDirectoriesLocally(Vector<String> dir, Map properties) {
		if (!this.running) return;
		String outdir = getParentDir(properties);
		File out = new File(outdir);
		//create if it doesn't exist
		if (!out.exists()) out.mkdir();
		//not successful? We have a problem.
		if (!out.exists()) 
			throw new IllegalArgumentException("Could not create output directory: " + outdir);
		for (String wiki : dir) {
			if (!this.running) return;
			wiki = filterBadChars(wiki);
			String newpath = outdir + File.separator + wiki;
			File newdir = new File(newpath);
			boolean result = newdir.mkdir();
			if (!result) {
				log.error("Could not create directory. " + newpath);
			}
		}
	}

	
	/**
	 * removes any illegal characters from the input.
	 * Note: Longterm, we're trying to transform Sharepoint wiki names into 
	 * Confluence <b>spacekeys</b>. Confluence spacekeys only allow letters and numbers.
	 * So, any punctuation is getting stripped out here for simplicity.
	 * We'll keep space chars for now, as we can preserve those in the space name,
	 * and they don't cause difficulties on the file system.
	 * @param input candidate wikiname as directory
	 * @return cleaned wikiname. Anything not [A-Za-z0-9 ] will be stripped out.
	 */
	protected String filterBadChars(String input) {
		String filtered = input.replaceAll("[^A-Za-z0-9 ]", "");
		if (!filtered.equals(input))
			log.warn("wiki name '" + input + "' had illegal chars. Transforming to: " + filtered);
		return filtered;
	}
	
	/**
	 * removes illegal characters from the page title.
	 * Note: Longterm, we're trying to transform Sharepoint pages into
	 * Confluence <b>pages</b>. Confluence pagenames do not allow some characters that
	 * Sharepoint pages allow. Disallowed characters will be stripped form the pagename.
	 * @param input list of candidate pages.
	 * @return same pages, but illegal chars in titles are stripped. Confluence does not allow:
	 * <br/>these characters
	 * <br/>:, @, /, \, |, ^, #, ;, [, ], {, }, <, >
	 * <br/>or pages that start with 
	 * <br/>$, .., ~
	 */
	protected Vector<SharepointPage> filterBadChars(Vector<SharepointPage> input) {
		for (SharepointPage page : input) {
			String title = page.getTitle();
			String copy = title;
			title = removeBadTitleChars(title);
			if (!copy.equals(title)) {
				log.warn("Cleaned bad characters from page title. '" + copy + "' became '" + title + "'");
			}
			page.setTitle(title);
		}
		return input;
	}

	/**
	 * replaces illegal title characters for Confluence pages
	 * @param input
	 * @return
	 */
	public static String removeBadTitleChars(String input) {
		input = input.replaceAll("[:@\\/\\\\|^#;\\[\\]{}<>]", "");
		input = input.replaceFirst("^[$~]|(\\.\\.)", "");
		return input;
	}

	/**
	 * creates files in the designated output directory from the given properties
	 * @param pages Hashmap. keys = Strings representing Sharepoint wikis. 
	 * values = SharepointPage objects representing the pages in the wiki key.
	 * @param properties connection and output directory settings 
	 */
	protected void createFilesLocally(HashMap<String, Vector<SharepointPage>> pages, Map properties) {
		for (String wiki : pages.keySet()) {
			if (!this.running) return;
			Vector<SharepointPage> wikipages = pages.get(wiki);
			for (Iterator iter = wikipages.iterator(); iter.hasNext();) {
				if (!this.running) return;
				SharepointPage page = (SharepointPage) iter.next();
				String filename = page.getTitle();
				String filecontents = page.getContents();
				String parent = getParentDir(properties);
				String wikidir = permuted.get(wiki);//determine wiki dir from permuted map (defined in getDirectories)
				parent += File.separator + wikidir; //we keep each page in a wiki directory 
				createFileLocally(filename, parent, filecontents);
			}
		}
	}

	/**
	 * creates the export directory based on the settings
	 * @param properties
	 * @return
	 */
	protected String getParentDir(Map properties) {
		String parent = (String) properties.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		if (!parent.endsWith(File.separator)) parent += File.separator;
		parent += EXPORT_DIR;
		return parent;
	}
	
	/**
	 * Creates the file at the given parentDir/filename, and writes the
	 * given filecontents to that file
	 * @param filename string, filename to be created. ex: Abc.txt
	 * @param parentDir string, parentdir filename will exist in. ex: outputdir/namespace/
	 * @param filecontents, string, text to be written to the newly created file
	 */
	protected void createFileLocally(String filename, String parentDir, String filecontents) {
		File parent = new File(parentDir);
		String fullpath = parent + File.separator + filename;
		File file = new File(fullpath);
		String message = "";
		try {
			message = "Checking for parent directory";
			if (!parent.exists())
				parent.mkdir();
			log.debug(message);
			message = "Creating new file: " + fullpath;
			file.createNewFile();
			log.debug(message);
			message = "Sending text to new file: " + fullpath;
			writeFile(fullpath, filecontents);
			log.debug(message);
		} catch (IOException e) {
			log.error("Problem while " + message);
			e.printStackTrace();
		}
	}

	/**
	 * writes the given text to the file at the given path
	 * @param path string, filepath where text will be written
	 * @param text string, text to write to filepath
	 */
	protected void writeFile(String path, String text) {
	    try {
	    	FileOutputStream fw = new FileOutputStream(path);
	    	OutputStreamWriter outstream = new OutputStreamWriter(fw, "utf-8");
	        BufferedWriter out = new BufferedWriter(outstream);
	        out.write(text);
	        out.close();
	    } catch (IOException e) {
	    	log.error("Problem writing to file: " + path);
	    	e.printStackTrace();
	    }
	}

	public void cancel() {
		String message = "Sharepoint Exporter - Sending Cancel Signal";
    	log.debug(message);
    	this.running = false;
	}
	
	/* Inner Classes */
	
	/**
	 * class which encapsulates page data for a Sharepoint page
	 */
	public class SharepointPage {
		private String title;
		private String contents;
		public SharepointPage(String title, String contents) {
			this.title = title;
			this.contents = contents;
		}
		public String getContents() {
			return contents;
		}
		public void setContents(String contents) {
			this.contents = contents;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String toString() {
			return title;
		}
	}
}