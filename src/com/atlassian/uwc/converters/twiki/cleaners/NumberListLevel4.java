package com.atlassian.uwc.converters.twiki.cleaners;

public class NumberListLevel4 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public NumberListLevel4() {
        super("([\t]|[ ]{3,3}){4,4}1\\.?",
                "####");

    }
}
