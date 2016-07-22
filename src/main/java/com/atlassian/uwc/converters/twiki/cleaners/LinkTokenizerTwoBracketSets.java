package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkTokenizerTwoBracketSets extends RegularExpressionCleaner
{
    /**
     * Change something like this:
     * [[http://yahoo.com][Yahoo home page]]
     * to:
     * YYYopenLinkYYYhttp://yahoo.comYYYcloseLinkYYYZZZopenLinkTextZZZYahoo home pageZZZcloseLinkText
     */
   public LinkTokenizerTwoBracketSets()
   {
      super("\\[\\[(.*?)\\]\\[(.*?)\\]\\]",
              LinkConstants.OPEN_LINK+
              "$2"+LinkConstants.DIVIDER+"$1"+
              LinkConstants.CLOSE_LINK);
       String javaregex = "\\[\\[(.*?)\\]\\[(.*?)\\]\\]{replace-with}[$2|$1]";
   }
}
