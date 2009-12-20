/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 14, 2005
 * Time: 9:29:51 AM
 */
public class Header extends RegularExpressionCleaner
{
   public static final int MAX_HEADER_LEVEL_SUPPORTED = 6;

   public String clean(String twikiText)
   {
      String partialResult = super.clean(twikiText);
      for (int i = MAX_HEADER_LEVEL_SUPPORTED; i > 0; --i)
      {
         partialResult = Pattern.
            compile("^h([\\+]{" + i + "})", Pattern.MULTILINE).
            matcher(partialResult).replaceAll("h" + i);
      }
      return partialResult;
   }

   public Header()
   {
      super("^---*([+]+)", "h$1. ", Pattern.MULTILINE);
   }
}
