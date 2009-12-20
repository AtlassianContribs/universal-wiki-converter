package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlBold extends RegularExpressionCleaner
{
   public HtmlBold()
   {
      super("<\\s*?/?\\s*?[bB]\\s*?>", "*");
   }
}
