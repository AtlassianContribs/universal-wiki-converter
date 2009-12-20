package com.atlassian.uwc.converters.test;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class TestLabelConverter extends BaseConverter {

	public void convert(Page page) {
		page.addLabel("abc");
		page.addLabel("white space");
		page.addLabel("CAPS");
	}

}
