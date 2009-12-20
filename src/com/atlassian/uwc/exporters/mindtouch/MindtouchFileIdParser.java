package com.atlassian.uwc.exporters.mindtouch;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


public class MindtouchFileIdParser extends DefaultHandler {

	/* Output State */
	private Vector<String> ids;
	private Vector<String> names;
	public Vector<String> getIds() {
		if (this.ids == null)
			this.ids = new Vector<String>();
		return this.ids;
	}
	public Vector<String> getNames() {
		if (this.names == null)
			this.names = new Vector<String>();
		return this.names;
	}

	
	/* DefaultHandler methods */
	Logger log = Logger.getLogger(this.getClass());
	
	public enum Type {
		FILE,
		NAME;
		public static Type getType(String input) {
			if (input.equals("file")) return FILE;
			if (input.equals("filename")) return NAME;
			return null;
		}
	}
	
	private boolean isName = false;
	String currentName = "";
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case FILE:
			getIds().add(getId(attributes));
			break;
		case NAME:
			isName = true;
			break;
		}
	}
	
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case NAME:
			isName = false;
			getNames().add(this.currentName);
			this.currentName = "";
			break;
		}
	}
	
	public void characters(char[] ch, int start, int length) {
		if (isName) {
			String content = String.copyValueOf(ch, start, length);
			this.currentName += content;
		}
	}
	
	private String getId(Attributes attributes) {
		return attributes.getValue("id");
	}

}
