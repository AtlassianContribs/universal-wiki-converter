package com.atlassian.uwc.converters.twiki;

import java.util.regex.Pattern;

import java.util.regex.Pattern;

/**
 * copied from the 'wiki importer' project
 * User: tkmower
 * Date: Mar 11, 2005
 * Time: 4:46:11 PM
 */
public class RegularExpressionCleaner implements  ContentCleaner
{
   private String regularExpression;
   private String replacement;
   private int flags;

   public RegularExpressionCleaner(String regularExpression, String replacement)
   {
      this.regularExpression = regularExpression;
      this.replacement = replacement;
   }

   public RegularExpressionCleaner(String regularExpression, String replacement, int flags)
   {
      this(regularExpression, replacement);

      this.flags = flags;
   }

   public String clean(String twikiText)
   {
      return Pattern.compile(getRegularExpression(), flags).matcher(twikiText).replaceAll(getReplacement());
   }

   public String getRegularExpression()
   {
      return regularExpression;
   }

   public String getReplacement()
   {
      return replacement;
   }
}
