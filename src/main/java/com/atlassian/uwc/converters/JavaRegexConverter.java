package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.Page;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

/**
 * This Java RegEx converter handles all the regular expressions whose 'key values'
 * end with .javaregex
 */
public class JavaRegexConverter extends BaseConverter {

    private static final Logger log = Logger.getLogger(JavaRegexConverter.class);
    private static HashMap jregexConverterCache = new HashMap();
    public static final String REGEX_SEPERATOR = "{replace-with}";
    public static String NEWLINE = null;

    private JavaRegexConverter() {
    }

    public void convert(Page page) {
        //get the regex and replacement
        String javaRegexAndReplacement = getValue();
        int sepLoc = javaRegexAndReplacement.indexOf(REGEX_SEPERATOR);
        String regex = javaRegexAndReplacement.substring(0, sepLoc);
        String replacement = javaRegexAndReplacement.substring(sepLoc+REGEX_SEPERATOR.length());
        // allow for replacement with newline chars
        if (replacement.contains("NEWLINE")) {
            if (NEWLINE==null) NEWLINE = System.getProperty("line.separator");
            replacement = replacement.replaceAll("NEWLINE", NEWLINE);
        }

        // Compile the regex.
        Pattern pattern = Pattern.compile(regex);
        // Get a Matcher based on the target string.
        Matcher matcher = pattern.matcher(page.getOriginalText());
        String converted =  matcher.replaceAll(replacement);

        // Uncomment these lines to debug regexps (but beware -- you will get LOTS of output):
        //log.debug("\n\nConverter: " + javaRegexAndReplacement +
        //          "\nBefore:\n" + page.getOriginalText() +
        //          "\nAfter:\n" + converted + "\n");

        page.setConvertedText(converted);
    }

    /**
     * here we're handing back an existing class if it's in the
     * cache and creating it if not.
     *
     * @return
     */
    public static Converter getConverter(String value) {
        if (jregexConverterCache.containsKey(value)) {
            return (Converter) jregexConverterCache.get(value);
        }
        JavaRegexConverter instance = new JavaRegexConverter();
        instance.setValue(value);
        jregexConverterCache.put(value, instance);
        if (log.isDebugEnabled()) {
//            log.debug("getConverter() created converter " + value);
        }
        return instance;  //To change body of created methods use File | Settings | File Templates.
    }
}
