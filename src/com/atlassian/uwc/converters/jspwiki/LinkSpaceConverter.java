package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * converts Jspwiki link syntax to Confluence links.
 * Note: the basic link syntax is the same. However, Jspwiki
 * has a special way of condensing linknames to a filename, so 
 * that the link does not have to use an alias.
 * In other words: The following links could all link to ThisPage.txt
 * <ul>
 * <li>ThisPage</li>
 * <li>This Page</li>
 * <li>this page</li>
 * </ul>
 * So, this class figures out the real page name, and creates an alias
 * if it's needed to the Confluence link syntax.
 * <br/>
 * Example:
 * input = [this page]
 * output = [this page|ThisPage]
 */
public class LinkSpaceConverter extends JspwikiLinkConverter {

	Logger log = Logger.getLogger(this.getClass());
	public static final String JSPWIKI_EXTS = "extensions";
	
	public void convert(Page page) {
		log.info("Converting Link Spaces - start");
		
		String input = page.getOriginalText();
		String converted = convertLinkSpaces(input);
		page.setConvertedText(converted);
		
		log.info("Converting Link Spaces - complete");
	}
	
	String basicLink = "\\[" +				//left bracket
						"(" +				//start capturing (group1)
							"[^\\]|]+" +
						")" +
						"\\|?" +
						"(" +
							"[^\\]]*" +	
						")" +				//end capturing (group1)
						"\\]";  			//right bracket
	Pattern linkPattern = Pattern.compile(basicLink);

	/**
	 * converts links.
	 * links without spaces will be left alone.
	 * links with spaces will be converted such that the 
	 * original link becomes an alias, and the link is condensed
	 * to have no whitespace, and first letters of words become upper case
	 * @param input example: A link [ab cd]
	 * @return example: A link [AbCd]
	 */
	protected String convertLinkSpaces(String input) {
		
		String pagedir = getPageDir();
		Matcher linkFinder = linkPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		boolean hasAlias = false;
		while (linkFinder.find()) {
			found = true;
			String link = linkFinder.group(2);
			String alias = linkFinder.group(1);
			hasAlias = !("".equals(link));
			link = !hasAlias?alias:link;
			String saved = link;
			if (link.contains(".")) { //external or file
				link = link.trim();
				alias = alias.trim();
				if (isImage(link)) continue;
			}
			if (pagedir == null) { //use best guess
				link = convertCaps(link);
				link = convertSpaces(link);
				link = convertQuestions(link);
				link = convertParens(link);
				if (link.equals(convertCaps(saved))) continue;
			}
			else {
				link = getPagename(pagedir, link);
				if (link.equals(saved)) continue;
			}
			String replacement = "";
			if (!hasAlias && link.equals(saved.trim())) 
				replacement = "[" + link + "]";	
			else 
				replacement = "[" + alias + "|" + link + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	protected String getPagename(String pagedir, String link) {
		File dir = new File(pagedir);
		String[] files = getPageFiles(dir);
		for (String name : files) {
			name = name.replaceFirst(".txt$", "");
			String orig = name;
			name = name.replaceAll("[+]", "");
			link = link.replaceAll("[+]", "");
			link = convertSpaces(link);
			link = convertParens(link);
			if (name.toLowerCase().equals(link.toLowerCase())) {
				return orig.replaceAll("[+]", " ");
			}
		}
		return link;
	}

	Pattern imagePattern = Pattern.compile("" +
			"(..)?" +		//2 chars preceding
			"([^\\/]+)" +	//not slash
			"(\\.....?)$");	//. and 3-4 chars
	protected boolean isImage(String link) {
		if (link.contains("Image src")) return true;
		Vector<String> extensions = getImageExtensions();
		boolean b = false;
		Matcher imageFinder = imagePattern.matcher(link);
		if (imageFinder.find()) {
			String pre = imageFinder.group(1);
			String ext = imageFinder.group(3);
			String extname = ext.replaceAll("[.]", "");
			if (extensions != null) return extensions.contains(extname);
			return ((pre == null) || (!pre.equals("//") &&
					!ext.contains(".htm"))); //not preceded by double backslashes
		}
		return b;
	}

	
	Vector<String> extensions = null;
	Pattern extPattern = Pattern.compile("[^,]+");
	private Vector<String> getImageExtensions() {
		if (extensions != null) return extensions;
		String extensionsString = this.getProperties().getProperty(JSPWIKI_EXTS, null);
		if (extensionsString == null) return null;
		extensions = new Vector<String>();
		Matcher extFinder = extPattern.matcher(extensionsString);
		boolean found = false;
		while (extFinder.find()) {
			found = true;
			String ext = extFinder.group();
			extensions.add(ext);
		}
		if (found) return extensions;
		return null;
	}

	String firstLetter = "(?<=^| )" +		//zero-width beginning of string or space
						"(\\w)" +			//one word character (group 1)
						"(?!ttps?:\\/\\/)";	//zero-width but not a http protocol
	Pattern firstPattern = Pattern.compile(firstLetter);
	Pattern fileProtocol = Pattern.compile("^file://", Pattern.CASE_INSENSITIVE);
	/**
	 * makes the first letter in a word uppercase
	 * @param input example: "ab cd"
	 * @return example: "Ab Cd"
	 */
	protected String convertCaps(String input) {
		//ignore inputs starting with file://
		if (fileProtocol.matcher(input).lookingAt()) return input;
		//examine everything else
		Matcher firstFinder = firstPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (firstFinder.find()) {
			found = true;
			String first = firstFinder.group(1);
			String replacement = first.toUpperCase();
			firstFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			firstFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * removes spaces
	 * @param input example: "a b"
	 * @return example: "ab";
	 */
	protected String convertSpaces(String input) {
		return input.replaceAll(" ", "");
	}
	
	/**
	 * removes parens
	 * @param input example: "(a)b"
	 * @return example: "ab";
	 */
	protected String convertParens(String input) {
		return input.replaceAll("[()]", "");
	}
	
	/**
	 * removes question marks
	 * @param input
	 * @return
	 */
	protected String convertQuestions(String input) {
		if (!input.startsWith("http"))	
			return input.replaceAll("[?]", "");
		return input;
	}
}
