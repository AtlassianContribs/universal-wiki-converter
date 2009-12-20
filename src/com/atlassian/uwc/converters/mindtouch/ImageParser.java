package com.atlassian.uwc.converters.mindtouch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class ImageParser extends DefaultXmlParser {
	Logger log = Logger.getLogger(this.getClass());
	
	Pattern filename = Pattern.compile("[@]api/deki/files/[^\\/]+/=([^\\/]+?)(?:\\?parent=([^\\/]+))?$");
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String src = attributes.getValue("src");
		if (src == null) {
			log.warn("Image src is undefined.");
			return;
		}
		Matcher filenameFinder = filename.matcher(src);
		if (filenameFinder.find()) {
			String filenameString = filenameFinder.group(1);
			String parent = filenameFinder.group(2);
			if (parent != null)
				appendOutput(createImgSyntax(filenameString, parent));
			else 
				appendOutput(createImgSyntax(filenameString));
		}
		else { //not dekiwiki image syntax, just return the src in ! chars
			appendOutput(createImgSyntax(src));
		}
	}
	private String createImgSyntax(String filenameString) {
		return "!" + filenameString + "!";
	}
	private String createImgSyntax(String filenameString, String parent) {
		if (getPage() != null && getPage().getName() != null && getPage().getName().equals(parent))
			return createImgSyntax(filenameString);
		return "!" + parent + "^" + filenameString + "!";
	}
}
