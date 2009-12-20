package com.atlassian.uwc.converters.smf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.hierarchies.MetaHierarchy;
import com.atlassian.uwc.ui.Page;

public class MetaPageContent extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Metadata to PageContent - Start");
		//get metadata
		Properties meta = null;
		try {
			meta = MetaHierarchy.getMeta(page);
		} catch (IOException e) {
			log.error("Problem getting meta data for file: " + page.getFile().getAbsolutePath());
			e.printStackTrace();
			return;
		}

		//add meta content
		String input = page.getOriginalText();
		String converted = convertMeta2PageContent(input, meta);
		page.setConvertedText(converted);
		log.debug("Converting Metadata to PageContent - Complete");
	}

	protected String convertMeta2PageContent(String input, Properties meta) {
		boolean addname = Boolean.parseBoolean(getProperties().getProperty("addname", "false"));
		boolean addtime = Boolean.parseBoolean(getProperties().getProperty("addtime", "false"));
		boolean addchildrenmacro = Boolean.parseBoolean(getProperties().getProperty("addchildrenmacro", "false"));
		boolean addgallerymacro = Boolean.parseBoolean(getProperties().getProperty("addgallerymacro", "false"));
		
		if (!addname && !addtime && !addchildrenmacro && !addgallerymacro) return input;

		String nameString = getNameContent(meta, addname);
		String timeString = getTimeContent(meta, addtime);
		String childrenString = getChildrenContent(meta, addchildrenmacro);
		String galleryString = getGalleryContent(meta, addgallerymacro);

		String metaPreString = nameString + timeString;
		if (!"".equals(metaPreString)) metaPreString += "\n";
		
		String metaPostString = childrenString + galleryString;
		
		return metaPreString + input + metaPostString;
	}

	private String getTimeContent(Properties meta, boolean addtime) {
		String timeformat = getProperties().getProperty("timeformat", "yyyy-MM-dd HH:mm:ss");
		String timeString = "";
		String time = getMetadata(meta, "time");
		long seconds = 0;
		try {
			seconds = Long.parseLong(time);
		} catch (Exception e) {
			;//can't parse time
		}
		if (addtime && time != null) {
			timeString += "*Original Timestamp:* ";
			long milli = seconds * 1000;
			Date date = new Date(milli);
			DateFormat dateFormat = null;
			try {
				dateFormat = new SimpleDateFormat(timeformat);
			} catch (IllegalArgumentException e) {
				log.error("Custom date format is not a valid SimpleDateFormat: " + timeformat);
			}
			timeString += (dateFormat.format(date)) + "\n";
		}
		return timeString;
	}

	private String getNameContent(Properties meta, boolean addname) {
		String nameformat = getProperties().getProperty("nameformat", "profile");
		String nameString = "";
		String metaname = getMetadata(meta, "username");
		String metaemail = getMetadata(meta, "useremail");
		String metauserid = getMetadata(meta, "userid");
		
		if (addname && metaname != null) {
			nameString += "*Original Poster:* ";
			if ("text".equals(nameformat)  
					|| (metaname.equals("Simple Machines") && metauserid.equals("0"))
			) {
				nameString += metaname;
			}
			else if ("profile".equals(nameformat)) {
				nameString += "[~" + metaname + "]"; 
			}
			else if ("email".equals(nameformat) && metaemail != null) {
				nameString += "[" + metaname + "|mailto:" + metaemail + "]";
			}
			else nameString = "";
			if (!"".equals(nameString)) nameString += "\n";
		}
		return nameString;
	}

	private String getChildrenContent(Properties meta, boolean addchildrenmacro) {
		String childString = "";
		String metatype = getMetadata(meta, "type");
		
		if (addchildrenmacro && metatype != null && "top".equals(metatype)) {
			childString += "\n" +
					"*Replies*\n" +
					"{children:sort=creation}\n";
		}
		
		return childString;
	}
	
	private String getGalleryContent(Properties meta, boolean addgallerymacro) {
		String galleryString = "";
		String metatype = getMetadata(meta, "type");
		
		if (addgallerymacro && metatype != null && 
				("top".equals(metatype) || "re".equals(metatype))) {
			galleryString += "\n" +
					"{gallery:title=Attached Images}\n";
		}
		
		return galleryString;
	}

	
	private String getMetadata(Properties meta, String key) {
		String metaname = meta.getProperty(key, null);
		if ("null".equals(metaname)) metaname = null;
		return metaname;
	}

}
