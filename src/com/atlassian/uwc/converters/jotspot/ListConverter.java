package com.atlassian.uwc.converters.jotspot;

import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * converts HTML lists (including nested lists) into Confluence lists 
 * @author Laura Kolker
 */
public class ListConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	
	public void convert(Page page) {
		log.debug("List Converter - starting");
		String input = page.getOriginalText();
		String converted = input;
	
		//chunk input into prelist, list, and postlist parts
		String[] parts = getOuterParts(input);
		String pre = parts[0];
		String lists = parts[1];
		String post = parts[2];
		
		//parse the lists
		lists = parseLists(lists);
		if (lists == null) //exception occurred, so return early
			return;
		
		//build the converted string
		converted = pre + lists + post;
		log.debug("converted (everything) = " + converted); 
		
		page.setConvertedText(converted);
		log.debug("List Converter - complete");
	}
	
	/**
	 * prepares the XML reader, and parses HTML lists
	 * @param lists can be multiple lists, and can have nonlist text between 
	 * multiple lists, but beginning and end must be list syntax.
	 * <br/>Example:
	 * <br/>&lt;ul&gt;&lt;li&gt;a&lt;/li&gt;&lt;/ul&gt; Some other text &lt;ol&gt;&lt;li&gt;b&lt;/li&gt;&lt;/ol&gt;
	 * @return Confluence syntax lists
	 * <br/>Example:
	 * <br/>* a
	 * <br/>Some other text
	 * <br/># b
	 */
	private String parseLists(String lists) {
		//get xml reader
		XMLReader reader = null;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			log.error("Had problem loading XMLReaderFactory. Exiting ListConverter.");
			e.printStackTrace();
			return null;
		}
		
		//prepare parser
		ListParser parser = new ListParser();
		reader.setContentHandler(parser);
		reader.setErrorHandler(parser);
		
		//parse
		try {
			lists = parseLists(lists, reader, parser);
		} catch (IOException e) {
			log.error("IO Problem while parsing.");
			e.printStackTrace();
		} catch (SAXException e) {
			log.error("SAX Problem while parsing.");
			e.printStackTrace();
		}
		
		return lists;
	}

	String openingTagAttributes = "[^>]*";
	String itemTag = "<li" + openingTagAttributes + ">";
	String listRegexStr = 
		"((?:(?:<\\/?[ou]l>\r?\n*)+" + 	// list start or end
		"(?:(?:"+ itemTag+ 				// item start
		".*?" + 						// item contents
		"<\\/li>" +						// item end
		"\r?\n*" +						// newlines allowed
		")+))+)" +						// repeat
		"((?:<\\/[ou]l>\r?\n?)+)"; 		// list final end 
	String listPlusRegex = "((.*?)" +  listRegexStr + ")";
	Pattern listPattern = Pattern.compile(listPlusRegex, Pattern.DOTALL);
	/**
	 * parses HTML lists with the given xmlreader and SAXparser
	 * @param lists HTML lists (as described in parseLists(String))
	 * @param xr given xmlreader
	 * @param parser given SAX parser
	 * @return Confluence syntax lists (as described in parseLists(String))
	 * @throws IOException
	 * @throws SAXException
	 */
	private String parseLists(String lists, XMLReader xr, ListParser parser) throws IOException, SAXException {
		log.debug("lists = " + lists);
		if (lists == null) {
			return null;
		}
		Matcher listSeperator = listPattern.matcher(lists);
		String newlist = "";
		//chunk the input into non-list and list chunks
		while (listSeperator.find()) {
			String pretext = listSeperator.group(2);
			if (!"".equals(pretext))
				pretext = "\n" + pretext;
			String list = listSeperator.group(3) + listSeperator.group(4);
			log.debug("pretext: " + pretext);
			log.debug("list: " + list);

			//parse the list with the SAX parser!
			InputSource source = new InputSource(new StringReader(list));
			xr.parse(source);
			
			//rejoin nonlist and list chunks
			String nextChunk = pretext + parser.getOutput().trim();
			newlist += nextChunk + "\n";
			parser.clearOutput();
		}
		return newlist;
	}
		

	Pattern parts = Pattern.compile("(.*?)(<[ou]l>.*<\\/[ou]l>)(.*)", Pattern.DOTALL);
	/**
	 * divide the input into 3 parts:
	 * <br/>before the lists
	 * <br/>the lists
	 * <br/>after the lists
	 * @param input
	 * @return String array with these 3 parts
	 */
	private String[] getOuterParts(String input) {
		Matcher itemFinder = parts.matcher(input);
		String[] partsArray = new String[3];
		if (itemFinder.matches()) {
			String pre = itemFinder.group(1);
			String mid = itemFinder.group(2);
			String post = itemFinder.group(3);
			log.debug("pre = " + pre + " post = " + post);
			partsArray[0] = pre;
			partsArray[1] = mid;
			partsArray[2] = post;
		}
		return partsArray;
	}

}
