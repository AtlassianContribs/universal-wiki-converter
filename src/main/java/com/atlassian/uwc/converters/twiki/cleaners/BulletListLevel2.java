package com.atlassian.uwc.converters.twiki.cleaners;

public class BulletListLevel2 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public BulletListLevel2() {
        super("([\t]|[ ]{3,3}){2,2}\\*",
                "\\*\\*");

    }
}
