package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorYellow extends RegularExpressionCleaner {
    /**
     * Color Red tag conversion
     */
    public ColorYellow() {
        super("%YELLOW%",
                "{color:YELLOW}");

    }
}
