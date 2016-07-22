package com.atlassian.uwc.converters.twiki.cleaners;

public class EscapeTheseChars extends RegularExpressionCleaner
{
    /**
     * list characters that should simply be escaped here
     * current list of chars seperated by commas:
     * (, )
     */
   public EscapeTheseChars()
   {
      super("(\\(|\\)|\\{|\\})",
              "\\\\$1");
   }
}
