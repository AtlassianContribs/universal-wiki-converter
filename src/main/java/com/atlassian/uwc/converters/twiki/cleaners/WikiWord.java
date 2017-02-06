package com.atlassian.uwc.converters.twiki.cleaners;

public class WikiWord extends RegularExpressionCleaner
{
   public WikiWord()
   {
      super("(?<!\\S)([A-Z][a-z][\\w\\.]*[A-Z]\\w*)", "[$1]");
   }
}
