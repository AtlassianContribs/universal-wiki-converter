package com.atlassian.uwc.converters.twiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.tikiwiki.RegexUtil;
import com.atlassian.uwc.ui.Page;

public class TWikLinkiPostProcessor extends BaseConverter {

    Logger log = Logger.getLogger(this.getClass());
    public static final String OPEN_LINK = "UWC_TOKEN_OL";
    public static final String CLOSE_LINK = "UWC_TOKEN_CL";

    public void convert(Page page) {
        String input = page.getOriginalText();
        String converted = fixLinksWithSpaces(input);
        page.setConvertedText(converted);
    }

    Pattern link = Pattern.compile("(?<=" + OPEN_LINK  + ")" +
    		"(.*?)" +
    		"(?=" + CLOSE_LINK + ")");
    Pattern aliasPattern = Pattern.compile("^[^|]*(?=\\|)");
    Pattern pagename = Pattern.compile("" +
    		"(?<=[|:]|^)" +
    		"([^|:^\n]*)" +
    		"(?=\\^|$)");
    protected String fixLinksWithSpaces(String input) {
    	Matcher linkFinder = link.matcher(input);
    	StringBuffer sb = new StringBuffer();
    	boolean found = false;
    	while (linkFinder.find()) {
    		found = true;
    		String contents = linkFinder.group(1);
    		//ignore http/ftp links
    		if (!isInternalLink(contents)) continue; 
    		//get the alias if there is one
    		String alias = null;
    		Matcher aliasFinder = aliasPattern.matcher(contents); 
    		if (aliasFinder.find()) {
    			alias = aliasFinder.group();
    		}
    		//get the pagename and fix the chars as needed
    		Matcher pagenameFinder = pagename.matcher(contents);
    		String page = contents;
    		String origpage = null;
    		String replacement = contents;
    		StringBuffer sb2 = new StringBuffer();
    		boolean found2 = false;
			if (pagenameFinder.find()) {
				found2 = true;
				page = pagenameFinder.group(1);
				origpage = page;
				page = fixInternalLinks(page);
				pagenameFinder.appendReplacement(sb2, page);
			}
			if (found2) {
				pagenameFinder.appendTail(sb2);
				replacement = sb2.toString();
			}
			//don't insert pagename alias if we're talking about a file
			if (contents.contains("^")) alias = null;
			//insert pagename alias if we've detected we need one
			if ("".equals(alias)) {
				alias = origpage;
				replacement = alias + replacement;
			}
     		replacement = RegexUtil.handleEscapesInReplacement(replacement);
     		//replace the original found content with the adjusted content
    		linkFinder.appendReplacement(sb, replacement);
    	}
    	if (found) {
    		linkFinder.appendTail(sb);
    		return sb.toString();
    	}
    	return input;
    }

	private String fixInternalLinks(String linkTarget) {
		linkTarget = linkTarget.replace(" ", "");
		linkTarget = linkTarget.replace("&", "");
		linkTarget = linkTarget.replace("-", ""); 
		linkTarget = linkTarget.replace(".", "");
		linkTarget = linkTarget.replace("/", "");
		linkTarget = linkTarget.replace("$", "");
		return linkTarget;
	}
	
	private String DEFAULT_PROTOCOLS = "http,https,ftp";
	private boolean isInternalLink(String linkTarget) {
		String protocols = DEFAULT_PROTOCOLS;
		if (this.getProperties().contains("twiki-link-protocol")) {
			protocols = this.getProperties().getProperty("twiki-link-protocol", DEFAULT_PROTOCOLS);
		}
		String[] eachProtocol = protocols.split(",");
		if (eachProtocol == null) {
			protocols = DEFAULT_PROTOCOLS;
			eachProtocol = null;
			eachProtocol = protocols.split(",");
		}
		boolean isInternal = true;
		for (String protocol : eachProtocol) {
			if (linkTarget.contains(protocol + ":")) 
				return false;
		}
		return isInternal;
	}


}
