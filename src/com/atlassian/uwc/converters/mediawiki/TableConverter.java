package com.atlassian.uwc.converters.mediawiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Mediawiki to Confluence Table Syntax Converter
 * NOTE: This is a lossy conversion. Lost data will include:
 * - styles
 * - borders
 * - row/colspan. Confluence can't do spans at all, so the conversion will do it's best to maintain 
 *   the table data, but the placement of said data will probably be off.
 * - tables within a table. Confluence can't do that, so the inner table formatting will be stripped out. 
 * 	 Data will be preserved, but again, the placement will probably be off.
 * @author Laura Kolker
 *
 */
public class TableConverter extends BaseConverter {
	
	private static final String TOKEN_NL = "~UWCTOKENNEWLINE~";
	private static final String CAPTION_PARAMS = "|borderStyle=dashed|borderColor=#ccc|bgColor=#fff";
	private static final String DEFAULT_OUTPUT = "confluence";
	private static final String PROP_OUTPUT = "tableoutput";
	
	//debugger objects
	public Logger log = Logger.getLogger(this.getClass());
	private Type type; //output type

	//CONSTANTS - tokens
	protected static final String HEADER_DELIM = "||";
	protected static final String CELL_DELIM = "|";
	protected static final String LIST_DELIM = "\n";
	//CONSTANTS - regex patterns
	private static Pattern captionPattern = Pattern.compile("\\|\\+\\s*([^\n]+)");  // |+(everything after)\n
	private static Pattern lines = Pattern.compile("(.*)\\n");						// (everything up to)\n	
	private static Pattern headerPattern = Pattern.compile("!(.*)");				// !(everything after)
	private static Pattern newrowPattern = Pattern.compile("\\|-.*");				// |-
	private static Pattern dataPattern = Pattern.compile("[!|]{0,2}([^\n|]+)");		// stuff between ! and | and \n
	private static String beginTable = "(\\{\\s*\\|)";								//{|
	private static String endTable = "(\\|\\s*\\})";								//|}
	private static String markCaption = "(\\|\\+)";									//|+
	private static Pattern innertable = Pattern.compile(beginTable + "|" + endTable + "|" +markCaption);
	private static Pattern attribute = Pattern.compile("\\w+=((\"[^\"]*\")|([^\\s\"]+))");
	
	public enum Type {
		CONFLUENCE, 			//default confluence output
		CONTENTFORMATTING;		//content formatting plugin output
		public static Type getType(String type) {
			if ("confluence".equals(type)) return CONFLUENCE;
			if ("contentformatting".equals(type)) return CONTENTFORMATTING;
			return null;
		}
	}
	
	public void convert(Page page) {
		log.debug("Converting Mediawiki Tables -- starting.");
		String text = page.getOriginalText();

		String tableoutput = getProperties().getProperty(PROP_OUTPUT, DEFAULT_OUTPUT);
		this.type = Type.getType(tableoutput);
		
		String converted = "";
		//											 {| (stuff) |}
		Pattern mediawikiTable = Pattern.compile("\\{\\|(.*?)\\|\\}", Pattern.DOTALL);	
		Matcher mediawikiTableFinder = mediawikiTable.matcher(text);
		StringBuffer sb  = new StringBuffer();
		while (mediawikiTableFinder.find()) {
			String tableinnards = mediawikiTableFinder.group(1);
			String replacement = "";
			switch(this.type) {
			case CONFLUENCE: 
				replacement = getReplacement(tableinnards);
				break;
			case CONTENTFORMATTING:
				replacement = getContentFormattingReplacement(tableinnards);
				break;
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			mediawikiTableFinder.appendReplacement(sb, replacement);
		}
		mediawikiTableFinder.appendTail(sb);
		converted = sb.toString();

		//double check that inner table syntax got stripped out
		if (hasInnertable(converted))
			converted = cleanInnertable(converted);

		page.setConvertedText(converted);
		log.debug("Converting Mediawiki Tables -- completed.");
	}

	public enum Context {
		CAPTION,
		HEADER,
		ROW,
		CELL;
		public static Context getContext(String input) {
			if (input.equals("|+")) return CAPTION;
			if (input.equals("!")) return HEADER;
			if (input.equals("|-")) return ROW;
			if (input.equals("|")) return CELL;
			return null;
		}
	}
	
	Pattern row = Pattern.compile("(?<=\n|^)([|!][-+]?)(.*?)($|\n+(?=[|!]))", Pattern.DOTALL);
	protected String getReplacement(String input) {
		input = input.replaceFirst(".*?(?=\n[|!])", "");
		input = input.trim();
		Matcher rowFinder = row.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		String end = "";
		String cToken = "";
		boolean first = true;
		while (rowFinder.find()) {
			found = true;
			String delim = rowFinder.group(1);
			Context context = Context.getContext(delim);
			String content = rowFinder.group(2);
			content = content.replaceAll("(?<=[!|]) +(?=\n)", "");
			content = content.replaceAll("(?<![!|])\n(?![!|])", TOKEN_NL);
			String replacement = "";
			switch (context) {
			case CAPTION:
				if (sb.toString().equals("")) {
					content = cleanMacros(content);
					replacement += "{panel:title="+content+CAPTION_PARAMS + "}\n";
					end += "{panel}";
				}
				else {
					cToken = CELL_DELIM;
					replacement = handleCell(cToken, content, replacement);	
				}
				break;
			case ROW:
				content = content.trim();
				content = content.replaceFirst("^\\Q" + TOKEN_NL + "\\E", "");
				content = content.replaceFirst("\\Q" + TOKEN_NL + "\\E$", "");
				boolean needNL = (!sb.toString().endsWith("\n"));
				if (!first) replacement += cToken + (needNL?"\n":"");
				if ("".equals(content)) break; //otherwise, fall into the CELL handling
			case CELL:
				if ("}".equals(content)) break; //ending an inner table - edge case.
				cToken = CELL_DELIM;
				replacement = handleCell(cToken, content, replacement);
				break;
			case HEADER:
				cToken = HEADER_DELIM;
				replacement = handleCell(cToken, content, replacement);
				break;
			}
			replacement = replacement.replaceAll("\\Q" + TOKEN_NL + "\\E", "\n");
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			rowFinder.appendReplacement(sb, replacement);
			first = false;
		}
		if (found) {
			if (!end.equals("")) end = "\n" + end;
			if (endComplete.matcher(sb.toString()).find()) cToken = "";
			sb.append(cToken + end);
			rowFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	Pattern endComplete = Pattern.compile("[|] *\n+ *$");

	private String getContentFormattingReplacement(String input) {
		String tableparams = getTableParams(input);
		input = input.replaceFirst(".*?(?=\n[|!])", "");
		input = input.trim();
		Matcher rowFinder = row.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		boolean first = true;
		while (rowFinder.find()) {
			found = true;
			String delim = rowFinder.group(1);
			Context context = Context.getContext(delim);
			String content = rowFinder.group(2);
			content = content.trim();
			String replacement = "";
			switch (context) {
//			case CAPTION:
			case ROW:
				if (!first) replacement += "{tr}\n";
				replacement += "{tr}\n";
				first = false;
				break;
			case CELL:
				String params = getCellParams(content);
				content = content.replaceFirst("^[^|]*\\|", "");
				String leftmacro = (!"".equals(params))?"{td:"+params+"}":"{td}"; 
				replacement += leftmacro + content.trim() + "{td}\n";
				break;
			case HEADER:
				params = getCellParams(content);
				content = content.replaceAll("\\s*\\Q||\\E\\s*", "{th}\n{th}");
				content = content.replaceFirst("^[^|]*\\|", "");
				leftmacro = (!"".equals(params))?"{th:"+params+"}":"{th}"; 
				replacement += leftmacro + content.trim() + "{th}\n";
				break;
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			rowFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			rowFinder.appendTail(sb);
			String tableinnards = sb.toString();
			if (!lastrow.matcher(tableinnards).find()) tableinnards += "{tr}\n";
			return "{table" + tableparams +
					"}\n" + tableinnards + "{table}\n";
		}
		return input;
	}
	
	Pattern lastrow = Pattern.compile("\\{tr[^}]*\\}\n?$");

	Pattern tableparams = Pattern.compile(".*?(?=\\n[|!])");
	protected String getTableParams(String input) {
		Matcher paramFinder = tableparams.matcher(input);
		String replacement = "";
		while (paramFinder.find()) {
			String rawparams = paramFinder.group();
			replacement = getConfParams(rawparams);
			break; //only do this once
		}
		if (!"".equals(replacement)) replacement = ":" + replacement;
		return replacement;
	}

	Pattern keyval = Pattern.compile("(\\S+)=\"([^\"]+)\"");
	private String getConfParams(String input) {
		Matcher keyvalFinder = keyval.matcher(input);
		String replacement = "";
		while (keyvalFinder.find()) {
			String key = keyvalFinder.group(1);
			String val = keyvalFinder.group(2);
			if (!"".equals(replacement)) replacement += "|";
			replacement += key + "=" + val;
		}
		return replacement;
	}

	Pattern prepipe = Pattern.compile("^[^|]*");
	protected String getCellParams(String input) {
		Matcher paramFinder = prepipe.matcher(input);
		while (paramFinder.find()) {
			String rawparams = paramFinder.group();
			return getConfParams(rawparams);
		}
		return "";
	}
	private String handleCell(String cToken, String content, String replacement) {
		if (hasInnertable(content))
			content = cleanInnertable(content);
		//cell data?
		Matcher dataFinder = dataPattern.matcher(content);
		while (dataFinder.find()) {
			String data = dataFinder.group(1);
			replacement = addDataSyntax(data, cToken, replacement);
		}
		return replacement;
	}
	
	Pattern itemEnd = Pattern.compile("" +
			"\\|\\s$");
	/**
	 * adds Confluence syntax for one data cell to the growing replacement string
	 * @param cellContents string that will be added as contents of new cell
	 * @param cToken current cell delimiter, either CELL_DELIM or HEADER_DELIM
	 * @param replacement current replacement string to be added to
	 * @return new replacement string, with new cell added
	 */
	private String addDataSyntax(String cellContents, String cToken, String replacement) {
		//strip out attributes (styles, etc.) as Confluence can't use them
		if (isAttribute(cellContents)) 
			return replacement;
		
		//get rid of extraneous whitespace
		cellContents = cellContents.trim();
		
		//prepare for handling list items
		if (isListItem(cellContents)) {
			cellContents = getListContents(cellContents);
		}

		//add the data cell
		if (cToken.endsWith("|")) cToken += " "; //don't add the space for list delims
		if ("".equals(cellContents))
			replacement += cToken;
		else
			replacement += cToken + cellContents + " ";
		
		replacement = replacement.replaceAll("[!]", "|"); //UWC-298
		
		return replacement;
	}

	/**
	 * removes extra opening newlines that could get in the way of
	 * the conversion
	 * @param text mediawiki table, without the delimiters: "{|" and "|}"
	 * @return String, with opening newlines removed.
	 * 
	 * Example:
	 * in = \n|+caption\n!Header1 || Header2
	 * out = |+caption\n!Header1 || Header2
	 */
	protected String removeOpeningNewlines(String text) {
		String cleaned = text;
		
		//                                   pre         post
		Pattern openingNL = Pattern.compile("(\\s*)(\\n+)(.*)", Pattern.DOTALL);
		Matcher openingNLFinder = openingNL.matcher(text);
		if (openingNLFinder.lookingAt()) {
			String pre = openingNLFinder.group(1);
			String post = openingNLFinder.group(3);
			cleaned = pre + post;
		}
		return cleaned;
	}

	/**
	 * removes inner table formatting
	 * @param data string that could contain mediawiki table formatting ("{|", "|}", "|+")
	 * @return that String without the mediawiki table formatting.
	 * 
	 * Example:
	 * in = "{| border=\"0\"\n" +
	 *		"|+_A table in a table_\n" +
	 * out =" border=\"0\"\n" +
	 *		"_A table in a table_\n";
	 */
	private String cleanInnertable(String data) {
		Matcher tableFinder = innertable.matcher(data);
		String cleaned = "";
		if (tableFinder.find()) {
			cleaned = tableFinder.replaceAll("");
		}
		return cleaned;
	}

	/**
	 * determines if innertable formatting is present in the given string
	 * @param text given string
	 * @return true, if innertable formatting is present
	 */
	private boolean hasInnertable(String text) {
		Matcher tableFinder = innertable.matcher(text);
		return tableFinder.find();
	}

	/**
	 * determines if the given string is formatted like an HTML attribute
	 * ex: style="color:red;"
	 * @param text given string
	 * @return true, if the string contains an attribute
	 */
	protected boolean isAttribute(String text) {
		Matcher attributeFinder = attribute.matcher(text);
		return attributeFinder.find();
	}

	Pattern listitem = Pattern.compile("" +
			"(?<=\n|^)([*#]+)(.*)");
	Pattern lastDelim = Pattern.compile("([*#]*)[*#]");
	protected boolean isListItem(String input) {
		Matcher listFinder = listitem.matcher(input.replaceAll("\\Q" + TOKEN_NL + "\\E", "\n"));
		boolean found = false;
		while (listFinder.find()) {
			found = true;
			String delim = listFinder.group(1);
			String content = listFinder.group(2);
			//remove bold syntax
			//Note: we don't really fix the input. 
			//That's handled with a list property external to this class.
			if (content.contains("*")) { 
				content = content.replaceAll("(?<=\\Q" + TOKEN_NL + "\\E)[#*]+", "");
				while (content.contains("*")) {
					content = content.replaceFirst("[*]", "");
					Matcher delimReplacer = lastDelim.matcher(delim);
					if (delimReplacer.find()) delim = delimReplacer.group(1);
				}
				Matcher newListFinder = listitem.matcher(delim+content);
				return newListFinder.lookingAt();
			}
		}
		return found;
	}
	
	Pattern listitemNoWS = Pattern.compile("" +
			"^([*#]+)(\\S.*)$");
	Pattern tokenLINoWS = Pattern.compile("(\\Q" + TOKEN_NL + "\\E[*#]+)(?![#* ])");
	protected String getListContents(String cellContents) {
		Matcher listFinder = listitemNoWS.matcher(cellContents);
		if (listFinder.find()) {
			String delim = listFinder.group(1);
			String content = listFinder.group(2);
			cellContents = delim + " " + content;
		}
		listFinder = tokenLINoWS.matcher(cellContents);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (listFinder.find()) { 
			found = true;
			String replacement = listFinder.group(1) + " ";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			listFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return cellContents;
	}
	
	Pattern macros = Pattern.compile("\\{[^}]+\\}");
	Pattern justrightcurlybrace = Pattern.compile("\\}");
	protected String cleanMacros(String input) {
		Matcher macroFinder = macros.matcher(input);
		input = macroFinder.replaceAll("");
		Matcher curlybraceFinder = justrightcurlybrace.matcher(input);
		input = curlybraceFinder.replaceAll("");
		return input;
	}
}
