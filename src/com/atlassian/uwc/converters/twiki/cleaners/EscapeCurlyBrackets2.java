package com.atlassian.uwc.converters.twiki.cleaners;

public class EscapeCurlyBrackets2 extends RegularExpressionCleaner
{
   public EscapeCurlyBrackets2()
   {
      super("\\}",
              "\\\\}");
   }
}
