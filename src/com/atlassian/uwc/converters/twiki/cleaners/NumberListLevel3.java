package com.atlassian.uwc.converters.twiki.cleaners;

public class NumberListLevel3 extends RegularExpressionCleaner {
    /**
     * convert bullet lists
     */
    public NumberListLevel3() {
        super("([\t]|[ ]{3,3}){3,3}1\\.?",
                "###");

    }
}
