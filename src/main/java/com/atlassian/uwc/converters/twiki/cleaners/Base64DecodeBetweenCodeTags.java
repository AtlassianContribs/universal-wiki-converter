package com.atlassian.uwc.converters.twiki.cleaners;

import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 */
public class Base64DecodeBetweenCodeTags extends RegularExpressionCleaner {


    public String clean(String twikiText) {
        // Compile the regex.
        String regex = VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING+"(.*?)"+VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING;
        Pattern pattern = Pattern.compile(regex);
        // Get a Matcher based on the target string.
        Matcher matcher = pattern.matcher(twikiText);
        String retString = twikiText;
        // Find all the matches.
        while (matcher.find()) {
            String codeBlockToken = matcher.group(1);
            HashMap cache = Base64EncodeBetweenCodeTags.codeBlockCache;
			// replace with original text
            String bytesToString = "{code}" +
                    cache.get(codeBlockToken) +
                    "{code}";
            int start = matcher.start();
            int end = matcher.end();
            retString = retString.substring(0,start) +
                    bytesToString +
                    retString.substring(end, retString.length());
            // reset the matcher so it will replace the 'next first'
            matcher = pattern.matcher(retString);
        }
        return retString;
    }

    /**
     * Not used since we're overriding clean
     * I'm not sure why the super class doesn't have
     * a default constructor
     */
    public Base64DecodeBetweenCodeTags() {
        // not used...
        super("", "");
    }
}
