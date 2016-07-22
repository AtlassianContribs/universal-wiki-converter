package com.atlassian.uwc.converters.twiki.cleaners;

/**
 * the twiki <verbatim> tags are used to surround html
 * and other code not to be rendered
 */
public class VerbatimTag extends RegularExpressionCleaner {
    /**
     *
     */
    public VerbatimTag() {
        super("</*verbatim>",
                "{code}");

    }

}
