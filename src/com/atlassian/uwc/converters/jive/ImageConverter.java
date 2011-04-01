package com.atlassian.uwc.converters.jive;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class ImageConverter extends AttachmentConverter {

	InternalLinkConverter linkconverter = new InternalLinkConverter();
	public void convert(Page page) {
		log.debug("Examining image syntax.");
		initTitleData();
		String input = page.getOriginalText();
		String id = getDocId(input);
		String converted = convertImage(input, id);
		if (!id.endsWith("-105")) //not handling this in comments because this might interfere with normal images
			converted = convertAttachmentLinks(converted, id);
		page.setConvertedText(converted);
	}

	public void initTitleData() {
		linkconverter.setProperties(getProperties());
		linkconverter.convertAll("init"); //initialize title data
	}

	Pattern image = Pattern.compile("<img([^>]+)>");
	Pattern imageid = Pattern.compile("__jive_ID=.(\\d+)");
	Pattern src = Pattern.compile("src=[\"']([^\"']+)[\"']");
	protected String convertImage(String input, String objid) {
		Matcher imageFinder = image.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (imageFinder.find()) {
			found = true;
			String imagedata = imageFinder.group(1);
			Matcher idFinder = imageid.matcher(imagedata);
			String filename = null;
			if (objid.endsWith("-105") || !idFinder.find()) { //endswith 105 == comment
				//external image?
				Matcher srcFinder = src.matcher(imagedata);
				if (srcFinder.find()) {
					filename = srcFinder.group(1);
//					log.debug("image src found: " + filename); //COMMENT
				}
				else {
					log.error("Could not transform image syntax: " + imageFinder.group());
					continue;
				}
			}
			else {
				String id = idFinder.group(1);
//				log.debug("image id found: " + id); //COMMENT
				filename = getFilename(id, objid, input);
			}
			String params = getParams(imagedata);
			
			String replacement = "!" + filename + params + "! ";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			imageFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			imageFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern attlink = Pattern.compile("<a([^>]+)>(.*?)<\\/a>");
	Pattern attlinkid = Pattern.compile("href=\"\\/servlet\\/JiveServlet\\/download\\/(\\d+)-\\d+-(\\d+)\\/");
	protected String convertAttachmentLinks(String input, String objid) {
		Matcher linkFinder = attlink.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String linkdata = linkFinder.group(1);
			String alias = linkFinder.group(2);
			if (!linkdata.contains("_jive_internal=\"true\"")) continue; //not an internal link
			Matcher idFinder = attlinkid.matcher(linkdata);
			if (!idFinder.find()) continue; //not an attachment link
			String docid = idFinder.group(1);
			String attid = idFinder.group(2);
			String filename = null; 
			if (!objid.startsWith(docid+"-")) { //referencing image from another page
				filename = getFilenameWithIds(docid+"-102", attid, true); 
				if (filename == null) { //blogpost?
					filename = getFilenameWithIds(docid+"-38", attid);
					if (filename == null) { //not migrated?
						
					}
					filename = getPagename(docid, 38) + "^" + filename;
				}
				else { //get pagename as per docid
					filename = getPagename(docid, 102) + "^" + filename;
				}
			}
			else filename = "^" + getFilenameWithIds(objid, attid);
			String replacement = "[" + alias + "|" + filename + "]";
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	private String getPagename(String objid, long typeid) {
		if (typeid == 38) 
			return linkconverter.getTitleFromMap(objid, "blogpost");
		else if (typeid == 102)
			return linkconverter.getTitleFromMap(objid, "document");
		log.warn("Couldn't get pagename for: " + objid + ", " + typeid);
		return null;
			
	}
	private String getFilename(String attid, String docid, String origInput) {
		initAttachmentData(); 
		log.debug("getting filename: " + docid);
		Vector<Data> alldata = attachmentdata.get(docid);
//		if (attachmentdata.isEmpty()) log.debug("attachmentdata object is empty!"); //COMMENT
//		else if (alldata == null) log.debug("attachmentdata for docid is null!"); //COMMENT
//		else if (alldata.isEmpty()) log.debug("attachmentdata for docid is empty!"); //COMMENT
		if (alldata == null || alldata.isEmpty()) {
			log.error("Current page (" + docid + ") does not have any attachment data."); //XXX TODO handle images from other pages
			return null;
		}
		for (Data data : alldata) {
			if (data.id.equals(attid)) {
				return data.name;
			}
		}
		log.error("Current page (" + docid +
				") does not have attachment data for attachment id: " + attid);//XXX TODO handle images from other pages
		return null;
	}
	
	private String getFilenameWithIds(String docid, String attid) {
		return getFilenameWithIds(docid, attid, false);
	}
	private String getFilenameWithIds(String docid, String attid, boolean suppressWarnings) {
		initAttachmentData(); 
		Vector<Data> alldata = attachmentdata.get(docid);
		if (alldata == null || alldata.isEmpty()) {
			if (!suppressWarnings) {
				log.error("Current page (" + docid + ") does not " +
						"have attachment data for attachment id: " + attid); 
			}
			return null;
		}
		for (Data data : alldata) {
			if (data.id.equals(attid)) {
				return data.name;
			}
		}
		log.error("Current page (" + docid +
				") does not have attachment data for attachment id: " + attid);//XXX TODO handle images from other pages
		return null;
	}
	
	Pattern transformableParams = Pattern.compile("((?:class)|(?:width)|(?:height))=\"([^\"]+)\"");
	Pattern thumbnail = Pattern.compile("\\bjive-image-thumbnail\\b");
	protected String getParams(String input) {
		Matcher paramFinder = transformableParams.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		String replacement = "";
		while (paramFinder.find()) {
			if (found) replacement += ",";
			found = true;
			String key = paramFinder.group(1);
			String val = paramFinder.group(2);
			if ("class".equals(key) && thumbnail.matcher(val).find()) {
				replacement = "thumbnail";
				break;
			}
			if (!val.endsWith("px")) val += "px"; //so far only supporting width and height
			replacement += key + "=" + val;
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
		}
		if (found) {
			return "|" + replacement;
		}
		return replacement;
	}

}
