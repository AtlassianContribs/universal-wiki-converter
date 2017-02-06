package com.atlassian.uwc.converters.twiki.cleaners;

public class EscapeCurlyBrackets1 extends RegularExpressionCleaner
{
   public EscapeCurlyBrackets1()
   {
      super("\\{",
              "\\\\{");
   }
}
