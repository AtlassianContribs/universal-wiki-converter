package com.atlassian.uwc.converters.jspwiki;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * converts Jspwiki style syntax to Confluence syntax.
 * Styles can include inline css.
 * Example:
 * <br/>
 * Jspwiki:<br/>
 * %%sub This is subscript text %%<br/>
 * Confluence:<br/>
 * ~This is subscript text~
 */
public class StyleConverter extends BaseConverter {
	
	/**
	 * supported conversion styles, or unknown
	 */
	public enum JspwikiStyle { 
		UNKNOWN,
		SUBSCRIPT,
		SUPERSCRIPT,
		STRIKETHROUGH,
		COMMENTBOX,
		INFOBOX,
		WARNINGBOX,
		ERRORBOX,
		UNDERLINE,
		ITALIC,
		COLOR,
		INLINESTYLE
	};
	
	/**
	 * conversions can be summarized into three types,
	 * <ul>
	 * <li>inline conversions - Conversions can be substituted within the same line. example: subscript</li>
	 * <li>multiline conversions - Conversions will span several lines. example: infobox</li>
	 * <li>special conversions - Conversion requires special handling, and has extra parameters. example: inlinestyle</li>
	 * </ul>
	 */
	public enum ConversionType {
		INLINE,
		MULTILINE,
		SPECIAL
	}
	
	/**
	 * hash of jspwiki syntax to this.JspwikiStyle objects. 
	 * initialized in the init method.
	 * <br/>keys: jspwiki style syntax descriptor. Examples: sub, information, small
	 * <br/>vals: JspwikiStyle object. Respectivly: SUBSCRIPT, INFOBOX, UNKNOWN
	 */
	private HashMap<String, JspwikiStyle> stylehash = null;
	
	/**
	 * hash of this.JspwikiStyle object to this.ConversionTypes. 
	 * initialized in the init method.
	 * <br/>keys: JspwikiStyle object. Examples: SUBSCRIPT, INFOBOX, UNKNOWN
	 * <br/>vals: associated ConversionType object. Respectively, INLINE, MULTILINE, SPECIAL
	 */
	private HashMap<JspwikiStyle, ConversionType> typehash = null;
	/**
	 * hash of this.JspwikiStyle objects to associated Confluence syntax delimiters.
	 * initialized in the init method. 
	 * <br/>keys: JspwikiStyle object. Examples: SUBSCRIPT, INFOBOX, UNKNOWN
	 * <br/>vals: associated Confluence syntax delimiter. Respectively: ~, {info}, {panel}
	 */
	private HashMap<JspwikiStyle, String> delimhash = null;
	/**
	 * hash of css properties with associated Confluence panel arguments.
	 * initialized in the init method.
	 * <br/>keys: css property. Examples: background-color, border-style
	 * <br/>vals: associated panel argument. Respectively: bgColor, borderStyle
	 */
	private HashMap<String,String> inlineStyleHash = null;
	
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * initializes necessary class objects. 
	 * If using methods in this class, must call this or the convert method first.
	 */
	protected void init() {
		stylehash = new HashMap<String, JspwikiStyle>();
		stylehash.put("sub", JspwikiStyle.SUBSCRIPT);
		stylehash.put("sup", JspwikiStyle.SUPERSCRIPT);
		stylehash.put("strike", JspwikiStyle.STRIKETHROUGH);
		stylehash.put("commentbox", JspwikiStyle.COMMENTBOX);
		stylehash.put("information", JspwikiStyle.INFOBOX);
		stylehash.put("warning", JspwikiStyle.WARNINGBOX);
		stylehash.put("error", JspwikiStyle.ERRORBOX);
		
		typehash = new HashMap<JspwikiStyle, ConversionType>();
		typehash.put(JspwikiStyle.SUBSCRIPT, ConversionType.INLINE);
		typehash.put(JspwikiStyle.SUPERSCRIPT, ConversionType.INLINE);
		typehash.put(JspwikiStyle.STRIKETHROUGH, ConversionType.INLINE);
		typehash.put(JspwikiStyle.COMMENTBOX, ConversionType.MULTILINE);
		typehash.put(JspwikiStyle.INFOBOX, ConversionType.MULTILINE);
		typehash.put(JspwikiStyle.WARNINGBOX, ConversionType.MULTILINE);
		typehash.put(JspwikiStyle.ERRORBOX, ConversionType.MULTILINE);
		typehash.put(JspwikiStyle.INLINESTYLE, ConversionType.SPECIAL);
		typehash.put(JspwikiStyle.UNDERLINE, ConversionType.SPECIAL);
		typehash.put(JspwikiStyle.ITALIC, ConversionType.SPECIAL);
		typehash.put(JspwikiStyle.COLOR, ConversionType.SPECIAL);
		typehash.put(JspwikiStyle.UNKNOWN, ConversionType.SPECIAL);
		
		delimhash = new HashMap<JspwikiStyle, String>();
		delimhash.put(JspwikiStyle.SUBSCRIPT, "~");
		delimhash.put(JspwikiStyle.SUPERSCRIPT, "^");
		delimhash.put(JspwikiStyle.STRIKETHROUGH, "-");
		delimhash.put(JspwikiStyle.COMMENTBOX, "{panel}");
		delimhash.put(JspwikiStyle.INFOBOX, "{info}");
		delimhash.put(JspwikiStyle.WARNINGBOX, "{note}");
		delimhash.put(JspwikiStyle.ERRORBOX, "{warning}");
		delimhash.put(JspwikiStyle.INLINESTYLE, "{panel}");
		delimhash.put(JspwikiStyle.UNDERLINE, "+");
		delimhash.put(JspwikiStyle.ITALIC, "_");
		delimhash.put(JspwikiStyle.COLOR, "{color}");
		delimhash.put(JspwikiStyle.UNKNOWN, "{panel}");
		
		inlineStyleHash = new HashMap<String, String>();
		inlineStyleHash.put("background-color", "bgColor");
		inlineStyleHash.put("border-style", "borderStyle");
		inlineStyleHash.put("border-width", "borderWidth");
		inlineStyleHash.put("border-color", "borderColor");
	}
	
	public void convert(Page page) {
		log.info("Converting Styles - start");
		init(); //set up necessary objects
		String input = page.getOriginalText();
		String converted = convertStyles(input);
		page.setConvertedText(converted);
		log.info("Converting Styles - complete");
	}
	
	String cssClass = "%%" +//opening percent delimiter
			"\\s*" +		//optional whitespace
			"((?:\\w+)" +	//name of class (group1)
			"|" +			//or
			"(?:\\([^)]+\\)))" + //inline css (group1)
			"\\s*" +		//any whitespace
			"([^%]+)" +		//anything not a percent until (group2)
			"%%";			//closing percent delimiter
	Pattern classPattern = Pattern.compile(cssClass);

	/**
	 * convert jspwiki style syntax to comparable Confluence syntax.
	 * @param input jspwiki style syntax
	 * @return comparable Confluence syntax
	 */
	protected String convertStyles(String input) {
		String converted = input;
		Matcher cssFinder = classPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (cssFinder.find()) {
			found = true;
			String styleRaw = cssFinder.group(1);
			String text = cssFinder.group(2);
			JspwikiStyle style = getStyle(styleRaw);
			ConversionType type = getType(style);
			String replacement = "";
			switch (type) {
			case INLINE:
				replacement = getInline(style, text);
				break;
			case MULTILINE:
				text = text.trim();
				replacement = getMultiline(style, text);
				break;
			case SPECIAL:
				text = text.trim();
				replacement = getSpecial(style, styleRaw, text);
				replacement = handleHeaders(replacement);
			}
			cssFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			cssFinder.appendTail(sb);
			converted = sb.toString();
		}
		return converted;
	}
	
	/**
	 * determines which JspwikiStyle object is associated with the
	 * given input
	 * @param input Jspwiki inline style type, examples:
	 * sub, strike, sup, information, etc.
	 * See <a href="http://jspwiki.org/wiki/JSPWikiStyles">Jspwiki Style Doc</a> for more info.
	 * @return associated JspwikiStyle object
	 */
	protected StyleConverter.JspwikiStyle getStyle(String input) {
		if (input.startsWith("(")) {
			if (input.contains("underline")) return JspwikiStyle.UNDERLINE;
			if (input.contains("italic")) return JspwikiStyle.ITALIC;
			if (input.contains("color") && !input.contains("background-color"))
				return JspwikiStyle.COLOR;
			return JspwikiStyle.INLINESTYLE;
		}
		JspwikiStyle style = this.stylehash.get(input);
		if (style == null) return JspwikiStyle.UNKNOWN;
		return style;
		
	}
	
	/**
	 * determines which ConversionType is associated with the given
	 * JspwikiStyle.
	 * @param style
	 * @return associated ConversionType object
	 */
	protected ConversionType getType(JspwikiStyle style) {
		return this.typehash.get(style);
	}
	
	/**
	 * creates confluence syntax for the given inline JspwikiStyle and text
	 * @param type SUBSCRIPT, SUPERSCRIPT, or STRIKETHROUGH
	 * @param text
	 * @return confluence syntax
	 * @throws IllegalArgumentException, if type is not an inline type
	 */
	protected String getInline(JspwikiStyle type, String text) {
		switch (type) {
		case SUBSCRIPT:
		case SUPERSCRIPT:
		case STRIKETHROUGH:
			String delim = this.delimhash.get(type);
			text = text.trim();
			return delim + text + delim;
		default:
			throwArgError(type);
			return null; //this code is never run
		}
	}

	/**
	 * creates confluence syntax for the given multiline JspwikiStyle
	 * and text.
	 * @param type COMMENTBOX, INFOBOX, WARNINGBOX, ERRORBOX
	 * @param text
	 * @return confluence syntax
	 * @throws IllegalArgumentException, if type is not a multiline type
	 */
	protected String getMultiline(JspwikiStyle type, String text) {
		switch (type) {
		case COMMENTBOX:
		case INFOBOX:
		case WARNINGBOX:
		case ERRORBOX:
			String delim = this.delimhash.get(type);
			String multi = delim + "\n" +
					text + "\n" +
					delim;
			return multi;
		default:
			throwArgError(type);
			return null; //this code is never run
		}
	}
	
	/**
	 * creates confluence syntax for the given special JspwikiStyle
	 * @param type INLINESTYLE or UNKNOWN
	 * @param styleInfo. for INLINESTYLE, this should be the associated css string.
	 * Example: ( background-color:blue; border-color:black )
	 * <br/>
	 * for UNKNOWN, this should be the jspwiki syntax that describes the unsupported style type.
	 * Example: sortable
	 * @param text
	 * @return confluence syntax
	 * @throws IllegalArgumentException, if type is not a Special type
	 */
	protected String getSpecial(JspwikiStyle type, String styleInfo, String text) {
		String delim = this.delimhash.get(type);
		switch (type) {
		case INLINESTYLE:
		case UNDERLINE:
		case ITALIC:
		case COLOR:
			String panelArgs = convertPanelArgs(styleInfo);
//			if (panelArgs.equals("") && type != JspwikiStyle.COLOR) return delim + text + delim;
			String panelWithArgs = "{panel:" + panelArgs + "}\n";
			String colorStarter = (hasColor(styleInfo))?"{color:" + 
					convertColorArgs(styleInfo) + "}":"";
			String colorEnder = (!"".equals(colorStarter)?"{color}":"");
			if (panelArgs.equals("") && type == JspwikiStyle.COLOR) return colorStarter + text + colorEnder; 
			return ("".equals(panelArgs)?"":panelWithArgs) +
				colorStarter +
				((type == JspwikiStyle.UNDERLINE || type == JspwikiStyle.ITALIC)?delim:"") + 
				text +
				((type == JspwikiStyle.UNDERLINE || type == JspwikiStyle.ITALIC)?delim:"") +
				colorEnder + 
				("".equals(panelArgs)?"":"\n{panel}");
		case UNKNOWN:
			return  delim + "\n" +
				text + "\n" +
				"----" + "\n" +
				"Jspwiki style: " + styleInfo + "\n" +
				delim;
		default:
			throwArgError(type);
			return null; //this code is never run
		}
	}
	
	Pattern colorMacroPattern = Pattern.compile("" +
			"(\\{color:[^}]+\\})(.*?)(\\{color\\})", Pattern.DOTALL);
	Pattern headerPattern = Pattern.compile("" +
			"h\\d\\. ");
	protected String handleHeaders(String input) {
		Matcher colorMacroFinder = colorMacroPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (colorMacroFinder.find()) {
			found = true;
			String colorBefore = colorMacroFinder.group(1);
			String contents = colorMacroFinder.group(2);
			String colorAfter = colorMacroFinder.group(3);
			String replacement = "";
			Matcher headerFinder = headerPattern.matcher("\\Q" + contents + "\\E");
			StringBuffer hb = new StringBuffer();
			boolean headerFound = false;
			while (headerFinder.find()) {
				headerFound = true;
				String header = headerFinder.group();
				String headerReplacement = colorAfter + "\n" + header + colorBefore;
				headerFinder.appendReplacement(hb, headerReplacement);
			}
			if (headerFound) {
				headerFinder.appendTail(hb);
				replacement = hb.toString();
			}
			else continue; //if no header just stop with this iteration
			replacement = replacement.replaceAll("\\\\[QE]", "");
			replacement = colorBefore + replacement + colorAfter;
			replacement = replacement.replaceAll("\\Q" +colorBefore+colorAfter+"\\E\n?", "");
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			colorMacroFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			colorMacroFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	String colorString = 	"[(; ]" + 	//either open-paren, semi-colon, or space
							"color:" +  //and then the string "color:"
							"\\s*" +	//optional whitespace
							"([#\\w]+)";//the color (group1)
	Pattern colorPattern = Pattern.compile(colorString);
	/**
	 * determines if given css string includes a color property
	 * @param input css
	 * @return true, if color is present
	 */
	protected boolean hasColor(String input) {
		Matcher colorFinder = colorPattern.matcher(input);
		return colorFinder.find();
	}
	
	/**
	 * gets the value of the css color property
	 * @param input css
	 * @return value of color, if color is present. null, if color is not present.<br/>
	 * Example: blue<br/>
	 * Example: #fff
	 */
	protected String convertColorArgs(String input) {
		Matcher colorFinder = colorPattern.matcher(input);
		if (colorFinder.find()) {
			return colorFinder.group(1);
		}
		return null;
	}

	/**
	 * converts css to valid confluence panel macro arguments.
	 * unsupported css will be ignored and logged.
	 * supports: background-color, border, border-style, border-color, border-width
	 * @param input css string from inline style. <br/>
	 * Example: ( background-color:red;border: 2px dashed black )
	 * @return valid panel arguments String<br/>
	 * Example: bgColor=red|borderWidth=2px|borderStyle=dashed|borderColor=black
	 */
	protected String convertPanelArgs(String input) {
		String panelArgs = "";
		input = input.replaceAll("[()]", "");
		String[] cssStatements = input.split(";");
		boolean first = true;
		for (String statement : cssStatements) {
			statement = statement.trim();
			if ("".equals(statement)) continue;
			if (!first) {
				panelArgs += "|";
			}
			String[] keyVal = statement.split(":");
			String key = keyVal[0];
			String val = keyVal[1];
			key = key.trim();
			val = val.trim();
			String panelKey = inlineStyleHash.get(key);
			String panelArg = "";
			if (panelKey == null && "border".equals(key)) {
				//handle border case
				String[] borderParts = val.split(" ");
				if (borderParts.length < 3) {
					log.info("css border does not have 3 params. Ignoring: " + val);
					continue;
				}
				borderParts = sortBorderParts(borderParts);
				panelArg = "borderWidth=" + borderParts[0] + "|" +
					"borderStyle=" + borderParts[1] + "|" +
					"borderColor=" + borderParts[2];
			}
			else if (key.equals("text-decoration") || key.equals("font-style") || key.equals("color")) {
				continue;
			}
			else if (panelKey == null) {
				//handle unsupported css case, but only log if not color
				if (!statement.startsWith("color:")) 
					log.info("css property not supported. Ignoring: " + statement);
				continue;
			}
			else {
				panelArg = panelKey + "=" + val;
			}
			panelArgs += panelArg;
			first = false;
		}
		if (panelArgs.endsWith("|")) panelArgs = panelArgs.substring(0, panelArgs.length()-1);
		return panelArgs;
	}


	
	/**
	 * throws an {@link IllegalArgumentException}, and
	 * logs which type caused it.
	 * @param type the type that will be logged as causing the exception
	 * 
	 */
	private void throwArgError(JspwikiStyle type) {
		throw new IllegalArgumentException(
				"Type " + type.toString() + 
				" is not a valid inline type. Use " +
				getMethodName(typehash.get(type).toString()) +
				" instead.");
	}
	/**
	 * creates associated getter method string for a given word
	 * @param input Example: INLINE
	 * @return associated getter method string. Example: getInline()
	 */
	protected String getMethodName(String input) {
		String firstChar = input.substring(0, 1);
		String rest = input.substring(1, input.length());
		String method = firstChar.toUpperCase() + rest.toLowerCase();
		return "get" + method + "()";
	}
	
	/**
	 * organizes the css border property values so that the appropriate
	 * confluence panel macro argument (borderWidth, borderStyle, borderColor)
	 * is associated with the appropriate value.
	 * Problems will be logged as an error.
	 * @param parts
	 * @return String array of border property values that are in the following order:
	 * <br/> array[0] is width
	 * <br/> array[1] is style
	 * <br/> array[2] is color
	 */
	protected String[] sortBorderParts(String[] parts) {
		String[] ordered = new String[3];
		//order should be width style color
		for (String part : parts) {
			if (isWidth(part)) ordered[0] = part; 		//width
			else if (isStyle(part)) ordered[1] = part; 	//style
			else ordered[2] = part;						//color
		}
		for (String part : ordered) {
			if (part == null)
				log.error("Some border part did not convert correctly: " + parts.toString());
		}
		return ordered;
	}

	String[] cssWidthChoices = {
			"thin", 
			"medium", 
			"thick", 
			"none"
	};
	String[] cssUnitChoices = {
			"px",
			"em",
			"cm", 
			"in",
			"pt"
	};
	/**
	 * determines if the css border property value is a width value.
	 * Supports: thin, thick, medium, none, and the following units:
	 * ps, em, cm, in, pt
	 * @param input css border property value. Example: thin
	 * @return true, if the value is a border-width value
	 */
	protected boolean isWidth(String input) {
		if (input == null) return false;
 		for (String width : cssWidthChoices) {
			if (input.equals(width)) return true;
		}
		for (String unit : cssUnitChoices) {
			if (input.endsWith(unit)) return true;
		}
		return false;
	}
	
	String[] cssStyleChoices = {
			"style", 
			"none",
			"hidden",
			"dotted",
			"dashed",
			"solid",
			"double",
			"groove",
			"ridge",
			"inset",
			"outset"
	};
	/**
	 * determines if the given css borderp property value is
	 * a style 
	 * @param input css border property value. Example: dashed
	 * @return true, if the input is a style value
	 */
	protected boolean isStyle(String input) {
		for (String style : cssStyleChoices) {
			if (style.equals(input)) return true;
		}
		return false;
	}

}
