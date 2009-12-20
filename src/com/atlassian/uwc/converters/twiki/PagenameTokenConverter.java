package com.atlassian.uwc.converters.twiki;

import com.atlassian.uwc.ui.Page;

public class PagenameTokenConverter extends VariableConverter {

	public void convert(Page page) {
		String input = page.getOriginalText();
		String converted = convertPagenameToken(input, page.getName());
		page.setConvertedText(converted);
	}

	protected String convertPagenameToken(String input, String name) {
		return replace(input, "~UWCTOKENCURRENTPAGE~", name);
	}
}
