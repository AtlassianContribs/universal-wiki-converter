package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkSimpleBracketed extends RegularExpressionCleaner
{
    /**
     * not sure about what this does at present,
     * but it looks like it might be simulating
     * links to other spaces? or translates a TWiki anchor
     * into something else?
     */
   public LinkSimpleBracketed()
   {
      super("(\\[\\[)([^\\[\\] ]*?)(\\]\\])",
              "[$2]");
   }
}
