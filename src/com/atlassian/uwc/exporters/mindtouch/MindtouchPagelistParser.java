package com.atlassian.uwc.exporters.mindtouch;

import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;


public class MindtouchPagelistParser extends DefaultHandler {

	/* Output State */
	
	private Vector<MindtouchPage> pages;
	private MindtouchPage current;
	private Stack<MindtouchPage> last;

	public Vector<MindtouchPage> getPages() {
		if (this.pages == null)
			this.pages = new Vector<MindtouchPage>();
		return this.pages;
	}
	
	public void clearPages() {
		this.pages.clear();
	}
	
	public Stack<MindtouchPage> getLast() {
		if (this.last == null)
			this.last = new Stack<MindtouchPage>();
		return this.last;
	}
	
	/* DefaultHandler methods */
	Logger log = Logger.getLogger(this.getClass());
	
	public enum Type {
		PAGE,
		TITLE,
		SUBPAGES;
		public static Type getType(String input) {
			if (input.equals("page")) return PAGE;
			if (input.equals("title")) return TITLE;
			if (input.equals("subpages")) return SUBPAGES;
			return null;
		}
	}
	
	private Type tagState;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		this.tagState = Type.getType(qName);
		if (this.tagState== null) return;
		switch (this.tagState) {
		case PAGE:
			MindtouchPage newpage = new MindtouchPage();
			newpage.id = getId(attributes);
			this.current = newpage;
			addPage(this.current);
			break;
		case SUBPAGES:
			changeLast(this.current);
			break;
		}
	}

	private void addPage(MindtouchPage page) {
		Vector<MindtouchPage> pages = getPages();
		if (pages.isEmpty()) pages.add(page);
		else {
			getLast().peek().getSubpages().add(page);
		}
	}
	private void changeLast(MindtouchPage page) {
		getLast().push(page);
	}

	private String getId(Attributes attributes) {
		return attributes.getValue("id");
	}
	
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		if (type == null) return;
		switch (type) {
		case SUBPAGES:
			getLast().pop();
			break;
		}
		this.tagState = null;
	}
	
	public void characters(char[] ch, int start, int length) {
		String content = String.copyValueOf(ch, start, length);
		if (this.tagState == null) return;
		switch (this.tagState) {
		case TITLE:
			if (this.current.title == null) this.current.title = "";
			this.current.title += content;
			break;
		}
	}
}
