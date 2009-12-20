package com.atlassian.uwc.converters.twiki.cleaners;

public class StripDoctypeTag extends RegularExpressionCleaner {
    /**
     * Things there are no conversions for
     */
    public StripDoctypeTag() {
        super("<!DOCTYPE html .*",
                "");

    }
}
