package com.atlassian.uwc.converters.mindtouch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class CommentParser extends DefaultXmlParser {

	private static final String DEFAULT_XMLFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String DEFAULT_RENDERFORMAT = "HH:mm, d MMM yyyy";
	Logger log = Logger.getLogger(this.getClass());

	public enum Type {
		COMMENT,
		USERNAME,
		DATE,
		CONTENT
		;
		static Type getType(String qName) {
			if ("comment".equals(qName)) return COMMENT;
			if ("username".equals(qName)) return USERNAME;
			if ("date.posted".equals(qName)) return DATE;
			if ("content".equals(qName)) return CONTENT;
			return null;
		}
	}
	
	private static Type type;
	private static boolean isComment = false;
	private static String username;
	private static String date;
	private static String content;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case COMMENT:
			isComment = true;
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case COMMENT:
			getPage().addComment(createComment());
			clear();
			isComment = false;
		}
	}
	

	private String createComment() {
		return username + " says:\n" + content + "\n" + formatdate(date);
	}

	private String formatdate(String input) {
		input = input.replaceFirst("T", " "); //these chars aren't allowed by SimpleDateFormat
		input = input.replaceFirst("Z$", "");
		String xmlFormat = getXmlFormat(); //the way the data is maintained in the xml
		String renderformat = getRenderFormat(); //how it was presented in Mindtouch
		return "~Posted " + changeTimestampFormat(xmlFormat, input, renderformat) + "~";
	}

	private String getRenderFormat() {
		Properties props = getProperties();
		if (props.containsKey("comment-date-renderformat"))
			return props.getProperty("comment-date-renderformat", DEFAULT_RENDERFORMAT);
		return DEFAULT_RENDERFORMAT;
	}

	private String getXmlFormat() {
		Properties props = getProperties();
		if (props.containsKey("comment-date-xmlformat"))
			return props.getProperty("comment-date-xmlformat", DEFAULT_XMLFORMAT);
		return DEFAULT_XMLFORMAT;
	}

	private String changeTimestampFormat(String oldFormat, String timestamp, String newFormat) {
		DateFormat dateFormat = new SimpleDateFormat(oldFormat);
		try {
			Date date = dateFormat.parse(timestamp);
			return getTimestamp(newFormat, date);
		} catch (ParseException e) {
			log.error("Could not format date:");
			e.printStackTrace();
			return timestamp;
		}
	}
	private String getTimestamp(String format, Date date) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		return (dateFormat.format(date));
	}
	private void clear() {
		username = "";
		date = "";
		content = "";
	}
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		if (type == null) return;
		if (isComment) {
			String text = String.copyValueOf(ch, start, length);
			switch (type) {
			case USERNAME:
				username = text;
				break;
			case DATE:
				date = text;
				break;
			case CONTENT:
				content = text;
				break;
			}
		}
	}
}
