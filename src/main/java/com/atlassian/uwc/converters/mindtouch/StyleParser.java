package com.atlassian.uwc.converters.mindtouch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.atlassian.uwc.converters.xml.DefaultXmlParser;

public class StyleParser extends DefaultXmlParser {

	Pattern color = Pattern.compile("" +
			"color:(.*)", Pattern.DOTALL);
	Pattern rgb = Pattern.compile("" +
			"rgb\\((\\d+), *(\\d+), *(\\d+)\\)");
	boolean hasColor = false;
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		String style = attributes.getValue("style");
		if (style == null || "".equals(style)) return;
		String[] cssString = style.split(";");
		String val = "";
		for (String css : cssString) {
			css = css.trim();
			Matcher colorFinder = color.matcher(css);
			if (colorFinder.find()) {
				val = colorFinder.group(1).trim();
				Matcher rgbFinder = rgb.matcher(val);
				if (rgbFinder.find()) {
					String r = rgbFinder.group(1);
					String g = rgbFinder.group(2);
					String b = rgbFinder.group(3);
					val = rgb2Hex(r,g,b);
				}
				hasColor = true;
				appendOutput("{color:" + val + "}");
			}
		}
	}
	private String rgb2Hex(String r, String g, String b) {
		String rHex = Integer.toHexString(Integer.parseInt(r));
		if (Pattern.matches(".", rHex))	//if it's only a single char 
			rHex = "0" + rHex;			//add a 0 to the beginning
		String gHex = Integer.toHexString(Integer.parseInt(g));
		if (Pattern.matches(".", gHex))	//if it's only a single char 
			gHex = "0" + gHex;			//add a 0 to the beginning
		String bHex = Integer.toHexString(Integer.parseInt(b));
		if (Pattern.matches(".", bHex))	//if it's only a single char 
			bHex = "0" + bHex;			//add a 0 to the beginning
		return "#" + rHex + gHex + bHex;
	}
	
	public void endElement(String uri, String localName, String qName) {
		if (hasColor) appendOutput("{color}");
		hasColor = false;
	}
	
	public void characters(char[] ch,
            int start,
            int length) throws SAXException {
		appendOutput(String.copyValueOf(ch, start, length));
	}
}
