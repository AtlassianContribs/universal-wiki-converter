package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.converters.DetokenizerConverter;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.ui.Page;

public class EscapeBracesConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Escaping Braces - start");
		String input = page.getOriginalText();
		String converted = escapeBraces(input);
		page.setConvertedText(converted);
		log.info("Escaping Braces - complete");
	}

	protected String escapeBraces(String input) {
		input = tokenizeMath(input);
		input = tokenizePre(input);
		input = tokenizeDoubleBraceSyntax(input);
		input = tokenizeTables(input);
		input = escapeSingleBraces(input);
		input = detokenize(input);
		return input;
	}

	private String tokenize(String input, String value) {
		return tokenize(input, value, "");
	}
	private String tokenize(String input, String value, String type) {
		Converter converter = JavaRegexAndTokenizerConverter.getConverter(value);
		Page page = new Page(null);
		page.setOriginalText(input);
		converter.convert(page);
		if (input != null && !input.equals(page.getConvertedText())) {
			log.debug("EscapeBraces: tokenized " + type + " content");
		}
		return page.getConvertedText();
	}

	protected String tokenizeMath(String input) {
		return tokenize(input, "(<math>.*?<\\/math>){replace-multiline-with}$1", "math");
	}

	protected String tokenizePre(String input) {
		return tokenize(input, "(<pre>.*?<\\/pre>){replace-multiline-with}$1", "pre");
	}

	protected String tokenizeDoubleBraceSyntax(String input) {
		return tokenize(input, "(\\{{2,2}.*?\\}{2,2}){replace-multiline-with}$1", "double curly brace");
	}

	protected String tokenizeTables(String input) {
		return tokenize(input, "(\\{\\|.*?\\|\\}){replace-multiline-with}$1", "table") ;
	}

	Pattern leftBrace = Pattern.compile(
			"(?<!\\\\)" +	//not escaped (zero-width neg lookbehind)
			"\\{{1,1}"		//one left brace
			);
	Pattern rightBrace = Pattern.compile("(?<!\\\\)\\}{1,1}");
	protected String escapeSingleBraces(String input) {
		String orig = input;
		Matcher leftFinder = leftBrace.matcher(input);
		if (leftFinder.find()) {
			input = leftFinder.replaceAll("\\\\{");
		}
		Matcher rightFinder = rightBrace.matcher(input);
		if (rightFinder.find()) {
			input = rightFinder.replaceAll("\\\\}");
		}
		if (!input.equals(orig)) {
			log.debug("EscapeBraces: replaced single braces in non-tokenized content.");
		}
		return input;
	}

	protected String detokenize(String input) {
		log.debug("EscapeBraces: detokenizing");
		Page page = new Page(null);
		page.setOriginalText(input);
		DetokenizerConverter converter = new DetokenizerConverter();
		converter.convert(page);
		return page.getConvertedText();
	}

}
