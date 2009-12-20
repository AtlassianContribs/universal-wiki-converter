package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkAliasWithSpace extends RegularExpressionCleaner
{
    /**
     * [[http://xml.org XML]]
     * [XML|http://xml.org]
     */
   public LinkAliasWithSpace()
   {
      super("\\[\\[(.*?) (.*?)\\]\\]", "[$2|$1]");
   }
}
