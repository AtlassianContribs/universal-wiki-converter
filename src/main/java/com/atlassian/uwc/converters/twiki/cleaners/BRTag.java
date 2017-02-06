package com.atlassian.uwc.converters.twiki.cleaners;

public class BRTag extends RegularExpressionCleaner {
    /**
     * Color Red tag conversion
     */
    public BRTag() {
        super("%BR%",
                "\\\\\\\\");

    }
}
