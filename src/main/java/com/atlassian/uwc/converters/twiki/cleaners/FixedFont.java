package com.atlassian.uwc.converters.twiki.cleaners;

public class FixedFont extends RegularExpressionCleaner
{

    /**
     * this regex is copied from TWiki.pm, essentially
     * it's saying this fixed font expression has to end with one
     * of the characters in the second matching group ()
     */
   public FixedFont()
   {
            super("=(.*?)=($|[\\s\\,\\.\\;\\:\\!\\?\\)])",
            "{{$1}} $2");
   }
}
