package com.atlassian.uwc.converters.smf;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ImageConverter extends BaseConverter {

	HashMap<String,File> files = null;
	Logger log = Logger.getLogger(this.getClass());
	
	public void convert(Page page) {
		this.files = null;
		String input = page.getOriginalText();
		String converted = convertImages(input, page.getAttachments());
		page.setConvertedText(converted);
	}

	protected String convertImages(String input, Set<File> attachments) {
		input = convertInlineImages(input, attachments);
		input = convertAttachmentLinks(input, attachments);
		return input;
	}

	Pattern image = Pattern.compile("\\[img\\](.*?)\\[\\/img\\]");
	Pattern attachid = Pattern.compile("attach=(\\d+)");
	protected String convertInlineImages(String input, Set<File> attachments) {
		HashMap<String, File> files = getFiles(attachments);
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String content = imageFinder.group(1);
			Matcher idFinder = attachid.matcher(content);
			String image = content; //default
			if (idFinder.find()) {
				String id = idFinder.group(1);
				log.debug("Found attach=id string in img content: " + content + "\nid=" + id);
				if (files.containsKey(id)) {
					File file = files.get(id);
					content = file.getName();
				}
				else log.debug("No attachment with that id found."); //XXX What if it's on another topic? Can't fix here. Namespace collisions resolved in hierarchy.
			}
			else log.debug("Could not find attach=id string in [img] code: " + content);
			String replacement = "!" + content + "!";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern links = Pattern.compile("\\[([^\\]]+)\\]");
	Pattern alias = Pattern.compile("([^|]+)\\|(.*)");
	protected String convertAttachmentLinks(String input, Set<File> attachments) {
		HashMap<String, File> files = getFiles(attachments);
		Matcher linkFinder = links.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String content = linkFinder.group(1);
			Matcher aliasFinder = alias.matcher(content);
			String alias = null;
			if (aliasFinder.find()) {
				alias = aliasFinder.group(1);
				content = aliasFinder.group(2);
				if (alias.equals(content)) alias = null;
			}
			Matcher idFinder = attachid.matcher(content);
			String image = content; //default
			if (idFinder.find()) {
				String id = idFinder.group(1);
				log.debug("Found attach=id string in link content: " + content + "\nid=" + id);
				if (files.containsKey(id)) {
					File file = files.get(id);
					content = "^" + file.getName();
					log.debug("filename for that id is: " + file.getName());
				}
				else log.debug("No attachment with that id found."); //XXX What if it's on another topic!!! Can't fix here. Namespace collisions resolved in hierarchy.
			}
			String replacement = "[" +
				(alias != null?alias+"|":"") +
				content + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	Pattern id = Pattern.compile("^\\d+");
	protected HashMap<String, File> getFiles(Set<File> attachments) {
		if (this.files == null) {
			this.files = new HashMap<String, File>();
			for (File file : attachments) {
				log.debug("Examining attachment: " + file.getName());
				Matcher idFinder = id.matcher(file.getName());
				if (idFinder.find()) {
					String id = idFinder.group();
					log.debug("Found attachment id: " + id);
					this.files.put(id, file);
				}
				else {
					log.warn("Could not find id for attachment: " + file.getName());
				}
			}
		}
		return this.files;
	}

}
