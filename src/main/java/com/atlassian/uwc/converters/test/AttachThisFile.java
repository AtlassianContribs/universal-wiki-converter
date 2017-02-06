package com.atlassian.uwc.converters.test;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.atlassian.uwc.converters.BaseConverter;
import com.atlassian.uwc.ui.Page;

public class AttachThisFile extends BaseConverter{

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		Properties props = this.getProperties();
		String path = props.getProperty("attach-this-file", null);
		if (path != null) {
			File file = new File(path);
			if (file.exists() && file.isFile()) {
				log.debug("Attaching file: " + path);
				page.addAttachment(file);
			}
			else log.error("Could not attach file: " + path);
		}
		
	}

}
