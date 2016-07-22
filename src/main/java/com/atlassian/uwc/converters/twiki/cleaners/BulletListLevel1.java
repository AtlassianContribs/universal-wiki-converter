package com.atlassian.uwc.converters.twiki.cleaners;

public class BulletListLevel1 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public BulletListLevel1() {
        super("([\t]|[ ]{3,3})\\*",
                "\\*");

    }
}
