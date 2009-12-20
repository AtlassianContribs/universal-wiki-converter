package com.atlassian.uwc.converters.xwiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.ui.Page;

public class VelocityCleaner extends BaseConverter {

	private static final String CLEANED_VELOCITY_SIMPLE = "The UWC detected velocity templates not " +
					"surrounded by pre blocks in this page. ";
	private static final String CLEANED_VELOCITY_MESSAGE = CLEANED_VELOCITY_SIMPLE +
				"\n" +
				"If you would like it to attempt to convert it, " +
				"re-run the conversion with the following converter from conf/converter.xwiki.properties " +
				"commented out:\n" +
				"Xwiki.0060.clean-velocity.class=com.atlassian.uwc.converters.xwiki.VelocityCleaner";
	protected static final String CLEANED_VELOCITY_INFO_BOX = "{info:title=Velocity Template}\n" +
			CLEANED_VELOCITY_MESSAGE + 
			"\n{info}\n";
	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Cleaning Velocity -- starting");
		String input = page.getOriginalText();
		String converted = cleanVelocity(input);
		page.setConvertedText(converted);
		log.info("Cleaning Velocity -- complete");
	}
	
	Pattern velocity = Pattern.compile("#set");
	protected String cleanVelocity(String input) {
		String nonPreInput = tokenizePre(input);
		Matcher velocityFinder = velocity.matcher(nonPreInput);
		if (velocityFinder.find()) {
			log.info(CLEANED_VELOCITY_SIMPLE + "Cleaning velocity.");
			return CLEANED_VELOCITY_INFO_BOX;
		}
		return input;
	}
	
	protected String tokenizePre(String input) {
		String value = "(\\{pre\\}.*?\\{\\/pre\\}){replace-multiline-with}$1";
		input = tokenize(input, value);
		value = "(\\{code\\}.*?\\{code\\}){replace-multiline-with}$1";
		return tokenize(input, value);
	}

	private String tokenize(String input, String value) {
		JavaRegexAndTokenizerConverter converter = (JavaRegexAndTokenizerConverter) 
			JavaRegexAndTokenizerConverter.getConverter(value);
		Page page = new Page(null);
		page.setOriginalText(input);
		page.setConvertedText(input);
		converter.convert(page);
		String out = page.getConvertedText();
		return out;
	}

}
