package com.atlassian.uwc.converters.jive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.jive.AttachmentConverter.Data;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class InternalLinkConverter extends BaseConverter {

	protected static HashMap<String, TitleData> titledata; // key is docid
	
	
	public void convert(Page page) {
		if (page.getName() != null) //not a comment
			log.debug("URLREPORT: Examining page '" + page.getName() + "' in space '" + page.getSpacekey() +
				"' for links.");
		String input = page.getOriginalText();
		String converted = convertAll(input);
		page.setConvertedText(converted);
	}

	public String convertAll(String input) {
		initTitleData(getProperties());
		String converted = convertLink(input);
		converted = convertSpan(converted);
		return converted;
	}

	Pattern link = Pattern.compile("(?s)<a ([^>]+)>(.*?)<\\/a>");
	Pattern href = Pattern.compile("href=\"([^\"]+)\"");
	Pattern title = Pattern.compile("title=\"([^\"]+)\"");
	Pattern jiveinternal = Pattern.compile("_jive_internal=\"([^\"]+)\"");
	Pattern defaultattr = Pattern.compile("(?:(?:__default_attr)|(?:id))=\"([^\"]+)\"");
	Pattern jivemacroname = Pattern.compile("(?:__)?jive_?macro(?:_name)?=\"([^\"]+)\"");
	Pattern linkid = Pattern.compile("(\\w+)-(\\d+)$");
	Logger log = Logger.getLogger(this.getClass());
	protected String convertLink(String input) {
		Matcher linkFinder = link.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (linkFinder.find()) {
			found = true;
			String params = linkFinder.group(1);
			//handle alias
			String alias = linkFinder.group(2);
			String replacement = constructConfLink(params, alias);
			
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			linkFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			linkFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}
	
	Pattern span = Pattern.compile("(?s)<span([^>]*?jive_macro_name=[^>]*?)\\/>");
	protected String convertSpan(String input) {
		Matcher spanFinder = span.matcher(input);
		StringBuffer sb = new StringBuffer();
		boolean found = false;
		while (spanFinder.find()) {
			found = true;
			String params = spanFinder.group(1);
			if (!params.contains("document") && !params.contains("blog")) continue;
			String replacement = constructConfLink(params, null);
			
			replacement = RegexUtil.handleEscapesInReplacement(replacement);
			spanFinder.appendReplacement(sb, replacement);
		}
		if (found) {
			spanFinder.appendTail(sb);
			return sb.toString();
		}
		return input;
	}

	protected String constructConfLink(String params, String alias) {
		String titleParam = getParamValue(params, title);
		if (alias == null || "".equals(alias)) {
			if (titleParam != null) alias = titleParam;
		}
		if (alias == null) alias = "";

		//default target is href param
		String hrefParam = getParamValue(params, href);
		String target = hrefParam;
		
		//deal with links that use relative urls
		String sourcedomain = getSourceDomain();
		String jiveinternalParam = getParamValue(params, jiveinternal);
		if (Boolean.parseBoolean(jiveinternalParam)) {
			if (sourcedomain == null) {
				log .error("Please configure the internaljivedomain property to be the http url of your source.");
				sourcedomain = "";
			}
			if (target != null) {
				if (!target.startsWith("http")) target = sourcedomain + target;
			}
		}
		
		//deal with links that use defaultattr to identify target
		String defaultattrParam = getParamValue(params, defaultattr);
		String jivemacronameParam = getParamValue(params, jivemacroname);
		if (defaultattrParam != null && jivemacronameParam != null) { //if we've migrated this particular title objid
			target = getTitleFromMap(defaultattrParam, jivemacronameParam);
		}
		if ((target == null || "".equals(target)) && defaultattrParam != null) {
			String uripart = getUriPart(jivemacronameParam);
			target = sourcedomain + uripart + defaultattrParam;
		}
		if (hrefParam != null) {
			Matcher linkidFinder = linkid.matcher(hrefParam);
			if (hrefParam.startsWith("/") && linkidFinder.find()) {
				String type = linkidFinder.group(1);
				String id = linkidFinder.group(2);
				String tmptarget = getTitleFromMap(id, type);
				if (tmptarget != null) target = tmptarget;
			}
		}

		//finish cleaning up alias
		alias = alias.trim();
		alias = removeHtmlTags(alias);
		if (target == null && alias != null) target = alias; //this can happen!
		if (alias.equals(target)) alias = "";
		if (!"".equals(alias)) alias += "|"; //add alias delim
		
		//build confluence link
		String replacement = "[" + alias + target + "]";
		log.debug("URLREPORT: fromhref='" + hrefParam + "'\tdefaultattr='" + defaultattrParam + "'\t" +
				"tohref='" + target + "'\talias='" + alias + "'");
		return replacement;
	}
	
	Pattern titleline = Pattern.compile("(?<=^|\n)" +
			"([^\t]*)\t" + //doc/blogid
			"([^\t]*)\t" + //doc/blogtype (102 vs 38)
			"([^\t]*)\t" + //containerid
			"([^\t]*)\t" + //containertype
			"([^\t]*)\t" + //version
			"([^\n]*)");	//title
	public void initTitleData(Properties properties) {
		if (titledata == null) {
			String path = properties.getProperty("titledata", null);
			if (path == null) {
				log.debug("No path for titledata property");
				return;
			}
			titledata = new HashMap<String, TitleData>();
			path = path.trim();
			String filestring = "";
			String line;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(path));
				while ((line = reader.readLine()) != null) {
					filestring += line + "\n";
				}
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Matcher dataFinder = titleline.matcher(filestring);
			boolean found = false;
			while (dataFinder.find()) {
				found = true;
				String docid = dataFinder.group(1);
				if (!docid.matches("\\d+")) continue;

				String objtype = dataFinder.group(2);
				docid = generateDocKey(docid, objtype);
				String containerid = dataFinder.group(3);
				String containertype = dataFinder.group(4);
				String spacekey = getSpacekey(containerid, containertype);
//				String version = dataFinder.group(5); //not using this information right now, XXX relevant for histories?
				String title = dataFinder.group(6);
				title = createConfPageTitle(spacekey, title);
				
				titledata.put(docid, new TitleData(title));
			}
			if (!found) {
				String excerpt = filestring.substring(0, 500);
				log.error("No data found in titledata file: " + excerpt);
			}
		}
	}

	private String getSpacekey(String containerid, String containertype) {
		HashMap<String, String> spacekeys = SpaceConverter.getSpacekeys(); //XXX for unit tests we need to insert this somehow
		String spacekey = spacekeys.get(generateDocKey(containerid, containertype));
		if (spacekey == null && "2020".equals(containertype)) { //if it's a user container, spacekey might be null
			spacekey = SpaceConverter.getSpacename(containerid + "-" + containertype);
			if (spacekey != null) spacekey = "~" + spacekey;
		}
		return spacekey;
	}
	
	protected String createConfPageTitle(String spacekey,String title){
		return spacekey + ":" +title;
	}

	public String generateDocKey(String objid, String objtype) {
		return objid + "-" + objtype;
	}
	
	Pattern doc = Pattern.compile("^doc", Pattern.CASE_INSENSITIVE);
	Pattern blog = Pattern.compile("^blog", Pattern.CASE_INSENSITIVE);
	public String getTitleFromMap(String objid, String jivemacronameParam) {
		if (titledata == null) return null;
		if (jivemacronameParam == null) return null;
		String typeid = getTypeId(jivemacronameParam);
		if (typeid == null) { //not migrated type (not doc or blog)
			handleNotMigratedError(objid, jivemacronameParam);
			return null; 
		}
		TitleData data = titledata.get(generateDocKey(objid, typeid));
		if (data == null) { //this particular page was not migrated
			handleNotMigratedError(objid, typeid);
			return null;
		}
		return data.getTitle();
	}

	protected void handleNotMigratedError(String objid, String typeid) {
		if (!"102".equals(typeid)) {
			String typename = ("38".equals(typeid))?"blog":"type:"+typeid;
			log.debug("URLREPORT: Problem with link to " +
					typename +
					". Not exported: " + objid);
		}
	}

	protected String getTypeId(String typename) {
		String typeid = null;
		if (doc.matcher(typename).find()) {
			typeid = "102";
		}
		else if (blog.matcher(typename).find()) {
			typeid = "38";
		}
		return typeid;
	}

	Pattern htmltag = Pattern.compile("<\\/?((strong)|(em)|(b)|(i)|(u)|(s)|(span))[^>]*>");
	private String removeHtmlTags(String input) {
		Matcher tagFinder = htmltag.matcher(input);
		if (tagFinder.find()) {
			return tagFinder.replaceAll("");
		}
		return input;
	}
	protected String getUriPart(String input) {
		if ("document".equals(input)) return "/docs/";
		return input;
	}
	protected String getSourceDomain() {
		return getProperties().getProperty("internaljivedomain", null);
	}
	private String getParamValue(String input, Pattern pattern) {
		Matcher paramFinder = pattern.matcher(input);
		if (paramFinder.find()) {
			return paramFinder.group(1);
		}
		return null;
	}

	//filenames tend to be like: DOC-12345-Shortened_Title-1.txt
	Pattern filenameConvention = Pattern.compile("^(\\w+)-(\\d+)");
	/** 
	 * 
	 * @param name
	 */
	public void filterTitle(String filename, Properties properties) {
		initTitleData(properties);
		Matcher infoFinder = filenameConvention.matcher(filename);
		if (infoFinder.find()) {
			String typename = infoFinder.group(1);
			String typeid = getTypeId(typename);
			String objid = infoFinder.group(2);
			String key = generateDocKey(objid, typeid);
			if (titledata != null && titledata.containsKey(key)) {
				titledata.get(key).filtered = true;
			}
				
		}
		
	}

	public class TitleData {
		public String title;
		public boolean filtered;
		public TitleData(String title) {
			this.title = title;
			this.filtered = false;
		}
		public String getTitle() {
			if (this.filtered) {
				log.debug("URLREPORT: FILTERING: Next item was a filtered link:");
				return null;
			}
			return title;
		}
		public String toString() {
			return title + ", " + filtered;
		}
	}
}
