package com.atlassian.uwc.ui.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class BadConverter extends BaseConverter {
	Pattern p = Pattern.compile("^(.*)");
	public void convert(Page page) {
		Matcher m = p.matcher(page.getOriginalText());
		if (m.find()) {
			String a = m.group(1);
			String b = m.group(2); //should cause exception
			page.setConvertedText(b);
		}
	}
}
