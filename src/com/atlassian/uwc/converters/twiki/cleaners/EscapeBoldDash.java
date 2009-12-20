package com.atlassian.uwc.converters.twiki.cleaners;

public class EscapeBoldDash extends RegularExpressionCleaner
{
   public EscapeBoldDash()
   {
      super("\\*-\\*",
              "*\\\\-*");
   }
}
