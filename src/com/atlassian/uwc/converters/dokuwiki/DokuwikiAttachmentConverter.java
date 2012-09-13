package com.atlassian.uwc.converters.dokuwiki;

import java.io.File;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class DokuwikiAttachmentConverter extends HierarchyImageConverter {

	@Override
	public void convert(Page page) {
		String converted = convertAttachments(page);
		page.setConvertedText(converted);
	}

	Pattern attachment = Pattern.compile("(?s)\\{\\{(.*?)\\}\\}");
	Pattern parts = Pattern.compile("([^|?]+)(?:([?|])(.*))?");
	Pattern type = Pattern.compile(".*?[.]([^.]+)$");
	private String convertAttachments(Page page) {
		String attdir = getAttachmentDirectory();
		String relpath = page.getPath().replaceFirst("[^\\"+File.separator+"]*$", "");
		String input = page.getOriginalText();
		Matcher attachmentFinder = attachment.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		HashSet<File> candidates = new HashSet<File>();
		while (attachmentFinder.find()) {
			found = true;
			String all = attachmentFinder.group(1);
			String replacement = "";
			Matcher partsFinder = parts.matcher(all);
			if (partsFinder.find()) {
				String dokupath = partsFinder.group(1);
				String fileRelPath = createRelativePath(dokupath, relpath);
				File file = new File(attdir + File.separator + fileRelPath);
				candidates.add(file);
				replacement = file.getName();
				Matcher typeFinder = type.matcher(file.getName());
				if (typeFinder.find()) {
					String filetype = typeFinder.group(1);
					if (isImage(file.getName())) {
						String size = getSizeData(partsFinder);
						replacement = "!" + replacement + size + "!";
					}
					else {
						replacement = "^" + replacement;
						String alias = getAlias(partsFinder);
						replacement = "[" + alias + replacement + "]";
					}
				}
			}
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			attachmentFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			attachmentFinder.appendTail(sb);
			for (File file : candidates) {
				log.debug("Found attachment: " + file.getName());
				page.addAttachment(file);
			}
			return sb.toString();
		}
		return input;
	}

	private String getSizeData(Matcher partsFinder) {
		String hasOtherData = partsFinder.group(2);
		String otherData = partsFinder.group(3);
		String sizedata = "";
		if (hasOtherData != null && otherData != null && !"".equals(otherData)) {
			if (hasOtherData.equals("?")) {
				Matcher paramFinder = params.matcher(otherData);
				if (paramFinder.find()) {
					String width = paramFinder.group(1);
					String height = paramFinder.group(3);
					sizedata = "|width=" + width + "px" + (height != null?",height="+height + "px":"");
				}
			}
		}
		return sizedata;
	}

	public String getAlias(Matcher partsFinder) {
		String hasOtherData = partsFinder.group(2);
		String otherData = partsFinder.group(3);
		String alias = "";
		if (hasOtherData != null && otherData != null && !"".equals(otherData)) {
			if (hasOtherData.equals("|")) {
				alias = otherData + "|";
			}
		}
		return alias;
	}
	
	Pattern isRelative = Pattern.compile("^:[^:]+$");
	protected String createRelativePath(String input, String relativePath) {
		input = input.trim();
		if (isRelative.matcher(input).find()) {
			String ignorable = getProperties().getProperty("filepath-hierarchy-ignorable-ancestors", "");
			relativePath = relativePath.replaceFirst("\\Q"+ignorable+"\\E", "");
			if (!relativePath.endsWith(File.separator)) relativePath += File.separator;
			input = input.replaceFirst(":", relativePath);
		}
		input = input.replaceAll(":", File.separator);
		if (!input.startsWith(File.separator)) input = File.separator + input;
		return input;
	}

}
