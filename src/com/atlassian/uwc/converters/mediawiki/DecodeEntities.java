package com.atlassian.uwc.converters.mediawiki;

import java.io.IOException;
import java.util.TreeMap;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.PropertyFileManager;

/**
 * @author Laura Kolker
 * @deprecated Use IllegalNameConverter's illegalnames-urldecode misc property instead.
 */
public class DecodeEntities extends BaseConverter {

	private static final String DEFAULT_ENCODING = "utf-8";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Decoding URL entities in filename -- start");
		String filename = page.getName();
		filename = decodeEntities(filename);
		page.setName(filename);
		log.debug("Decoding URL entities in filename -- complete");
	}
	protected String decodeEntities(String input) {
		String encoding = getEncoding();
		try {
			return URIUtil.decode(input, encoding);
		} catch (URIException e) {
			log.error("Problem decoding with charset: " + encoding);
			e.printStackTrace();
		}
		return input;
	}
	
	/**
	 * @return the encoding string, either the one defined in 
	 * conf/exporter.mediawiki.properties or the default (utf-8)
	 */
	private String getEncoding() {
		String propsfile = "conf/exporter.mediawiki.properties";
		String encoding = null;
		try {
			TreeMap<String, String> map = PropertyFileManager.loadPropertiesFile(propsfile);
			encoding = map.get("encoding");
		} catch (IOException e) {
			log.error("Could not load properties file: " + propsfile);
			e.printStackTrace();
		}
		if (encoding == null) return DEFAULT_ENCODING;
		return encoding;
	}

}
