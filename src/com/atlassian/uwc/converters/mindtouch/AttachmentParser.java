package com.atlassian.uwc.converters.mindtouch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;
import com.atlassian.uwc.filters.NoSvnFilter;

public class AttachmentParser extends DefaultXmlParser {
	public enum Type {
		FILES,
		;
		static Type getType(String qName) {
			if ("files".equals(qName)) return FILES;
			return null;
		}
	}
	Logger log = Logger.getLogger(this.getClass());
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		if (type == null) return;
		String numfiles = attributes.getValue("count");
		int num = Integer.parseInt(numfiles);
		//attach the files if there are any
		if (num > 0) {
			File[] files = getFiles();
			if (files == null) return;
			for (File file : files) {
				getPage().addAttachment(file);
			}
		}
	}
	Pattern xmlext = Pattern.compile("\\.xml$");
	private File[] getFiles() {
		if (getPage() == null || getPage().getFile()==null || getPage().getFile().getAbsolutePath() == null) {
			log.error("Problem with getting page's absolute path. Page, page.file, or page.file.absolutepath is null");
			return null;
		}
		String pagepath = getPage().getFile().getAbsolutePath();
		Matcher xmlextFinder = xmlext.matcher(pagepath);
		if (xmlextFinder.find()) {
			String attdirpath = xmlextFinder.replaceAll("_attachments");
			File attdir = new File(attdirpath);
			if (!attdir.exists() || !attdir.isDirectory()) {
				log.error("Attachments directory does not exist or is a directory: " + attdirpath);
				return null;
			}
			return attdir.listFiles(new NoSvnFilter());
		}
		else log.warn("Page file didn't have expected extension: " + pagepath);
		return null;
	}
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		//do nothing - which clears any extra tag data
	}
}
