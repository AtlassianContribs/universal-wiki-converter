package com.atlassian.uwc.converters.tikiwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles page naming by using the page's metadata to get the pagename
 */
public class MetadataTitle extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Getting Title from Metadata - start");
		String input = page.getOriginalText();
		String name = getNameFromMetadata(input);
		if (name != null)
			page.setName(name);
		log.info("Getting Title from Metadata - complete");
	}
	
	String pagenameData = "pagename=(.*)";
	Pattern pagenamePattern = Pattern.compile(pagenameData);
	/**
	 * gets the title from the input's metadata
	 * @param input tikiwiki export file contents. should include title metadata.
	 * @return title or null, if could not figure it out
	 */
	protected String getNameFromMetadata(String input) {
		Matcher metadataFinder = getMatcher(input);
		String pagename = null;
		if (metadataFinder.lookingAt()) {
			String metadata = metadataFinder.group(1);
			pagename = getPagename(metadata);
			pagename = decodeEntities(pagename);
		}
		return pagename;
	}

	/**
	 * Finds the pagename amongst the metadata
	 * @param metadata
	 * @return pagename or null, if no relevant pagename metadata
	 */
	protected String getPagename(String metadata) {
		String pagename = null;
		if (metadata == null) return null;
		Matcher pagenameFinder = pagenamePattern.matcher(metadata);
		if (pagenameFinder.find()) {
			pagename = pagenameFinder.group(1);
			if (pagename.endsWith(";"))
				pagename = pagename.substring(0, pagename.length()-1);
		}
		return pagename;
	}

	protected String decodeEntities(String pagename) {
		if (pagename == null) return null;
		String decoded = null;
		String encoding = "utf-8";
		try {
			decoded = URIUtil.decode(pagename, encoding);
		} catch (URIException e) {
			log.error("Problem decoding pagename with encoding: " + encoding);
			e.printStackTrace();
		}
		if (decoded != null) return decoded;
		return pagename;
	}

	/**
	 * @param input
	 * @return gets a matcher from the MetadataCleaner.allmetaPattern, and
	 * the given input
	 */
	private Matcher getMatcher(String input) {
		MetadataCleaner meta = new MetadataCleaner();
		return meta.allmetaPattern.matcher(input);
	}

}
