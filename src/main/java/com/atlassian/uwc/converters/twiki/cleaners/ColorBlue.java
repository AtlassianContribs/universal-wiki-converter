package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorBlue extends RegularExpressionCleaner {
    /**
     * Color blue tag conversion
     */
    public ColorBlue() {
        super("%BLUE%",
                "{color:BLUE}");

    }
}
