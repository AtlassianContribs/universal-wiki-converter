package com.atlassian.uwc.converters.pmwiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

import java.util.StringTokenizer;

/**
 * Massages links into a consistent format which makes sense when
 * transferred to Confluence
 */
public class PmWikiLinkAdjuster extends BaseConverter {

    private static final String LINK_START = "_UWC_LINK_START_";
    private static final String LINK_END = "_UWC_LINK_END_";

    /**
     * Converts any links from the PmWiki format to Confluence's format. Any links pointing to
     * other documents in the wiki are massaged further so that they point to the correct page title.
     *
     * @param page A page with text to be converted.
     */
    public void convert(Page page) {
        assert page != null;
        assert page.getOriginalText() != null;

        String text = page.getOriginalText();

        int linkStart = text.indexOf(LINK_START);
        while (linkStart >= 0) {
            int linkEnd = text.indexOf(LINK_END, linkStart);
            if (linkEnd < 0) {
                break;
            }
            // grab the [[ text inside the link ]]
            String linkTarget = text.substring(linkStart + LINK_START.length(), linkEnd);

            // if the target link is external don't change it
            if (!linkTarget.startsWith("http") && !linkTarget.startsWith("ftp")) {
            	linkTarget = prependWithGroupName(linkTarget, page);
                linkTarget = convertLinksToStripExtraneousPrefixes(linkTarget);
                linkTarget = convertLinksToCamelCase(linkTarget);
            }

            // inserts the link and removes the markers surrounding the link target
            text = text.substring(0, linkStart) + linkTarget + text.substring(linkEnd + LINK_END.length());
            linkStart = text.indexOf(LINK_START);
        }
        page.setConvertedText(text);
    }

    private String convertLinksToStripExtraneousPrefixes(String linkTarget) {
        // if link contains two prepends then strip the first such as Main.CrossTeamImpacts.WhatAreImpacts
        String split[] = linkTarget.split("\\.");
        if (split.length == 3) {
            linkTarget = split[1] + "." + split[2];
        }
        // now remove the middle link
        // commented out because it's different then what is fixed. return removeMiddleNameInLink(linkTarget);
        return linkTarget;
    }

    private String prependWithGroupName(String linkTarget, Page page) {
    	return linkTarget.replaceFirst("[.\\/-]", ":");
    }

    /**
     * This is for cases such as  [Clone an instance|Teams.RELEASE.InstanceCloning]
     * which should actually map to  [Clone an instance|Teams.InstanceCloning]
     *
     * @param linkTarget
     * @return
     */
    protected String removeMiddleNameInLink(String linkTarget) {
        String split[] = linkTarget.split("\\.");
        if (split.length == 3) {
            linkTarget = split[0] + "." + split[2];
        }
        return linkTarget;
    }

    /**
     * PmWiki link targets can be written many different ways.
     * From the docs:
     * [[Wiki sandbox]], [[wiki sandbox]], and [[WikiSandbox]] all display
     * differently but link to the same page - which is titled WikiSandbox.
     * <p/>
     * So we're going to massage everything to one format which also matches
     * the page names, but leave the original ws handling as an alias.
     * <p/>
     *
     * @param linkTarget
     * @return
     */
    public String convertLinksToCamelCase(String linkTarget) {
    	String orig = linkTarget;
    	linkTarget = linkTarget.replaceAll(" ", ""); 
    	if (!orig.equals(linkTarget)) {
    		String nospace = orig.replaceFirst("^[^:]+:", ""); //get rid of space in alias
    		linkTarget = nospace + "|" + linkTarget; 
    	}
    	return linkTarget;
    }

}
