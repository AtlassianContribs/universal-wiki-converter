package com.atlassian.uwc.converters.twiki.cleaners;

public class CodeClose extends RegularExpressionCleaner {
    /**
     * Things there are no conversions for
     */
    public CodeClose() {
        super("</code>",
                "{code}");

    }
}
