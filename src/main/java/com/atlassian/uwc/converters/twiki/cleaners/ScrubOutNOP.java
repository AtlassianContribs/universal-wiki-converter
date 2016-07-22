package com.atlassian.uwc.converters.twiki.cleaners;


public class ScrubOutNOP extends RegularExpressionCleaner {
    /**
     * Things there are no conversions for
     */
    public ScrubOutNOP() {
        super("<nop>",
                "");

    }
}
