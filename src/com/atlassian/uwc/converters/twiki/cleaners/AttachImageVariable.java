package com.atlassian.uwc.converters.twiki.cleaners;

public class AttachImageVariable extends RegularExpressionCleaner
{
    /** I was going to add this to handle images, but come to think
     * of it a good old PERL regex is going to work fine....leaving this
     * here for now
     * @todo - delete this if not found in a converter.twiki.properties file by fall 2006
     *
     */
   public AttachImageVariable()
   {
      super("%ATTACHURL.*?%/(\\S*)(.jpeg|.JPEG|.jpg|.JPG|.gif|.GIF|.png|.PNG)", "!$1$2!");
   }
}
