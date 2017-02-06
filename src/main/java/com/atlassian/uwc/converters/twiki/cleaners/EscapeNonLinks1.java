package com.atlassian.uwc.converters.twiki.cleaners;

public class EscapeNonLinks1 extends RegularExpressionCleaner
{
    /**
     * x[x xxx x]x  to x\[x xxx x\]x
     * here we are trying to escape square brackets that
     * are not links in Confluence
     */
   public EscapeNonLinks1()
   {
      super("([^\\[])(\\[)([^\\[\\]]+)(\\])([^\\]])",
              "$1\\\\$2$3\\\\$4$5");
   }
}
