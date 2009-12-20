package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorEndtag extends RegularExpressionCleaner {
    /**
     * Color endtag conversion
     */
    public ColorEndtag() {
        super("%ENDCOLOR%",
                "{color}");

    }
}
