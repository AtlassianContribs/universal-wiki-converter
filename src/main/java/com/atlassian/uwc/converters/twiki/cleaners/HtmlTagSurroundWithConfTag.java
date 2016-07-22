package com.atlassian.uwc.converters.twiki.cleaners;

public class HtmlTagSurroundWithConfTag extends RegularExpressionCleaner
{
    /**
     * this looks for any html tag like <b> and surrounds with
     * Confluence {html}<b>{html} tags
     */
   public HtmlTagSurroundWithConfTag()
   {
      super("(<|</)(.*?)(>)", "{html}$1$2$3{html}");
   }
}
