package com.atlassian.uwc.converters.twiki.cleaners;

import java.util.regex.Pattern;

/**
 *
 */
public class TableColumnSpacer extends RegularExpressionCleaner {

    /**
     * spacing out twiki colspans
     */
    public TableColumnSpacer() {
        super("\\|\\|", "| | ");
    }
}
