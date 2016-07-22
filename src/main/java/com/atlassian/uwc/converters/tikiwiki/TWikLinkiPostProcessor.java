package com.atlassian.uwc.converters.tikiwiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TWikLinkiPostProcessor extends BaseConverter {

    Logger log = Logger.getLogger(this.getClass());

    public void convert(Page page) {
        // look for matches

        // fix matching links
        String input = page.getOriginalText();
        String converted = fixLinksWithSpaces(input);
        page.setConvertedText(converted);

    }

    /**
     * Find the links and make sure any spaces in the link targets are removed
     * @todo - do we need to CamelCase these too?
     * @param wikiText
     * @return
     */
    public String fixLinksWithSpaces(String wikiText) {
        String regex = "UWC_TOKEN_OL(.*?)\\|(.*?)UWC_TOKEN_CL";
        // Compile the regex.
        Pattern pattern = Pattern.compile(regex, 0);
        // Get a Matcher based on the target string.
        Matcher matcher = pattern.matcher(wikiText);
        String retString = wikiText;
        // Find all the matches.
        while (matcher.find()) {
            String linkTarget = matcher.group(2);
            linkTarget = linkTarget.replace(" ", "");
            int start = matcher.start();
            int end = matcher.end();
            retString = retString.substring(0, start) +
                    linkTarget +
                    retString.substring(end, retString.length());
            // reset the matcher so it will replace the 'next first'
            matcher = pattern.matcher(retString);
        }
        return retString;
    }


}
