package com.atlassian.uwc.converters.xwiki;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

/**
 * Converts xwiki link syntax to confluence link syntax
 */
public class LinkConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Links -- starting");
		String input = page.getOriginalText();
		String converted = convertLinks(input);
		page.setConvertedText(converted);
		log.info("Converting Links -- complete");
	}

	Pattern xwikiLink = Pattern.compile("" +
			"\\[" +
			"([^\\]]+)" +
			"\\]");
	Pattern target = Pattern.compile("" +
			"(.*)" +
			"\\|_([^|]+)$"
			);
	/**
	 * converts xwiki link syntax to confluence link syntax
	 * @param input
	 * @return
	 */
	protected String convertLinks(String input) {
		Matcher xwikiLinkFinder = xwikiLink.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (xwikiLinkFinder.find()) {
			found = true;
			//contents is all of the parts of an xwiki link, including alias, etc.
			String contents = xwikiLinkFinder.group(1);
			//all '>' delimiters become '|' delimiter
			contents = contents.replaceAll(">", "|");
			contents = fixSpaceSyntax(contents);	
			contents = fixAnchors(contents, input);
			Matcher targetFinder = target.matcher(contents);
			String type = null;
			//check for target parameters
			if (targetFinder.find()) { 
				contents = targetFinder.group(1); //get rid of target parameter
				type = targetFinder.group(2);	  //target type could be blank, self, etc.
			}
			//blank targets become link-window macros
			if ("blank".equals(type)) 
				contents = getLinkWindowSyntax(contents);
			else //re-add in confluence link brackets to everything else
				contents = "[" + contents + "]";
			contents = RegexUtil.handleEscapesInReplacement(contents);
			xwikiLinkFinder.appendReplacement(sb, contents);
		}
		if (found) {
			xwikiLinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern anchor = Pattern.compile("" +
			"([^#]+)#([^#]+)"
			);
	protected String fixAnchors(String link, String input) {
		Matcher anchorFinder = anchor.matcher(link);
		if (anchorFinder.find()) {
			String nonanchor = anchorFinder.group(1);
			String anchor = anchorFinder.group(2);
			//deal with #H delim
			if (anchor.startsWith("H")) 
				anchor = anchor.substring(1); //remove H of #H delim
			//deal with no-ws'd header
			Vector<String> headers = getHeaders(input);
			if (headers != null) { 
				for (String header : headers) {
					String noWsHeader = header.replaceAll(" ", "");
					if (noWsHeader.equals(anchor)) {
						anchor = header;
						break;
					}
				}
			}
			link = nonanchor + "#" + anchor;
		}
		return link;
	}

	Pattern header = Pattern.compile("" +
			"(?<=^|\n)" +
			"h\\d\\. " +
			"([^\n]+)"
			);
	protected Vector<String> getHeaders(String input) {
		Matcher headerFinder = header.matcher(input);
		Vector<String> headers = new Vector<String>();
		while (headerFinder.find()) {
			headers.add(headerFinder.group(1));
		}
		if (headers.isEmpty())
			return null;
		return headers;
	}

	protected static String fixSpaceSyntax(String contents) {
		//internal space.page delimiter becomes space:page
		if (!isExternalLink(contents))
			contents = contents.replaceAll("\\.", ":");
		//check for instance of both virtual wiki and space. If so, combine them.
		if (contents.indexOf(":") != contents.lastIndexOf(":")) 
			contents = contents.replaceFirst(":", "");
		return contents;
	}
	
	Pattern linkParts = Pattern.compile("" +
			"(?:([^|]+)\\|)?" +	//alias, group 1
			"(.*)"				//link, group2
			);
	/**
	 * @param contents link contents, including alias and link, but not target
	 * @return link syntax as confluence link-window macro
	 */
	private String getLinkWindowSyntax(String contents) {
		Matcher partFinder = linkParts.matcher(contents);
		if (partFinder.find()) {
			String alias = partFinder.group(1);
			String link = partFinder.group(2);
			if (alias == null) alias = link;
			contents = "{link-window:" + link + "}" + alias + "{link-window}";
		}
		return contents;
	}
	
	/**
	 * @param contents
	 * @return true if contents represent an external link
	 */
	private static boolean isExternalLink(String contents) {
		String link = getLink(contents);
		boolean isHttp = link.startsWith("http:");
		boolean isMail = link.startsWith("mailto:");
		return isHttp || isMail;
	}

	/**
	 * @param input contents of an xwiki link, including alias and other parameters
	 * @return just the link part. Example: input = alias|link, return = link
	 */
	protected static String getLink(String input) {
		String[] linkParts = input.split("\\|");
		if (linkParts.length > 1) return linkParts[1];
		if (linkParts.length == 0) return input; //XXX avoids exceptions with weird links
		return linkParts[0];
	}

}
