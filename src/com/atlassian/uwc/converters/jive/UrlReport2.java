package com.atlassian.uwc.converters.jive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.IllegalLinkNameConverter;
import com.atlassian.uwc.converters.jive.SpaceConverter.ContainerInfo;
import com.atlassian.uwc.ui.Page;

public class UrlReport2 extends InternalLinkConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		String params = getJiveMeta(page.getOriginalText());
		if (params == null) log.error("No metadata for page: " + page.getName());
		String type = getType(params);
		if (isDocument(type)) {
			initTitleData(getProperties()); //space data already init'd
			String objid = getId(params);
			String conftitle = getTitleFromMap(objid, "doc");
			conftitle = handleIllegalChars(conftitle, params, false);
			conftitle = getTargetDomain() + "/display/" + conftitle;
			String jivetitle = getSourceDomain() + getUriPart("document") + "DOC-" + objid;
			log.debug("URLREPORT2: " +
					"Jive document '" +
					jivetitle + //"http://jive.foo.com/DOC-12345"
					"' was migrated to Confluence page '" +
					conftitle + //"http://confluence.foo.com/display/SPACE/My+Page" +
			"'\n");
		} else if (isBlog(type)) {
			initTitleData(getProperties()); //space data already init'd
			String objid = getId(params);
			String conftitle = getTitleFromMap(objid, "blog");
			conftitle = handleIllegalChars(conftitle, params, true);
			conftitle = getTargetDomain() + "/display/" + conftitle;
			log.debug("URLREPORT2: " +
					"Jive blog with id '" +
					objid + 
					"' was migrated to Confluence page '" +
					conftitle + 
			"'\n");
		}
	}
	
	private String getTargetDomain() {
		return getProperties().getProperty("targetconfdomain", null);
	}

	Pattern fulltitle = Pattern.compile("^([^:]+):(.*)$");
	Pattern blogdate = Pattern.compile("^(\\/\\d{4,4}\\/\\d{2,2}\\/\\d{2,2}\\/)(.*)$");
	IllegalLinkNameConverter converter = new IllegalLinkNameConverter();
	private String handleIllegalChars(String input, String params, boolean isBlog) {
		Matcher fulltitleFinder = fulltitle.matcher(input);
		if (fulltitleFinder.find()) {
			String spacekey = fulltitleFinder.group(1);
			//if spacekey is null, handle user container?
			if ("null".equals(spacekey)) spacekey = handleUserContainer(params);
			//handle illegal characters
			String title = fulltitleFinder.group(2);
			String date = "";
			if (isBlog) {
				Matcher dateFinder = blogdate.matcher(title);
				if (dateFinder.find()) {
					date = dateFinder.group(1);
					title = dateFinder.group(2);
				}
			}
			title = date + converter.convertIllegalName(title);
			//various uri encodings 
			title = title.replaceAll("[ ]", "+");
			if (!title.startsWith("/")) title = "/" + title;
			return spacekey + title;
		}
		return input;
	}

	Pattern containertypePattern = Pattern.compile("containertype=(\\d+)");
	Pattern usernamePattern = Pattern.compile("usercontainername=(\\w+)");
	private String handleUserContainer(String params) {
		Matcher typeFinder = containertypePattern.matcher(params);
		String containertype = (typeFinder.find())?typeFinder.group(1):null;
		if ("2020".equals(containertype)) {
			Matcher nameFinder = usernamePattern.matcher(params);
			return "~" + ((nameFinder.find())?nameFinder.group(1):null);
		}
		return null;
	}

	Pattern idPattern = Pattern.compile("^id=(\\d+)");
	private String getId(String input) {
		Matcher idFinder = idPattern.matcher(input);
		return (idFinder.find())?idFinder.group(1):null;
	}

	Pattern typePattern = Pattern.compile("\\|type=(\\w+)");
	private String getType(String input) {
		Matcher typeFinder = typePattern.matcher(input);
		return (typeFinder.find())?typeFinder.group(1):null;
	}
	private boolean isDocument(String input) {
		return "DOC".equals(input);
	}
	private boolean isBlog(String input) {
		return "BLOG".equals(input);
	}
	Pattern jivemeta = Pattern.compile("\\{jive-export-meta:([^}]+)\\}");
	private String getJiveMeta(String input) {
		Matcher jivemetaFinder = jivemeta.matcher(input);
		if (jivemetaFinder.find()) {
			return jivemetaFinder.group(1);
		}
		return null;
	}
}
