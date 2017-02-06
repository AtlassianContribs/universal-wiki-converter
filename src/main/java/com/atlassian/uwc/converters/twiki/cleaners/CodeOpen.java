package com.atlassian.uwc.converters.twiki.cleaners;

public class CodeOpen extends RegularExpressionCleaner {
    /**
     * Things there are no conversions for
     */
    public CodeOpen() {
        super("<code>",
                "{code}");

    }
}
