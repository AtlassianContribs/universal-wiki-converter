package com.atlassian.uwc.converters.instiki;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.IllegalNameConverter;
import com.atlassian.uwc.converters.dokuwiki.HierarchyTitleConverter;
import com.atlassian.uwc.ui.Page;

public class TitleConverter extends IllegalNameConverter {

	Logger log = Logger.getLogger(this.getClass());
	@Override
	public void convert(Page page) {
		String input = page.getName();
		String converted = decodeUrl(input);
		converted = converted.replaceFirst("[.]x?html$", "");
		converted = HierarchyTitleConverter.casify(converted);
		log.debug("Page title: " + converted);
		page.setName(converted);
	}

}
