package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlItalics extends RegularExpressionCleaner
{
   public HtmlItalics()
   {
      super("<\\s*?/?\\s*?[iI]\\s*?>", "_");
   }
}
