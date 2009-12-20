package com.atlassian.uwc.converters.test;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class TestQMTitle extends BaseConverter {

	
	public void convert(Page page) {
		String title = page.getName();
		title = title.replaceFirst("\\.[^.]*$", "?");
		page.setName(title);
	}

}
