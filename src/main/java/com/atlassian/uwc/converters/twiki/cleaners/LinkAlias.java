package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkAlias extends RegularExpressionCleaner
{
   public LinkAlias()
   {
      super("\\[\\[(.*?)\\]\\[(.*?)\\]\\]", "[$2|$1]");
   }
}
