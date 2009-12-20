package com.atlassian.uwc.exporters;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;

public class SMFExporter extends SQLExporter {
	
	private static final String DEFAULT_UNDERSCORE_CHARS = " ";
	public static final String OUTDIR = "smf-export";
	public static final String DEFAULT_ENCODING = "utf-8";
//	private static final String PROPKEY_REMOVE = "attachment-chars-remove";
//	private static final String PROPKEY_TOUNDERSCORE = "attachment-chars-to-underscore";
		

	/* user configured properties */
	private Map properties;
	private String encoding;
	
	public void export(Map properties) throws ClassNotFoundException, SQLException {
		//setup
		this.running = true;
		PropertyConfigurator.configure("log4j.properties");

		//export
		log.info("Exporting SMF...");
		setProperties(properties);
		connectToDB();
		exportData();
		closeDB();

		//cleanup
		if (this.running) log.info("Export Complete.");
		this.running = false;
		
	}

	protected void start() { //used by junit
		this.running = true;
	}

	protected void setProperties(Map properties) {
		this.properties = properties;
	}
	
	protected Map getProperties() { //used by junit
		return this.properties;
	}
	
	/* Db Props */
	private static final String PROPKEY_DRIVER = "jdbc.driver.class";
	private static final String PROPKEY_URL = "db.url";
	private static final String PROPKEY_NAME= "db.name";
	private static final String PROPKEY_LOGIN = "db.user";
	private static final String PROPKEY_PASS = "db.pass";
	
	protected void connectToDB() throws ClassNotFoundException, SQLException {
		if (!this.running) return;
		String driver = (String) this.properties.get(PROPKEY_DRIVER);
		String url = (String) this.properties.get(PROPKEY_URL);
		String name = (String) this.properties.get(PROPKEY_NAME);
		String login = (String) this.properties.get(PROPKEY_LOGIN);
		String pass = (String) this.properties.get(PROPKEY_PASS);
		
		connectToDB(driver, url, name, login, pass);
	}

	protected void exportData() {
		if (!this.running) return;
		clearAttachments();
		
		//get the data from the db
		setEncoding();
		List<Category> categories = exportCategories();
		if (categories == null) return;
		List<Board> boards = exportBoards();	
		if (boards == null) return;
		List<Message> messages = exportMessages();
		exportAttachments();
		
		//create files
		List<Data> data = createOutData(categories, boards, messages);
		outputData(data);
	}

	protected void setEncoding() {
		String encoding = (String) this.properties.get("db.encoding");
		if (encoding != null && !"".equals(encoding)) this.encoding = encoding;
		else encoding = DEFAULT_ENCODING;
	}
	
	protected List<Data> createOutData(List<Category> categories, List<Board> boards, List<Message> messages) {
		Vector<Data> alldata = new Vector<Data>();
		HashMap<String,String> board2Categories = new HashMap<String,String> ();
		for (Iterator iter = categories.iterator(); iter.hasNext();) {
			Category cat = (Category) iter.next();
			Data data = new Data();
			data.id = cat.id;
			data.type = Type.CATEGORY;
			data.title = cat.name;
			alldata.add(data);
		}
		int boardStartIndex = alldata.size();
		for (Iterator iter = boards.iterator(); iter.hasNext();) {
			Board board = (Board) iter.next();
			Data data = new Data();
			data.id = board.id;
			data.type = Type.BOARD;
			data.title = board.name;
			data.content = board.description;
			data.parentid = (board.parenttype == Type.CATEGORY)?board.category:board.parent;
			data.parenttype = board.parenttype;
			alldata.add(data);
			board2Categories.put(data.type+data.id, data.parenttype+data.parentid);
		}
		int boardEndIndex = alldata.size();
		for (int i = boardStartIndex; i < boardEndIndex; i++) {
			Data boardData = alldata.get(i);
			if (Type.CATEGORY == boardData.parenttype) 
				boardData.ancestors = boardData.parenttype + boardData.parentid;
			else
				boardData.ancestors = getBoardAncestors(board2Categories, boardData.parentid);
		}
		for (Iterator iter = messages.iterator(); iter.hasNext();) {
			Message msg = (Message) iter.next();
			Data data = new Data();
			data.id = msg.id;
			data.type = msg.isfirst ? Type.TOPIC : Type.REPLY;
			data.title = msg.title;
			data.content = msg.content;
			data.parentid = msg.isfirst ? msg.board : msg.topic;
			data.parenttype = msg.isfirst ? Type.BOARD : Type.TOPIC;
			data.ancestors = getMessageAncestors(board2Categories, msg);
			data.time = msg.time;
			data.userid = msg.userid;
			data.username = msg.username;
			data.useremail = msg.useremail;
			data.attachmentdelim = getAttachmentDelim(msg.id, data.ATTACH_DELIM);
			data.attachments = getAttachmentList(msg.id, data.attachmentdelim);
			data.attachmentnames = getAttachmentNames(msg.id, data.attachmentdelim);
			alldata.add(data);
		}
		return alldata;
	}

	private String getMessageAncestors(HashMap<String, String> parentRelationships, Message msg) {
		String category, boards, messages;
		Vector<String> ancestors = getAllAncestors(parentRelationships, msg.board);
		boards = category = "";
		int index = 0;
		for (Iterator iter = ancestors.iterator(); iter.hasNext();) {
			String id = (String) iter.next();
			if (id.startsWith(Type.CATEGORY+"")) category = id + ":" + category;
			else boards = id + ":" + boards; //vector order is backwards
		}
		messages = getAncestorMessages(msg);
		String messageAncestors = category + boards + messages;
		//cleanup (mostly for easier testing)
		messageAncestors = messageAncestors.replaceAll("::+", ":");
		messageAncestors = messageAncestors.replaceFirst(":$", "");
		return messageAncestors;
	}

	private String getBoardAncestors(HashMap<String, String> parentRelationships, String parentid) {
		String category, boards;
		Vector<String> ancestors = getAllAncestors(parentRelationships, parentid);
		boards = category = "";
		int index = 0;
		for (Iterator iter = ancestors.iterator(); iter.hasNext();) {
			String id = (String) iter.next();
			if (id.startsWith(Type.CATEGORY+"")) category = id + ":" + category;
			else boards = id + ":" + boards; //vector order is backwards
		}
		String boardAncestors = category + boards;
		//cleanup (mostly for easier testing)
		boardAncestors = boardAncestors.replaceAll("::+", ":");
		boardAncestors = boardAncestors.replaceFirst(":$", "");
		return boardAncestors;
	}

	
	protected Vector<String> getAllAncestors(HashMap<String, String> parentRelationships, String boardid) {
		Vector<String> all = new Vector<String>();
		String currentParent = Type.BOARD + boardid;
		all.add(currentParent);
		String nextParent = parentRelationships.get(currentParent);
		if (nextParent != null) all.add(nextParent);
		int pressurevalve = 100;
		while (nextParent != null && !nextParent.startsWith(Type.CATEGORY + "")) {
			currentParent = nextParent;
			nextParent = parentRelationships.get(currentParent);
			if (nextParent == null || "".equals(nextParent) || pressurevalve-- < 0) {
				log.warn("Couldn't find parent category for: " + currentParent +
						". You will need to manually fix it's hierarchy.");
			}
			else all.add(nextParent);
		}
		return all;
	}

	private String getAncestorMessages(Message msg) {
		return (msg.isfirst ? "" : ":" + Type.TOPIC + msg.firstid);
	}

	protected String getAttachmentDelim(String id, String currentdelim) {
		HashMap<String, Vector<Attachment>> attachments = getAttachments();
		Vector<Attachment> messageAttachments = attachments.containsKey(id)?attachments.get(id):new Vector<Attachment>();
		String allatt = "";
		for (int i = 0; i < messageAttachments.size(); i++) {
			Attachment attachment = (Attachment) messageAttachments.get(i);
			allatt += attachment.name;
		}
		int pressurevalue = 100;
		String current = currentdelim;
		String next = currentdelim;
		while (Pattern.compile("["+next+"]").matcher(allatt).find()) {
			next = getNextDelimCandidate(current); 
			if (next == null || pressurevalue-- < 0) {
				log.error("Problem finding suitable attachment delimiter for attachments " + allatt);
				break;
			}
			current += next;
		}
		return next;
	}
	
	String delimCandidates = ",;:?`";
	private String getNextDelimCandidate(String current) {
		String left = delimCandidates.replaceAll("[" + current + "]", "");
		if ("".equals(left)) return null;
		return left.charAt(0) + "";
	}

	private static HashMap<String,Vector<Attachment>> attachments = null;
	protected String getAttachmentList(String id, String delim) {
		HashMap<String, Vector<Attachment>> attachments = getAttachments();
		boolean isHashSql = isSqlHash();
		Vector<Attachment> messageAttachments = attachments.containsKey(id)?attachments.get(id):new Vector<Attachment>();
		String current = "";
		for (int i = 0; i < messageAttachments.size(); i++) {
			Attachment att = messageAttachments.get(i);
			if (i > 0) current += delim;
			if (isHashSql) current += getSqlHashFilename(att);
			else current += getMd5HashFilename(att);
		}
		return current;
	}

	private Boolean isSqlHash = null;
	private boolean isSqlHash() {
		if (this.isSqlHash == null)
			this.isSqlHash = Boolean.parseBoolean((String) this.properties.get("db.att.hashInSQL"));
		return this.isSqlHash;
	}
	protected String getAttachmentNames(String id, String delim) {
		HashMap<String, Vector<Attachment>> attachments = getAttachments();
		boolean isHashSql = isSqlHash();
		Vector<Attachment> messageAttachments = attachments.containsKey(id)?attachments.get(id):new Vector<Attachment>();
		String current = "";
		for (int i = 0; i < messageAttachments.size(); i++) {
			Attachment att = messageAttachments.get(i);
			if (i > 0) current += delim;
			current += getRealname(att);
		}
		return current;
	}
	
	protected void saveAttachmentWithId(Attachment attachment) {
		HashMap attachments = getAttachments();
		Vector<Attachment> current = attachments.containsKey(attachment.message)?
				(Vector<Attachment>) attachments.get(attachment.message):
				new Vector<Attachment>();
		current.add(attachment);
		attachments.put(attachment.message, current);

	}

	protected String getSqlHashFilename(Attachment attachment) {
		return attachment.id + "_" + attachment.hash;
	}
	
		protected String getMd5HashFilename(Attachment attachment) {
			//SEE: SMF's Sources/Subs.php getAttachmentFilename line 3457
			//whitespace to _
			String cleanName = attachment.name.replaceAll("\\s", "_");
			//get rid of everything not word, dot, dash
			cleanName = cleanName.replaceAll("[^\\w_.-]", ""); 
			return  attachment.id + "_" + cleanName.replaceAll("\\.", "_") + getMd5(cleanName);
		}
	
//	private String toUnderscore(String att) {
//		String toUnderscoreChars = getToUnderscoreChars();
//		if (toUnderscoreChars != null && !"".equals(toUnderscoreChars)) {
//			for (int j = 0; j < toUnderscoreChars.length(); j++) {
//				String c = Character.toString(toUnderscoreChars.charAt(j));
//				//can't use a char class. how do we know what to escape?
//				att = att.replaceAll("\\Q"+c+"\\E", "_"); 
//			}
//		}
//		return att;
//	}
//
//	protected String removeChars(String att) {
//		String removeChars = getRemoveChars();
//		if (removeChars != null && !"".equals(removeChars)) {
//			for (int j = 0; j < removeChars.length(); j++) {
//				String c = Character.toString(removeChars.charAt(j));
//				//can't use a char class. how do we know what to escape?
//				att = att.replaceAll("\\Q"+c+"\\E", ""); 
//			}
//		}
//		return att;
//	}

//	private String getRemoveChars() {
//		if (properties.containsKey(PROPKEY_REMOVE))
//			return (String) properties.get(PROPKEY_REMOVE);
//		return null;
//	}
//
//	private String getToUnderscoreChars() {
//		if (properties.containsKey(PROPKEY_TOUNDERSCORE))
//			return (String) properties.get(PROPKEY_TOUNDERSCORE);
//		return DEFAULT_UNDERSCORE_CHARS;
//	}

	Pattern thumb = Pattern.compile("(\\.[^_]+)_thumb$");
	protected String getRealname(Attachment attachment) {
		Matcher thumbFinder = thumb.matcher(attachment.name);
		if (thumbFinder.find()) {
			String ext = thumbFinder.group(1);
			return attachment.id + "_" + attachment.name + ext;
		}
		return attachment.id + "_" + attachment.name;
	}
	
	protected String getMd5(String input) {
		String actual;
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			log.error("Could not create Message Digest for MD5.");
			e.printStackTrace();
			return "";
		}
		byte[] buffer = input.getBytes();
		md5.update(buffer);
		actual = new BigInteger(1,md5.digest()).toString(16);
		int start = actual.length();
		//md5s should be padded to 32 chars in length
		for (int i = start; i < 32; i++) actual = '0' + actual;
		return actual;
	}
	
	private HashMap<String, Vector<Attachment>> getAttachments() {
		if (attachments == null)
			attachments = new HashMap<String, Vector<Attachment>>();
		return attachments;
	}
	public void clearAttachments() {
		attachments = null;
	}


	protected void outputData(List<Data> data) {
		File out = new File(getOutdirProp());
		if (!out.exists()) out.mkdir();
		if (!out.exists()) {
			String error = "Cannot create output directory: " + getOutdirProp();
			log.error(error);
			throw new IllegalArgumentException(error);
		}
		
		for (Iterator iter = data.iterator(); iter.hasNext();) {
			Data datum = (Data) iter.next();
			//file with page contents
			String contentFilename = datum.getContentFilename();
			String contentBody = datum.getContentBody();
			writeFile(addParentDir(contentFilename), contentBody, encoding); 
			
			//file with metadata
			String metaFilename = datum.getMetaFilename();
			String metaBody = datum.getMetaBody();
			writeFile(addParentDir(metaFilename), metaBody, encoding); 
		}
	}
	private static final String PROPKEY_OUTDIR = "output.dir";
	protected String addParentDir(String filename) {
		String outdir = getOutdirProp();
		if (!filename.startsWith(File.separator)) outdir += File.separator;
		outdir += filename;
		return outdir;
	}

	private String getOutdirProp() {
		String outdir = (String) this.properties.get(PROPKEY_OUTDIR);
		if (!outdir.endsWith(File.separator)) outdir += File.separator;
		outdir += OUTDIR;
		return outdir;
	}

	private static final String PROPKEY_CAT_ID = "db.col.cat.id";
	private static final String PROPKEY_CAT_NAME = "db.col.cat.name";
	private static final String PROPKEY_CAT_TABLE = "db.table.cat";
	private static final String PROPKEY_CAT_SQL = "db.sql.cat";
	protected List<Category> exportCategories() {
		if (!this.running) return null;
		
		//get user configured column names and sql
		String colId = (String) this.properties.get(PROPKEY_CAT_ID);
		String colName = (String) this.properties.get(PROPKEY_CAT_NAME);
		String table = (String) this.properties.get(PROPKEY_CAT_TABLE);
		String rawSql = (String) this.properties.get(PROPKEY_CAT_SQL);
		//update sql to reflect user configured column names
		String sql = rawSql.replaceAll("\\Q{" + PROPKEY_CAT_ID + "}\\E", colId);
		sql = sql.replaceAll("\\Q{" + PROPKEY_CAT_NAME + "}\\E", colName);
		sql = sql.replaceAll("\\Q{" + PROPKEY_CAT_TABLE + "}\\E", table);
		
		Vector<Category> categories = new Vector<Category>();

		ResultSet data = null;
		try {
			data = sql(sql);
			while (data.next()) { //get each category
				Category cat = new Category();
				cat.id = data.getString(colId);
				cat.name = encode(data.getBytes(colName)); 
				categories.add(cat);
			}
		} catch (SQLException e) {
			log.error("Could not export Categories with sql: " + sql);
			e.printStackTrace();
			return null;
		}
		return categories;
	}

	private static final String PROPKEY_BOARD_ID = "db.col.board.id";
	private static final String PROPKEY_BOARD_CAT = "db.col.board.cat";
	private static final String PROPKEY_BOARD_LEVEL = "db.col.board.level";
	private static final String PROPKEY_BOARD_PARENT = "db.col.board.parent";
	private static final String PROPKEY_BOARD_PARENTTYPE = "db.col.board.parenttype";
	private static final String PROPKEY_BOARD_NAME = "db.col.board.name";
	private static final String PROPKEY_BOARD_DESC = "db.col.board.desc";
	private static final String PROPKEY_BOARD_TABLE = "db.table.board";
	private static final String PROPKEY_BOARD_SQL = "db.sql.board";
	protected List<Board> exportBoards() {
		if (!this.running) return null;
		
		//get user configured column names and sql
		String colId = (String) this.properties.get(PROPKEY_BOARD_ID);
		String colCat = (String) this.properties.get(PROPKEY_BOARD_CAT);
		String colLevel = (String) this.properties.get(PROPKEY_BOARD_LEVEL);
		String colParent = (String) this.properties.get(PROPKEY_BOARD_PARENT);
		String colParentType = (String) this.properties.get(PROPKEY_BOARD_PARENTTYPE);
		String colName = (String) this.properties.get(PROPKEY_BOARD_NAME);
		String colDesc = (String) this.properties.get(PROPKEY_BOARD_DESC);
		String table = (String) this.properties.get(PROPKEY_BOARD_TABLE);
		String rawSql = (String) this.properties.get(PROPKEY_BOARD_SQL);
		//update sql to reflect user configured column names
		String sql = rawSql.replaceAll("\\Q{" + PROPKEY_BOARD_ID + "}\\E", colId);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_CAT + "}\\E", colCat);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_LEVEL + "}\\E", colLevel);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_PARENT + "}\\E", colParent);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_PARENTTYPE + "}\\E", colParentType);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_NAME + "}\\E", colName);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_DESC + "}\\E", colDesc);
		sql = sql.replaceAll("\\Q{" + PROPKEY_BOARD_TABLE + "}\\E", table);
		
		Vector<Board> boards = new Vector<Board>();

		ResultSet data = null;
		try {
			data = sql(sql);
			while (data.next()) { //get each category
				Board board = new Board();
				board.id = data.getString(colId);
				board.category = data.getString(colCat);
				board.level = data.getString(colLevel);
				board.parent = data.getString(colParent);
				board.parenttype = Type.getType(data.getString(colParentType));
				board.name = encode(data.getBytes(colName)); 
				board.description = encode(data.getBytes(colDesc)); 
				boards.add(board);
			}
		} catch (SQLException e) {
			log.error("Could not export Boards with sql: " + sql);
			e.printStackTrace();
			return null;
		}
		return boards;
	}
	
	private static final String PROPKEY_MESSAGE_ID = "db.col.msg.id";
	private static final String PROPKEY_MESSAGE_TOPIC = "db.col.msg.topic";
	private static final String PROPKEY_MESSAGE_BOARD = "db.col.msg.board";
	private static final String PROPKEY_MESSAGE_USERID = "db.col.msg.userid";
	private static final String PROPKEY_MESSAGE_USERNAME = "db.col.msg.username";
	private static final String PROPKEY_MESSAGE_USEREMAIL = "db.col.msg.useremail";
	private static final String PROPKEY_MESSAGE_TIME = "db.col.msg.time";
	private static final String PROPKEY_MESSAGE_TITLE = "db.col.msg.title";
	private static final String PROPKEY_MESSAGE_CONTENT = "db.col.msg.content";
	private static final String PROPKEY_MESSAGE_ISFIRST = "db.col.msg.isfirst";
	private static final String PROPKEY_MESSAGE_FIRSTID = "db.col.msg.firstid";
	private static final String PROPKEY_MESSAGE_TABLE = "db.table.msg";
	private static final String PROPKEY_MESSAGE_SQL = "db.sql.messages";
	protected List<Message> exportMessages() {
		if (!this.running) return null;
		
		//get user configured column names and sql
		String colId = (String) this.properties.get(PROPKEY_MESSAGE_ID);
		String colTopic = (String) this.properties.get(PROPKEY_MESSAGE_TOPIC);
		String colBoard = (String) this.properties.get(PROPKEY_MESSAGE_BOARD);
		String colUserid = (String) this.properties.get(PROPKEY_MESSAGE_USERID);
		String colUsername = (String) this.properties.get(PROPKEY_MESSAGE_USERNAME);
		String colUseremail = (String) this.properties.get(PROPKEY_MESSAGE_USEREMAIL);
		String colTime = (String) this.properties.get(PROPKEY_MESSAGE_TIME);
		String colTitle = (String) this.properties.get(PROPKEY_MESSAGE_TITLE);
		String colContent = (String) this.properties.get(PROPKEY_MESSAGE_CONTENT);
		String colIsfirst = (String) this.properties.get(PROPKEY_MESSAGE_ISFIRST);
		String colFirstid = (String) this.properties.get(PROPKEY_MESSAGE_FIRSTID);
		String table = (String) this.properties.get(PROPKEY_MESSAGE_TABLE);
		String rawSql = (String) this.properties.get(PROPKEY_MESSAGE_SQL);
		//update sql to reflect user configured column names
		String sql = rawSql.replaceAll("\\Q{" + PROPKEY_MESSAGE_ID + "}\\E", colId);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_TOPIC + "}\\E", colTopic);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_BOARD + "}\\E", colBoard);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_USERID + "}\\E", colUserid);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_USERNAME + "}\\E", colUsername);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_USEREMAIL + "}\\E", colUseremail);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_TIME + "}\\E", colTime);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_TITLE + "}\\E", colTitle);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_CONTENT + "}\\E", colContent);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_ISFIRST + "}\\E", colIsfirst);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_FIRSTID + "}\\E", colFirstid);
		sql = sql.replaceAll("\\Q{" + PROPKEY_MESSAGE_TABLE + "}\\E", table);
		
		Vector<Message> messages = new Vector<Message>();

		ResultSet data = null;
		try {
			data = sql(sql);
			while (data.next()) { //get each category
				Message message = new Message();
				message.id = data.getString(colId);
				message.topic = data.getString(colTopic);
				message.board = data.getString(colBoard);
				message.userid = data.getString(colUserid);
				message.username = data.getString(colUsername); 
				message.useremail = data.getString(colUseremail); 
				message.time = data.getString(colTime); 
				message.title = encode(data.getBytes(colTitle));
				message.content= encode(data.getBytes(colContent));
				message.isfirst = data.getBoolean(colIsfirst);
				message.firstid = data.getString(colFirstid);
				messages.add(message);
			}
		} catch (SQLException e) {
			log.error("Could not export Messages with sql: " + sql);
			e.printStackTrace();
			return null;
		}
		return messages;
	}
	
	private static final String PROPKEY_ATTACHMENT_ID = "db.col.att.id";
	private static final String PROPKEY_ATTACHMENT_THUMB = "db.col.att.thumb";
	private static final String PROPKEY_ATTACHMENT_MESSAGE = "db.col.att.message";
	private static final String PROPKEY_ATTACHMENT_NAME = "db.col.att.name";
	private static final String PROPKEY_ATTACHMENT_HASH = "db.col.att.hash";
	private static final String PROPKEY_ATTACHMENT_TABLE = "db.table.att";
	private static final String PROPKEY_ATTACHMENT_SQL = "db.sql.att";
	public List<Attachment> exportAttachments() {
			if (!this.running) return null;
			
			//get user configured column names and sql
			String colId = (String) this.properties.get(PROPKEY_ATTACHMENT_ID);
			String colThumb = (String) this.properties.get(PROPKEY_ATTACHMENT_THUMB);
			String colMessage = (String) this.properties.get(PROPKEY_ATTACHMENT_MESSAGE);
			String colName = (String) this.properties.get(PROPKEY_ATTACHMENT_NAME);
			String colHash = (String) this.properties.get(PROPKEY_ATTACHMENT_HASH);
			String table = (String) this.properties.get(PROPKEY_ATTACHMENT_TABLE);
			String rawSql = (String) this.properties.get(PROPKEY_ATTACHMENT_SQL);
			//update sql to reflect user configured column names
			String sql = rawSql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_ID + "}\\E", colId);
			sql = sql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_THUMB + "}\\E", colThumb);
			sql = sql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_MESSAGE + "}\\E", colMessage);
			sql = sql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_NAME + "}\\E", colName);
			sql = sql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_HASH + "}\\E", colHash);
			sql = sql.replaceAll("\\Q{" + PROPKEY_ATTACHMENT_TABLE + "}\\E", table);
			
			Vector<Attachment> attachments = new Vector<Attachment>();

			ResultSet data = null;
			try {
				data = sql(sql);
				while (data.next()) { //get each category
					Attachment attachment = new Attachment();
					attachment.id = data.getString(colId);
					attachment.thumb = data.getString(colThumb);
					attachment.message = data.getString(colMessage);
					attachment.name = encode(data.getBytes(colName)); 
					attachment.hash = data.getString(colHash); 
					saveAttachmentWithId(attachment);
					attachments.add(attachment);
				}
			} catch (SQLException e) {
				log.error("Could not export Attachments with sql: " + sql);
				e.printStackTrace();
				return null;
			}
			return attachments;
	}
	
	boolean first = true;
	public String encode(byte[] bytes) {
		try {
			return new String (bytes, encoding);
		} catch (UnsupportedEncodingException e) {
			//we only emit this warning once, but it could happen a lot.
			if (first) {
				log.warn("Could not character encode with this encoding: " + encoding);
				e.printStackTrace();
			}
			first = false;
		}
		return new String(bytes);
	}

	
	/* Data Objects */
	
	public class Category {
		public String id;
		public String name;
	}
	
	public class Board {
		public String id;
		public String category; // cat id
		public String level;	// child level
		public String parent;	// parent id
		public Type parenttype; // CATEGORY or BOARD
		public String name;
		public String description;
	}

	public class Message {
		public String id;
		public String topic;
		public String board;
		public String userid;
		public String username;
		public String useremail;
		public String time;
		public String title;
		public String content;
		public boolean isfirst;
		public String firstid;
	}
	
	public enum Type {
		CATEGORY,
		BOARD,
		TOPIC,
		REPLY;
		public String toString() {
			switch(this) {
			case CATEGORY: return "cat";
			case BOARD: return "brd";
			case TOPIC: return "top";
			case REPLY: return "re";
			default: return null;
			}
		};
		public static Type getType(String type) {
			if (type.startsWith("c")) return CATEGORY;
			if (type.startsWith("b")) return BOARD;
			if (type.startsWith("t")) return TOPIC;
			if (type.startsWith("r")) return REPLY;
			return null;
		}
	}
	
	public class Attachment {
		public String id; /* attachment id */
		public String thumb; /* id of thumbnail image */
		public String message; /* id of associated message */
		public String name; /* original file name */
		public String hash; /* hash used to locate file */
		public Attachment(String id, String thumb, String message, String name, String hash) {
			this.id = id;
			this.thumb = thumb;
			this.message = message;
			this.name = name;
			this.hash = hash;
		}
		public Attachment() {
		}
	}

	public class Data {
		private static final String FILENAME_DELIM = "_";
		private static final String CONTENT_FILETYPE = ".txt";
		private static final String META_FILETYPE = ".meta";
		public static final String ATTACH_DELIM = ","; //default attachment delimiter
		public String id;
		public String title;
		public Type type;
		public String parentid;
		public Type parenttype;
		public String ancestors;
		public String time;
		public String useremail;
		public String username;
		public String userid;
		public String content;
		public String attachments; /* delimited list of attachment names */
		public String attachmentnames;
		public String attachmentdelim = ATTACH_DELIM; /*custom delimiter - default is comma */
		
		public String getContentFilename() {
			if (title == null || id == null || type == null)
				throw new IllegalStateException("Cannot get filename if Data.title, .id or .type is null.");
			String condensed = condenseTitle(title);
			return condensed + FILENAME_DELIM + type.toString() + id + CONTENT_FILETYPE;
		}
		protected String condenseTitle(String input) {
			return input.replaceAll("\\W", "");
		}
		public String getContentBody() {
			if (content == null) return "";
			return content;
		}
		public String getMetaFilename() {
			if (title == null || id == null || type == null)
				throw new IllegalStateException("Cannot get filename if Data.title, .id or .type is null.");
			String condensed = condenseTitle(title);
			return condensed + FILENAME_DELIM + type.toString() + id + META_FILETYPE;
		}
		public String getMetaBody() {
			String meta = "";
			meta += "id=" + id + "\n" +
					"type=" + type + "\n" +
					"title=" + title + "\n" +
					"parentid=" + parentid + "\n" +
					"parenttype=" + parenttype + "\n" +
					"ancestors=" + ancestors + "\n" +
					"time=" + time + "\n" +
					"userid=" + userid + "\n" +
					"username=" + username + "\n" +
					"useremail=" + useremail + "\n" +
					"attachments.location=" + attachments + "\n" +
					"attachments.name=" + attachmentnames + "\n" +
					"attachments.delim=" + attachmentdelim + "\n";
			return meta;
		}
	}
}
