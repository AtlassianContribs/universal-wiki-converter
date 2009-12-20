package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorRed extends RegularExpressionCleaner {
    /**
     * Color Red tag conversion
     */
    public ColorRed() {
        super("%RED%",
                "{color:RED}");

    }
}
