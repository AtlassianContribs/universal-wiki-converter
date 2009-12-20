package com.atlassian.uwc.converters.twiki.cleaners;

public class Link extends RegularExpressionCleaner
{
    /**
     * not sure about what this does at present,
     * but it looks like it might be simulating
     * links to other spaces? or translates a TWiki anchor
     * into something else?
     */
   public Link()
   {
      super("\\[([\\w]*)\\.([\\w\\#]*)\\]", "[$1:$2]");
   }
}
