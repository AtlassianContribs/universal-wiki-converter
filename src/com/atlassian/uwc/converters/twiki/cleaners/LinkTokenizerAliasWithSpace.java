package com.atlassian.uwc.converters.twiki.cleaners;

import com.atlassian.uwc.util.TokenMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LinkTokenizerAliasWithSpace extends RegularExpressionCleaner
{
    /**
     * [[http://xml.org XML]]
     * [XML|http://xml.org]
     */
   public LinkTokenizerAliasWithSpace()
   {
//      super("\\[\\[([^\\[\\]]*?) ([^\\[\\]]*?)\\]\\]",
//              LinkConstants.OPEN_LINK+
//              "$2"+LinkConstants.DIVIDER+"$1"+
//              LinkConstants.CLOSE_LINK);
        super("","");
   }

    public String clean(String twikiText) {
//        String retString = TokenMap.replaceAndTokenize(twikiText,
//                "\\[\\[([^\\[\\]]*?) ([^\\[\\]]*?)\\]\\]",
//                LinkConstants.OPEN_LINK+
//                "$2"+LinkConstants.DIVIDER+"$1"+
//                LinkConstants.CLOSE_LINK);
        String retString = TokenMap.replaceAndTokenize(twikiText,
                "\\[\\[([^\\[\\]]*?) ([^\\[\\]]*?)\\]\\]",
                "["+
                "$2"+"|"+"$1"+
                "]");
        return retString;
    }



}
