package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorOrange extends RegularExpressionCleaner {
    /**
     * Color Red tag conversion
     */
    public ColorOrange() {
        super("%ORANGE%",
                "{color:ORANGE}");

    }
}
