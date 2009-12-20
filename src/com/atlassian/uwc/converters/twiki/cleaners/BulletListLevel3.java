package com.atlassian.uwc.converters.twiki.cleaners;

public class BulletListLevel3 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public BulletListLevel3() {
        super("([\t]|[ ]{3,3}){3,3}\\*",
                "\\*\\*\\*");

    }
}
