package com.atlassian.uwc.converters.twiki.cleaners;

public class Anchor extends com.atlassian.uwc.converters.twiki.RegularExpressionCleaner
{
    /**
     * defining the anchor target (as opposed to what links to the anchor)
     *
     * From TWiki reference:
     * To define an anchor write #AnchorName at the beginning of a line. The anchor name must be a WikiWord.
     */
   public Anchor()
   {
       // this previous regex first param looked more like anchor link
//       super("(?<!\\[)\\#(?!\\S*\\])(\\S*)", "{anchor:$1}");
       super("(\\n)#(\\S*)", "$1{anchor:$2}");
   }
}
