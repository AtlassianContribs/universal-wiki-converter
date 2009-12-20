package com.atlassian.uwc.converters.twiki.cleaners;

public class NumberListLevel1 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public NumberListLevel1() {
        super("([\\t]|[ ]{3,3}){1,1}1\\.?",
                "#");

    }
}
