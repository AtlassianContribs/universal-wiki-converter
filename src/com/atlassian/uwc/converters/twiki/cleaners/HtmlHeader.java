package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.uwc.converters.twiki.CompositeContentCleaner;

public class HtmlHeader extends CompositeContentCleaner
{
   public HtmlHeader()
   {
      super(createCleaners());
   }

   private static List createCleaners()
   {
      List cleaners = new ArrayList();
      cleaners.add(new RegularExpressionCleaner("<\\s*?[hH](\\d)\\s*?>", "h$1. "));
      cleaners.add(new RegularExpressionCleaner("<\\s*?(/[hH]\\d)\\s*?>", "\n"));
      return cleaners;
   }
}
