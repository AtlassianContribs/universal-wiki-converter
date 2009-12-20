package com.atlassian.uwc.converters;

import com.atlassian.uwc.ui.Page;

/**
 * A simple converter to chop off the page name extensions
 * For instance a page named MoreFun.txt will become MoreFun when
 * the page is sent to Confluence
 */
public class ChopPageExtensionsConverter extends BaseConverter {
    public void convert(Page page) {
        String pageName = page.getName();
        int extensionLoc = pageName.lastIndexOf(".");
        if (extensionLoc < 0) {
        	log.debug("ChopPageExtensionsConverter: Page has no extension to remove. Skipping.");
        	return;
        }
        pageName = pageName.substring(0,extensionLoc);
        if ("".equals(pageName)) {
        	String error = "ChopPageExtensionsConverter: New pagename for '" + page.getName() + "' would be empty. Skipping.";
			log.error(error);
			addError(Feedback.CONVERTER_ERROR, error, true);
        	return;
        }
        page.setName(pageName);
    }
}
