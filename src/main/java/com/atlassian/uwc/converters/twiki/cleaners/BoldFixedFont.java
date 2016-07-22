/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.atlassian.uwc.converters.twiki.cleaners;


/**
 * Created by IntelliJ IDEA.
 * User: tkmower
 * Date: Mar 14, 2005
 * Time: 10:37:04 AM
 */
public class BoldFixedFont extends RegularExpressionCleaner {
    public BoldFixedFont() {
//       super("==(\\b.*?\\b)==",
        super("==(.*?)==",
                "{{*$1*}}");
    }
}
