package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlHref extends RegularExpressionCleaner
{
   public HtmlHref()
   {
      super("<\\s*?a\\s+?href\\s*?=\\s*?\"(.*?)\"\\s*?>(.*?)<\\s*?/\\s*?a\\s*?>", "[$2|$1]");
   }
}
