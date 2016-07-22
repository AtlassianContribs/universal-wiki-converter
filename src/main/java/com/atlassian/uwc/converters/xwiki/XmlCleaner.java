package com.atlassian.uwc.converters.xwiki;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * Removes Xwiki export xml, leaving only the value of the content tag 
 */
public class XmlCleaner extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	private static final String DEFAULT_USERAGENT = "Universal Wiki Converter";
	public void convert(Page page) {
		log.info("Cleaning Xml -- starting");
		String input = page.getOriginalText();
		String converted = cleanXml(input);
		page.setConvertedText(converted);
		log.info("Cleaning Xml -- complete");
	}

	protected String cleanXml(String input) {
		//use dom4j to get the correct content tag
		Element xml = getRootElement(input);
		Element contentEl = xml.element("content");
		String contentXml = contentEl.asXML();
		String content = cleanContent(contentXml);
		//xwiki sometimes outputs html entities in page content xml. 
		content = StringEscapeUtils.unescapeHtml(content);
		//XXX need to do it twice to handle Xwiki output &amp;nbsp; 
		content = StringEscapeUtils.unescapeHtml(content); 
		return content;
	}
	
	Pattern content = Pattern.compile("" +
			"<content>(.*?)<\\/content>", Pattern.DOTALL
			);
	protected String cleanContent(String input) {
		Matcher contentFinder = content.matcher(input);
		if (contentFinder.find()) {
			return contentFinder.group(1);
		}
		return input;
	}
	
	protected Element getRootElement(String input) {
		Document document = null;
		System.setProperty( "http.agent", getUserAgent());
		try {
			document = DocumentHelper.parseText(input);
		} catch (DocumentException e) {
			log.error("Problem parsing with dom4j. File may have invalid html.");
			log.error(e.getMessage());
			return null;
		}
		Element rootElement = document.getRootElement();
		return rootElement;
	}
	private String getUserAgent() {
		Properties props = this.getProperties();
		if (!props.containsKey("user-agent"))
			return DEFAULT_USERAGENT;
		return props.getProperty("user-agent", DEFAULT_USERAGENT);
	}

}
