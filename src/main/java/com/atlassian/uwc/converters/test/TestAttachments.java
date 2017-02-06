package com.atlassian.uwc.converters.test;

import java.io.File;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class TestAttachments extends BaseConverter {

	public void convert(Page page) {
		File[] files = page.getFile().getParentFile().listFiles();
		for (File file : files) {
			if (file.getName().equals(page.getName())) continue;
			page.addAttachment(file);
		}

	}

}
