package com.atlassian.uwc.converters.twiki.cleaners;

public class RemoveSTARTINCLUDE extends RegularExpressionCleaner
{
    /**
     * remove
     */
   public RemoveSTARTINCLUDE()
   {
      super("%STARTINCLUDE%", "");
   }
}
