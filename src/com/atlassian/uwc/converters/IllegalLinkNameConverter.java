package com.atlassian.uwc.converters;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.converters.twiki.JavaRegexAndTokenizerConverter;
import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles correcting links to illegal pagenames, that would have been changed
 * to something legal by IllegalPageNameConverter
 */
public class IllegalLinkNameConverter extends IllegalNameConverter {

	private static final String PROPKEY_CUSTOMPROTOCOL = "illegalnames-customprotocol";

	/**
	 * delimiter for an alias in links
	 */
	private static final String ALIAS_DELIM = "|";

	public static final String ALLOW_AT_IN_LINKS_KEY = "allow-at-in-links";
	public static final String ALLOW_TILDE_IN_LINKS_KEY = "allow-tilde-in-links";
	
	public void convert(Page page) {
		log.info("Converting Links Referencing Illegal Names - start");
		
		tokenizeCodeBlocks(page);
		//save the tokenized changes
		page.setOriginalText(page.getConvertedText()); 
		
		String input = page.getOriginalText();
		String converted = legalizeLinks(input);
		page.setConvertedText(converted);


		//save the converted text so we can detokenize
		page.setOriginalText(converted); 
		detokenizeCodeBlocks(page);
		
		log.info("Converting Links Referencing Illegal Names - complete");
	}

	String linksPrefix = 		
		"(?<=" +		//zero-width group starts
			"\\[" +		//left bracket
		")";			//end zero-width group


	String linksSuffix =
		"(?=" +			//zero-width group starts
			"\\]" +		//right bracket
		")";			//end zero-width group
	String links =
		linksPrefix +	//left Bracket
		"("+			//start capture (group 1)
			".*?" +		//everything until
			"[^\\\\]" +	//not a backslash (this would mean our closing bracket was escaped
		")" +			//end capture (group 1)
		linksSuffix;	//rightBracket
	
	Pattern linkPattern = Pattern.compile(links);
	
	String brackets = "(?<=^|[^\\\\])(\\[|\\])";
	Pattern bracketPattern = Pattern.compile(brackets);
		
	String protocol = "((https?://)|(mailto:)|(file:)|(ftp:))(.*)";
	Pattern protocolPattern = Pattern.compile(protocol); 
	
	/**
	 * @param input
	 * @return true if the link is an external link (http://somepage.com),
	 * returns false if the link is an internal wiki page
	 */
	public boolean isExternalLink(String input) {
		Matcher protocolFinder;
		if (this.getProperties().containsKey(PROPKEY_CUSTOMPROTOCOL)) {
			try {
				String custom = this.getProperties().getProperty(PROPKEY_CUSTOMPROTOCOL, protocol);
				Pattern customPattern = Pattern.compile(custom);
				protocolFinder = customPattern.matcher(input);
			} catch (RuntimeException e) {
				log.warn("Problem compiling custom protocol with link: " + input + " - Using default protocol.");
				protocolFinder = protocolPattern.matcher(input);
			}
		}
		else 
			protocolFinder = protocolPattern.matcher(input);
		return protocolFinder.lookingAt();
	}
	
	
	/**
	 * transforms any links to illegal pagenames to their
	 * legal counterpart
	 * @param input
	 * @return input with legalized links
	 */
	protected String legalizeLinks(String input) {
		String legal = input;

//		HashSet<String> originalNames = getIllegalPagenames();
//		if (originalNames != null) 
//			legal = legalizeWithState(legal, originalNames); //XXX this handles the right bracket issue, but is v. slow
		
		return legalizeLinksWithoutState(legal);
	} 

	

	/**
	 * uses a list of illegal pagenames that have been found to
	 * legalize the links
	 * @param input confluence syntax with links
	 * @param originalNames unique list of illegal pagenames
	 * @return
	 */
	protected String legalizeWithState(String input, HashSet<String> originalNames) {
		if (originalNames == null) 
			throw new IllegalArgumentException(
					"illegal pagenames object must not be null. Use setIllegalPagenames before calling this method.");
		
		String contentWithLinks = input;
		for (String pagename : originalNames) {
			String replacement = convertIllegalName(pagename);
			String pagenamePattern = createPagenamePattern(pagename);
			replacement = "{group1}" + replacement;
			contentWithLinks = RegexUtil.loopRegex(contentWithLinks, pagenamePattern, replacement);
		}
		
		return contentWithLinks;
	}

	/**
	 * creates a string representing a regex that would find
	 * a link to the given pagename
	 * @param pagename
	 * @return regex for finding links to the given pagename
	 */
	private String createPagenamePattern(String pagename) {
		pagename = Pattern.quote(pagename); //escapes any regex chars
		String pattern = 
			linksPrefix +  	//left bracket
			"(" +			//start capture (group1)
				".*?" +		//anything until
			")" +			//end capture (group 1)
			pagename +		//the pagename
			linksSuffix;	//right bracket
		return pattern;
	}

	/**
	 * transforms the links in the given input such that
	 * any links to illegal pages are transformed to their
	 * legal equivalent
	 * @param input 
	 * @return input with legal links
	 */
	protected String legalizeLinksWithoutState(String input) {
		//Look for links
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		Matcher linkFinder = linkPattern.matcher(input);
		while (linkFinder.find()) {
			if (escaped(input, linkFinder.start(), '\\')) {
				continue;
			}
			found = true;
			String linkContents = linkFinder.group(1);

			//link parts: alias, anchor symbol (if any), link
			String alias = identifyAlias(linkContents);
			if (alias == null) alias = "";
			if (!"".equals(alias)) alias += ALIAS_DELIM;
			String anchor = identifyInPageAnchor(linkContents.replaceFirst("^[^|]+\\|", ""));
			String link = identifyLink(linkContents);
			if (isAttachment(link)) 
				continue;
			String otherAnchor = identifyOtherPageAnchor(link);
			if (!"".equals(otherAnchor)) otherAnchor = "#" + otherAnchor;
			//if theirs another anchor, remove that from the link string
			String pagename =  (!"".equals(otherAnchor))
					?link.substring(0,link.length() - otherAnchor.length())
					:link;
			
			if (!isExternalLink(pagename)) { //if it's external, it can't have an illegal confluence name.
				String space = "";
				if (hasSpace(pagename)) {
					space = identifySpace(pagename);
					pagename = removeSpace(pagename, space);
				}
				//important for syntax like shortcut links 
				this.setAllowAt(allowsAt());
				this.setAllowTilde(allowsTilde());
				pagename = convertIllegalName(pagename); //get rid of the illegal chars here.
				//rebuild with space
				pagename = space + pagename;
			}

			String replacement = alias + anchor + pagename + otherAnchor;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


	private boolean allowsAt() {
		if (this.properties == null) return false;
		if (this.properties.containsKey(ALLOW_AT_IN_LINKS_KEY)) {
			String val = (String) this.properties.get(ALLOW_AT_IN_LINKS_KEY);
			if ("true".equals(val))
				return true;
		}
		return false;
	}

	private boolean allowsTilde() {
		if (this.properties == null) return false;
		if (this.properties.containsKey(ALLOW_TILDE_IN_LINKS_KEY)) {
			String val = (String) this.properties.get(ALLOW_TILDE_IN_LINKS_KEY);
			if ("true".equals(val))
				return true;
		}
		return false;
	}

	protected boolean hasSpace(String pagename) {
		return pagename.contains(":");
	}

	Pattern space = Pattern.compile("" +
			"^([^:]+:)");
	protected String identifySpace(String pagename) {
		Matcher spaceFinder = space.matcher(pagename);
		if (spaceFinder.find()) {
			return spaceFinder.group(1);
		}
		return "";
	}

	protected String removeSpace(String pagename, String space) {
		log.debug("space = '" + space + "' pagename='" + pagename +"'");
		space = "\\Q" + space + "\\E"; 
		return RegexUtil.loopRegex(pagename, space, "");
	}

	/**
	 * @param input string that might have an escaped character
	 * @param index index of character that might be escaped
	 * @param ch character used to escape the character at the given index (probably backslash) 
	 * @return true if the character at the given index of the input string is escaped
	 * by the given character
	 */
	protected boolean escaped(String input, int index, char ch) {
		if (index == 0 || index == 1)
			return false;
		if (input.charAt(index-2) == ch) return true;
		return false;
	}
	
	String alias = 
		"([^|]+)" +	// (group 1) not a pipe until the end or 
		"(\\|.*)?"; // (group 2) optional pipe and then anything until the end 
	Pattern aliasPattern = Pattern.compile(alias);
	/**
	 * figures out the alias content of a confluence link
	 * @param input the link (without brackets). So for example:<br/>
	 * If the Confluence syntax for a link was "[alias|Page Name]", then the input
	 * you would pass would be "alias|Page Name".
	 * @return the alias, for the above example, the return value would be "alias";
	 */
	protected String identifyAlias(String input) {
		Matcher aliasFinder = aliasPattern.matcher(input);
		if (aliasFinder.find()) {
			if (aliasFinder.group(2) == null)
				return ""; //no alias
			return aliasFinder.group(1);
		}
		return input;
	}

	String anchor = 
		"(?:^|\\|)" + //the beginning of the string or a pipe
		"(#)";		  //a hash
	Pattern anchorPattern = Pattern.compile(anchor);
	/**
	 * examines a link, and returns a #, if the link 
	 * has an anchor to a section within this page. For example:<br/>
	 * If a confluence link was like so: "[alias|#anchor]",
	 * then the input would be "alias|#anchor",
	 * and the return value would be #.
	 * However, as only in-page-anchors are found  by this method,
	 * an empty string would be returned if the input was:
	 * alias|OtherPage#anchor
	 * @param input The contents of the link. 
	 * @return A hash (#) symbol, if such an anchor
	 * exists in the given input, or an empty string (""),
	 * if no such anchor exists.
	 */
	protected String identifyInPageAnchor(String input) {
		Matcher anchorFinder = anchorPattern.matcher(input);
		if (anchorFinder.find()) {
			return "#";
		}
		return "";
	}

	String otherAnchor =
		"^" +			//beginning of string
		"[^#]+" +		//not a hash
		"#" +			//one hash
		"(" +			//start capture (group1)
			".*" +		//everything til the end
		")";			//end capture (group1)
	Pattern otherAnchorPattern = Pattern.compile(otherAnchor);
	/**
	 * examines a link, and if it finds a anchor for another page
	 * it returns the anchor. For example:
	 * If a confluence link was "[alias|OtherPage#anchor]", 
	 * then the input would need to be "alias|OtherPage#anchor", 
	 * and the return value would be "#anchor". 
	 * @param input The contents of a confluence link (minus the enclosing brackets)
	 * @return the anchor
	 */
	protected String identifyOtherPageAnchor(String input) {
		Matcher anchorFinder = otherAnchorPattern.matcher(input);
		if (anchorFinder.find()) {
			return anchorFinder.group(1); 
		}
		return "";
	}
	
	String linkContent = 
		"[^#|]" + //not a hash or a pipe
		"[^|]*" + //not a pipe until
		"$";	  //the end of the string
	Pattern linkContentPattern = Pattern.compile(linkContent);
	/**
	 * gets the link to the page, for a given confluence link.
	 * For Example<br/>
	 * If a Confluence link was "[alias|page#anchor]",
	 * The input would be alias|page#anchor, and the return value would be
	 * page#anchor.
	 * <br/>
	 * If a Confluence link was "[#anchor]", 
	 * then the input would be "#anchor", and the return value would be "anchor".
	 * If a Confluence link was "[^attachment1.gif]", the input would be
	 * "^attachment1.gif, and the return value would be "^attachment.gif"
	 * @param input confluence link minus the brackets
	 * @return the page name the link is for
	 */
	protected String identifyLink(String input) {
		Matcher linkFinder = linkContentPattern.matcher(input);
		if (linkFinder.find()) {
			return linkFinder.group();
		}
		return input;
	}

	/**
	 * @param input a confluence link, minus such extraneous details
	 * as aliases, and the anchors of other pagenames
	 * For example,<br/>
	 * if input = "^attachment.gif", then it return true.
	 * if input = "pagename", then it returns false.
	 * @return true if the link references an attachment
	 */
	protected boolean isAttachment(String input) {
		return input.contains("^");
	}


	
	String codeblockTokenizerConverterString = 
			"(" +				//start capture (group 1)
				"\\{" +			//a left brace
					"code" +	//the string "code"
					"[^}]*" +	//anything but a right brace until
				"\\}" +			//a right brace
				"(" +			//start capture (group 2)
					".*?" +		//anything except a newline until
				")" +			//end capture (group 2)
				"\\{" +			//a left brace
					"code" +	//the string "code"
				"\\}" +			//a right brace
			")" +				//end capture (group 1)
			JavaRegexAndTokenizerConverter.REGEX_SEPERATOR_MULTI_LINE + //converter regex replacement trigger 
			"$1";				//replacement
	String noformatblockTokenizerConverterString =  
		"(" +				//start capture (group 1)
			"\\{" +			//a left brace
				"noformat" +	//the string "noformat"
				"[^}]*" +	//anything but a right brace until
			"\\}" +			//a right brace
			"(" +			//start capture (group 2)
				".*?" +		//anything except a newline until
			")" +			//end capture (group 2)
			"\\{" +			//a left brace
				"noformat" +	//the string "noformat"
			"\\}" +			//a right brace
		")" +				//end capture (group 1)
		JavaRegexAndTokenizerConverter.REGEX_SEPERATOR_MULTI_LINE + //converter regex replacement trigger 
		"$1";				//replacement 

	/**
	 * tokenizes any instances of code blocks, so that 
	 * the contents of a code block is not affected by this class
	 * @param page page with code blocks to tokenize
	 * @return page with tokenzied code blocks
	 */
	protected Page tokenizeCodeBlocks(Page page) {
		JavaRegexAndTokenizerConverter codeTokenizer = 
			(JavaRegexAndTokenizerConverter) JavaRegexAndTokenizerConverter.getConverter(
				this.codeblockTokenizerConverterString);
		codeTokenizer.convert(page);
		page.setOriginalText(page.getConvertedText());
		JavaRegexAndTokenizerConverter noformatTokenizer =
			(JavaRegexAndTokenizerConverter) JavaRegexAndTokenizerConverter.getConverter(
				this.noformatblockTokenizerConverterString);
		noformatTokenizer.convert(page);
		return page;
	}
	/**
	 * detokenizes any code block tokens.
	 * @param page page to detokenize
	 * @return detokenized page
	 */
	protected Page detokenizeCodeBlocks(Page page) {
		DetokenizerConverter detokenizer = 
			new DetokenizerConverter();
		detokenizer.convert(page);
		return page;
	}
}
