package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkTokenResolver extends RegularExpressionCleaner
{
    /**
     * Change something like this:
     * [[http://yahoo.com][Yahoo home page]]
     * Resolve tokenized links to:
     * YYYopenLinkYYYhttp://yahoo.comYYYcloseLinkYYYZZZopenLinkTextZZZYahoo home pageZZZcloseLinkText
     */
   public LinkTokenResolver()
   {
      super(LinkConstants.OPEN_LINK+"(.*?)"+
              LinkConstants.DIVIDER+"(.*?)"+
              LinkConstants.CLOSE_LINK,
              "[$1|$2]");
   }
}
