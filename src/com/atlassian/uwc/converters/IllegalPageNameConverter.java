package com.atlassian.uwc.converters;

import org.apache.log4j.Logger;

import com.atlassian.uwc.ui.Page;

/**
 * @author Laura Kolker
 * handles correcting illegal page names
 */
public class IllegalPageNameConverter extends IllegalNameConverter {

	Logger log = Logger.getLogger(this.getClass());
	public void convert(Page page) {
		log.info("Converting Illegal Page Names - start");
		
		String pagename = page.getName();
		String legal = convertIllegalName(pagename);
		page.setName(legal);
		
		log.info("Converting Illegal Page Names - complete");
	}
	
	public String convertIllegalName(String input) {
		//make note of this pagename for use with link converting
		if (illegal(input)) addIllegalPagename(input); 
		//do the conversion
		return super.convertIllegalName(input);
	}

}
