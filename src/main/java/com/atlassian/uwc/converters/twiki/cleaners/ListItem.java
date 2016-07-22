package com.atlassian.uwc.converters.twiki.cleaners;

public class ListItem extends NestedContentCleaner
{

   protected String getBulletType()
   {
      return "   \\*";
   }

   protected String getBulletReplacement()
   {
      return "*";
   }
}
