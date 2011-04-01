package com.atlassian.uwc.exporters;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.atlassian.uwc.ui.FileUtils;

public class JiveExporter extends SQLExporter {

	private static final String OUTPUT_INNERDELIM = "\t";
	private static final int BLOG_JIVECONSTANT = 38;
	private static final int DOC_JIVECONSTANT = 102;
	private static final int SPACE_JIVECONSTANT = 14;
	private static final String DEFAULT_ENCODING = "utf-8";
	public static final String EXPORT_DIR_NAME = "exported_jive_content";
	//JDBC Settings
	private String driver;
	private String url;
	private String name;
	private String login;
	private String pass;

	//Export settings
	private String outdir;
	private String containerOutFile;
	private String hierarchyOutFile;
	private String attachmentOutFile;
	private String titleOutFile;
	private HashMap<String,String> sqlStatements;
	private HashMap<String, String> containerSqls;
	private String encoding = DEFAULT_ENCODING;
	
	//Debug settings
	private boolean debug;
	private String debugOutFile;
	
	//State
	private HashMap<String, Container> containers;
	private Hierarchy hierarchy;
	private HashMap<String, Hierarchy> allchildren = new HashMap<String, JiveExporter.Hierarchy>();
	private boolean hasWrittenAttData = false;
	private boolean hasWrittenTitleData = false;
	
	public void export(Map propertiesMap) throws ClassNotFoundException,
			SQLException {
		this.running = true;
		log.info("Jive Content Export - STARTING\n");
		//load configuration settings
		loadConfig(propertiesMap);
		//connect to database
		connectToDB(driver, url, name, login, pass);
		//export files and output one at a time
		exportJive();
		//close database
		closeDB();
		log.info("Jive Content Export - COMPLETED\n");
		this.running = false;
	}

	private void loadConfig(Map propertiesMap) {
		//jdbc
		this.driver = (String) propertiesMap.get("jdbc.driver.class");
		this.url = (String) propertiesMap.get("dbUrl");
		this.name = (String) propertiesMap.get("databaseName");
		this.login = (String) propertiesMap.get("login");
		this.pass = (String) propertiesMap.get("password");
		
		//export
		this.outdir = (String) propertiesMap.get("output");
		this.containerOutFile = (String) propertiesMap.get("output.container");
		this.hierarchyOutFile = (String) propertiesMap.get("output.hierarchy");
		this.attachmentOutFile = (String) propertiesMap.get("output.attachments");
		this.titleOutFile = (String) propertiesMap.get("output.titles");
		this.sqlStatements = new HashMap<String, String>();
		Set<String> keySet = propertiesMap.keySet();
		for (String key : keySet) {
			if (key.startsWith("sql.")) 
				this.sqlStatements.put(key, (String) propertiesMap.get(key));
		}
		this.encoding = (String) propertiesMap.get("encoding");
		if (this.encoding == null) this.encoding = DEFAULT_ENCODING;
		
		//debug
		this.debug = Boolean.parseBoolean((String) propertiesMap.get("debug"));
		this.debugOutFile = (String) propertiesMap.get("debug.out");
		
		if (this.debug) {
			log.info("Config Settings:\n" +
					"driver = " +this.driver + "\n" +
					"url = " +this.url + "\n" +
					"name = " +this.name+ "\n" +
					"login = " +this.login+ "\n" +
					"pass = " +this.pass + "\n" +
					"outdir = " +this.outdir + "\n" +
					"debug = " +this.debug + "\n" +
					"debugOutfile = " +this.debugOutFile + "\n"
					);
		}
	}

	private String testString = "";
	private void exportJive() {
		if (!this.running) return;
		if (this.debug) {
			for (String key : this.sqlStatements.keySet()) {
				if (key.startsWith("sql.test.")) 
					testSql(this.sqlStatements.get(key));
			}
		}
		else { //only prepare the export directory if we're not in debug mode
			if (!prepExpDir()) return;
		}
		setupEncoding();
		exportDocuments();
		exportBlogposts();
		//XXX exportThreads();
		if (!getContainers().isEmpty()) { //container data should have been filled when doc and other obj type data was run
			exportContainers();
			exportHierarchy();
		}
	}

	private boolean prepExpDir() {
		if (!this.running) return false;
		String expdirpath = getExportPath(this.outdir);
		log.debug("Preparing Export Directory: " + expdirpath);
		File expdir = new File(expdirpath);
		if (expdir.exists()) {
			log.debug("Deleting pre-existing export directory: " + expdirpath);
			FileUtils.deleteDir(expdir);
		}
		if (!expdir.exists()) {
			log.debug("Creating empty export directory: " + expdirpath);
			expdir.mkdir();
		}
		if (!expdir.exists()) {
			log.error("Problem creating export directory. " +
					"Check that configured output directory exists " +
					"and that you have permission to write to that directory: " + this.outdir);
			return false;
		}
		return true;
	}
	

	private void setupEncoding() {
		if (!this.running) return;
		if (this.sqlStatements.containsKey("sql.encoding")) {
			String sqlStatement = this.sqlStatements.get("sql.encoding");
			log.debug("Setting encoding: " + sqlStatement);
			try {
				ResultSet encResults = sql(sqlStatement, true);
			} catch (SQLException e) {
				log.error("Problem setting encoding.");
			}
		}
	}


	private void exportDocuments() {
		if (!this.running) return;
		log.debug("Exporting Documents");
		if (this.sqlStatements.containsKey("sql.doc") && this.sqlStatements.containsKey("sql.doc.versions")) {
			int commentIndex = 0;
			int attachmentIndex = 0;
			String sqlStatement = this.sqlStatements.get("sql.doc");
			try {
				ResultSet docResults = sql(sqlStatement);
				//get the count data
				docResults.last();
				int maxDocs = docResults.getRow();
				docResults.beforeFirst();
				if (maxDocs > 0) testString += "Count: " + maxDocs + "\n";
				// go through the results for each page version
				int index = 0;
				String lastfile = "";
				Vector<Long> noResultsIds = new Vector<Long>();
				while (docResults.next()) {
					if (!this.running) return;
					//get basic data
					long internalDocId = docResults.getLong("internalDocID");
					int latestVersionId = docResults.getInt("versionID");
					int containerType = docResults.getInt("containerType");
					long containerId = docResults.getLong("containerId");
					
					//filter by container
					if (!fromAllowedContainer(containerId, containerType)) {
						maxDocs--;
						continue;
					}
					
					
					//handle versions
					String versionSql = this.sqlStatements.get("sql.doc.versions");
					versionSql = versionSql.replaceFirst("\\?", "'"+internalDocId + "'");
					ResultSet versionResults = sql(versionSql);
					boolean hasresults = false;
					while (versionResults.next()) {
						hasresults = true;
						String title = versionResults.getString("title");
						long userid = versionResults.getLong("userId");
						String username = ((userid > 0)?getUsername(userid):userid+"");
						long creationDate = versionResults.getLong("creationDate");
						long modificationDate = versionResults.getLong("modificationDate");
						String summary = versionResults.getString("summary");
						int versionId = versionResults.getInt("versionID");
						String bodyText = getBodyText(internalDocId);
						String tags = getTags(internalDocId, DOC_JIVECONSTANT);

						if (this.debug && tags != null) {
							testString += internalDocId + ": " + tags + "\n";
						}
						else if (this.debug) {
							testString += "|" + internalDocId + "|" + versionId + "|" + 
							containerType + "|" + containerId + "|" +
							username.substring(0,2) + "|" + creationDate + "|" + 
							modificationDate + "|" +  
							((summary == null)?"NO":"") + "SUMMARY|" +
							((bodyText == null)?"NO":"") + "BODY|" +
							"\n";
						}
						else {
							String contentType = "DOC";
							String expdir = getExportPath(this.outdir);
							String containerdir = createContainerDir(expdir, containerType, containerId);
							String filename = createFilename(internalDocId, versionId, contentType, title);
							String filepath = expdir + containerdir + filename;
							String content = createDocContent(internalDocId, versionId, contentType, title,
									containerType, containerId,
									username, creationDate, modificationDate, summary, bodyText, tags);
							lastfile = filepath;
							writeFile(filepath, content, this.encoding); 
							
							//export title data (we'll need this in a seperate map file for links and attachments to other pages)
							exportTitles(internalDocId, DOC_JIVECONSTANT, containerId, containerType, versionId, title);
						}
					}
					//export comments
					commentIndex += exportComments(internalDocId,
							containerType, containerId, "doc");
					
					//export attachment data (for use with attachments on the file system)
					attachmentIndex += exportAttachments(internalDocId, DOC_JIVECONSTANT);
					
					//do some logging
					if (!hasresults) {
						noResultsIds.add(internalDocId);
					}
					if ((index % 100) == 0) {
						double percent = Math.round(100*(((double) index)/maxDocs));
						log.debug("Exporting " + index + " Documents. " + percent + "%");
					}
					index++;
				}
				
				log.info("Exporting " + index + " Documents. 100.0%");
				log.info("Exported " + commentIndex + " comments for document.");
				log.info("Exported data for " + attachmentIndex + " attachments.");
				log.debug("Lastfile = " + lastfile);
				if (noResultsIds.size() > 0) {
					int expectedtotal = maxDocs - noResultsIds.size();
					String ids = "";
					for (Long id : noResultsIds) {
						if (!"".equals(ids)) ids += ", ";
						ids += id +"";
					}
					log.info("Some pages had no results. no-results-count = " + noResultsIds.size()
							+ "\n" 
							+ "Expected Total number of files exported: " + expectedtotal + "\n"
							+ "Affected ids: " + ids + "\n");
				}
				if (this.debug) {
					testString += "Final Count: " + index + "\n";
					writeFile(this.debugOutFile, testString, this.encoding);
				}
			} catch (SQLException e) {
				log.error("Problem getting documents: " + sqlStatement, e);
				return;
			}
		}
	}

	private int exportAttachments(long objid, int objtype) throws SQLException {
		if (!this.running) return 0;
		if (this.attachmentOutFile == null) return 0;
		int index = 0;
		if (this.sqlStatements.containsKey("sql.att.attachment")) {
			String type = "attachment";
			index += handleAttachmentType(objid, objtype, type);
		}
		if (this.sqlStatements.containsKey("sql.att.image")) {
			String type = "image";
			index += handleAttachmentType(objid, objtype, type);
		}
		if (objtype != DOC_JIVECONSTANT) return index; //only doing binarybody obj for document types
		if (this.sqlStatements.containsKey("sql.att.binaryBody")) {
			String type = "binaryBody";
			index += handleAttachmentType(objid, objtype, type);
		}
		return index;
	}

	protected int handleAttachmentType(long objid, int objtype, String type) throws SQLException {
		String attSql = this.sqlStatements.get("sql.att." + type);
		attSql = attSql.replaceAll("@objid", "'"+objid + "'");
		attSql = attSql.replaceAll("@objtype", "'"+objtype + "'");
		ResultSet attResults = sql(attSql);
		int index = 0;
		while (attResults.next()) {
			long id = attResults.getLong("id");
			String name = attResults.getString("fileName");
			long date = attResults.getLong("date");
			
			if (this.debug) {
				name = name.substring(0,1);
				String dateString = date + "";
				dateString = dateString.substring(dateString.length()-1);
				date = Long.parseLong(dateString);
			}
			
			String attachmentData = objid + OUTPUT_INNERDELIM + 
				objtype + OUTPUT_INNERDELIM +
				id + OUTPUT_INNERDELIM + 
				name + OUTPUT_INNERDELIM +
				type + OUTPUT_INNERDELIM + 
				date + "\n";
			
			//write attachment data
			if (!hasWrittenAttData) {
				attachmentData = "objid\tobjtype\tattid\tfilename\ttype\ttimestamp\n" +
									attachmentData;
				writeFile(this.attachmentOutFile, attachmentData, this.encoding);
				hasWrittenAttData = true;
			}
			else {
				appendToFile(this.attachmentOutFile, attachmentData, this.encoding);
			}
			index++;
			
		}
		return index;
	}

	protected int exportComments(long referrerId,
			int containerType, long containerId, String exporttype) throws SQLException {
		if (!this.running) return 0;
		int index = 0;
		//handle comments
		if (this.sqlStatements.containsKey("sql.comment." + exporttype)) {
			String commentSql = this.sqlStatements.get("sql.comment." + exporttype);
			commentSql = commentSql.replaceAll("\\?", "'"+referrerId + "'");
			ResultSet commentResults = sql(commentSql);
			while (commentResults.next()) {
				long commentid = commentResults.getLong("commentId");
				long parentCommentId = commentResults.getLong("parentCommentId"); //null or comment being replied to
				int objtype = commentResults.getInt("objectType"); //for blogs: 38 (blogpost), for docs: 102
				long objid = commentResults.getLong("objectId"); //blogpostid or internaldocid
				long userid = commentResults.getLong("userId");
				String username = ((userid > 0)?getUsername(userid):userid+"");
				long creationDate = commentResults.getLong("creationDate");
				long modDate = commentResults.getLong("modificationDate");
				String body = commentResults.getString("body");
				
				if (this.debug) {
					testString += "|" + commentid + "|COMMENT|\n"; 
				}
				else {
					String contentType = "COMMENT";
					String title = objtype+"_"+objid;
					int versionId = 1;
					String expdir = getExportPath(this.outdir);
					String containerdir = createContainerDir(expdir, containerType, containerId);
					String filename = createFilename(commentid, versionId, contentType, title);
					String filepath = expdir + containerdir + filename;
					String content = createCommentContent(commentid, versionId, contentType, title, 
							parentCommentId, objtype, objid, //comment parentage info
							username, null, creationDate, modDate, body);
					writeFile(filepath, content, this.encoding); 
				}
				index++;
			}
		}
		return index;
	}

	private boolean fromAllowedContainer(long objContainerId, int objContainerType) throws SQLException {
		if (!this.running) return false;
		HashMap<String, Container> containers = getContainers();
		//setup containers the first time
		if (this.containerSqls == null) {
			this.containerSqls = new HashMap<String, String>();
			for (String sqlkey : this.sqlStatements.keySet()) {
				if (sqlkey.startsWith("sql.container."))
					containerSqls.put(sqlkey, this.sqlStatements.get(sqlkey));
			}
			if (this.containerSqls.isEmpty()) {
				log.info("No sql.container.X statements. Cannot export container data.");
			}
			//get the allowed containers
			for (String sqlkey : this.containerSqls.keySet()) {
				if (sqlkey.startsWith("sql.container.filter.")) {
					String sql = this.containerSqls.get(sqlkey);
					ResultSet containerResults = sql(sql);
					while (containerResults.next()) {
						long id = containerResults.getLong(1);
						int containertype = Integer.parseInt(sqlkey.replaceAll("^sql.container.filter.", ""));
						String containername = null;
						String description = null;
						String nameSql = this.containerSqls.get("sql.container.name."+containertype);
						nameSql = nameSql.replaceAll("\\?", "'"+id + "'");
						ResultSet nameResults = sql(nameSql);
						while (nameResults.next()) {
							containername = nameResults.getString("name");
							description = nameResults.getString("description");
						} 
						String typename = null;
						String typenameSql = this.containerSqls.get("sql.container.typename");
						typenameSql = typenameSql.replaceAll("\\?", "'"+containertype + "'");
						ResultSet typeResults = sql(typenameSql);
						while (typeResults.next()) {
							typename = typeResults.getString("code");
						}
						
						long parentid = -1;
						int parenttype = -1;
						String parentSql = this.containerSqls.get("sql.container.parent."+containertype);
						parentSql = parentSql.replaceAll("\\?", "'"+id + "'");
						ResultSet parentResults = sql(parentSql);
						while (parentResults.next()) {
							parentid = parentResults.getLong("id");
							parenttype = parentResults.getInt("objtype");
							break;
						}
						Container container = new Container(id, containertype, 
								containername, description,
								typename, parentid, parenttype);
						this.containers.put(getUniqueContainerId(id, containertype), container);
						assignHierarchyRelationships(getUniqueContainerId(parentid, parenttype), getUniqueContainerId(id, containertype));
					}
				}
			}
		}
		if (this.containers.isEmpty()) return true; //allow all objects regardless of container
		//check to see if the passed container data matches any of the allowed containers
		return this.containers.containsKey(getUniqueContainerId(objContainerId, objContainerType));
	}

	private void assignHierarchyRelationships(String parent, String child) {
		if (!this.running) return;
		if (parent == null || child == null) return;
		if (this.hierarchy == null) {
			this.hierarchy = new Hierarchy(parent);
			hierarchy.add(child);
			return;
		}
		Hierarchy tmp = assignHierarchyRelationship(parent, child, this.hierarchy);
		//we didn't find any place to attach, and the current root is not the root community
		if (tmp == null && !this.hierarchy.id.equals("1-14")) { 
			Hierarchy root = new Hierarchy("1-14"); //root hierarchy is id 1, type 14
			Hierarchy current = this.hierarchy;
			root.add(current);
			Hierarchy newparent = new Hierarchy(parent);
			newparent.add(child);
			root.add(newparent);
			this.hierarchy = root;
		} //we didn't find any place to attach, and the current root _is_ the root community
		else if (tmp == null) {
			Hierarchy newparent = new Hierarchy(parent);
			newparent.add(child);
			this.hierarchy.add(newparent);
		}
		else { //we found someplace to attach
			this.hierarchy = tmp;
		}
	}

	private Hierarchy assignHierarchyRelationship(String parent, String child,
			Hierarchy hierarchy) {
		if (parent.equals(hierarchy.id)) { //this hierarchy has the same unique parent id, assign child
			Hierarchy foundChild = allchildren.get(child); //did we ever see this child?
			if (foundChild != null) { //yes, child already exists
				hierarchy.add(foundChild); //assign the existing child to this hierarchy
			} else { //no, the child does not already exist
				hierarchy.add(child); //add as a new child to this hierarchy
			}
			return hierarchy;
		}
		else if (child.equals(hierarchy.id)) { //current hierarchy has child unique id, create parent
			Hierarchy pHierarchy = new Hierarchy(parent);
			pHierarchy.add(hierarchy);
			return pHierarchy;
		}
		else { //check the rest of the hierarchy
			Hierarchy foundParent = allchildren.get(parent); //did we ever see this parent?
			if (foundParent != null) { //yes, parent already exists
				Hierarchy foundChild = allchildren.get(child); //did we ever see this child?
				if (foundChild != null) {  //yes, child already exists
					hierarchy.removeChild(foundChild.id); //reassign child to parent
					foundParent.add(foundChild);
				} else { //or no child does not already exist
					assignHierarchyRelationship(parent, child, foundParent); //recursively assign child to parent
				}
				return hierarchy;
			}
		}
		return null; //no place to attach child to 
	}

	private String getUniqueContainerId(long id, int containertype) {
		return id + "-" + containertype;
	}

	private HashMap<String,Container> getContainers() {
		if (this.containers == null) {
			this.containers = new HashMap<String, JiveExporter.Container>();
		}
		return this.containers;
	}

	private void exportContainers() {
		if (!this.running) return;
		if (this.containers.isEmpty()) return;
		String output = "";
		Container container = null;
		for (String uniqueid : this.containers.keySet()) {
			container = this.containers.get(uniqueid);
			output += ((this.debug)?container.toTruncatedString():container.toString())+"\n";
		}
		output = container.getHeader() + "\n" + output;
		if (this.containerOutFile == null) return;
		writeFile(this.containerOutFile, output, this.encoding); 
	}

	protected Container getContainer(String id) {
		return this.containers.get(id);
	}
	
	private void exportHierarchy() {
		if (!this.running) return;
		if (this.containers.isEmpty()) return;
		String output = this.hierarchy + "\n";
		if (this.hierarchyOutFile == null) return;
		writeFile(this.hierarchyOutFile, output, this.encoding); 
	}

	private String getBodyText(long id) throws SQLException {
		if (this.sqlStatements.containsKey("sql.doc.body")) {
			String sqlStatement = this.sqlStatements.get("sql.doc.body");
			sqlStatement = sqlStatement.replaceAll("\\?", id + "");
			ResultSet bodyResults = sql(sqlStatement);
			while (bodyResults.next()) {
				return bodyResults.getString("bodyText");
			}
		}
		return null;
	}

	private String getUsername(long userid) throws SQLException {
		if (this.sqlStatements.containsKey("sql.doc.user")) {
			String sqlStatement = this.sqlStatements.get("sql.doc.user");
			sqlStatement = sqlStatement.replaceAll("\\?", userid+"");
			ResultSet userResults = sql(sqlStatement);
			while (userResults.next()) {
				return userResults.getString("username");
			}
		}
		return userid+"";
	}
	

	private String getTags(long id, int type) throws SQLException {
		String tags = null;
		if (this.sqlStatements.containsKey("sql.tag")) {
			String sqlStatement = this.sqlStatements.get("sql.tag");
			sqlStatement = sqlStatement.replaceAll("@objid", "'" + id + "'");
			sqlStatement = sqlStatement.replaceAll("@objtype", "'" + type + "'");
			ResultSet tagResults = sql(sqlStatement);
			while (tagResults.next()) {
				if (tags == null) tags = "";
				else tags += ", ";
				String tag = tagResults.getString("tagName");
				if (this.debug) tag = tag.substring(0, 1);
				tags += tag;
			}
		}
		return tags;
	}


	private String createContainerDir(String expdirpath, int containerType,
			long containerId) {
		String dirname = containerType + "-" + containerId;
		File expdir = new File(expdirpath); 
		assert (expdir.exists());
		String testContainerPath = expdir.getPath() + File.separator + dirname;
		File testContainer = new File (testContainerPath);
		if (!testContainer.exists()) {
			testContainer.mkdir();
		}
		assert (testContainer.exists());
		return dirname + File.separator;
	}

	protected String createFilename(long internalDocId, int versionId,
			String contentType, String title) {
		String shorttitle = title.replaceAll("\\s", "_");
		shorttitle = shorttitle.replaceAll("\\W", "");
		if (shorttitle.length() > 20)
			shorttitle = shorttitle.substring(0, 20);
		return contentType + "-" + internalDocId + "-" + shorttitle + "-" + versionId + ".txt";
	}

	private String getExportPath(String outdir) {
		if (!outdir.endsWith(File.separator)) outdir += File.separator;
		outdir += EXPORT_DIR_NAME + File.separator;
		return outdir;
	}

	protected String createDocContent(long internalDocId, int versionId,
			String contentType, String title, int containerType, long containerId, String username,
			long creationDate, long modificationDate, String summary,
			String bodyText, String tags) {
		String body = (summary != null)?summary:((bodyText!=null)?bodyText:"");
		String usercontainername = null;
		return createPageContent(internalDocId, versionId, contentType, title, containerType, containerId,
				username, usercontainername, creationDate, modificationDate, body, tags);
	}
	

	private String createCommentContent(long commentid, int versionId,
			String contentType, String title, 
			long parentCommentId, int objtype, long objid, String username,
			Object object, long creationDate, long modDate, String body) {
		return "{jive-export-meta:" +
		"id=" + commentid +
		"|version=" + versionId +
		"|type=" + contentType +
		"|commentparent=" + parentCommentId + //comment id of any parent or 0
		"|referrertype=" + objtype + //38 or 102
		"|referrerid=" + objid + //blogpostid or docid
		"}\n"+
		"{user:" + username + "}\n" +
		"{timestamp:" +modDate + "}\n"+
		((body!=null)?body:"");
	}

	protected String createPageContent(long internalDocId, int versionId,
			String contentType, String title, int containerType, long containerId, String username,
			String usercontainername, long creationDate, long modificationDate, 
			String bodyText, String tags) {
		return "{jive-export-meta:" +
				"id=" + internalDocId +
				"|version=" + versionId +
				"|type=" + contentType +
				"|containertype=" + containerType +
				"|containerid=" + containerId +
				((usercontainername != null)?"|usercontainername="+usercontainername:"")+
				"}\n"+
				"{user:" + username + "}\n" +
				"{timestamp:" +modificationDate + "}\n" +
				((tags != null)?"{tags: " + tags + "}\n":"") +
				"{{title: " + title + " }}\n" +
				((bodyText!=null)?bodyText:"");
	}
	
	/* Blog Methods */
	
	private void exportBlogposts() {
		if (!this.running) return;
		log.debug("Exporting Blogs");
		if (this.sqlStatements.containsKey("sql.blog")) {
			int commentIndex = 0;
			int attachmentIndex = 0;
			String sqlStatement = this.sqlStatements.get("sql.blog");
			try {
				ResultSet blogResults = sql(sqlStatement);
				//get the count data
				blogResults.last();
				int maxBlogs = blogResults.getRow();
				blogResults.beforeFirst();
				if (maxBlogs > 0) testString += "Count: " + maxBlogs + "\n";
				// go through the results for each page version
				int index = 0;
				String lastfile = "";
				Vector<Long> noResultsIds = new Vector<Long>();
				while (blogResults.next()) {
					if (!this.running) return;
					long blogpostId = blogResults.getLong("blogpostID");
					long blogId = blogResults.getLong("blogID");
					int containerType = blogResults.getInt("containerType");
					long containerId = blogResults.getLong("containerId");
					String usercontainername = (containerType == 2020)?getUserContainerName(containerId):null;
					
					if (!fromAllowedContainer(containerId, containerType)) {
						maxBlogs--;
						continue;
					}
					
					long userid = blogResults.getLong("userId");
					String username = ((userid > 0)?getUsername(userid):userid+"");
					String title = blogResults.getString("subject");
					String body = blogResults.getString("body");
					long creationDate = blogResults.getLong("creationDate");
					long modificationDate = blogResults.getLong("modificationDate");
					int versionId = 1;
					String tags = getTags(blogpostId, BLOG_JIVECONSTANT);

					if (this.debug && tags != null) {
						testString += blogpostId + ": " + tags + "\n";
					}
					else if (this.debug) {
						testString += "|" + blogpostId + "|" + blogId + "|" + 
						containerType + "|" + containerId + "|" +
						(usercontainername==null?"":usercontainername.substring(0,2)) + "|" + 
						username.substring(0,2) + "|" +
						title.substring(0,1) + "|" + ((body != null)?"BODY":"") + "|\n" ;
					}
					else {
						String contentType = "BLOG";
						String expdir = getExportPath(this.outdir);
						String containerdir = createContainerDir(expdir, containerType, containerId);
						String filename = createFilename(blogpostId, versionId, contentType, title);
						String filepath = expdir + containerdir + filename;
						String content = createPageContent(blogpostId, versionId, contentType, title,
								containerType, containerId,
								username, usercontainername, 
								creationDate, modificationDate, body, tags);
						lastfile = filepath;
						writeFile(filepath, content, this.encoding); 
						
						//export title data (we'll need this in a seperate map file for links and attachments to other pages)
						title = getDate(modificationDate) + title;
						exportTitles(blogpostId, BLOG_JIVECONSTANT, containerId, containerType, versionId, title);
					}
					//export comments
					commentIndex += exportComments(blogpostId,
							containerType, containerId, "blog");
					
					attachmentIndex += exportAttachments(blogpostId, BLOG_JIVECONSTANT);
					
					//do some logging
					index++;
					if ((index % 100) == 0) {
						double percent = Math.round(100*(((double) index)/maxBlogs));
						log.debug("Exporting " + index + " Blogs. " + percent + "%");
					}
				}
				log.info("Exporting " + index + " Blogs. 100.0%");
				log.info("Exported " + commentIndex + " comments for blogs.");
				log.info("Exported data for " + attachmentIndex + " attachments.");
				log.debug("Lastfile = " + lastfile);
				if (this.debug) {
					testString += "Final Count: " + index;
					writeFile(this.debugOutFile, testString, this.encoding);
				}
			} catch (SQLException e) {
				log.error("Problem getting blogs: " + sqlStatement, e);
				return;
			}
		}
	}
	
	private String getDate(long modificationDate) {
		String timestamp = modificationDate + "";
		Date epoch = new Date(Long.parseLong(timestamp));
		DateFormat format = new SimpleDateFormat("/yyyy/MM/dd/");
		return format.format(epoch);
	}

	private void exportTitles(long objid, int objtype,
			long containerId, int containerType, int versionId, String title) {
		String titleData = objid + OUTPUT_INNERDELIM + 
			objtype + OUTPUT_INNERDELIM +
			containerId + OUTPUT_INNERDELIM +
			containerType + OUTPUT_INNERDELIM + 
			versionId + OUTPUT_INNERDELIM + 
			title + "\n";
	
		//write title data
		if (!hasWrittenTitleData) {
			titleData = "objid\tobjtype\tcontainerid\tcontainertype\tversion\ttitle\n" +
			titleData;
			writeFile(this.titleOutFile, titleData, this.encoding);
			hasWrittenTitleData = true;
		}
		else {
			appendToFile(this.titleOutFile, titleData, this.encoding);
		}

	}

	protected String getUserContainerName(long containerId) throws SQLException {
		if (this.sqlStatements.containsKey("sql.blog.usercontainer")) {
			String sqlStatement = this.sqlStatements.get("sql.blog.usercontainer");
			sqlStatement = sqlStatement.replaceAll("\\?", "'" + containerId+"'");
			ResultSet userResults = sql(sqlStatement);
			while (userResults.next()) {
				return userResults.getString("username");
			}
		}
		return null;
	}

	private void testSql(String sqlStatement) {
		try {
			ResultSet result = sql(sqlStatement);
			while (result.next()) {
				String row = "";
				String delim = "|";
				for (int i = 1; i <= 16; i++) row += result.getString(i) + delim;
				writeFile(this.debugOutFile, row, this.encoding);
				
			}
		} catch (SQLException e) {
			log.error("Problem running sql: " + sqlStatement, e);
		}
	}
	
	private class Container {
		private long id;
		private int type;
		private String name;
		private String description;
		private String typename;
		private long parentid;
		private long parenttype;
		private String delim = OUTPUT_INNERDELIM;
		
		public Container(long id, int type, String name, String description,
				String typename, long parentid, long parenttype) {
			this.id = id;
			this.type = type;
			this.name = name;
			this.description = description;
			this.typename = typename;
			this.parentid = parentid;
			this.parenttype = parenttype;
		}
		
		public String toString() {
			return id + delim + 
				type + delim + 
				typename + delim +
				name + delim + 
				getDescription() + delim + 
				parentid + delim + 
				parenttype;
		}
		
		public String getHeader() {
			return "container id" + delim + 
			"container type" + delim + 
			"type name" + delim + 
			"container name" + delim + 
			"container description" + delim + 
			"parent id" + delim + 
			"parent type";
		}
		
		public String toTruncatedString() {
			String description = getDescription();
			String truncname = (name == null)?"":(name.length()<2?name:name.substring(0,2));
			String truncdesc = description.length()<2?description:description.substring(0,2);
			return id + delim + 
				type + delim + 
				typename + delim +
				truncname + delim + 
				truncdesc + delim + 
				parentid + delim + 
				parenttype;
		}
		
		public String toHierarchyString() {
			String truncname = (name == null)?"":(name.length()<1?name:name.substring(0,1));
			String typestring = "";
			switch (type) {
			case 14: typestring = "space"; break;
			case 600: typestring = "project"; break;
			case 700: typestring = "socialgroup"; break;
			case 2020: typestring = "usercontainer"; break;
			}
			return "Jive " + typestring + " '" + (debug?truncname:name) + "'";
		}
		
		public String getDescription() {
			if (description == null) return "";
			return description.replaceAll("[\r\n]", " ");//confluence will not allow newlines and they're inconvenient
		}
		
	}

	private class Hierarchy {
		public String id; //from getUniqueContainerId
		private TreeMap<String, Hierarchy> children;
		public Hierarchy(String id) {
			this.id = id;
		}
		public TreeMap<String,Hierarchy> getChildren() {
			if (children == null) children = new TreeMap<String, JiveExporter.Hierarchy>();
			return children;
		}
		public void add(String child) {
			getChildren();
			Hierarchy childHier = new Hierarchy(child);
			children.put(child, childHier);
			allchildren.put(child, childHier);
		}
		public void add(Hierarchy child) {
			getChildren();
			children.put(child.id, child);
			allchildren.put(child.id, child);
		}
		public Hierarchy removeChild(String child) {
			return children.remove(child);
		}
		public String toString() {
			return toString("*");
		}
		public String toString(String delim) {
			String s = "";
			if (getContainer(id) == null) {
				s = delim + " Jive root level containers (id:" + id +")\n";
			}
			else 
				s = delim + " " + getContainer(id).toHierarchyString() + " -> " +
						getSpacekey(id) + "\n"; 
			for (String childStr : getChildren().keySet()) {
				Hierarchy child = getChildren().get(childStr);
				s += child.toString(delim+delim.substring(0,1)); 
			}
			return s;
		}
		
		private String getSpacekey(String id) {
			return id; //FIXME should be using a map
		}
	}
}
