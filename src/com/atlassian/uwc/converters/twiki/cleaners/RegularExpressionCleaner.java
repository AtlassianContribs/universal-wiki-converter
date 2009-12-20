/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;

import com.atlassian.uwc.converters.twiki.ContentCleaner;

/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 11, 2005
 * Time: 4:46:11 PM
 */
public class RegularExpressionCleaner implements  ContentCleaner
{
   protected String regularExpression;
   protected String replacement;
   protected int flags;

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
