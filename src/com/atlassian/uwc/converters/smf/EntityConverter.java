package com.atlassian.uwc.converters.smf;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class EntityConverter extends BaseConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.debug("Converting Html Entities - Start");
		String input = page.getOriginalText();
		String converted = StringEscapeUtils.unescapeHtml(input);
		page.setConvertedText(converted);
		log.debug("Converting Html Entities - Complete");
	}

}
