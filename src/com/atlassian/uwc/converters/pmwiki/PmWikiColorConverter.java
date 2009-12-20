package com.atlassian.uwc.converters.pmwiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

import java.util.StringTokenizer;

/**
 * Converter - convert PmWiki color tags to Confluence color tags.
 */
public class PmWikiColorConverter extends BaseConverter {
    private static Logger log = Logger.getLogger(PmWikiColorConverter.class);

    public String[] pmWikiColorStrings = {"%red%", "%green%"};
    public String[] confluenceColorStrings = {"{color:red}", "{color:green}"};

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

        for (int i = 0; i < pmWikiColorStrings.length; i++) {
            String colorString = pmWikiColorStrings[i];
            int marker = 0;
            marker = text.indexOf(colorString, marker);
            while (marker >= 0) {
                // replace tag
                int endOfLineMarker = text.indexOf("\n",marker+1);
                String line = text.substring(marker, endOfLineMarker);
//                line.replaceAll()
                // look for a %% by end of line and convert
                // if no %% then add a {color} at end of line
                // look for next
                marker = text.indexOf(colorString, marker);
            }

        }

    }

}
