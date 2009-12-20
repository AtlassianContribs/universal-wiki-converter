package com.atlassian.uwc.converters.twiki.cleaners;

public class NumberedListItem extends NestedContentCleaner
{

   protected String getBulletType()
   {
      return "1";
   }

   protected String getBulletReplacement()
   {
      return "#";
   }
}
