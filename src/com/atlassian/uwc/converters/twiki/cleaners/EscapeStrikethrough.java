package com.atlassian.uwc.converters.twiki.cleaners;

import org.apache.log4j.Logger;

public class EscapeStrikethrough extends RegularExpressionCleaner
{
    Logger log = Logger.getLogger("EscapeStrikethrough");
   public EscapeStrikethrough()
   {
      super("([ ={}])-(.*?)-([ ={}])",
    		  "$1\\\\\\-$2\\\\-$3");
   }
}
