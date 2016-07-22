package com.atlassian.uwc.converters.twiki.cleaners;

public class NumberListLevel2 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public NumberListLevel2() {
        super("([\t]|[ ]{3,3}){2,2}1\\.?",
                "##");

    }
}
