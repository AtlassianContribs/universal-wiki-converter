package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlBlockQuote extends RegularExpressionCleaner
{
   public HtmlBlockQuote()
   {
      super("<\\s*?/?\\s*?blockquote\\s*?>", "{quote}");
   }
}
