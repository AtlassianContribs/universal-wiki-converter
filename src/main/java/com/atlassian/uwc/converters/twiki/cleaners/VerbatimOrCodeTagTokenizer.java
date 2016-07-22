package com.atlassian.uwc.converters.twiki.cleaners;

/**
 * the twiki <verbatim> tags are used to surround html
 * and other code not to be rendered.
 * also handles <code> and <pre> tags
 */
public class VerbatimOrCodeTagTokenizer extends RegularExpressionCleaner {
    /**
     *
     */
    public final static String CODE_TOKEN = "YYYcodeYYY";
    public final static String CODE_TOKEN_POST_ENCODING = "ZZZcodeZZZ";
    public VerbatimOrCodeTagTokenizer() {
        super("(</*code>|</*verbatim>|</*pre>)",
                CODE_TOKEN);

    }

}
