package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.uwc.converters.twiki.CompositeContentCleaner;

public class HtmlCode extends CompositeContentCleaner
{
   public HtmlCode()
   {
      super(createCleaners());
   }

   private static List createCleaners()
   {
      List cleaners = new ArrayList();
      cleaners.add(new RegularExpressionCleaner("<\\s*?\\s*?[cC][oO][dD][eE]\\s*?>", "{{"));
      cleaners.add(new RegularExpressionCleaner("<\\s*?/\\s*?[cC][oO][dD][eE]\\s*?>", "}}"));
      return cleaners;
   }
}
