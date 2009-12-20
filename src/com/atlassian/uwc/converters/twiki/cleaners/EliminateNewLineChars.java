package com.atlassian.uwc.converters.twiki.cleaners;

public class EliminateNewLineChars extends com.atlassian.uwc.converters.twiki.RegularExpressionCleaner
{
    public static String newLine = System.getProperty("line.separator");
   public EliminateNewLineChars()
   {
//       super("([^\\n])(\\n)([^\\n])", "$1 $3");
       super("([^"+newLine+"])("+newLine+")([^"+newLine+"])", "$1 $3");
   }
}
