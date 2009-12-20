package com.atlassian.uwc.converters.twiki;

import java.util.List;
import java.util.Iterator;

public class CompositeContentCleaner implements ContentCleaner
{
   private List cleaners;

   public CompositeContentCleaner(List cleaners)
   {
      this.cleaners = cleaners;
   }

   public String clean(String input)
   {
      String output = input;
      for (Iterator iterator = cleaners.iterator(); iterator.hasNext();)
      {
         ContentCleaner cleaner = (ContentCleaner) iterator.next();
         output = cleaner.clean(output);
      }
      return output;
   }
}
