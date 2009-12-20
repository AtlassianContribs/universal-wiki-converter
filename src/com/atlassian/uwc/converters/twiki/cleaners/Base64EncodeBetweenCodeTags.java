package com.atlassian.uwc.converters.twiki.cleaners;

import org.apache.commons.codec.binary.Base64;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;

/**
 *
 */
public class Base64EncodeBetweenCodeTags extends RegularExpressionCleaner {
    public static HashMap codeBlockCache = new HashMap();
    public static final String CODE_BLOCK_TOKEN = "text_block_token";
    public String clean(String twikiText) {
        // Compile the regex.
        String regex = VerbatimOrCodeTagTokenizer.CODE_TOKEN+"(.*?)"+VerbatimOrCodeTagTokenizer.CODE_TOKEN;
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE|Pattern.DOTALL);
        // Get a Matcher based on the target string.
        Matcher matcher = pattern.matcher(twikiText);
        String retString = twikiText;
        // Find all the matches.
        while (matcher.find()) {
            String textToEncode = matcher.group(1);
            // encode the text
            String keyToken = CODE_BLOCK_TOKEN + codeBlockCache.size()+1; //XXX This is creating strings like text_block_token01. Should the codeBlockCache.size()+1 be surrounded by parens?
            codeBlockCache.put(keyToken,textToEncode);
            String bytesToString = VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING +
                    keyToken +
                    VerbatimOrCodeTagTokenizer.CODE_TOKEN_POST_ENCODING;
            retString = matcher.replaceFirst(bytesToString);

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
    public Base64EncodeBetweenCodeTags() {
        // not used...
        super("", "");
    }
}
