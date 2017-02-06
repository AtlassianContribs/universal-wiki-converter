package com.atlassian.uwc.converters.jotspot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * handles converting jotspot &lt;span&gt; based styles:
 * bold, italics, underline, strikethrough, color
 * @author Laura Kolker
 */
public class FontStyleConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Font Style Converter - starting");

		String input = page.getOriginalText();
		String converted = input;
		String regex = "\\<span style=(.*?)\\>(.*?)\\<\\/span\\>"; //span with style

		Pattern p = Pattern.compile(regex);
		StringBuffer sb  = new StringBuffer();
		Matcher m = p.matcher(input);
		while (m.find()) {
			String style = m.group(1);
			String content = m.group(2);
			log.debug("style = " + style);
			log.debug("content = " + content);
			String replacement = getReplacement(style, content);
			if (replacement.equals(content))
				continue;
			log.debug("replacement = " + replacement);
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		converted = sb.toString();
		
		page.setConvertedText(converted);
		log.debug("Font Style Converter - complete");
	}
	
	Pattern boldPattern = Pattern.compile("bold");
	Pattern italicPattern = Pattern.compile("italic");
	Pattern underlinePattern = Pattern.compile("underline");
	Pattern linethroughPattern = Pattern.compile("line-through");
	Pattern colorPattern = Pattern.compile("color");
	
	/**
	 * gets Confluence replacement for a given jotspot style
	 * @param style style attribute contents for that given span.
	 * <br/>Example:
	 * <br/>If the span was: &lt;span style="font-weight:bold" &gt;Some Content &lt;/span&gt;
	 * <br/>The style that would be passed would be "font-weight:bold"
	 * @param content Text Content of the span
	 * <br/>Example: 
	 * <br/>If the span was: &lt;span style="font-weight:bold" &gt;Some Content &lt;/span&gt;
	 * <br/>the content that would be passed would be "Some Content"
	 * @return
	 */
	private String getReplacement(String style, String content) {
		
		Matcher boldFinder = boldPattern.matcher(style);
		Matcher italicFinder = italicPattern.matcher(style);
		Matcher underlineFinder = underlinePattern.matcher(style);
		Matcher linethroughFinder = linethroughPattern.matcher(style);
		Matcher colorFinder = colorPattern.matcher(style);
	
		boolean isBold = boldFinder.find();
		boolean isItalic = italicFinder.find();
		boolean isUnderline = underlineFinder.find();
		boolean isLinethrough = linethroughFinder.find();
		boolean isColor = colorFinder.find();
		log.debug("isBold: " + isBold 
				+ " isItalic: " + isItalic 
				+ " isUnderline: " + isUnderline
				+ " isLinethrough: " + isLinethrough
				+ " isColor: " + isColor);
		
		//the span can have multiple styles, so make sure we have them all
		String output = isUnderline?underline(content):content;
		output = isItalic?italicize(output):output;
		output = isBold?enbolden(output):output;
		output = isLinethrough?strikeout(output):output;
		output = isColor?color(output, style):output;
		
		return output;
	}
	
	/**
	 * @param text
	 * @return Confluence underlining syntax for the given text
	 */
	private String underline(String text) {
		log.debug("underlining: " + text);
		return "+" + text + "+";
	}

	/**
	 * @param text
	 * @return Confluence italics syntax for the given text
	 */
	private String italicize(String text) {
		log.debug("italicizing: " + text);
		return "_" + text + "_";
	}
	
	/**
	 * @param text
	 * @return Confluence bold syntax for the given text
	 */
	private String enbolden(String text) {
		log.debug("bolding: " + text);
		return "*" + text + "*";
	}
	
	/**
	 * @param text
	 * @return Confluence strikeout syntax for the given text
	 */
	private String strikeout(String text) {
		log.debug("striking: " + text);
		return "-" + text + "-";
	}
	
	/**
	 * @param text
	 * @param style 
	 * @return Confluence color syntax for the given text and color
	 */
	private String color(String text, String style) {
		log.debug("coloring: " + text);
		log.debug("with: " + style);
		String hexColor = getHex(style);
		return "{color:" + hexColor + "}" + text + "{color}";
	}

	Pattern rgbColor = Pattern.compile("rgb\\(([\\d, ]+)\\)");
	/**
	 * @param input color could be <ul><li/>#ffffff<li/>rgb(0, 0, 0)<li/>red</ul> 
	 * @return Confluence usable color - either hex or written color, so
	 * <ul><li/>#ffffff<li/>#000000</li>red</ul>
	 */
	private String getHex(String input) {
		Matcher rgbFinder = rgbColor.matcher(input);
		String hexnum = "#";
		if (rgbFinder.find()) {
			String[] octal = rgbFinder.group(1).split(",");
			for (int i = 0; i < octal.length; i++) {
				String num = octal[i];
				num = num.trim();
				log.debug("original = " + num);
				String asHex = Integer.toHexString(Integer.parseInt(num));
				log.debug("asHex + " + asHex);
				hexnum += asHex;
			}
			input = hexnum;
			log.debug("hexnum = " + hexnum);
		}
		return input;
	}

}
