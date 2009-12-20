package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;

/**
 * In TWiki a camel case word with ! in front gets ignored
 * So !CamelCase  becomes in Confluence {nl}CamelCase{nl}
 */
public class CamelCaseEscape extends RegularExpressionCleaner {
    /**
     * This pattern is for escaping camel case text so that
     * Confluence does not interpret it as such
     * so  !ThisIsNotALink
     * becomes  {nl}ThisIsNotALink{nl}
     */
    public CamelCaseEscape() {
        super("!([A-Z][A-Za-z.0-9]+[A-Z][A-Za-z.0-9]+)(\\n| |$)",
                "{nl}$1{nl}$2");

    }

    public String clean(String twikiText) {
        String partialResult = super.clean(twikiText);
        return partialResult;
    }
}
