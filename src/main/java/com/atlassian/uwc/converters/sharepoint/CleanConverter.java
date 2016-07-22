package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Cleans raw exported sharepoint output for conversion.
 * This includes:
 * cleaning the non HTML elements out of the file,
 * cleaning up whitespace
 * getting rid of unnecessary tags or syntax like empty span tags,
 * adding quotes to attributes,
 * running the text through JTidy to clean up any unparseable html, etc.
 */
public class CleanConverter extends SharepointConverter {
	private static final String RAWOUTPUT_KEY_CONTENT = "WikiField";
	private static final String RAWOUTPUT_KEY_MODIFIED = "vti_modifiedby";
	public void convert(Page page) {
		log.info("Cleaning Input File");
		String originalText = page.getOriginalText();
		String convertedText = clean(originalText);
		page.setConvertedText(convertedText);
	}
	
	
	/**
	 * cleans the raw sharepoint data file into a (somewhat) valid html file
	 * @param input
	 * @return
	 */
	protected String clean(String input) {
		String cleaned = cleanRaw(input);
		cleaned = cleanWS(cleaned); //we have to do this twice. #1: before trying to handle lists.
		cleaned = cleanLists(cleaned); //has to be before we call JTidy, which can't interpret some of SP's nesting habits
		cleaned = cleanUnclosedAnchorLinks(cleaned); //has to be before we call JTidy.
		cleaned = cleanMsoNormalAtt(cleaned);
		cleaned = cleanEmptySpanTags(cleaned); //has to be before JTidy (or lose ws)
		cleaned = getRootElement(cleaned).asXML(); //this calls JTidy
		cleaned = cleanAttributes(cleaned);
		cleaned = cleanBreaks(cleaned);
		cleaned = cleanAnchors(cleaned);
		cleaned = cleanParaTags(cleaned);
		cleaned = cleanEmptyFontTags(cleaned);
		cleaned = cleanEmptySpaceBetweenClosingTags(cleaned);
		cleaned = cleanDiv(cleaned);
		cleaned = cleanHead(cleaned);
		cleaned = cleanBody(cleaned);
		cleaned = cleanXmlns(cleaned);
		cleaned = revertNL2WS(cleaned); //JTidy sometimes mysteriously turns needed spaces into NL
		cleaned = cleanWS(cleaned); //we have to do this twice. #2: after JTidy
		cleaned = cleanUnderlinedLinks(cleaned);
		return cleaned;
	}
	

	String cleaner = 
		RAWOUTPUT_KEY_CONTENT +	//raw output key for content
		"[^|]*" +				//anything not a pipe
		"\\|" +					//a pipe 
		"(" +					//begin capture (group 1)
			".*" +//?" +				//everything until
		")" +					//end capture (group 1)
		" " +					//a space
		RAWOUTPUT_KEY_MODIFIED; //next delimiter
	Pattern cleanerPattern = Pattern.compile(cleaner, Pattern.DOTALL);
	/**
	 * cleans the raw sharepoint generated output to just contain the
	 * page content 
	 * @param input raw sharepoint generated output representing a page
	 * @return html fragment representing the page content
	 */
	protected String cleanRaw(String input) {
		Matcher cleanerFinder = cleanerPattern.matcher(input);
		if (cleanerFinder.find()) {
			return cleanerFinder.group(1);
		}
		return input;
	}
	
	String tagAttributes = 
		"(?<=" +		//ignore contents of parens (see zero-width positive look behind)
			"<" +		//< character
		")" +			
		"(" +			//begin capture (group 1)
			"[^>]+" +	//anything not a > until
		")" +			//end capture (group 1)
		"(?=" +			//ignore contents of parens (see zero-width positive look ahead)
			">" +		//> character
		")";
	Pattern tagAttsPattern = Pattern.compile(tagAttributes);
	String noQuotes = 
		"(?<=" +		//ignore contents of parens (see zero-width positive look behind)
			"=" +		//= character
		")" +		
		"(" +			//begin capture (group 1)
			"[^\"]" +	//not a quote
			"[^ ]*" +	//not a space until
		")" +			//end capture (group 1)
		"(?=" +			//ignore contents of parens (see zero-width positive look ahead)
			"\\s|$" +	//whitespace or end of input
		")";
	Pattern noQuotesPattern = Pattern.compile(noQuotes);
	/**
	 * cleans the attributes of html tags
	 * @param input
	 * @return all attributes will have quotes
	 * <br/>Example: If input = &lt;tag att=nothing/&gt;
	 * then the return val would be &lt;tag att="nothing"/&gt;
	 */
	protected String cleanAttributes(String input) {
		Matcher tagAttsFinder = tagAttsPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (tagAttsFinder.find()) {
			found = true;
			String atts = tagAttsFinder.group(1);
			Matcher noQuotesFinder = noQuotesPattern.matcher(atts);
			StringBuffer sb2 = new StringBuffer();
			while(noQuotesFinder.find()) {
				String needsQuotes = noQuotesFinder.group(1);
				String replacement = "\"" + needsQuotes + "\"";
				noQuotesFinder.appendReplacement(sb2, replacement);
			}
			noQuotesFinder.appendTail(sb2);
			String replacement = sb2.toString();
			replacement = handleBackslashes(replacement);
			replacement = handleDollarSigns(replacement);
			tagAttsFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			tagAttsFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	String uncleanBreak = "<br([^\\/>]*)>";
	Pattern uncleanBreakPattern = Pattern.compile(uncleanBreak);
	/**
	 * cleans up invalid &lt;br&gt; tags
	 * @param input
	 * @return if the input was &lt;br&gt;, then the output would be
	 * &lt;br/&gt;
	 */
	protected String cleanBreaks(String input) {
		Matcher uncleanBreakFinder = uncleanBreakPattern.matcher(input);
		if (uncleanBreakFinder.find())
			return uncleanBreakFinder.replaceAll("<br/>");
		return input;
	}

	String anchor = 
		"<" +			//opening tag char <
		"a " +			//anchor name 
		".*?" +			//anything until 
		"href=\"#\"" +	//href is internal anchor
		"[^>]*" +		//anything not a > until
		">" +			//closing tag >
		"(?!" +			//don't capture, and look for evidence that the following is not true
			".*?" +		//anything until
			"<\\/?a>" +	//no closing or opening anchor 
		")";
	Pattern anchorPattern = Pattern.compile(anchor);
	/**
	 * cleans any unclosed anchor tags
	 * @param input
	 * @return Example:
	 * if input is &lt;a href="#" name="something"&gt;, then the return
	 * value would be &lt;a href="#" name="something"/&gt;
	 */
	protected String cleanAnchors(String input) {
		Matcher anchorFinder = anchorPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (anchorFinder.find()) {
			found = true;
			String anchor = anchorFinder.group();
			String replacement = anchor;
			if (!anchor.endsWith("/>")) {
				replacement = anchor.substring(0, (anchor.length())-1) + "/>";
			}
			anchorFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			anchorFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	String mungedParas = "<p>(\\s*<p>)";
	Pattern mungedParasPattern = Pattern.compile(mungedParas);
	/**
	 * cleans up inclosed paragraph tags. Note: The likely occurrence
	 * for this is for two opening paragraph tags to be back to back.
	 * HACK ALERT
	 * @param input
	 * @return if the input is &lt;p&gt;&lt;p&gt; then the return value would be
	 * &lt;p/&gt;&lt;p&gt;
	 */
	protected String cleanParaTags(String input) {
		Matcher mungedParasFinder = mungedParasPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (mungedParasFinder.find()) {
			found = true;
			String end = mungedParasFinder.group(1);
			String replacement = "<p/>" + end;
			mungedParasFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			mungedParasFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	/**
	 * cleans divs from the sharepoint html.
	 * Note: The divs in a sharepoint data file are usually unnecessary
	 * HACK ALERT
	 * @param input
	 * @return the input with all div's deleted
	 */
	protected String cleanDiv(String input) {
		Element root = getRootElement(input, false);
		if (root == null) {
			log.error("Can't convert this file.");
			return input;
		}
		return removeTagRecursively(root, "div");
	}




	/**
	 * removes all references to a tag without deleting its child nodes
	 * @param root the dom4j root element containing the entire html doc to be 
	 * transformed
	 * @param tag string representation of the tag to be deleted. Example: div
	 * @return html string with removed elements
	 */
	protected String removeTagRecursively(Element root, String tag) {
		root = transformByRemoving(root, tag); 
		return root.asXML();
	}


	/**
	 * removes any tags with the same name as the given tag from the given
	 * root element, recursively checking the roots children as well.
	 * Children of the removed tag are preserved and re-added to the
	 * root element's tree in the position the removed element was at. 
	 * Also, checks to see if newlines are needed and adds those
	 * as necessary.
	 * @param root
	 * @param tag
	 * @return comparable element with elements of name tag removed
	 */
	private Element transformByRemoving(Element root, String tag) {
		String name = root.getName();
		if (name.equals(tag)) {
			List content = root.content();
			Element parent = root.getParent();
			if (parent == null) parent = new DefaultElement("tmp");
			int index = parent.indexOf(root);
			if (needsNL(content))
				content = addBreak(content);
			parent.remove(root);
			parent.content().addAll(index, content);
			root = parent;
		}
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transformByRemoving(nodeEl, tag);
			}
			if (!running) break;
		}
		return root;
	}


	/**
	 * html tags that implicitly provide newlines.
	 * Used by needsNL method.
	 */
	String[] TAG_HAS_NLS = {
		"p",
		"div",
		"br",
		"ul",
		"ol",
		"li"
	};
	/**
	 * checks to see if the given content list needs a newline.
	 * Used with the cleanDivs feature.
	 * @param content
	 * @return true if: the given content (a) is not empty and
	 * (b) contains one item and it is a Text node, or
	 * (c) none of the Element nodes were of a sort that 
	 * will already be handling NLs (see TAG_HAS_NLS), for example: p, br, ul, etc.
	 */
	protected boolean needsNL(List content) {
		combineTextNodes(content);
		if (content.isEmpty()) return false;
		Object firstItem = content.get(0);
		if (content.size() == 1) { 
			if (firstItem instanceof Text) return true; //just one text node
		}
		Object item = null;
		int index;
		for (index = 0; index < content.size(); index++) { //get the first Element node
			item = content.get(index);
			if (item instanceof Element) break;
		}
		if (item instanceof Element) {	//assuming we found an Element node
			Element el = (Element) item;
			String name = el.getName();
			for (int i = 0; i < TAG_HAS_NLS.length; i++) {
				String tag = TAG_HAS_NLS[i];
				String nextname = "";
				for (int j = index+1; j < content.size(); j++) { //get the next Element node
					Object next = content.get(j);
					if (next instanceof Text) continue;
					Element nextEl = (Element) next;
					nextname = nextEl.getName();
				}
				//check to see if either this or the next node handles the break implicitly
				if (tag.equals(name) || tag.equals(nextname)) return false;
			} 
			return true; //we had Elements, but they were inline
		}
		return false; //we had no Elements, default is return false
	}


	/**
	 * adds a break element to the end of the given content list
	 * @param content
	 * @return content with new break element
	 */
	protected List addBreak(List content) {
		Element el = new DefaultElement("br");
		content.add(el);
		return content;
	}


	/**
	 * cleans whitespace from input.
	 * @param input
	 * @return Replaces all text versions of newlines (\r and \n)
	 * with their appropriate actual newline.
	 * Removes refs to \r
	 * Limits long lists of newlines to two newlines.
	 * Example: if the input is "\\r\\n\\r\\n\\r\\n  \\r\\n", the
	 * return value would be "\n\n"
	 */
	protected String cleanWS(String input) {
		String cleaned = input;
		cleaned = cleaned.replaceAll("\\\\r", "\r");
		cleaned = cleaned.replaceAll("\\\\n", "\n");
		cleaned = cleaned.replaceAll("\r", "");
		cleaned = cleaned.replaceAll("&nbsp;", " ");
		cleaned = cleaned.replaceAll("\n\\s+", "\n");
		cleaned = cleaned.replaceAll(">\\s*\n\\s*(?=\\S)", ">");
		cleaned = cleaned.replaceAll("(?<=\\S)\\s*\n\\s*<", "<");
		cleaned = cleaned.replaceAll("\n\\s*", " ");
		cleaned = cleaned.replaceAll("[^\\s\\p{Graph}]", " ");
		return cleaned;
	}

	static String headTag = "<head.*?<\\/head>";
	static Pattern headPattern = Pattern.compile(headTag, Pattern.DOTALL);
	/**
	 * removes head tags and their child elements from the given input
	 * @param input
	 * @return
	 */
	protected static String cleanHead(String input) {
		Matcher headFinder = headPattern.matcher(input);
		if (headFinder.find()) {
			return headFinder.replaceAll("");
		}
		return input;
	}

	static String bodyTag = "<\\/?body[^>]*>";
	static Pattern bodyPattern = Pattern.compile(bodyTag);
	/**
	 * removes body tags from the given input (but not child tags)
	 * @param input
	 * @return
	 */
	protected static String cleanBody(String input) {
		Matcher bodyFinder = bodyPattern.matcher(input);
		if (bodyFinder.find()) {
			return bodyFinder.replaceAll("");
		}
		return input;
	}
	
	/**
	 * cleans up unnecessary tags added by JTidy (head, meta, body, xmlns attributes).
	 * NOTE: This method is used by the Test classes. 
	 * The reason we do this is that getRootElement must call JTidy to validate 
	 * and fix the inevitably broken html. However, it insists on adding head and body
	 * tags. On top of that, but in order to keep dom4j 
	 * from complaining, we have to output xml, which means that dom4j
	 * will add xmlns attributes in all sorts of inappropriate places.
	 * This method combines all the cleanup so that we can keep the
	 * test cases logical and useful.  
	 * @param input
	 * @return
	 */
	static protected String cleanJTidyExtras(String input) {
		String cleaned = cleanHead(input);
		cleaned = cleanBody(cleaned);
		cleaned = cleanXmlns(cleaned);
		return cleaned;
	}

	/**
	 * removes xlmns attributes from the given input
	 * @param input
	 * @return
	 */
	static protected String cleanXmlns(String input) {
		return input.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
	}

	Pattern listPattern = Pattern.compile("(?<=<([ou])l>)(.*)(?=<\\/\\1l>)");
	Pattern tagPattern = Pattern.compile("" +
			"(" +
				"<\\/li>" +
				"|" +
				"<[ou]l>" +
			")" +
			"(?!" +
				"<li>" +
				"|" +
				"<\\/[ou]l>" +
			")" +
			"(?=.)" //something not the end of the string, but keep zero-width
	);
	/**
	 * fixes Sharepoint list syntax that would confuse JTidy:
	 * adding &lt;li/&gt; tags in front of ambiguous list items nests
	 * @param input
	 * @return
	 */
	protected String cleanLists(String input) {
		Matcher listFinder = listPattern.matcher(input);
		boolean found = false;
		StringBuffer sb = new StringBuffer();
		while (listFinder.find()) {
			found = true;
			String contents = listFinder.group(2);
			Matcher tagFinder = tagPattern.matcher(contents);
			StringBuffer sb2 = new StringBuffer();
			boolean found2 = false;
			while (tagFinder.find()) {
				found2 = true;
				String tag = tagFinder.group(1);
				String replacement2 = tag + "<li/>";
				tagFinder.appendReplacement(sb2, replacement2);
			}
			if (found2) {
				tagFinder.appendTail(sb2);
				String replacement = sb2.toString();
				listFinder.appendReplacement(sb, replacement);
			}
		}
		if (found) {
			listFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	/**
	 * fixes Sharepoint list syntax in the given root element 
	 * that would confuse JTidy
	 * @param root
	 * @return
	 */
	protected Element cleanLists(Element root) {
		String name = root.getName();
		if (Pattern.matches("li", name)) { //fix list items with multiple list children
			List content = root.content();
			boolean first = true;
			for (int i = 0; i < content.size(); i++) {
				Node node = (Node) content.get(i);
				if (node instanceof Text) continue;
				if (node instanceof Element) {
					String nodename = node.getName();
					if (nodename != null && Pattern.matches("[ou]l", nodename)) {
						if (first) {
							first = false;
							continue;
						}
						Element childListItem = new DefaultElement("li");
						childListItem.content().add(node);
						int index = i;
						root.content().remove(index);
						root.content().add(index, childListItem);
						childListItem.setParent(root); 
						node.setParent(childListItem);
					}
				}
			}
		}
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				nodeEl = cleanLists(nodeEl);
			}
		}
		return root;
	}

	/**
	 * removes MsoNormal classes from the given input
	 * @param input
	 * @return
	 */
	protected String cleanMsoNormalAtt(String input) {
		return input.replaceAll("class=MsoNormal", "");
	}

	//has to be before JTidy, because the whitespace will be lost when it transforms it to <span/>
	String emptySpanTagsWithWS = "<span[^>]*>(\\s+)<\\/span>";
	Pattern emptySpanTagsWithWSPattern = Pattern.compile(emptySpanTagsWithWS, Pattern.CASE_INSENSITIVE);
	/**
	 * removes empty span tags or span tags containing only whitespace.
	 * In the latter case, the whitespace is maintained
	 * @param input
	 * @return
	 */
	protected String cleanEmptySpanTags(String input) {
		Matcher emptySpanTagsWithWSFinder = emptySpanTagsWithWSPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (emptySpanTagsWithWSFinder.find()) {
			found = true;
			String ws = emptySpanTagsWithWSFinder.group(1);
			emptySpanTagsWithWSFinder.appendReplacement(sb, ws);
		}
		if (found) {
			emptySpanTagsWithWSFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	String underlinedLinks = 
		"<u>" +						//underline syntax
		"(" +						//begin capturing group 1
			"(?:" +					
				"(?:<br\\/>)" +		//optional line break
				"|" +				//or
				"(?:<b>)" +			//bold tag
			")?" +					
			"<a[^>]*>" +			//link
			".*?" +					//...
			"<\\/a>" +				//close link
			"(?:" +					
				"(?:<br\\/>)" +		//optional line break 
				"|" +				//or
				"(?:<\\/b>)" +		//bold tag
			")?" +					
		")" +
		"<\\/u>";					//underline tag
	Pattern ulLinksPattern = Pattern.compile(underlinedLinks);
	/**
	 * removes surrounding underline tags from link syntax
	 * @param input
	 * @return
	 */
	protected String cleanUnderlinedLinks(String input) {
		Matcher ulLinksFinder = ulLinksPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (ulLinksFinder.find()) {
			found = true;
			String link = ulLinksFinder.group(1);
			link = handleBackslashes(link);
			link = handleDollarSigns(link);
			ulLinksFinder.appendReplacement(sb, link);
		}
		if (found) {
			ulLinksFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern emptyFontTag = Pattern.compile("" +
			"<font[^/>]*\\/>");
	/**
	 * remove empty font tags
	 * @param input
	 * @return
	 */
	protected String cleanEmptyFontTags(String input) {
		Matcher emptyFontFinder = emptyFontTag.matcher(input);
		if (emptyFontFinder.find()) {
			return emptyFontFinder.replaceAll("");
		}
		return input;
	}

	Pattern emptySpaceBetweenClosing = Pattern.compile("" +
			"(<\\/[^>]*>)" +
			"\\s+" +
			"(<\\/[^>]*>)"
			);
	/**
	 * removes whitespace seperating closing tags.
	 * Example: '&lt;/b&gt;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/span&gt;'
	 * becomes '&lt;/b&gt;&lt;/span&gt;'
	 * @param input
	 * @return
	 */
	protected String cleanEmptySpaceBetweenClosingTags(String input) {
		Matcher emptySpaceFinder = emptySpaceBetweenClosing.matcher(input);
		return RegexUtil.loopRegex(emptySpaceFinder, input, "{group1}{group2}");
	}


	Pattern nl2ws = Pattern.compile("" +
			"(?<!" +						//0-width negative lookbehind
				"(\\s)" +					//not ws
				"|" +						//or
				"(<br( xmlns=\"\")?\\/>)" + //explicit break
				"|" +						//or 
				"<html>" +					//beginning of file
			")" +
			"\n" +							//newline
			"(?=" +							//followed by 0-width pos lookahead
				"<" +						//open tag <
				"(?:strong)" +				//strong 
				"|" +						//or
				"(?:b)" +					//b
				"|" +						//or
				"(?:em)" +					//em
				"|" +						//or
				"(?:i)" +					//i
				"|" +						//or
				"(?:u)" +					//u
				"|" +						//or
				"(?:span\\/)" +				//empty span
				"[^>]*" +					
				">" +
				"\\S" +						//followed by non-ws
			")"
	);
	/**
	 * Reverts necessary whitespace that JTidy had converted to newlines. 
	 * Sometimes JTidy turns perfectly good spaces into newlines.
	 * But then the newlines are erased, and whitespace seperating
	 * inline elements like bold and span is then missing. 
	 * See CleanConverterTest.testPreserveInlineWS for an example.
	 * @param input
	 * @return
	 */
	protected String revertNL2WS(String input) {
		Matcher nl2wsFinder = nl2ws.matcher(input);
		if (nl2wsFinder.find()) {
			return nl2wsFinder.replaceAll(" ");
		}
		return input;
	}
}
