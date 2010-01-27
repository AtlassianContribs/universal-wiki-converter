package com.atlassian.uwc.exporters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.exporters.mindtouch.MindtouchFileIdParser;
import com.atlassian.uwc.exporters.mindtouch.MindtouchPage;
import com.atlassian.uwc.exporters.mindtouch.MindtouchPagelistParser;

public class MindtouchExporter implements Exporter {

	private static final String EXT_ATT = "attachments";
	private static final String EXT_SUB = "subpages";
	private static final String FILECONTENT_TAGEND = "</pagedata>";
	private static final String FILECONTENT_TAGSTART = "<pagedata>";
	private static final String EXT_FILE = ".xml";
	private static final String FILESYS_DELIM = "_";
	private static final String DEFAULT_USERAGENT = "Universal Wiki Converter";
	private static final String DEFAULT_BUFFERSIZE = "1024";
	private static final String DEFAULT_TIMEOUT_MS = "10000";
	private static final String DEFAULT_IGNOREMT = "true";
	private static final String DEFAULT_PORT = "80";
	private static final String EXPORT_DIR = "exported_mindtouch_pages";
	
	public static final String PROPKEY_URLBASE = "url.base";
	public static final String PROPKEY_USER = "user";
	public static final String PROPKEY_PASS = "pass";
	public static final String PROPKEY_URLPORT = "url.port";
	public static final String PROPKEY_USERAGENT = "user-agent";
	public static final String PROPKEY_OUTPUTDIR = "output.dir";
	public static final String PROPKEY_BUFFERSIZE = "att.buffer";
	public static final String PROPKEY_TIMEOUT = "timeout";
	public static final String PROPKEY_IGNOREMT = "ignore.mindtouch";

	
	private boolean running = false;
	private Map properties;
	private HttpClient client;
	private int error;
	
	Logger log = Logger.getLogger(this.getClass());

	
	
	public void export(Map propertiesMap) {
		log.info("Exporting Mindtouch...");
		this.running = true;
		
		setProperties(propertiesMap);
		export();
		
		this.running = false;
		log.info("Export Complete.");
	}

	private void export() {
		if (!this.running) return;
		log.info("Getting page tree.");
		Vector<MindtouchPage> pages = getPages();
		if (pages == null) return;
		log.info("Getting page content.");
		pages = getContent(pages);
		log.info("Getting page tags.");
		pages = getTags(pages);
		log.info("Getting page comments.");
		pages = getComments(pages);
		log.info("Getting attachment info.");
		pages = getAttachments(pages);

		outputPageData(pages);
	}

	protected Vector<MindtouchPage> getPages() {
		if (!this.running) return null;
		String xml = getPagelistXml();
		if (xml == null) return null;
		Vector<MindtouchPage> pages = parsePageXml(xml);
		if (isIgnoringMindtouch()) {
			pages = MindtouchPage.removeNode("MindTouch", pages);
		}
		return pages;
	}

	protected String getPagelistXml() {
		if (!this.running) return null;
		String baseurl = getBaseUrl();
		int port = getPort();
		String user = getUser();
		String pass = getPass();

		String apiurl = createApiUrl(baseurl);
		String forceAuthentication = "?authenticate=true";
		//rest api for sitemap
		//http://developer.mindtouch.com/Deki/API_Reference/GET%3apages
		String url = apiurl + "pages/" + forceAuthentication; 
		GetMethod method = new GetMethod(url);
		
		HttpClient client = getClient();
		authenticate(baseurl, port, user, pass, method, client);
		
		try {
			int result = client.executeMethod(method);
			if (result != 200) {
				handleError(result, method);
			}
			return method.getResponseBodyAsString();
		} catch (Exception e) {
			log.error("Problem occured when making rest request to: " + url);
			e.printStackTrace();
			throw new RuntimeException(e); //we need to do this to tell the UWC that an err occurred
		} finally {
			method.releaseConnection();
		}
	}

	protected void handleError(int result, HttpMethod method) {
		log.error("Problem encountered when making rest call: " + 
				result + ": " + method.getStatusLine());
		this.error = result;
		throw new BadResult("" + result);
	}

	private void authenticate(String baseurl, int port, String user, String pass, HttpMethod method, HttpClient client) {
		client.getState().setCredentials(new AuthScope(baseurl, port),
				new UsernamePasswordCredentials(user, pass));
		method.setDoAuthentication(true);
	}

	
	protected Vector<MindtouchPage> parsePageXml(String xml) {
		if (!this.running) return null;
		log.debug("Parsing page list xml.");
		MindtouchPagelistParser parser = new MindtouchPagelistParser();
		parseXml(xml, parser);
		return parser.getPages();
	}

	private void parseXml(String xml, DefaultHandler parser) {
		if (!this.running) return;
		//set up parser objects
		XMLReader reader = getXmlReader();
		reader.setContentHandler(parser);
		reader.setErrorHandler(parser);
		//parse
		InputSource source = new InputSource(new StringReader(xml));
		System.setProperty("http.agent", getUserAgent());
		try {
			reader.parse(source);
		} catch (Exception e) {
			String message = "Error while parsing xml. Skipping";
			log.error(message);
			throw new RuntimeException(e); //Skipping
		}
	}
	
	/**
	 * @return the object that will be used to drive the parsing
	 */
	private XMLReader getXmlReader() {
		try {
			return XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			String message = "Could not load XmlReader. Skipping.";
			log.error(message);
			e.printStackTrace();
			return null;
		}
	}

	private String getUserAgent() {
		if (!this.properties.containsKey("user-agent"))
			return DEFAULT_USERAGENT;
		return (String) this.properties.get("user-agent");
	}

	protected Vector<MindtouchPage> getContent(Vector<MindtouchPage> pages) {
		if (!this.running) return pages;
		for (MindtouchPage page : pages) {
			log.debug("...Getting content for: " + getFilename(page));
			//get the content for this page
			page.content = getContent(page.id);
			//get the content for subpages
			getContent(page.getSubpages());
		}
		return pages;
	}

	protected String getContent(String id) {
		if (!this.running) return null;
		String content = getPagePart(id, "contents", "format=xhtml");
		if (content != null)
			content = getAttachmentParents(content);
		return content;
	}

	Pattern attachment = Pattern.compile("" +
			"((?:(?:src)|(?:href))=\"[^\"]+[@]api\\/deki\\/files\\/)(\\d+)(\\/=[^\"]+)");
	Pattern title = Pattern.compile("(?s)<title>(.*?)<\\/title>");
	protected String getAttachmentParents(String input) {
		Matcher attFinder = attachment.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (attFinder.find()) {
			found = true;
			String before = attFinder.group(1);
			String id = attFinder.group(2);
			String after = attFinder.group(3);
			String fileinfo = getFileInfo(id, "info", ""); //XXX what if fileinfo is null?
			Matcher titleFinder = title.matcher(fileinfo);
			if (titleFinder.find()) {
				String title = titleFinder.group(1);
				after += "?parent=" + title;
			}
			String replacement = before + id + after;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			attFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			attFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected Vector<MindtouchPage> getTags(Vector<MindtouchPage> pages) {
		if (!this.running) return pages;
		for (MindtouchPage page : pages) {
			log.debug("...Getting tags for: " + getFilename(page));
			//get the content for this page
			page.tags = getTags(page.id);
			//get the content for subpages
			getTags(page.getSubpages());
		}
		return pages;
	}
	
	protected String getTags(String id) {
		if (!this.running) return null;
		return getPagePart(id, "tags", "");
	}
	

	protected Vector<MindtouchPage> getComments(Vector<MindtouchPage> pages) {
		if (!this.running) return pages;
		for (MindtouchPage page : pages) {
			log.debug("...Getting comments for: " + getFilename(page));
			//get the content for this page
			page.comments = getComments(page.id);
			//get the content for subpages
			getComments(page.getSubpages());
		}
		return pages;
	}
	
	protected String getComments(String id) {
		if (!this.running) return null;
		return getPagePart(id, "comments", "sortby=date.posted");
	}
	
	protected Vector<MindtouchPage> getAttachments(Vector<MindtouchPage> pages) {
		if (!this.running) return pages;
		for (MindtouchPage page : pages) {
			log.debug("...Getting file data for: " + getFilename(page));
			//get the content for this page
			page.attachments = getAttachments(page.id);
			//get the content for subpages
			getAttachments(page.getSubpages());
		}
		return pages;
	}
	
	protected String getAttachments(String id) {
		if (!this.running) return null;
		return getPagePart(id, "files", "");
	}
	
	/* XXX Start OutputPageData methods */
	
	protected void outputPageData(Vector<MindtouchPage> pages) {
		if (!this.running) return;
		String output = getOutput();
		File outdir = cleanOutputDir(output);
		log.info("Writing to file system.");
		outputPageData(pages, outdir);
	}
	
	protected void outputPageData(Vector<MindtouchPage> pages, File outdir) {
		for (MindtouchPage page : pages) {
			if (!this.running) return;
			//write page content file
			String filename = getFilename(page);
			String content = getFileContent(page);
			File file = createFile(filename, outdir);
			log.debug("...Writing page content to: " + filename);
			write(file, content);
			//attachment directory
			if (page.hasAttachments()) {
				String attdir = getAttFilename(page);
				File attdirFile = createDir(attdir, outdir);
				outputAttachmentData(page, attdirFile);
			}
			//subpages directory
			if (!page.getSubpages().isEmpty()) {
				String subdir = getSubFilename(page);
				File subdirFile = createDir(subdir, outdir);
				outputPageData(page.getSubpages(), subdirFile);
			}
		}
		
	}

	protected String getFilename(MindtouchPage page) {
		return page.id + FILESYS_DELIM + getFilesystemTitle(page.title) + EXT_FILE;
	}

	Pattern notwordchar = Pattern.compile("\\W");
	protected String getFilesystemTitle(String input) {
		Matcher notwordFinder = notwordchar.matcher(input);
		return notwordFinder.replaceAll("");
	}

	protected String getFileContent(MindtouchPage page) {
		return FILECONTENT_TAGSTART + 
				page.content + page.tags + page.comments + page.attachments + 
				FILECONTENT_TAGEND;
	}

	protected File createFile(String filename, File parent) {
		String abspath = parent.getAbsolutePath() + File.separator + filename;
		return new File(abspath);
	}

	protected void write(File file, String content) {
		try {
			FileOutputStream fw = new FileOutputStream(file);
			OutputStreamWriter outstream = new OutputStreamWriter(fw,
					"utf-8");
			BufferedWriter out = new BufferedWriter(outstream);
			out.write(content);
			out.close();
		} catch (IOException e) {
			log.error("Problem writing to file: " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	protected String getAttFilename(MindtouchPage page) {
		return page.id + FILESYS_DELIM + getFilesystemTitle(page.title) + 
				FILESYS_DELIM + EXT_ATT;
	}

	// output the actual attachments using the attachment data and another rest call
	protected void outputAttachmentData(MindtouchPage page, File attdir) {
		if (!this.running) return;
		//get the file ids from the saved xml
		String xml = page.attachments;
		MindtouchFileIdParser parser = new MindtouchFileIdParser();
		parseXml(xml, parser);
		Vector<String> ids = parser.getIds();
		Vector<String> names = parser.getNames();
		//get the files from the rest api and save to file system
		outputAttachmentData(ids, names, attdir);
	}
	
	protected void outputAttachmentData(Vector<String> ids, Vector<String> names, File attdir) {
		if (!this.running) return;
		String parent = attdir.getAbsolutePath();
		if (!parent.endsWith(File.separator)) parent += File.separator;
		
		String baseurl = getBaseUrl();
		int port = getPort();
		String user = getUser();
		String pass = getPass();
		
		String apiurl = createApiUrl(baseurl);
		String forceAuthentication = "?authenticate=true";
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			String name = names.get(i);
			log.debug("...Downloading file " + name);
			//http://192.168.2.247/@api/deki/files/fileid
			//http://developer.mindtouch.com/Deki/API_Reference/GET%3afiles%2f%2f%7bfileid%7d
			String url = apiurl + "files/" + id + "/" + forceAuthentication; 
			GetMethod method = new GetMethod(url);
			
			HttpClient client = getClient();
			authenticate(baseurl, port, user, pass, method, client);
			
			//get file from rest as stream
			byte[] response = null;
			try {
				response = getFile(method, client);
			} catch (Exception e) {
				log.error("Problem occured when making rest request to: " + url);
				e.printStackTrace();
				return;
			}
			//write bytes as file 
			String path = parent + name;
			try {
				writeFile(response, path);
			} catch (IOException e) {
				log.error("Problem writing to file: " + path);
				e.printStackTrace();
			}
		}
	}
	
	private byte[] getFile(HttpMethod method, HttpClient client) throws HttpException, IOException {
		byte[] response = null;
		try {
			int result = client.executeMethod(method);
			if (result != 200) {
				handleError(result, method);
			}
			//output them to the filesystem in the attdir
			response = method.getResponseBody();
		} finally {
			method.releaseConnection();
		}
		return response;
	}

	/**
	 * writes the given bytes to the file at the given path
	 * @param path string, filepath where text will be written
	 * @param bytes byte array, btyes to write to filepath
	 * @throws IOException 
	 */
	protected void writeFile(byte[] bytes, String path) throws IOException {
		FileOutputStream filestream = null;
		BufferedOutputStream outstream = null;
		try {
			filestream = new FileOutputStream(path);
			outstream = new BufferedOutputStream(filestream);
			outstream.write(bytes);
		} finally {
			outstream.close();
			filestream.close();
		}
	}
	
	protected String getSubFilename(MindtouchPage page) {
		return page.id + FILESYS_DELIM + getFilesystemTitle(page.title) + 
		FILESYS_DELIM + EXT_SUB;
	}

	protected File createDir(String newdir, File parentdir) {
		String parent = parentdir.getAbsolutePath();
		if (!parent.endsWith(File.separator)) parent += File.separator;
		String path = parent + newdir;
		File dirFile = new File(path);
		if (dirFile.mkdir()) {
			return dirFile;
		}
		log.error("Problem creating directory: " + path);
		return dirFile;
	}
	
	/* XXX End OutputPageData methods */
	
	protected String getPagePart(String id, String pagepart, String parameters) {
		if (!this.running) return null;
		String baseurl = getBaseUrl();
		int port = getPort();
		String user = getUser();
		String pass = getPass();

		String apiurl = createApiUrl(baseurl);
		String forceAuthentication = "?authenticate=true";
		//rest api for getting one page's content
		//http://developer.mindtouch.com/Deki/API_Reference/GET%3apages%2f%2f%7bpageid%7d%2f%2fcontents
		if (!"".equals(parameters)) parameters = "&" + parameters;
		String url = apiurl + "pages/" + id + "/" + pagepart + "/" + forceAuthentication + parameters; 
		GetMethod method = new GetMethod(url);
		
		HttpClient client = getClient();
		authenticate(baseurl, port, user, pass, method, client);
		
		try {
			int result = client.executeMethod(method);
			if (result != 200) {
				handleError(result, method);
			}
			return method.getResponseBodyAsString();
		} catch (Exception e) {
			log.error("Problem occured when making rest request to: " + url);
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return null;
	}

	protected String getFileInfo(String id, String pagepart, String parameters) {
		if (!this.running) return null;
		String baseurl = getBaseUrl();
		int port = getPort();
		String user = getUser();
		String pass = getPass();

		String apiurl = createApiUrl(baseurl);
		String forceAuthentication = "?authenticate=true";
		if (!"".equals(parameters)) parameters = "&" + parameters;
		String url = apiurl + "files/" + id + "/" + pagepart + "/" + forceAuthentication + parameters; 
		GetMethod method = new GetMethod(url);
		
		HttpClient client = getClient();
		authenticate(baseurl, port, user, pass, method, client);
		
		try {
			int result = client.executeMethod(method);
			if (result != 200) {
				handleError(result, method);
			}
			return method.getResponseBodyAsString();
		} catch (Exception e) {
			log.error("Problem occured when making rest request to: " + url);
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
		return null;
	}

	
	protected String createApiUrl(String input) {
		if (!input.endsWith("/")) input += "/";
		input += "@api/deki/";
		if (!input.startsWith("http:")) input = "http://" + input;
		return input;
	}

	
	/**
	 * deletes and recreates the output directory
	 */
	protected File cleanOutputDir(String output) {
		if (!this.running) return null;
		if (!output.endsWith(File.separator)) output += File.separator;
		output = output + EXPORT_DIR;
		File file = new File(output);
		if (!file.exists()) {
			log.info("Creating output directory: " + output);
			file.mkdir();
		}
		else {
			deleteDir(file);
			file.mkdir();
			log.info("Cleaning and creating output directory: " + output);
		}
		return file;
	}

	/**
	 * deletes the given file. This method is used recursively.
	 * @param file can be a directory or a file. Directory does not have to be empty.
	 */
	protected void deleteDir(File file) {
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
			return;
		}
		else { // or delete the directory
			File[] files = file.listFiles();
			for (File f : files) {
				deleteDir(f);
			}
			file.delete();
		}

	}

	
	
	public void cancel() {
		this.running = false;
	}

	/* Getters and Setters */
	public Map getProperties() {
		return properties;
	}

	private String getBaseUrl() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_URLBASE);
		if (prop == null) {
			log.error("Problem examining exporter settings.");
			throw new IllegalArgumentException("url.base must be defined. See exporter.mindtouch.properties");
		}
		return prop;
	}

	private int getPort() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_URLPORT);
		if (prop == null)  prop = DEFAULT_PORT; //default port;
		return Integer.parseInt(prop);
	}
	
	private String getUser() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_USER);
		if (prop == null) {
			log.error("Problem examining exporter settings.");
			throw new IllegalArgumentException("user must be defined. See exporter.mindtouch.properties");
		}
		return prop;
	}
	
	private String getPass() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_PASS);
		if (prop == null) prop = ""; //default
		return prop;
	}
	
	Pattern notdigit = Pattern.compile("\\D");
	private int getBufferSize() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_BUFFERSIZE);
		if (prop == null || notdigit.matcher(prop).find()) prop = DEFAULT_BUFFERSIZE; //default
		return Integer.parseInt(prop);
	}
	
	private String getOutput() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_OUTPUTDIR);
		if (prop == null) {
			log.error("Problem examining exporter settings.");
			throw new IllegalArgumentException("output.dir must be defined. See exporter.mindtouch.properties");
		}
		return prop;
	}
	
	private HttpClient getClient() {
		if (this.client == null) {
			this.client = new HttpClient();
			HttpConnectionManager httpConnectionManager = client.getHttpConnectionManager();
			HttpConnectionManagerParams params = httpConnectionManager.getParams();
			params.setConnectionTimeout(getTimeout());
		}
		return this.client;
	}
	
	private int getTimeout() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_TIMEOUT);
		if (prop == null) prop = DEFAULT_TIMEOUT_MS;
		return Integer.parseInt(prop);
	}
	
	private boolean isIgnoringMindtouch() {
		String prop = (String) properties.get(MindtouchExporter.PROPKEY_IGNOREMT);
		if (prop == null) prop = DEFAULT_IGNOREMT;
		return Boolean.parseBoolean(prop);
	}
	
	public int getError() {
		return error;
	}

	public void setProperties(Map properties) { //useful for unit testing
		this.properties = properties;
	}
	
	protected void startRunning() { //useful for junit
		this.running = true; 
	}

	public class BadResult extends RuntimeException {

		public BadResult(String message) {
			super(message);
		}
		
	}
}
