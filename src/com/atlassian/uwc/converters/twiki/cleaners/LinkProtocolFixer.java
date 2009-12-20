package com.atlassian.uwc.converters.twiki.cleaners;

public class LinkProtocolFixer extends RegularExpressionCleaner
{
    /**
     * Confluence cannot parse this
     * [www.qingdaonews.com.cn/]
     * but it can parse this
     * [http://www.qingdaonews.com.cn/]
     * so basically stick http:// in front of anything that is naked
     * of protocol
     */
   public LinkProtocolFixer()
   {
      super(LinkConstants.OPEN_LINK+"(.*?)"+
              LinkConstants.DIVIDER+"([^HhFf\\^][^TtIi][^TtLl][^PpEe])(.+?\\..+?)"+
              LinkConstants.CLOSE_LINK,
              LinkConstants.OPEN_LINK+
              "$1"+LinkConstants.DIVIDER+"http://$2$3"+
              LinkConstants.CLOSE_LINK);
       String javaregex = "\\[(.*?)\\|([^HhFf\\^][^TtIi][^TtLl][^PpEe])(.+?\\..+?)\\]{replace-with}[$1|http://$2$3\\]";
   }
}
