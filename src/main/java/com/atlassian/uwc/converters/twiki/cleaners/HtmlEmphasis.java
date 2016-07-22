package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlEmphasis extends RegularExpressionCleaner
{
   public HtmlEmphasis()
   {
      super("<\\s*?/?\\s*?[eE][mM]\\s*?>", "_");
   }
}
