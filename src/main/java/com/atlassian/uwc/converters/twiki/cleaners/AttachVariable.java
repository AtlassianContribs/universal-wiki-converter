package com.atlassian.uwc.converters.twiki.cleaners;

public class AttachVariable extends RegularExpressionCleaner
{
   public AttachVariable()
   {
      super("%ATTACHURL.*?%/(\\S*)", "^$1"); //XXX What is this for?
   }
}
