package com.atlassian.uwc.converters.sharepoint;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

import com.atlassian.uwc.ui.Page;

/**
 * Converts Sharepoint color syntax (both foreground and background) to
 * Confluence color syntax.
 * Foreground color is transformed to {color} macros.
 * Background color is handled with {panel} macros when possible, and
 * ignored otherwise. This class works closely with HeaderConverter and ListConverter
 * in order to handle the myriad edge cases that Sharepoint emits.
 */
public class ColorConverter extends SharepointConverter {

	public void convert(Page page) {
		log.info("Converting Color Syntax");
		String input = page.getOriginalText();
		String converted = convertColor(input);
		page.setConvertedText(converted);
	}
	
	/**
	 * converts background and foreground sharepoin color syntax
	 * to confluence color syntax.
	 * @param input
	 * @return
	 */
	protected String convertColor(String input) {
		Element root = getRootElement(input, false);
		Element changed = transformColors(root);
		switchColorAndBackground(changed);
		combineSamePanels(changed);
		disallowNestingPanel(changed);
		return toString(changed);
	}

	/**
	 * transforms references to foreground and background color
	 * in the given root element and it's children to 
	 * Confluence color syntax
	 * @param root
	 * @return
	 */
	private Element transformColors(Element root) {
		//look through all elements for color or background color attribtues or css
		List<Attribute> atts = root.attributes();
		Color color = null;
		Background bg = null;
		for (int i = 0; i < atts.size(); i++) {
			Attribute att = atts.get(i);
			if (att.getName().equals("color")) { //look for color attributes
				color = getColor(att);
				root.remove(att);
				i--;
			}
			else if (att.getName().equals("style")) { 	//look for style attributes
				if (hasStyleColor(att))					//style attribute has color
					color = getStyleColor(att);
				if (hasStyleBackground(att)) 			//style attribtue has background-color
					bg = getStyleBackground(att);
				root.remove(att);
				i--;
			}
			if (color != null && bg != null) break;
		}
		if (color != null || bg != null ) { //we've identified color attributes. 
			//if the element has data that needs to be maintained, then don't remove the element
			if (shouldSaveElement(root.getName())) {
				transformContentAddChildTextNodes(color, bg, root);
			} 
			else { //otherwise remove the element
				//remove the element and add attribute to this as text child
				transformContentReplaceWithText(color, bg, root); 
			}
		}
		//look at children nodes
		List rootContent = root.content();
		for (int i = 0; i < rootContent.size(); i++) {
			Object node = rootContent.get(i); //could be Text or Element objects
			if (node instanceof Element) {
				Element nodeEl = (Element) node;
				transformColors(nodeEl);
			}
		}
		return root;
	}

	/**
	 * switches color and panel macros, if necessary.
	 * Note: panel macros can contain color macros, but color
	 * macros cannot contain panel macros. 
	 * @param root
	 */
	protected void switchColorAndBackground(Element root) {
		String last = "";
		List content = root.content();
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (!(node instanceof Text)) continue;
			if ("".equals(node.getText().trim()) && 
					i > 0 &&
					content.get(i-1) instanceof Text) {
				Text textnode = (Text) content.get(i-1);
				textnode.setText(textnode.getText() + node.getText());
				content.remove(i);
				i--;
				continue;
			}
			else if (last != null && 
					last.trim().startsWith("{color:") &&
					node.getText().trim().startsWith("{panel:bgColor")) {
				switchNodes(content, i);
			}
			else if (last != null && 
					last.trim().equals("{panel}") && 
					node.getText().trim().equals("{color}")) {
				switchNodes(content, i);
			}
			last = ((Text) node).getText();
		}
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (node instanceof Element)
				switchColorAndBackground((Element) node);
		}
		
	}

	String panelpatternString = "\\{panel([^}]*)\\}";
	Pattern panelpattern = Pattern.compile(panelpatternString);
	/**
	 * combines back to back panels that have the same attributes.
	 * Note: Sharepoint sometimes emits multiple tags with the same
	 * background color attributes back to back, rather than one parent
	 * div/span containing the attribute. 
	 * @param root
	 */
	protected void combineSamePanels(Element root) {
		String lastcolor = "";
		int lastindex = 0;
		List content = root.content();
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (node instanceof Text)  {
				Text textnode = (Text) node;
				String text = textnode.getText();
				String thiscolor = getPanelColor(text);
				if (thiscolor == null) continue;
				if (thiscolor.equals(lastcolor)) {
					for (int j = lastindex + 1; j <= i; j++) {
						Node oldnode = (Node) content.get(j);
						if (!(oldnode instanceof Text)) continue;
						Matcher panelFinder = panelpattern.matcher(oldnode.getText());
						String replacement = oldnode.getText();
						if (panelFinder.find()) {
							replacement = panelFinder.replaceAll("");
						}
						oldnode.setText(replacement);
					}
				}
				
				lastcolor = thiscolor;
				lastindex = i;
			}
		}
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (node instanceof Element)
				combineSamePanels((Element) node);
		}
		
	}

	/**
	 * removes nested panels.
	 * Note: Sharepoint sometimes emits nested tags with background colors.
	 * Converting this nesting is complicated as Confluence
	 * panel tags can't handle nesting. We've decided to simply strip out the 
	 * outer nested panels in these cases.
	 * @param root
	 */
	protected void disallowNestingPanel(Element root) {
		Pattern pattern = panelpattern;
		String tag = "{panel}";
		disallowNesting(root, tag, pattern);
	}
	
	String colorpatternString = "\\{color([^}]*)\\}";
	Pattern colorpattern = Pattern.compile(colorpatternString);
	
	/** 
	 * removes nested color macros.
	 * Note: Sharepoint sometimes emits nested tags with foreground colors.
	 * Converting this nesting is complicated as Confluence color macros
	 * can't handle nesting. We've decided to simply strip out the
	 * outer nested color macros in these cases. 
	 * @param root
	 */
	protected void disallowNestingColor(Element root) {
		Pattern pattern = colorpattern;
		String tag = "{color}";
		combineTextNodes(root);
		disallowNesting(root, tag, pattern);
		
	}

	/**
	 * disallows nesting in the given root element of the given tag, findable 
	 * by the given regex pattern. The outer nested elements will be removed, 
	 * but their contents will be preserved.
	 * @param root element potentially containing nested elements
	 * @param tag name of tag we don't want to nest
	 * @param pattern regex pattern used to find the tag we don't want to nest 
	 */
	protected void disallowNesting(Element root, String tag, Pattern pattern) {
		List content = root.content();
		boolean inTag = false;
		Stack outer = new Stack<String>(); 
		int nests = 0;
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (!(node instanceof Text)) continue;
			Text textnode = (Text) node;
			String text = textnode.getText();
			Matcher finder = pattern.matcher(text);
			StringBuffer sb = new StringBuffer();
			boolean found = false;
			while (finder.find()) {
				String atts = finder.group(1);
				boolean isOpeningTag = !"".equals(atts);
				if (isOpeningTag) outer.push(finder.group());
				if (inTag && isOpeningTag) { //look for nested panels and append closing tags
					found = true;
					String replacement = tag + finder.group();
					finder.appendReplacement(sb, replacement);
					nests++;
				}
				else {
					inTag = !inTag; 
					if (nests > 0) { //but if there were nests, then we need to re-add the parent panel after the child
						found = true;
						outer.pop(); //get rid of last one
						String replacement = finder.group() + outer.pop();
						finder.appendReplacement(sb, replacement);
						inTag = !inTag; 
						nests--;
					}
				}
			}
			if (found) {
				finder.appendTail(sb);
				textnode.setText(sb.toString());
			}
			
		}
		for (int i = 0; i < content.size(); i++) {
			Node node = (Node) content.get(i);
			if (node instanceof Element)
				disallowNesting((Element) node, tag, pattern);
		}
		
	}

	
	String panelcolor = "bgColor=([^;}]+)";
	Pattern panelcolorPattern = Pattern.compile(panelcolor);
	/**
	 * gets the color attribute of the first panel in the given input
	 * @param input
	 * @return color attribute: red, #ff0033, etc. or null, if no attribute
	 * could be found
	 */
	protected String getPanelColor(String input) {
		Matcher panelcolorFinder = panelcolorPattern.matcher(input);
		if (panelcolorFinder.find()) {
			return panelcolorFinder.group(1);
		}
		return null;
	}

	/**
	 * switches two nodes from the given content, the one
	 * represented by i and the previous one.
	 * @param content
	 * @param i index representing the node to be switched with it's previous node.
	 * This int must be greater than 0 and no more than the max num of elements in 
	 * the given content list
	 */
	private void switchNodes(List content, int i) {
		Node lastNode = (Node) content.get(i-1);
		Node thisNode = (Node) content.get(i);
		content.remove(lastNode);
		content.remove(thisNode);
		content.add(i-1, thisNode);
		content.add(i, lastNode);
	}

	/**
	 * gets the color object of the given attribute
	 * @param att
	 * @return
	 */
	protected Color getColor(Attribute att) {
		String val = att.getValue();
		return new Color(val);
	}

	String stylepairs = "([^:]+):([^;]+)(;|$)";
	Pattern stylePattern = Pattern.compile(stylepairs);
	/**
	 * finds if a given css style attribute has color
	 * @param att
	 * @return true if style attribute contains a color key
	 */
	protected boolean hasStyleColor(Attribute att) {
		String value = att.getValue();
		return hasStyleKey(value, "color");
	}

	/**
	 * finds if a given css style attribute has background-color
	 * @param att
	 * @return true if style attribtue contains background-color key
	 */
	protected boolean hasStyleBackground(Attribute att) {
		String value = att.getValue();
		return hasStyleKey(value, "background-color");
	}
	
	/**
	 * @param input
	 * @param searchkey
	 * @return true if given input has css like syntax, 
	 * with a key matching the given searchkey.
	 */
	private boolean hasStyleKey(String input, String searchkey) {
		Matcher colorFinder = stylePattern.matcher(input);
		while (colorFinder.find() ) {
			String key = colorFinder.group(1);
			if (searchkey.equals(key)) return true;
		}
		return false;
	}
	
	/**
	 * gets Color object for value of color css in given style attribute
	 * @param att
	 * @return
	 */
	protected Color getStyleColor(Attribute att) {
		String value = att.getValue();
		String style = getStyleKey(value, "color");
		return new Color(style);
	}

	/**
	 * gets Background object for value of background-color css in
	 * given style attribute
	 * @param att
	 * @return
	 */
	protected Background getStyleBackground(Attribute att) {
		String value = att.getValue();
		String style = getStyleKey(value, "background-color");
		return new Background(style);
	}

	/**
	 * gets value of searchkey for css in the given input.
	 * Example: input = color:red;height:50px;
	 * searchkey = color
	 * then return value = red
	 * @param input
	 * @param searchkey
	 * @return
	 */
	private String getStyleKey(String input, String searchkey) {
		Matcher colorFinder = stylePattern.matcher(input);
		while (colorFinder.find() ) {
			String val = colorFinder.group(2);
			String key = colorFinder.group(1);
			if (searchkey.equals(key)) {
				return val;
			}
		}
		return null;
	}
	
	/**
	 * list of tags which could have data we need to maintain in the attributes
	 */
	String[] SAVE_DATA_CANDIDATES = {
		"font",
		"td",
		"li"
	};
	/**
	 * @param name
	 * @return true if tags with the given name could have data we need for
	 * future conversions
	 */
	protected boolean shouldSaveElement(String name) {
		for (String candidate : SAVE_DATA_CANDIDATES) {
			if (candidate.equals(name)) return true;
		}
		return false;
	}

	/**
	 * creates Confluence syntax for given color and bg, and adds it to the
	 * given root element as appropriate
	 * @param color Color object representing foreground color to be converted, or null
	 * @param bg Background object representing background color to be converted or null
	 * @param root
	 */
	protected void transformContentAddChildTextNodes(Color color, Background bg, Element root) {
		List content = root.content();
		String pre = "", post = "";
		if (bg != null) {
			pre = "{panel:bgColor=" + bg.getValue() + "}";
			post = "{panel}";
		}
		if (color != null) {
			pre += "{color:" + color.getValue() + "}";
			post = "{color}" + post;
		}
		Text preText = new DefaultText(pre);
		Text postText = new DefaultText(post);
		content.add(0, preText);
		content.add(postText);
	}

	/**
	 * replaces the given root element with new Confluence syntax elements
	 * for the given color and bg
	 * @param color
	 * @param bg
	 * @param root
	 */
	protected void transformContentReplaceWithText(Color color, Background bg, Element root) {
		transformContentAddChildTextNodes(color, bg, root);
		List content = root.content();
		Element parent = root.getParent();
		if (parent == null) {
			parent = new DefaultElement("tmp");
			parent.setContent(content);
		}
		else {
			int index = parent.indexOf(root);
			parent.remove(root);
			parent.content().addAll(index, content);
		}
		
	}

	/**
	 * types of color objects: foreground (COLOR) or background (BACKGROUND)
	 */
	public enum ColorType {
		/**
		 * represents foreground color
		 */
		COLOR, 
		/**
		 * represents background color
		 */
		BACKGROUND
	}
	/**
	 * object representing color
	 */
	public class ColorObject {
		/**
		 * value of color: red, #ff0000, etc.
		 */
		private String value;
		/**
		 * background or foreground color
		 */
		ColorType type;
		/**
		 * creates color object with the given value.
		 * If the value is an RGB value (looks like: rgb(0, 124, 255)), then
		 * transforms the value to a hex value
		 * @param value
		 */
		public ColorObject(String value) {
			if (isRGB(value))
				value = rgb2Hex(value);
			this.value = value;
		}
		/**
		 * @return value
		 */
		public String getValue() {
			return this.value;
		}
		/**
		 * @return color type (foreground or background)
		 */
		public ColorType getType() {
			return this.type;
		}
		/**
		 * true if this and the given object are the same color type and
		 * have the same value
		 * @param object
		 * @return
		 */
		public boolean equals(ColorObject object) {
			ColorType aType = this.getType();
			ColorType bType = object.getType();
			String aVal = this.getValue();
			String bVal = object.getValue();
			return aType == bType && aVal.equals(bVal);
		}
		public String toString() {
			return this.type + ":" + this.value;
		}
	}
	
	/**
	 * foreground color object
	 */
	public class Color extends ColorObject{
		/**
		 * creates a foreground color object with the given value
		 * @param value
		 */
		public Color(String value) {
			super(value);
			this.type = ColorType.COLOR;
		}
	}
	/**
	 * background color object
	 */
	public class Background extends ColorObject {
		/**
		 * creates a background color object with the given value
		 * @param value
		 */
		public Background(String value) {
			super(value);
			this.type = ColorType.BACKGROUND;
		}
	}
	
	static String rgb = "rgb\\s*\\(([^)]+)\\)";
	static Pattern rgbPattern = Pattern.compile(rgb);
	/**
	 * @param val
	 * @return true if the value represents an rgb style color
	 * Example: rgb(0, 124, 255)
	 */
	protected static boolean isRGB(String val) {
		val = val.trim();
		Matcher rgbFinder = rgbPattern.matcher(val);
		return rgbFinder.find();
	}

	static Pattern rgbColor = Pattern.compile("rgb\\s*\\(([\\d, ]+)\\)");
	/**
	 * transforms the given input to a hex color
	 * @param input rgb color value
	 * @return given input as a hex color
	 */
	protected static String rgb2Hex(String input) {
		Matcher rgbFinder = rgbColor.matcher(input);
		String hexnum = "#";
		if (rgbFinder.find()) {
			String[] octal = rgbFinder.group(1).split(",");
			for (int i = 0; i < octal.length; i++) {
				String num = octal[i];
				num = num.trim();
				String asHex = Integer.toHexString(Integer.parseInt(num));
				if (Pattern.matches(".", asHex))	//if it's only a single char 
					asHex = "0" + asHex;			//add a 0 to the beginning
				hexnum += asHex;
			}
			input = hexnum;
		}
		return input;
	}

}
