package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;

import com.atlassian.uwc.converters.twiki.ContentCleaner;

/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 21, 2005
 * Time: 3:16:29 PM
 */
public abstract class NestedContentCleaner implements ContentCleaner
{
   protected static final int MAX_HEADER_LEVEL_SUPPORTED = 6;

   public String clean(String twikiText)
   {
      String partialResult = twikiText;
      for (int i = MAX_HEADER_LEVEL_SUPPORTED; i > 0; --i)
      {
         partialResult = Pattern.
            compile(getRegularExpression(i), Pattern.MULTILINE).
            matcher(partialResult).replaceAll(getReplacement(i));
      }

      return partialResult;
   }

   protected String getRegularExpression(int i)
   {
      return "^((\\t{" + i + "})+)" + getBulletType() + " ";
   }

   protected String getReplacement(int nestedDepth)
   {
      StringBuffer buffer = new StringBuffer();
      for (int i = 0; i < nestedDepth; ++i)
      {
         buffer.append(getBulletReplacement());
      }
      buffer.append(" ");
      return buffer.toString();
   }

   protected abstract String getBulletType();

   protected abstract String getBulletReplacement();
}
