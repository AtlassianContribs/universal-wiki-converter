package com.atlassian.uwc.converters.twiki.cleaners;

import com.atlassian.uwc.converters.twiki.CompositeContentCleaner;

import java.util.ArrayList;
import java.util.List;


public class HtmlBreak extends CompositeContentCleaner
{
   public HtmlBreak()
   {
      super(createCleaners());
   }

   private static List createCleaners()
   {
      List cleaners = new ArrayList();
      cleaners.add(new RegularExpressionCleaner("<\\s*?[bB][rR]\\s*?/?\\s*?>", "\n"));
      cleaners.add(new RegularExpressionCleaner("<\\s*?/?\\s*?[pP]\\s*?/?\\s*?>", "\n"));
      return cleaners;
   }
}
