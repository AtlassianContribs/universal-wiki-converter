package com.atlassian.uwc.converters.xml;

import java.util.Stack;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BasicParser extends DefaultXmlParser {
	static Stack<Type> typestack = new Stack();
	static boolean addNL = false;
	static boolean needsSpace = false;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		Type type = Type.getType(qName);
		String content = "";
		if (type.needsSpace()) content += " ";
		content += type.getDelim();
		if (type.needsNL(true)) content = content + "\n";
		if (typestack.isEmpty()) addNL = needsSpace = false;
		appendOutput(content);
		typestack.push(type);
	}
	
	public void endElement(String uri, String localName, String qName) {
		Type type = Type.getType(qName);
		String content = type.getDelim();
		if (type.needsNL(false)) content = "\n" + content;
		appendOutput(content);
		typestack.pop();
		if (typestack.isEmpty() && addNL) {
			appendOutput(" ");
			addNL = false;
		}
	}

	Pattern ws = Pattern.compile("\\s+");
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		   String content = String.copyValueOf(ch, start, length);
		   content = content.replaceAll("\\s+", " ");
		   content = content.trim();
		   appendOutput(content);
		   if (!ws.matcher(content).matches()) needsSpace = true;
	}
	
	public enum Type {
		BOLD,
		ITAL,
		UNDERLINE,
		STRIKE,
		QUOTE,
		BREAK;
		
		public static Type getType(String qName) {
			if ("b".equals(qName) || "strong".equals(qName)) return BOLD;
			if ("i".equals(qName) || "em".equals(qName)) return ITAL;
			if ("u".equals(qName)) return UNDERLINE;
			if ("s".equals(qName)) return STRIKE;
			if ("blockquote".equals(qName)) return QUOTE;
			if ("br".equals(qName)) return BREAK;
			return null;
		}
		
		public String getDelim() {
			switch (this) {
				case BOLD: return "*";
				case ITAL: return "_";
				case UNDERLINE: return "+";
				case STRIKE: return "-";
				case QUOTE: return "{quote}";
				case BREAK: return "";
			}
			return "";
		}
		
		public boolean needsNL(boolean start) {
			if (!typestack.isEmpty()) {
				//check to see if we should be swallowing newlines
				Stack<Type> tmp = new Stack<Type>();
				tmp.addAll(typestack);
				while (!tmp.isEmpty()) {
					Type popped = tmp.pop();
					switch(popped) {
						case BOLD:
						case ITAL:
						case UNDERLINE:
						case STRIKE: {
							//suppressing nl for now. Let's add it later
							addNL = true;
							return false;
						}
					}
				}
			}
			switch (this) {
				case QUOTE: return true;
				case BREAK: return !start; //only want to return true with endElement
			}
			if (typestack.isEmpty() && !start && addNL) {
				addNL = false;
				return true;
			}
			return false;
		}
		public boolean needsSpace() {
			if (typestack.isEmpty()) return false;
			switch (this) {
				case BOLD:
				case ITAL:
				case UNDERLINE:
				case STRIKE: {
					boolean tmp = needsSpace;
					needsSpace = false;
					return tmp;
				}	
			}
			return false;
		}
	}
	
}
