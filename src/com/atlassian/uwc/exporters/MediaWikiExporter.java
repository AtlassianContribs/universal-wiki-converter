package com.atlassian.uwc.exporters;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;

/**
 * exports pages from a Mediawiki database to text files.
 * Requires a properties file. See sample properties file exporter.mediawiki.properties
 * @author Laura Kolker
 *
 */
public class MediaWikiExporter extends SQLExporter {
	private static final String DEFAULT_ENCODING = "utf-8";
	//CONSTANTS
	//properties constants
	private static final String DEFAULT_PROPERTIES_LOCATION = "exporter.mediawiki.properties";
	protected static final String EXPORTER_PROPERTIES_OUTPUTDIR = "output";
	private static final String EXPORTER_PROPERTIES_PASSWORD = "password";
	private static final String EXPORTER_PROPRETIES_LOGIN = "login";
	private static final String EXPORTER_PROPERTIES_DRIVER = "jdbc.driver.class";
	private static final String EXPORTER_PROPERTIES_DBURL = "dbUrl";
	private static final String EXPORTER_PROPERTIES_DBNAME = "databaseName";
	private static final String EXPORTER_PROPERTIES_DBPREFIX = "dbPrefix";
	private static final String EXPORTER_PROPERTIES_ENCODING = "encoding";
	private static final String EXPORTER_PROPERTIES_URLENCODING = "urlencoding";
	private static final String EXPORTER_PROPERTIES_HISTORY = "history";
	private static final String EXPORTER_PROPERTIES_HISTORYSUFFIX = "history-suffix";
	private static final String EXPORTER_PROPERTIES_UDMF = "udmf";
	private static final String EXPORTER_PROPERTIES_ORIGTITLE = "origtitle";
	//mediawiki database constants (accurate for Mediawiki 1.7.1. Are these different for other mediawikis?)
	private static final String PAGE_TABLE = "page";
	private static final String REV_TABLE = "revision";
	private static final String TEXT_TABLE = "text";
	private static final String COL_ID = "page_id";
	private static final String COL_LATEST = "page_latest";
	private static final String COL_TITLE = "page_title";
	private static final String COL_NAMESPACE = "page_namespace";
	private static final String COL_REV = "rev_id";
	private static final String COL_REV_TEXT = "rev_text_id";
	private static final String COL_REV_PAGE = "rev_page";
	private static final String COL_REV_USER = "rev_user_text";
	private static final String COL_REV_DATE = "rev_timestamp";
	private static final String COL_TEXT_ID = "old_id";
	private static final String COL_TEXT = "old_text";
	private static final String NAMESPACE_INTERNAL = "12";
	private static final String NAMESPACE_SPECIAL = "8";
	//output directory
	private static final String EXPORT_DIR = "exported_mediawiki_pages";
	
	private String dbName;
	private String dbUrl;
	private String login;
	private String password;
	private String output;
	private String jdbcDriver;
	private String prefix;
	private String encoding;
	private String history;
	private String historySuffix;
	private String urlencoding;
	private String udmf;
	private String origtitle;
	//optional sql properties
	private String optPageSql;
	private String optTextIdSql;
	private String optTextSql;
	private String optRevSql;
	private String optUdmfSql;
	private String optTitleCol;
	private String optTextCol;
	private String optPageIdCol;
	private String optNamespaceCol;
	private String optTextIdCol;
	//namespace properties
	private String nsPropIds;
	private String nsPropExportAllCustom;
	private String nsPropCustomMap;
	private HashMap<Integer, String> nsCustomMap;
	
	//Descriptive names for mediawiki namespaces that use numbers in the database
	String[] namespaces = {"Pages", "Discussions", "Users", "UserDiscussions"};
	
	
	/**
	 * entry method if we use this class as an App.
	 * properties must be in exporter.mediawiki.properties
	 * @param args
	 */
	public static void main(String[] args) {
		MediaWikiExporter exp = new MediaWikiExporter();
		Map propsMap = exp.getDbProperties(DEFAULT_PROPERTIES_LOCATION);
		try {
			exp.export(propsMap);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * default properties grabber. used by main when this class is used as an app.
	 * @param filename path to properties file
	 * @return map of properties from properties file
	 */
	private Map getDbProperties(String filename) {
		Properties props = new Properties();
		Map propsMap = new HashMap();
		try {
			props.load(new FileInputStream(filename));
			propsMap.put(EXPORTER_PROPERTIES_DBNAME, props.getProperty(EXPORTER_PROPERTIES_DBNAME));
			propsMap.put(EXPORTER_PROPERTIES_DBURL, props.getProperty(EXPORTER_PROPERTIES_DBURL));
			propsMap.put(EXPORTER_PROPERTIES_DRIVER, props.getProperty(EXPORTER_PROPERTIES_DRIVER));
			propsMap.put(EXPORTER_PROPRETIES_LOGIN, props.getProperty(EXPORTER_PROPRETIES_LOGIN));
			propsMap.put(EXPORTER_PROPERTIES_PASSWORD, props.getProperty(EXPORTER_PROPERTIES_PASSWORD));
			propsMap.put(EXPORTER_PROPERTIES_OUTPUTDIR, props.getProperty(EXPORTER_PROPERTIES_OUTPUTDIR));
			propsMap.put(EXPORTER_PROPERTIES_DBPREFIX, props.getProperty(EXPORTER_PROPERTIES_DBPREFIX));
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
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void export(Map properties) throws ClassNotFoundException, SQLException {
		this.running = true;
		//set up log4j
		PropertyConfigurator.configure("log4j.properties");
		//setup database connection
		log.info("Exporting Mediawiki...");
		connectToDB(properties);
		//do the export
		exportMediawiki();
		//close the connection
		closeDB();
		//log status
		if (this.running) log.info("Export Complete.");

		this.running = false;
	}


	/**
	 * connects to the database described by the given properties
	 * @param props Map of properties. See example file export.mediawiki.properties
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	private void connectToDB(Map props) throws ClassNotFoundException, SQLException {
		
		dbName = (String) props.get(EXPORTER_PROPERTIES_DBNAME);
		dbUrl = (String) props.get(EXPORTER_PROPERTIES_DBURL);
		login = (String) props.get(EXPORTER_PROPRETIES_LOGIN);
		password = (String) props.get(EXPORTER_PROPERTIES_PASSWORD);
		output = (String) props.get(EXPORTER_PROPERTIES_OUTPUTDIR);
		jdbcDriver = (String) props.get(EXPORTER_PROPERTIES_DRIVER);
		fillOptionalProperties(props);
		connectToDB(jdbcDriver, dbUrl, dbName, login, password);
	}
	
	private void fillOptionalProperties(Map props) {
		prefix = (String) props.get(EXPORTER_PROPERTIES_DBPREFIX);
		if (prefix == null) prefix = "";
		encoding = (String) props.get(EXPORTER_PROPERTIES_ENCODING);
		if (encoding == null) encoding = DEFAULT_ENCODING;
		urlencoding = (String) props.get(EXPORTER_PROPERTIES_URLENCODING);
		if (urlencoding == null) urlencoding = "false";
		history = (String) props.get(EXPORTER_PROPERTIES_HISTORY);
		if (history == null) history = "false";
		historySuffix = (String) props.get(EXPORTER_PROPERTIES_HISTORYSUFFIX);
		if (historySuffix == null) historySuffix = "";
		if (udmf == null) udmf = "";
		udmf = (String) props.get(EXPORTER_PROPERTIES_UDMF);
		if (origtitle == null) origtitle = "";
		origtitle = (String) props.get(EXPORTER_PROPERTIES_ORIGTITLE);
		
		//leave opt sql props null, if unfilled
		optPageSql = (String) props.get("db.sql.pagedata");
		optTextIdSql = (String) props.get("db.sql.textiddata");
		optTextSql = (String) props.get("db.sql.textdata");
		optRevSql = (String) props.get("db.sql.revdata");
		optUdmfSql = (String) props.get("db.sql.udmfdata");
		optTitleCol = (String) props.get("db.column.title");
		optNamespaceCol = (String) props.get("db.column.namespace");
		optPageIdCol = (String) props.get("db.column.pageid");
		optTextIdCol = (String) props.get("db.column.textid");
		optTextCol = (String) props.get("db.column.text");
		
		//namespace properties
		nsPropIds = (String) props.get("namespaces.ids");
		nsPropExportAllCustom = (String) props.get("namespaces.exportallcustom");
		nsPropCustomMap = (String) props.get("namespaces.customnamespace.mapping");

	}

	/**
	 * exports the mediawiki associated with the open database connection 'con'
	 * @throws SQLException if an error occurs while executing an sql command
	 */
	private void exportMediawiki() throws SQLException {
		if (!this.running) return;
		//prepare output directory
		cleanOutputDir();
		Vector pages = null;
		//get syntax data from db
		if (existsSqlProperties()) {
			getMediaWikiPages(
					optPageSql, optTextSql, optTitleCol, optTextCol, 
					optNamespaceCol, optPageIdCol, optTextIdCol);
		} 
		else {
			getMediaWikiPages();
		}
	}

	/**
	 * deletes and recreates the output directory
	 */
	protected void cleanOutputDir() {
		if (!this.running) return;
		output = output + EXPORT_DIR;
		File file = new File(output);
		if (!file.exists()) {
			log.info("Creating output directory: " + output);
			file.mkdir();
		}
		else {
			deleteDir(file);
			file.mkdir();
			log.info("Cleaning and creating output directory:" + output);
		}
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
	 * @return true if enough optional sql properties have been set
	 */
	private boolean existsSqlProperties() {
		return (optPageSql != null 
				&& optTextSql != null
				&& optTextIdSql != null
				&& optTitleCol != null
				&& optTextCol != null
				&& optPageIdCol != null
				&& optNamespaceCol != null
				&& optTextIdCol != null);
	}
	
	/**
	 * get all the interesting mediawiki pages (not the Special ones)
	 * from the database
	 * @return Vector of MediaWikiPage objects containing titles and text, etc.
	 * @throws SQLException if an error occurs when executing the sql command
	 */
	private void getMediaWikiPages() throws SQLException {
		ResultSet pagedata = null;
		try {
			//get pages 
			String pageSql = "select " + 
			COL_ID + ", " + 
			COL_NAMESPACE +", " +
			COL_TITLE + ", " +
			COL_LATEST + " " + 
			"from " + prefix + PAGE_TABLE + " " +
			getNamespaceWhereClause() + ";"; 					 
			pagedata = sql(pageSql);

			try {
				while (pagedata.next()) {
					if (!this.running) return;
					// page data
					String id = pagedata.getString(COL_ID); 
					String latest = pagedata.getString(COL_LATEST);
					String namespace = pagedata.getString(COL_NAMESPACE); 
					byte[] bytes2 = pagedata.getBytes(COL_TITLE); //get bytes, 'cause we might have unicode issues
					String title = null;
					try {
						title = getTitle(bytes2);
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					//get all the revision ids we need
					Vector<String> allRevs = new Vector<String>();
					if (gettingHistory()) {
						allRevs = getAllRevIds(id); //handle histories
					}
					else {
						allRevs.add(latest); //just the latest revision id
					}

					//user timestamp data
					HashMap<String,String[]>revUdmfMap = null;
					String udmfSql = "select " + COL_REV_USER + "," + COL_REV_DATE + "," + COL_REV + 
					" from " + prefix + REV_TABLE +
					" where " + COL_REV_PAGE + "='" + id + "';";
					if (gettingUserdate()) { 
						revUdmfMap = getUserDateMap(udmfSql); //rev_id -> [username,timestamp]
					}

					int numRevs = 1;
					for (String rev : allRevs) {
						//get the text id
						String textIdSql = "select " + COL_REV_TEXT + " from " + prefix + REV_TABLE + 
						" where " + COL_REV + "='" + rev + "';"; 
						ResultSet revdata = sql(textIdSql);
						String textid = "";
						while (revdata.next()) {
							textid = revdata.getString(COL_REV_TEXT); 
						}
						//get the text
						String textSql = "select " + COL_TEXT + " from " + prefix + TEXT_TABLE + 
						" where " + COL_TEXT_ID +  "='" + textid + "';";
						ResultSet textdata = sql(textSql);
						String text = "";
						while (textdata.next() ) {
							if (!this.running) return;
							byte[] bytes = textdata.getBytes(COL_TEXT);
							try {
								text = new String(bytes, encoding);
							} catch (UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (gettingUserdate()) { //date for udmf framework: usernames and timestamps 
							if (!this.running) return;
							String userdate = getUserDateData(rev, revUdmfMap);
							text = userdate + text;
						}

						//save the data into a local object
						MediaWikiPage mwpage = new MediaWikiPage(title, text, namespace, id, (numRevs++)+""); 
						//next: 1) handle URL decoding when converting, 2) handle other getMEdiawikiPages method, 3)refactor
						//next: refactor you can use the jdb URL to set the UTF-8 encoding?


						//output the file to the system
						createFileLocally(mwpage);
						revdata.close();
						textdata.close();
					}
				}
			} catch (SQLException e) {
				log.error("Problem while examining data.");
				e.printStackTrace();
			} 
		} finally {
			pagedata.close();
		}
	}

	protected String getNamespaceWhereClause() {
		String whereAllCustom = getExportAllCustomNamespaceProperty()?
				COL_NAMESPACE + ">=100":
				"";
		String nsIds = getNamespaceIdsProperty();
		String whereNSIds = "";
		for (String id : nsIds.split(",")) {
			if (!"".equals(whereNSIds)) whereNSIds += " or ";
			if (id.matches("\\d+")) {
				whereNSIds += COL_NAMESPACE + "=" + id;
			}
		}
		if ("".equals(whereNSIds)) {
			whereNSIds = "page_namespace=0 or page_namespace=2"; //default namespaces are main and user
		}
		String where = "";
		if (!"".equals(whereAllCustom) && !"".equals(whereNSIds)) 
			where = whereAllCustom + " or " + whereNSIds;
		else where = whereAllCustom + whereNSIds;
		if (!"".equals(where)) where = " where " + where;
		return where;
	}

	

	private boolean getExportAllCustomNamespaceProperty() {
		boolean defaultProp = true;
		if (nsPropExportAllCustom == null) return defaultProp;
		if ("".equals(nsPropExportAllCustom)) return defaultProp;
		
		if (nsPropExportAllCustom.trim().matches("(?i)false")) return false;
		else if (nsPropExportAllCustom.trim().matches("(?i)true")) return true;
			
		return defaultProp;
	}

	private String getNamespaceIdsProperty() {
		if (nsPropIds == null) return "";
		return nsPropIds;
	}




	Pattern firstCol = Pattern.compile("^(?i)select\\s*(\\w*).*$");
	Pattern allCols = Pattern.compile("^(?i)select\\s*(.*) from.*$");
	/**
	 * get all the mediawiki pages from the database using the optional property SQL.
	 * @return Vector of MediaWikiPage objects containing titles and text, etc.
	 * @throws SQLException if an error occurs while executing the SQL command
	 */
	private void getMediaWikiPages(
			String pageSql, 
			String textSql,
			String titleColumn,
			String textColumn,
			String namespaceColumn,
			String pageIdColumn,
			String textIdColumn) throws SQLException {
		String message = null;
		ResultSet pageData, textData, textIdData;
		pageData = textData = textIdData = null;
		try {
			message = pageSql;
			pageData = sql(pageSql);
			while (pageData.next()) {
				if (!this.running) return;
				//get the relevant strings
				String latest = pageData.getString(textIdColumn); 
				String namespace = pageData.getString(namespaceColumn); 
				String id = pageData.getString(pageIdColumn); 
				byte[] bytes2 = pageData.getBytes(titleColumn); //get bytes, 'cause we might have unicode issues
				String title = null;
				try {
					title = getTitle(bytes2);
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//replace references to page props with real data
				String textSqlAdj = textSql.replaceAll("db.column.pageid", id);
				textSqlAdj = textSqlAdj.replaceAll("db.column.title", title);
				textSqlAdj = textSqlAdj.replaceAll("db.column.namespace", namespace);
				
				//handle histories
				Vector<String> allRevs = new Vector<String>();
				if (gettingHistory()) {
					if (optRevSql != null && !"".equals(optRevSql)) {
						String revsql = optRevSql.replaceAll("db.column.pageid", id);
						Matcher colFinder = firstCol.matcher(revsql);
						if (colFinder.find()) {
							String col = colFinder.group(1); //select SOMECOLUMN
							allRevs = getAllRevIds(revsql, col);
						}
						else {
							log.warn("Couldn't find return column. Using default revsql.");
							allRevs = getAllRevIds(id);
						}
					}
					else allRevs = getAllRevIds(id); //no optional rev sql
				}
				else {
					allRevs.add(latest); //just the latest one
				}
				
				//handle user date data (udmf)
				//user timestamp data
				String defaultUdmfSql = "select " + COL_REV_USER + "," + COL_REV_DATE + "," + COL_REV + 
								 " from " + prefix + REV_TABLE +
								 " where " + COL_REV_PAGE + "='" + id + "';";
				HashMap<String,String[]>revUdmfMap = null;
				if (gettingUserdate()) { 
					if (optUdmfSql != null && !"".equals(optUdmfSql)) {
						String udmfSql = optUdmfSql.replaceAll("db.column.pageid", id);
						Matcher colFinder = allCols.matcher(udmfSql);
						if (colFinder.find()) {
							String[] cols = colFinder.group(1).split(",");
							revUdmfMap = getUserDateMap(udmfSql, cols); //rev_id -> [username,timestamp]
						}
						else {
							log.warn("Couldn't find return columns. Using default revsql.");
							revUdmfMap = getUserDateMap(defaultUdmfSql); //rev_id -> [username,timestamp]
						}
					}
					else revUdmfMap = getUserDateMap(defaultUdmfSql); //no optional rev sql
				}
				

				
				int numRevs = 1;
				String textSqlRepeater = textSqlAdj;
				for (String rev : allRevs) {
					//get text id
					String textIdSql = optTextIdSql.replaceAll("db.column.textid", rev);
					message = textIdSql;
					textIdData = sql(textIdSql);
					String textid = "";
					while (textIdData.next()) {
						if (!this.running) return;
						textid = textIdData.getString(1); //get first column result
					}
					
					//get text
					textSqlAdj = textSqlRepeater.replaceAll("db.column.textid", textid);
					message = textSqlAdj;
					textData = sql(textSqlAdj);
					String text = "";
					while (textData.next() ) {
						if (!this.running) return;
						byte[] bytes = textData.getBytes(COL_TEXT);
						try {
							text = new String(bytes, encoding);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if (gettingUserdate()) { //date for udmf framework: usernames and timestamps 
						if (!this.running) return;
						String userdate = getUserDateData(rev, revUdmfMap);
						text = userdate + text;
					}
					
					if (title == null || text == null || id == null) {
						message = "title, text, or id is null. Check optional sql properties.";
						throw new IllegalArgumentException();
					}
					
					//save the data into a local object
					MediaWikiPage mwpage = new MediaWikiPage(title, text, namespace, id, (numRevs++)+""); 
					
					//output the file to the system
					createFileLocally(mwpage);
					
					textData.close();
					textIdData.close();
				}
			}
		} catch (SQLException e) {
			log.error("Problem while running custom SQL: " + message);
			throw e;
		} finally {
			pageData.close();
		}
	}

	protected String getTitle(byte[] rawbytes) throws UnsupportedEncodingException {
		if (encoding == null) encoding = DEFAULT_ENCODING;
		String title = new String (rawbytes, encoding); //enforce utf-8 encoding
		if (this.urlencoding != null && Boolean.parseBoolean(this.urlencoding))
			return URLEncoder.encode(title, encoding) ; //make directory safe
		return title;
	}

	/**
	 * creates files in the designated output directory (see output field)
	 * Text pages are created in Namespace directories below the output directory.
	 * @param pages MediaWikiPage Vector. 
	 */
	private void createFilesLocally(Vector pages) {
		for (Iterator iter = pages.iterator(); iter.hasNext();) {
			if (!this.running) return;
			MediaWikiPage page = (MediaWikiPage) iter.next();
			createFileLocally(page);
		}
	}

	/**
	 * creates one file for the given MediaWikiPage object
	 * @param page
	 */
	protected void createFileLocally(MediaWikiPage page) {
		if (!this.running) return;
		String filename = gettingHistory()?
				createFilename(page.title, page.namespace, page.versionId):
				createFilename(page.title, page.namespace);
		if (gettingOrigTitle()) {
			log.debug("Adding original title to content: " + page.title);
			page.text += "\n" + "{orig-title:" + page.title +"}\n";
		}
		String filecontents = page.text;
		String parent = getParent(page.namespace);
		createFileLocally(filename, parent, filecontents);
	}

	/**
	 * creates the filename based on the page title.
	 * converts ":" characters to "__" characters.
	 * @param title String, example: ABC or Help:Abc
	 * @return filename, string. example: Abc.txt or Help__Abc.txt
	 */
	protected String createFilename(String title, String namespace) {
		return createFilename(title, namespace, null);
	}
	/**
	 * creates the filename based on the page title.
	 * converts ":" characters to "__" characters.
	 * @param title String, example: ABC or Help:Abc
	 * @param namespace namespace id which identifies the namespace
	 * @param version page revision id, used with page history export
	 * @return filename as string. example: Abc.txt, Help__Abc.txt, Abc-23.txt
	 */
	protected String createFilename(String title, String namespace, String version) {
		log.debug("getting filename from title: " + title);
		String base = title;
		Pattern colons = Pattern.compile(":|(?:%3A)");
		Matcher colonFinder = colons.matcher(base);
		if (colonFinder.find()) base = colonFinder.replaceAll("__");
		Pattern fileDelims = Pattern.compile("[/\\\\]");
		Matcher delimFinder = fileDelims.matcher(base);
		if (delimFinder.find()) base = delimFinder.replaceAll("_");
		
		String extension = ".txt"; 
		if (version != null) {
			String suffix = this.historySuffix;
			Pattern hash = Pattern.compile("#");
			Matcher hashFinder = hash.matcher(suffix);
			if (hashFinder.find()) {
				extension = hashFinder.replaceFirst(version);
			}
			else log.warn("Couldn't find # in history-suffix. Won't be able to preserve histories.");
		}
		
		int namespaceNum = Integer.parseInt(namespace);
		String namespaceStr = "";
		try {
			if (namespaces[namespaceNum].endsWith("Discussions"))
				namespaceStr = "_Discussion";
		} catch (ArrayIndexOutOfBoundsException e) {
			//XXX This Exception is caught and handled on purpose.
			//We don't do anything if we don't know what the namespace it is;
			//We only want to use known namespaces here, so if we run into 
			//an unknown one, we just don't use it.
		}
		String filename = base + namespaceStr + extension;
		log.debug("new filename will be: " + filename);
		return filename;
	}

	/**
	 * gets the parent directory, given the output (a field) and the namespace
	 * @param namespace
	 * @return output/namespace/
	 */
	private String getParent(String namespace) {
		String parent = "";
		try {
			int nsId = Integer.parseInt(namespace);
			String nsName = "";
			if (nsId > (namespaces.length-1)) {
				nsName = getNamespaceDirName(nsId);
			}
			else nsName = namespaces[nsId];
			parent = output + File.separator + nsName + File.separator;			
		} catch (ArrayIndexOutOfBoundsException e) {
			//this shouldn't happen, but we'll handle this just in case
			parent = output + File.separator + "Misc" + File.separator;
		}
		log.debug("Parent directory = " + parent);
		return parent;
	}

	protected String getNamespaceDirName(int nsId) {
		HashMap<Integer,String> nsCustomMap = getNamespaceCustomMap();
		if (nsCustomMap.containsKey(nsId)) return nsCustomMap.get(nsId);
		return nsId+"";
	}

	private HashMap<Integer, String> getNamespaceCustomMap() {
		if (this.nsCustomMap == null) {
			this.nsCustomMap = new HashMap<Integer, String>();
			if (this.nsPropCustomMap == null) return this.nsCustomMap;
			for (String pair : this.nsPropCustomMap.split(",")) {
				String[] parts = pair.split("=>");
				if (parts != null && parts.length == 2) {
					String key = parts[0];
					String val = parts[1];
					if (!key.matches("\\d+")) {
						log.error("Invalid property: namespaces.customnamespace.mapping");
						continue;
					}
					this.nsCustomMap.put(Integer.parseInt(key), val);
				}
				else {
					log.error("Invalid property: namespaces.customnamespace.mapping");
				}
			}
		}
		return this.nsCustomMap;
	}

	/**
	 * Creates the file at the given parentDir/filename, and writes the
	 * given filecontents to that file
	 * @param filename string, filename to be created. ex: Abc.txt
	 * @param parentDir string, parentdir filename will exist in. ex: outputdir/namespace/
	 * @param filecontents, string, text to be written to the newly created file
	 */
	private void createFileLocally(String filename, String parentDir, String filecontents) {
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
		writeFile(path, text, encoding);
	}
	
	/* Start History Methods */

	private boolean gettingHistory() {
		try {
			return Boolean.parseBoolean(this.history);
		} catch (RuntimeException e){
			return false;
		}
	}

	private Vector<String> getAllRevIds(String id) throws SQLException {
		String col = COL_REV; //rev ids (not rev text ids)
		String sql = "select " + col +
				" from " + prefix + REV_TABLE +
				" where " + COL_REV_PAGE + "=" + id;
		return getAllRevIds(sql, col);
	}

	/**
	 * 
	 * @param sql SQL statement that will be run to get all the revision ids for
	 * a given page id.
	 * @param col the name of the column that is associated with revision id.
	 * @return Vector of revision ids
	 * @throws SQLException
	 */
	private Vector<String> getAllRevIds(String sql, String col) throws SQLException {
		ResultSet data = null;
		try {
			data = sql(sql);
			Vector<String> all = new Vector<String>();
			while (data.next()) {
				if (!this.running) return null;
				String rev = data.getString(col);
				all.add(rev);
			}
			return all;
		}
		finally {
			data.close();
		}
	}
	
	/* End History Methods */
	
	/* Start User Date (udmf) Methods */
	
	private boolean gettingUserdate() {
		try {
			return Boolean.parseBoolean(this.udmf);
		} catch (RuntimeException e){
			return false;
		}
	}

	private HashMap<String, String[]> getUserDateMap(String sql) throws SQLException {
		String[] cols = {COL_REV, COL_REV_USER, COL_REV_DATE};
		return getUserDateMap(sql, cols);
	}
	private HashMap<String, String[]> getUserDateMap(String sql, String[] returncols) throws SQLException {
		HashMap<String, String[]> map = new HashMap<String, String[]>();
		ResultSet data = null;
		try {
			data = sql(sql);
			String user = "", date = "", rev = "";

			while (data.next()) {
				rev = data.getString(returncols[0].trim());
				byte[] userbytes = data.getBytes(returncols[1].trim());
				try {
					user = new String(userbytes, encoding);
				} catch (UnsupportedEncodingException e) {
					log.warn("Problem with encoding: " + encoding);
					e.printStackTrace();
					user = data.getString(returncols[1].trim()); 
				}
				date = data.getString(returncols[2].trim());
				String[] val = {user,date};
				map.put(rev, val);
			}
		} finally { 
			data.close();
		}
		return map;
	}

	private String getUserDateData(String rev, HashMap<String, String[]> revUdmfMap) {
		if (!revUdmfMap.containsKey(rev)) return "";
		String[] data = revUdmfMap.get(rev);
		String userdate = ("".equals(data[0])?"":"{user:" + data[0] + "}\n") +
				"{timestamp:" + data[1] + "}\n";
		return userdate;
	}

	/* End User Date (udmf) Methods */
	
	/* Orig Title Methods */
	
	private boolean gettingOrigTitle() {
		try {
			return Boolean.parseBoolean(this.origtitle);
		} catch (RuntimeException e){
			return false;
		}
	}
	
	/* Start Setters/Getters */
	protected void setEncoding(String encoding) {
		this.encoding = encoding; //useful for junit
	}
	/**
	 * @param urlencoding true/false
	 */
	protected void setUrlEncoding(String urlencoding) {
		this.urlencoding = urlencoding; //useful for junit
	}
	/**
	 * simple class to hold page data we might need when outputing the file
	 * @author Laura Kolker
	 */
	private class MediaWikiPage {
		public String title;
		public String text;
		public String namespace;
		public String id;
		public String versionId;
		
		MediaWikiPage (String title, String text, String namespace, String id, String versionId) {
			this.title = title;
			this.text = text;
			this.namespace = namespace;
			this.id = id;
			this.versionId = versionId;
		}
	}

}
