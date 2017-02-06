package com.atlassian.uwc.converters.twiki.cleaners;

public class BulletListLevel4 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public BulletListLevel4() {
        super("([\t]|[ ]{3,3}){4,4}\\*",
                "\\*\\*\\*\\*");

    }
}
