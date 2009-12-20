package com.atlassian.uwc.converters.twiki.cleaners;

public class AttachedImage extends RegularExpressionCleaner
{
   public AttachedImage()
   {
      super("<img.*?src=\"%ATTACH.*?%/(.*?)\".*?/.*?>", "!$1!");
   }
}
