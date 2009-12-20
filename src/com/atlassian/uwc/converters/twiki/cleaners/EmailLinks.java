package com.atlassian.uwc.converters.twiki.cleaners;

/**
 * I found this regex here:
 * http://www.sitepoint.com/print/java-regex-api-explained
 *
 * had to modify the front since \\w doesn't match legitimate email
 * chars like -
 */
public class EmailLinks extends RegularExpressionCleaner {
    /**
     *
     */
    public EmailLinks() {
        super("(?:mailto:)?(\\S+)@(\\w+\\.)(\\w+)(\\.\\w+)?",
                "[mailto:$1@$2$3$4]");

    }

}
