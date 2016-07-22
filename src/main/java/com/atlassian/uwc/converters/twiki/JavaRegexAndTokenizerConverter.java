package com.atlassian.uwc.converters.twiki;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.converters.Converter;
import com.atlassian.uwc.ui.Page;
import com.atlassian.uwc.util.TokenMap;

import java.util.HashMap;

/**
 * This Java RegEx converter handles all the regular expressions whose 'key values'
 * end with .javaregex
 */
public class JavaRegexAndTokenizerConverter extends BaseConverter {

    private static HashMap jregexConverterCache = new HashMap();
    public static final String REGEX_SEPERATOR = "{replace-with}";
    public static final String REGEX_SEPERATOR_MULTI_LINE = "{replace-multiline-with}";
    public static String NEWLINE = null;

    private JavaRegexAndTokenizerConverter() {
    }

    public void convert(Page page) {
        boolean singleLineReplacement = false;
        boolean multiLineReplacement = false;
        //get the regex and replacement
        String javaRegexAndReplacement = getValue();
        String regexSeperator = REGEX_SEPERATOR;
        int sepLoc = javaRegexAndReplacement.indexOf(REGEX_SEPERATOR);
        if (sepLoc > -1) {
            singleLineReplacement = true;
        } else if (sepLoc == -1) {
            regexSeperator = REGEX_SEPERATOR_MULTI_LINE;
            sepLoc = javaRegexAndReplacement.indexOf(REGEX_SEPERATOR_MULTI_LINE);
            multiLineReplacement = true;
        }
        String regex = javaRegexAndReplacement.substring(0, sepLoc);
        String replacement = javaRegexAndReplacement.substring(sepLoc + regexSeperator.length());
        // allow for replacement with newline chars
        if (replacement.contains("NEWLINE")) {
            if (NEWLINE == null) NEWLINE = System.getProperty("line.separator");
            replacement = replacement.replaceAll("NEWLINE", NEWLINE);
        }

        // set this to original text in case the regex is not formed
        // properly and we pass over the replacement if statements
        String converted = page.getOriginalText();
        if (singleLineReplacement) {
            converted = TokenMap.replaceAndTokenize(page.getOriginalText(),
                    regex,
                    replacement);
        } else if (multiLineReplacement) {
            converted = TokenMap.replaceAndTokenizeMultiLine(page.getOriginalText(),
                    regex,
                    replacement);
        }
        page.setConvertedText(converted);
//        // Compile the regex.
//
//        Pattern pattern = Pattern.compile(regex);
//        // Get a Matcher based on the target string.
//        Matcher matcher = pattern.matcher(page.getOriginalText());
//        String converted =  matcher.replaceAll(replacement);
//        page.setConvertedText(converted);
    }

    /**
     * here we're handing back an existing class if it's in the
     * cache and creating it if not.
     *
     * @return
     */
    public static Converter getConverter(String value) {
        if (JavaRegexAndTokenizerConverter.jregexConverterCache.containsKey(value)) {
            return (Converter) JavaRegexAndTokenizerConverter.jregexConverterCache.get(value);
        }
        JavaRegexAndTokenizerConverter instance = new JavaRegexAndTokenizerConverter();
        instance.setValue(value);
        JavaRegexAndTokenizerConverter.jregexConverterCache.put(value, instance);
        return instance;  //To change body of created methods use File | Settings | File Templates.
    }
}
