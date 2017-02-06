package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint Header syntax (font tags with simple size attributes and
 * header elements) to Confluence header syntax (h#. some text)
 */
public class HeaderConverter extends SharepointConverter {

	private static final String FONTTAG = "font";

	public void convert(Page page) {
		log.info("Converting Header Syntax");
		String input = page.getOriginalText();
		String converted = convertHeaders(input);
		page.setConvertedText(converted);

	}

	/**
	 * converts Sharepoint header syntax to confluence header syntax
	 * @param input
	 * @return
	 */
	protected String convertHeaders(String input) {
		input = switchStyleAndFontPositions(input);
		Element root = getRootElement(input, false);
		Element changed = transformTags(root);
		ColorConverter helper = new ColorConverter();
		helper.disallowNestingColor(changed);
		String xml = toString(changed);
		xml = fixColorTags(xml);
		xml = noWsBeforeHeaderSyntax(xml);
		return xml;
	}

	Pattern headerTagPattern = Pattern.compile("h\\d");
	/**
	 * transforms font tags with simple size attributes 
	 * (Example: size="2") and header tags to Confluence
	 * header syntax
	 * @param root 
	 * @return
	 */
	private Element transformTags(Element root) {
		//look for font tags
		String name = root.getName();
		if (FONTTAG.equals(name)) { //<font size=\"5\">
			String size = root.attributeValue("size");
			log.debug("size = " + size);
			String confSize = transformSize(size);
			List contentList = root.content();
			Element parent;
			if (confSize != null && shouldTransform(root)) {
				List replace = createReplaceList(root, confSize);
				root = setNewParentContent(root, replace);
			}
			else {
				parent = updateParentContentList(root, contentList);
				root = parent;
			}
		}
		else if (Pattern.matches("h\\d", name)) { //<h5>
			String size = name.substring(1);
			List replace = createReplaceList(root, size);
			root = setNewParentContent(root, replace);
		}
		//look for children
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transformTags(nodeEl);
			}
		}
		return root;
	}
	
	Pattern stylePattern = Pattern.compile("[*+_]+");
	Pattern nlPattern = Pattern.compile("[^\n]$");
	/**
	 * creates the content list that will replace the given el.
	 * This content list will maintain the given el's content
	 * while introducing the Confluence syntax for a header of
	 * the given confSize. Newlines before and after will be
	 * added as necessary
	 * @param el
	 * @param confSize
	 * @return
	 */
	protected List createReplaceList(Element el, String confSize) {
		cleanHeaderNL(el);
		//add confSize text element to beginning of content list
		String headerString = "h" + confSize + ". ";
		Text header = new DefaultText(headerString);
		el.content().add(0, header);
		
		if (needsNLAfter(el)) {
			Text nl = new DefaultText("\n");
			el.content().add(nl);
		}
		if (needsNLBefore(el)) {
			Text nl = new DefaultText("\n");
			el.content().add(0, nl);
		}
		
		//return contentlist
		return el.content();
	}
	

	/**
	 * cleans this element and it's children of unnecessary
	 * newlines
	 * @param el
	 */
	private void cleanHeaderNL(Element el) {
		combineTextNodes(el);
		List content = el.content();
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (node instanceof Text) {
				Text textnode = (Text) node;
				String text = textnode.getText();
				text = cleanHeaderNL(text);
				textnode.setText(text);
			}
			else if (node instanceof Element) {
				Element nodeEl = (Element) node;
				cleanHeaderNL(nodeEl);
			}
		}
	}
	


	/**
	 * removes unnecessary newlines from the given input
	 * @param input
	 * @return either returns input with all newlines stripped except
	 * final newlines if they can be found.
	 */
	private String cleanHeaderNL(String input) {
		String noNewlines = input.replaceAll("\n", "");
		Matcher nlMatcher = Pattern.compile("(\n+)$").matcher(input);
		if (nlMatcher.find())
			input = noNewlines + nlMatcher.group(1);
		else input = noNewlines;
		return input;
	}

	/**
	 * @param el
	 * @return true if given header el needs newline after it:
	 * if it does not already end with a newline,
	 * if the following element is a font tag with it's own size (header) attribute,
	 * if the following element is a confluence header,
	 * if the following element does not start with a newline
	 */
	protected boolean needsNLAfter(Element el) {
		String contentString = toString(el.content());
		if (contentString.endsWith("\n")) return false;
		if (el.getParent() != null) {
			Element parent = el.getParent();
			int index = parent.content().indexOf(el);
			//forwards
			while (parent.content().size() > index + 1) {
				Node node = (Node) parent.content().get(index + 1);
				String text = node.asXML();
				if (Pattern.matches("[*+_]+", text) || 
						Pattern.matches("\\{color\\}", text) ||
						Pattern.matches("[ ]+", text) ||
						Pattern.matches("<span\\/>", text)
						) {
					index++;
					continue;
				}
				if (Pattern.matches("\n+.*", text)) return false;
				if (Pattern.matches("<font[^>]*size[^>]*>.*", text)) return true;
				if (Pattern.matches("h\\d\\. .*", text)) return true;
				return true;
			}
		}
		return false;
	}

	/**
	 * @param el
	 * @return true if given header el requires a newline before it:
	 * if it isn't preceded by a newline
	 */
	protected boolean needsNLBefore(Element el) {
		if (el.getParent() != null) {
			Element parent = el.getParent();
			int index = parent.content().indexOf(el);
			index--;
			//forwards
			if (index > -1) {
				Node node = (Node) parent.content().get(index);
				String text = node.asXML();
				//strip out styles
				text = text.replaceAll("[_+*]", "");
				text = text.replaceAll("\\{color[^}]*\\}", "");
				//examine
				if ("".equals(text)) return false;
 				if (Pattern.matches(".*\\s*\n\\s*", text)) return false;
 				return true;
			}
		}
		return false;
	}


	String allowedNewlines = "^\n?[^\n]*\n?(\\{color[^}]*\\}\n?[^\n]*)*$";
	Pattern allowedNewlinesPattern = Pattern.compile(allowedNewlines);
	/**
	 * @param el
	 * @return true if should transform to confluence header syntax
	 */
	protected boolean shouldTransform(Element el) {
		//don't try to transform empty tags
		if (el.content().isEmpty()) return false;
		
		//no newlines unless they happen to come just after color tags
		String xml = toString(el.content());
		Matcher allowedNewlinesFinder = allowedNewlinesPattern.matcher(xml);
		
		//check for prior newline
		Element parent = el.getParent();
		boolean startsLine = true;
		boolean trumpsStartLine = false;
		if (parent != null) {
			int index = parent.content().indexOf(el);
			if (--index >=0) {
				Node sibling = (Node) parent.content().get(index);
				String siblingXml = sibling.asXML();
				String inline = "[*_+]|\\{color[^}]*\\}";
				String nlWillBeAdded = "<\\/[ou]l>";
				if (siblingXml.matches(inline)||
						siblingXml.endsWith(nlWillBeAdded)) { 
					if (--index < 0) {
						trumpsStartLine = true;
						startsLine = true;
					}
					else {
						Node previousSibling = (Node) parent.content().get(index);
						String previousXml = previousSibling.asXML();
						siblingXml = previousXml;
					}
				}
				if (!trumpsStartLine) {
					String regex = ".*[\n ]*\n[\n ]*" + //some pattern of newlines
							"[*_+]?" +					//opt bold, emph, or underline
							"(\\{color:[^}]*\\})?$";	//optional {color:xxx} tag
					startsLine = Pattern.matches(regex, siblingXml) ||
								 Pattern.matches(".*" + nlWillBeAdded + "$", siblingXml);
				}			}
		}
		
		//make sure it doesn't contain any more font tags with size attributes
		Pattern noFontTagsPattern = Pattern.compile("<font[^>]+size");
		Matcher noFontTagsFinder = noFontTagsPattern.matcher(xml);
		boolean noFontTags = !noFontTagsFinder.find();
		
		//disallow more panel tags
		boolean noPanelTags = !xml.contains("{panel");

		return allowedNewlinesFinder.find() &&
				startsLine &&
				noPanelTags &&
				noFontTags;
	}

	/**
	 * transforms size attribute of font tags (size=3) to
	 * appropriate confluence header size
	 * @param size
	 * @return
	 */
	protected String transformSize(String size) {
		if (size == null) return null;
		if (Pattern.matches("\\d+", size)) {
			int i = Integer.parseInt(size);
			if (i > 6) return "1";
			if (i < 3) return "6";
			switch (i) {
				case 6: return "2";
				case 5: return "3";
				case 4: return "4";
				case 3: return "5";
			}
		}
		return null;
	}

	
	/**
	 * fixes mixed up syntaxes.
	 * Example: in = {color:red}\nh1. in red{color} will be changed to
	 * \nh1. {color:red}in red{color}
	 * @param input
	 * @return
	 */
	protected String fixColorTags(String input) {
		//example: {color:red}\nh1. in red {color}
		String output = fixTwoLineColorTags(input);
		
		//example: \n{color:red} h1. in red {color}
		output = fixSameLineColorTags(output);
		
		//example: \nh1. {color}{color:red} in red{color}{color:blue}
		output = fixNestedColorTags(output);
		
		//list items
		//example: <li class="MsoNormal">{color:black}h6.
		output = fixListItemColorTags(output);
		return output;
	}
	
	String fixableColorTags = 
		"(\\{color:[^}]+\\})" +	//group 1
		"(\\s*\n)" +			//group 2
		"(h\\d\\. )" +			//group 3
		"([^\n]+\\s*" +			//group...
		"\\{color\\})";			//...4
	Pattern fixableColorPattern = Pattern.compile(fixableColorTags);
	

	/**
	 * fixes header/color syntax mashups that need to be on the same line.
	 * Example: input = {color:red}\nh1. in red {color}
	 * output = \nh1. {color:red}in red {color}
	 * @param input
	 * @return
	 */
	private String fixTwoLineColorTags(String input) {
		Matcher fixableColorFinder = fixableColorPattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		String output = input;
		boolean found = false;
		while (fixableColorFinder.find()) {
			found = true;
			String colorStart = fixableColorFinder.group(1);
			String ws = fixableColorFinder.group(2);
			String headerStart = fixableColorFinder.group(3);
			String header = fixableColorFinder.group(4);
			header = header.replaceAll("\n", "");
			
			String replacement = ws + headerStart + colorStart + header;
			fixableColorFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			fixableColorFinder.appendTail(sb);
			output = sb.toString();
		}
		return output;
	}

	String sameLineFixColorTags = 
		"(" +						//group...
			"^|" +
			"\n|" +
			"(?<!\n)<li[^>]*>" +
		")" +						//1
		"(" +						//group...
			"[*_+]?" +				
			"\\{color:[^}]+\\}" +	
		")" + 						//2
		"\\s*" +
		"(h\\d\\. )";				//group 3
	Pattern sameLineFixColorPattern = Pattern.compile(sameLineFixColorTags);
	/**
	 * fixes mixed up color/header syntax on the same line
	 * Example: input = \n{color:red} h1. in red {color}
	 * output = \nh1. {color:red}in red {color}
	 * @param input
	 * @return
	 */
	private String fixSameLineColorTags(String input) {
		StringBuffer sb;
		boolean found;
		Matcher sameLineFixColorFinder = sameLineFixColorPattern.matcher(input);
		sb = new StringBuffer();
		found = false;
		while (sameLineFixColorFinder.find()) {
			found = true;
			String pre = sameLineFixColorFinder.group(1);
			String colorStart = sameLineFixColorFinder.group(2);
			String header = sameLineFixColorFinder.group(3);
			String replacement = pre + header + colorStart;
			sameLineFixColorFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			sameLineFixColorFinder.appendTail(sb);
			input = sb.toString();
		}
		return input;
	}

	String nestingFixColorTags = 
		"(?<=^|\n)" +
		"(h\\d\\. )" + 			//group 1
		"(\\{color\\})" +		//group 2
		"\\s*" +	
		"(\\{color:[^}]+\\}" + 	//group...
		"\\s*" +
		"[^\n]+\\s*" +			//...
		"\\{color\\})" +		//...4
		"(\\{color:[^}]+\\})"; 	//group 5			

	Pattern nestingFixColorPattern = Pattern.compile(nestingFixColorTags);

	/**
	 * fixes nested color tags
	 * Example: input = \nh1. {color}{color:red} in red{color}{color:blue}
	 * output = {color}\nh1. {color:red} in red{color}\n{color:blue}
	 * @param input
	 * @return
	 */
	private String fixNestedColorTags(String input) {
		StringBuffer sb;
		boolean found;
		Matcher nestingFixColorFinder = nestingFixColorPattern.matcher(input);
		sb = new StringBuffer();
		found = false;
		while (nestingFixColorFinder.find()) {
			found = true;
			String header = nestingFixColorFinder.group(1);
			String firstEndColor = nestingFixColorFinder.group(2);
			String contents = nestingFixColorFinder.group(3);
			String lastStartColor = nestingFixColorFinder.group(4);
			String replacement = firstEndColor + "\n" + header + contents + "\n" + lastStartColor;
			nestingFixColorFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			nestingFixColorFinder.appendTail(sb);
			input = sb.toString();
		}
		return input;
	}


	//example: <li class="MsoNormal">{color:black}h6.
	String listitemColorTags = 
			"(<li[^>]*>)" +			//group 1		
			"\\s*" +	
			"(\\{color:[^}]+\\})" + 	//group 2
			"(h\\d\\. )" + 			//group 3
			"([^\n]+?)" +			//group 4
			"(\\{color\\})" +		//group 5
			"";
	
	Pattern listitemFixColorPattern = Pattern.compile(listitemColorTags);
	/**
	 * fixes mashed up list items, colors and headers.
	 * Example: input = <li>{color:black}h6. abc {color}
	 * output = <li>h6. {color:black}abc {color}
	 * @param input
	 * @return
	 */
	private String fixListItemColorTags(String input) {
		StringBuffer sb;
		boolean found;
		Matcher listitemFixColorFinder = listitemFixColorPattern.matcher(input);
		sb = new StringBuffer();
		found = false;
		while (listitemFixColorFinder.find()) {
			found = true;
			String listitem = listitemFixColorFinder.group(1);
			String startColor = listitemFixColorFinder.group(2);
			String header = listitemFixColorFinder.group(3);
			String contents = listitemFixColorFinder.group(4);
			String endColor = listitemFixColorFinder.group(5);
			String replacement = listitem + header + startColor + contents + endColor;
			listitemFixColorFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			listitemFixColorFinder.appendTail(sb);
			input = sb.toString();
		}
		return input;
	}

	String styleAndFontTags = 
		"([_*+]+(?:<span>)?(?:\\{color[^}]*\\})?)" +	//group 1		
		"\\s*" +	
		"(<font[^>]*>)" + 	//group 2
		"(.*?)" +			//group 3
		"(<\\/font>)" + 	//group 4
		"((?:\\{color\\})?(?:<\\/span>)?[_*+]+)" +		//group 5
		"";

	Pattern styleAndFontTagsPattern = Pattern.compile(styleAndFontTags);
	/**
	 * switches style and font tags.
	 * Example: input = +{color:red}&lt;font size=2&gt;Something&lt;/font&gt;{color}+
	 * output = &lt;font size=2&gt;+{color:red}Something{color}+&lt;/font&gt;
	 * @param input
	 * @return
	 */
	protected String switchStyleAndFontPositions(String input) {
		StringBuffer sb;
		boolean found;
		Matcher styleAndFontTagsFinder = styleAndFontTagsPattern.matcher(input);
		sb = new StringBuffer();
		found = false;
		while (styleAndFontTagsFinder.find()) {
			found = true;
			String startStyle = styleAndFontTagsFinder.group(1);
			String startFont = styleAndFontTagsFinder.group(2);
			String contents = styleAndFontTagsFinder.group(3);
			String endFont = styleAndFontTagsFinder.group(4);
			String endStyle = styleAndFontTagsFinder.group(5);
			String replacement = startFont + startStyle + contents + endStyle + endFont;
			styleAndFontTagsFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			styleAndFontTagsFinder.appendTail(sb);
			input = sb.toString();
		}
		
		return input;
	}


	Pattern wsBeforeHeader = Pattern.compile("(?<=^|\n) +(?=h\\d\\.)");
	/**
	 * removes non-newline whitespace before the header syntax.
	 * Example: input = \n   h2. Something
	 * output = \nh2. Something
	 * @param input
	 * @return
	 */
	protected String noWsBeforeHeaderSyntax(String input) {
		Matcher wsBeforeHeaderFinder = wsBeforeHeader.matcher(input);
		if (wsBeforeHeaderFinder.find()) {
			return wsBeforeHeaderFinder.replaceAll("");
		}
		return input;
	}

}
