package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class ImageConverter extends BaseConverter {

	private static final String ALIGN_PREFIX = "align=";

	private static final String CONF_THUMBNAIL = "thumbnail";

	private static final String IMAGE_DELIM = "|";
	
	private static final String MULT_DELIM = ",";
	
	public enum Alignment {
		LEFT { public String toString() {return "";}},
		RIGHT { public String toString() {
			return ALIGN_PREFIX + super.toString().toLowerCase();
			}},
		CENTER { public String toString() {
			return ALIGN_PREFIX +  super.toString().toLowerCase();
			}}
	}
	
	Logger log = Logger.getLogger(this.getClass());

	public void convert(Page page) {
		log.info("Converting Images - start");

		String input = page.getOriginalText();
		String converted = convertImages(input);
		page.setConvertedText(converted);

		log.info("Converting Images - complete");
	}

	String image = 
		"\\[\\[" +			//opening two brackets
		":?" +				//optional colon
		"Image:" +			//the string 'Image:'
		"(" +				//start capturing (group1)
			"[^\\]]+" +		//anything not a right bracket until
		")" +				//end capturing (group1)
		"\\]\\]";			//two right brackets

	Pattern imagePattern = Pattern.compile(image, Pattern.CASE_INSENSITIVE);

	protected String convertImages(String input) {
		Matcher imageFinder = imagePattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String contents = imageFinder.group(1);
			log.debug("image content = " + contents);
			contents = handleImageProperty(contents);
			String replacement = "!" + contents + "!";
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


	Pattern size = Pattern.compile("\\d+");
	Pattern heightPattern = Pattern.compile("(\\d+)x(\\d+)px");
	/**
	 * checks to see if a thumb property is present, and if so returns the
	 * converter image string with a thumb parameter
	 * 
	 * @param input - image name and possibly properties
	 * @return correctly ordered confluence syntax with thumb property
	 */
	protected String handleImageProperty(String input) {
		// XXX when we want to handle other image properties (uwc-103), we'll do
		// it here
		// XXX Also note: CONF-8177 http://jira.atlassian.com/browse/CONF-8177
		// as of Confluence 2.4.2, thumbnail and alignment properties not co-existing
		// peacefully, but we should assume they should be able to and convert
		// accordingly
		log.debug("handling image property for input: " + input);
		
		//split on pipes
		String[] props = input.split("\\" + IMAGE_DELIM); //escape 'cause pipe is a regex char
		
		//handle errors
		if (props.length == 0) {
			log.error("no image content to handle.");
			return input;
		}
		//get relevant image info
		String img = props[0];
		boolean thumbnail = false;
		Alignment align = Alignment.LEFT;
		String sizing = null;
		//look through props. [0] was the image.
		for (int i = 1; i < props.length; i++) {
			String prop = props[i];
			if (prop.startsWith("thumb")) {
				thumbnail = true;
				continue;
			}
			else if (prop.startsWith("right")) {
				align = Alignment.RIGHT;
				continue;
			}
			else if (prop.startsWith("frame")) {
				align = Alignment.RIGHT;
				continue;
			}
			else if (prop.startsWith("center")) {
				align = Alignment.CENTER;
				continue;
			}
			else if (size.matcher(prop).lookingAt()) {
				Matcher heightFinder = heightPattern.matcher(prop);
				if (heightFinder.matches()) {
					String width = heightFinder.group(1);
					String height = heightFinder.group(2);
					sizing = "width=" + width + "px,height="+ height + "px";
				}
				else {
					sizing = "width=" + prop.replaceAll("\\D", "") + "px";
				}
			}
		}
		return createConfluenceImage(img, thumbnail, align, sizing);
	}

	protected String createConfluenceImage(String img, boolean thumbnail, Alignment align) {
		return createConfluenceImage(img, thumbnail, align, null);
	}
	protected String createConfluenceImage(String img, boolean thumbnail, Alignment align, String sizing) {
		log.debug("Creating confluence image syntax.");
		String imageSyntax = img;
		imageSyntax += handleThumbnail(thumbnail);
		String delim = (imageSyntax.equals(img)?IMAGE_DELIM:MULT_DELIM);
		imageSyntax += handleAlignment(align, delim);
		delim = (imageSyntax.equals(img)?IMAGE_DELIM:MULT_DELIM);
		imageSyntax += handleSize(sizing, delim);
		return imageSyntax;
	}

	protected String handleThumbnail(boolean thumbnail) {
		return  thumbnail ?
				IMAGE_DELIM + CONF_THUMBNAIL :
				"";
	}
	
	protected String handleAlignment(Alignment align, String delim) {
		String alignString = align.toString();
		alignString = ("".equals(alignString)?"":delim+alignString);
		return alignString;
	}

	protected String handleSize(String size, String delim) {
		if (size == null) return "";
		return delim + size;
	}

}
