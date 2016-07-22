package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;

import com.atlassian.uwc.ui.Page;

/**
 * Removes line-break tags from within inline-only tags (like strong)
 */
public class InlineConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Handling Inline Syntax Cases");
		String input = page.getOriginalText();
		String converted = convertInline(input);
		page.setConvertedText(converted);
	}

	/**
	 * tags that should not have linebreaks as child nodes
	 */
	private final String[] INLINE_TAGS_ARRAY = {
			"b",
			"strong",
			"em",
			"i",
			"u"
	};
	/**
	 * Vector version of INLINE_TAGS_ARRAY
	 */
	private Vector<String> INLINE_TAGS; 

	/**
	 * removes instances of linebreaks within inline elements
	 * @param input
	 * @return
	 */
	protected String convertInline(String input) {
		createInlineTagsVector();
		Element root = getRootElement(input, false);
		Element changed = transformInline(root);
		return changed.asXML();
	}

	/**
	 * fills INLINE_TAGS with the contents of INLINE_TAGS_ARRAY
	 */
	private void createInlineTagsVector() {
		INLINE_TAGS = new Vector<String>();
		for (int i = 0; i < INLINE_TAGS_ARRAY.length; i++) {
			String tag = INLINE_TAGS_ARRAY[i];
			INLINE_TAGS.add(tag);
		}
	}

	/**
	 * removes instances of br tags within inline only elements
	 * @param root
	 * @return
	 */
	private Element transformInline(Element root) {
		String name = root.getName();
		if (INLINE_TAGS.contains(name)) {
			transform(root, "br", "", false, true);
			List content = root.content();
			boolean removeRoot = shouldRemoveRoot(content);
			if (removeRoot) {
				root = removeRoot(root);
			}
		}
		//look for children
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				nodeEl = transformInline(nodeEl);
			}
		}
		return root;
	}

	/**
	 * if parent is nonnull, removes root from parent, and returns parent.
	 * @param root
	 * @return
	 */
	private Element removeRoot(Element root) {
		Element parent = root.getParent();
		if (parent != null) {
			parent.remove(root);
			root = parent;
		}
		return root;
	}

	/**
	 * @param content
	 * @return true if should remove root:
	 * if content is empty, or contains only Text with empty strings
	 */
	private boolean shouldRemoveRoot(List content) {
		boolean removeRoot = false;
		if (content.isEmpty()) removeRoot = true;
		else {
			removeRoot = true;
			for (int i = 0; i < content.size(); i++) {
				Node node = (Node) content.get(i);
				if (node instanceof Element) {
					removeRoot = false;
					break;
				}
				if (node instanceof Text) {
					Text textnode = (Text) node;
					if (!"".equals(textnode.getText())) {
						removeRoot = false;
						break;
					}
				}
			}
		}
		return removeRoot;
	}
}
