/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;


/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 17, 2005
 * Time: 8:17:22 AM
 */
public class Separator extends RegularExpressionCleaner
{
   public Separator()
   {
      super("^[-]{3,}", "----", Pattern.MULTILINE);
   }
}
