package com.atlassian.uwc.converters.jive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.jive.SpaceConverter.ContainerInfo;
import com.atlassian.uwc.ui.Page;

public class AttachmentConverter extends BaseConverter {

	private static final String JIVE_ATT_FILETYPE = ".bin";
	private static final String JIVE_CONSTANT = "45";
	protected static HashMap<String, Vector<Data>> attachmentdata; // key is docid, value is list of attachment data
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Looking for attachments.");
		initAttachmentData();
		String docid = getDocId(page.getOriginalText());
		log.debug("docid = " + docid);
		if (docid != null)
			createAttachmentsAndAttach(page, docid);
	}

	Pattern attachmentline = Pattern.compile("(?<=^|\n)" +
			"([^\t]*)\t" + //doc/blogid
			"([^\t]*)\t" + //doc/blogtype (102 vs 38)
			"([^\t]*)\t" + //objid
			"([^\t]*)\t" + //filename
			"([^\t]*)\t" + //filetype
			"([^\n]*)");	//timestamp
	protected void initAttachmentData() {
		if (attachmentdata == null) {
			attachmentdata = new HashMap<String, Vector<AttachmentConverter.Data>>();
			String path = getProperties().getProperty("attachmentdata", null);
			if (path == null) {
				log.debug("No path for attachmentdata property");
				return;
			}
			path = path.trim();
			String filestring = "";
			String line;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
				while ((line = reader.readLine()) != null) {
					filestring += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Matcher dataFinder = attachmentline.matcher(filestring);
			boolean found = false;
			while (dataFinder.find()) {
				found = true;
				String docid = dataFinder.group(1);
				if (!docid.matches("\\d+")) continue;

				String objtype = dataFinder.group(2);
				docid = generateDocKey(docid, objtype);
				String attid = dataFinder.group(3);
				String name = dataFinder.group(4);
				String type = dataFinder.group(5);
				String timestamp = dataFinder.group(6);
				Vector<Data> thisdata = null;
				if (attachmentdata.containsKey(docid)) {
					thisdata = attachmentdata.get(docid);
				}
				else {
					thisdata = new Vector<AttachmentConverter.Data>();
				}
				thisdata.add(new Data(attid, name, type, timestamp));
				attachmentdata.put(docid, thisdata);
				log.debug("attachmentdata : " + docid + " -> " + attid);
			}
			if (!found) {
				String excerpt = filestring.substring(0, 500);
				log.error("No data found in attachmentdata file: " + excerpt);
			}
		}
	}

	public String generateDocKey(String objid, String objtype) {
		return objid + "-" + objtype;
	}

	Pattern jivemeta = Pattern.compile("\\{jive-export-meta:([^}]+)\\}");
	Pattern idPattern = Pattern.compile("^id=(\\d+)");
	Pattern typePattern = Pattern.compile("\\|type=(\\w+)");
	public String getDocId(String input) {
		Matcher jivemetaFinder = jivemeta.matcher(input);
		if (jivemetaFinder.find()) {
			String params = jivemetaFinder.group(1);
			Matcher idFinder = idPattern.matcher(params);
			String id = (idFinder.find())?idFinder.group(1):null;
			Matcher typeFinder = typePattern.matcher(params);
			String type = (typeFinder.find())?typeFinder.group(1):null;
			log.debug("getting id. type = " + type);
			int typenum = -1;
			if ("DOC".equals(type)) typenum = 102;
			else if ("BLOG".equals(type)) typenum = 38; 
			else if ("COMMENT".equals(type)) typenum = 105; 
			else log.error("Problem with attachemnts. Unknown object type: " + type);
			return generateDocKey(id, typenum+"");
		}
		return null;
	}

	private void createAttachmentsAndAttach(Page page, String docid) {
		if (attachmentdata.containsKey(docid)) {
			Vector<Data> attachments = attachmentdata.get(docid);
			//key = filename (we only want one of each filename)
			HashMap<String,Data> filtered = new HashMap<String, AttachmentConverter.Data>();
			
			 //save only the latest version of the attachment
			for (Data data : attachments) {
				String key = data.name;
				if (filtered.containsKey(key)) { //compare them?
					Data previous = filtered.get(key);
					if (asLong(previous.timestamp) < asLong(data.timestamp))
						filtered.put(key, data);
				}
				else filtered.put(key, data);
			}
			log.debug("There are " + filtered.size() + " attachments for " + docid);
			//attach the files 
			for (String key : filtered.keySet()) {
				Data data = filtered.get(key);
				String filename = getFilename(data); //get jive naming convention 
				String parent = getParent(filename);
				String fullpath = getAttachmentDirectory() + parent + filename;
				File file = new File(fullpath);
				if (!file.exists()) {
					log.error("Could not find attachment for docid '" + docid +
							"' at filepath: " + fullpath + " for attachment with name: " + data.name);
				}
				else {
					log.debug("Attaching (id:" + data.id + ", name: " + data.name + ")");
					page.addAttachment(file, data.name);
				}
			}
		}
	}

	private long asLong(String longstring) {
		return Long.parseLong(longstring);
	}

	private String getParent(String filename) {
		return File.separator + filename.substring(0,1) + 
		File.separator + filename.substring(1,2) + 
		File.separator + filename.substring(2,3) + 
		File.separator;
	}

	//jive has a really strange naming convention they use in sbs 4.0.6 for file system style attachments
	private String getFilename(Data data) {
		String basename = data.type + JIVE_CONSTANT + data.id;
		//reverse it!
		String reversed = "";
		for (char c : basename.toCharArray()) {
			reversed = c + reversed;
		}
		return reversed + JIVE_ATT_FILETYPE;
	}

	public class Data {
		public String id;
		public String name;
		public String type;
		public String timestamp;
		public Data(String attid, String name, String type,
				String timestamp) {
			this.id = attid;
			this.name = name;
			this.type = type;
			this.timestamp = timestamp;
		}
	}
}
