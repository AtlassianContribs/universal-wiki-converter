package com.atlassian.uwc.converters.twiki.cleaners;

/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 14, 2005
 * Time: 2:27:18 PM
 */
public class TableOfContents extends RegularExpressionCleaner
{
   public TableOfContents()
   {
      super("%TOC%",
            "{toc}");
   }

}
