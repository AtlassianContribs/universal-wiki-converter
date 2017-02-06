package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkTokenizerSimpleBracketed extends RegularExpressionCleaner
{
    /**
     * this converts straight links
     * [[http://www.cnn.com]]
     */
   public LinkTokenizerSimpleBracketed()
   {
      super("(\\[\\[)([^\\[\\] ]*?)(\\]\\])",
              LinkConstants.OPEN_LINK+
              LinkConstants.DIVIDER+"$2"+
              LinkConstants.CLOSE_LINK);
       String javaregex = "(\\[\\[)([^\\[\\] ]*?)(\\]\\]){replace-with}[|$2]";
   }
}
