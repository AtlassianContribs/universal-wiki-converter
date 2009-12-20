package com.atlassian.uwc.converters.sharepoint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.ContentListFacade;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.exporters.SharepointExporter;
import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint Link syntax to Confluence link syntax
 */
public class LinkConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Link Syntax");
		String input = page.getOriginalText();
		String converted = convertLinks(input);
		page.setConvertedText(converted);
	}

	/**
	 * converts link syntax.
	 * @param input
	 * @return
	 */
	protected String convertLinks(String input) {
		Element root = getRootElement(input, false);
		//look the through the elements, and make changes
		Element changed = transformLinks(root);
		//turn elements back into a string
		String converted = toString(changed);
		//disallow newlines in Confluence link syntax
		converted = removeNewlinesInLinks(converted);
		converted = addWSAsNecessary(converted);
		return converted;
	}


	/**
	 * transforms instances of sharepoint link syntax with 
	 * Confluence link syntax
	 * @param root
	 * @return
	 */
	protected Element transformLinks(Element root) {
		//get replacement string
		String search = "a";
		List replace = null;
		while ((replace = getNextReplacement(root)) != null) {
			boolean all = false;
			root = transform(root, search, replace, all);
		}
		return root;
	}

	/**
	 * looks for the next link element that will need to be replaced
	 * and creates its replacement text 
	 * @param root
	 * @return replacement text for next link element in need of
	 * replacing
	 */
	protected List getNextReplacement(Element root) {
		String search = "a"; //looking for links
		running = true;
		if (root.getName().equals(search)) {
			String href = root.attributeValue("href");
			String aname = root.attributeValue("name");
			for (Object node : root.content()) {
				if (node instanceof Element) {
					Element el = (Element) node;
					String name = el.getName();
					//extract font tags to outside the link
					if ("font".equals(name)) {
						extractElement(el);
					}
					//allow img tags
					else if ("img".equals(name)) {
						//don't do anything here, just leave it alone
					}
					//remove other tags (styles like bold should already have been handled)
					else {
						root = setNewParentContent(el, el.content());
					}
				}
			}
			running = false;
			return createLink(href, root.content(), aname);
		}
		List rootContent = root.content();
		List next = null;
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				List nextReplacement = getNextReplacement(nodeEl);
				if (nextReplacement != null) {
					if (next == null) 
						next = new ContentListFacade((DefaultElement) node, nextReplacement);
					else
						next.addAll(nextReplacement);
				}
			}
			if (!running) break;
		}
		return next;
	}

	/**
	 * switch this element place with it's parent, leaving all other
	 * children for both elements unmoved within the overall structure of the tree.
	 * Example: if el is &lt;C&gt;<br/>
	 * and the tree structure looks like &lt;a&gt;&lt;B&gt;&lt;C&gt;xxx&lt;/C&gt;&lt;/B&gt;&lt;/a&gt;<br/>
	 * that tree becomes: &lt;a&gt;&lt;C&gt;&lt;B&gt;xxx&lt;/B&gt;&lt;/C&gt;&lt;/a&gt;
	 * @param el
	 */
	protected void extractElement(Element el) {
		Element parent = el.getParent();
		if (parent == null) {
			log.debug("parent is null. Can't extract element");
			return;
		}
		Element gparent = parent.getParent();
		if (gparent == null) {
			log.debug("gparent is null. Can't extract element");
			return;
		}
		//el's content becomes parent's at the point el was at
		//gparent's content becomes el at the point parent was at
		//example: <a><B><C>xxx</C></B></a>
		//becomes: <a><C><B>xxx</B></C></a>
		int indexofEl = parent.content().indexOf(el);
		int indexofParent = gparent.content().indexOf(parent);
		parent.content().remove(el); //a removes font
		parent.content().addAll(indexofEl, el.content()); // a adds font's content
		gparent.content().remove(parent); //html removes a
		el.content().clear(); //remove font's content
		gparent.content().add(indexofParent, el); //attach font at point of a
		el.content().add(parent);
	}

	/**
	 * type of link
	 */
	public enum Link { 
		/**
		 * external link. Example: http://something.com
		 */
		EXTERNAL,
		/**
		 * internal link. Example: [Some Page]
		 */
		WIKIPAGE,
		/**
		 * link to attachment
		 */
		ATTACHMENT,
		/**
		 * internal anchor definition. Example: {anchor:name}
		 */
		ANCHOR,
		/**
		 * link to anchor. Example: [#name]
		 */
		TO_ANCHOR, 
		/**
		 * email. Example: mailto:somebody@some.com
		 */
		EMAIL,
	}
	/**
	 * creates Confluence link.
	 * @param href location of website or file. If href does not start with
	 * http, then it will be assumed that the href refers to the 
	 * sharepoint location listed in the attachment directory field of the UWC
	 * @param alias
	 * @param aname name of link, used for anchors
	 * @return 
	 */
	protected List createLink(String href, List alias, String aname) {
		Link type = getLinkType(href, aname);
		switch(type) {
		case ATTACHMENT:
			href = getAttachmentLink(href);
			break;
		case ANCHOR:
			Text anchor = getAnchorLink(aname);
			Vector v = new Vector();//can't create ContentListFacade with Text nodes
			v.add(anchor);
			return v;
		case WIKIPAGE:
			href = getWikipageLink(href);
			break; //FIXME
		case TO_ANCHOR: //XXX Nothing to do here
		case EXTERNAL:  //XXX Nothing to do here
		case EMAIL: 	//XXX Nothing to do here
			break; 
		}
		return createLinkContentList(href, alias, type);
	}
	
	//convenience method for testing
	/**
	 * creates link string.
	 * Note: this is a convenience method used by test classes.
	 * @param href
	 * @param alias
	 * @param aname
	 * @return
	 */
	protected String createLink(String href, String alias, String aname) {
		Text aliasText = new DefaultText(alias);
		List aliasList = new Vector();
		aliasList.add(aliasText);
		List outList = createLink(href, aliasList, aname);
		return LinkConverterTest.concatList(outList);
	}
	
	Pattern hrefparts = Pattern.compile("" +
			"([^:]+:)?" +	//optional spacekey (not a colon until a colon)
			"([^#]+)" +		//pagename, not including internal anchors (everything up to a #)
			"(#.*)?"		//optional internal anchor
			);
	/**
	 * creates a link as a list of content, using the given href, 
	 * a list of content which will be the alias, and the type of link.
	 * @param href url for link
	 * @param alias List of content that will be used as the alias.
	 * @param type type of link (external, email, etc.)
	 * @return
	 */
	protected List createLinkContentList(String href, List alias, Link type) {
		//remove bad chars from pagename part of href
		Matcher hrefpartsFinder = hrefparts.matcher(href);
		if (!href.startsWith("http:") && //not external link 
				!href.startsWith("mailto") && //not a link
				!href.startsWith("/") && //not probably a file
				hrefpartsFinder.matches()) {
			String space = hrefpartsFinder.group(1);
			String pagename = hrefpartsFinder.group(2);
			String copy = pagename;
			pagename = SharepointExporter.removeBadTitleChars(pagename);
			if (!copy.equals(pagename)) {
				log.warn("Removing bad chars from link. '" + copy + "' became '" + pagename + "'");
			}
			href = space + pagename;
		}
		//remove NL from alias
		alias = cleanAliasNodes(alias, type);
		//construct link around alias
		Text leftBracket = new DefaultText("[");
		Text hrefText = new DefaultText("|" + href + "]");
		alias.add(0, leftBracket);
		alias.add(hrefText);
		return alias;
	}

	/**
	 * cleans up the alias list content.
	 * @param alias list of alias content
	 * @param type type of link
	 * @return
	 */
	private List cleanAliasNodes(List alias, Link type) {
		combineTextNodes(alias);
		for (int i = 0; i < alias.size(); i++) {
			Node node = (Node) alias.get(i);
			if (node instanceof Text) {
				String text = node.getText();
				text = text.trim();
				if (type != Link.EMAIL && type != Link.EXTERNAL) 
					text = SharepointExporter.removeBadTitleChars(text);
				node.setText(text);
			}
			else if (node instanceof Element) {
				Element element = ((Element)node);
				cleanAliasNodes(element.content(), type);
				if (!element.getName().equals("img")) {
					alias.addAll(element.content());
					alias.remove(element);
					i--;
				}
			}
		}
		return alias;
	}
	
	/**
	 * determines link type with given href and anchor name (aname).
	 * @param href url of link
	 * @param aname name of internal anchor or null or empty (""), if no
	 * such internal anchor name exists
	 * @return 
	 */
	protected Link getLinkType(String href, String aname) {
		if (href == null) href = "#";
		if (href.startsWith("http")) return Link.EXTERNAL;
		if (href.startsWith("mailto")) return Link.EMAIL;
		if (href.startsWith("#")) {
			if (href.equals("#") && aname != null && !"".equals(aname)) return Link.ANCHOR;
			return Link.TO_ANCHOR;
		}
		if (href.endsWith("aspx")) return Link.WIKIPAGE;
		return Link.ATTACHMENT;
	}

	/**
	 * gets complete url to attachment, given relative url for link.
	 * Note: complete url will be determined with the attachment directory
	 * @param input relative url for attachment. Example: /test%20wiki/myfile.doc
	 * @return 
	 */
	protected String getAttachmentLink(String input) {
		String attdir = getAttachmentDirectory();
		if (attdir == null) attdir = "";
		if (attdir.endsWith("/")) attdir = attdir.replaceFirst("\\/$", "");
		if (input.startsWith("/")) input = input.replaceFirst("^\\/", "");
		return attdir + "/" + input;
	}

	/**
	 * creates anchor definition macro with given anchor name
	 * @param aname
	 * @return example, if aname = 'abc', then return value will be
	 * {anchor:abc}
	 */
	protected Text getAnchorLink(String aname) {
		String anchorString = "{anchor:" + aname + "}";
		Text anchor = new DefaultText(anchorString);
		return anchor;
	}

	/**
	 * creates link to internal wiki page, given relative wikipage name
	 * @param input Example: "test%20wiki/My%20Page"
	 * @return For the above input example, the return value would be something like:
	 * testwiki2:My Page (assuming the permutations file associates "test wiki" with testwiki2)
	 */
	protected String getWikipageLink(String input) {
		//XXX What space happens with subsites?
		String[] parts = input.split("/");
		String pagename = parts[parts.length-1];
		String space = input.replaceFirst("\\Q" + pagename + "\\E$", "");
		pagename = pagename.replaceFirst("\\.aspx", "");
		pagename = decode(pagename);
		space = decode(space);
		space = space.replaceAll("\\/", "");
		space = getExportedSpacePermutation(space);
		return space + ":" + pagename;
	}

	/**
	 * decodes entities to utf-8 char encoding
	 * @param input
	 * @return If input is test%20wiki, then the return value would be
	 * test wiki
	 */
	protected String decode(String input) {
		String encoding = "utf-8";
		try {
			return URIUtil.decode(input, encoding);
		} catch (URIException e) {
			log.error("Problem decoding with charset: " + encoding);
			e.printStackTrace();
		}
		return input;
	}

	/**
	 * gets sharepoint/spacekey permutation as defined in tmp.permutations.sharepoint.properties
	 * where the given space acts as the key.
	 * 
	 * @param space
	 * @return
	 */
	private String getExportedSpacePermutation(String space) {
		HashMap<String, String> permutations = getPermutationsMap();
		
		//get the permutation
		if (permutations.containsKey(space))
			return permutations.get(space);
		return space;
	}

	HashMap<String, String> permutations = null;
	/**
	 * @return gets a mapping of sharepoint wiki names (keys) to appropriate
	 * and valid confluence space keys (values). Use the sharepoint wiki name
	 * as a key to get the chosen space key. These values are already uses as directories
	 * for the exported pages. See SharepointExporter
	 */
	private HashMap<String, String> getPermutationsMap() {
		if (permutations == null) {
			//load the permutations file (created by the SharepointExporter), and create a map
			String path = "conf/tmp.permutations.sharepoint.properties";
			String line;

			permutations = new HashMap<String, String>();
			String key = null;
			String value = null;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
				while ((line = reader.readLine()) != null) {
					if (value != null) {
						permutations.put(key, value);
						value = null;
					}
					String[] lineparts = line.split(":");
					if (lineparts[0].equals("Key")) key = lineparts[1];
					else value = lineparts[1];
				}
				if (value != null) {
					permutations.put(key, value);
					value = null;
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return permutations;
	}


	
	String confLink = 
		"(?<=\\[)" +	//zero-width left bracket '['
		"([^\\]]+)" +	//everything before right bracket
		"(?=\\])";		//zero-width right bracket ']'
	Pattern confLinkPattern = Pattern.compile(confLink);
	/**
	 * removes newlines from link syntax.
	 * @param input
	 * @return
	 */
	protected String removeNewlinesInLinks(String input) {
		Matcher confLinkFinder = confLinkPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (confLinkFinder.find()) {
			found = true;
			String linkContents = confLinkFinder.group(1);
			if (!linkContents.contains("\n")) continue;
			String replacement = linkContents.replaceAll("\n", " ");
			confLinkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			confLinkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}


	Pattern missingWS = Pattern.compile("" +
			"(\\S(?:<\\/font>)|(?:<\\/?span>))(\\[)"
			);
	/**
	 * fixes missing whitespace issues.
	 * Note: Sometimes links end up with no whitespace between them
	 * and previous text. This is for that problem. 
	 * @param input
	 * @return
	 */
	protected String addWSAsNecessary(String input) {
		Matcher missingWSFinder = missingWS.matcher(input);
		String replacement = "{group1} {group2}";
		return RegexUtil.loopRegex(missingWSFinder, input, replacement);
	}
	
}
