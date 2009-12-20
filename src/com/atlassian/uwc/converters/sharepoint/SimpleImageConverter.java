package com.atlassian.uwc.converters.sharepoint;

import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint image syntax to Confluence image syntax. Note:
 * links to Sharepoint images continue to refer to the Sharepoint server.
 * This class does not download or attach images to the Confluence page.
 */
public class SimpleImageConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Image Syntax");
		String input = page.getOriginalText();
		String converted = convertImages(input);
		page.setConvertedText(converted);	}

	/**
	 * converts sharepoint image syntax to confluence image syntax,
	 * using the Sharepoint server to serve the images.
	 * @param input
	 * @return
	 */
	protected String convertImages(String input) {
		Element root = getRootElement(input, false);
		Element changed = transformImages(root);
		return changed.asXML();
	}

	/**
	 * transforms image syntax.
	 * Note: Recursive method
	 * @param root
	 * @return
	 */
	protected Element transformImages(Element root) {
		String name = root.getName();
		if ("img".equals(name)) {
			List<Attribute> atts = root.attributes();
			String src = "";
			for (int i = 0; i < atts.size(); i++) {
				Attribute att = atts.get(i);
				if (att.getName().equals("src")) {
					src = getValue(att);
					atts.remove(att);
					break;
				}
			}
			String attributeString = createAttributeString(atts);
			String replacement = createConfluenceImageString(src, attributeString);
			root = replaceRootInParent(root, replacement);
		}
		//look for children
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transformImages(nodeEl);
			}
		}
		return root;
	}

	/**
	 * gets the value of the given attribute
	 * @param att
	 * @return
	 */
	protected String getValue(Attribute att) {
		return att.getValue();
	}

	/**
	 * creats a confluence image syntax attribute string 
	 * from the given list of attributes
	 * @param atts
	 * @return
	 */
	protected String createAttributeString(List<Attribute> atts) {
		String output = "";
		for (int i = 0; i < atts.size(); i++) {
			if (i > 0) output += ", ";
			Attribute attribute = (Attribute) atts.get(i);
			String name = attribute.getName();
			String value = attribute.getValue();
			output += name + "=\"" + value + "\"";
		}
		return output;
	}

	/**
	 * creates confluence image syntax with the given src and attribute strings
	 * @param src
	 * @param attributeString
	 * @return
	 */
	protected String createConfluenceImageString(String src, String attributeString) {
		if (!src.startsWith("http:")) {
			String dir = getAttachmentDirectory();
			if (dir == null) {
				log.error("Attachment Directory has not been set!");
				dir = "";
			}
			if (dir.endsWith("/")) dir = dir.substring(0, dir.length()-1);
			if (src.startsWith("/")) src = src.substring(1);
			src = dir + "/" + src;
		}
		if (attributeString != null && !"".equals(attributeString))
			attributeString = "|" + attributeString;
		return "!" + src + attributeString + "!";
	}

}
