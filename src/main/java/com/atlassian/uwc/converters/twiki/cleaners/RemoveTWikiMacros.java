package com.atlassian.uwc.converters.twiki.cleaners;

public class RemoveTWikiMacros extends RegularExpressionCleaner
{
    /**
     * this looks like the start of a pattern
     * to find TWiki macros and variables
     * but isn't really useful.
     */
   public RemoveTWikiMacros()
   {
      super("%META:(.*?)%", "");
   }
}
