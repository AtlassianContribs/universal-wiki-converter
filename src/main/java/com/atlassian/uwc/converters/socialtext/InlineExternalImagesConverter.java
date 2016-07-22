package com.atlassian.uwc.converters.socialtext;

import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * transforms external links that are pointing at image files to inline image syntax.
 * The image files are identified by whether they use an extension from the list
 * defined by the converter property:
 * Socialtext.0900.extensions.property=gif,jpg,jpeg,bmp,png
 */
public class InlineExternalImagesConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass()); 
	public void convert(Page page) {
		log.debug("Converting Spacenames - start");
		String input = page.getOriginalText();
		String converted = convertImages(input);
		page.setConvertedText(converted);
		log.info("Converting Spacenames - complete");
	}

	Pattern link = Pattern.compile("\\[([^\\]]+)\\]");
	Pattern external = Pattern.compile("http://[^^|#/]+\\/[^^:|#]+?\\.([^^:|#/.]+)$");
	protected String convertImages(String input) {
		Vector<String> extensions = getExtensions();
		if (extensions != null) {
			Matcher linkFinder = link.matcher(input);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			while (linkFinder.find()) {
				found = true;
				String contents = linkFinder.group(1);
				Matcher extFinder = external.matcher(contents);
				String replacement = "";
				if (extFinder.find()) {
					String extension = extFinder.group(1);
					if (extensions.contains(extension)) {
						replacement = "!" + contents + "!";
					}
					else continue;
				}
				else continue;
				replacement = RegexUtil.handleEscapesInReplacement(replacement);
				linkFinder.appendReplacement(sb, replacement);
			}
			if (found) {
				linkFinder.appendTail(sb);
				return sb.toString();
			}
			return input;
		}
		return input;
	}
	/**
	 * @return vector of extensions defined by the comma delimted property:
	 * Socialtext.0900.extensions.property=gif,jpg,jpeg,bmp,png
	 */
	protected Vector<String> getExtensions() {
		Properties props = getProperties();
		if (!props.containsKey("extensions")) return null;
		String extRaw = props.getProperty("extensions");
		String[] exts = extRaw.split(",");
		Vector<String> extensions = new Vector<String>();
		for (String ext : exts) {
			extensions.add(ext);
		}
		return extensions;
	}

}
