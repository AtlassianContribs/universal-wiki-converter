package com.atlassian.uwc.converters;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;

import com.atlassian.uwc.converters.IllegalChar.Type;
import com.atlassian.uwc.util.PropertyFileManager;


/**
 * @author Laura Kolker
 * parent class for IllegalLinkNameConverter and IllegalPageNameConverter.
 * It can be used to find illegal page names, and help translate them 
 * to legal equivalents.
 */
public abstract class IllegalNameConverter extends BaseConverter {

	private static final String URLDECODE_KEY = "illegalnames-urldecode";
	private static final String DEFAULT_REPLACEMENT = "_";
	private static final String ILLEGALCHAR_PROP_SUFFIX = ".replacement";
	private static final String ILLEGALCHAR_PROP_PREFIX = "illegalchar.";
	private static final String ILLEGALSTART_PROP_PREFIX = "illegalstart.";
	private static final String CONFIG_SETTINGS_FILE = "settings.illegalcharmap.properties";
	private static final String PROP_DIR = "conf";
	private static final String FILE_SEP = System.getProperty("file.separator");

	private String[] requiredIllegalChars = {
			"colon",
			"semicolon",
			"lessthan",
			"greaterthan",
			"at",
			"forwardslash",
			"backslash",
			"pipe",
			"hash",
			"leftbracket",
			"rightbracket",
			"leftcurlybrace",
			"rightcurlybrace",
			"carat",
	};
	private String[] requiredIllegalStartChars = {
			"dollar",
			"twodots", //..
			"tilde",
	}; 

	private HashMap<String,String> illegalKeyValues = null;

	private HashSet<String> illegalPagenames = null;
	
	/**
	 * if true, the @ character will be allowed to remain.
	 * This is useful for links that might have the same syntax
	 * as Confluence's shortcut links.
	 * Currently the only way to set this is to put the following property
	 * in the converter properties file: 
	 * wiki.xxxx.allow-at-in-links.property=true
	 * That property will only affect links. It will not affect pagenames
	 */
	private boolean allowAt = false;
	/**
	 * if true, the ~ character will be allowed to remain.
	 * This is useful for converting to user profile links.
	 * Currently the only way to set this is to put the following property
	 * in the converter properties file: 
	 * wiki.xxxx.allow-tilde-in-links.property=true
	 * That property will only affect links. It will not affect pagenames
	 */
	private boolean allowTilde = false;
	
	/* public methods */
	public IllegalNameConverter() {
		buildIllegalKeyValueMap();
	}
	
	/**
	 * looks to see if the given input is illegal
	 * and returns that input with legal equivalents 
	 * @param input the input to examine for illegal characters
	 * @return input with legal equivalent
	 */
	public String convertIllegalName(String input) {
		//Create map of replacements
		List<IllegalChar> illegalChars = getIllegalCharObjects();
		//Look for illegal characters
		String legal = searchAndReplaceIllegalChars(input, illegalChars);
		//replace illegal characters using replacement map
		return legal;
	}

	/* private or protected - Helper methods */
	
	/**
	 * creates the key-value map of illegal characters
	 */
	private void buildIllegalKeyValueMap() {
		illegalKeyValues = new HashMap<String, String>();
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "colon" + ILLEGALCHAR_PROP_SUFFIX, ":");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "semicolon" + ILLEGALCHAR_PROP_SUFFIX, ";");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "lessthan" + ILLEGALCHAR_PROP_SUFFIX, "<");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "greaterthan" + ILLEGALCHAR_PROP_SUFFIX, ">");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "at" + ILLEGALCHAR_PROP_SUFFIX, "@");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "forwardslash" + ILLEGALCHAR_PROP_SUFFIX, "/");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "backslash" + ILLEGALCHAR_PROP_SUFFIX, "\\");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "pipe" + ILLEGALCHAR_PROP_SUFFIX, "|");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "hash" + ILLEGALCHAR_PROP_SUFFIX, "#");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "leftbracket" + ILLEGALCHAR_PROP_SUFFIX, "[");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "rightbracket" + ILLEGALCHAR_PROP_SUFFIX, "]");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "leftcurlybrace" + ILLEGALCHAR_PROP_SUFFIX, "{");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "rightcurlybrace" + ILLEGALCHAR_PROP_SUFFIX, "}");
		illegalKeyValues.put(ILLEGALCHAR_PROP_PREFIX + "carat" + ILLEGALCHAR_PROP_SUFFIX, "^");
		illegalKeyValues.put(ILLEGALSTART_PROP_PREFIX + "dollar" + ILLEGALCHAR_PROP_SUFFIX, "$");
		illegalKeyValues.put(ILLEGALSTART_PROP_PREFIX + "twodots" + ILLEGALCHAR_PROP_SUFFIX, "..");
		illegalKeyValues.put(ILLEGALSTART_PROP_PREFIX + "tilde" + ILLEGALCHAR_PROP_SUFFIX, "~");

	}

	/**
	 * @return List of IllegalChar objects representing all of the
	 * illegal pagename characters that Confluence forbids.
	 */
	protected List<IllegalChar> getIllegalCharObjects() {
		List<IllegalChar> illegalChars = new Vector<IllegalChar>();

		TreeMap<String, String> properties = getIllegalCharProperties();
		
		illegalChars.addAll(getIllegalAnywhere(properties, requiredIllegalChars));
		illegalChars.addAll(getIllegalStarting(properties, requiredIllegalStartChars));

		return illegalChars;
	}

	/**
	 * @return a map of the illegalchar properties from the 
	 * settings.illegalcharmap.properties file
	 */
	protected TreeMap<String, String> getIllegalCharProperties() {
		String propLocation = PROP_DIR + FILE_SEP + CONFIG_SETTINGS_FILE;
		TreeMap<String, String> properties = null;
		try {
			properties = PropertyFileManager.loadPropertiesFile(propLocation);
		} catch (IOException e) {
			log.error("Problem loading properties file: " + propLocation);
			e.printStackTrace();
		}
		return properties;
	}

	/**
	 * @param properties
	 * @param required
	 * @return
	 */
	protected Vector<IllegalChar> getIllegalAnywhere(
			TreeMap<String, String> properties, String[] required) {
		Vector<IllegalChar> illegals = new Vector<IllegalChar>();
		
		for (int i = 0; i < required.length; i++) {
			String baseKey = required[i];
			if (this.allowAt && "at".equals(baseKey)) continue;
			String key = ILLEGALCHAR_PROP_PREFIX + 
					baseKey + 
					ILLEGALCHAR_PROP_SUFFIX;
			IllegalChar illegal = 
				createIllegalChar(properties, key, IllegalChar.Type.ANYWHERE);
			illegals.add(illegal);
		}
		
		return illegals;
	}

	/**
	 * creates an IllegalChar object for the given key and type,
	 * using the give properties 
	 * @param properties customized properties from the settings.illegalcharmap.properties
	 * @param key a key that is referenced in the given properties
	 * @param type expected IllegalChar type (ANYWHERE, or START_ONLY)
	 * @return an IllegalChar object 
	 */
	protected IllegalChar createIllegalChar(
			TreeMap<String, String> properties, String key, IllegalChar.Type type) {
		String replacement = null;

		if (properties.containsKey(key)) {
			replacement = properties.get(key);
			replacement = (isLegalReplacement(replacement, type))?
					replacement:
					getDefaultReplacement();
		}
		else {
			replacement = getDefaultReplacement();
		}
		
		String value = getIllegalCharValue(key);
		IllegalChar illegal = 
			new IllegalChar(value, replacement, type);
		
		return illegal;
	}

	private String illegalChars = " : ; < > @ / \\ | # [ ] { } ^ ";
	private String illegalStart = " $ .. ~ ";
	/**
	 * determins if a candidate replacement for the given IllegalChar.Type
	 * is a legal replacement
	 * @param replacement candidate replacement
	 * @param type ANYWHERE or START_ONLY
	 * @return true if the replacement is legal
	 */
	protected boolean isLegalReplacement(String replacement, Type type) {
		Pattern oneCharPattern = getReplacementPattern(replacement);
		String input = (type == IllegalChar.Type.START_ONLY)?illegalStart:illegalChars;
		Matcher illegalFinder = oneCharPattern.matcher(input);
		boolean notFound = ! (illegalFinder.find());
		return notFound;
	}

	/**
	 * creates a regex Pattern for finding the given replacement in a string
	 * @param a char or series of chars that could be used as a replacement
	 * for some IllegalChar
	 * @return Pattern with the given replacement character seperated by spaces.
	 * It will be useful for comparing a candidate replacment against
	 * strings illegalChars and illegalStart
	 */
	private Pattern getReplacementPattern(String replacement) {
		String delim = "\\";
		String oneChar = 
				" " +			//a space
				delim + 		//escape the next char
				replacement + 	
				" ";			//a space
		Pattern oneCharPattern = null;
		try {
			oneCharPattern = Pattern.compile(oneChar);
		} catch (PatternSyntaxException e ) { //no backslash necessary
			oneChar = " " + replacement + " "; //try without delim
			oneCharPattern = Pattern.compile(oneChar);
		}
		return oneCharPattern;
	}

	/**
	 * @return the default replacement char. (Used if
	 * a customized replacement char is illegal itsel.f)
	 */
	protected String getDefaultReplacement() {
		return DEFAULT_REPLACEMENT;
	}

	/**
	 * @param key gets the current character value for
	 * a given illegalchar key.
	 * Example: input = illegalchar.colon.replacement
	 * return value = ":", or return the key, if no such value exists
	 * @return value for a given IllegalChar, given a key
	 */
	protected String getIllegalCharValue(String key) {
		String val = this.illegalKeyValues.get(key);
		val = (val == null)?key:val;
		val = ("".equals(val))?key:val;
		return val;
	}

	/**
	 * creates a Vector of IllegalChar objects which describe
	 * those of the START_ONLY type.
	 * @param properties key-value paired properteis from settings.illegalcharmap.properties
	 * @param required array of base keys ("colon", "greaterthan", etc.)
	 * @return Vector of IllegalChar objects
	 */
	protected Vector<IllegalChar> getIllegalStarting(
			TreeMap<String, String> properties, String[] required) {
		Vector<IllegalChar> illegals = new Vector<IllegalChar>();
		
		for (int i = 0; i < required.length; i++) {
			String baseKey = required[i];
			if (this.allowTilde && "tilde".equals(baseKey)) continue;
			String key = ILLEGALSTART_PROP_PREFIX +
					baseKey +
					ILLEGALCHAR_PROP_SUFFIX;
			IllegalChar illegal = 
				createIllegalChar(properties, key, IllegalChar.Type.START_ONLY);
			illegals.add(illegal);
		}
		
		return illegals;
	}

	/**
	 * Examines the given input, decodes and URL entities, and replaces each instance of
	 * and illegal character with its designated replacement
	 * @param input the given input to be searched
	 * @param illegalChars a list of IllegalChar objects representing the chars that must
	 * be replaced
	 * @return the input with all of the proper replacements
	 */
	protected String searchAndReplaceIllegalChars(String input, List<IllegalChar> illegalChars) {
		String legal = input;
		if (shouldUrlDecode()) {
			log.debug("undecoded filename = " + legal); //DELETE
			legal = decodeUrl(legal);
			log.debug("decoded filename = " + legal);
		}
		for (IllegalChar illegal : illegalChars) {
			legal = illegal.getReplacement(legal);
		}
		return legal;
	}

	private boolean shouldUrlDecode() {
		Properties props = getProperties();
		String setting = (String) props.get(URLDECODE_KEY);
		if (setting == null) return false;
		if (setting.equals("true")) return true;
		return false;
	}

	/**
	 * decodes any URL entities in the given input
	 * @param input
	 * @return input with any URL entities decoded.
	 */
	protected String decodeUrl(String input) {
		String encoding = "utf-8";
		if (input == null) {
			log.info("decodeUrl: input is null.");
			return input;
		}
		try {
			/* we have to use the apache decoder, 
			 * as URLDecoder.decode is unreliable. 
			 * See: uwc-4, and 
			 * See: com.atlassian.uwc.converters.mediawiki.DecodeEntities
			 */
			return URIUtil.decode(input, encoding); 
		} catch (URIException e) {
			log.error("Problem with URL decoding:\n" +
					"input = " +input +"\n" +
					"encoding = " + encoding);
			e.printStackTrace();
		}
		return input;
	}

	/**
	 * @return set of illegal pagenames that have been found in the
	 * process of running this converter.
	 */
	public HashSet<String> getIllegalPagenames() {
		return this.illegalPagenames;
	}
	
	/**
	 * @param illegalNames assign set of illegal pagenames
	 * that have been found in a previous conversion.
	 * Useful for finding links to illegal pagenames.
	 */
	public void setIllegalPagenames(HashSet<String> illegalNames) {
		this.illegalPagenames = illegalNames;
	}
	
	/**
	 * adds the given input to the illegal pagenames object
	 * for use in the future. 
	 * Useful for finding links to illegal pagenames.
	 * @param input an illegal pagename
	 */
	public void addIllegalPagename(String input) {
		if (this.illegalPagenames == null)
			this.illegalPagenames = new HashSet<String>();
		this.illegalPagenames.add(input);
	}

	
	private String illegalCharClass = 
		"[" +			//start char class
			":;<>@/|#" +//chars
			"\\[" +		//left bracket, still part of char class
			"\\]" +		//actual right bracket, still part of char class
			"{}^" +		//some more chars
		"]" +			//end char class
		"|" +			//or
		"\\\\";			//a backslash (too many issues with fitting it into the char class)
	private String illegalStartClass = 
		"[" +			//start char class
			"$~" +		//dollar or tilde
		"]" +			//end char class
		"|" +			//or
		"\\.\\.";		//two dots
	Pattern illegalPattern = Pattern.compile(illegalCharClass);
	Pattern illegalStartPattern = Pattern.compile(illegalStartClass);
	Pattern entityPattern = Pattern.compile("%..");
	/**
	 * @param input
	 * @return true if the given input contains illegal characters
	 * or URL entities (which could contain illegal characters)
	 */
	protected boolean illegal(String input) {
		Matcher illegalStarter = illegalStartPattern.matcher(input);
		Matcher illegalFinder = illegalPattern.matcher(input);
		Matcher entityFinder = entityPattern.matcher(input);
		return illegalStarter.lookingAt() || illegalFinder.find() || entityFinder.find();
	}

	public void setAllowAt(boolean allow) {
		this.allowAt = allow;
	}

	public void setAllowTilde(boolean allow) {
		this.allowTilde = allow;
	}
}
