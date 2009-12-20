package com.atlassian.uwc.converters.jspwiki;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class AttachmentSpaceConverter extends JspwikiLinkConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Attachment Spaces - Starting");
		String input = page.getOriginalText();
		String converted = convertAttachmentSpaces(input);
		page.setConvertedText(converted);
		log.debug("Converting Attachment Spaces - Completed");
	}
	
	Pattern links = Pattern.compile("" +
			"(\\[[^|\\]]*)([^\\]]+\\])");
	protected String convertAttachmentSpaces(String input) {
		Matcher linksFinder = links.matcher(input);
		String pagedir = getPageDir();
		boolean found = false;
		StringBuffer sb = new StringBuffer();
		while (linksFinder.find()) {
			found = true;
			String target = linksFinder.group(2);
			String alias = linksFinder.group(1);
			if (!target.startsWith("|")) {
				target = alias + target;
				alias = "";
			}
			target = target.replaceAll("^[|]\\s+", "|"); //uwc-348 spaces after pipes
			if (pagedir == null || isAttachment(pagedir, target))
				target = target.replaceAll(" ", "+");
			String replacement = alias + target;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linksFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linksFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected boolean isAttachment(String pagedir, String link) {
		File dir = new File(pagedir);
		String[] files = getPageFiles(dir);
		for (String filename : files) {
			filename = filename.replaceFirst(".txt$", "");
			filename = filename.replaceAll("[ +]", "");
			link = link.replaceAll("[ +]", "");
			if (link.endsWith(filename+"]")) return false;
		}
		return true;
	}
}
