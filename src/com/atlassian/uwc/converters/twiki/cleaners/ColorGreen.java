package com.atlassian.uwc.converters.twiki.cleaners;

public class ColorGreen extends RegularExpressionCleaner {
    /**
     * Color Red tag conversion
     */
    public ColorGreen() {
        super("%GREEN%",
                "{color:GREEN}");

    }
}
