package com.atlassian.uwc.converters.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LinkParser extends DefaultXmlParser {

	private static final String TOKEN = "UWCSTARTLINK";
	Pattern tokenPattern = Pattern.compile("^(?s)(.*?)" + TOKEN + "(.*)$");
	private String target = "";
	private String alias = "";
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		target = attributes.getValue("href");
		appendOutput(TOKEN);
	}
	
	Pattern space = Pattern.compile("[ ]+");
	public void endElement(String uri, String localName, String qName) {
		if (!"".equals(alias) && !space.matcher(alias).matches()) alias += "|"; 
		String link = "[" + alias + target + "]";
		
		String out = getOutput();
		Matcher finder = tokenPattern.matcher(out);
		if (finder.find()) { //find the token for this element
			String pre = finder.group(1); //save existing pre-list content
			out = finder.group(2);
			this.clearOutput(); //delete all saved content
			appendOutput(pre + link + out); //rewrite saved content with link 
		}
		
		alias = target = "";
		
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		String content = String.copyValueOf(ch, start, length);
		alias += content;
	}
}
